/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.aspect;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.Iterator;

import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.transform.TransformationUtil;
import org.codehaus.aspectwerkz.CrossCuttingInfo;
import org.codehaus.aspectwerkz.DeploymentModel;

/**
 * Implements the default aspect container strategy.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class DefaultAspectContainerStrategy implements AspectContainer {

    /**
     * Introduction container containing introduction declared by this aspect, keys by introduction names
     */
    private Map m_introductionContainers = new HashMap();

    /**
     * The prototype.
     */
    private CrossCuttingInfo m_prototype;

    /**
     * Holds a reference to the sole per JVM cross-cutting instance.
     */
    private Object m_perJvm;

    /**
     * Holds references to the per class cross-cutting instances.
     */
    private Map m_perClass = new WeakHashMap();

    /**
     * Holds references to the per instance cross-cutting instances.
     */
    private Map m_perInstance = new WeakHashMap();

    /**
     * Holds references to the per thread cross-cutting instances.
     */
    private Map m_perThread = new WeakHashMap();

    /**
     * The methods repository.
     */
    private Method[] m_adviceRepository = new Method[0];

    /**
     * Creates a new transient container strategy.
     *
     * @param prototype the advice prototype
     */
    public DefaultAspectContainerStrategy(final CrossCuttingInfo prototype) {
        if (prototype == null) {
            throw new IllegalArgumentException("aspect prototype can not be null");
        }
        m_prototype = prototype;
        createAdviceRepository();
    }

    /**
     * Invokes an introduced method with the index specified.
     *
     * @param methodIndex the method index
     * @param joinPoint   the join point
     * @return the result from the invocation
     */
    public Object invokeAdvice(final int methodIndex, final JoinPoint joinPoint) {
        Object result = null;
        switch (m_prototype.getDeploymentModel()) {

            case DeploymentModel.PER_JVM:
                result = invokeAdvicePerJvm(methodIndex, joinPoint);
                break;

            case DeploymentModel.PER_CLASS:
                result = invokeAdvicePerClass(methodIndex, joinPoint);
                break;

            case DeploymentModel.PER_INSTANCE:
                result = invokeAdvicePerInstance(methodIndex, joinPoint);
                break;

            case DeploymentModel.PER_THREAD:
                result = invokeAdvicePerThread(methodIndex, joinPoint);
                break;

            default:
                throw new RuntimeException("invalid deployment model: " + m_prototype.getDeploymentModel());
        }
        return result;
    }

    /**
     * Returns a specific advice by index.
     *
     * @param index the index
     * @return the advice
     */
    public Method getAdvice(final int index) {
        if (index < 0) {
            throw new IllegalArgumentException("advice index can not be less than 0");
        }
        return m_adviceRepository[index];
    }

    /**
     * Returns the cross-cutting info.
     *
     * @return the cross-cutting info
     */
    public CrossCuttingInfo getCrossCuttingInfo() {
        return m_prototype;
    }

    /**
     * Invokes the advice method on a per JVM basis.
     *
     * @param methodIndex the method index
     * @param joinPoint   the join point
     * @return the result from the method invocation
     */
    private Object invokeAdvicePerJvm(final int methodIndex, final JoinPoint joinPoint) {
        Object result = null;
        try {
            createPerJvmCrossCuttingInstance();
            Method method = m_adviceRepository[methodIndex];
            result = method.invoke(m_perJvm, new Object[]{joinPoint});
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
     * Invokes the advice method on a per class basis.
     *
     * @param methodIndex the method index
     * @param joinPoint   the join point
     * @return the result from the method invocation
     */
    private Object invokeAdvicePerClass(final int methodIndex, final JoinPoint joinPoint) {
        final Class targetClass = joinPoint.getTargetClass();
        Object result = null;
        try {
            createPerClassCrossCuttingInstance(targetClass);
            result = m_adviceRepository[methodIndex].invoke(m_perClass.get(targetClass), new Object[]{joinPoint});
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
     * Invokes the advice method on a per instance basis.
     *
     * @param methodIndex the method index
     * @param joinPoint   the join point
     * @return the result from the method invocation
     */
    private Object invokeAdvicePerInstance(final int methodIndex, final JoinPoint joinPoint) {
        Object result = null;
        Object targetInstance = joinPoint.getTargetInstance();

        if (targetInstance == null) { // can be null if f.e. an aspect has deployment model perInstance and has caller side pointcuts defined
            return invokeAdvicePerClass(methodIndex, joinPoint);
        }
        try {
            createPerInstanceCrossCuttingInstance(targetInstance);
            result = m_adviceRepository[methodIndex].invoke(m_perInstance.get(targetInstance), new Object[]{joinPoint});
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
     * Invokes the advice method on a per thread basis.
     *
     * @param methodIndex the method index
     * @param joinPoint   the join point
     * @return the result from the method invocation
     */
    private Object invokeAdvicePerThread(final int methodIndex, final JoinPoint joinPoint) {
        Object result;
        try {
            final Thread currentThread = Thread.currentThread();
            createPerThreadCrossCuttingInstance(currentThread);
            Method method = m_adviceRepository[methodIndex];
            result = method.invoke(m_perThread.get(currentThread), new Object[]{joinPoint});
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
     * Creates a new perJVM cross-cutting instance, if it already exists then return it.
     *
     * @return the cross-cutting instance
     */
    public Object createPerJvmCrossCuttingInstance() {
        if (m_perJvm == null) {
            try {
                m_perJvm = m_prototype.getAspectClass().newInstance();
            }
            catch (Exception e) {
                throw new WrappedRuntimeException(e);
            }
        }
        return m_perJvm;
    }

    /**
     * Creates a new perClass cross-cutting instance, if it already exists then return it.
     *
     * @param callingClass
     * @return the cross-cutting instance
     */
    public Object createPerClassCrossCuttingInstance(final Class callingClass) {
        if (!m_perClass.containsKey(callingClass)) {
            synchronized (m_perClass) {
                try {
                    m_perClass.put(callingClass, m_prototype.getAspectClass().newInstance());
                }
                catch (Exception e) {
                    throw new WrappedRuntimeException(e);
                }
            }
        }
        return m_perClass.get(callingClass);
    }

    /**
     * Creates a new perInstance cross-cutting instance, if it already exists then return it.
     *
     * @param callingInstance
     * @return the cross-cutting instance
     */
    public Object createPerInstanceCrossCuttingInstance(final Object callingInstance) {
        if (callingInstance == null) {
            return createPerClassCrossCuttingInstance(callingInstance.getClass());
        }
        if (!m_perInstance.containsKey(callingInstance)) {
            synchronized (m_perInstance) {
                try {
                    m_perInstance.put(callingInstance, m_prototype.getAspectClass().newInstance());
                }
                catch (Exception e) {
                    throw new WrappedRuntimeException(e);
                }
            }
        }
        return m_perInstance.get(callingInstance);
    }

    /**
     * Creates a new perThread cross-cutting instance, if it already exists then return it.
     *
     * @param thread the thread for the aspect
     * @return the cross-cutting instance
     */
    public Object createPerThreadCrossCuttingInstance(final Thread thread) {
        if (!m_perThread.containsKey(thread)) {
            synchronized (m_perThread) {
                try {
                    m_perThread.put(thread, m_prototype.getAspectClass().newInstance());
                }
                catch (Exception e) {
                    throw new WrappedRuntimeException(e);
                }
            }
        }
        return m_perThread.get(thread);
    }

    /**
     * Creates a repository for the advice methods.
     */
    private void createAdviceRepository() {
        synchronized (m_adviceRepository) {
            List methodList = TransformationUtil.createSortedMethodList(m_prototype.getAspectClass());
            m_adviceRepository = new Method[methodList.size()];
            for (int i = 0; i < m_adviceRepository.length; i++) {
                Method method = (Method)methodList.get(i);
                method.setAccessible(true);
                m_adviceRepository[i] = method;
            }
        }
    }

    /**
     * Attach the introduction container to this aspect container to mirror the "aspect contains 0-n introduction"
     *
     * @param name           of the introduction
     * @param introContainer introduction container
     */
    public void addIntroductionContainer(String name, IntroductionContainer introContainer) {
        m_introductionContainers.put(name, introContainer);
    }

    /**
     * Returns the introduction container of given name (introduction name) or null if not linked.
     *
     * @param name of the introduction
     * @return introduction container
     */
    public IntroductionContainer getIntroductionContainer(String name) {
        return (IntroductionContainer)m_introductionContainers.get(name);
    }
}
