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
package org.codehaus.aspectwerkz.persistence;

import java.io.Serializable;

/**
 * Implements a container for the modified field.
 * Holds the field name, the new value and the type of the field.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: ModifiedField.java,v 1.1 2003-06-17 16:23:24 jboner Exp $
 */
public final class ModifiedField implements Serializable {

    /**
     * Holds the name of the field.
     */
    private final String m_name;

    /**
     * Holds the current value of the field.
     */
    private final Object m_value;

    /**
     * Creates a new instance and sets the name, value and type.
     *
     * @param name the name of the field
     * @param value the new value
     */
    public ModifiedField(final String name, final Object value) {
        if (name == null) throw new IllegalArgumentException("field name can not be null");
        m_name = name;
        m_value = value;
    }

    /**
     * Returns the name of the field.
     *
     * @return the name of the field
     */
    public String getName() {
        return m_name;
    }

    /**
     * Returns the new value of the field.
     *
     * @return the new value of the field
     */
    public Object getValue() {
        return m_value;
    }
}

