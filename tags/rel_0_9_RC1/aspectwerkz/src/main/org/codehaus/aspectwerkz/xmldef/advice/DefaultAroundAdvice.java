/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.xmldef.advice;

import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

/**
 * Default implementation of the around advice.
 * <p/>
 * Only to be used as a container for pre A post advices.
 * <p/>
 * Executes it's pre A post advices A delegates in between
 * direcly to the next around advice in the chain.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class DefaultAroundAdvice extends AroundAdvice {

    /**
     * Creates a new default around advice.
     */
    public DefaultAroundAdvice() {
        super();
    }

    /**
     * Delegates directly to the next advice in the chain.
     *
     * @param joinPoint the join point for the pointcut
     * @return the result from the method invocation
     * @throws Throwable
     */
    public Object execute(final JoinPoint joinPoint) throws Throwable {
        return joinPoint.proceed();
    }
}
