/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.constructor;

import org.codehaus.aspectwerkz.Pointcut;
import org.codehaus.aspectwerkz.aspect.Aspect;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import test.Loggable;

/**
 * @Aspect perJVM
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class ConstructorTestAspect extends Aspect {

    // ============ Pointcuts ============

    /** @Call test.constructor.TestAroundAdvice.new() */
    Pointcut pc1;

    // ============ Advices ============

    /**
     * @Around pc1
     */
    public Object around1(final JoinPoint joinPoint) throws Throwable {
       ((Loggable)joinPoint.getTargetInstance()).log("before ");
        final Object result = joinPoint.proceed();
        ((Loggable)joinPoint.getTargetInstance()).log("after ");
        return result;
    }
}
