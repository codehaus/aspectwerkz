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
import org.objectweb.asm.Constants;
import org.objectweb.asm.CodeAdapter;
import org.objectweb.asm.Label;
import org.codehaus.aspectwerkz.definition.SystemDefinition;
import org.codehaus.aspectwerkz.expression.ExpressionContext;
import org.codehaus.aspectwerkz.expression.PointcutType;
import org.codehaus.aspectwerkz.joinpoint.management.JoinPointType;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.ConstructorInfo;
import org.codehaus.aspectwerkz.reflect.MemberInfo;
import org.codehaus.aspectwerkz.reflect.impl.asm.AsmClassInfo;
import org.codehaus.aspectwerkz.transform.Context;
import org.codehaus.aspectwerkz.transform.TransformationUtil;
import org.codehaus.aspectwerkz.transform.TransformationConstants;
import org.codehaus.aspectwerkz.transform.inlining.compiler.AbstractJoinPointCompiler;
import org.codehaus.aspectwerkz.transform.inlining.ContextImpl;
import org.codehaus.aspectwerkz.transform.inlining.AsmHelper;
import org.codehaus.aspectwerkz.transform.inlining.EmittedJoinPoint;
import org.codehaus.aspectwerkz.annotation.instrumentation.asm.AsmAnnotationHelper;

import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.Set;

import gnu.trove.TLongObjectHashMap;
import gnu.trove.TIntObjectHashMap;

