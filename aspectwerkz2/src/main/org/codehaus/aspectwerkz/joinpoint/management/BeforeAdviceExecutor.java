/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.joinpoint.management;

import org.codehaus.aspectwerkz.IndexTuple;
import org.codehaus.aspectwerkz.System;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

/**
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public class BeforeAdviceExecutor implements AdviceExecutor {

    /**
     * The advices indexes.
     */
    private final IndexTuple[] m_adviceIndexes;

    /**
     * The aspect system.
     */
    private final System m_system;

    /**
     *
     * @param adviceIndexes
     * @param system
     */
    public BeforeAdviceExecutor(final IndexTuple[] adviceIndexes, final System system) {
        m_adviceIndexes = adviceIndexes;
        m_system = system;
    }

    /**
     * Executes its advices one by one. After the last advice has been executed, the original method is invoked.
     *
     * @param joinPoint    the current join point
     * @return             null
     */
    public Object proceed(final JoinPoint joinPoint) throws Throwable {
        for (int i = 0, j = m_adviceIndexes.length; i < j; i++) {
            IndexTuple index = m_adviceIndexes[i];
            int aspectIndex = index.getAspectIndex();
            int methodIndex = index.getMethodIndex();
            m_system.getAspectManager().getAspect(aspectIndex).___AW_invokeAdvice(methodIndex, joinPoint);
        }
        return null;
    }

    /**
     * Creates a deep copy of the advice executor.
     *
     * @return a deep copy of the intance
     */
    public AdviceExecutor deepCopy() {
        return new BeforeAdviceExecutor(m_adviceIndexes, m_system);
    }
}
