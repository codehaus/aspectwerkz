/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz;

import org.codehaus.aspectwerkz.aspect.management.AspectManager;

import java.io.Serializable;

/**
 * A tuple with two indexes describing the aspect and a method (advice or introduced).
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class IndexTuple implements Serializable {

    /**
     * Index for the aspect.
     */
    private final int m_aspectIndex;

    /**
     * Index for the advice method.
     */
    private final int m_methodIndex;

    /**
     * The uuid - informational purpose
     */
    private final String m_uuid;

    /**
     * The aspect manager
     */
    private final AspectManager m_aspectManager;

    /**
     * Creates a new index tuple.
     *
     * @param aspectIndex the aspect index
     * @param methodIndex the method index
     */
    public IndexTuple(final int aspectIndex, final int methodIndex, final String uuid, final AspectManager aspectManager) {
        m_aspectIndex = aspectIndex;
        m_methodIndex = methodIndex;
        m_uuid = uuid;
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

    public String toString() {
        StringBuffer sb = new StringBuffer("IndexTuple[");
        sb.append(m_aspectManager).append(",");
        sb.append(m_aspectIndex).append(",");
        sb.append(m_methodIndex).append("]");
        sb.append(hashCode());
        return sb.toString();
    }
}
