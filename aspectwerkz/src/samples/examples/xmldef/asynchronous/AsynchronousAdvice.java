/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package examples.asynchronous;

import org.codehaus.aspectwerkz.xmldef.advice.AroundAdvice;
import org.codehaus.aspectwerkz.xmldef.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import examples.util.concurrent.AsynchronousManager;
import examples.util.definition.ThreadPoolDefinition;

/**
 * This advice makes it possible to achive asynchronous method invocations.<br/>
 *
 * All the methods that are picked out by the pointcuts mapped to this advice
 * are being executed in it's own thread.<br/>
 *
 * Uses a thread pool.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
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
                            joinPoint.proceedInNewThread(); // caution: needs to be proceedInNewThread() A not proceed()
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
