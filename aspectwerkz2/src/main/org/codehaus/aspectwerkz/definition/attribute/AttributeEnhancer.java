/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition.attribute;

import com.thoughtworks.qdox.model.JavaField;
import com.thoughtworks.qdox.model.JavaMethod;

/**
 * Enhances a classes with attributes.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public interface AttributeEnhancer
{
    final String ATTRIBUTE_TYPE = "Custom";

    /**
     * Initializes the attribute enhancer. <p/>Must always be called before use.
     *
     * @param className the class name
     * @param classPath the class path
     * @return true if the class was succefully loaded, false otherwise
     */
    boolean initialize(String className, String classPath);

    /**
     * Inserts an attribute on class level.
     *
     * @param attribute the attribute
     */
    void insertClassAttribute(Object attribute);

    /**
     * Inserts an attribute on field level.
     *
     * @param field     the QDox java field
     * @param attribute the attribute
     */
    void insertFieldAttribute(JavaField field, Object attribute);

    /**
     * Inserts an attribute on method level.
     *
     * @param method    the QDox java method
     * @param attribute the attribute
     */
    void insertMethodAttribute(JavaMethod method, Object attribute);

    /**
     * Writes the enhanced class to file.
     *
     * @param destDir the destination directory
     */
    void write(String destDir);

    /**
     * Return the first interfaces implemented by a level in the class hierarchy (bottom top)
     *
     * @return nearest superclass (including itself) ' implemented interfaces
     */
    String[] getNearestInterfacesInHierarchy(String innerClassName);
}
