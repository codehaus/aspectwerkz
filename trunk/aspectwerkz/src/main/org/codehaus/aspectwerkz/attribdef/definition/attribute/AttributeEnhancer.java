/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.attribdef.definition.attribute;

import com.thoughtworks.qdox.model.JavaMethod;

/**
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public interface AttributeEnhancer {
    /**
     * Initializes the attribute enhancer.
     * Must always be called before use.
     *
     * @param className the class name
     * @param classPath the class path
     */
    void initialize(String className, String classPath);

    /**
     * Inserts an attribute on class level.
     *
     * @param attribute the attribute
     */
    void insertClassAttribute(Object attribute);

    /**
     * Inserts an attribute on field level.
     *
     * @param field the field name
     * @param attribute the attribute
     */
    void insertFieldAttribute(String field, Object attribute);

    /**
     * Inserts an attribute on method level.
     *
     * @param method the QDox java method
     * @param attribute the attribute
     */
    void insertMethodAttribute(JavaMethod method, Object attribute);

    /**
     * Writes the enhanced class to file.
     *
     * @param destDir the destination directory
     */
    void write(String destDir);
}
