/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz;

import org.codehaus.aspectwerkz.aspect.AdviceType;
import org.codehaus.aspectwerkz.transform.inlining.AsmHelper;

import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * Contains advice info, like indexes describing the aspect and a method (advice or introduced),
 * aspect manager etc.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r </a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur </a>
 */
public class AdviceInfo implements Serializable {

    /**
     * The advice method.
     */
    private Method m_method;

    /**
     * The aspect context.
     */
    private AspectContext m_aspectContext;

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
     * @param aspectContext         the aspect context
     * @param method         the method
     * @param type                the advice type
     * @param specialArgumentType the special arg type
     */
    public AdviceInfo(final AspectContext aspectContext,
                      final Method method,
                      final AdviceType type,
                      final String specialArgumentType) {
        m_aspectContext = aspectContext;
        m_method = method;
        m_type = type;
        m_specialArgumentType = AsmHelper.convertReflectDescToTypeDesc(specialArgumentType);
    }

    /**
     * Returns the name of the advice.
     *
     * @param aspectName
     * @param adviceName
     * @return the name
     */
    public static String createAdviceName(final String aspectName, final String adviceName) {
        return new StringBuffer().append(aspectName).append('/').append(adviceName).toString();
    }

    /**
     * Returns the aspect context.
     *
     * @return the aspect context
     */
    public AspectContext getAspectContext() {
        return m_aspectContext;
    }

    /**
     * Returns the name of the advice.
     *
     * @return
     */
    public String getName() {
        return createAdviceName(m_aspectContext.getName(), m_method.getName());
    }

    /**
     * Return the method.
     *
     * @return the method
     */
    public Method getMethod() {
        return m_method;
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
        sb.append(m_aspectContext.getName()).append(',');
        sb.append(m_method.getName()).append(',');
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
        m_aspectContext = (AspectContext) fields.get("m_aspectContext", null);
        m_type = (AdviceType) fields.get("m_type", null);
        m_method = (Method)fields.get("m_method", null);
        m_methodToArgIndexes = (int[]) fields.get("m_methodToArgIndexes", null);
        m_specialArgumentType = (String) fields.get("m_specialArgumentType", null);
    }
}