/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz;

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
     * Index for the aspect manager within the system
     * (the system is tight to the affected classloader, thus the join point)
     */
    private final int m_aspectManagerIndex;

    /**
     * Creates a new index tuple.
     *
     * @param aspectIndex the aspect index
     * @param methodIndex the method index
     */
    public IndexTuple(final int aspectIndex, final int methodIndex, final String uuid, final int aspectManagerIndex) {
        m_aspectIndex = aspectIndex;
        m_methodIndex = methodIndex;
        m_uuid = uuid;
        m_aspectManagerIndex = aspectManagerIndex;
        //java.lang.System.out.println("tuple = " + aspectIndex +","+methodIndex+","+uuid+", "+aspectManagerIndex+", " + this.toString());
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
     * Return the aspectManager index.
     * Note that the index is not unique accross a VM, but unique from a classloader point of view
     * in its hierarchy.
     *
     * @return the aspect manager index
     */
    public int getAspectManagerIndex() {
        return m_aspectManagerIndex;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("IndexTuple[");
        sb.append(m_aspectManagerIndex).append(",");
        sb.append(m_aspectIndex).append(",");
        sb.append(m_methodIndex).append("]");
        sb.append(hashCode());
        return sb.toString();
    }
}
