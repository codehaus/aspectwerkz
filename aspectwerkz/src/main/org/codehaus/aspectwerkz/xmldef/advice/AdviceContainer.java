/*
 * AspectWerkz - a dynamic, lightweight and high-performant AOP/AOSD framework for Java.
 * Copyright (C) 2002-2003  Jonas Bonér. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.codehaus.aspectwerkz.advice;

import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.ContainerType;

/**
 * Interface for the advice container implementations.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: AdviceContainer.java,v 1.1 2003-06-17 15:27:33 jboner Exp $
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
