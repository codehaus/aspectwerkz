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
import org.codehaus.aspectwerkz.CrossCuttingInfo;
import org.codehaus.aspectwerkz.aspect.management.AspectManager;
import org.codehaus.aspectwerkz.aspect.AspectContainer;

import java.lang.reflect.Method;

/**
 * Handles the execution of the around advices.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class AroundAdviceExecutor {

    /**
     * The index of the current advice.
     */
    private int m_currentAdviceIndex = -1;

    private int m_stackIndex = -1;

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
            final ISystem system,
            final int joinPointType) {
        m_adviceIndexes = adviceIndexes;
        m_system = system;
        m_aspectManagers = m_system.getAspectManagers();
        m_joinPointType = joinPointType;

//        System.out.println("AroundAdviceExecutor.new");
//        for (int i = 0; i < m_adviceIndexes.length; i++) {
//            IndexTuple index = m_adviceIndexes[i];
//            System.out.println("  m_adviceIndex = " + index);
//
//            AspectManager am = m_aspectManagers[index.getAspectManagerIndex()];
//            AspectContainer aspectC = am.getAspectContainer(index.getAspectIndex());
//            Method advice = aspectC.getAdvice(index.getMethodIndex());
//            System.out.println("  advice = " + am.getUuid()+":"+aspectC.getCrossCuttingInfo().getName()+":"+advice);
//        }
    }

    /**
     * Executes its advices one by one. After the last advice has been executed, the original method is invoked.
     *
     * @param joinPoint the current join point
     * @return the result from the next advice in the chain or the invocation of the target method
     */
    public Object proceed(final JoinPointBase joinPoint) throws Throwable {
        //System.out.println("AroundAdviceExecutor.proceed " + joinPoint);
        if (!joinPoint.isInCflow()) {
            return JoinPointBase.invokeJoinPoint(joinPoint, m_joinPointType);
        }
        //System.out.println("m_stackIndex = " + m_stackIndex);
        //System.out.println("m_currentAdviceIndex = " + m_currentAdviceIndex);
        m_stackIndex++;
        try {
            if (m_stackIndex == 0) {
                if (joinPoint.m_beforeAdviceExecutor.hasAdvices()) {
                    joinPoint.m_beforeAdviceExecutor.proceed(joinPoint);
                }
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
                    result = m_aspectManagers[index.getAspectManagerIndex()]
                                .getAspectContainer(index.getAspectIndex())
                                .invokeAdvice(index.getMethodIndex(), joinPoint);
                }
                finally {
                    m_currentAdviceIndex--;
                }
            }

            if (m_stackIndex == 0) {
                if (joinPoint.m_afterAdviceExecutor.hasAdvices()) {
                    joinPoint.m_afterAdviceExecutor.proceed(joinPoint);
                }
            }

            return result;
        }
        finally {
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
}
