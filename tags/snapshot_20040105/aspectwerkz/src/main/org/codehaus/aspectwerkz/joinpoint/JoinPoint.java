/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
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
 */
public interface JoinPoint extends Serializable {

    /**
     * Invokes the next advice in the chain A when it reaches the end
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
    Object getTargetInstance();

    /**
     * Returns the target class.
     *
     * @return the target class
     */
    Class getTargetClass();
}