/**
 * Instruments ctor CALL join points by replacing INVOKEXXX instructions with invocations of the compiled join point.
 * <br/>
 * It calls the JPClass.invoke static method. The signature of the invoke method is:
 * <pre>
 *      invoke(args.., caller) - note: no callee as arg0
 * </pre>
 * (The reason why is that it simplifies call pointcut stack management)
 *
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class ConstructorCallVisitor extends ClassAdapter implements TransformationConstants {

    private final static TIntObjectHashMap EMPTY_INTHASHMAP = new TIntObjectHashMap(0);

    private final ContextImpl m_ctx;
    private final ClassLoader m_loader;
    private final ClassInfo m_callerClassInfo;

    /**
     * Map of NEW instructions.
     * The key is the method (withincode) hash
     * The value is a TLongObjectHashMap whose key is index of NEW instructions and value instance of NewInvocationStruct
     */
    private final TLongObjectHashMap m_newInvocationsByCallerMemberHash;

    private Label m_lastLabelForLineNumber = EmittedJoinPoint.NO_LINE_NUMBER;

    /**
     * Creates a new instance.
     *
     * @param cv
     * @param loader
     * @param classInfo
     * @param ctx
     */
    public ConstructorCallVisitor(final ClassVisitor cv,
                                  final ClassLoader loader,
                                  final ClassInfo classInfo,
                                  final Context ctx,
                                  final TLongObjectHashMap newInvocationsByCallerMemberHash) {
        super(cv);
        m_loader = loader;
        m_callerClassInfo = classInfo;
        m_ctx = (ContextImpl) ctx;
        m_newInvocationsByCallerMemberHash = newInvocationsByCallerMemberHash;
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

        if (name.startsWith(WRAPPER_METHOD_PREFIX) ||
            Modifier.isNative(access) ||
            Modifier.isAbstract(access)) {
            return super.visitMethod(access, name, desc, exceptions, attrs);
        }

        CodeVisitor mv = cv.visitMethod(access, name, desc, exceptions, attrs);
        return mv == null ? null : new ReplaceNewInstructionCodeAdapter(
                mv,
                m_loader,
                m_callerClassInfo,
                m_ctx.getClassName(),
                name,
                desc,
                (TIntObjectHashMap) m_newInvocationsByCallerMemberHash.get(getMemberHash(name, desc))
        );
    }


    /**
     * Replaces 'new' instructions with a call to the compiled JoinPoint instance.
     * <br/>
     * It does the following:
     * - remove NEW <class> when we know (from first visit) that it matchs
     * - remove DUP that follows NEW <class>
     * - replace INVOKESPECIAL <ctor signature> with call to JP
     *
     * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
     */
    public class ReplaceNewInstructionCodeAdapter extends CodeAdapter {

        private final ClassLoader m_loader;
        private final ClassInfo m_callerClassInfo;
        private final String m_callerClassName;
        private final String m_callerMethodName;
        private final String m_callerMethodDesc;
        private final MemberInfo m_callerMemberInfo;

        /**
         * Map of NewInvocationStruct indexed by NEW indexes (incremented thru the visit) for the visited member code body
         */
        private final TIntObjectHashMap m_newInvocations;

        /**
         * Index of NEW instr. in the scope of the visited member code body
         */
        private int m_newInvocationIndex = -1;

        /**
         * Stack of NewInovationStruct, which mirrors the corresponding INVOKESPECIAL <init> when a NEW has been visited.
         * If the entry is NULL, it means that this ctor call does not match.
         * This allow to compute the match only once when the NEW is visited (since we have data from the first visit)
         * while supporting nested interception like new Foo(new Bar("s"))
         */
        private final Stack m_newInvocationStructStack = new Stack();

        /**
         * Flag set to true just after a NEW that match has been visited
         */
        private boolean m_skipNextDup = false;

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
        public ReplaceNewInstructionCodeAdapter(final CodeVisitor ca,
                                                final ClassLoader loader,
                                                final ClassInfo callerClassInfo,
                                                final String callerClassName,
                                                final String callerMethodName,
                                                final String callerMethodDesc,
                                                final TIntObjectHashMap newInvocations) {
            super(ca);
            m_loader = loader;
            m_callerClassInfo = callerClassInfo;
            m_callerClassName = callerClassName;
            m_callerMethodName = callerMethodName;
            m_callerMethodDesc = callerMethodDesc;
            m_newInvocations = (newInvocations != null) ? newInvocations : EMPTY_INTHASHMAP;

            if (CLINIT_METHOD_NAME.equals(m_callerMethodName)) {
                m_callerMemberInfo = m_callerClassInfo.staticInitializer();
            } else if (INIT_METHOD_NAME.equals(m_callerMethodName)) {
                final int hash = AsmHelper.calculateConstructorHash(m_callerMethodDesc);
                m_callerMemberInfo = m_callerClassInfo.getConstructor(hash);
            } else {
                final int hash = AsmHelper.calculateMethodHash(m_callerMethodName, m_callerMethodDesc);
                m_callerMemberInfo = m_callerClassInfo.getMethod(hash);
            }
            if (m_callerMemberInfo == null) {
                System.err.println(
                        "AW::WARNING " +
                        "metadata structure could not be build for method ["
                        + m_callerClassInfo.getName().replace('/', '.')
                        + '.' + m_callerMethodName + ':' + m_callerMethodDesc + ']'
                );
            }
        }

        /**
         * Label
         *
         * @param label
         */
        public void visitLabel(Label label) {
            m_lastLabelForLineNumber = label;
            super.visitLabel(label);
        }

        /**
         * Removes the NEW when we know that the corresponding INVOKE SPECIAL <init> is advised.
         *
         * @param opcode
         * @param desc
         */
        public void visitTypeInsn(int opcode, String desc) {
            if (m_callerMemberInfo == null) {
                return;
            }

            if (opcode == NEW) {
                m_newInvocationIndex++;
                // build the callee ConstructorInfo and check for a match
                NewInvocationStruct newInvocationStruct = (NewInvocationStruct) m_newInvocations.get(
                        m_newInvocationIndex
                );
                if (newInvocationStruct == null) {
                    super.visitTypeInsn(opcode, desc);//we failed
                    return;
                }
                String calleeClassName = newInvocationStruct.className;
                String calleeMethodName = INIT_METHOD_NAME;
                String calleeMethodDesc = newInvocationStruct.ctorDesc;
                int joinPointHash = AsmHelper.calculateMethodHash(calleeMethodName, calleeMethodDesc);
                ClassInfo classInfo = AsmClassInfo.getClassInfo(calleeClassName, m_loader);
                ConstructorInfo calleeConstructorInfo = classInfo.getConstructor(joinPointHash);
                if (calleeConstructorInfo == null) {
                    super.visitTypeInsn(opcode, desc);//we failed
                    System.err.println(
                            "AW::WARNING " +
                            "metadata structure could not be build for method ["
                            + classInfo.getName().replace('/', '.')
                            + '.' + calleeMethodName + ':' + calleeMethodDesc + ']'
                    );
                    return;
                }

                // do we have a match - if so, skip the NEW and the DUP
                ExpressionContext ctx = new ExpressionContext(
                        PointcutType.CALL, calleeConstructorInfo, m_callerMemberInfo
                );
                if (constructorFilter(m_ctx.getDefinitions(), ctx, calleeConstructorInfo)) {
                    // push NULL as a struct (means no match)
                    m_newInvocationStructStack.push(null);
                    super.visitTypeInsn(opcode, desc);
                } else {
                    // keep track of the ConstructorInfo so that we don't compute it again in visitMethodInsn <init>
                    newInvocationStruct.constructorInfo = calleeConstructorInfo;
                    newInvocationStruct.joinPointHash = joinPointHash;
                    m_newInvocationStructStack.push(newInvocationStruct);
                    // skip NEW instr and flag to skip next DUP
                    m_skipNextDup = true;
                    //System.out.println("RECORD " + calleeClassName + calleeMethodDesc);
                }
            } else {
                // is not a NEW instr
                super.visitTypeInsn(opcode, desc);
            }
        }

        /**
         * Remove the DUP instruction if we know that those were for a NEW ... INVOKESPECIAL that match.
         *
         * @param opcode
         */
        public void visitInsn(int opcode) {
            if ((opcode == DUP || opcode == DUP_X1) && m_skipNextDup) {
                //System.out.println("SKIP dup");
                ;// skip the DUP
                if (opcode == DUP_X1)
                    super.visitInsn(DUP);
            } else {
                super.visitInsn(opcode);
            }
            m_skipNextDup = false;
        }

        /**
         * Visits INVOKESPECIAL <init> instructions and replace them with a call to the join point when matched.
         *
         * @param opcode
         * @param calleeClassName
         * @param calleeConstructorName
         * @param calleeConstructorDesc
         */
        public void visitMethodInsn(final int opcode,
                                    final String calleeClassName,
                                    final String calleeConstructorName,
                                    final String calleeConstructorDesc) {

            if (m_callerMemberInfo == null) {
                super.visitMethodInsn(opcode, calleeClassName, calleeConstructorName, calleeConstructorDesc);
                return;
            }

            if (!INIT_METHOD_NAME.equals(calleeConstructorName) ||
                    calleeClassName.endsWith(AbstractJoinPointCompiler.JOIN_POINT_CLASS_SUFFIX)) {
                super.visitMethodInsn(opcode, calleeClassName, calleeConstructorName, calleeConstructorDesc);
                return;
            }

            // get the info from the invocation stack since all the matching has already been done
            if (m_newInvocationStructStack.isEmpty()) {
                // nothing to weave
                super.visitMethodInsn(opcode, calleeClassName, calleeConstructorName, calleeConstructorDesc);
                return;
            }

            NewInvocationStruct struct = (NewInvocationStruct) m_newInvocationStructStack.pop();
            if (struct == null) {
                // not matched
                super.visitMethodInsn(opcode, calleeClassName, calleeConstructorName, calleeConstructorDesc);
            } else {
                m_ctx.markAsAdvised();

                String joinPointClassName = TransformationUtil.getJoinPointClassName(
                        m_callerClassName,
                        m_callerMethodName,
                        m_callerMethodDesc,
                        calleeClassName,
                        JoinPointType.CONSTRUCTOR_CALL_INT,
                        struct.joinPointHash
                );

                // load the caller instance (this), or null if in a static context
                // note that callee instance [mandatory since ctor] and args are already on the stack
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
                        TransformationUtil.getInvokeSignatureForConstructorCallJoinPoints(
                                calleeConstructorDesc,
                                m_callerClassName,
                                calleeClassName
                        )
                );

                // emit the joinpoint
                m_ctx.addEmittedJoinPoint(
                        new EmittedJoinPoint(
                                JoinPointType.CONSTRUCTOR_CALL_INT,
                                m_callerClassName,
                                m_callerMethodName,
                                m_callerMethodDesc,
                                m_callerMemberInfo.getModifiers(),
                                calleeClassName,
                                calleeConstructorName,
                                calleeConstructorDesc,
                                struct.constructorInfo.getModifiers(),
                                struct.joinPointHash,
                                joinPointClassName,
                                m_lastLabelForLineNumber
                        )
                );
            }
        }

        /**
         * Filters out the ctor that are not eligible for transformation.
         *
         * @param definitions
         * @param ctx
         * @param calleeConstructorInfo
         * @return boolean true if the method should be filtered out
         */
        public boolean constructorFilter(final Set definitions,
                                         final ExpressionContext ctx,
                                         final ConstructorInfo calleeConstructorInfo) {
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

    private static int getMemberHash(String name, String desc) {
        int hash = 29;
        hash = (29 * hash) + name.hashCode();
        return (29 * hash) + desc.hashCode();
    }

    /**
     * Lookahead index of NEW instruction for NEW + DUP + INVOKESPECIAL instructions
     * Remember the NEW instruction index
     * <p/>
     * Special case when withincode ctor of called ctor:
     * <pre>public Foo() { super(new Foo()); }</pre>
     * In such a case, it is not possible to intercept the call to new Foo() since this cannot be
     * referenced as long as this(..) or super(..) has not been called.
     *
     * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
     */
    public static class LookaheadNewDupInvokeSpecialInstructionClassAdapter
            extends AsmAnnotationHelper.NullClassAdapter {

        private String m_callerMemberName;

        // list of new invocations by caller member hash
        public TLongObjectHashMap m_newInvocationsByCallerMemberHash;

        public LookaheadNewDupInvokeSpecialInstructionClassAdapter(TLongObjectHashMap newInvocations) {
            m_newInvocationsByCallerMemberHash = newInvocations;
        }

        public CodeVisitor visitMethod(final int access,
                                       final String name,
                                       final String desc,
                                       final String[] exceptions,
                                       final Attribute attrs) {
            if (name.startsWith(WRAPPER_METHOD_PREFIX) ||
                Modifier.isNative(access) ||
                Modifier.isAbstract(access)) {
                ;//ignore
            }

            m_callerMemberName = name;

            TIntObjectHashMap newInvocations = new TIntObjectHashMap(5);
            m_newInvocationsByCallerMemberHash.put(getMemberHash(name, desc), newInvocations);
            return new LookaheadNewDupInvokeSpecialInstructionCodeAdapter(
                    super.visitMethod(access, name, desc, exceptions, attrs),
                    newInvocations,
                    m_callerMemberName
            );
        }
    }

    public static class LookaheadNewDupInvokeSpecialInstructionCodeAdapter
            extends AfterObjectInitializationCodeAdapter {

        private TIntObjectHashMap m_newInvocations;

        private Stack m_newIndexStack = new Stack();
        private int m_newIndex = -1;

        /**
         * Creates a new instance.
         */
        public LookaheadNewDupInvokeSpecialInstructionCodeAdapter(CodeVisitor cv, TIntObjectHashMap newInvocations,
                                                                  final String callerMemberName) {
            super(cv, callerMemberName);
            m_newInvocations = newInvocations;
        }

        public void visitTypeInsn(int opcode, String desc) {
            // make sure to call super first to compute post object initialization flag
            super.visitTypeInsn(opcode, desc);
            if (opcode == NEW) {
                m_newIndex++;
                m_newIndexStack.push(new Integer(m_newIndex));
            }
        }

        public void visitMethodInsn(final int opcode,
                                    final String calleeClassName,
                                    final String calleeMethodName,
                                    final String calleeMethodDesc) {
            // make sure to call super first to compute post object initialization flag
            super.visitMethodInsn(opcode, calleeClassName, calleeMethodName, calleeMethodDesc);

            if (INIT_METHOD_NAME.equals(calleeMethodName) && opcode == INVOKESPECIAL) {
                if (!m_isObjectInitialized) {
                    // skip - remove the NEW index from the stack
                    if (!m_newIndexStack.isEmpty()) {
                        m_newIndexStack.pop();
                    }
                } else {
                    if (!m_newIndexStack.isEmpty()) {
                        int index = ((Integer) m_newIndexStack.pop()).intValue();
                        NewInvocationStruct newInvocationStruct = new NewInvocationStruct();
                        newInvocationStruct.className = calleeClassName;
                        newInvocationStruct.ctorDesc = calleeMethodDesc;
                        // constructorInfo and matching will be done at weave time and not at lookahead time
                        m_newInvocations.put(index, newInvocationStruct);
                    }
                }
            }
        }
    }

    private static class NewInvocationStruct {
        public String className;
        public String ctorDesc;
        public ConstructorInfo constructorInfo = null;
        public int joinPointHash = -1;
    }

}