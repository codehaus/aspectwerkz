/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.joinpoint.management;

import org.codehaus.aspectwerkz.IndexTuple;
import org.codehaus.aspectwerkz.System;
import org.codehaus.aspectwerkz.aspect.management.AspectManager;

/**
 * Handles the execution of the around advices.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
class AroundAdviceExecutor {

    /**
     * The index of the current advice.
     */
    private int m_currentAdviceIndex = -1;

    /**
     * The advices indexes.
     */
    private final IndexTuple[] m_adviceIndexes;

    /**
     * The aspect system.
     */
    private final System m_system;

    /**
     * The aspect manager.
     */
    private final AspectManager m_aspectManager;

    /**
     * The join point type.
     */
    private final int m_joinPointType;

    /**
     * Creates a new around advice executor.
     *
     * @param adviceIndexes
     * @param system
     * @param joinPointType
     */
    public AroundAdviceExecutor(
            final IndexTuple[] adviceIndexes,
            final System system,
            final int joinPointType) {
        m_adviceIndexes = adviceIndexes;
        m_system = system;
        m_aspectManager = m_system.getAspectManager();
        m_joinPointType = joinPointType;
    }

    /**
     * Executes its advices one by one. After the last advice has been executed, the original method is invoked.
     *
     * @param joinPoint the current join point
     * @return the result from the next advice in the chain or the invocation of the target method
     */
    public Object proceed(final JoinPointBase joinPoint) throws Throwable {
        if (!joinPoint.isInCflow()) {
            return JoinPointBase.invokeJoinPoint(joinPoint, m_joinPointType);
        }
        Object result = null;
        if (m_currentAdviceIndex == m_adviceIndexes.length - 1) {
            m_currentAdviceIndex = -1;
            try {
                result = JoinPointBase.invokeJoinPoint(joinPoint, m_joinPointType);
            }
            finally {
                m_currentAdviceIndex = m_adviceIndexes.length - 1;
            }
        }
        else {
            m_currentAdviceIndex++;
            try {
                IndexTuple index = m_adviceIndexes[m_currentAdviceIndex];
                result = m_aspectManager.getAspect(index.getAspectIndex()).
                        ___AW_invokeAdvice(index.getMethodIndex(), joinPoint);
            }
            finally {
                m_currentAdviceIndex--;
            }
        }
        return result;
    }

    /**
     * Checks if the executor has any advices.
     *
     * @return true if it has advices
     */
    public boolean hasAdvices() {
        return m_adviceIndexes.length != 0;
    }
}
