/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz;

import org.codehaus.aspectwerkz.aspect.management.AspectManager;

import java.io.ObjectInputStream;
import java.io.Serializable;

/**
 * Contains advice info, like indexes describing the aspect and a method (advice or introduced),
 * aspect manager etc.
 * 
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur </a>
 */
public class AdviceInfo implements Serializable {
    /**
     * Index for the aspect.
     */
    private int m_aspectIndex;

    /**
     * Index for the advice method.
     */
    private int m_methodIndex;

    /**
     * The uuid - informational purpose
     */
    private String m_uuid;

    /**
     * The aspect manager
     */
    private AspectManager m_aspectManager;

    /**
     * The advice method arg index mapped to the target method arg index
     */
    private int[] m_methodToArgIndexes;

    /**
     * Creates a new advice info.
     * 
     * @param aspectIndex the aspect index
     * @param methodIndex the method index
     * @param aspectManager the aspectManager
     */
    public AdviceInfo(final int aspectIndex, final int methodIndex, final AspectManager aspectManager) {
        m_aspectIndex = aspectIndex;
        m_methodIndex = methodIndex;
        m_aspectManager = aspectManager;
    }

    /**
     * Return the aspect index.
     * 
     * @return the aspect index
     */
    public int getAspectIndex() {
        return m_aspectIndex;
    }

    /**
     * Return the method index.
     * 
     * @return the method index
     */
    public int getMethodIndex() {
        return m_methodIndex;
    }

    /**
     * Return the aspectManager.
     * 
     * @return the aspect manager
     */
    public AspectManager getAspectManager() {
        return m_aspectManager;
    }

    /**
     * Sets the advice method to target method arg mapping A value of -1 means "not mapped"
     * 
     * @param map
     */
    public void setMethodToArgIndexes(final int[] map) {
        m_methodToArgIndexes = map;
    }

    /**
     * Returns the advice method to target method arg index mapping.
     * 
     * @return the indexes
     */
    public int[] getMethodToArgIndexes() {
        return m_methodToArgIndexes;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("IndexTuple[");
        sb.append(m_aspectManager).append(',');
        sb.append(m_aspectIndex).append(',');
        sb.append(m_methodIndex).append(']');
        sb.append(hashCode());
        return sb.toString();
    }

    /**
     * Provides custom deserialization.
     * 
     * @param stream the object input stream containing the serialized object
     * @throws Exception in case of failure
     */
    private void readObject(final ObjectInputStream stream) throws Exception {
        ObjectInputStream.GetField fields = stream.readFields();
        m_uuid = (String) fields.get("m_uuid", null);
        m_methodIndex = fields.get("m_methodIndex", 0);
        m_aspectIndex = fields.get("m_aspectIndex", 0);
        m_methodToArgIndexes = (int[])fields.get("m_methodToArgIndexes", null);
        m_aspectManager = SystemLoader.getSystem(Thread.currentThread().getContextClassLoader()).getAspectManager(
            m_uuid);
    }
}