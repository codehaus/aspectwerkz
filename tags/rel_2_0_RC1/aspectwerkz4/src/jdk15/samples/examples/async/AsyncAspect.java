/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD-style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package examples.async;

import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.annotation.*;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

@org.codehaus.aspectwerkz.annotation.Aspect
        public class AsyncAspect {

    private Executor m_threadPool = Executors.newCachedThreadPool();

    @Around
            @Execution(Async.class)
            @Within(Service.class)
            public Object async(final JoinPoint jp) throws Throwable {
        m_threadPool.execute(
                new Runnable() {
                    public void run() {
                        try {
                            // proceed in a new thread
                            jp.proceed();
                        } catch (Throwable e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
        );
        return null;
    }


    @Retention(RetentionPolicy.RUNTIME)
            @Target(ElementType.METHOD)
            public static @interface Async {
        int timeout() default 0;
    }

    @Retention(RetentionPolicy.RUNTIME)
            @Target(ElementType.TYPE)
            public static @interface Service {
    }
}