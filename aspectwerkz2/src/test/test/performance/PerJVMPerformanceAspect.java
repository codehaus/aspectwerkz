/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.performance;

import org.codehaus.aspectwerkz.aspect.Aspect;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.Pointcut;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 * @Aspect perJVM
 */
public class PerJVMPerformanceAspect extends Aspect {

    /**
     * @Class test.performance.PerformanceTest
     */
    Pointcut mixin;

    /**
     * @Call void test.performance.PerformanceTest.methodAdvisedMethodPerJVM()
     */
    Pointcut pc;

    /**
     * @Around pc
     */
    public Object advice1(final JoinPoint joinPoint) throws Throwable {
        return joinPoint.proceed();
    }

    /**
     * Around pc
     */
    public Object advice2(final JoinPoint joinPoint) throws Throwable {
        return joinPoint.proceed();
    }

    /**
     * Around pc
     */
    public Object advice3(final JoinPoint joinPoint) throws Throwable {
        return joinPoint.proceed();
    }

    /**
     * Around pc
     */
    public Object advice4(final JoinPoint joinPoint) throws Throwable {
        return joinPoint.proceed();
    }

    /**
     * Around pc
     */
    public Object advice5(final JoinPoint joinPoint) throws Throwable {
        return joinPoint.proceed();
    }

    /**
     * @Introduce mixin
     */
    public class PerJVMImpl implements PerJVM {
        public void runPerJVM() {
        }
    }
}
