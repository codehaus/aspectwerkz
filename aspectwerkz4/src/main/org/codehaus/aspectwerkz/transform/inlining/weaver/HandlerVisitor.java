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
import org.objectweb.asm.CodeAdapter;
import org.objectweb.asm.Label;
import org.codehaus.aspectwerkz.definition.SystemDefinition;
import org.codehaus.aspectwerkz.expression.ExpressionContext;
import org.codehaus.aspectwerkz.expression.PointcutType;
import org.codehaus.aspectwerkz.joinpoint.management.JoinPointType;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.MemberInfo;
import org.codehaus.aspectwerkz.reflect.impl.asm.AsmClassInfo;
import org.codehaus.aspectwerkz.transform.Context;
import org.codehaus.aspectwerkz.transform.TransformationUtil;
import org.codehaus.aspectwerkz.transform.TransformationConstants;
import org.codehaus.aspectwerkz.transform.inlining.ContextImpl;
import org.codehaus.aspectwerkz.transform.inlining.AsmHelper;
import org.codehaus.aspectwerkz.transform.inlining.EmittedJoinPoint;

import java.util.Iterator;
import java.util.Set;

/**
 * Advises catch clauses by inserting a call to the join point as the first thing in the catch block.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public class HandlerVisitor extends ClassAdapter implements TransformationConstants {

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
    public HandlerVisitor(final ClassVisitor cv,
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

        // TODO - support withincode <clinit> for handler pc
        if (CLINIT_METHOD_NAME.equals(name) ||
            name.startsWith(WRAPPER_METHOD_PREFIX)) {
            return super.visitMethod(access, name, desc, exceptions, attrs);
        }

        CodeVisitor mv = cv.visitMethod(access, name, desc, exceptions, attrs);
        return mv == null ? null : new CatchClauseCodeAdapter(
                mv,
                m_loader,
                m_callerClassInfo,
                m_ctx.getClassName(),
                name,
                desc
        );
    }

    /**
     * Advises catch clauses by inserting a call to the join point as the first thing in the catch block.
     *
     * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
     */
    public class CatchClauseCodeAdapter extends CodeAdapter {

        private final ClassLoader m_loader;
        private final ClassInfo m_callerClassInfo;
        private final String m_callerClassName;
        private final String m_callerMethodName;
        private final String m_callerMethodDesc;
        private final MemberInfo m_callerMemberInfo;
        private int m_lineNumber = EmittedJoinPoint.NO_LINE_NUMBER;

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
        public CatchClauseCodeAdapter(final CodeVisitor ca,
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
                int hash = AsmHelper.calculateConstructorHash(m_callerMethodDesc);
                m_callerMemberInfo = m_callerClassInfo.getConstructor(hash);
            } else {
                int hash = AsmHelper.calculateMethodHash(m_callerMethodName, m_callerMethodDesc);
                m_callerMemberInfo = m_callerClassInfo.getMethod(hash);
            }
        }

        /**
         * Line number
         *
         * @param lineNumber
         * @param label
         */
        public void visitLineNumber(int lineNumber, Label label) {
            m_lineNumber = lineNumber;
        }

        /**
         * Visits a catch clause.
         *
         * @param startLabel
         * @param endLabel
         * @param handlerLabel
         * @param exceptionTypeName
         */
        public void visitTryCatchBlock(final Label startLabel,
                                       final Label endLabel,
                                       final Label handlerLabel,
                                       final String exceptionTypeName) {

            if (m_callerMemberInfo == null) {
                System.err.println(
                        "AW::WARNING " +
                        "metadata structure could not be build for method ["
                        + m_callerClassInfo.getName().replace('/', '.')
                        + '.' + m_callerMethodName + ':' + m_callerMethodDesc + ']'
                );
                cv.visitTryCatchBlock(startLabel, endLabel, handlerLabel, exceptionTypeName);
                return;
            }

            final ClassInfo classInfo = AsmClassInfo.getClassInfo(exceptionTypeName, m_loader);
            final ExpressionContext ctx = new ExpressionContext(PointcutType.HANDLER, classInfo, m_callerMemberInfo);

            if (!handlerFilter(m_ctx.getDefinitions(), ctx)) {
                m_ctx.markAsAdvised();

                final String exceptionTypeDesc = L + exceptionTypeName + SEMICOLON;
                final int joinPointHash = AsmHelper.calculateClassHash(exceptionTypeDesc);

                final String joinPointClassName = TransformationUtil.getJoinPointClassName(
                        m_callerClassName,
                        exceptionTypeName,
                        JoinPointType.HANDLER,
                        joinPointHash
                );
                // add the call to the join point
                cv.visitMethodInsn(
                        INVOKESTATIC, joinPointClassName, INVOKE_METHOD_NAME,
                        TransformationUtil.getInvokeSignatureForHandlerJoinPoints(
                                m_callerClassName, exceptionTypeName
                        )
                );

                // emit the joinpoint
                m_ctx.addEmittedJoinPoint(
                        new EmittedJoinPoint(
                                JoinPointType.HANDLER,
                                m_callerClassName,
                                m_callerMethodName,
                                m_callerMethodDesc,
                                m_callerMemberInfo.getModifiers(),
                                exceptionTypeName,
                                "",
                                exceptionTypeDesc,
                                -1,
                                joinPointHash,
                                joinPointClassName,
                                m_lineNumber
                        )
                );
            }
            cv.visitTryCatchBlock(startLabel, endLabel, handlerLabel, exceptionTypeName);
        }

        /**
         * Filters out the catch clauses that are not eligible for transformation.
         *
         * @param definitions
         * @param ctx
         * @return boolean true if the catch clause should be filtered out
         */
        public boolean handlerFilter(final Set definitions, final ExpressionContext ctx) {
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