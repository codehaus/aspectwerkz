/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform;

import java.util.Map;
import java.util.HashMap;

import javassist.ClassPool;
import javassist.LoaderClassPath;

/**
 * Transformation context.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class Context {

    /**
     * The class loader for the class being transformed.
     */
    private final ClassLoader m_loader;

    /**
     * The Javassist Repository based on the context class loader.
     */
    private final ClassPool m_repository;

    /**
     * The mixin meta-data repository.
     */
    private Map m_metaDataRepository;

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
     * Creates a new context.
     *
     * @param loader the class loader
     */
    public Context(final ClassLoader loader) {
        m_loader = loader;
        m_repository = new ClassPool(null);
        m_repository.insertClassPath(new LoaderClassPath(loader));
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
     * Returns the repository.
     *
     * @return the Javassist Repository based on context class loader
     */
    public ClassPool getClassPool() {
        return m_repository;
    }

    /**
     * Returns the meta-data repository.
     *
     * @return the meta-data repository
     */
    public Map getMetaDataRepository() {
        return m_metaDataRepository;
    }

    /**
     * Sets the meta-data repository.
     *
     * @param repository the meta-data repository
     */
    public void setMetaDataRepository(final Map repository) {
        m_metaDataRepository = repository;
    }

    /**
     * Marks the class being transformed as advised. The marker can at most be set once per class per transformer
     */
    public void markAsAdvised() {
        m_advised = true;
    }

    public void markAsPrepared() {
        m_prepared = true;
    }

    /**
     * Checks if the class being transformed has beed advised.
     *
     * @return boolean
     */
    public boolean isAdvised() {
        return m_advised;
    }

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
        if (m_readOnly) throw new IllegalStateException("context is read only");
        m_metaData.put(key, value);
    }

}
