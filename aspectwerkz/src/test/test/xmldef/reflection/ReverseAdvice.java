/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.xmldef.reflection;

import org.codehaus.aspectwerkz.xmldef.advice.AroundAdvice;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

public class ReverseAdvice extends AroundAdvice {

    public Object execute(final JoinPoint joinPoint) throws Throwable {
        Integer result = (Integer) joinPoint.proceed();
        return new Integer( -1 * result.intValue() );
    }
}
