package test;

import org.codehaus.aspectwerkz.advice.AroundAdvice;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.MethodJoinPoint;

/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
public class MyStaticMethodAdvice2 extends AroundAdvice {
    public MyStaticMethodAdvice2() {
        super();
    }

    public Object execute(final JoinPoint joinPoint) throws Throwable {
        MethodJoinPoint jp = (MethodJoinPoint)joinPoint;
        ((StaticMethodAdviceTest)jp.getTargetObject()).log("before1 ");
        final Object result = joinPoint.proceed();
        ((StaticMethodAdviceTest)jp.getTargetObject()).log("after1 ");
        return result;
    }
}
