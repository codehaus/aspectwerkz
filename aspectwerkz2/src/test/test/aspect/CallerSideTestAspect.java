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
import test.CallerSideAdviceTest;

/**
 * @Aspect perJVM
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class CallerSideTestAspect extends Aspect {

    // ============ Pointcuts ============

    /** @Call test.CallerSideAdviceTest->String test.CallerSideTestHelper.invokeMemberMethodPre() */
    Pointcut pc1;
    /** @Call test.CallerSideAdviceTest->String test.CallerSideTestHelper.invokeMemberMethodPost() */
    Pointcut pc2;
    /** @Call * test.CallerSideAdviceTest->String test.CallerSideTestHelper.invokeMemberMethodPrePost() */
    Pointcut pc3;
    /** @Call test.CallerSideAdviceTest->String test.CallerSideTestHelper.invokeStaticMethodPre() */
    Pointcut pc4;
    /** @Call test.CallerSideAdviceTest->String test.CallerSideTestHelper.invokeStaticMethodPost() */
    Pointcut pc5;
    /** @Call test.CallerSideAdviceTest->String test.CallerSideTestHelper.invokeStaticMethodPrePost() */
    Pointcut pc6;

    // ============ Advices ============

    /**
     * @Before pc1 || pc3 || pc4 || pc6
     */
    public void preAdvice1(final JoinPoint joinPoint) throws Throwable {
        CallerSideAdviceTest.log("pre1 ");
    }

    /**
     * @Before pc1 || pc3 || pc4 || pc6
     */
    public void preAdvice2(final JoinPoint joinPoint) throws Throwable {
        CallerSideAdviceTest.log("pre2 ");
    }

    /**
     * @After pc2 || pc3 || pc5 || pc6
     */
    public void postAdvice1(final JoinPoint joinPoint) throws Throwable {
        CallerSideAdviceTest.log("post1 ");
    }

    /**
     * @After pc2 || pc3 || pc5 || pc6
     */
    public void postAdvice2(final JoinPoint joinPoint) throws Throwable {
        CallerSideAdviceTest.log("post2 ");
    }
}
