/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package examples.exception;

import org.codehaus.aspectwerkz.aspect.AbstractAspect;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.ThrowsJoinPoint;
import org.codehaus.aspectwerkz.Pointcut;

/**
 * @Aspect
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class ExceptionHandlingAspect extends AbstractAspect {

    /**
     * @Throws * examples.exception.Target.*(..)#java.lang.Exception
     */
    Pointcut methods;

    /**
     * @Around methods
     */
    public Object logEntry(final JoinPoint joinPoint) throws Throwable {
        ThrowsJoinPoint jp = (ThrowsJoinPoint)joinPoint;
        System.out.println("Class = " + jp.getTargetClass());
        System.out.println("Method = " + jp.getMethodName());
        System.out.println("Exception = " + jp.getExceptionName());
        System.out.println("Message = " + jp.getMessage());
        return "fake result from advice";
    }
}
