/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.advice;

/**
 * Holds an advice/index tuple.
 * Used when reordering of advices are necessary.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public final class AdviceIndexTuple {

    /**
     * The index for the advice.
     */
    private final int m_index;

    /**
     * The mapped name for the advice.
     */
    private final String m_name;

    /**
     * Sets the advice name A the index.
     *
     * @param name the name of the advice
     * @param index the index of the advice
     */
    public AdviceIndexTuple(final String name, final int index) {
        m_name = name;
        m_index = index;
    }

    /**
     * Returns the name of the advice.
     *
     * @return the name of the advice
     */
    public String getName() {
        return m_name;
    }

    /**
     * Returns the index for the advice.
     *
     * @return the index
     */
    public int getIndex() {
        return m_index;
    }
}
