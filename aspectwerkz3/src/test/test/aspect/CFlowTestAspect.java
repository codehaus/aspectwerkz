/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.aspect;

import org.codehaus.aspectwerkz.Pointcut;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

import test.Loggable;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @Aspect perJVM
 */
public class CFlowTestAspect
{
    // ============ Pointcuts ============

    /**
     * @Expression cflow(call(* test.CFlowTest.step1()) && within(test.CFlowTest))
     */
    Pointcut cflowPC1;

    /**
     * @Expression cflow(call(* test.CFlowTest.step1_A()) && within(test.CFlowTest))
     */
    Pointcut cflowPC2;

    /**
     * @Expression cflow(call(* test.CFlowTest.step1_B()) && within(test.CFlowTest))
     */
    Pointcut cflowPC3;

    /**
     * @Expression cflow(call(* test.CFlowTest.step2()) && within(test.CFlowTest))
     */
    Pointcut cflowPC4;

    /**
     * @Expression execution(* test.CFlowTest.step2())
     */
    Pointcut pc1;

    /**
     * @Expression execution(* test.CFlowTest.step2_B())
     */
    Pointcut pc2;

    // ============ Advices ============

    /**
     * @Around pc2 AND cflowPC1
     */
    public Object execute(final JoinPoint joinPoint)
        throws Throwable
    {
        ((Loggable) joinPoint.getTargetInstance()).log("advice-before ");

        final Object result = joinPoint.proceed();

        ((Loggable) joinPoint.getTargetInstance()).log("advice-after ");

        return result;
    }

    /**
     * @Around pc2 AND cflowPC3 AND cflowPC2
     */
    public Object execute2(final JoinPoint joinPoint)
        throws Throwable
    {
        ((Loggable) joinPoint.getTargetInstance()).log("advice-before2 ");

        final Object result = joinPoint.proceed();

        ((Loggable) joinPoint.getTargetInstance()).log("advice-after2 ");

        return result;
    }

    /**
     * @Around execution(* test.CFlowTest.step2Anonymous())
     *         &&
     *         cflow(call(* test.CFlowTest.step1Anonymous()) && within(test.CFlowTest))
     */
    public Object executeAnonymous(final JoinPoint joinPoint)
        throws Throwable
    {
        ((Loggable) joinPoint.getTargetInstance()).log(
            "advice-beforeAnonymous ");

        final Object result = joinPoint.proceed();

        ((Loggable) joinPoint.getTargetInstance()).log("advice-afterAnonymous ");

        return result;
    }

    /**
     * @Around execution(* test.CFlowTest.step2_C())
     *         AND NOT
     *         cflow(call(* test.CFlowTest.step1_C()) && within(test.CFlowTest))
     */
    public Object executeC(final JoinPoint joinPoint)
        throws Throwable
    {
        ((Loggable) joinPoint.getTargetInstance()).log("advice-beforeC ");

        final Object result = joinPoint.proceed();

        ((Loggable) joinPoint.getTargetInstance()).log("advice-afterC ");

        return result;
    }
}
