/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package examples.cflow;

import org.codehaus.aspectwerkz.aspect.AbstractAspect;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.Pointcut;

/**
 * @Aspect
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class CFlowAspect extends AbstractAspect {

    /**
     * @CFlow void examples.cflow.Target.step1()
     */
    Pointcut cflowPointcut;

    /**
     * @Execution void examples.cflow.Target.step2()
     */
    Pointcut methodsToLog;

    /**
     * @Around cflowPointcut && methodsToLog
     */
    public Object logMethod(final JoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();
        System.out.println("  --> invoking advice triggered by step2");
        return result;
    }
}
