/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.xmldef.introduction;

import java.lang.reflect.Method;

import org.codehaus.aspectwerkz.ContainerType;

/**
 * Interface for the introduction container implementations.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public interface IntroductionContainer {
    /**
     * Invokes the method on a per JVM basis.
     *
     * @param methodIndex the method index
     * @param parameters the parameters for the invocation
     * @return the result from the method invocation
     */
    Object invokePerJvm(int methodIndex, Object[] parameters);

    /**
     * Invokes the method on a per class basis.
     *
     * @param callingObject a reference to the calling object
     * @param methodIndex the method index
     * @param parameters the parameters for the invocation
     * @return the result from the method invocation
     */
    Object invokePerClass(Object callingObject, int methodIndex, Object[] parameters);

    /**
     * Invokes the method on a per instance basis.
     *
     * @param callingObject a reference to the calling object
     * @param methodIndex the method index
     * @param parameters the parameters for the invocation
     * @return the result from the method invocation
     */
    Object invokePerInstance(Object callingObject, int methodIndex, Object[] parameters);

    /**
     * Invokes the method on a per thread basis.
     *
     * @param methodIndex the method index
     * @param parameters the parameters for the invocation
     * @return the result from the method invocation
     */
    Object invokePerThread(int methodIndex, Object[] parameters);

    /**
     * Returns a specific method by the method index.
     *
     * @param index the method index
     * @return the method
     */
    Method getMethod(int index);

    /**
     * Returns all the methods for this introduction.
     *
     * @return the methods
     */
    Method[] getMethods();

    /**
     * Swaps the current introduction implementation.
     *
     * @param implClass the class of the new implementation to use
     */
    void swapImplementation(Class implClass);

    /**
     * Returns the container type.
     *
     * @return the container type
     */
    ContainerType getContainerType();
}
