/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.xmldef;

import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.xmldef.advice.AroundAdvice;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class SimpleMethodAdvice extends AroundAdvice {
    public SimpleMethodAdvice() {
        super();
    }

    public Object execute(final JoinPoint joinPoint) throws Throwable {
        final Object result = joinPoint.proceed();
        return result;
    }
}
