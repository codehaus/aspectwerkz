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
import org.codehaus.aspectwerkz.joinpoint.FieldJoinPoint;
import org.codehaus.aspectwerkz.aspect.AbstractAspect;

/**
 * @Aspect perJVM
 */
public class LoggingAspect extends AbstractAspect {

    private int m_level = 0;

    /**
     * @Pointcut execution(* examples.logging.Target.toLog*(..))
     */
    void methodsToLog() {}

    /**
     * @Pointcut set(int examples.logging.Target.m_counter)
     */
    void fieldsToLog() {}

    /**
     * @AroundAdvice methodsToLog
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

    /**
     * @PreAdvice fieldsToLog
     */
    public void logEntry(final JoinPoint joinPoint) throws Throwable {
        FieldJoinPoint jp = (FieldJoinPoint)joinPoint;
        System.out.println("ENTER: " + jp.getTargetClass().getName() + "::" + jp.getFieldName());
    }

    /**
     * @PostAdvice fieldsToLog
     */
    public void logExit(final JoinPoint joinPoint) throws Throwable {
        FieldJoinPoint jp = (FieldJoinPoint)joinPoint;
        System.out.println("EXIT: " + jp.getTargetClass().getName() + "::" + jp.getFieldName());
    }

    private void indent() {
        for (int i = 0; i < m_level; i++) {
            System.out.print("  ");
        }
    }
}
