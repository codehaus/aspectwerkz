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
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class MethodExecutionBeforeAfterAdvice implements MethodInterceptor {

    private MethodExecutionAfterAdvice m_afterAdvice;

    public MethodExecutionBeforeAfterAdvice(MethodExecutionAfterAdvice afterAdvice) {
        m_afterAdvice = afterAdvice;
    }

    public Object intercept(Object target, Method m, Object[] args, MethodProxy proxy) throws Throwable {
        Run.ADVICE_HIT++;
        Object result = proxy.invokeSuper(target, args);
        m_afterAdvice.intercept(target, m, args, proxy);
        return result;
    }
}