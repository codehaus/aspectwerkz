/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.joinpoint.management;

import org.codehaus.aspectwerkz.IndexTuple;

/**
 * Contains the around, before and after advices.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
class AdviceContainer {
    private final IndexTuple[] m_aroundAdvices;
    private final IndexTuple[] m_beforeAdvices;
    private final IndexTuple[] m_afterAdvices;

    public AdviceContainer(final IndexTuple[] aroundAdvices, final IndexTuple[] beforeAdvices,
                           final IndexTuple[] afterAdvices) {
        m_aroundAdvices = aroundAdvices;
        m_beforeAdvices = beforeAdvices;
        m_afterAdvices = afterAdvices;
    }

    public IndexTuple[] getAroundAdvices() {
        return m_aroundAdvices;
    }

    public IndexTuple[] getBeforeAdvices() {
        return m_beforeAdvices;
    }

    public IndexTuple[] getAfterAdvices() {
        return m_afterAdvices;
    }
}
