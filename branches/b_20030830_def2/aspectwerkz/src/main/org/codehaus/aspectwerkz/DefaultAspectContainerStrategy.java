/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.WeakHashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import org.codehaus.aspectwerkz.ContainerType;
import org.codehaus.aspectwerkz.MethodComparator;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.advice.AbstractAdvice;
import org.codehaus.aspectwerkz.transform.TransformationUtil;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;

/**
 * Implements the default aspect container strategy.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class DefaultAspectContainerStrategy implements AspectContainer {

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
     * The aspect prototype.
     */
    protected final AbstractAspect m_prototype;

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
     * @param prototype the advice prototype
     */
    public DefaultAspectContainerStrategy(final AbstractAspect prototype) {
        if (prototype == null) throw new IllegalArgumentException("aspect prototype can not be null");

        m_prototype = prototype;
        m_implClass = m_prototype.getClass();

        createIntroductionMethodRepository();
    }

    /**
     * Returns the sole per JVM advice.
     *
     * @TODO: return the correct method instead of the whole aspect!!!!
     * @TODO: perhaps have an invoke methods as with the introductions, and pass the index etc. BETTER!!!
     *
     * @param joinPoint the joint point
     * @return the advice
     */
    public Object getPerJvmAdvice(final JoinPoint joinPoint) {
        if (m_perJvm == null) {
            try {
                m_perJvm = AbstractAspect.newInstance(m_prototype);
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
                    m_perClass.put(callingClass, AbstractAspect.newInstance(m_prototype));
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
        if (callingInstance == null) {
            return getPerClassAdvice(joinPoint);
        }
        if (!m_perInstance.containsKey(callingInstance)) {
            synchronized (m_perInstance) {
                try {
                    m_perInstance.put(callingInstance, AbstractAspect.newInstance(m_prototype));
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
                    m_perThread.put(currentThread, AbstractAspect.newInstance(m_prototype));
                }
                catch (Exception e) {
                    throw new WrappedRuntimeException(e);
                }
            }
        }
        return m_perThread.get(currentThread);
    }

    /**
     * Invokes the method on a per JVM basis.
     *
     * @param methodIndex the method index
     * @param parameters the parameters for the invocation
     * @return the result from the method invocation
     */
    public Object invokeIntroductionPerJvm(final int methodIndex,
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
    public Object invokeIntroductionPerClass(final Object callingObject,
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
    public Object invokeIntroductionPerInstance(final Object callingObject,
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
    public Object invokeIntroductionPerThread(final int methodIndex,
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
     * @TODO: how to handle the SWAP of impl.?????
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

    /**
     * Creates an repository with the introduced methods.
     *
     * @TODO: makes a repo out of ALL methods not just the introduction methods!!!!!!!!!!
     */
    private void createIntroductionMethodRepository() {
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
}
