/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform.inlining.weaver;

import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.Set;

import org.objectweb.asm.*;
import org.codehaus.aspectwerkz.transform.Context;
import org.codehaus.aspectwerkz.transform.TransformationUtil;
import org.codehaus.aspectwerkz.transform.TransformationConstants;
import org.codehaus.aspectwerkz.transform.inlining.ContextImpl;
import org.codehaus.aspectwerkz.transform.inlining.AsmHelper;
import org.codehaus.aspectwerkz.definition.SystemDefinition;
import org.codehaus.aspectwerkz.expression.ExpressionContext;
import org.codehaus.aspectwerkz.expression.PointcutType;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.FieldInfo;

/**
 * Creates a public wrapper methods that get/set fields
 *
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class FieldWrapperVisitor extends ClassAdapter implements TransformationConstants {

    private final ContextImpl m_ctx;
    private String m_declaringTypeName;
    private final ClassInfo m_classInfo;
    private final Set m_addedMethods;

    /**
     * Creates a new class adapter.
     *
     * @param cv
     * @param classInfo
     * @param ctx
     * @param addedMethods
     */
    public FieldWrapperVisitor(final ClassVisitor cv, final ClassInfo classInfo,
                               final Context ctx, final Set addedMethods) {
        super(cv);
        m_classInfo = classInfo;
        m_ctx = (ContextImpl) ctx;
        m_addedMethods = addedMethods;
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
        m_declaringTypeName = name;
        super.visit(version, access, name, superName, interfaces, sourceFile);
    }

    /**
     * Visits the fields.
     *
     * @param access
     * @param fieldName
     * @param fieldDesc
     * @param value
     * @param attrs
     */
    public void visitField(int access,
                           String fieldName,
                           String fieldDesc,
                           Object value,
                           Attribute attrs) {
        // we need the field anyway
        super.visitField(access, fieldName, fieldDesc, value, attrs);

        if (fieldName.startsWith("class$") || // synthetic field
            fieldName.equals(SERIAL_VERSION_UID_FIELD_NAME)) {  // can have been added by the weaver (not safe)
            return;
        }

        int joinPointHash = AsmHelper.calculateFieldHash(fieldName, fieldDesc);
        FieldInfo fieldInfo = getFieldInfo(m_classInfo, m_declaringTypeName, fieldName, fieldDesc, joinPointHash);

        ExpressionContext[] ctxs = new ExpressionContext[]{
            new ExpressionContext(PointcutType.SET, fieldInfo, null),
            new ExpressionContext(PointcutType.GET, fieldInfo, null)//TODO are we sure that within=null means match
        };

        if (fieldFilter(m_ctx.getDefinitions(), ctxs, fieldInfo)) {
            return;
        } else {
            m_ctx.markAsAdvised();
            createGetFieldWrapperMethod(Modifier.isStatic(access), fieldName, fieldDesc);
            createPutFieldWrapperMethod(Modifier.isStatic(access), fieldName, fieldDesc);
        }
    }


    /**
     * Creates a public wrapper method that delegates to the PUTFIELD instruction of the non-public field.
     * Static method if field is static (PUTSTATIC instr)
     *
     * @param isStaticField
     * @param name
     * @param desc
     */
    private void createPutFieldWrapperMethod(boolean isStaticField,
                                             final String name,
                                             final String desc) {
        String wrapperName = TransformationUtil.getWrapperMethodName(
                name, desc, m_declaringTypeName, PUTFIELD_WRAPPER_METHOD_PREFIX
        );

        StringBuffer signature = new StringBuffer();
        signature.append('(');
        signature.append(desc);
        signature.append(')');
        signature.append('V');

        if (m_addedMethods.contains(AlreadyAddedMethodVisitor.getMethodKey(wrapperName, signature.toString()))) {
            return;
        }

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
            mv.visitFieldInsn(PUTSTATIC, m_declaringTypeName, name, desc);
        } else {
            mv.visitVarInsn(ALOAD, 0);
            AsmHelper.loadArgumentTypes(mv, new Type[]{fieldType}, false);
            mv.visitFieldInsn(PUTFIELD, m_declaringTypeName, name, desc);
        }

        AsmHelper.addReturnStatement(mv, Type.VOID_TYPE);
        mv.visitMaxs(0, 0);
    }


    /**
     * Creates a public wrapper method that delegates to the GETFIELD instruction of the non-public field.
     *
     * @param isStaticField
     * @param name
     * @param desc
     */
    private void createGetFieldWrapperMethod(final boolean isStaticField,
                                             final String name,
                                             final String desc) {
        String wrapperName = TransformationUtil.getWrapperMethodName(
                name, desc, m_declaringTypeName, GETFIELD_WRAPPER_METHOD_PREFIX
        );

        StringBuffer signature = new StringBuffer();
        signature.append('(');
        signature.append(')');
        signature.append(desc);

        if (m_addedMethods.contains(AlreadyAddedMethodVisitor.getMethodKey(wrapperName, signature.toString()))) {
            return;
        }

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
            mv.visitFieldInsn(GETSTATIC, m_declaringTypeName, name, desc);
        } else {
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, m_declaringTypeName, name, desc);
        }

        AsmHelper.addReturnStatement(mv, Type.getType(desc));
        mv.visitMaxs(0, 0);
    }

    /**
     * Returns the field info.
     * FIXME code duplicate with FieldGetSetTF
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
     * @param ctxs
     * @param fieldInfo
     * @return boolean true if the field should be filtered out
     */
    public boolean fieldFilter(final Set definitions,
                               final ExpressionContext[] ctxs,
                               final FieldInfo fieldInfo) {
        if (fieldInfo.getName().startsWith(ORIGINAL_METHOD_PREFIX)) {//FIXME needed ?
            return true;
        }
        for (int i = 0; i < ctxs.length; i++) {
            ExpressionContext ctx = ctxs[i];
            for (Iterator it = definitions.iterator(); it.hasNext();) {
                if (((SystemDefinition) it.next()).hasPointcut(ctx)) {
                    return false;
                } else {
                    continue;
                }
            }
        }
        return true;
    }

}