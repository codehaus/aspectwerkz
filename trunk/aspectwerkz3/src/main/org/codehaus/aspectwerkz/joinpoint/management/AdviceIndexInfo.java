/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.joinpoint.management;

import org.codehaus.aspectwerkz.AdviceInfo;

/**
 * Contains the around, before and after advices.
 * 
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public class AdviceIndexInfo {
    private final AdviceInfo[] m_aroundAdvices;

    private final AdviceInfo[] m_beforeAdvices;

    private final AdviceInfo[] m_afterAdvices;

    public AdviceIndexInfo(final AdviceInfo[] aroundAdvices,
                           final AdviceInfo[] beforeAdvices,
                           final AdviceInfo[] afterAdvices) {
        m_aroundAdvices = aroundAdvices;
        m_beforeAdvices = beforeAdvices;
        m_afterAdvices = afterAdvices;
    }

    public AdviceInfo[] getAroundAdvices() {
        return m_aroundAdvices;
    }

    public AdviceInfo[] getBeforeAdvices() {
        return m_beforeAdvices;
    }

    public AdviceInfo[] getAfterAdvices() {
        return m_afterAdvices;
    }
}