/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package examples.logging;

import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.MemberSignature;
import org.codehaus.aspectwerkz.CrossCuttingInfo;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public abstract class AbstractLoggingAspect {

    private int m_level = 0;

    private CrossCuttingInfo m_info;

    public AbstractLoggingAspect(CrossCuttingInfo info) {
        m_info = info;
    }

    /**
     * @Around methodsToLog
     */
    public Object logMethod(JoinPoint joinPoint) throws Throwable {
        MemberSignature signature = (MemberSignature) joinPoint.getSignature();
        indent();
        System.out.println(
                "--> "
                + joinPoint.getTargetClass().getName()
                + "::"
                + signature.getName()
        );
        m_level++;
        final Object result = joinPoint.proceed();
        m_level--;
        indent();
        System.out.println(
                "<-- "
                + joinPoint.getTargetClass().getName()
                + "::"
                + signature.getName()
        );
        return result;
    }

    /**
     * @Before methodsToLog
     */
    public void logBefore(final JoinPoint joinPoint) throws Throwable {
        MemberSignature signature = (MemberSignature) joinPoint.getSignature();
        System.out.println(
                "BEFORE: "
                + joinPoint.getTargetClass().getName()
                + "::"
                + signature.getName()
        );
    }

    /**
     * @After returning(java.lang.String) methodsToLog
     */
    public void logAfterReturning(final JoinPoint joinPoint) throws Throwable {
        MemberSignature signature = (MemberSignature) joinPoint.getSignature();
        System.out.println(
                "AFTER RETURNING: "
                + joinPoint.getTargetClass().getName()
                + "::"
                + signature.getName()
        );
    }

    /**
     * @After throwing(java.lang.RuntimeException) methodsToLog
     */
    public void logAfterThrowing(final JoinPoint joinPoint) throws Throwable {
        MemberSignature signature = (MemberSignature) joinPoint.getSignature();
        System.out.println(
                "AFTER THROWING: "
                + joinPoint.getTargetClass().getName()
                + "::"
                + signature.getName()
        );
    }

    /**
     * @After finally methodsToLog
     */
    public void logAfterFinally(final JoinPoint joinPoint) throws Throwable {
        MemberSignature signature = (MemberSignature) joinPoint.getSignature();
        System.out.println(
                "AFTER FINALLY: "
                + joinPoint.getTargetClass().getName()
                + "::"
                + signature.getName()
        );
    }

    private void indent() {
        for (int i = 0; i < m_level; i++) {
            System.out.print("  ");
        }
    }
}