/*
 * AspectWerkz - a dynamic, lightweight A high-performant AOP/AOSD framework for Java.
 * Copyright (C) 2002-2003  Jonas Bon�r. All rights reserved.
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
package org.codehaus.aspectwerkz.introduction;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.WeakHashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.ContainerType;
import org.codehaus.aspectwerkz.MethodComparator;
import org.codehaus.aspectwerkz.transform.TransformationUtil;

/**
 * Implements the default introduction container strategy.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 * @version $Id: DefaultIntroductionContainerStrategy.java,v 1.3 2003-07-03 13:10:49 jboner Exp $
 */
public class DefaultIntroductionContainerStrategy implements IntroductionContainer {

    /**
     * Holds a reference to the sole per JVM advice.
     */
    protected Object m_perJvm;

    /**
     * Holds references to the per class advices.
     */
    protected Map m_perClass = new HashMap();

    /**
     * Holds references to the per instance advices.
     */
    protected Map m_perInstance = new WeakHashMap();

    /**
     * Holds references to the per thread advices.
     */
    protected Map m_perThread = new WeakHashMap();

    /**
     * Holds the <code>Class</code> for the implementation.
     */
    protected Class m_implClass;

    /**
     * Stores the methods for the introduction.
     */
    protected Method[] m_methods = new Method[0];

    /**
     * Creates a new transient container strategy.
     *
     * @param implClass the implementation class
     */
    public DefaultIntroductionContainerStrategy(final Class implClass) {
        if (implClass == null) return; // we have an interface only introduction

        m_implClass = implClass;
        synchronized (m_methods) {
            Method[] declaredMethods = m_implClass.getDeclaredMethods();

            // sort the list so that we can enshure that the indexes are in synch
            // see AddImplementationTransformer#addIntroductions
            List toSort = new ArrayList();
            for (int i = 0; i < declaredMethods.length; i++) {

                // remove the getUuid, ___hidden$getMetaData, ___hidden$addMetaData methods
                // A the added proxy methods before sorting the method list
                if (!declaredMethods[i].getName().equals(
                        TransformationUtil.GET_UUID_METHOD) &&
                        !declaredMethods[i].getName().equals(
                                TransformationUtil.GET_META_DATA_METHOD) &&
                        !declaredMethods[i].getName().equals(
                                TransformationUtil.SET_META_DATA_METHOD) &&
                        !declaredMethods[i].getName().startsWith(
                                TransformationUtil.ORIGINAL_METHOD_PREFIX) ) {
                    toSort.add(declaredMethods[i]);
                }
            }
            Collections.sort(toSort, MethodComparator.
                    getInstance(MethodComparator.NORMAL_METHOD));

            m_methods = new Method[toSort.size()];
            for (int i = 0; i < m_methods.length; i++) {
                m_methods[i] = (Method)toSort.get(i);
            }
        }
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
     * @param methodIndex the method index
     * @param parameters the parameters for the invocation
     * @return the result from the method invocation
     */
    public Object invokePerClass(final Object callingObject,
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
     * @param methodIndex the method index
     * @param parameters the parameters for the invocation
     * @return the result from the method invocation
     */
    public Object invokePerInstance(final Object callingObject,
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
     * Invokes the method on a per thread basis.
     *
     * @param methodIndex the method index
     * @param parameters the parameters for the invocation
     * @return the result from the method invocation
     */
    public Object invokePerThread(final int methodIndex,
                                  final Object[] parameters) {
        Object result;
        try {
            final Thread currentThread = Thread.currentThread();
            if (!m_perThread.containsKey(currentThread)) {
                synchronized (m_perThread) {
                    m_perThread.put(
                            currentThread,
                            m_implClass.newInstance());
                }
            }
            result = m_methods[methodIndex].invoke(
                    m_perThread.get(currentThread), parameters);
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
     * Returns the container type.
     *
     * @return the container type
     */
    public ContainerType getContainerType() {
        return ContainerType.TRANSIENT;
    }

    /**
     * Returns a specific method by the method index.
     *
     * @param index the method index
     * @return the method
     */
    public Method getMethod(final int index) {
        if (index < 0) throw new IllegalArgumentException("method index can not be less than 0");
        return m_methods[index];
    }

    /**
     * Returns all the methods for this introduction.
     *
     * @return the methods
     */
    public Method[] getMethods() {
        return m_methods;
    }
}
