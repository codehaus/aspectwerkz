/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz;

import java.io.Serializable;

/**
 * Type-safe enum for the different Java language types.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class Type implements Serializable {

    public static final Type OBJECT = new Type("OBJECT");
    public static final Type LONG = new Type("LONG");
    public static final Type INT = new Type("INT");
    public static final Type SHORT = new Type("SHORT");
    public static final Type DOUBLE = new Type("DOUBLE");
    public static final Type FLOAT = new Type("FLOAT");
    public static final Type BYTE = new Type("BYTE");
    public static final Type BOOLEAN = new Type("BOOLEAN");
    public static final Type CHAR = new Type("CHAR");

    /**
     * The name of the type.
     */
    private final String m_name;

    /**
     * Returns the name of the type.
     *
     * @return the name of the type
     */
    public String toString() {
        return m_name;
    }

    /**
     * Private constructor to prevent instantiation A subclassing.
     *
     * @param name the name of the type
     */
    private Type(final String name) {
        m_name = name;
    }
}
