/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package examples.cflow;

import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.aspect.Aspect;
import org.codehaus.aspectwerkz.Pointcut;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @Aspect
 */
public class CFlowAspect extends Aspect {

    /**
     * @CFlow void examples.cflow.Target.step1()
     */
    Pointcut cflowPointcut;

    /**
     * @Execution void examples.cflow.Target.step2()
     */
    Pointcut methodsToLog;

    /**
     * @Around methodsToLog IN cflowPointcut
     */
    public Object logMethod(final JoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();
        System.out.println("  --> invoking advice triggered by step2");
        return result;
    }
}
