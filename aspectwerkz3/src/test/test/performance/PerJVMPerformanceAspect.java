/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.performance;

import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 * @Aspect perJVM
 */
public class PerJVMPerformanceAspect {
    /**
     * @Around call(void test.performance.PerformanceTest.methodAdvisedMethodPerJVM()) && within(test.performance.*)
     */
    public Object advice1(final JoinPoint joinPoint) throws Throwable {
        return joinPoint.proceed();
    }

    /**
     * Around call(void test.performance.PerformanceTest.methodAdvisedMethodPerJVM()) && within(test.performance.*)
     */
    public Object advice2(final JoinPoint joinPoint) throws Throwable {
        return joinPoint.proceed();
    }

    /**
     * Around call(void test.performance.PerformanceTest.methodAdvisedMethodPerJVM()) && within(test.performance.*)
     */
    public Object advice3(final JoinPoint joinPoint) throws Throwable {
        return joinPoint.proceed();
    }

    /**
     * Around call(void test.performance.PerformanceTest.methodAdvisedMethodPerJVM()) && within(test.performance.*)
     */
    public Object advice4(final JoinPoint joinPoint) throws Throwable {
        return joinPoint.proceed();
    }

    /**
     * Around call(void test.performance.PerformanceTest.methodAdvisedMethodPerJVM()) && within(test.performance.*)
     */
    public Object advice5(final JoinPoint joinPoint) throws Throwable {
        return joinPoint.proceed();
    }

    /**
     * @Introduce within(test.performance.PerformanceTest)
     */
    public static class PerJVMImpl implements PerJVM {
        public void runPerJVM() {
        }
    }
}
