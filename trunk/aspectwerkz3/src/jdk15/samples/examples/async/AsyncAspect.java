/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD-style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package examples.async;

import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.Pointcut;
import org.codehaus.aspectwerkz.annotation.Expression;
import org.codehaus.aspectwerkz.annotation.Around;
import org.codehaus.aspectwerkz.annotation.Introduce;
import org.codehaus.aspectwerkz.annotation.Execution;
import org.codehaus.aspectwerkz.annotation.Within;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public class AsyncAspect {

    private Executor m_threadPool = Executors.newCachedThreadPool();

    @Expression("execution(void examples.async.Math.async*(..))")
//    @Expression("execution(@examples.async.Math.AsyncAspect$Async) && within(@examples.async.Math.AsyncAspect$Service)")
    Pointcut asyncMethods;

    @Around("asyncMethods")
    @Execution(Async.class)
    @Within(Service.class)
//    @Around
    public Object execute(final JoinPoint joinPoint) throws Throwable {
        m_threadPool.execute(
                new Runnable() {
                    public void run() {
                        try {
                            // proceed in a new thread
                            joinPoint.proceed();
                        } catch (Throwable e) {
                            throw new WrappedRuntimeException(e);
                        }
                    }
                }
        );
        return null;
    }

    @Introduce("withincode(void examples.async.Math.multiply(..))")
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public static @interface Async {
        int timeout() default 0;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public static @interface Service {
    }
















    /// syntax tests

// advice()..
//
//
// @Not(
//     @And(
//     )
// )
//
// @And(
//     @Not(
//
// )
//
// //@Expression(
//     @Execution(modifiers=Modifier.PUBLIC, annotations={Async.class   })
// //@Execution(Async.class)
//         void foo() {}
//
//
//        within=Service.class
//        )
//    XXX Named;
//
//    @Expression(
//        AND={Named.class, ...}
//
//    Pointcut annotatedMethods;

}