/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.extension.persistence;

import java.lang.reflect.Method;

/**
 * Container for the index field information.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
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
