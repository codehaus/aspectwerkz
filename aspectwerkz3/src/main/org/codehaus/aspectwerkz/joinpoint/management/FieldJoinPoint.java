/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.joinpoint.management;

import org.codehaus.aspectwerkz.joinpoint.FieldRtti;
import org.codehaus.aspectwerkz.joinpoint.FieldSignature;
import org.codehaus.aspectwerkz.joinpoint.Rtti;
import org.codehaus.aspectwerkz.joinpoint.Signature;

/**
 * Abstraction of a field join point.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
class FieldJoinPoint extends JoinPointBase {
    private final FieldSignature m_signature;
    private final FieldRtti m_rtti;

    /**
     * Creates a new join point.
     *
     * @param type
     * @param targetClass
     * @param signature
     * @param rtti
     * @param joinPointMetaData
     * @param aroundAdviceExecutor
     * @param beforeAdviceExecutor
     * @param afterAdviceExecutor
     */
    public FieldJoinPoint(
            final int type, final Class targetClass, final Signature signature, final Rtti rtti,
            final JoinPointMetaData joinPointMetaData, final AroundAdviceExecutor aroundAdviceExecutor,
            final BeforeAdviceExecutor beforeAdviceExecutor,
            final AfterAdviceExecutor afterAdviceExecutor) {
        super(type, targetClass, joinPointMetaData, aroundAdviceExecutor, beforeAdviceExecutor, afterAdviceExecutor);
        m_signature = (FieldSignature)signature;
        m_rtti = (FieldRtti)rtti;
    }

    /**
     * Walks through the pointcuts and invokes all its advices. When the last advice of the last pointcut has been
     * invoked, the original method is invoked. Is called recursively.
     *
     * @return the result from the next invocation
     * @throws Throwable
     */
    public Object proceed() throws Throwable {
        final Object result = m_aroundAdviceExecutor.proceed(this);
        m_rtti.setFieldValue(result);
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
     * Returns the RTTI for the join point.
     *
     * @return the RTTI
     */
    public Rtti getRtti() {
        return m_rtti;
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
