/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.aspect;

import java.lang.reflect.Method;

import org.codehaus.aspectwerkz.ContainerType;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

/**
 * Interface for the introduction container implementations.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public interface AspectContainer {

    /**
     * Invokes the advice method on a per JVM basis.
     *
     * @param methodIndex the method index
     * @param joinPoint the join point
     * @return the result from the method invocation
     */
    Object invokeAdvicePerJvm(int methodIndex, JoinPoint joinPoint);

    /**
     * Invokes the advice method on a per class basis.
     *
     * @param methodIndex the method index
     * @param joinPoint the join point
     * @return the result from the method invocation
     */
    Object invokeAdvicePerClass(int methodIndex, JoinPoint joinPoint);

    /**
     * Invokes the advice method on a per instance basis.
     *
     * @param methodIndex the method index
     * @param joinPoint the join point
     * @return the result from the method invocation
     */
    Object invokeAdvicePerInstance(int methodIndex, JoinPoint joinPoint);

    /**
     * Invokes the advice method on a per thread basis.
     *
     * @param methodIndex the method index
     * @param joinPoint the join point
     * @return the result from the method invocation
     */
    Object invokeAdvicePerThread(final int methodIndex, final JoinPoint joinPoint);

    /**
     * Invokes the method on a per JVM basis.
     *
     * @param methodIndex the method index
     * @param parameters the parameters for the invocation
     * @return the result from the method invocation
     */
    Object invokeIntroductionPerJvm(int methodIndex, Object[] parameters);

    /**
     * Invokes the method on a per class basis.
     *
     * @param callingObject a reference to the calling object
     * @param methodIndex the method index
     * @param parameters the parameters for the invocation
     * @return the result from the method invocation
     */
    Object invokeIntroductionPerClass(Object callingObject, int methodIndex, Object[] parameters);

    /**
     * Invokes the method on a per instance basis.
     *
     * @param callingObject a reference to the calling object
     * @param methodIndex the method index
     * @param parameters the parameters for the invocation
     * @return the result from the method invocation
     */
    Object invokeIntroductionPerInstance(Object callingObject, int methodIndex, Object[] parameters);

    /**
     * Invokes the method on a per thread basis.
     *
     * @param methodIndex the method index
     * @param parameters the parameters for the invocation
     * @return the result from the method invocation
     */
    Object invokeIntroductionPerThread(int methodIndex, Object[] parameters);

    /**
     * Returns a specific method by the method index.
     *
     * @param index the method index
     * @return the method
     */
    Method getMethod(int index);

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
