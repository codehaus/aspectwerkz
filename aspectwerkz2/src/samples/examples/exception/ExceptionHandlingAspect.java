/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package examples.exception;

import org.codehaus.aspectwerkz.Pointcut;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.CatchClauseSignature;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @Aspect
 */
public class ExceptionHandlingAspect {

    /**
     * @Expression handler(java.lang.Exception)
     */
    Pointcut methods;

    /**
     * @Before methods
     */
    public Object logEntry(final JoinPoint joinPoint) throws Throwable {
        CatchClauseSignature sig = (CatchClauseSignature)joinPoint.getSignature();
        Exception e = (Exception)sig.getParameterValue();
        System.out.println("[From advice] exception catched:" + e.toString());
        return "fake result from advice";
    }
}
