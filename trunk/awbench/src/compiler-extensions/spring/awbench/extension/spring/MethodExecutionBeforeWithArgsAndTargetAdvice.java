/**************************************************************************************
 * Copyright (c) Jonas Bon?r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package awbench.extension.spring;

import java.lang.reflect.Method;

import org.springframework.aop.MethodBeforeAdvice;
import awbench.Run;
import awbench.method.Execution;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class MethodExecutionBeforeWithArgsAndTargetAdvice implements MethodBeforeAdvice {
    public void before(Method m, Object[] args, Object target) throws Throwable {
        Execution execution = (Execution)target;
        int i = ((Integer) args[0]).intValue();
        Run.ADVICE_HIT++;
    }
}