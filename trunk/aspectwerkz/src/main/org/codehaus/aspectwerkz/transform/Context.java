package org.codehaus.aspectwerkz.transform;

import java.util.Map;
import java.util.HashMap;

/**
 * Transformation context.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
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
     * Marks the class being transformed as advised.
     */
    public void markAsAdvised() {
        m_advised = true;
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
     * @param key the key
     * @param value the value
     */
    public void addMetaData(final Object key, final Object value) {
        if (m_readOnly) throw new IllegalStateException("context is read only");
        m_metaData.put(key, value);
    }
}
