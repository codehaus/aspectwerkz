/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform.inlining.compiler;

import org.objectweb.asm.CodeVisitor;
import org.objectweb.asm.Type;

import org.codehaus.aspectwerkz.transform.inlining.AsmHelper;

import java.lang.reflect.Modifier;

/**
 * A compiler that compiles/generates a class that represents a specific join point, a class which invokes the advices
 * and the target join point statically.
 *
 * In this case, CALLEE is the catched exception instance itself.
 *
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur </a>
 */
public class HandlerJoinPointCompiler extends AbstractJoinPointCompiler {

    /**
     * Creates a new join point compiler instance.
     *
     * @param model
     */
    HandlerJoinPointCompiler(final CompilationInfo.Model model) {
        super(model);
    }

    /**
     * Creates join point specific fields.
     */
    protected void createJoinPointSpecificFields() {
        m_fieldNames = new String[0];
        m_cw.visitField(
                ACC_PRIVATE + ACC_STATIC,
                SIGNATURE_FIELD_NAME,
                HANDLER_SIGNATURE_IMPL_CLASS_SIGNATURE,
                null,
                null
        );
    }

    /**
     * Creates the signature for the join point.
     * <p/>
     * FIXME signature field should NOT be of type Signature but of the specific type (update all refs as well)
     *
     * @param cv
     */
    protected void createSignature(final CodeVisitor cv) {
        cv.visitFieldInsn(GETSTATIC, m_joinPointClassName, TARGET_CLASS_FIELD_NAME, CLASS_CLASS_SIGNATURE);
        cv.visitLdcInsn(new Integer(m_joinPointHash));


        cv.visitMethodInsn(
                INVOKESTATIC,
                SIGNATURE_FACTORY_CLASS,
                NEW_CATCH_CLAUSE_SIGNATURE_METHOD_NAME,
                NEW_HANDLER_SIGNATURE_METHOD_SIGNATURE
        );
        cv.visitFieldInsn(
                PUTSTATIC, m_joinPointClassName, SIGNATURE_FIELD_NAME, HANDLER_SIGNATURE_IMPL_CLASS_SIGNATURE
        );

    }

//    /**
//     * Creates a RTTI instance and adds it to the JoinPoint instance (RTTI member field).
//     *
//     * @param cv
//     * @param joinPointInstanceIndex
//     * @param callerIndex
//     * @param calleeIndex
//     */
//    protected void createAndAddRttiInstance(final CodeVisitor cv,
//                                            final int joinPointInstanceIndex,
//                                            final int callerIndex,
//                                            final int calleeIndex) {
//        createAndAddRttiInstance(
//                cv,
//                joinPointInstanceIndex, callerIndex, calleeIndex,
//                HANDLER_RTTI_IMPL_CLASS_NAME,
//                HANDLER_RTTI_IMPL_CLASS_SIGNATURE,
//                HANDLER_RTTI_IMPL_INIT_SIGNATURE,
//                HANDLER_SIGNATURE_IMPL_CLASS_SIGNATURE
//        );
//    }

    /**
     * Optimized implementation that does not retrieve the parameters from the join point instance but is passed
     * directly to the method from the input parameters in the 'invoke' method. Can only be used if no around advice
     * exists.
     *
     * @param cv
     * @param argStartIndex index on stack of first target method arg (0 or 1, depends of static target or not)
     */
    protected void createInlinedJoinPointInvocation(final CodeVisitor cv, final boolean isOptimizedJoinPoint,
                                                    final int argStartIndex, final int joinPointIndex) {

        // load the exception
        cv.visitVarInsn(ALOAD, 0);//TODO if changed perhaps load CALLEE instead that host the exception ?
//        // load the target exception (arg0 else not available for static target)
//        if (!Modifier.isStatic(m_calleeMemberModifiers)) {
//            cv.visitVarInsn(ALOAD, 0);
//        }
//
        //throw new UnsupportedOperationException("join point type is not supported: HANDLER");
    }

