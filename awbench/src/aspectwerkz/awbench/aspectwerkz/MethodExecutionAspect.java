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
        // TODO - we don't make use of sjp / jp so a very lazy impl could hide its weakness
        // but we want it comparable to before() advice
        s_count++;
    }

    /** @Before execution(* awbench.method.Execution.beforeJP()) */
    public void beforeJP(JoinPoint jp) { // , Rtti rtti) {//when using Rtti, the current impl creates a lof of object not lazyly
        // TODO - we don't make use of sjp / jp so a very lazy impl could hide its weakness
        // but we want it comparable to before() advice
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
        return jp.proceed();
    }

    /** @Around  execution(* awbench.method.Execution.aroundSJP()) */
    public Object aroundSJP(StaticJoinPoint jp) throws Throwable {
        s_count++;
        return jp.proceed();
    }

    //TODO: add Rtti around

    /** @Before  execution(* awbench.method.Execution.withArgsAndTarget(int)) && args(i) && target(t) */
    public void beforeWithArgsAndTarget(int i, Execution t) {
        int j = i;
        Execution u = t;
        s_count++;
    }


}
