/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package examples.xmldef.cflow;

import org.codehaus.aspectwerkz.xmldef.advice.AroundAdvice;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 *
 * @aspectwerkz.advice-def name=cflow
 *                         deployment-model=perJVM
 *                         attribute=cflow
 */
public class LoggingAdvice extends AroundAdvice {

    public LoggingAdvice() {
        super();
    }

    public Object execute(final JoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();
        System.out.println("  --> invoking advice triggered by step2");
        return result;
    }
}
