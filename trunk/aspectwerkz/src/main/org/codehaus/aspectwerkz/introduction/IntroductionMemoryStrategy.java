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

import java.util.Map;
import java.util.WeakHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import gnu.trove.THashMap;

import org.codehaus.aspectwerkz.MethodComparator;
import org.codehaus.aspectwerkz.MemoryType;
import org.codehaus.aspectwerkz.transform.TransformationUtil;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;

/**
 * Base class for the different memory strategies.
 *
 * @author <a href="mailto:jboner@acm.org">Jonas Bonér</a>
 * @version $Id: IntroductionMemoryStrategy.java,v 1.1.1.1 2003-05-11 15:14:20 jboner Exp $
 */
public abstract class IntroductionMemoryStrategy {

    /**
     * Holds a reference to the sole per JVM advice.
     */
    protected Object m_perJvm;

    /**
     * Holds references to the per class advices.
     */
    protected Map m_perClass = new THashMap();

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
     * Creates a new distribution strategy.
     *
     * @param implClass the implementation class
     */
    public IntroductionMemoryStrategy(final Class implClass) {
        if (implClass == null) return; // we have an interface only introduction
        m_implClass = implClass;
        synchronized (m_methods) {
            final Method[] declaredMethods = m_implClass.getDeclaredMethods();

            // sort the list so that we can enshure that the indexes are in synch
            // see AddImplementationTransformer#addIntroductions
            final List toSort = new ArrayList();
            for (int i = 0; i < declaredMethods.length; i++) {
                // remove the getUuidString, ___hidden$getMetaData and setMetaData methods
                if (!declaredMethods[i].getName().equals(
                        TransformationUtil.GET_UUID_METHOD) &&
                        !declaredMethods[i].getName().equals(
                                TransformationUtil.GET_META_DATA_METHOD) &&
                        !declaredMethods[i].getName().equals(
                                TransformationUtil.SET_META_DATA_METHOD)) {
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
    public abstract Object invokePerJvm(final int methodIndex,
                                        final Object[] parameters);

    /**
     * Invokes the method on a per class basis.
     *
     * @param callingObject a reference to the calling object
     * @param callingObjectUuid the UUID for the calling object
     * @param methodIndex the method index
     * @param parameters the parameters for the invocation
     * @return the result from the method invocation
     */
    public abstract Object invokePerClass(final Object callingObject,
                                          final Object callingObjectUuid,
                                          final int methodIndex,
                                          final Object[] parameters);

    /**
     * Invokes the method on a per instance basis.
     *
     * @param callingObject a reference to the calling object
     * @param callingObjectUuid the UUID for the calling object
     * @param methodIndex the method index
     * @param parameters the parameters for the invocation
     * @return the result from the method invocation
     */
    public abstract Object invokePerInstance(final Object callingObject,
                                             final Object callingObjectUuid,
                                             final int methodIndex,
                                             final Object[] parameters);

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

    /**
     * Swaps the current introduction implementation.
     *
     * @param implClass the class of the new implementation to use
     */
    public abstract void swapImplementation(final Class implClass);

    /**
     * Returns the memory type.
     *
     * @return the memory type
     */
    public abstract MemoryType getMemoryType();
}


