/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.orthogonal;

import test.Loggable;
import org.codehaus.aspectwerkz.Pointcut;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @Aspect perJVM
 */
public class OrthogonalTestAspect {
    // ============ Pointcuts ============

    /**
    * @Expression execution(* test.orthogonal.OrthogonalTest.method*(..))
    */
    Pointcut pcMethod;

    /**
    * @Expression get(* test.orthogonal.OrthogonalTest.m_getFieldAroundAdviced)
    */
    Pointcut pcGet;

    /**
    * @Expression set(* test.orthogonal.OrthogonalTest.m_setFieldAroundAdviced)
    */
    Pointcut pcSet;

    // ============ Advices ============

    /**
    * @Around pcMethod || pcGet || pcSet
    */
    public Object advice1(final JoinPoint joinPoint) throws Throwable {
        ((Loggable)joinPoint.getTargetInstance()).log("before ");
        Object o = joinPoint.proceed();
        ((Loggable)joinPoint.getTargetInstance()).log("after ");
        return o;
    }
}
