/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.aspect;

import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.CrossCutting;

import java.lang.reflect.Method;

/**
 * Interface for that all aspect container implementations must implement.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public interface AspectContainer {

    /**
     * Invokes the advice method on a per JVM basis.
     *
     * @param methodIndex the method index
     * @param joinPoint   the join point
     * @return the result from the method invocation
     */
    Object invokeAdvicePerJvm(int methodIndex, JoinPoint joinPoint);

    /**
     * Invokes the advice method on a per class basis.
     *
     * @param methodIndex the method index
     * @param joinPoint   the join point
     * @return the result from the method invocation
     */
    Object invokeAdvicePerClass(int methodIndex, JoinPoint joinPoint);

    /**
     * Invokes the advice method on a per instance basis.
     *
     * @param methodIndex the method index
     * @param joinPoint   the join point
     * @return the result from the method invocation
     */
    Object invokeAdvicePerInstance(int methodIndex, JoinPoint joinPoint);

    /**
     * Invokes the advice method on a per thread basis.
     *
     * @param methodIndex the method index
     * @param joinPoint   the join point
     * @return the result from the method invocation
     */
    Object invokeAdvicePerThread(int methodIndex, JoinPoint joinPoint);

    /**
     * Returns a specific advice by index.
     *
     * @param index the index
     * @return the advice
     */
    Method getAdvice(int index);

    /**
     * Returns all advice.
     *
     * @return the method
     */
    Method[] getAdvice();

    /**
     * Creates a new perJVM aspect, if it does not already exist, then return it.
     *
     * @return the aspect
     */
    CrossCutting createPerJvmAspect();

    /**
     * Creates a new perClass aspect, if it does not already exist, then return it.
     *
     * @return the aspect
     */
    CrossCutting createPerClassAspect(Class callingClass);

    /**
     * Creates a new perInstance aspect, if it does not already exist, then return it.
     *
     * @return the aspect
     */
    CrossCutting createPerInstanceAspect(Object callingInstance);

    /**
     * Creates a new perThread aspect, if it does not already exist, then return it.
     *
     * @param thread the thread for the aspect
     * @return the aspect
     */
    CrossCutting createPerThreadAspect(Thread thread);

    /**
     * Returns the sole per JVM aspect.
     *
     * @return the aspect
     */
    CrossCutting getPerJvmAspect();

    /**
     * Returns the aspect for the current class
     *
     * @return the aspect
     */
    CrossCutting getPerClassAspect(Class callingClass);

    /**
     * Returns the aspect for the current instance.
     *
     * @return the aspect
     */
    CrossCutting getPerInstanceAspect(Object callingInstance);

    /**
     * Returns the aspect for the current thread.
     *
     * @param thread the thread for the aspect
     * @return the aspect
     */
    CrossCutting getPerThreadAspect(Thread thread);

    /**
     * Attach the introduction container to this aspect container to mirror the "aspect contains 0-n introduction"
     *
     * @param name           of the introduction
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
