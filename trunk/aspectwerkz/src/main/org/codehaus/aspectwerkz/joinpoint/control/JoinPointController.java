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
package org.codehaus.aspectwerkz.joinpoint.control;

import java.io.Serializable;

import org.codehaus.aspectwerkz.joinpoint.MethodJoinPoint;

/**
 * Interface to be implemented by each join point controller.
 *
 * @author <a href="mailto:stefan.finkenzeller@gmx.net">Stefan Finkenzeller</a>
 */
public interface JoinPointController extends Serializable {

    /**
     * Proceeds in the execution model for the join point to the next logical pointcut/advice
     * <p/>
     *
     * Joinpoint controller implementations need to implement the business logic for handling e.g.
     * advice redundancy, advice dependency, advice compatibility or special exception handling
     * here.
     *
     * @param jp    The joinpoint using this controller
     * @return      The result of the invocation.
     */
    public Object proceed(MethodJoinPoint jp) throws Throwable;

    /**
     * Clones the controller
     *
     * @return      Clone of this controller.
     */
    public JoinPointController deepCopy();
}
