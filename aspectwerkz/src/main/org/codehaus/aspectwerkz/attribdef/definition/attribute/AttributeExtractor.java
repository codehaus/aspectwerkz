/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.attribdef.definition.attribute;

/**
 * Methods that should be implemented in order to extract attributes
 * associate with a class.  An implementation this class needs to be
 * provided for each bytecode manipulation library or other meta-data
 * storage mechanism that is supported.
 *
 * Based on code from the Attrib4j project by Mark Pollack and Ted Neward (http://attrib4j.sourceforge.net/).
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public interface AttributeExtractor {

    /**
     * Using the provided class loader, load the class so that it can
     * be passed to a bytecode manipulation library.
     *
     * @param className The fully qualified classname
     * @param loader Classloader to user to load the class.
     */
    void initialize(String className, ClassLoader loader);

    /**
     * Retreives attributes associated with the class.
     *
     * @return An array of attributes that satisfy the instanceof comparison with the filter class.
     * Null if there are no attributes associated with the class.
     */
    Object[] getClassAttributes();

    /**
     * Retreives custom attributes applied to a specific method of the class.
     *
     * @param methodName The name of the method.
     * @param methodParamTypes The signature of the method.
     * @return An array of custom attributes.  Null if there are no attributes.
     */
    Object[] getMethodAttributes(String methodName, String[] methodParamTypes);

    /**
     * Retreives custom attributes applied to a specific field of the class.
     *
     * @param fieldName the name of a class field.
     * @return An array of custom attributes.  Null if there are no attributes.
     */
    Object[] getFieldAttributes(String fieldName);
}
