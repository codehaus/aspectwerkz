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
import org.codehaus.aspectwerkz.Pointcut;

/**
 * @Aspect perJVM
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class LoggingAspect extends AbstractAspect {

    private int m_level = 0;

    // ============ Pointcuts ============

    /**
     * @Execution * examples.logging.Target.toLog1(..)
     */
    Pointcut methodsToLog1;

    /**
     * @Execution * examples.logging.Target.toLog2(..)
     */
    Pointcut methodsToLog2;

    /**
     * @Execution * examples.logging.Target.toLog3(..)
     */
    Pointcut methodsToLog3;

    /**
     * @Get int examples.logging.Target.m_counter1
     */
    Pointcut logGet;

    /**
     * @Set int examples.logging.Target.m_counter2
     */
    Pointcut logSet;

    // ============ Advices ============

    /**
     * @AroundAdvice methodsToLog1 || methodsToLog2 || methodsToLog3
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
     * @PreAdvice logSet
     */
    public void logEntry(final JoinPoint joinPoint) throws Throwable {
        FieldJoinPoint jp = (FieldJoinPoint)joinPoint;
        System.out.println("ENTER: " + jp.getTargetClass().getName() + "::" + jp.getFieldName());
    }

    /**
     * @PostAdvice logSet
     */
    public void logExit(final JoinPoint joinPoint) throws Throwable {
        FieldJoinPoint jp = (FieldJoinPoint)joinPoint;
        System.out.println("EXIT: " + jp.getTargetClass().getName() + "::" + jp.getFieldName());
    }

    // ============ Introductions ============

    public String getName() {
        return "Jonas Bonér";
    }

    private void indent() {
        for (int i = 0; i < m_level; i++) {
            System.out.print("  ");
        }
    }
}
