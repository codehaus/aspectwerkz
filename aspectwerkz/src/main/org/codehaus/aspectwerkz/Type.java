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
package org.codehaus.aspectwerkz;

import java.io.Serializable;

/**
 * Type-safe enum for the different Java language types.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: Type.java,v 1.3 2003-07-03 13:10:49 jboner Exp $
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
