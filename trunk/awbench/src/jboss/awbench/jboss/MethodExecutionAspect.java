/**************************************************************************************
 * Copyright (c) Jonas Bon?r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package awbench.jboss;

import awbench.method.Execution;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.MethodInvocation;

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
 */
public class MethodExecutionAspect {

    public static int s_count = 0;


    public Object before(Invocation jp) throws Throwable {
        s_count++;
        return jp.invokeNext();
    }

    public Object beforeSJP(Invocation jp) throws Throwable {
        s_count++;
        return jp.invokeNext();
    }

    public Object beforeJP(Invocation jp) throws Throwable {
        s_count++;
        return jp.invokeNext();
    }

    // Note: advice cannot have args in JBoss AOP
    public Object beforeWithPrimitiveArgs(Invocation jp)/*, int i)*/ throws Throwable {
        int j = ((Integer)((MethodInvocation)jp).getArguments()[0]).intValue();
        s_count++;
        return jp.invokeNext();
    }

    // Note: advice cannot have args in JBoss AOP
    public Object beforeWithWrappedArgs(Invocation jp)/*, Integer i)*/ throws Throwable {
        Integer j = (Integer)((MethodInvocation)jp).getArguments()[0];
        s_count++;
        return jp.invokeNext();
    }

    //JBossAOP does not have before after, so here we are using TWO advices since using one around advice
    //will not be the same (the after advice is not necessarily by the same developper and in the same aspect ...
    public Object beforeAfter_1(Invocation jp) throws Throwable {
        s_count++;
        return jp.invokeNext();
    }
    public Object beforeAfter_2(Invocation jp) throws Throwable {
        Object o = jp.invokeNext();
        s_count++;
        return o;
    }

    public Object aroundJP(Invocation jp) throws Throwable {
        s_count++;
        return jp.invokeNext();
    }

    public Object aroundSJP(Invocation jp) throws Throwable {
        s_count++;
        return jp.invokeNext();
    }

    // Note: advice cannot have args in JBoss AOP
    public Object beforeWithArgsAndTarget(Invocation jp)/*, int i)*/ throws Throwable {
        int j = ((Integer)((MethodInvocation)jp).getArguments()[0]).intValue();
        Execution u = (Execution)jp.getTargetObject();
        s_count++;
        return jp.invokeNext();
    }
}
