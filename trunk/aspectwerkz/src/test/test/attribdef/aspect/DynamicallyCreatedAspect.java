/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.attribdef.aspect;

import org.codehaus.aspectwerkz.attribdef.Pointcut;
import org.codehaus.aspectwerkz.attribdef.aspect.Aspect;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.MethodJoinPoint;
import test.attribdef.Loggable;

/**
 * @Aspect perJVM
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class DynamicallyCreatedAspect extends Aspect {

    // ============ Pointcuts ============

    /** @Execution * test.attribdef.DynamicDeploymentTest.createAspectTestMethod(..) */
    Pointcut pc1;

    // ============ Advices ============

    /**
     * @Around pc1 name=newAdvice
     */
    public Object advice1(final JoinPoint joinPoint) throws Throwable {
        MethodJoinPoint jp = (MethodJoinPoint)joinPoint;
        ((Loggable)jp.getTargetInstance()).log("beforeNew ");
        final Object result = joinPoint.proceed();
        ((Loggable)jp.getTargetInstance()).log("afterNew ");
        return result;
    }
}
