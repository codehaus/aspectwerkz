/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.metadata;

import java.util.List;
import java.util.ArrayList;
import java.io.Serializable;

/**
 * Holds meta-data for a class.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class ClassMetaData implements MetaData, Serializable {

    /**
     * The name of the class.
     */
    private String m_name;

    /**
     * A list with the <code>MethodMetaData</code> instances.
     */
    private List m_methods = new ArrayList();

    /**
     * A list with the <code>FieldMetaData</code> instances.
     */
    private List m_fields = new ArrayList();

    /**
     * A list with the interfaces.
     */
    private List m_interfaces = new ArrayList();

    /**
     * The super class.
     */
    private ClassMetaData m_superClass;

    /**
     * Returns the name of the class.
     *
     * @return the name of the class
     */
    public String getName() {
        return m_name;
    }

    /**
     * Sets the name of the class.
     *
     * @param name the name of the class
     */
    public void setName(final String name) {
        m_name = name;
    }

    /**
     * Returns a list with all the methods meta-data.
     *
     * @return the methods meta-data
     */
    public List getMethods() {
        return m_methods;
    }

    /**
     * Sets a list with <code>MethodMetaData</code> instances.
     *
     * @param methods a list with <code>MethodMetaData</code> instances
     */
    public void setMethods(final List methods) {
        m_methods = methods;
    }

    /**
     * Returns a list with all the field meta-data.
     *
     * @return the field meta-data
     */
    public List getFields() {
        return m_fields;
    }

    /**
     * Sets a list with <code>FieldMetaData</code> instances.
     *
     * @param methods a list with <code>FieldMetaData</code> instances
     */
    public void setFields(final List fields) {
        m_fields = fields;
    }

    /**
     * Returns the interfaces.
     *
     * @return the interfaces
     */
    public List getInterfaces() {
        return m_interfaces;
    }

    /**
     * Sets the interfaces.
     *
     * @param interfaces the interfaces
     */
    public void setInterfaces(final List interfaces) {
        m_interfaces = interfaces;
    }

    /**
     * Returns the super class.
     *
     * @return the super class
     */
    public ClassMetaData getSuperClass() {
        return m_superClass;
    }

    /**
     * Sets the super class.
     *
     * @param superClass the super class
     */
    public void setSuperClass(final ClassMetaData superClass) {
        m_superClass = superClass;
    }
}

