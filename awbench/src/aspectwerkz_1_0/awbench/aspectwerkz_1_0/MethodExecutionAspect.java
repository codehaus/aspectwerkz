/**************************************************************************************
 * Copyright (c) Jonas Bon?r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package awbench.aspectwerkz_1_0;

import awbench.method.Execution;
import awbench.Run;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.Rtti;

/**
 * AspectWerkz 1.0 aspects
 * Note: 1.0 has args() but not StaticJp and this()/target()
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class MethodExecutionAspect {

    /** @Before execution(* awbench.method.Execution.before()) */
    public void before(JoinPoint jp) {
        Run.ADVICE_HIT++;
    }

    /** @Before execution(* awbench.method.Execution.beforeSJP()) */
    public void beforeSJP(JoinPoint jp) {
        Run.ADVICE_HIT++;
    }

    /** @Before execution(* awbench.method.Execution.beforeJP()) */
    public void beforeJP(JoinPoint jp) {
        Run.ADVICE_HIT++;
    }

    /** @Before execution(* awbench.method.Execution.withPrimitiveArgs(int)) && args(i) */
    public void beforeWithPrimitiveArgs(JoinPoint jp, int i) {
        int j = i;
        Run.ADVICE_HIT++;
    }

    /** @Before execution(* awbench.method.Execution.withWrappedArgs(java.lang.Integer)) && args(i) */
    public void beforeWithWrappedArgs(JoinPoint jp, Integer i) {
        Rtti rtti = jp.getRtti();
        Integer j = i;
        Run.ADVICE_HIT++;
    }

    /** @Before execution(* awbench.method.Execution.beforeAfter())
        @After  execution(* awbench.method.Execution.beforeAfter()) */
    public void beforeAfter(JoinPoint jp) {
        Run.ADVICE_HIT++;
    }

    /**
     * @Around execution(* awbench.method.Execution.afterReturningString())
     */
    public Object afterReturning(JoinPoint jp) throws Throwable {
        String value = null;
        Object result = jp.proceed();
        if (value instanceof String) {
            value = (String)result;
            Run.ADVICE_HIT++;
        }
         return result;
    }

     /**
      * @Around execution(* awbench.method.Execution.afterThrowingRTE())
     */
    public Object afterThrowing(JoinPoint jp) throws Throwable {
        RuntimeException e;
         Object result = null;
         try {
             result = jp.proceed();
         } catch (RuntimeException throwable) {
             e = throwable;
             Run.ADVICE_HIT++;
             throw e;
         }
         return result;
     }

    /** @Around  execution(* awbench.method.Execution.aroundJP()) */
    public Object aroundJP(JoinPoint jp) throws Throwable {
        Run.ADVICE_HIT++;
        Rtti rtti = jp.getRtti();
        return jp.proceed();
    }

    /** @Around  execution(* awbench.method.Execution.aroundSJP()) */
    public Object aroundSJP(JoinPoint jp) throws Throwable {
        Run.ADVICE_HIT++;
        return jp.proceed();
    }

    /** @Before  execution(* awbench.method.Execution.withArgsAndTarget(int)) && args(i) */
    public void beforeWithArgsAndTarget(JoinPoint jp, int i) {
        int j = i;
        Execution u = (Execution)jp.getTarget();
        Run.ADVICE_HIT++;
    }

    /** @Around  execution(* awbench.method.Execution.aroundStackedWithArgAndTarget(int)) && args(i) */
    public void aroundStackedWithArgAndTarget_1(JoinPoint jp, int i) {
        int j = i;
        Execution u = (Execution)jp.getTarget();
        Run.ADVICE_HIT++;
    }

    /** @Around  execution(* awbench.method.Execution.aroundStackedWithArgAndTarget(int)) && args(i) */
    public void aroundStackedWithArgAndTarget_2(JoinPoint jp, int i) {
        int j = i;
        Execution u = (Execution)jp.getTarget();
        Run.ADVICE_HIT++;
    }

}
