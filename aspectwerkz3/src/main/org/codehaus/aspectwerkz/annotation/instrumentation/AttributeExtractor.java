/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.annotation.instrumentation;

/**
 * Methods that should be implemented in order to extract attributes associate with a class.
 * <p/>
 * An implementation this class needs to be provided for each bytecode manipulation library or other meta-data storage
 * mechanism that is supported.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public interface AttributeExtractor {
    public static final Object[] EMPTY_OBJECT_ARRAY = new Object[]{};

    /**
     * Retreives attributes associated with the class.
     *
     * @return An array of attributes that satisfy the instanceof comparison with the filter class. Null if there are no
     *         attributes associated with the class.
     */
    Object[] getClassAttributes();

    /**
     * Retreives custom attributes applied to a specific method of the class.
     *
     * @param methodName       The name of the method.
     * @param methodParamTypes The signature of the method.
     * @return An array of custom attributes.  Null if there are no attributes.
     */
    Object[] getMethodAttributes(String methodName, String[] methodParamTypes);

    /**
     * Return all the attributes associated with a constructor that have a particular method signature.
     * 
     * @param constructorParamTypes An array of parameter types as given by the reflection api.
     * @return the constructor attributes.
     */
    Object[] getConstructorAttributes(String[] constructorParamTypes);
        
    /**
     * Retreives custom attributes applied to a specific field of the class.
     *
     * @param fieldName the name of a class field.
     * @return An array of custom attributes.  Null if there are no attributes.
     */
    Object[] getFieldAttributes(String fieldName);
}
