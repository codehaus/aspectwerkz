/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.joinpoint.management;

import org.codehaus.aspectwerkz.joinpoint.Signature;

import java.util.List;

/**
 * Abstraction of a method join point.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
class MethodJoinPoint extends JoinPointBase {

    private final MethodSignatureImpl m_signature;

    private int stackIndex = -1;
    private Object result = null;

    /**
     * Creates a new join point.
     *
     * @param uuid
     * @param type
     * @param targetClass
     * @param signature
     * @param cflowExpressions
     * @param aroundAdviceExecutor
     * @param beforeAdviceExecutor
     * @param afterAdviceExecutor
     */
    public MethodJoinPoint(
            final String uuid,
            final int type,
            final Class targetClass,
            final Signature signature,
            final List cflowExpressions,
            final AroundAdviceExecutor aroundAdviceExecutor,
            final BeforeAdviceExecutor beforeAdviceExecutor,
            final AfterAdviceExecutor afterAdviceExecutor) {
        super(uuid, type, targetClass, cflowExpressions, aroundAdviceExecutor, beforeAdviceExecutor, afterAdviceExecutor);
        m_signature = (MethodSignatureImpl)signature;
    }

    /**
     * Walks through the pointcuts and invokes all its advices. When the last advice of the last pointcut has been
     * invoked, the original method is invoked. Is called recursively.
     *
     * @return the result from the next invocation
     * @throws Throwable
     */
    public Object proceed() throws Throwable {
        //System.out.println("proceed() " + this + " " + stackIndex);

//        stackIndex++;
//        try {
//            if (stackIndex == 0) {
//                if (m_beforeAdviceExecutor.hasAdvices()) {
//                    m_beforeAdviceExecutor.proceed(this);
//                }


            /*final Object */result = m_aroundAdviceExecutor.proceed(this);
                            m_signature.setReturnValue(result);
//        }
//
//         if (stackIndex == 1)
//                if (m_afterAdviceExecutor.hasAdvices()) {
//                    m_afterAdviceExecutor.proceed(this);
//                }
//        } finally {
//            stackIndex = -1;
//        }



         return result;



    }

    /**
     * Returns the signature for the join point.
     *
     * @return the signature
     */
    public Signature getSignature() {
        return m_signature;
    }

    /**
     * Returns a string representation of the join point.
     *
     * @return a string representation
     * @TODO: implement toString to something meaningful
     */
    public String toString() {
        return super.toString();
    }
}
