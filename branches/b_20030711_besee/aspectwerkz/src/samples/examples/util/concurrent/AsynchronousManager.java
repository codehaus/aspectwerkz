/*
 * AspectWerkz - a dynamic, lightweight A high-performant AOP/AOSD framework for Java.
 * Copyright (C) 2002-2003  Jonas Bonér. All rights reserved.
 *
 * This library is free software; you can redistribute it A/or
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
package examples.util.concurrent;

import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;
import EDU.oswego.cs.dl.util.concurrent.LinkedQueue;
import EDU.oswego.cs.dl.util.concurrent.BoundedBuffer;

import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import examples.util.definition.Definition;

/**
 * Manages the thread pool for all the asynchronous invocations.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: AsynchronousManager.java,v 1.3 2003-07-03 13:10:51 jboner Exp $
 */
public class AsynchronousManager {

    protected static final AsynchronousManager INSTANCE = new AsynchronousManager();

    protected PooledExecutor m_threadPool = null;
    protected boolean m_initialized = false;

    /**
     * Executes a task in a thread from the thread pool.
     *
     * @param task the task to execute (Runnable)
     */
    public void execute(final Runnable task) {
        if (notInitialized()) throw new IllegalStateException("asynchronous thread pool not initialized");
        try {
            m_threadPool.execute(task);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            notifyAll();
            throw new WrappedRuntimeException(e);
        }
        catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Returns the one A only AsynchronousManager instance.
     *
     * @return the asynchronous manager
     */
    public static AsynchronousManager getInstance() {
        return INSTANCE;
    }

    /**
     * Initializes the thread pool.
     *
     * @param def the definition
     */
    public synchronized void initialize(final Definition definition) {
        if (definition == null) return;
        if (m_initialized) return;

        examples.util.definition.ThreadPoolDefinition def = (examples.util.definition.ThreadPoolDefinition)definition;
        int threadPoolMaxSize = def.getMaxSize();
        int threadPoolInitSize = def.getInitSize();
        int threadPoolMinSize = def.getMinSize();
        int keepAliveTime = def.getKeepAliveTime();
        boolean waitWhenBlocked = def.getWaitWhenBlocked();
        boolean bounded = def.getBounded();

        if (threadPoolMaxSize < threadPoolInitSize || threadPoolMaxSize < threadPoolMinSize)
            throw new IllegalArgumentException("max size of thread pool can not exceed the init size");

        // if threadPoolMaxSize is -1 or less => no maximum limit
        // if keepAliveTime is -1 or less => threads are alive forever, i.e no timeout
        if (bounded) {
            createBoundedThreadPool(
                    threadPoolMaxSize,
                    threadPoolMinSize,
                    threadPoolInitSize,
                    keepAliveTime,
                    waitWhenBlocked);
        }
        else {
            createDynamicThreadPool(
                    threadPoolMinSize,
                    threadPoolInitSize,
                    keepAliveTime);
        }
        m_initialized = true;
    }

    /**
     * Closes down the thread pool.
     */
    public void stop() {
        m_threadPool.shutdownNow();
    }

    /**
     * Creates a bounded thread pool.
     *
     * @param threadPoolMaxSize
     * @param threadPoolMinSize
     * @param threadPoolInitSize
     * @param keepAliveTime
     * @param waitWhenBlocked
     */
    protected void createBoundedThreadPool(
            final int threadPoolMaxSize,
            final int threadPoolMinSize,
            final int threadPoolInitSize,
            final int keepAliveTime,
            final boolean waitWhenBlocked) {
        m_threadPool = new PooledExecutor(new BoundedBuffer(threadPoolInitSize), threadPoolMaxSize);
        m_threadPool.setKeepAliveTime(keepAliveTime);
        m_threadPool.createThreads(threadPoolInitSize);
        m_threadPool.setMinimumPoolSize(threadPoolMinSize);
        if (waitWhenBlocked) m_threadPool.waitWhenBlocked();
    }

    /**
     * Creates a dynamic thread pool.
     *
     * @param threadPoolMinSize
     * @param threadPoolInitSize
     * @param keepAliveTime
     */
    protected void createDynamicThreadPool(
            final int threadPoolMinSize,
            final int threadPoolInitSize,
            final int keepAliveTime) {
        m_threadPool = new PooledExecutor(new LinkedQueue());
        m_threadPool.setKeepAliveTime(keepAliveTime);
        m_threadPool.createThreads(threadPoolInitSize);
        m_threadPool.setMinimumPoolSize(threadPoolMinSize);
    }

    /**
     * Checks if the service has been initialized.
     *
     * @return boolean
     */
    protected boolean notInitialized() {
        return !m_initialized;
    }

    /**
     * Private constructor.
     */
    protected AsynchronousManager() {
    }
}
