/*
 * AspectWerkz - a dynamic, lightweight and high-performant AOP/AOSD framework for Java.
 * Copyright (C) 2002-2003  Jonas Bonér. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.codehaus.aspectwerkz.metadata;

import java.io.Serializable;

/**
 * Holds meta-data for a method. Used by the transformers.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: MethodMetaData.java,v 1.1 2003-06-17 14:58:31 jboner Exp $
 */
public class MethodMetaData implements MetaData, Serializable {

    /**
     * The name of the method.
     */
    private String m_name;

    /**
     * The return type.
     */
    private String m_returnType;

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
     * Returns the return type.
     *
     * @return the return type
     */
    public String getReturnType() {
        return m_returnType;
    }

    /**
     * Sets the return type.
     *
     * @param returnType the return type
     */
    public void setReturnType(final String returnType) {
        m_returnType = returnType;
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
        if (this == o) return true;
        if (!(o instanceof MethodMetaData)) return false;
        final MethodMetaData obj = (MethodMetaData)o;
        return areEqualsOrBothNull(obj.m_name, this.m_name) &&
                areEqualsOrBothNull(obj.m_returnType, this.m_returnType) &&
                areEqualsOrBothNull(obj.m_parameterTypes, this.m_parameterTypes) &&
                areEqualsOrBothNull(obj.m_exceptionTypes, this.m_exceptionTypes) &&
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
        result = 37 * result + hashCodeOrZeroIfNull(m_returnType);
        result = 37 * result + hashCodeOrZeroIfNull(m_parameterTypes);
        result = 37 * result + hashCodeOrZeroIfNull(m_exceptionTypes);
        result = 37 * result + m_modifiers;
        return result;
    }

    protected static boolean areEqualsOrBothNull(final Object o1, final Object o2) {
        if (null == o1) return (null == o2);
        return o1.equals(o2);
    }

    protected static int hashCodeOrZeroIfNull(final Object o) {
        if (null == o) return 19;
        return o.hashCode();
    }
}
