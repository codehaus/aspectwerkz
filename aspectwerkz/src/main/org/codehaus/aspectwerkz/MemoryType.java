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
 * Type-safe enum for the different memory types.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class MemoryType implements Serializable {

    public static final MemoryType TRANSIENT = new MemoryType("TRANSIENT");
    public static final MemoryType PERSISTENT = new MemoryType("PERSISTENT");

    private final String m_name;

    /**
     * Creates a new type.
     *
     * @param name
     */
    private MemoryType(String name) {
        m_name = name;
    }

    /**
     * String representation for the type.
     *
     * @return the string representation for the type
     */
    public String toString() {
        return m_name;
    }
}
