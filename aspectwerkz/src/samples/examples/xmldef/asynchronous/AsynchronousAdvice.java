/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package examples.xmldef.asynchronous;

import org.codehaus.aspectwerkz.xmldef.advice.AroundAdvice;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;

import examples.util.concurrent.AsynchronousManager;
import examples.util.definition.ThreadPoolDefinition;

/**
 * This advice makes it possible to achive asynchronous method invocations.
 * <p/>
 * All the methods that are picked out by the pointcuts mapped to this advice
 * are being executed in it's own thread.
 * <p/>
 * Uses a thread pool.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class AsynchronousAdvice extends AroundAdvice {

    public AsynchronousAdvice() {
        super();
    }

    public Object execute(final JoinPoint joinPoint) throws Throwable {
        AsynchronousManager.getInstance().execute(
                new Runnable() {
                    public void run() {
                        try {
                            // invoke the intercepted method
                            joinPoint.proceedInNewThread(); // caution: needs to be proceedInNewThread() and not proceed()
                        }
                        catch (Throwable e) {
                            throw new WrappedRuntimeException(e);
                        }
                    }
                }
        );
        return null;
    }

    static {
        // initialize the thread pool
        ThreadPoolDefinition def = new ThreadPoolDefinition();
        def.setBounded(true);
        def.setMaxSize(10);
        def.setMinSize(5);
        def.setInitSize(5);
        def.setKeepAliveTime(6000);
        def.setWaitWhenBlocked(true);
        AsynchronousManager.getInstance().initialize(def);
    }
}
