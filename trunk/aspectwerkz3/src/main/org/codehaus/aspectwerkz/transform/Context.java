/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform;

import org.codehaus.aspectwerkz.definition.SystemDefinitionContainer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Transformation context.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class Context {
    /**
    * The class loader for the class being transformed.
    */
    private final ClassLoader m_loader;

    /**
    * Marks the class being transformed as advised.
    */
    private boolean m_advised = false;

    /**
    * Marks the class being transformed as advised.
    */
    private boolean m_prepared = false;

    /**
    * Marks the context as read-only.
    */
    private boolean m_readOnly = false;

    /**
    * Meta-data for the transformation.
    */
    private Map m_metaData = new HashMap();

    /**
    * The contextual list of SystemDefinitions
    */
    private final List m_definitions;

    /**
    * Creates a new context.
    *
    * @param loader the class loader
    */
    public Context(final ClassLoader loader) {
        m_loader = loader;

        // Note: we are not using a lazy loading for the definitions since it is cached anyway
        m_definitions = SystemDefinitionContainer.getHierarchicalDefs(m_loader);
    }

    /**
    * Returns the class loader.
    *
    * @return the class loader
    */
    public ClassLoader getLoader() {
        return m_loader;
    }

    /**
    * The definitions context (with hierarchical structure)
    * @return
    */
    public List getDefinitions() {
        return m_definitions;
    }

    /**
    * Marks the class being transformed as advised. The marker can at most be set once per class per transformer
    */
    public void markAsAdvised() {
        m_advised = true;
    }

    /**
    * Marks the class as prepared.
    */
    public void markAsPrepared() {
        m_prepared = true;
    }

    /**
    * Resets the isAdviced flag.
    */
    public void resetAdvised() {
        m_advised = false;
    }

    /**
    * Checks if the class being transformed has beed advised.
    *
    * @return boolean
    */
    public boolean isAdvised() {
        return m_advised;
    }

    /**
    * Checks if the class is prepared.
    *
    * @return
    */
    public boolean isPrepared() {
        return m_prepared;
    }

    /**
    * Marks the context as read-only.
    */
    public void markAsReadOnly() {
        m_readOnly = true;
    }

    /**
    * Checks if the context is read-only.
    *
    * @return boolean
    */
    public boolean isReadOnly() {
        return m_readOnly;
    }

    /**
    * Returns meta-data for the transformation.
    *
    * @param key the key
    * @return the value
    */
    public Object getMetaData(final Object key) {
        return m_metaData.get(key);
    }

    /**
    * Adds new meta-data for the transformation.
    *
    * @param key   the key
    * @param value the value
    */
    public void addMetaData(final Object key, final Object value) {
        if (m_readOnly) {
            throw new IllegalStateException("context is read only");
        }
        m_metaData.put(key, value);
    }
}
