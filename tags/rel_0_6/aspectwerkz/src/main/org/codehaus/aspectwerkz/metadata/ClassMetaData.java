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
package org.codehaus.aspectwerkz.metadata;

import java.util.List;
import java.util.ArrayList;
import java.io.Serializable;

/**
 * Holds meta-data for a class.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: ClassMetaData.java,v 1.2 2003-06-17 16:07:55 jboner Exp $
 */
public class ClassMetaData implements MetaData, Serializable {

    /**
     * The name of the class.
     */
    private String m_name;

    /**
     * A list with the <code>MethodMetaData</code> instances.
     */
    private List m_methods = new ArrayList();

    /**
     * A list with the <code>FieldMetaData</code> instances.
     */
    private List m_fields = new ArrayList();

    /**
     * Returns the name of the class.
     *
     * @return the name of the class
     */
    public String getClassName() {
        return m_name;
    }

    /**
     * Sets the name of the class.
     *
     * @param name the name of the class
     */
    public void setName(final String name) {
        m_name = name;
    }

    /**
     * Returns a list with all the methods meta-data.
     *
     * @return the methods meta-data
     */
    public List getMethods() {
        return m_methods;
    }

    /**
     * Sets a list with <code>MethodMetaData</code> instances.
     *
     * @param methods a list with <code>MethodMetaData</code> instances
     */
    public void setMethods(final List methods) {
        m_methods = methods;
    }

    /**
     * Returns a list with all the field meta-data.
     *
     * @return the field meta-data
     */
    public List getFields() {
        return m_fields;
    }

    /**
     * Sets a list with <code>FieldMetaData</code> instances.
     *
     * @param methods a list with <code>FieldMetaData</code> instances
     */
    public void setFields(final List fields) {
        m_fields = fields;
    }
}

