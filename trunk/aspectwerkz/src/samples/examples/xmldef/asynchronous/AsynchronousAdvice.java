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
package examples.asynchronous;

import org.codehaus.aspectwerkz.advice.AroundAdvice;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
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
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: AsynchronousAdvice.java,v 1.2 2003-06-09 07:04:13 jboner Exp $
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
