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
package org.codehaus.aspectwerkz.definition.metadata;

import java.io.Serializable;

/**
 * Holds meta-data for a method. Used by the transformers.
 *
 * @author <a href="mailto:jboner@acm.org">Jonas Bonér</a>
 * @version $Id: MethodMetaData.java,v 1.1.1.1 2003-05-11 15:14:00 jboner Exp $
 */
public class MethodMetaData implements Serializable {

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
}
