/**************************************************************************************
 * Copyright (c) Jonas Bon?r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.aopc;

import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

/**
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class BaseAspect
{
    public Object logAround(JoinPoint jp)
        throws Throwable
    {
        ((Callable) jp.getTargetInstance()).log("beforeAround ");

        Object result = jp.proceed();

        ((Callable) jp.getTargetInstance()).log("afterAround ");

        return result;
    }
}
