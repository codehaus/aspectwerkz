/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
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
     * Handler
     */
    Pointcut handler;

    // ============ Advices ============

    /**
     * Before handler
     */
    public void before(final JoinPoint joinPoint) throws Throwable {
        HandlerTest.log("pre ");
    }
}
