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
package org.codehaus.aspectwerkz.introduction;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.WeakHashMap;

import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.MemoryType;

/**
 * Implements a transient version of the introduction memory strategy.
 *
 * @author <a href="mailto:jboner@acm.org">Jonas Bonér</a>
 * @version $Id: TransientIntroductionMemoryStrategy.java,v 1.1.1.1 2003-05-11 15:14:23 jboner Exp $
 */
public class TransientIntroductionMemoryStrategy
        extends IntroductionMemoryStrategy {

    /**
     * Creates a new transient distribution strategy.
     *
     * @param implClass the implementation class
     */
    public TransientIntroductionMemoryStrategy(final Class implClass) {
        super(implClass);
    }

    /**
     * Invokes the method on a per JVM basis.
     *
     * @param methodIndex the method index
     * @param parameters the parameters for the invocation
     * @return the result from the method invocation
     */
    public Object invokePerJvm(final int methodIndex,
                               final Object[] parameters) {
        Object result = null;
        try {
            if (m_perJvm == null) {
                m_perJvm = m_implClass.newInstance();
            }
            result = m_methods[methodIndex].invoke(m_perJvm, parameters);
        }
        catch (InvocationTargetException e) {
            throw new WrappedRuntimeException(e.getCause());
        }
        catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
        return result;
    }

    /**
     * Invokes the method on a per class basis.
     *
     * @param callingObject a reference to the calling object
     * @param callingObjectUuid the UUID for the calling object
     * @param methodIndex the method index
     * @param parameters the parameters for the invocation
     * @return the result from the method invocation
     */
    public Object invokePerClass(final Object callingObject,
                                 final Object callingObjectUuid,
                                 final int methodIndex,
                                 final Object[] parameters) {
        final Class callingClass = callingObject.getClass();
        Object result = null;
        try {
            if (!m_perClass.containsKey(callingClass)) {
                synchronized (m_perClass) {
                    m_perClass.put(callingClass, m_implClass.newInstance());
                }
            }
            result = m_methods[methodIndex].
                    invoke(m_perClass.get(callingClass), parameters);
        }
        catch (InvocationTargetException e) {
            throw new WrappedRuntimeException(e.getCause());
        }
        catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
        return result;
    }

    /**
     * Invokes the method on a per instance basis.
     *
     * @param callingObject a reference to the calling object
     * @param callingObjectUuid the UUID for the calling object
     * @param methodIndex the method index
     * @param parameters the parameters for the invocation
     * @return the result from the method invocation
     */
    public Object invokePerInstance(final Object callingObject,
                                    final Object callingObjectUuid,
                                    final int methodIndex,
                                    final Object[] parameters) {
        Object result = null;
        try {
            if (!m_perInstance.containsKey(callingObject)) {
                synchronized (m_perInstance) {
                    m_perInstance.put(callingObject, m_implClass.newInstance());
                }
            }
            result = m_methods[methodIndex].
                    invoke(m_perInstance.get(callingObject), parameters);
        }
        catch (InvocationTargetException e) {
            throw new WrappedRuntimeException(e.getCause());
        }
        catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
        return result;
    }

    /**
     * Swaps the current introduction implementation.
     *
     * @param implClass the class of the new implementation to use
     */
    public void swapImplementation(final Class implClass) {
        if (implClass == null) throw new IllegalArgumentException("implementation class can not be null");
        synchronized (this) {
            try {
                m_implClass = implClass;
                m_methods = m_implClass.getDeclaredMethods();

                m_perJvm = null;
                m_perClass = new HashMap(m_perClass.size());
                m_perInstance = new WeakHashMap(m_perClass.size());
                m_perThread = new WeakHashMap(m_perClass.size());
            }
            catch (Exception e) {
                new WrappedRuntimeException(e);
            }
        }
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
