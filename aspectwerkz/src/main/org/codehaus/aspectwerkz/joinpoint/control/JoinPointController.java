/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
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
