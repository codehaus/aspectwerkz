/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
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
import org.objectweb.asm.Type;
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
import org.codehaus.aspectwerkz.annotation.instrumentation.asm.AsmAnnotationHelper;

import java.util.Iterator;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.Modifier;

/**
 * Advises catch clauses by inserting a call to the join point as the first thing in the catch block.
 *
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class HandlerVisitor extends ClassAdapter implements TransformationConstants {

    /**
     * A visitor that looks for all catch clause and keep track of them
     * providing that they match
     *
     * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
     */
    public static class LookaheadCatchLabelsClassAdapter extends ClassAdapter {
        /**
         * list of CatchLabelStruct that matches
         */
        List m_catchLabels = new ArrayList();

        /**
         * map of Integer(index in whole class)-->asm.Label for all the visited labels
         */
        private final Map m_labelIndexes = new HashMap();

        /**
         * current label index in whole class, from 0 to N
         */
        private int m_labelIndex = -1;

        private final ContextImpl m_ctx;
        private final ClassLoader m_loader;
        private final ClassInfo m_callerClassInfo;

        /**
         * Visit the class
         *
         * @param cv
         * @param loader
         * @param callerClassInfo
         * @param ctx
         * @param catchLabels
         */
        public LookaheadCatchLabelsClassAdapter(ClassVisitor cv, ClassLoader loader, ClassInfo callerClassInfo, Context ctx, List catchLabels) {
            super(cv);
            m_catchLabels = catchLabels;
            m_loader = loader;
            m_callerClassInfo = callerClassInfo;
            m_ctx = (ContextImpl) ctx;
        }

        /**
         * Visit method bodies
         * TODO withincode(clinit) support
         *
         * @param access
         * @param callerMethodName
         * @param callerMethodDesc
         * @param exceptions
         * @param attrs
         * @return
         */
        public CodeVisitor visitMethod(final int access,
                                       final String callerMethodName,
                                       final String callerMethodDesc,
                                       final String[] exceptions,
                                       final Attribute attrs) {
            // TODO - support withincode <clinit> for handler pc
            if (CLINIT_METHOD_NAME.equals(callerMethodName) ||
                callerMethodName.startsWith(WRAPPER_METHOD_PREFIX)) {
                return super.visitMethod(access, callerMethodName, callerMethodDesc, exceptions, attrs);
            }

            CodeVisitor mv = cv.visitMethod(access, callerMethodName, callerMethodDesc, exceptions, attrs);
            if (mv == null) return mv;

            final MemberInfo callerMemberInfo;
            if (INIT_METHOD_NAME.equals(callerMethodName)) {
                int hash = AsmHelper.calculateConstructorHash(callerMethodDesc);
                callerMemberInfo = m_callerClassInfo.getConstructor(hash);
            } else {
                int hash = AsmHelper.calculateMethodHash(callerMethodName, callerMethodDesc);
                callerMemberInfo = m_callerClassInfo.getMethod(hash);
            }
            if (callerMemberInfo == null) {
                System.err.println(
                        "AW::WARNING " +
                        "metadata structure could not be build for method ["
                        + m_callerClassInfo.getName().replace('/', '.')
                        + '.' + callerMethodName + ':' + callerMethodDesc + ']'
                );
                return mv;
            }

            /**
             * Visit the method, and keep track of all labels so that when visittryCatch is reached
             * we can remember the index
             *
             * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
             */
            return new CodeAdapter(mv) {
                public void visitLabel(Label label) {
                    m_labelIndexes.put(label, new Integer(++m_labelIndex));
                    super.visitLabel(label);
                }

                public void visitTryCatchBlock(Label startLabel, Label endLabel, Label handlerLabel, String exceptionTypeName) {
                    if (exceptionTypeName == null) {
                        // finally block
                        super.visitTryCatchBlock(startLabel, endLabel, handlerLabel, exceptionTypeName);
                        return;
                    }
                    final ClassInfo exceptionClassInfo = AsmClassInfo.getClassInfo(exceptionTypeName, m_loader);
                    final ExpressionContext ctx = new ExpressionContext(PointcutType.HANDLER, exceptionClassInfo, callerMemberInfo);
                    if (!handlerFilter(m_ctx.getDefinitions(), ctx)) {
                        // remember its index and the exception exceptionClassInfo
                        Integer index = (Integer)m_labelIndexes.get(handlerLabel);
                        if (index != null) {
                            m_catchLabels.add(new CatchLabelStruct(index.intValue(),
                                                                   exceptionClassInfo,
                                                                   m_callerClassInfo,
                                                                   callerMemberInfo)
                            );
                        }
                    }
                    super.visitTryCatchBlock(startLabel, endLabel, handlerLabel, exceptionTypeName);
                }
            };
        }
    }

    //---- non lookahead visitor

    private final ContextImpl m_ctx;
    private final ClassLoader m_loader;
    private final ClassInfo m_callerClassInfo;

    /**
     * List of matching catch clause
     */
    private final List m_catchLabels;

    /**
     * catch clause index in whole class
     */
    private int m_labelIndex = -1;

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
                          final Context ctx,
                          final List catchLabels) {
        super(cv);
        m_loader = loader;
        m_callerClassInfo = classInfo;
        m_ctx = (ContextImpl) ctx;
        m_catchLabels = catchLabels;
    }

    /**
     * Visits the methods bodies to weave in JP calls at catch clauses
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
                mv
        );
    }

    /**
     * Advises catch clauses by inserting a call to the join point as the first thing in the catch block.
     *
     * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
     */
    public class CatchClauseCodeAdapter extends CodeAdapter {

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
        public CatchClauseCodeAdapter(final CodeVisitor ca
) {
            super(ca);

//            m_loader = loader;
//            m_callerClassInfo = callerClassInfo;
//            m_callerClassName = callerClassName;
//            m_callerMethodName = callerMethodName;
//            m_callerMethodDesc = callerMethodDesc;
//
//            if (INIT_METHOD_NAME.equals(m_callerMethodName)) {
//                int hash = AsmHelper.calculateConstructorHash(m_callerMethodDesc);
//                m_callerMemberInfo = m_callerClassInfo.getConstructor(hash);
//            } else {
//                int hash = AsmHelper.calculateMethodHash(m_callerMethodName, m_callerMethodDesc);
//                m_callerMemberInfo = m_callerClassInfo.getMethod(hash);
//            }
        }

        /**
         * Line number
         *
         * @param lineNumber
         * @param label
         */
        public void visitLineNumber(int lineNumber, Label label) {
            m_lineNumber = lineNumber;
            super.visitLineNumber(lineNumber, label);
        }

        public void visitLabel(Label label) {
            super.visitLabel(label);

            // check if it is a catch label
            int index = ++m_labelIndex;
            CatchLabelStruct catchLabel = null;
            for (Iterator iterator = m_catchLabels.iterator(); iterator.hasNext();) {
                CatchLabelStruct aCatchLabel = (CatchLabelStruct) iterator.next();
                if (aCatchLabel.labelIndexInWholeClass == index) {
                    catchLabel = aCatchLabel;
                    break;
                }
            }
            if (catchLabel == null) {
                return;
            }
            // matched
            m_ctx.markAsAdvised();
            final String callerTypeName = catchLabel.caller.getName().replace('.', '/');
            final String exceptionTypeDesc = catchLabel.exception.getSignature();
            final String exceptionTypeName = Type.getType(exceptionTypeDesc).getInternalName();
            final int joinPointHash = AsmHelper.calculateClassHash(exceptionTypeDesc);
            final String joinPointClassName = TransformationUtil.getJoinPointClassName(
                    callerTypeName,
                    exceptionTypeName,
                    JoinPointType.HANDLER,
                    joinPointHash
            );

            // add the call to the join point
            // exception instance is on the stack
            // load caller instance if any
            if (Modifier.isStatic(catchLabel.callerMember.getModifiers())) {
                cv.visitInsn(ACONST_NULL);
            } else {
                cv.visitVarInsn(ALOAD, 0);
            }
            //cv.visitInsn(SWAP);
            cv.visitMethodInsn(
                    INVOKESTATIC, joinPointClassName, INVOKE_METHOD_NAME,
                    TransformationUtil.getInvokeSignatureForHandlerJoinPoints(
                            callerTypeName, exceptionTypeName
                    )
            );

            // emit the joinpoint
            m_ctx.addEmittedJoinPoint(
                    new EmittedJoinPoint(
                            JoinPointType.HANDLER,
                            callerTypeName,
                            catchLabel.callerMember.getName(),
                            catchLabel.callerMember.getSignature(),
                            catchLabel.callerMember.getModifiers(),
                            exceptionTypeName,
                            "",
                            exceptionTypeDesc,
                            0,// a bit meaningless but must not be static
                            joinPointHash,
                            joinPointClassName,
                            m_lineNumber
                    )
            );
        }
    }

    /**
     * Filters out the catch clauses that are not eligible for transformation.
     *
     * @param definitions
     * @param ctx
     * @return boolean true if the catch clause should be filtered out
     */
    static boolean handlerFilter(final Set definitions, final ExpressionContext ctx) {
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
     * A struct to represent a catch clause.
     * The index is class wide, and the exception class info is kept.
     *
     * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
     */
    private static class CatchLabelStruct {
        int labelIndexInWholeClass = -1;
        ClassInfo exception = null;
        ClassInfo caller = null;
        MemberInfo callerMember = null;

        private CatchLabelStruct(int indexInClass, ClassInfo exception, ClassInfo caller, MemberInfo callerMember) {
            labelIndexInWholeClass = indexInClass;
            this.exception = exception;
            this.caller = caller;
            this.callerMember = callerMember;
        }
    }
}