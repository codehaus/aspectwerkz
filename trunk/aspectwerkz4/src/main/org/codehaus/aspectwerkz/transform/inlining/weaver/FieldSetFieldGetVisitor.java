/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform.inlining.weaver;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.CodeVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Constants;
import org.objectweb.asm.CodeAdapter;
import org.objectweb.asm.Type;
import org.codehaus.aspectwerkz.definition.SystemDefinition;
import org.codehaus.aspectwerkz.expression.ExpressionContext;
import org.codehaus.aspectwerkz.expression.PointcutType;
import org.codehaus.aspectwerkz.joinpoint.management.JoinPointType;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.FieldInfo;
import org.codehaus.aspectwerkz.reflect.MemberInfo;
import org.codehaus.aspectwerkz.reflect.impl.asm.AsmClassInfo;
import org.codehaus.aspectwerkz.transform.Context;
import org.codehaus.aspectwerkz.transform.TransformationUtil;
import org.codehaus.aspectwerkz.transform.TransformationConstants;
import org.codehaus.aspectwerkz.transform.inlining.compiler.AbstractJoinPointCompiler;
import org.codehaus.aspectwerkz.transform.inlining.ContextImpl;
import org.codehaus.aspectwerkz.transform.inlining.AsmHelper;
import org.codehaus.aspectwerkz.transform.inlining.EmittedJoinPoint;

