/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz;

import org.codehaus.aspectwerkz.aspect.management.AspectManager;
import org.codehaus.aspectwerkz.aspect.AdviceType;
import org.codehaus.aspectwerkz.transform.AsmHelper;
import org.codehaus.aspectwerkz.transform.AsmHelper;

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
    private transient AspectManager m_aspectManager;

    /**
     * The advice method arg index mapped to the target method arg index
     */
    private int[] m_methodToArgIndexes;

    /**
     * The "special" argument for the advice.
     */
    private String m_specialArgumentType;

    /**
     * The advice type.
     */
    private AdviceType m_type;

    /**
     * Creates a new advice info.
     *
     * @param aspectIndex         the aspect index
     * @param methodIndex         the method index
     * @param aspectManager       the aspectManager
     * @param type                the advice type
     * @param specialArgumentType the special arg type
     */
    public AdviceInfo(final int aspectIndex,
                      final int methodIndex,
                      final AspectManager aspectManager,
                      final AdviceType type,
                      final String specialArgumentType) {
        m_aspectIndex = aspectIndex;
        m_methodIndex = methodIndex;
        m_aspectManager = aspectManager;
        m_type = type;
        m_specialArgumentType = AsmHelper.convertReflectDescToTypeDesc(specialArgumentType);
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

    /**
     * Returns the special argument type.
     *
     * @return
     */
    public String getSpecialArgumentType() {
        return m_specialArgumentType;
    }

    /**
     * Returns the advice type.
     *
     * @return
     */
    public AdviceType getType() {
        return m_type;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("AdviceInfo[");
        sb.append(m_type).append(',');
        sb.append(m_aspectManager).append(',');
        sb.append(m_aspectIndex).append(',');
        sb.append(m_methodIndex).append(',');
        sb.append(m_specialArgumentType).append(']');
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
        m_type = (AdviceType) fields.get("m_type", null);
        m_methodIndex = fields.get("m_methodIndex", 0);
        m_aspectIndex = fields.get("m_aspectIndex", 0);
        m_methodToArgIndexes = (int[]) fields.get("m_methodToArgIndexes", null);
        m_specialArgumentType = (String) fields.get("m_specialArgumentType", null);
        m_aspectManager = SystemLoader.getSystem(
                Thread.currentThread().
                getContextClassLoader()
        ).getAspectManager(m_uuid);
    }
}