/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.joinpoint.management;

import org.codehaus.aspectwerkz.IndexTuple;
import org.codehaus.aspectwerkz.ISystem;
import org.codehaus.aspectwerkz.AOPCSystem;
import org.codehaus.aspectwerkz.aspect.management.AspectManager;

/**
 * Handles the execution of the before advices.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class BeforeAdviceExecutor {

    /**
     * The advices indexes.
     */
    private final IndexTuple[] m_adviceIndexes;

    /**
     * The aspect system.
     */
    private final ISystem m_system;

    /**
     * The aspect manager.
     */
    private final AspectManager[] m_aspectManagers;

    /**
     * Creates a new advice executor.
     *
     * @param adviceIndexes
     * @param system
     */
    public BeforeAdviceExecutor(final IndexTuple[] adviceIndexes, final ISystem system) {
        m_adviceIndexes = adviceIndexes;
        m_system = system;
        m_aspectManagers = m_system.getAspectManagers();//TODO remove - not needed
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
        for (int i = 0, j = m_adviceIndexes.length; i < j; i++) {
            IndexTuple index = m_adviceIndexes[i];
            int aspectIndex = index.getAspectIndex();
            int methodIndex = index.getMethodIndex();
            m_aspectManagers[index.getAspectManagerIndex()]
                .getAspectContainer(aspectIndex).invokeAdvice(methodIndex, joinPoint);
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
