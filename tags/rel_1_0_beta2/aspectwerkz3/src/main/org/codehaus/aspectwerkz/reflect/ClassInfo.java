/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.reflect;

/**
 * Interface for the class info implementations.
 * 
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public interface ClassInfo extends ReflectionInfo {
    /**
     * Returns a constructor info by its hash.
     * 
     * @param hash
     * @return
     */
    ConstructorInfo getConstructor(int hash);

    /**
     * Returns the constructors info.
     * 
     * @return the constructors info
     */
    ConstructorInfo[] getConstructors();

    /**
     * Returns a method info by its hash.
     * 
     * @param hash
     * @return
     */
    MethodInfo getMethod(int hash);

    /**
     * Returns the methods info.
     * 
     * @return the methods info
     */
    MethodInfo[] getMethods();

    /**
     * Returns a field info by its hash.
     * 
     * @param hash
     * @return
     */
    FieldInfo getField(int hash);

    /**
     * Returns the fields info.
     * 
     * @return the fields info
     */
    FieldInfo[] getFields();

    /**
     * Returns the interfaces.
     * 
     * @return the interfaces
     */
    ClassInfo[] getInterfaces();

    /**
     * Returns the super class.
     * 
     * @return the super class
     */
    ClassInfo getSuperClass();

    /**
     * Returns the component type if array type else null.
     * 
     * @return the component type
     */
    ClassInfo getComponentType();

    /**
     * Is the class an interface.
     * 
     * @return
     */
    boolean isInterface();

    /**
     * Is the class a primitive type.
     * 
     * @return
     */
    boolean isPrimitive();

    /**
     * Is the class an array type.
     * 
     * @return
     */
    boolean isArray();
}