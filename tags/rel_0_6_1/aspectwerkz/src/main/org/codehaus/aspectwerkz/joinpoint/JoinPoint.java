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
package org.codehaus.aspectwerkz.joinpoint;

import java.io.Serializable;

/**
 * Interface for the join point concept.<br/>
 * I.e.a well defined point of execution in the program picked out by the
 * <code>Pointcut</code>.<br/>
 * Handles the invocation of the advices added to the join point.<br/>
 * Stores meta data from the join point.<br/>
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: JoinPoint.java,v 1.2 2003-06-09 07:04:13 jboner Exp $
 */
public interface JoinPoint extends Serializable {

    /**
     * Invokes the next advice in the chain and when it reaches the end
     * of the chain it invokes the original method.
     *
     * @return the result from the next invocation
     * @throws Throwable
     */
    Object proceed() throws Throwable;

    /**
     * To be called instead of proceed() when a new thread is spawned.
     * or the the result will be unpredicable.
     *
     * @return the result from the next invocation
     * @throws Throwable
     */
    Object proceedInNewThread() throws Throwable;

    /**
     * Returns the target object.
     *
     * @return the target object
     */
    Object getTargetObject();

    /**
     * Returns the target class.
     *
     * @return the target class
     */
    Class getTargetClass();
}
