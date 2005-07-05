/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform.inlining.weaver;

import org.objectweb.asm.Constants;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.CodeVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.Attribute;
import org.codehaus.aspectwerkz.transform.Context;
import org.codehaus.aspectwerkz.transform.TransformationUtil;
import org.codehaus.aspectwerkz.transform.TransformationConstants;
import org.codehaus.aspectwerkz.transform.inlining.ContextImpl;
import org.codehaus.aspectwerkz.transform.inlining.EmittedJoinPoint;
import org.codehaus.aspectwerkz.transform.inlining.AsmHelper;
import org.codehaus.aspectwerkz.joinpoint.management.JoinPointType;

import java.util.List;
import java.util.Iterator;
import java.util.Set;
import java.lang.reflect.Modifier;

/**
 * Adds field and method and ctor wrappers when there has been at least one joinpoint emitted.
 *
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r </a>
 */
public class AddWrapperVisitor extends ClassAdapter implements Constants, TransformationConstants {

    private ContextImpl m_context;

    private Set m_addedMethods;


    public AddWrapperVisitor(ClassVisitor classVisitor, Context context, Set alreadyAddedMethods) {
        super(classVisitor);
        m_context = (ContextImpl) context;
        m_addedMethods = alreadyAddedMethods;
    }

    /**
     * Visits the class.
     *
     * @param access
     * @param name
     * @param superName
     * @param interfaces
     * @param sourceFile
     */
    public void visit(final int version, final int access,
                      final String name,
                      final String superName,
                      final String[] interfaces,
                      final String sourceFile) {
        // iterate on the emitted joinpoints
        // we don't need to filter more since the joinpoint type and the weaving phase did that for us
        List jps = m_context.getEmittedJoinPoints();
        for (Iterator iterator = jps.iterator(); iterator.hasNext();) {
            EmittedJoinPoint emittedJoinPoint = (EmittedJoinPoint) iterator.next();
            int jpType = emittedJoinPoint.getJoinPointType();
            if (Modifier.isPublic(emittedJoinPoint.getCalleeMemberModifiers())
                || !name.equals(emittedJoinPoint.getCalleeClassName())) {//TODO ?
                continue;
            }
            switch (jpType) {
                case (JoinPointType.FIELD_GET_INT) :
                    createGetFieldWrapperMethod(
                            Modifier.isStatic(emittedJoinPoint.getCalleeMemberModifiers()),
                            name,
                            emittedJoinPoint.getCalleeMemberName(),
                            emittedJoinPoint.getCalleeMemberDesc()
                    );
                    break;
                case (JoinPointType.FIELD_SET_INT) :
                        createPutFieldWrapperMethod(
                                Modifier.isStatic(emittedJoinPoint.getCalleeMemberModifiers()),
                                name,
                                emittedJoinPoint.getCalleeMemberName(),
                                emittedJoinPoint.getCalleeMemberDesc()
                        );
                    break;
                case (JoinPointType.METHOD_EXECUTION_INT) :
                case (JoinPointType.METHOD_CALL_INT) :
                    createMethodWrapperMethod(
                            emittedJoinPoint.getCalleeMemberModifiers(),
                            name,
                            emittedJoinPoint.getCalleeMemberName(),
                            emittedJoinPoint.getCalleeMemberDesc(),
                            new String[0],//TODO should throw Throwable ??
                            null//TODO do we need the attr ??
                    );
                    break;
                case (JoinPointType.CONSTRUCTOR_CALL_INT) :
                case (JoinPointType.CONSTRUCTOR_EXECUTION_INT) :
                    createConstructorWrapperMethod(
                            emittedJoinPoint.getCalleeMemberModifiers(),
                            name,
                            emittedJoinPoint.getCalleeMemberName(),
                            emittedJoinPoint.getCalleeMemberDesc(),
                            new String[0],//TODO should throw Throwable ??
                            null//TODO do we need the attr ??
                    );
                    break;
            }
        }

        super.visit(version, access, name, superName, interfaces, sourceFile);
    }

    /**
     * Creates a public wrapper method that delegates to the GETFIELD instruction of the non-public field.
     *
     * @param isStaticField
     * @param declaringTypeName
     * @param name
     * @param desc
     */
    private void createGetFieldWrapperMethod(final boolean isStaticField,
                                             final String declaringTypeName,
                                             final String name,
                                             final String desc) {
        String wrapperName = TransformationUtil.getWrapperMethodName(
                name, desc, declaringTypeName, GETFIELD_WRAPPER_METHOD_PREFIX
        );

        StringBuffer signature = new StringBuffer();
        signature.append('(');
        signature.append(')');
        signature.append(desc);

        final String wrapperKey = AlreadyAddedMethodAdapter.getMethodKey(wrapperName, signature.toString());
        if (m_addedMethods.contains(wrapperKey)) {
            return;
        }
        m_addedMethods.add(wrapperKey);

        int modifiers = ACC_SYNTHETIC;
        if (isStaticField) {
            modifiers |= ACC_STATIC;
        }

        CodeVisitor mv = cv.visitMethod(
                modifiers,
                wrapperName,
                signature.toString(),
                new String[]{},
                null
        );

        if (isStaticField) {
            mv.visitFieldInsn(GETSTATIC, declaringTypeName, name, desc);
        } else {
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, declaringTypeName, name, desc);
        }

