/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test;

import org.codehaus.aspectwerkz.xmldef.advice.AroundAdvice;
import org.codehaus.aspectwerkz.xmldef.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.xmldef.joinpoint.MethodJoinPoint;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public class CFlowLoggingAdvice extends AroundAdvice {

    public CFlowLoggingAdvice() {
        super();
    }

    public Object execute(final JoinPoint joinPoint) throws Throwable {
        MethodJoinPoint jp = (MethodJoinPoint)joinPoint;
        ((Loggable)jp.getTargetObject()).log("advice-before ");
        final Object result = joinPoint.proceed();
        ((Loggable)jp.getTargetObject()).log("advice-after ");
        return result;
    }
}
