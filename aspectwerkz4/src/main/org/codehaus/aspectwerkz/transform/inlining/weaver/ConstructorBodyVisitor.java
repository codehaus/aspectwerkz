/**************************************************************************************
 * Copyright (c) Jonas Bon?r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform.inlining.weaver;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.Constants;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.CodeVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Type;
import org.objectweb.asm.CodeAdapter;
import org.objectweb.asm.Label;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.ConstructorInfo;
import org.codehaus.aspectwerkz.transform.Context;
import org.codehaus.aspectwerkz.transform.TransformationUtil;
import org.codehaus.aspectwerkz.transform.TransformationConstants;
import org.codehaus.aspectwerkz.transform.inlining.ContextImpl;
import org.codehaus.aspectwerkz.transform.inlining.AsmHelper;
import org.codehaus.aspectwerkz.transform.inlining.EmittedJoinPoint;
import org.codehaus.aspectwerkz.expression.ExpressionContext;
import org.codehaus.aspectwerkz.expression.PointcutType;
import org.codehaus.aspectwerkz.definition.SystemDefinition;
import org.codehaus.aspectwerkz.joinpoint.management.JoinPointType;

import java.util.Iterator;
import java.util.Set;

/**
 * Handles constructor execution weaving.
 * For each matching ctor, a static method is added with the same signature and with the extra thisClass parameter
 * prepended to the list. Then the orginal ctor body is changed to call the JP.invoke, only after call to this / super
 * initializers.
 * <p/>
 * TODO rename in ..execution..
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class ConstructorBodyVisitor extends ClassAdapter implements TransformationConstants {

    private final ContextImpl m_ctx;
    private final ClassInfo m_calleeClassInfo;
    private String m_declaringTypeName;
    private Set m_addedMethods;

    /**
     * Creates a new instance.
     *
     * @param cv
     * @param classInfo
     * @param ctx
     * @param addedMethods
     */
    public ConstructorBodyVisitor(final ClassVisitor cv,
                                  final ClassInfo classInfo,
                                  final Context ctx,
                                  final Set addedMethods) {
        super(cv);
        m_calleeClassInfo = classInfo;
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
     * @param access
     * @param name
     * @param desc
     * @param exceptions
     * @param attrs
     * @return
     */
    public CodeVisitor visitMethod(int access,
                                   String name,
                                   String desc,
                                   String[] exceptions,
                                   Attribute attrs) {
        if (!INIT_METHOD_NAME.equals(name)) {
            return super.visitMethod(access, name, desc, exceptions, attrs);
        }

        int hash = AsmHelper.calculateConstructorHash(desc);
        ConstructorInfo constructorInfo = m_calleeClassInfo.getConstructor(hash);
        if (constructorInfo == null) {
            System.err.println(
                    "AW::WARNING " +
                    "metadata structure could not be build for constructor ["
                    + m_calleeClassInfo.getName().replace('/', '.')
                    + ".<init>: " + desc + ']'
            );
            return cv.visitMethod(access, name, desc, exceptions, attrs);
        }

        ExpressionContext ctx = new ExpressionContext(PointcutType.EXECUTION, constructorInfo, constructorInfo);

        if (constructorFilter(m_ctx.getDefinitions(), ctx)) {
            return cv.visitMethod(access, name, desc, exceptions, attrs);
        } else {
            String wrapperName = TransformationUtil.getConstructorBodyMethodName(m_declaringTypeName);
            String wrapperDesc = TransformationUtil.getConstructorBodyMethodSignature(desc, m_declaringTypeName);
            if (m_addedMethods.contains(AlreadyAddedMethodAdapter.getMethodKey(wrapperName, wrapperDesc))) {
                return cv.visitMethod(access, name, desc, exceptions, attrs);
            }

            m_ctx.markAsAdvised();

            // create the proxy constructor for the original constructor
            CodeVisitor proxyCtorCodeVisitor = cv.visitMethod(access, name, desc, exceptions, attrs);
            // create the ctorBodyMethod for the original constructor body
            int modifiers = ACC_SYNTHETIC | ACC_STATIC;
            CodeVisitor ctorBodyMethodCodeVisitor = cv.visitMethod(
                    modifiers, wrapperName, wrapperDesc, exceptions, attrs
            );

            // return a dispatch Code Adapter in between the orginal one and both of them
            return new DispatchCtorBodyCodeAdapter(proxyCtorCodeVisitor, ctorBodyMethodCodeVisitor, access, desc);
        }
    }


    /**
     * Creates the "proxy constructor" join point invocation body
     *
     * @param ctorProxy
     * @param access
     * @param desc
     */
    private void insertJoinPointInvoke(final CodeVisitor ctorProxy,
                                       final int access,
                                       final String desc) {
        // load "this"
        ctorProxy.visitVarInsn(ALOAD, 0);// is too simple f.e. when DUP was used
        // load args
        AsmHelper.loadArgumentTypes(ctorProxy, Type.getArgumentTypes(desc), false);

        // caller = callee
        ctorProxy.visitVarInsn(ALOAD, 0);

        int joinPointHash = AsmHelper.calculateConstructorHash(desc);
        String joinPointClassName = TransformationUtil.getJoinPointClassName(
                m_declaringTypeName,
                INIT_METHOD_NAME,
                desc,
                m_declaringTypeName,
                JoinPointType.CONSTRUCTOR_EXECUTION_INT,
                joinPointHash
        );

        ctorProxy.visitMethodInsn(
                INVOKESTATIC,
                joinPointClassName,
                INVOKE_METHOD_NAME,
                TransformationUtil.getInvokeSignatureForCodeJoinPoints(
                        access, desc, m_declaringTypeName, m_declaringTypeName
                )
        );

        // emit the joinpoint
        m_ctx.addEmittedJoinPoint(
                new EmittedJoinPoint(
                        JoinPointType.CONSTRUCTOR_EXECUTION_INT,
                        m_declaringTypeName,
                        TransformationConstants.INIT_METHOD_NAME,
                        desc,
                        access,
                        m_declaringTypeName,
                        TransformationConstants.INIT_METHOD_NAME,
                        desc,
                        access,
                        joinPointHash,
                        joinPointClassName,
                        EmittedJoinPoint.NO_LINE_NUMBER
                )
        );
    }

    /**
     * Filters out the constructor that are not eligible for transformation.
     *
     * @param definitions
     * @param ctx
     * @return boolean true if the constructor should be filtered out
     */
    public static boolean constructorFilter(final Set definitions,
                                            final ExpressionContext ctx) {
        for (Iterator it = definitions.iterator(); it.hasNext();) {
            if (((SystemDefinition) it.next()).hasPointcut(ctx)) {
                return false;
            } else {
                continue;
            }
        }
        return true;
    }

    /**
     * A class that dispatch the ctor body instruction to any other given code visitor
     * </p>
     * The behavior is like this:
     * 1/ as long as the INVOKESPECIAL for the object initialization has not been reached, every bytecode
     * instruction is dispatched in the ctor code visitor. [note 1]
     * 2/ when this one is reached, it is only added in the ctor code visitor and a JP invoke is added
     * 3/ after that, only the other code visitor receives the instructions
     * </p>
     * [note 1] To support schemes like http://java.sun.com/docs/books/vmspec/2nd-edition/html/ClassFile.doc.html#9839
     * where the stack is like ALOAD_0 + DUP, we handle a special case.
     * f.e. CGlib proxy ctor are like that..
     * Don't know if some other info can be left on the stack (f.e. ILOAD 1, DUP ...)
     */
    private class DispatchCtorBodyCodeAdapter extends CodeAdapter {
        private CodeVisitor m_ctorBodyMethodCodeVisitor;
        private CodeVisitor m_proxyCtorCodeVisitor;
        private final int m_constructorAccess;
        private final String m_constructorDesc;

        private int m_newCount = 0;

        private boolean m_proxyCtorCodeDone = false;
        private boolean m_isALOADDUPHeuristic = false;
        private int m_index = -1;

        public DispatchCtorBodyCodeAdapter(CodeVisitor proxyCtor, CodeVisitor ctorBodyMethod, final int access,
                                           final String desc) {
            super(proxyCtor);
            m_proxyCtorCodeVisitor = proxyCtor;
            m_ctorBodyMethodCodeVisitor = ctorBodyMethod;
            m_constructorAccess = access;
            m_constructorDesc = desc;
        }

        public void visitInsn(int opcode) {
            super.visitInsn(opcode);
            if (!m_proxyCtorCodeDone && opcode == DUP && m_index == 0) {
                // heuristic for ALOAD_0 + DUP confirmed
                m_isALOADDUPHeuristic = true;
                m_index++;
            }
        }

        public void visitIntInsn(int i, int i1) {
            super.visitIntInsn(i, i1);
        }

        public void visitVarInsn(int opcode, int i1) {
            super.visitVarInsn(opcode, i1);
            if (!m_proxyCtorCodeDone) {
                if (opcode == ALOAD && i1 == 0) {
                    m_index++;
                }
            }
        }

        public void visitFieldInsn(int i, String s, String s1, String s2) {
            super.visitFieldInsn(i, s, s1, s2);
        }

        public void visitLdcInsn(Object o) {
            super.visitLdcInsn(o);
        }

        public void visitIincInsn(int i, int i1) {
            super.visitIincInsn(i, i1);
        }

        public void visitMultiANewArrayInsn(String s, int i) {
            super.visitMultiANewArrayInsn(s, i);
        }

        /**
         * Visit NEW type to ignore corresponding INVOKESPECIAL for those
         *
         * @param opcode
         * @param name
         */
        public void visitTypeInsn(int opcode, String name) {
            super.visitTypeInsn(opcode, name);
            if (opcode == NEW) {
                m_newCount++;
            }
        }

        public void visitMethodInsn(int opcode,
                                    String owner,
                                    String name,
                                    String desc) {
            if (!m_proxyCtorCodeDone) {
                if (opcode == INVOKESPECIAL) {
                    if (m_newCount == 0) {
                        // first INVOKESPECIAL encountered to <init> for a NON new XXX()
                        m_proxyCtorCodeVisitor.visitMethodInsn(opcode, owner, name, desc);
                        // insert the JoinPoint invocation
                        insertJoinPointInvoke(m_proxyCtorCodeVisitor, m_constructorAccess, m_constructorDesc);
                        m_proxyCtorCodeVisitor.visitInsn(RETURN);
                        m_proxyCtorCodeVisitor.visitMaxs(0, 0);
                        m_proxyCtorCodeVisitor = null;
                        m_proxyCtorCodeDone = true;
                        cv = m_ctorBodyMethodCodeVisitor;
                        // load ALOAD 0 if under heuristic
                        if (m_isALOADDUPHeuristic) {
                            m_ctorBodyMethodCodeVisitor.visitVarInsn(ALOAD, 0);
                        }
                    } else {
                        m_newCount--;
                        cv.visitMethodInsn(opcode, owner, name, desc);
                    }
                } else {
                    cv.visitMethodInsn(opcode, owner, name, desc);
                }
            } else {
                cv.visitMethodInsn(opcode, owner, name, desc);
            }
        }

    }
}
