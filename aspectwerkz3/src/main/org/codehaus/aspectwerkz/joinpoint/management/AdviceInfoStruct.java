/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.joinpoint.management;

import java.util.List;

import org.codehaus.aspectwerkz.AdviceInfo;

/**
 * Holds the around, before and after XXX advices.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class AdviceInfoStruct {
    private final AdviceInfo[] m_aroundAdvices;
    private final AdviceInfo[] m_beforeAdvices;
    private final AdviceInfo[] m_afterFinallyAdvices;

    /**
     * After only is a special case of after returning, where the returned value is not checked.
     */
    private final AdviceInfo[] m_afterReturningAdvices;
    private final AdviceInfo[] m_afterThrowingAdvices;

    /**
     * Creates a new info instance.
     *
     * @param aroundAdvices
     * @param beforeAdvices
     * @param afterFinallyAdvices
     * @param afterReturningAdvices
     * @param afterThrowingAdvices
     */
    public AdviceInfoStruct(final List aroundAdvices,
                            final List beforeAdvices,
                            final List afterFinallyAdvices,
                            final List afterReturningAdvices,
                            final List afterThrowingAdvices) {
        m_aroundAdvices = (AdviceInfo[])aroundAdvices.toArray(AdviceInfo.ADVICE_INFO_ARRAY);
        m_beforeAdvices = (AdviceInfo[])beforeAdvices.toArray(AdviceInfo.ADVICE_INFO_ARRAY);
        m_afterFinallyAdvices = (AdviceInfo[])afterFinallyAdvices.toArray(AdviceInfo.ADVICE_INFO_ARRAY);
        m_afterReturningAdvices = (AdviceInfo[])afterReturningAdvices.toArray(AdviceInfo.ADVICE_INFO_ARRAY);
        m_afterThrowingAdvices = (AdviceInfo[])afterThrowingAdvices.toArray(AdviceInfo.ADVICE_INFO_ARRAY);
    }

    public AdviceInfo[] getAroundAdvices() {
        return m_aroundAdvices;
    }

    public AdviceInfo[] getBeforeAdvices() {
        return m_beforeAdvices;
    }

    public AdviceInfo[] getAfterFinallyAdvices() {
        return m_afterFinallyAdvices;
    }

    public AdviceInfo[] getAfterReturningAdvices() {
        return m_afterReturningAdvices;
    }

    public AdviceInfo[] getAfterThrowingAdvices() {
        return m_afterThrowingAdvices;
    }

}