/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.xmldef.advice;

import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.ContainerType;

/**
 * Interface for the advice container implementations.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public interface AdviceContainer {
    /**
     * Returns the advice per JVM basis.
     *
     * @param joinPoint the joint point
     * @return the advice
     */
    Object getPerJvmAdvice(JoinPoint joinPoint);

    /**
     * Returns the advice per class basis.
     *
     * @param joinPoint the joint point
     * @return the advice
     */
    Object getPerClassAdvice(JoinPoint joinPoint);

    /**
     * Returns the advice per instance basis.
     *
     * @param joinPoint the joint point
     * @return the advice
     */
    Object getPerInstanceAdvice(JoinPoint joinPoint);

    /**
     * Returns the advice for the current thread.
     *
     * @return the advice
     */
    Object getPerThreadAdvice();

    /**
     * Returns the memory type.
     *
     * @return the memory type
     */
    ContainerType getContainerType();
}
