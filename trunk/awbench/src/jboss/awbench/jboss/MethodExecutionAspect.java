/**************************************************************************************
 * Copyright (c) Jonas Bon?r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package awbench.jboss;

import awbench.method.Execution;
import awbench.Run;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.MethodInvocation;
import org.jboss.aop.joinpoint.MethodJoinpoint;

/**
 * JBoss 1.0 so called "AOP" aspects
 * Does not support args(), this()/target(), and neither before/after constructs...
 * <p/>
 * Does not support before / after and thus requires invokeNext() to be called...
 * <p/>
 * JBoss requires XML def for 1.4 and only supports annotation for 1.5, though providing a 1.4 annotation compiler ???
 * Probably since they cannot resolve precedence in a proper way without advice stack in XML.
 * <p/>
 * We leave the annotation there for readability purpose.
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class MethodExecutionAspect {

    public Object before(Invocation jp) throws Throwable {
        Run.ADVICE_HIT++;
        return jp.invokeNext();
    }

    public Object beforeSJP(Invocation jp) throws Throwable {
        Run.ADVICE_HIT++;
        Object sig = ((MethodInvocation)jp).getMethod();//signature access
        return jp.invokeNext();
    }

    public Object beforeJP(Invocation jp) throws Throwable {
        Run.ADVICE_HIT++;
        Object target = jp.getTargetObject();
        return jp.invokeNext();
    }

    // Note: advice cannot have args in JBoss AOP
    public Object beforeWithPrimitiveArgs(Invocation jp) throws Throwable {
        int j = ((Integer)((MethodInvocation)jp).getArguments()[0]).intValue();
        Run.ADVICE_HIT++;
        return jp.invokeNext();
    }

    // Note: advice cannot have args in JBoss AOP
    public Object beforeWithWrappedArgs(Invocation jp) throws Throwable {
        Integer j = (Integer)((MethodInvocation)jp).getArguments()[0];
        Run.ADVICE_HIT++;
        return jp.invokeNext();
    }

    //JBossAOP does not have before after, so here we are using TWO advices since using one around advice
    //will not be the same (the after advice is not necessarily by the same developper and in the same aspect ...
    public Object beforeAfter_1(Invocation jp) throws Throwable {
        Run.ADVICE_HIT++;
        return jp.invokeNext();
    }
    public Object beforeAfter_2(Invocation jp) throws Throwable {
        Object o = jp.invokeNext();
        Run.ADVICE_HIT++;
        return o;
    }

    public Object afterReturning(Invocation jp) throws Throwable {
        String value = null;
        Object result = jp.invokeNext();
        if (result instanceof String) {
            value = (String)result;
            Run.ADVICE_HIT++;
        }
         return result;
    }

    public Object afterThrowing(Invocation jp) throws Throwable {
        RuntimeException e;
         Object result = null;
         try {
             result = jp.invokeNext();
         } catch (RuntimeException throwable) {
             e = throwable;
             Run.ADVICE_HIT++;
             throw e;
         }
         return result;
     }

    public Object around(Invocation jp) throws Throwable {
        Run.ADVICE_HIT++;
        return jp.invokeNext();
    }

    public Object aroundSJP(Invocation jp) throws Throwable {
        Run.ADVICE_HIT++;
        Object sig = ((MethodInvocation)jp).getMethod();
        return jp.invokeNext();
    }

    public Object aroundJP(Invocation jp) throws Throwable {
        Run.ADVICE_HIT++;
        Object target = jp.getTargetObject();
        return jp.invokeNext();
    }

    // Note: advice cannot have args in JBoss AOP
    public Object beforeWithArgsAndTarget(Invocation jp) throws Throwable {
        int j = ((Integer)((MethodInvocation)jp).getArguments()[0]).intValue();
        Execution u = (Execution)jp.getTargetObject();
        Run.ADVICE_HIT++;
        return jp.invokeNext();
    }

    // Note: advice cannot have args in JBoss AOP
    public Object aroundStackedWithArgAndTarget_1(Invocation jp) throws Throwable {
        int j = ((Integer)((MethodInvocation)jp).getArguments()[0]).intValue();
        Execution u = (Execution)jp.getTargetObject();
        Run.ADVICE_HIT++;
        return jp.invokeNext();
    }
    // Note: advice cannot have args in JBoss AOP
    public Object aroundStackedWithArgAndTarget_2(Invocation jp) throws Throwable {
        int j = ((Integer)((MethodInvocation)jp).getArguments()[0]).intValue();
        Execution u = (Execution)jp.getTargetObject();
        Run.ADVICE_HIT++;
        return jp.invokeNext();
    }

}
