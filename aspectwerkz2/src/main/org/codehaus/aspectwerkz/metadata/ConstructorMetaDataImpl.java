/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.metadata;

import org.codehaus.aspectwerkz.definition.attribute.CustomAttribute;

import java.util.List;
import java.util.ArrayList;

/**
 * Holds meta-data for a constructor.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class ConstructorMetaDataImpl implements ConstructorMetaData {

    /**
     * The name of the constructor.
     */
    private String m_name;

    /**
     * The modifiers.
     */
    private int m_modifiers;

    /**
     * A list with the parameter types.
     */
    private String[] m_parameterTypes;

    /**
     * A list with the exception types.
     */
    private String[] m_exceptionTypes;

    /**
     * The attributes.
     */
    private List m_attributes = new ArrayList();

    /**
     * Returns the attributes.
     *
     * @return the attributes
     */
    public List getAttributes() {
        return m_attributes;
    }

    /**
     * Adds an attribute.
     *
     * @param attribute the attribute
     */
    public void addAttribute(final CustomAttribute attribute) {
        m_attributes.add(attribute);
    }

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

    /**
     * Returns the parameter types.
     *
     * @return the parameter types
     */
    public String[] getParameterTypes() {
        return m_parameterTypes;
    }

    /**
     * Sets the parameter types.
     *
     * @param parameterTypes the parameter types
     */
    public void setParameterTypes(final String[] parameterTypes) {
        m_parameterTypes = parameterTypes;
    }

    /**
     * Returns the exception types.
     *
     * @return the exception types
     */
    public String[] getExceptionTypes() {
        return m_exceptionTypes;
    }

    /**
     * Sets the exception types
     *
     * @param exceptionTypes the exception types
     */
    public void setExceptionTypes(final String[] exceptionTypes) {
        m_exceptionTypes = exceptionTypes;
    }

    /**
     * The overridden equals method.
     *
     * @param o the other object
     * @return boolean
     */
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ConstructorMetaData)) {
            return false;
        }
        final ConstructorMetaDataImpl obj = (ConstructorMetaDataImpl)o;
        return areEqualsOrBothNull(obj.m_name, this.m_name) &&
               areStringArraysEqual(obj.m_parameterTypes, this.m_parameterTypes) &&
               areStringArraysEqual(obj.m_exceptionTypes, this.m_exceptionTypes) &&
               obj.m_modifiers == this.m_modifiers;
    }

    /**
     * The overridden equals method.
     *
     * @return the hash code
     */
    public int hashCode() {
        int result = 17;
        result = 37 * result + hashCodeOrZeroIfNull(m_name);
        if (m_parameterTypes != null) {
            for (int i = 0; i < m_parameterTypes.length; i++) {
                result = 37 * result + hashCodeOrZeroIfNull(m_parameterTypes[i]);
            }
        }
        if (m_exceptionTypes != null) {
            for (int i = 0; i < m_exceptionTypes.length; i++) {
                result = 37 * result + hashCodeOrZeroIfNull(m_exceptionTypes[i]);
            }
        }
        result = 37 * result + m_modifiers;
        return result;
    }

    protected static boolean areEqualsOrBothNull(final Object o1, final Object o2) {
        if (null == o1) {
            return (null == o2);
        }
        return o1.equals(o2);
    }

    protected static boolean areStringArraysEqual(final String[] o1, final String[] o2) {
        if (null == o1) {
            return (null == o2);
        }
        if (o1.length != o2.length) {
            return false;
        }
        for (int i = 0; i < o1.length; i++) {
            if (!o1[i].equals(o2[i])) {
                return false;
            }
        }
        return true;
    }

    protected static int hashCodeOrZeroIfNull(final Object o) {
        if (null == o) {
            return 19;
        }
        return o.hashCode();
    }
}
