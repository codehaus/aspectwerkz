/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.joinpoint.management;

import org.codehaus.aspectwerkz.IndexTuple;
import org.codehaus.aspectwerkz.System;
import org.codehaus.aspectwerkz.CrossCutting;
import org.codehaus.aspectwerkz.aspect.management.AspectManager;

/**
 * Handles the execution of the after advices.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public class AfterAdviceExecutor {

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
     * Creates a new advice executor.
     *
     * @param adviceIndexes
     * @param system
     */
    public AfterAdviceExecutor(final IndexTuple[] adviceIndexes, final System system) {
        m_adviceIndexes = adviceIndexes;
        m_system = system;
        m_aspectManager = m_system.getAspectManager();
    }

    /**
     * Executes its advices one by one. After the last advice has been executed, the original method is invoked.
     *
     * @param joinPoint the current join point
     * @return null
     */
    public Object proceed(final JoinPointBase joinPoint) throws Throwable {
        if (!joinPoint.isInCflow()) {
            return null;
        }
        for (int i = m_adviceIndexes.length - 1; i >= 0; i--) {
            IndexTuple index = m_adviceIndexes[i];
            int aspectIndex = index.getAspectIndex();
            int methodIndex = index.getMethodIndex();
            m_aspectManager.getAspectPrototype(aspectIndex).getCrossCuttingInfo().invokeAdvice(methodIndex, joinPoint);
        }
        return null;
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
