/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package examples.attribdef.logging;

import org.codehaus.aspectwerkz.attribdef.aspect.Aspect;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.MethodJoinPoint;
import org.codehaus.aspectwerkz.joinpoint.FieldJoinPoint;

/**
 * @Aspect
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public abstract class AbstractLoggingAspect extends Aspect {

    private int m_level = 0;

    /**
     * @Around methodsToLog1
     * @Around methodsToLog2
     * @Around methodsToLog3
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
     * @Before logSet
     * @Before logGet
     */
    public void logEntry(final JoinPoint joinPoint) throws Throwable {
        FieldJoinPoint jp = (FieldJoinPoint)joinPoint;
        System.out.println("ENTER: " + jp.getTargetClass().getName() + "::" + jp.getFieldName());
    }

    /**
     * @After logSet
     * @After logGet
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
