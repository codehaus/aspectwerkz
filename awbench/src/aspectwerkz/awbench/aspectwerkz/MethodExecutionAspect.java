/**************************************************************************************
 * Copyright (c) Jonas Bon?r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package awbench.aspectwerkz;

import awbench.method.Execution;
import awbench.Run;
import org.codehaus.aspectwerkz.joinpoint.StaticJoinPoint;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.Rtti;

/**
 * AW 2.0 aspect
 * The advice bodies should be the same for comparative benchs that is if the JP is not accessed,
 * it should not be neither in other frameworks bench.
 * Else write another test.
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class MethodExecutionAspect {

    /** @Before execution(* awbench.method.Execution.before()) */
    public void before() {
        Run.ADVICE_HIT++;
    }

    /** @Before execution(* awbench.method.Execution.beforeSJP()) */
    public void beforeSJP(StaticJoinPoint sjp) {
        Object sig = sjp.getSignature();
        Run.ADVICE_HIT++;
    }

    /** @Before execution(* awbench.method.Execution.beforeJP()) */
    public void beforeJP(JoinPoint jp) {
        Object target = jp.getTarget();
        Run.ADVICE_HIT++;
    }

    /** @Before execution(* awbench.method.Execution.beforeWithPrimitiveArgs(int)) && args(i) */
    public void beforeWithPrimitiveArgs(int i) {
        int j = i;
        Run.ADVICE_HIT++;
    }

    /** @Before execution(* awbench.method.Execution.beforeWithWrappedArgs(java.lang.Integer)) && args(i) */
    public void beforeWithWrappedArgs(Integer i) {
        Integer j = i;
        Run.ADVICE_HIT++;
    }

    /** @Before execution(* awbench.method.Execution.beforeAfter())
        @After  execution(* awbench.method.Execution.beforeAfter()) */
    public void beforeAfter() {
        Run.ADVICE_HIT++;
    }

    /**
     * @AfterReturning(
     *      type = "returnValue",
     *      pointcut = "execution(* awbench.method.Execution.afterReturningString())"
     * )
     */
    public void afterReturning(String returnValue) {
        String value = returnValue;
        Run.ADVICE_HIT++;
    }

     /**
      * @AfterThrowing(
      *      type = "rte",
      *      pointcut = "execution(* awbench.method.Execution.afterThrowingRTE())"
      * )
     */
    public void afterThrowing(RuntimeException rte) {
        RuntimeException e = rte;
        Run.ADVICE_HIT++;
    }

    /** @Around  execution(* awbench.method.Execution.around_()) */
    public Object around(StaticJoinPoint sjp) throws Throwable {
        Run.ADVICE_HIT++;
        return sjp.proceed();
    }

    /** @Around  execution(* awbench.method.Execution.aroundSJP()) */
    public Object aroundSJP(StaticJoinPoint sjp) throws Throwable {
        Run.ADVICE_HIT++;
        Object o = sjp.getSignature();
        return sjp.proceed();
    }

    /** @Around  execution(* awbench.method.Execution.aroundJP()) */
    public Object aroundJP(JoinPoint jp) throws Throwable {
        Run.ADVICE_HIT++;
        Object target = jp.getTarget();
        return jp.proceed();
    }

    /** @Before  execution(* awbench.method.Execution.beforeWithArgsAndTarget(int)) && args(i) && target(t) */
    public void beforeWithArgsAndTarget(int i, Execution t) {
        int j = i;
        Execution u = t;
        Run.ADVICE_HIT++;
    }

    /** @Around  execution(* awbench.method.Execution.aroundStackedWithArgAndTarget(int)) && args(i) && target(t) */
    public Object aroundStackedWithArgAndTarget_1(StaticJoinPoint sjp, int i, Execution t) throws Throwable {
        int j = i;
        Execution u = t;
        Run.ADVICE_HIT++;
        return sjp.proceed();
    }

    /** @Around  execution(* awbench.method.Execution.aroundStackedWithArgAndTarget(int)) && args(i) && target(t) */
    public Object aroundStackedWithArgAndTarget_2(StaticJoinPoint sjp, int i, Execution t) throws Throwable {
        int j = i;
        Execution u = t;
        Run.ADVICE_HIT++;
        return sjp.proceed();
    }
}
