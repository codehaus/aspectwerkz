/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.joinpoint.management;

import org.codehaus.aspectwerkz.joinpoint.CatchClauseRtti;
import org.codehaus.aspectwerkz.joinpoint.CatchClauseSignature;
import org.codehaus.aspectwerkz.joinpoint.Rtti;
import org.codehaus.aspectwerkz.joinpoint.Signature;
import java.util.List;

/**
 * Abstraction of a catch clause join point.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
class CatchClauseJoinPoint extends JoinPointBase {
    private final CatchClauseSignature m_signature;
    private final CatchClauseRtti m_rtti;

    /**
     * Creates a new join point.
     *
     * @param targetClass
     * @param signature
     * @param rtti
     * @param joinPointMetaData
     * @param aroundAdviceExecutor
     * @param beforeAdviceExecutor
     * @param afterAdviceExecutor
     */
    public CatchClauseJoinPoint(final Class targetClass, final Signature signature, final Rtti rtti,
                                final JoinPointMetaData joinPointMetaData, final AroundAdviceExecutor aroundAdviceExecutor,
                                final BeforeAdviceExecutor beforeAdviceExecutor,
                                final AfterAdviceExecutor afterAdviceExecutor) {
        super(JoinPointType.HANDLER, targetClass, joinPointMetaData, aroundAdviceExecutor, beforeAdviceExecutor,
              afterAdviceExecutor);
        m_signature = (CatchClauseSignature)signature;
        m_rtti = (CatchClauseRtti)rtti;
    }

    /**
     * Walks through the pointcuts and invokes all its advices. When the last advice of the last pointcut has been
     * invoked, the original method is invoked. Is called recursively.
     *
     * @return the result from the next invocation
     * @throws Throwable
     * @TODO: which advices should we support for catch handlers? AspectJ only supports before, due to bytecode problems
     * (not possible to detect the end of a catch clause with 100% accuracy).
     */
    public Object proceed() throws Throwable {
        if (m_beforeAdviceExecutor.hasAdvices()) {
            m_beforeAdviceExecutor.proceed(this);
        }
        return null;
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
