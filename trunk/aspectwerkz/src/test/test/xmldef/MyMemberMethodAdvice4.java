/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.xmldef;

import org.codehaus.aspectwerkz.xmldef.advice.AroundAdvice;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.MethodJoinPoint;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class MyMemberMethodAdvice4 extends AroundAdvice {
    public MyMemberMethodAdvice4() {
        super();
    }

    public Object execute(final JoinPoint joinPoint) throws Throwable {
        final Object result = joinPoint.proceed();
        MethodJoinPoint jp = (MethodJoinPoint)joinPoint;
        String metadata =
                jp.getTargetClass().getName() +
                jp.getMethod().getName() +
                jp.getTargetInstance().hashCode() +
                jp.getParameters()[0] +
                jp.getParameterTypes()[0].getName() +
                jp.getReturnType().getName() +
                jp.getResult();
        return metadata;
    }
}
