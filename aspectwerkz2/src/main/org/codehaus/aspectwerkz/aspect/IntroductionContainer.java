/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.aspect;

/**
 * Interface for the introduction container implementations.
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public interface IntroductionContainer {

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
     * Swaps the current introduction implementation.
     *
     * @param implClass the class of the new implementation to use
     */
    void swapImplementation(Class implClass);

    /**
     * Returns the target instance from an introduction
     * @param mixinImpl aka "this" from the mixin impl
     * @return the target instance or null (if not perInstance deployed mixin)
     */
    Object getTargetInstance(Object mixinImpl);

    /**
     * Returns the target class from an introduction
     * @param mixinImpl aka "this" from the mixin impl
     * @return the target class or null (if not perInstance or perClas deployed mixin)
     */
    Class getTargetClass(Object mixinImpl);

}
