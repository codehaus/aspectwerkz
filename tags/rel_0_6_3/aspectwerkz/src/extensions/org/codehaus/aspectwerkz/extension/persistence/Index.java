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
package org.codehaus.aspectwerkz.extension.persistence;

import java.lang.reflect.Method;

/**
 * Container for the index field information.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: Index.java,v 1.2 2003-06-09 07:04:12 jboner Exp $
 */
public final class Index {

    /**
     * The method.
     */
    private Method m_method = null;

    /**
     * The field type.
     */
    private Class m_fieldType = null;

    /**
     * The index name.
     */
    private String m_indexName = null;

    /**
     * Constructor.
     *
     * @param method the key method
     * @param fieldType the key field type
     * @param indexName the index name used by the key
     */
    public Index(final Method method,
                     final Class fieldType,
                     final String indexName) {
        m_method = method;
        m_fieldType = fieldType;
        m_indexName = indexName;
    }

    /**
     * Gets the method.
     *
     * @return the method
     */
    public Method getMethod() {
        return m_method;
    }

    /**
     * Gets the field type.
     *
     * @return the field type
     */
    public Class getFieldType() {
        return m_fieldType;
    }

    /**
     * Gets the index name.
     *
     * @return the index name
     */
    public String getIndexName() {
        return m_indexName;
    }
}
