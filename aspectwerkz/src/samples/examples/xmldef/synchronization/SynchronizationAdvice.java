/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package examples.synchronization;

import EDU.oswego.cs.dl.util.concurrent.Mutex;

import org.codehaus.aspectwerkz.advice.AroundAdvice;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

/**
 * This advice implements method synchronization.<br/>
 *
 * It synchronizes access to the methods that are picked out by the
 * pointcuts mapped to this advice.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class SynchronizationAdvice extends AroundAdvice {

    private Mutex m_mutex = new Mutex();
    // if a counting semaphore is needed use:
    // private Semaphore m_mutex = new Semaphore(nrAllowed);

    public SynchronizationAdvice() {
        super();
    }

    public Object execute(final JoinPoint joinPoint) throws Throwable {
        System.out.println("Acquiring mutex\t" + Thread.currentThread());
        m_mutex.acquire();
        System.out.println("Has mutex\t\t" + Thread.currentThread());
        Object result = joinPoint.proceed();
        m_mutex.release();
        System.out.println("Releasing mutex\t" + Thread.currentThread());
        return result;
    }
}

