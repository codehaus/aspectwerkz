/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package examples.logging;

import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.MethodJoinPoint;
import org.codehaus.aspectwerkz.aspect.AbstractAspect;

/**
 * @Aspect("perThread")
 */
public class LoggingAspect extends AbstractAspect {

    private int m_level = 0;

    /**
     * @Pointcut ("* examples.logging.Target.toLog*(..)")
     */
    void test() {}

    /**
     * @AroundAdvice ("test")
     */
    public Object logMethod(final JoinPoint joinPoint) throws Throwable {
        MethodJoinPoint jp = (MethodJoinPoint)joinPoint;
        indent();
        System.out.println("--> " + jp.getTargetClass().getName() + "::" + jp.getMethodName());
        m_level++;
        final Object result = joinPoint.proceed();
        m_level--;
        indent();
        System.out.println("<-- " + jp.getTargetClass().getName() + "::" + jp.getMethodName());
        return result;
    }

    private void indent() {
        for (int i = 0; i < m_level; i++) {
            System.out.print("  ");
        }
    }
}
