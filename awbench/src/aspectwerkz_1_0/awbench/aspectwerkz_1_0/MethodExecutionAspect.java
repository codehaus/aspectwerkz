/**************************************************************************************
 * Copyright (c) Jonas Bon?r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package awbench.aspectwerkz_1_0;

import awbench.method.Execution;
import org.codehaus.aspectwerkz.joinpoint.StaticJoinPoint;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

/**
 * AspectWerkz 1.0 aspects
 * Note: 1.0 has args() but not StaticJp and this()/target()
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class MethodExecutionAspect {

    public static int s_count = 0;


    /** @Before execution(* awbench.method.Execution.before()) */
    public void before(JoinPoint jp) {
        s_count++;
    }

    /** @Before execution(* awbench.method.Execution.beforeSjp()) */
    public void beforeSjp(JoinPoint jp) {
        s_count++;
    }

    /** @Before execution(* awbench.method.Execution.beforeJp()) */
    public void beforeJp(JoinPoint jp) {
        s_count++;
    }

    /** @Before execution(* awbench.method.Execution.withPrimitiveArgs(int)) && args(i) */
    public void beforeWithPrimitiveArgs(JoinPoint jp, int i) {
        int j = i;
        s_count++;
    }

    /** @Before execution(* awbench.method.Execution.withWrappedArgs(java.lang.Integer)) && args(i) */
    public void beforeWithWrappedArgs(JoinPoint jp, Integer i) {
        Integer j = i;
        s_count++;
    }

    /** @Before execution(* awbench.method.Execution.beforeAfter()) */
    /** @After  execution(* awbench.method.Execution.beforeAfter()) */
    public void beforeAfter(JoinPoint jp) {
        s_count++;
    }

    /** @Around  execution(* awbench.method.Execution.aroundJp()) */
    public Object aroundJp(JoinPoint jp) throws Throwable {
        s_count++;
        return jp.proceed();
    }

    //TODO: add Rtti around

    /** @Before  execution(* awbench.method.Execution.withArgsAndTarget(int)) && args(i) */
    public void beforeWithArgsAndTarget(JoinPoint jp, int i) {
        int j = i;
        Execution u = (Execution)jp.getTarget();
        s_count++;
    }


}
