/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.handler;

import org.codehaus.aspectwerkz.Pointcut;
import org.codehaus.aspectwerkz.aspect.Aspect;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @Aspect perJVM
 */
public class HandlerTestAspect extends Aspect {

    // ============ Pointcuts ============

    /**
     * @Handler test.handler.HandlerTestAroundException
     */
    Pointcut handler1;

    /**
     * @Handler test.handler.HandlerTestBeforeException
     */
    Pointcut handler2;

    /**
     * @Handler test.handler.HandlerTestAfterException
     */
    Pointcut handler3;

    // ============ Advices ============

    /**
     * @Around handler1
     */
    public Object aroundCall(final JoinPoint joinPoint) throws Throwable {
        HandlerTest.log("before ");
        final Object result = joinPoint.proceed();
        HandlerTest.log("after ");
        return result;
    }

    /**
     * @Before handler2
     */
    public void beforeCall(final JoinPoint joinPoint) throws Throwable {
        HandlerTest.log("pre ");
    }

    /**
     * @After handler3
     */
    public void afterCall(final JoinPoint joinPoint) throws Throwable {
        HandlerTest.log("post ");
    }
}
