/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz;

/**
 * Holds an name/index tuple.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public final class NameIndexTuple {
    /**
     * The index.
     */
    private final IndexTuple m_index;

    /**
     * The name.
     */
    private final String m_name;

    /**
     * Sets the name and the advice index.
     *
     * @param name  the name
     * @param index the index
     */
    public NameIndexTuple(final String name, final IndexTuple index) {
        m_name = name;
        m_index = index;
    }

    /**
     * Returns the name.
     *
     * @return the name
     */
    public String getName() {
        return m_name;
    }

    /**
     * Returns the index.
     *
     * @return the index
     */
    public IndexTuple getIndex() {
        return m_index;
    }
}
