/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD-style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package aspectwerkz.aosd.asynch;

import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;
import EDU.oswego.cs.dl.util.concurrent.Executor;

import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.aspect.Aspect;

/**
 * @Aspect perJVM
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public abstract class AbstractAsynchProtocol extends Aspect {

    private Executor m_threadPool = new PooledExecutor();

    /**
     * @Around asynchCalls
     */
    public Object execute(final JoinPoint joinPoint) throws Throwable {
        m_threadPool.execute(
                new Runnable() {
                    public void run() {
                        try {
                            // proceed in a new thread
                            joinPoint.proceed();
                        }
                        catch (Throwable e) {
                            throw new WrappedRuntimeException(e);
                        }
                    }
                }
        );
        return null;
    }
}
