/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.performance;

import org.codehaus.aspectwerkz.aspect.Aspect;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.Pointcut;

/**
 * @Aspect
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public class PerClassPerformanceAspect extends Aspect {

    /**
     * @Class test.performance.PerformanceTest
     */
    Pointcut mixin;

    /**
     * @Execution void test.performance.PerformanceTest.methodAdvisedMethodPerClass()
     */
    Pointcut pc;

    /**
     * @Around pc
     */
    public Object advice(final JoinPoint joinPoint) throws Throwable {
        return joinPoint.proceed();
    }

    /**
     * @Introduce mixin
     */
    public class PerClassImpl implements PerClass {
        public void runPerClass() {
        }
    }
}
