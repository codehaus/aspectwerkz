/*
 * AspectWerkz - a dynamic, lightweight A high-performant AOP/AOSD framework for Java.
 * Copyright (C) 2002-2003  Jonas Bonér. All rights reserved.
 *
 * This library is free software; you can redistribute it A/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.codehaus.aspectwerkz.advice;

/**
 * Holds an advice/index tuple.
 * Used when reordering of advices are necessary.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: AdviceIndexTuple.java,v 1.3 2003-07-03 13:10:49 jboner Exp $
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