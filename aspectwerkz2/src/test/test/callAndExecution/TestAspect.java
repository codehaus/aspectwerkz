/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.callAndExecution;

import org.codehaus.aspectwerkz.Pointcut;
import org.codehaus.aspectwerkz.aspect.Aspect;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class TestAspect extends Aspect {

    // ============ Pointcuts ============

    /**
     * @Call *..*->void test.callAndExecution.CallExecutionTest.privateMethod()
     */
    Pointcut call1;

    /**
     * @Call *..*->void test.callAndExecution.CallExecutionTest.publicMethod()
     */
    Pointcut call2;

    /**
     * @Execution void test.callAndExecution.CallExecutionTest.privateMethod()
     */
    Pointcut execution1;

    /**
     * @Execution void test.callAndExecution.CallExecutionTest.publicMethod()
     */
    Pointcut execution2;

    // ============ Advices ============

    /**
     * @Around call1 || call2
     */
    public Object advice1(final JoinPoint joinPoint) throws Throwable {
        CallExecutionTest.log("call1 ");
        Object result = joinPoint.proceed();
        CallExecutionTest.log("call2 ");
        return result;
    }

    /**
     * @Around execution1 || execution2
     */
    public Object advice2(final JoinPoint joinPoint) throws Throwable {
        CallExecutionTest.log("execution1 ");
        Object result = joinPoint.proceed();
        CallExecutionTest.log("execution2 ");
        return result;
    }
}
