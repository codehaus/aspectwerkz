/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.aspect;

import org.codehaus.aspectwerkz.Pointcut;
import org.codehaus.aspectwerkz.aspect.Aspect;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import test.Loggable;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @Aspect perJVM
 */
public class DynamicallyCreatedAspect extends Aspect {

    // ============ Pointcuts ============

    /**
     * @Execution * test.DynamicDeploymentTest.createAspectTestMethod(..)
     */
    Pointcut pc1;

    // ============ Advices ============

    /**
     * @Around pc1
     */
    public Object advice1(final JoinPoint joinPoint) throws Throwable {
        ((Loggable) joinPoint.getTargetInstance()).log("beforeNew ");
        final Object result = joinPoint.proceed();
        ((Loggable) joinPoint.getTargetInstance()).log("afterNew ");
        return result;
    }
}
