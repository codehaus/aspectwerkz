/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
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
import org.codehaus.aspectwerkz.transform.inlining.EmittedJoinPoint;
import org.codehaus.aspectwerkz.joinpoint.management.JoinPointType;
import org.codehaus.aspectwerkz.definition.SystemDefinition;
import org.codehaus.aspectwerkz.expression.ExpressionContext;
import org.codehaus.aspectwerkz.expression.PointcutType;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.MethodInfo;
import org.codehaus.aspectwerkz.intercept.AdvisableImpl;

/**
 * Adds a "proxy method" to the methods that matches an <tt>execution</tt> pointcut as well as prefixing the "original
 * method".
 * <br/>
 * The proxy method calls the JPClass.invoke static method. The signature of the invoke method depends if the
 * target method is static or not as follow:
 * <pre>
 *      invoke(callee, args.., caller) // non static
 *      invoke(args.., caller) // static
 * </pre>
 * (The reason why is that it simplifies call pointcut stack management)
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r </a>
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class MethodExecutionVisitor extends ClassAdapter implements TransformationConstants {

    private final ClassInfo m_classInfo;
    private final ContextImpl m_ctx;
    private String m_declaringTypeName;
    private final Set m_addedMethods;

    /**
     * Creates a new class adapter.
     *
     * @param cv
     * @param classInfo
     * @param ctx
     * @param addedMethods
     */
    public MethodExecutionVisitor(final ClassVisitor cv,
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
    public void visit(final int version,
                      final int access,
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
            name.startsWith(SYNTHETIC_MEMBER_PREFIX) ||
            name.startsWith(WRAPPER_METHOD_PREFIX) ||
            (AdvisableImpl.ADD_ADVICE_METHOD_NAME.equals(name) && AdvisableImpl.ADD_ADVICE_METHOD_DESC.equals(desc)) ||
            (AdvisableImpl.REMOVE_ADVICE_METHOD_NAME.equals(name) && AdvisableImpl.REMOVE_ADVICE_METHOD_DESC.equals(desc))) {
            return cv.visitMethod(access, name, desc, exceptions, attrs);
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
            // bail out
            return cv.visitMethod(access, name, desc, exceptions, attrs);
        }

        ExpressionContext ctx = new ExpressionContext(PointcutType.EXECUTION, methodInfo, methodInfo);

        if (methodFilter(m_ctx.getDefinitions(), ctx, methodInfo)) {
            return cv.visitMethod(access, name, desc, exceptions, attrs);
        } else {
            String prefixedOriginalName = TransformationUtil.getPrefixedOriginalMethodName(name, m_declaringTypeName);
            if (m_addedMethods.contains(AlreadyAddedMethodAdapter.getMethodKey(prefixedOriginalName, desc))) {
                return cv.visitMethod(access, name, desc, exceptions, attrs);
            }

            m_ctx.markAsAdvised();

            // create the proxy for the original method
            createProxyMethod(access, name, desc, exceptions, attrs, methodInfo);

            int modifiers = ACC_SYNTHETIC;
            if (Modifier.isStatic(access)) {
                modifiers |= ACC_STATIC;
            }
            // prefix the original method
            return cv.visitMethod(
                    modifiers,
                    prefixedOriginalName,
                    desc, exceptions, attrs
            );
        }
    }

    /**
     * Creates the "proxy method", e.g. the method that has the same name and signature as the original method but a
     * completely other implementation.
     *
     * @param access
     * @param name
     * @param desc
     * @param exceptions
     * @param attrs
     */
    private void createProxyMethod(final int access,
                                   final String name,
                                   final String desc,
                                   final String[] exceptions,
                                   final Attribute attrs,
                                   final MethodInfo methodInfo) {
        CodeVisitor mv = cv.visitMethod(access, name, desc, exceptions, attrs);

        // load "this" ie callee if target method is not static
        if (!Modifier.isStatic(access)) {
            mv.visitVarInsn(ALOAD, 0);
        }
        // load args
        AsmHelper.loadArgumentTypes(mv, Type.getArgumentTypes(desc), Modifier.isStatic(access));
        // load "this" ie caller or null if method is static
        if (Modifier.isStatic(access)) {
            mv.visitInsn(ACONST_NULL);
        } else {
            mv.visitVarInsn(ALOAD, 0);
        }

        int joinPointHash = AsmHelper.calculateMethodHash(name, desc);
        String joinPointClassName = TransformationUtil.getJoinPointClassName(
                m_declaringTypeName,
                name,
                desc,
                m_declaringTypeName,
                JoinPointType.METHOD_EXECUTION_INT,
                joinPointHash
        );

        // TODO: should we provide some sort of option to do JITgen when weaving instead of when loading ?
        // use case: offline full packaging and alike

        mv.visitMethodInsn(
                INVOKESTATIC,
                joinPointClassName,
                INVOKE_METHOD_NAME,
                TransformationUtil.getInvokeSignatureForCodeJoinPoints(
                        access, desc, m_declaringTypeName, m_declaringTypeName
                )
        );

        AsmHelper.addReturnStatement(mv, Type.getReturnType(desc));
        mv.visitMaxs(0, 0);

        // emit the joinpoint
        m_ctx.addEmittedJoinPoint(
                new EmittedJoinPoint(
                        JoinPointType.METHOD_EXECUTION_INT,
                        m_declaringTypeName,
                        name,
                        desc,
                        access,
                        m_declaringTypeName,
                        name,
                        desc,
                        access,
                        joinPointHash,
                        joinPointClassName,
                        EmittedJoinPoint.NO_LINE_NUMBER
                )
        );
    }

    /**
     * Filters out the methods that are not eligible for transformation.
     *
     * @param definitions
     * @param ctx
     * @param methodInfo
     * @return boolean true if the method should be filtered out
     */
    public static boolean methodFilter(final Set definitions,
                                       final ExpressionContext ctx,
                                       final MethodInfo methodInfo) {
        if (Modifier.isAbstract(methodInfo.getModifiers())
            || Modifier.isNative(methodInfo.getModifiers())
            || methodInfo.getName().equals(INIT_METHOD_NAME)
            || methodInfo.getName().equals(CLINIT_METHOD_NAME)
            || methodInfo.getName().startsWith(ORIGINAL_METHOD_PREFIX)) {
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