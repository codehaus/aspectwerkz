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

import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.MemoryType;

/**
 * Implements a transient version of the advice memory strategy.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: TransientAdviceMemoryStrategy.java,v 1.2 2003-06-09 07:04:13 jboner Exp $
 */
public class TransientAdviceMemoryStrategy extends AdviceMemoryStrategy {

    /**
     * Creates a new transient distribution strategy.
     *
     * @param prototype the advice prototype
     */
    public TransientAdviceMemoryStrategy(AbstractAdvice prototype) {
        super(prototype);
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
                    m_perClass.put(
                            callingClass,
                            AbstractAdvice.newInstance(m_prototype));
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
        final Object callingInstance = joinPoint.getTargetObject();
        if (callingInstance == null) throw new RuntimeException("advice applied to static context");
        if (!m_perInstance.containsKey(callingInstance)) {
            synchronized (m_perInstance) {
                try {
                    m_perInstance.put(
                            callingInstance,
                            AbstractAdvice.newInstance(m_prototype));
                }
                catch (Exception e) {
                    throw new WrappedRuntimeException(e);
                }
            }
        }
        return m_perInstance.get(callingInstance);
    }

    /**
     * Returns the memory type.
     *
     * @return the memory type
     */
    public MemoryType getMemoryType() {
        return MemoryType.TRANSIENT;
    }
}
