/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.joinpoint.management;

import org.codehaus.aspectwerkz.IndexTuple;
import org.codehaus.aspectwerkz.aspect.AspectContainer;
import java.io.Serializable;

/**
 * Handles the execution of the around advices.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class AroundAdviceExecutor implements Serializable {
    /**
     * The index of the current advice.
     */
    private int m_currentAdviceIndex = -1;

    /**
     * The stack index.
     */
    private int m_stackIndex = -1;

    /**
     * The advices indexes.
     */
    private final IndexTuple[] m_adviceIndexes;

    /**
     * The join point type.
     */
    private final int m_joinPointType;

    /**
     * Creates a new around advice executor.
     *
     * @param adviceIndexes
     * @param joinPointType
     */
    public AroundAdviceExecutor(final IndexTuple[] adviceIndexes, final int joinPointType) {
        m_adviceIndexes = adviceIndexes;
        m_joinPointType = joinPointType;
    }

    /**
     * Executes its advices one by one. After the last advice has been executed, the original method is invoked.
     *
     * @param joinPoint the current join point
     * @return the result from the next advice in the chain or the invocation of the target method
     */
    public Object proceed(final JoinPointBase joinPoint) throws Throwable {
        // if we have cflow advice then let it run first, then check if we are in cflow
        if (!joinPoint.isInCflow()) {
            return JoinPointBase.invokeJoinPoint(joinPoint, m_joinPointType);
        }
        m_stackIndex++;
        try {
            if (m_stackIndex == 0) {
                if (joinPoint.m_beforeAdviceExecutor.hasAdvices()) {
                    joinPoint.m_beforeAdviceExecutor.proceed(joinPoint);
                }
            }
            Object result = null;
            if (m_currentAdviceIndex == (m_adviceIndexes.length - 1)) {
                m_currentAdviceIndex = -1;
                try {
                    result = JoinPointBase.invokeJoinPoint(joinPoint, m_joinPointType);
                } finally {
                    m_currentAdviceIndex = m_adviceIndexes.length - 1;
                }
            } else {
                m_currentAdviceIndex++;
                try {
                    IndexTuple index = m_adviceIndexes[m_currentAdviceIndex];
                    AspectContainer container = index.getAspectManager().getAspectContainer(index.getAspectIndex());
                    result = container.invokeAdvice(index.getMethodIndex(), joinPoint);
                } finally {
                    m_currentAdviceIndex--;
                }
            }
            if (m_stackIndex == 0) {
                if (joinPoint.m_afterAdviceExecutor.hasAdvices()) {
                    joinPoint.m_afterAdviceExecutor.proceed(joinPoint);
                }
            }
            return result;
        } finally {
            m_stackIndex--;
        }
    }

    /**
     * Checks if the executor has any advices.
     *
     * @return true if it has advices
     */
    public boolean hasAdvices() {
        return m_adviceIndexes.length != 0;
    }

    /**
     * Resets the invocation flow.
     * <p/>
     * Will restart the execution chain of advice.
     */
    public void reset() {
        m_currentAdviceIndex = -1;
        m_stackIndex = -1;
    }
}
