/*
 * AspectWerkz - a dynamic, lightweight and high-performant AOP/AOSD framework for Java.
 * Copyright (C) 2002-2003  Jonas Bonér. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
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
 * @author <a href="mailto:jboner@acm.org">Jonas Bonér</a>
 * @version %I%, %G%
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

