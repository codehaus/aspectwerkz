/*
 * AspectWerkz - a dynamic, lightweight and high-performant AOP/AOSD framework for Java.
 * Copyright (C) 2002-2003  Jonas Bonér. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
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
 * Type-safe enum for the different container types.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: ContainerType.java,v 1.1 2003-06-17 15:04:39 jboner Exp $
 */
public class ContainerType implements Serializable {

    public static final ContainerType TRANSIENT = new ContainerType("TRANSIENT");
    public static final ContainerType PERSISTENT = new ContainerType("PERSISTENT");

    private final String m_name;

    /**
     * Creates a new type.
     *
     * @param name
     */
    private ContainerType(final String name) {
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
