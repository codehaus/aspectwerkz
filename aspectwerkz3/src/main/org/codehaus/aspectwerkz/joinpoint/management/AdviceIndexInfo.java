/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.joinpoint.management;

import org.codehaus.aspectwerkz.AdviceIndex;

/**
 * Contains the around, before and after advices.
 * 
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public class AdviceIndexInfo {
    private final AdviceIndex[] m_aroundAdvices;

    private final AdviceIndex[] m_beforeAdvices;

    private final AdviceIndex[] m_afterAdvices;

    public AdviceIndexInfo(final AdviceIndex[] aroundAdvices,
                           final AdviceIndex[] beforeAdvices,
                           final AdviceIndex[] afterAdvices) {
        m_aroundAdvices = aroundAdvices;
        m_beforeAdvices = beforeAdvices;
        m_afterAdvices = afterAdvices;
    }

    public AdviceIndex[] getAroundAdvices() {
        return m_aroundAdvices;
    }

    public AdviceIndex[] getBeforeAdvices() {
        return m_beforeAdvices;
    }

    public AdviceIndex[] getAfterAdvices() {
        return m_afterAdvices;
    }
}