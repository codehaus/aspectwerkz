/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.xmldef;

import org.codehaus.aspectwerkz.xmldef.advice.AroundAdvice;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.MethodJoinPoint;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class DynamicallyCreatedTransientAdvice extends AroundAdvice {
    public DynamicallyCreatedTransientAdvice() {
        super();
    }
    public Object execute(final JoinPoint joinPoint) throws Throwable {
        MethodJoinPoint jp = (MethodJoinPoint)joinPoint;
        ((Loggable)jp.getTargetInstance()).log("before ");
        final Object result = joinPoint.proceed();
        ((Loggable)jp.getTargetInstance()).log("after ");
        return result;
    }
}
