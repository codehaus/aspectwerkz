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
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public class MethodExecutionAfterThrowingAdvice implements MethodInterceptor {
    public Object intercept(Object target, Method m, Object[] args, MethodProxy proxy) throws Throwable {
        RuntimeException rte = null;
        Object result = null;
        try {
            result = proxy.invokeSuper(target, args);
        } catch (RuntimeException throwable) {
            Run.ADVICE_HIT++;
            rte = throwable;
            throw rte;
        }
        return result;
    }
}