        AsmHelper.addReturnStatement(mv, Type.getType(desc));
        mv.visitMaxs(0, 0);
    }

    /**
     * Creates a public wrapper method that delegates to the PUTFIELD instruction of the non-public field.
     * Static method if field is static (PUTSTATIC instr)
     *
     * @param isStaticField
     * @param declaringTypeName
     * @param name
     * @param desc
     */
    private void createPutFieldWrapperMethod(boolean isStaticField,
                                             final String declaringTypeName,
                                             final String name,
                                             final String desc) {
        String wrapperName = TransformationUtil.getWrapperMethodName(
                name, desc, declaringTypeName, PUTFIELD_WRAPPER_METHOD_PREFIX
        );

        StringBuffer signature = new StringBuffer();
        signature.append('(');
        signature.append(desc);
        signature.append(')');
        signature.append('V');

        final String wrapperKey = AlreadyAddedMethodAdapter.getMethodKey(wrapperName, signature.toString());
        if (m_addedMethods.contains(wrapperKey)) {
            return;
        }
        m_addedMethods.add(wrapperKey);

        int modifiers = ACC_SYNTHETIC;
        if (isStaticField) {
            modifiers |= ACC_STATIC;
        }

        CodeVisitor mv = cv.visitMethod(
                modifiers,
                wrapperName,
                signature.toString(),
                new String[]{},
                null
        );

        Type fieldType = Type.getType(desc);
        if (isStaticField) {
            AsmHelper.loadArgumentTypes(mv, new Type[]{fieldType}, true);
            mv.visitFieldInsn(PUTSTATIC, declaringTypeName, name, desc);
        } else {
            mv.visitVarInsn(ALOAD, 0);
            AsmHelper.loadArgumentTypes(mv, new Type[]{fieldType}, false);
            mv.visitFieldInsn(PUTFIELD, declaringTypeName, name, desc);
        }

        AsmHelper.addReturnStatement(mv, Type.VOID_TYPE);
        mv.visitMaxs(0, 0);
    }

    /**
     * Creates a public wrapper method that delegates to the non-public target method.
     *
     * @param access
     * @param declaringTypeName
     * @param name
     * @param desc
     * @param exceptions
     * @param attrs
     */
    private void createMethodWrapperMethod(final int access,
                                           final String declaringTypeName,
                                           final String name,
                                           final String desc,
                                           final String[] exceptions,
                                           final Attribute attrs) {
        final String wrapperName = TransformationUtil.getWrapperMethodName(
                name, desc, declaringTypeName, INVOKE_WRAPPER_METHOD_PREFIX
        );

        final String wrapperKey = AlreadyAddedMethodAdapter.getMethodKey(wrapperName, desc);
        if (m_addedMethods.contains(wrapperKey)) {
            return;
        }
        m_addedMethods.add(wrapperKey);

        int modifiers = ACC_SYNTHETIC;
        if (Modifier.isStatic(access)) {
            modifiers |= ACC_STATIC;
        }

        CodeVisitor mv = super.visitMethod(
                modifiers,
                wrapperName,
                desc,
                exceptions,
                attrs
        );

        if (Modifier.isStatic(access)) {
            AsmHelper.loadArgumentTypes(mv, Type.getArgumentTypes(desc), Modifier.isStatic(access));
            mv.visitMethodInsn(INVOKESTATIC, declaringTypeName, name, desc);
        } else {
            mv.visitVarInsn(ALOAD, 0);
            AsmHelper.loadArgumentTypes(mv, Type.getArgumentTypes(desc), Modifier.isStatic(access));
            mv.visitMethodInsn(INVOKEVIRTUAL, declaringTypeName, name, desc);
        }

        AsmHelper.addReturnStatement(mv, Type.getReturnType(desc));

        mv.visitMaxs(0, 0);
    }

    /**
     * Creates a public wrapper static method that delegates to the non-public target ctor.
     *
     * @param access
     * @param declaringTypeName
     * @param name
     * @param desc
     * @param exceptions
     * @param attrs
     */
    private void createConstructorWrapperMethod(final int access,
                                           final String declaringTypeName,
                                           final String name,
                                           final String desc,
                                           final String[] exceptions,
                                           final Attribute attrs) {
        final String wrapperName = TransformationUtil.getWrapperMethodName(
                name, desc, declaringTypeName, INVOKE_WRAPPER_METHOD_PREFIX
        );

        final String wrapperKey = AlreadyAddedMethodAdapter.getMethodKey(wrapperName, desc);
        if (m_addedMethods.contains(wrapperKey)) {
            return;
        }
        m_addedMethods.add(wrapperKey);

        int modifiers = ACC_SYNTHETIC;
        modifiers |= ACC_STATIC;

        Type declaringType = Type.getType('L'+declaringTypeName+';');
        String ctorDesc = Type.getMethodDescriptor(declaringType, Type.getArgumentTypes(desc));

        CodeVisitor mv = super.visitMethod(
                modifiers,
                wrapperName,
                ctorDesc,
                exceptions,
                attrs
        );

        mv.visitTypeInsn(NEW, declaringTypeName);
        mv.visitInsn(DUP);
        AsmHelper.loadArgumentTypes(mv, Type.getArgumentTypes(desc), true);
        mv.visitMethodInsn(INVOKESPECIAL, declaringTypeName, name, desc);
        AsmHelper.addReturnStatement(mv, declaringType);

        mv.visitMaxs(0, 0);
    }

}
