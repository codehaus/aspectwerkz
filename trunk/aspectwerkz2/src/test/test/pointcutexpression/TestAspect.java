/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.pointcutexpression;

import org.codehaus.aspectwerkz.aspect.Aspect;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.Pointcut;
import test.Loggable;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @Aspect
 */
public class TestAspect extends Aspect {

    /**
     * @Execution void test.pointcutexpression.PointcutExpressionTest.*()
     */
    Pointcut generic;
    /**
     * @Execution void test.pointcutexpression.PointcutExpressionTest.A()
     */
    Pointcut A;
    /**
     * @Execution void test.pointcutexpression.PointcutExpressionTest.B()
     */
    Pointcut B;
    /**
     * @Execution void test.pointcutexpression.PointcutExpressionTest.C()
     */
    Pointcut C;
    /**
     * @Execution void test.pointcutexpression.PointcutExpressionTest.D()
     */
    Pointcut D;
    /**
     * @Execution void test.pointcutexpression.PointcutExpressionTest.E()
     */
    Pointcut E;
    /**
     * @Execution void test.pointcutexpression.PointcutExpressionTest.F()
     */
    Pointcut F;
    /**
     * @Execution void test.pointcutexpression.PointcutExpressionTest.G()
     */
    Pointcut G;
    /**
     * @Execution void test.pointcutexpression.PointcutExpressionTest.H()
     */
    Pointcut H;
    /**
     * @Execution void test.pointcutexpression.PointcutExpressionTest.I()
     */
    Pointcut I;
    /**
     * @Execution void test.pointcutexpression.PointcutExpressionTest.J()
     */
    Pointcut J;
    /**
     * @Execution void test.pointcutexpression.PointcutExpressionTest.K()
     */
    Pointcut K;
    /**
     * @Execution void test.pointcutexpression.PointcutExpressionTest.L()
     */
    Pointcut L;
    /**
     * @Execution void test.pointcutexpression.PointcutExpressionTest.M()
     */
    Pointcut M;
    /**
     * @Execution void test.pointcutexpression.PointcutExpressionTest.N()
     */
    Pointcut N;
    /**
     * @Execution void test.pointcutexpression.PointcutExpressionTest.O()
     */
    Pointcut O;

    /**
     * @Around B || C
     */
    public Object advice1(final JoinPoint joinPoint) throws Throwable {
        ((Loggable)joinPoint.getTargetInstance()).log("before1 ");
        final Object result = joinPoint.proceed();
        ((Loggable)joinPoint.getTargetInstance()).log("after1 ");
        return result;
    }

    /**
     * @Around D && !E
     */
    public Object advice2(final JoinPoint joinPoint) throws Throwable {
        ((Loggable)joinPoint.getTargetInstance()).log("before1 ");
        final Object result = joinPoint.proceed();
        ((Loggable)joinPoint.getTargetInstance()).log("after1 ");
        return result;
    }

    /**
     * @Around (F || G) && H
     */
    public Object advice3(final JoinPoint joinPoint) throws Throwable {
        ((Loggable)joinPoint.getTargetInstance()).log("before1 ");
        final Object result = joinPoint.proceed();
        ((Loggable)joinPoint.getTargetInstance()).log("after1 ");
        return result;
    }

    /**
     * @Around (I || J) && generic
     */
    public Object advice4(final JoinPoint joinPoint) throws Throwable {
        ((Loggable)joinPoint.getTargetInstance()).log("before1 ");
        final Object result = joinPoint.proceed();
        ((Loggable)joinPoint.getTargetInstance()).log("after1 ");
        return result;
    }

    /**
     * @Around !K && !(L || M) && N
     */
    public Object advice5(final JoinPoint joinPoint) throws Throwable {
        ((Loggable)joinPoint.getTargetInstance()).log("before1 ");
        final Object result = joinPoint.proceed();
        ((Loggable)joinPoint.getTargetInstance()).log("after1 ");
        return result;
    }

    /**
     * @Around O
     */
    public Object advice6(final JoinPoint joinPoint) throws Throwable {
        ((Loggable)joinPoint.getTargetInstance()).log("before1 ");
        final Object result = joinPoint.proceed();
        ((Loggable)joinPoint.getTargetInstance()).log("after1 ");
        return result;
    }
}
