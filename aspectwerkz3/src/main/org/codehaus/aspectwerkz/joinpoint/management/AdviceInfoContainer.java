/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.joinpoint.management;

import java.util.List;

import org.codehaus.aspectwerkz.AdviceInfo;

/**
 * Container for the advice infos that belongs to a specific join point.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r </a>
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class AdviceInfoContainer {

    private final AdviceInfo[] m_aroundAdvices;
    private final AdviceInfo[] m_beforeAdvices;
    private final AdviceInfo[] m_afterFinallyAdvices;
    private final AdviceInfo[] m_afterReturningAdvices;
    private final AdviceInfo[] m_afterThrowingAdvices;

    /**
     * Creates a advice info container.
     *
     * @param aroundAdvices
     * @param beforeAdvices
     * @param afterFinallyAdvices
     * @param afterReturningAdvices
     * @param afterThrowingAdvices
     */
    public AdviceInfoContainer(final List aroundAdvices,
                               final List beforeAdvices,
                               final List afterFinallyAdvices,
                               final List afterReturningAdvices,
                               final List afterThrowingAdvices) {
        m_aroundAdvices = (AdviceInfo[]) aroundAdvices.toArray(AdviceInfo.EMPTY_ADVICE_INFO_ARRAY);
        m_beforeAdvices = (AdviceInfo[]) beforeAdvices.toArray(AdviceInfo.EMPTY_ADVICE_INFO_ARRAY);
        m_afterFinallyAdvices = (AdviceInfo[]) afterFinallyAdvices.toArray(AdviceInfo.EMPTY_ADVICE_INFO_ARRAY);
        m_afterReturningAdvices = (AdviceInfo[]) afterReturningAdvices.toArray(AdviceInfo.EMPTY_ADVICE_INFO_ARRAY);
        m_afterThrowingAdvices = (AdviceInfo[]) afterThrowingAdvices.toArray(AdviceInfo.EMPTY_ADVICE_INFO_ARRAY);
    }

    /**
     * Returns the around advice infos.
     *
     * @return
     */
    public AdviceInfo[] getAroundAdviceInfos() {
        return m_aroundAdvices;
    }

    /**
     * Returns the before advice infos.
     *
     * @return
     */
    public AdviceInfo[] getBeforeAdviceInfos() {
        return m_beforeAdvices;
    }

    /**
     * Returns the after finally advice infos.
     *
     * @return
     */
    public AdviceInfo[] getAfterFinallyAdviceInfos() {
        return m_afterFinallyAdvices;
    }

    /**
     * Returns the after returning advice infos.
     *
     * @return
     */
    public AdviceInfo[] getAfterReturningAdviceInfos() {
        return m_afterReturningAdvices;
    }

    /**
     * Returns the after throwing advice infos.
     *
     * @return
     */
    public AdviceInfo[] getAfterThrowingAdviceInfos() {
        return m_afterThrowingAdvices;
    }

}