import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Instruments method SET and GET join points by replacing PUTFIELD and GETFIELD instructions with invocations
 * of the compiled join point.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public class FieldSetFieldGetVisitor extends ClassAdapter implements TransformationConstants {

    private final ContextImpl m_ctx;
    private final ClassLoader m_loader;
    private final ClassInfo m_callerClassInfo;

    /**
     * Creates a new instance.
     *
     * @param cv
     * @param loader
     * @param classInfo
     * @param ctx
     */
    public FieldSetFieldGetVisitor(final ClassVisitor cv,
                                   final ClassLoader loader,
                                   final ClassInfo classInfo,
                                   final Context ctx) {
        super(cv);
        m_loader = loader;
        m_callerClassInfo = classInfo;
        m_ctx = (ContextImpl) ctx;
    }

    /**
     * Visits the caller methods.
     *
     * @param access
     * @param name
     * @param desc
     * @param exceptions
     * @param attrs
     * @return
     */
    public CodeVisitor visitMethod(final int access,
                                   final String name,
                                   final String desc,
                                   final String[] exceptions,
                                   final Attribute attrs) {

        // TODO - support withincode <clinit> for static fields
        if (CLINIT_METHOD_NAME.equals(name) ||
            name.startsWith(WRAPPER_METHOD_PREFIX)) {
            return super.visitMethod(access, name, desc, exceptions, attrs);
        }

        CodeVisitor mv = cv.visitMethod(access, name, desc, exceptions, attrs);
        return mv == null ? null : new ReplacePutFieldAndGetFieldInstructionCodeAdapter(
                mv,
                m_loader,
                m_callerClassInfo,
                m_ctx.getClassName(),
                name,
                desc
        );
    }

    /**
     * Replaces PUTFIELD and GETFIELD instructions with a call to the compiled JoinPoint instance.
     *
     * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
     */
    public class ReplacePutFieldAndGetFieldInstructionCodeAdapter extends CodeAdapter {

        private final ClassLoader m_loader;
        private final ClassInfo m_callerClassInfo;
        private final String m_callerClassName;
        private final String m_callerMethodName;
        private final String m_callerMethodDesc;
        private final MemberInfo m_callerMemberInfo;

        /**
         * Creates a new instance.
         *
         * @param ca
         * @param loader
         * @param callerClassInfo
         * @param callerClassName
         * @param callerMethodName
         * @param callerMethodDesc
         */
        public ReplacePutFieldAndGetFieldInstructionCodeAdapter(final CodeVisitor ca,
                                                                final ClassLoader loader,
                                                                final ClassInfo callerClassInfo,
                                                                final String callerClassName,
                                                                final String callerMethodName,
                                                                final String callerMethodDesc) {
            super(ca);

            m_loader = loader;
            m_callerClassInfo = callerClassInfo;
            m_callerClassName = callerClassName;
            m_callerMethodName = callerMethodName;
            m_callerMethodDesc = callerMethodDesc;

            if (INIT_METHOD_NAME.equals(m_callerMethodName)) {
                m_callerMemberInfo =
                m_callerClassInfo.getConstructor(AsmHelper.calculateConstructorHash(m_callerMethodDesc));
            } else {
                m_callerMemberInfo =
                m_callerClassInfo.getMethod(AsmHelper.calculateMethodHash(m_callerMethodName, m_callerMethodDesc));
            }

            if (m_callerMemberInfo == null) {
                throw new Error(
                        "caller method info metadata structure could not be build for method: "
                        + callerClassName
                        + '.'
                        + callerMethodName
                        + ':'
                        + callerMethodDesc
                );
            }
        }

        /**
         * Visits PUTFIELD and GETFIELD instructions.
         *
         * @param opcode
         * @param className
         * @param fieldName
         * @param fieldDesc
         */
        public void visitFieldInsn(final int opcode,
                                   final String className,
                                   final String fieldName,
                                   final String fieldDesc) {

            if (className.endsWith(AbstractJoinPointCompiler.JOIN_POINT_CLASS_SUFFIX) ||
                fieldName.startsWith(ASPECTWERKZ_PREFIX) ||
                className.startsWith(ASPECTWERKZ_PACKAGE_NAME) ||
                fieldName.startsWith("class$") || // synthetic field
                fieldName.equals(SERIAL_VERSION_UID_FIELD_NAME) // can have been added by the weaver (not safe)
            ) {
                super.visitFieldInsn(opcode, className, fieldName, fieldDesc);
                return;
            }

            Type fieldType = Type.getType(fieldDesc);

            int joinPointHash = AsmHelper.calculateFieldHash(fieldName, fieldDesc);
            ClassInfo classInfo = AsmClassInfo.getClassInfo(className.replace('/', '.'), m_loader);
            FieldInfo fieldInfo = getFieldInfo(classInfo, className, fieldName, fieldDesc, joinPointHash);

            if (opcode == PUTFIELD || opcode == PUTSTATIC) {
                ExpressionContext ctx = new ExpressionContext(PointcutType.SET, fieldInfo, m_callerMemberInfo);

                if (fieldFilter(m_ctx.getDefinitions(), ctx, fieldInfo)) {
                    super.visitFieldInsn(opcode, className, fieldName, fieldDesc);
                } else {
                    m_ctx.markAsAdvised();

                    String joinPointClassName = TransformationUtil.getJoinPointClassName(
                            m_callerClassName,
                            className,
                            JoinPointType.FIELD_SET,
                            joinPointHash
                    );

                    // load the caller instance (this), or null if in a static context
                    // note that callee instance [optional] and args are already on the stack
                    if (Modifier.isStatic(m_callerMemberInfo.getModifiers())) {
                        visitInsn(ACONST_NULL);
                    } else {
                        visitVarInsn(ALOAD, 0);
                    }

                    // add the call to the join point
                    super.visitMethodInsn(
                            INVOKESTATIC,
                            joinPointClassName,
                            INVOKE_METHOD_NAME,
                            TransformationUtil.getInvokeSignatureForFieldJoinPoints(
                                    fieldInfo.getModifiers(), fieldDesc, m_callerClassName, className
                            )
                    );
                    super.visitInsn(POP);// field is set by the JP

                    // emit the joinpoint
                    m_ctx.addEmittedJoinPoint(
                            new EmittedJoinPoint(
                                    JoinPointType.FIELD_SET,
                                    m_callerClassName,
                                    m_callerMethodName,
                                    m_callerMethodDesc,
                                    m_callerMemberInfo.getModifiers(),
                                    className,
                                    fieldName,
                                    fieldDesc,
                                    fieldInfo.getModifiers(),
                                    joinPointHash,
                                    joinPointClassName
                            )
                    );
                }
            } else if (opcode == GETFIELD || opcode == GETSTATIC) {
                ExpressionContext ctx = new ExpressionContext(PointcutType.GET, fieldInfo, m_callerMemberInfo);

                if (fieldFilter(m_ctx.getDefinitions(), ctx, fieldInfo)) {
                    super.visitFieldInsn(opcode, className, fieldName, fieldDesc);
                } else {
                    m_ctx.markAsAdvised();

                    String joinPointClassName = TransformationUtil.getJoinPointClassName(
                            m_callerClassName,
                            className,
                            JoinPointType.FIELD_GET,
                            joinPointHash
                    );

                    // if static context pop the 'this' instance and load NULL
                    if (Modifier.isStatic(m_callerMemberInfo.getModifiers())) {
                        visitInsn(ACONST_NULL);
                    }

                    // no param to field, so pass a default value to the invoke method
                    AsmHelper.addDefaultValue(this, fieldType);

                    // if static context load NULL else 'this'
                    if (Modifier.isStatic(m_callerMemberInfo.getModifiers())) {
                        visitInsn(ACONST_NULL);
                    } else {
                        visitVarInsn(ALOAD, 0);
                    }

                    // add the call to the join point
                    super.visitMethodInsn(
                            INVOKESTATIC,
                            joinPointClassName,
                            INVOKE_METHOD_NAME,
                            TransformationUtil.getInvokeSignatureForFieldJoinPoints(
                                    fieldInfo.getModifiers(), fieldDesc, m_callerClassName, className
                            )
                    );
                    //super.visitInsn(POP);//pop the field value returned from jp.invoke for now

                    // emit the joinpoint
                    m_ctx.addEmittedJoinPoint(
                            new EmittedJoinPoint(
                                    JoinPointType.FIELD_GET,
                                    m_callerClassName,
                                    m_callerMethodName,
                                    m_callerMethodDesc,
                                    m_callerMemberInfo.getModifiers(),
                                    className,
                                    fieldName,
                                    fieldDesc,
                                    fieldInfo.getModifiers(),
                                    joinPointHash,
                                    joinPointClassName
                            )
                    );
                }
            } else {
                super.visitFieldInsn(opcode, className, fieldName, fieldDesc);
            }
        }

        /**
         * Returns the field info.
         *
         * @param classInfo
         * @param className
         * @param fieldName
         * @param fieldDesc
         * @param joinPointHash
         * @return
         */
        private FieldInfo getFieldInfo(final ClassInfo classInfo,
                                       final String className,
                                       final String fieldName,
                                       final String fieldDesc,
                                       final int joinPointHash) {
            FieldInfo fieldInfo = classInfo.getField(joinPointHash);
            if (fieldInfo == null) {
                // lookup in the class hierarchy
                ClassInfo superClassInfo = classInfo.getSuperclass();
                while (superClassInfo != null) {
                    fieldInfo = superClassInfo.getField(joinPointHash);
                    if (fieldInfo == null) {
                        // go up in the hierarchy
                        superClassInfo = superClassInfo.getSuperclass();
                    } else {
                        break;
                    }
                }
                if (fieldInfo == null) {
                    throw new Error(
                            "field info metadata structure could not be build for field: "
                            + className
                            + '.'
                            + fieldName
                            + ':'
                            + fieldDesc
                    );
                }
            }
            return fieldInfo;
        }

        /**
         * Filters out the fields that are not eligible for transformation.
         *
         * @param definitions
         * @param ctx
         * @param fieldInfo
         * @return boolean true if the field should be filtered out
         */
        public boolean fieldFilter(final Set definitions,
                                   final ExpressionContext ctx,
                                   final FieldInfo fieldInfo) {
            if (fieldInfo.getName().startsWith(ORIGINAL_METHOD_PREFIX)) {
                return true;
            }
            for (Iterator it = definitions.iterator(); it.hasNext();) {
                if (((SystemDefinition) it.next()).hasPointcut(ctx)) {
                    return false;
                } else {
                    continue;
                }
            }
            return true;
        }
    }
}