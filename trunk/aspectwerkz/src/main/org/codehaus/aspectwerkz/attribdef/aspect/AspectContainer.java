/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.attribdef.aspect;

import java.lang.reflect.Method;

import org.codehaus.aspectwerkz.ContainerType;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

/**
 * Interface for the aspect container implementations.
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
     * Returns a specific method by the method index.
     *
     * @param index the method index
     * @return the method
     */
    Method getMethod(int index);

    /**
     * Returns the container type.
     *
     * @return the container type
     */
    ContainerType getContainerType();

    /**
     * @return the sole instance of a PER_JVM aspect
     */
    public Aspect getPerJvmAspect();

    /**
     * @param callingClass
     * @return the class attached instance of a PER_CLASS aspect
     */
    public Aspect getPerClassAspect(final Class callingClass);

    /**
     * @param callingInstance
     * @return the instance attached instance of a PER_INSTANCE aspect
     */
    public Aspect getPerInstanceAspect(final Object callingInstance);

    /**
     * @return the thread attached instance of a PER_CLASS aspect
     */
    public Aspect getPerThreadAspect();

    /**
     * Attach the introduction container to this aspect container
     * to mirror the "aspect contains 0-n introduction"
     * @param name of the introduction
     * @param introContainer introduction container
     */
    public void addIntroductionContainer(String name, IntroductionContainer introContainer);

    /**
     * Returns the introduction container of given name (introduction name)
     * or null if not linked.
     * @param name of the introduction
     * @return introduction container
     */
    public IntroductionContainer getIntroductionContainer(String name);

}


