/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package samples.tracing;

import org.codehaus.aspectwerkz.definition.Pointcut;
import org.codehaus.aspectwerkz.joinpoint.StaticJoinPoint;
import org.codehaus.aspectwerkz.joinpoint.MemberSignature;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r </a>
 */
public class TracingAspect {

    private int m_level = 0;

    public Object logMethod(StaticJoinPoint joinPoint) throws Throwable {
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

    public void logBefore(final StaticJoinPoint joinPoint) throws Throwable {
        MemberSignature signature = (MemberSignature) joinPoint.getSignature();
        System.out.println(
                "BEFORE: "
                + joinPoint.getTargetClass().getName()
                + "::"
                + signature.getName()
        );
    }

    public void logAfterReturning(final StaticJoinPoint joinPoint) throws Throwable {
        MemberSignature signature = (MemberSignature) joinPoint.getSignature();
        System.out.println(
                "AFTER RETURNING: "
                + joinPoint.getTargetClass().getName()
                + "::"
                + signature.getName()
        );
    }

    public void logAfterThrowingRE(final StaticJoinPoint joinPoint) throws Throwable {
        MemberSignature signature = (MemberSignature) joinPoint.getSignature();
        System.out.println(
                "AFTER THROWING RE: "
                + joinPoint.getTargetClass().getName()
                + "::"
                + signature.getName()
        );
    }

    public void logAfterThrowingIAE(final StaticJoinPoint joinPoint) throws Throwable {
        MemberSignature signature = (MemberSignature) joinPoint.getSignature();
        System.out.println(
                "AFTER THROWING IAE: "
                + joinPoint.getTargetClass().getName()
                + "::"
                + signature.getName()
        );
    }

    public void logAfter(final StaticJoinPoint joinPoint) throws Throwable {
        MemberSignature signature = (MemberSignature) joinPoint.getSignature();
        System.out.println(
                "AFTER: "
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

    /**
     * @Expression execution(* examples.logging.Target.toLog*(..))
     */
    Pointcut methodsToLog() {
        return null;
    };
}