/**************************************************************************************
 * Copyright (c) Jonas Bon?r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package awbench.cglib;

import java.lang.reflect.Method;

import awbench.Run;
import awbench.method.Execution;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public class MethodExecutionGetTargetAndArgsAroundAdvice2 implements MethodInterceptor {
    public Object intercept(Object target, Method m, Object[] args, MethodProxy proxy) throws Throwable {
        Run.ADVICE_HIT++;
        int i = ((Integer) args[0]).intValue();
        Execution execution = (Execution) target;
        return proxy.invokeSuper(target, args);
    }
}