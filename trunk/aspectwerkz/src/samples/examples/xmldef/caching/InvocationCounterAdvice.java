/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package examples.xmldef.caching;

import org.codehaus.aspectwerkz.xmldef.advice.PreAdvice;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.CallerSideJoinPoint;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class InvocationCounterAdvice extends PreAdvice {

    public InvocationCounterAdvice() {
        super();
    }

    public void execute(final JoinPoint joinPoint) throws Throwable {
        CallerSideJoinPoint jp = (CallerSideJoinPoint)joinPoint;
        CacheStatistics.addMethodInvocation(
                jp.getCalleeMethodName(),
                jp.getCalleeMethodParameterTypes());
        joinPoint.proceed();
    }
}
