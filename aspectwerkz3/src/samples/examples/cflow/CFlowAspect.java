/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package examples.cflow;

import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.Pointcut;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public class CFlowAspect {

    /**
     * @Expression cflow(call(void examples.cflow.Target.step1()))
     */
    Pointcut cflowPointcut;

    /**
     * @Expression execution(void examples.cflow.Target.step2())
     */
    Pointcut methodsToLog;

    /**
     * @Around methodsToLog AND cflowPointcut
     */
    public Object logMethod(final JoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();
        System.out.println("  --> invoking advice triggered by step2");
        return result;
    }
}
