/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.metadata;

/**
 * Holds meta-data for a field. Used by the transformers.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class FieldMetaData implements MemberMetaData {

    /**
     * The name of the method.
     */
    private String m_name;

    /**
     * The type.
     */
    private String m_type;

    /**
     * The modifiers.
     */
    private int m_modifiers;

    /**
     * Returns the name.
     *
     * @return the name
     */
    public String getName() {
        return m_name;
    }

    /**
     * Sets the name.
     *
     * @param name the name
     */
    public void setName(final String name) {
        m_name = name;
    }

    /**
     * Returns the type.
     *
     * @return the type
     */
    public String getType() {
        return m_type;
    }

    /**
     * Sets the type.
     *
     * @param type the type
     */
    public void setType(final String type) {
        m_type = type;
    }

    /**
     * Returns the modifiers.
     *
     * @return the modifiers
     */
    public int getModifiers() {
        return m_modifiers;
    }

    /**
     * Sets the modifiers.
     *
     * @param modifiers the modifiers
     */
    public void setModifiers(final int modifiers) {
        m_modifiers = modifiers;
    }
}
