/**************************************************************************************
 * Copyright (c) Jonas Bon?r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package awbench.extension.spring;

import java.lang.reflect.Method;

import org.springframework.aop.ThrowsAdvice;
import awbench.Run;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class MethodExecutionAfterThrowingAdvice implements ThrowsAdvice {
    public void afterThrowing(Method method, Object[] args, Object target, Throwable subclass) {
        if (subclass instanceof RuntimeException) {
            Run.ADVICE_HIT++;
        }
    }
}