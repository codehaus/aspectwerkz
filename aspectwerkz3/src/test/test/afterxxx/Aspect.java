/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.afterxxx;

import org.codehaus.aspectwerkz.joinpoint.StaticJoinPoint;
import org.codehaus.aspectwerkz.Pointcut;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r </a>
 */
public class Aspect {

    /**
     * @Expression execution(* test.afterxxx.Test.all(..))
     */
    Pointcut all;

    /**
     * @Expression execution(* test.afterxxx.Test.aroundFinally(..))
     */
    Pointcut aroundFinally;

    /**
     * @Expression execution(* test.afterxxx.Test.aroundReturning(..))
     */
    Pointcut aroundReturning;

    /**
     * @Expression execution(* test.afterxxx.Test.aroundFinallyReturning(..))
     */
    Pointcut aroundFinallyReturning;

    /**
     * @Expression execution(* test.afterxxx.Test.aroundFinallyReturningThrowing(..))
     */
    Pointcut aroundFinallyReturningThrowing;

    /**
     * @Expression execution(* test.afterxxx.Test.aroundReturningThrowing(..))
     */
    Pointcut aroundReturningThrowing;

    /**
     * @Expression execution(* test.afterxxx.Test._finally(..))
     */
    Pointcut _finally;

    /**
     * @Expression execution(* test.afterxxx.Test.finallyReturning(..))
     */
    Pointcut finallyReturning;

    /**
     * @Expression execution(* test.afterxxx.Test.finallyReturningThrowing(..))
     */
    Pointcut finallyReturningThrowing;

    /**
     * @Expression execution(* test.afterxxx.Test.returning(..))
     */
    Pointcut returning;

    /**
     * @Expression execution(* test.afterxxx.Test.returningThrowing(..))
     */
    Pointcut returningThrowing;

    /**
     * @Around all || aroundFinally || aroundFinallyReturning ||
     *         aroundFinallyReturningThrowing || aroundReturningThrowing || aroundReturning
     */
    public Object logAround(StaticJoinPoint joinPoint) throws Throwable {
        Test.log("logAround ");
        final Object result = joinPoint.proceed();
        return result;
    }

    /**
     * @After returning(java.lang.String) aroundFinallyReturning || aroundFinallyReturningThrowing ||
     *                                    aroundReturningThrowing || finallyReturning || finallyReturningThrowing ||
     *                                    returningThrowing || aroundReturning || returning
     */
    public void logAfterReturning(final StaticJoinPoint joinPoint) throws Throwable {
        Test.log("logAfterReturning ");
    }

    /**
     * @After throwing(java.lang.RuntimeException) aroundFinallyReturningThrowing || aroundReturningThrowing ||
     *                                             finallyReturningThrowing || returningThrowing
     */
    public void logAfterThrowing(final StaticJoinPoint joinPoint) throws Throwable {
        Test.log("logAfterThrowing ");
    }

    /**
     * @After finally aroundFinally || aroundFinallyReturning || aroundFinallyReturningThrowing ||
     *                _finally || finallyReturning || finallyReturningThrowing
     */
    public void logAfterFinally(final StaticJoinPoint joinPoint) throws Throwable {
        Test.log("logAfterFinally ");
    }
}