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

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @Aspect perJVM
 */
public class ConstructorTestAspect extends Aspect {

    // ============ Pointcuts ============

    /**
     * @Call test.constructor.TestAroundAdvice.new(..)
     */
    Pointcut pc1;

    /**
     * @Call test.constructor.TestBeforeAdvice.new()
     */
    Pointcut pc2;

    /**
     * @Call test.constructor.TestAfterAdvice.new(String)
     */
    Pointcut pc3;

    /**
     * @Call test.constructor.TestBeforeAfterAdvice.new(String[])
     */
    Pointcut pc4;

    /**
     * @Call test.constructor.TestReturnFalseType.new()
     */
    Pointcut pc5;

    // ============ Advices ============

    /**
     * @Around pc1
     */
    public Object around1(final JoinPoint joinPoint) throws Throwable {
        ConstructorAdviceTest.log("before ");
        final Object result = joinPoint.proceed();
        ConstructorAdviceTest.log("after ");
        return result;
    }

    /**
     * @Before pc2 || pc4
     */
    public void before1(final JoinPoint joinPoint) throws Throwable {
        ConstructorAdviceTest.log("pre ");
    }

    /**
     * @After pc3 ||pc4
     */
    public void after1(final JoinPoint joinPoint) throws Throwable {
        ConstructorAdviceTest.log("post ");
    }

    /**
     * @Around pc5
     */
    public Object around2(final JoinPoint joinPoint) throws Throwable {
        return new Integer(0);
    }
}
