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
    Pointcut call1;

    /**
     * @Call test.constructor.TestBeforeAdvice.new()
     */
    Pointcut call2;

    /**
     * @Call test.constructor.TestAfterAdvice.new(String)
     */
    Pointcut call3;

    /**
     * @Call test.constructor.TestBeforeAfterAdvice.new(String[])
     */
    Pointcut call4;

    /**
     * @Call test.constructor.TestReturnFalseType.new()
     */
    Pointcut call5;

    /**
     * @Execution test.constructor.TestAroundAdvice.new(..)
     */
    Pointcut execution1;

    /**
     * @Execution test.constructor.TestBeforeAdvice.new()
     */
    Pointcut execution2;

    /**
     * @Execution test.constructor.TestAfterAdvice.new(String)
     */
    Pointcut execution3;

    /**
     * @Execution test.constructor.TestBeforeAfterAdvice.new(String[])
     */
    Pointcut execution4;

    /**
     * @Execution test.constructor.TestReturnFalseType.new()
     */
    Pointcut execution5;


    // ============ Advices ============

    /**
     * @Around call1
     */
    public Object aroundCall(final JoinPoint joinPoint) throws Throwable {
        ConstructorAdviceTest.logCall("beforeCall ");
        final Object result = joinPoint.proceed();
        ConstructorAdviceTest.logCall("afterCall ");
        return result;
    }

    /**
     * @Before call2 || call4
     */
    public void beforeCall(final JoinPoint joinPoint) throws Throwable {
        ConstructorAdviceTest.logCall("preCall ");
    }

    /**
     * @After call3 ||call4
     */
    public void afterCall(final JoinPoint joinPoint) throws Throwable {
        ConstructorAdviceTest.logCall("postCall ");
    }

    /**
     * @Around call5
     */
    public Object aroundCall2(final JoinPoint joinPoint) throws Throwable {
        return new Integer(0);
    }

    /**
     * @Around execution1
     */
    public Object aroundExecution(final JoinPoint joinPoint) throws Throwable {
        ConstructorAdviceTest.logExecution("beforeExecution ");
        final Object result = joinPoint.proceed();
        ConstructorAdviceTest.logExecution("afterExecution ");
        return result;
    }

    /**
     * @Before execution2 || execution4
     */
    public void beforeExecution(final JoinPoint joinPoint) throws Throwable {
        ConstructorAdviceTest.logExecution("preExecution ");
    }

    /**
     * @After execution3 || execution4
     */
    public void afterExecution(final JoinPoint joinPoint) throws Throwable {
        ConstructorAdviceTest.logExecution("postExecution ");
    }

    /**
     * @Around execution5
     */
    public Object aroundExecution2(final JoinPoint joinPoint) throws Throwable {
        return new Integer(0);
    }
}
