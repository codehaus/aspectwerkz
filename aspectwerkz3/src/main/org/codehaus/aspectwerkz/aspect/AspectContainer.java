/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.aspect;

import org.codehaus.aspectwerkz.CrossCuttingInfo;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

import java.lang.reflect.Method;

/**
 * Interface for that all aspect container implementations must implement.
 * 
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r </a>
 */
public interface AspectContainer {
    /**
     * Invokes the advice method on a per JVM basis.
     * 
     * @param methodIndex the method index
     * @param joinPoint the join point
     * @return the result from the method invocation
     */
    Object invokeAdvice(int methodIndex, JoinPoint joinPoint) throws Throwable;

    /**
     * Returns a specific advice by index.
     * 
     * @param index the index
     * @return the advice
     */
    Method getAdvice(int index);

    /**
     * Creates a new perJVM cross-cutting instance, if it already exists then return it.
     * 
     * @return the cross-cutting instance
     */
    Object createPerJvmAspect();

    /**
     * Creates a new perClass cross-cutting instance, if it already exists then return it.
     * 
     * @param callingClass
     * @return the cross-cutting instance
     */
    Object createPerClassAspect(Class callingClass);

    /**
     * Creates a new perInstance cross-cutting instance, if it already exists then return it.
     * 
     * @param callingInstance
     * @return the cross-cutting instance
     */
    Object createPerInstanceAspect(Object callingInstance);

    /**
     * Creates a new perThread cross-cutting instance, if it already exists then return it.
     * 
     * @param thread the thread for the aspect
     * @return the cross-cutting instance
     */
    Object createPerThreadAspect(Thread thread);

    /**
     * Returns the cross-cutting info.
     * 
     * @return the cross-cutting info
     */
    CrossCuttingInfo getCrossCuttingInfo();

    /**
     * Attach the introduction container to this aspect container to mirror the "aspect contains 0-n
     * introduction"
     * 
     * @param name of the introduction
     * @param introContainer introduction container
     */
    void addIntroductionContainer(String name, IntroductionContainer introContainer);

    /**
     * Returns the introduction container of given name (introduction name) or null if not linked.
     * 
     * @param name of the introduction
     * @return introduction container
     */
    IntroductionContainer getIntroductionContainer(String name);
}