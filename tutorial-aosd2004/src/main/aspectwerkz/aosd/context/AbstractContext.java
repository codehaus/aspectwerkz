/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD-style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package aspectwerkz.aosd.context;

import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

/**
 * Abstract base class that provides default implementation of the <tt>Context</tt> interface.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public abstract class AbstractContext implements Context {

    protected final Map m_env = new HashMap();
    protected boolean m_readOnly = false;

    /**
     * Retrieve an item from the context.
     *
     * @param key the key of item
     * @return the item stored in context
     * @throws aspectwerkz.aosd.context.ContextException if item not present
     */
    public Object get(final Object key) throws ContextException {
        if (key == null) throw new IllegalArgumentException("key can not be null");
        Object data = m_env.get(key);
        if (data == null) throw new ContextException("no such key exists");
        return data;
    }

    /**
     * Helper method fo adding items to context.
     *
     * @param key the items key
     * @param value the item
     * @exception java.lang.IllegalStateException if context is read only
     */
    public void put(final Object key, final Object value) throws IllegalStateException {
        if (isReadOnly()) throw new IllegalStateException("context is read only");
        m_env.put(key, value);
    }

    /**
     * Make the context read-only.
     * <p/>Any attempt to write to the context via put() will result in an IllegalStateException.
     */
    public void markReadOnly() {
        m_readOnly = true;
    }

    /**
     * Checks if the context is read only.
     *
     * @return boolean
     */
    public boolean isReadOnly() {
        return m_readOnly;
    }

    /**
     * Overridden toString() method.
     *
     * @return the string representation for the context
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        for (Iterator it = m_env.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry)it.next();
            buffer.append(entry.getKey().toString());
            buffer.append("=>");
            buffer.append(entry.getValue().toString());
            buffer.append(' ');
        }
        return buffer.toString();
    }
}
