/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.hierarchicalpattern;

import org.codehaus.aspectwerkz.Pointcut;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

import test.Loggable;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @Aspect
 */
public class TestAspect
{
    /**
     * @Expression execution(* test.hierarchicalpattern.DummyInterface1+.testMethod1(..))
     */
    Pointcut pc1;

    /**
     * @Expression execution(* test.hierarchicalpattern.DummyInterface2+.testMethod2(..))
     */
    Pointcut pc2;

    /**
     * @Around pc1 || pc2
     */
    public Object advice(final JoinPoint joinPoint)
        throws Throwable
    {
        ((Loggable) joinPoint.getTargetInstance()).log("before1 ");

        final Object result = joinPoint.proceed();

        ((Loggable) joinPoint.getTargetInstance()).log("after1 ");

        return result;
    }
}
