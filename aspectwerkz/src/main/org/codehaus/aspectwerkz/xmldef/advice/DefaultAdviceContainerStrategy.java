/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.xmldef.advice;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.HashMap;

import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.ContainerType;

/**
 * Implements the default advice container strategy.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class DefaultAdviceContainerStrategy implements AdviceContainer {

    /**
     * Holds a reference to the sole per JVM advice.
     */
    protected Object m_perJvm;

    /**
     * Holds references to the per class advices.
     */
    protected final Map m_perClass = new HashMap();

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
     * Creates a new transient container strategy.
     *
     * @param prototype the advice prototype
     */
    public DefaultAdviceContainerStrategy(final AbstractAdvice prototype) {
        if (prototype == null) throw new IllegalArgumentException("advice prototype can not be null");
        m_prototype = prototype;
    }

    /**
     * Returns the sole per JVM advice.
     *
     * @param joinPoint the joint point
     * @return the advice
     */
    public Object getPerJvmAdvice(final JoinPoint joinPoint) {
        if (m_perJvm == null) {
            try {
                m_perJvm = AbstractAdvice.newInstance(m_prototype);
            }
            catch (Exception e) {
                throw new WrappedRuntimeException(e);
            }
        }
        return m_perJvm;
    }

    /**
     * Returns the advice for the current class.
     *
     * @param joinPoint the joint point
     * @return the advice
     */
    public Object getPerClassAdvice(final JoinPoint joinPoint) {
        final Class callingClass = joinPoint.getTargetClass();
        if (!m_perClass.containsKey(callingClass)) {
            synchronized (m_perClass) {
                try {
                    m_perClass.put(callingClass, AbstractAdvice.newInstance(m_prototype));
                }
                catch (Exception e) {
                    throw new WrappedRuntimeException(e);
                }
            }
        }
        return m_perClass.get(callingClass);
    }

    /**
     * Returns the advice for the current instance.
     *
     * @param joinPoint the joint point
     * @return the advice
     */
    public Object getPerInstanceAdvice(final JoinPoint joinPoint) {
        final Object callingInstance = joinPoint.getTargetInstance();
        if (callingInstance == null) {
            return getPerClassAdvice(joinPoint);
        }
        if (!m_perInstance.containsKey(callingInstance)) {
            synchronized (m_perInstance) {
                try {
                    m_perInstance.put(callingInstance, AbstractAdvice.newInstance(m_prototype));
                }
                catch (Exception e) {
                    throw new WrappedRuntimeException(e);
                }
            }
        }
        return m_perInstance.get(callingInstance);
    }

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
                    m_perThread.put(currentThread, AbstractAdvice.newInstance(m_prototype));
                }
                catch (Exception e) {
                    throw new WrappedRuntimeException(e);
                }
            }
        }
        return m_perThread.get(currentThread);
    }

    /**
     * Returns the container type.
     *
     * @return the container type
     */
    public ContainerType getContainerType() {
        return ContainerType.TRANSIENT;
    }
}
