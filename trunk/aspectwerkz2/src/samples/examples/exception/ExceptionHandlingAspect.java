/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package examples.exception;

import org.codehaus.aspectwerkz.Pointcut;
import org.codehaus.aspectwerkz.aspect.Aspect;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

/**
 * @Aspect
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class ExceptionHandlingAspect extends Aspect {

    /**
     * @Throws * examples.exception.Target.*(..)#java.lang.Exception
     */
    Pointcut methods;

    /**
     * @Around methods
     */
    public Object logEntry(final JoinPoint joinPoint) throws Throwable {
        throw new UnsupportedOperationException("throws are currently not supported");
//        ThrowsJoinPoint jp = (ThrowsJoinPoint)joinPoint;
//        System.out.println("'" +
//                jp.getExceptionName() + "' with message '" +
//                jp.getMessage() + "' has ben thrown out of '" +
//                jp.getTargetClass().getName() + "." +
//                jp.getMethodName() + "'");
//        return "fake result from advice";
    }
}
