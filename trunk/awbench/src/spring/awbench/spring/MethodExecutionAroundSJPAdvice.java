/**************************************************************************************
 * Copyright (c) Jonas Bon?r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package awbench.spring;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import awbench.Run;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class MethodExecutionAroundSJPAdvice implements MethodInterceptor {
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Object m = invocation.getMethod();
        Run.ADVICE_HIT++;
        return invocation.proceed();
    }
}