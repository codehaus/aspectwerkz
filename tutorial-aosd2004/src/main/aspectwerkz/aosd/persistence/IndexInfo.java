/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD-style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package aspectwerkz.aosd.persistence;

import java.lang.reflect.Method;

/**
 * Placeholder for the index field information.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public final class IndexInfo {

    private Method m_method = null;
    private Class m_fieldType = null;
    private String m_indexName = null;

    /**
     * Creates a new index info instance.
     *
     * @param method the key method
     * @param fieldType the key field type
     * @param indexName the index name used by the key
     */
    public IndexInfo(final Method method, final Class fieldType, final String indexName) {
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
