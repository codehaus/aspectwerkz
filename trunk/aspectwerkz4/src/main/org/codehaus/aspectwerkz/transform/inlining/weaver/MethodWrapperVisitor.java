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
import org.codehaus.aspectwerkz.reflect.MethodInfo;

/**
 * Creates a public wrapper methods that delegates to the non-public target methods.
 * <p/>
 * TODO: the inner class model is broken since Inner classes are impl thru a java compiler trick
 * with wrapper method (package private static access$100(Outer) etc). We thus need to add equivalent
 * wrapper methods, which means a huge penalty for RW.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public class MethodWrapperVisitor extends ClassAdapter implements TransformationConstants {

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
    public MethodWrapperVisitor(final ClassVisitor cv,
                                final ClassInfo classInfo,
                                final Context ctx,
                                final Set addedMethods) {
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
     * Visits the methods.
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
        if (INIT_METHOD_NAME.equals(name) ||
            CLINIT_METHOD_NAME.equals(name) ||
            name.startsWith(ASPECTWERKZ_PREFIX) ||
            name.startsWith(WRAPPER_METHOD_PREFIX)) {
            //TODO filter on synthetic method ?
            return super.visitMethod(access, name, desc, exceptions, attrs);
        }

        if (Modifier.isPublic(access)) {
            return super.visitMethod(access, name, desc, exceptions, attrs);
        }

        int hash = AsmHelper.calculateMethodHash(name, desc);
        MethodInfo methodInfo = m_classInfo.getMethod(hash);
        if (methodInfo == null) {
            System.err.println(
                    "AW::WARNING " +
                    "metadata structure could not be build for method ["
                    + m_classInfo.getName().replace('/', '.')
                    + '.' + name + ':' + desc + ']'
            );
        }

        ExpressionContext[] ctxs = new ExpressionContext[]{
            new ExpressionContext(PointcutType.EXECUTION, methodInfo, methodInfo),
            new ExpressionContext(PointcutType.CALL, methodInfo, null)
        };

        if (methodFilter(m_ctx.getDefinitions(), ctxs, methodInfo)) {
            return cv.visitMethod(access, name, desc, exceptions, attrs);
        } else {
            m_ctx.markAsAdvised();
            createMethodWrapperMethod(access, name, desc, exceptions, attrs);
            return cv.visitMethod(access, name, desc, exceptions, attrs);
        }
    }


    /**
     * Creates a public wrapper method that delegates to the non-public target method.
     *
     * @param access
     * @param name
     * @param desc
     * @param exceptions
     * @param attrs
     */
    private void createMethodWrapperMethod(final int access,
                                           final String name,
                                           final String desc,
                                           final String[] exceptions,
                                           final Attribute attrs) {
        final String wrapperName = TransformationUtil.getWrapperMethodName(
                name, desc, m_declaringTypeName, INVOKE_WRAPPER_METHOD_PREFIX
        );
        if (m_addedMethods.contains(AlreadyAddedMethodAdapter.getMethodKey(wrapperName, desc))) {
            return;
        }

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
            mv.visitMethodInsn(INVOKESTATIC, m_declaringTypeName, name, desc);
        } else {
            mv.visitVarInsn(ALOAD, 0);
            AsmHelper.loadArgumentTypes(mv, Type.getArgumentTypes(desc), Modifier.isStatic(access));
            mv.visitMethodInsn(INVOKEVIRTUAL, m_declaringTypeName, name, desc);
        }

        AsmHelper.addReturnStatement(mv, Type.getReturnType(desc));

        mv.visitMaxs(0, 0);
    }

    /**
     * Filters out the methods that are not eligible for transformation.
     *
     * @param definitions
     * @param ctxs
     * @param methodInfo
     * @return boolean true if the method should be filtered out
     */
    public static boolean methodFilter(final Set definitions,
                                       final ExpressionContext[] ctxs,
                                       final MethodInfo methodInfo) {
        if (Modifier.isAbstract(methodInfo.getModifiers())
            || Modifier.isNative(methodInfo.getModifiers())
            || methodInfo.getName().equals(INIT_METHOD_NAME)
            || methodInfo.getName().equals(CLINIT_METHOD_NAME)
            || methodInfo.getName().startsWith(ORIGINAL_METHOD_PREFIX)) {
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