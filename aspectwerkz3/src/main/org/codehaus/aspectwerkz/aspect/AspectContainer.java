/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.aspect;

import org.codehaus.aspectwerkz.AspectContext;

/**
 * Interface for that all aspect container implementations must implement.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r </a>
 */
public interface AspectContainer {

    /**
     * Creates a new perJVM cross-cutting instance, if it already exists then return it.
     *
     * @return the cross-cutting instance
     */
    Object aspectOf();

    /**
     * Creates a new perClass cross-cutting instance, if it already exists then return it.
     *
     * @param callingClass
     * @return the cross-cutting instance
     */
    Object aspectOf(Class callingClass);

    /**
     * Creates a new perInstance cross-cutting instance, if it already exists then return it.
     *
     * @param callingInstance
     * @return the cross-cutting instance
     */
    Object aspectOf(Object callingInstance);

    /**
     * Creates a new perThread cross-cutting instance, if it already exists then return it.
     *
     * @param thread the thread for the aspect
     * @return the cross-cutting instance
     */
    Object aspectOf(Thread thread);

    /**
     * Returns the context.
     *
     * @return the context
     */
    AspectContext getContext();

//    /**
//     * Returns the pointcut manager for the aspect.
//     *
//     * @return the pointcut manager
//     */
//    PointcutManager getPointcutManager();

//    /**
//     * Returns the advice info for the advice with the name specified.
//     *
//     * @param name the name of the advice
//     * @return the advice info
//     */
//    AdviceInfo getAdviceInfo(String name);

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