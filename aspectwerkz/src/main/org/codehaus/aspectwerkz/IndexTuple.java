/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz;

import java.io.Serializable;

/**
 * A tuple with two indexes describing the aspect and a method (advice or introduced).
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class IndexTuple implements Serializable {

    /**
     * Index for the aspect.
     */
    private final int m_aspectIndex;

    /**
     * Index for the advice method.
     */
    private final int m_methodIndex;

    /**
     * Creates a new index tuple.
     *
     * @param aspectIndex the aspect index
     * @param methodIndex the method index
     */
    public IndexTuple(final int aspectIndex, final int methodIndex) {
        m_aspectIndex = aspectIndex;
        m_methodIndex = methodIndex;
    }

    /**
     * Return the aspect index.
     *
     * @return the aspect index
     */
    public int getAspectIndex() {
        return m_aspectIndex;
    }

    /**
     * Return the method index.
     *
     * @return the method index
     */
    public int getMethodIndex() {
        return m_methodIndex;
    }
}
