package test;

import org.codehaus.aspectwerkz.advice.AroundAdvice;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
public class MyMethodAdviceForPerfTest extends AroundAdvice {
    public MyMethodAdviceForPerfTest() {
        super();
    }

    public Object execute(final JoinPoint joinPoint) throws Throwable {
        return joinPoint.proceed();
    }
}
