/**************************************************************************************
 * Copyright (c) Jonas Bon?r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package awbench.aspectwerkz;

import awbench.method.Execution;
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
 */
public class MethodExecutionAspect {

    public static int s_count = 0;

    /** @Before execution(* awbench.method.Execution.before()) */
    public void before() {
        s_count++;
    }

    /** @Before execution(* awbench.method.Execution.beforeSJP()) */
    public void beforeSJP(StaticJoinPoint sjp) {
        s_count++;
    }

    /** @Before execution(* awbench.method.Execution.beforeJP()) */
    public void beforeJP(JoinPoint jp) {
        Rtti rtti = jp.getRtti();
        s_count++;
    }

    /** @Before execution(* awbench.method.Execution.withPrimitiveArgs(int)) && args(i) */
    public void beforeWithPrimitiveArgs(int i) {
        int j = i;
        s_count++;
    }

    /** @Before execution(* awbench.method.Execution.withWrappedArgs(java.lang.Integer)) && args(i) */
    public void beforeWithWrappedArgs(Integer i) {
        Integer j = i;
        s_count++;
    }

    /** @Before execution(* awbench.method.Execution.beforeAfter()) */
    /** @After  execution(* awbench.method.Execution.beforeAfter()) */
    public void beforeAfter() {
        s_count++;
    }

    /** @Around  execution(* awbench.method.Execution.aroundJP()) */
    public Object aroundJP(JoinPoint jp) throws Throwable {
        s_count++;
        Rtti rtti = jp.getRtti();
        return jp.proceed();
    }

    /** @Around  execution(* awbench.method.Execution.aroundSJP()) */
    public Object aroundSJP(StaticJoinPoint sjp) throws Throwable {
        s_count++;
        return sjp.proceed();
    }

    /** @Before  execution(* awbench.method.Execution.withArgsAndTarget(int)) && args(i) && target(t) */
    public void beforeWithArgsAndTarget(int i, Execution t) {
        int j = i;
        Execution u = t;
        s_count++;
    }

    /** @Around  execution(* awbench.method.Execution.aroundStackedWithArgAndTarget(int)) && args(i) && target(t) */
    public Object aroundStackedWithArgAndTarget_1(StaticJoinPoint sjp, int i, Execution t) throws Throwable {
        int j = i;
        Execution u = t;
        s_count++;
        return sjp.proceed();
    }

    /** @Around  execution(* awbench.method.Execution.aroundStackedWithArgAndTarget(int)) && args(i) && target(t) */
    public Object aroundStackedWithArgAndTarget_2(StaticJoinPoint sjp, int i, Execution t) throws Throwable {
        int j = i;
        Execution u = t;
        s_count++;
        return sjp.proceed();
    }
}
