/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.joinpoint.management;

import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public interface AdviceExecutor {
    /**
     * Default implementation of a joinpoint controller that is being used if no other controller was
     * specified for the join point.<P>
     * <p/>
     * Steps linearly through each pointcut of the joinpoint. In each pointcut it executes its advices
     * one by one. After the last advice in the last pointcut was executed, the original method is being
     * invoked.
     *
     * @param joinPoint The joinpoint using this controller
     * @return The result of the invocation.
     */
    Object proceed(JoinPoint joinPoint) throws Throwable;

    /**
     * Checks if the executor has any advices.
     *
     * @return true if it has advices
     */
    boolean hasAdvices();

    /**
     * Clones the executor.
     */
    AdviceExecutor newInstance();
}
