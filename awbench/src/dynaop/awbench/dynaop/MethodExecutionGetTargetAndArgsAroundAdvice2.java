/**************************************************************************************
 * Copyright (c) Jonas Bon?r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package awbench.dynaop;

import dynaop.Interceptor;
import dynaop.Invocation;
import awbench.Run;
import awbench.method.Execution;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class MethodExecutionGetTargetAndArgsAroundAdvice2 implements Interceptor {
    public Object intercept(Invocation invocation) throws Throwable {
        Run.ADVICE_HIT++;
        Object[] args = invocation.getArguments();
        Object target = invocation.getProxy().getProxyContext().unwrap();
        int i = ((Integer) args[0]).intValue();
        Execution execution = (Execution) target;
        return invocation.proceed();
    }
}