    /**
     * Creates a call to the target join point, the parameter(s) to the join point are retrieved from the invocation
     * local join point instance.
     *
     * @param cv
     */
    protected void createJoinPointInvocation(final CodeVisitor cv) {
        //cv.visitInsn();
//        // load the target instance member field unless calleeMember is static
//        if (!Modifier.isStatic(m_calleeMemberModifiers)) {
//            cv.visitVarInsn(ALOAD, 0);
//            cv.visitFieldInsn(GETFIELD, m_joinPointClassName, CALLEE_INSTANCE_FIELD_NAME, m_calleeClassSignature);
//        }
//        throw new UnsupportedOperationException("join point type is not supported: HANDLER");
          throw new UnsupportedOperationException("Should not happen - join point type is not supported: HANDLER");
    }

//    /**
//     * Adds the parameters to the RTTI instance, builds up an Object array of the arguments if needed.
//     *
//     * @param cv
//     * @param joinPointInstanceIndex
//     * @param argStartIndex
//     * @param stackFreeIndex
//     */
//    protected void addParametersToRttiInstance(final CodeVisitor cv,
//                                               final int joinPointInstanceIndex,
//                                               final int argStartIndex,
//                                               final int stackFreeIndex) {
//        // we know we don't need to wrap the parameter (which is the exception)
//        AsmHelper.loadType(cv, argStartIndex, m_argumentTypes[0]);
//        loadJoinPointInstance(cv, NON_OPTIMIZED_JOIN_POINT, joinPointInstanceIndex, m_joinPointClassName);
//        cv.visitFieldInsn(
//                GETFIELD, m_joinPointClassName, RTTI_INSTANCE_FIELD_NAME,
//                HANDLER_RTTI_IMPL_CLASS_SIGNATURE
//        );
//        cv.visitVarInsn(ALOAD, argStartIndex);
//        cv.visitMethodInsn(
//                INVOKEVIRTUAL, HANDLER_RTTI_IMPL_CLASS_NAME,
//                SET_PARAMETER_VALUE_METHOD_NAME, SET_PARAMETER_VALUE_METHOD_SIGNATURE
//        );
//    }


    /**
     * Returns the join points return type.
     *
     * @return
     */
    protected Type getJoinPointReturnType() {
        return Type.getType(m_calleeMemberDesc);
    }

    /**
     * Returns the join points argument type(s).
     *
     * @return
     */
    protected Type[] getJoinPointArgumentTypes() {
        return new Type[0];//TODO should callee be arg instead ? to bind it later ?
    }

    /**
     * Creates the getRtti method
     */
    protected void createGetRttiMethod() {
        CodeVisitor cv = m_cw.visitMethod(ACC_PUBLIC, GET_RTTI_METHOD_NAME, GET_RTTI_METHOD_SIGNATURE, null, null);

        // new CtorRttiImpl( .. )
        cv.visitTypeInsn(NEW, HANDLER_RTTI_IMPL_CLASS_NAME);
        cv.visitInsn(DUP);
        cv.visitFieldInsn(
                GETSTATIC, m_joinPointClassName, SIGNATURE_FIELD_NAME, HANDLER_SIGNATURE_IMPL_CLASS_SIGNATURE
        );
        cv.visitVarInsn(ALOAD, 0);
        cv.visitFieldInsn(GETFIELD, m_joinPointClassName, CALLER_INSTANCE_FIELD_NAME, m_callerClassSignature);
        cv.visitVarInsn(ALOAD, 0);
        cv.visitFieldInsn(GETFIELD, m_joinPointClassName, CALLEE_INSTANCE_FIELD_NAME, m_calleeClassSignature);
        cv.visitMethodInsn(
                INVOKESPECIAL, HANDLER_RTTI_IMPL_CLASS_NAME, INIT_METHOD_NAME, HANDLER_RTTI_IMPL_INIT_SIGNATURE
        );

//        // set the value
//        cv.visitInsn(DUP);
//        cv.visitVarInsn(ALOAD, 0);
//        cv.visitFieldInsn(GETFIELD, m_joinPointClassName, RETURN_VALUE_FIELD_NAME, m_returnType.getDescriptor());
//        cv.visitMethodInsn(
//                INVOKEVIRTUAL, HANDLER_RTTI_IMPL_CLASS_NAME, SET_RETURN_VALUE_METHOD_NAME,
//                SET_RETURN_VALUE_METHOD_SIGNATURE
//        );

        cv.visitInsn(ARETURN);
        cv.visitMaxs(0, 0);
    }

    /**
     * Creates the getSignature method.
     */
    protected void createGetSignatureMethod() {
        CodeVisitor cv = m_cw.visitMethod(
                ACC_PUBLIC,
                GET_SIGNATURE_METHOD_NAME,
                GET_SIGNATURE_METHOD_SIGNATURE,
                null,
                null
        );
        cv.visitFieldInsn(
                GETSTATIC, m_joinPointClassName,
                SIGNATURE_FIELD_NAME, HANDLER_SIGNATURE_IMPL_CLASS_SIGNATURE
        );
        cv.visitInsn(ARETURN);
        cv.visitMaxs(0, 0);
    }
}