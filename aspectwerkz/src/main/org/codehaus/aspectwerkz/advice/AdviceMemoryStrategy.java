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
package org.codehaus.aspectwerkz.advice;

import java.util.Map;
import java.util.WeakHashMap;

import gnu.trove.THashMap;

import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.MemoryType;

/**
 * Base class for the different memory strategies.
 *
 * @author <a href="mailto:jboner@acm.org">Jonas Bonér</a>
 * @version $Id: AdviceMemoryStrategy.java,v 1.1.1.1 2003-05-11 15:13:38 jboner Exp $
 */
public abstract class AdviceMemoryStrategy {

    /**
     * Holds a reference to the sole per JVM advice.
     */
    protected Object m_perJvm;

    /**
     * Holds references to the per class advices.
     */
    protected final Map m_perClass = new THashMap();

    /**
     * Holds references to the per instance advices.
     */
    protected final Map m_perInstance = new WeakHashMap();

    /**
     * Holds references to the per thread advices.
     */
    protected final Map m_perThread = new WeakHashMap();

    /**
     * The advice prototype.
     */
    protected final AbstractAdvice m_prototype;

    /**
     * Creates a new distribution strategy.
     *
     * @param prototype the advice prototype
     */
    public AdviceMemoryStrategy(final AbstractAdvice prototype) {
        if (prototype == null) throw new IllegalArgumentException("advice prototype can not be null");
        m_prototype = prototype;
    }

    /**
     * Returns the advice per JVM basis.
     *
     * @param joinPoint the joint point
     * @return the advice
     */
    public abstract Object getPerJvmAdvice(final JoinPoint joinPoint);

    /**
     * Returns the advice per class basis.
     *
     * @param joinPoint the joint point
     * @return the advice
     */
    public abstract Object getPerClassAdvice(final JoinPoint joinPoint);

    /**
     * Returns the advice per instance basis.
     *
     * @param joinPoint the joint point
     * @return the advice
     */
    public abstract Object getPerInstanceAdvice(final JoinPoint joinPoint);

    /**
     * Returns the advice for the current thread.
     *
     * @return the advice
     */
    public Object getPerThreadAdvice() {
        final Thread currentThread = Thread.currentThread();
        if (!m_perThread.containsKey(currentThread)) {
            synchronized (m_perThread) {
                try {
                    m_perThread.put(
                            currentThread,
                            AbstractAdvice.newInstance(m_prototype));
                }
                catch (Exception e) {
                    throw new WrappedRuntimeException(e);
                }
            }
        }
        return m_perThread.get(currentThread);
    }

    /**
     * Returns the memory type.
     *
     * @return the memory type
     */
    public abstract MemoryType getMemoryType();
}


