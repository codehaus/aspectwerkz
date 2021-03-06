/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.xmldef.introduction;

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
 */
public class DefaultIntroductionContainerStrategy implements IntroductionContainer {

    /**
     * Holds a reference to the sole per JVM introduction.
     */
    protected Object m_perJvm;

    /**
     * Holds references to the per class introductions.
     */
    protected Map m_perClass = new HashMap();

    /**
     * Holds references to the per instance introductions.
     */
    protected Map m_perInstance = new WeakHashMap();

    /**
     * Holds references to the per thread introductions.
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
     * Creates a new default introduction container.
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

                // remove the ___AW_getUuid, ___AW_getMetaData, ___AW_addMetaData and class$ methods
                // as well as the added proxy methods before sorting the method list
                if (!declaredMethods[i].getName().startsWith(
                        TransformationUtil.CLASS_LOOKUP_METHOD) &&
                        !declaredMethods[i].getName().startsWith(
                                TransformationUtil.GET_UUID_METHOD) &&
                        !declaredMethods[i].getName().startsWith(
                                TransformationUtil.GET_META_DATA_METHOD) &&
                        !declaredMethods[i].getName().startsWith(
                                TransformationUtil.SET_META_DATA_METHOD) &&
                        !declaredMethods[i].getName().startsWith(
                                TransformationUtil.ORIGINAL_METHOD_PREFIX)) {
                    toSort.add(declaredMethods[i]);
                }
            }
            Collections.sort(toSort, MethodComparator.getInstance(MethodComparator.NORMAL_METHOD));

            m_methods = new Method[toSort.size()];
            for (int i = 0; i < m_methods.length; i++) {
                Method method = (Method)toSort.get(i);
                method.setAccessible(true);
                m_methods[i] = method;
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
            throw new WrappedRuntimeException(e.getTargetException());
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
            result = m_methods[methodIndex].invoke(m_perClass.get(callingClass), parameters);
        }
        catch (InvocationTargetException e) {
            throw new WrappedRuntimeException(e.getTargetException());
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
            result = m_methods[methodIndex].invoke(m_perInstance.get(callingObject), parameters);
        }
        catch (InvocationTargetException e) {
            throw new WrappedRuntimeException(e.getTargetException());
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
                    m_perThread.put(currentThread, m_implClass.newInstance());
                }
            }
            result = m_methods[methodIndex].invoke(m_perThread.get(currentThread), parameters);
        }
        catch (InvocationTargetException e) {
            throw new WrappedRuntimeException(e.getTargetException());
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

                for (int i = 0; i < m_methods.length; i++) {
                    m_methods[i].setAccessible(true);
                }

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
