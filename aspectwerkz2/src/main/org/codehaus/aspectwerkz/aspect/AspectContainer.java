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

import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.transform.TransformationUtil;
import org.codehaus.aspectwerkz.CrossCutting;
import org.codehaus.aspectwerkz.CrossCuttingInfo;

/**
 * Implements the default aspect container strategy.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class AspectContainer {

    /**
     * Introduction container containing introduction declared by this aspect, keys by introduction names
     */
    private Map m_introductionContainers = new HashMap();

    /**
     * Holds a reference to the sole per JVM aspect.
     */
    protected CrossCutting m_perJvm;

    /**
     * Holds references to the per class introductions.
     */
    protected Map m_perClass = new WeakHashMap();

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
    protected CrossCuttingInfo m_prototype;

    /**
     * The methods repository.
     */
    protected Method[] m_adviceRepository = new Method[0];

    /**
     * Creates a new transient container strategy.
     *
     * @param prototype the advice prototype
     */
    public AspectContainer(final CrossCuttingInfo prototype) {
        if (prototype == null) {
            throw new IllegalArgumentException("aspect prototype can not be null");
        }
        m_prototype = prototype;
        createAdviceRepository();
    }

    /**
     * Invokes the advice method on a per JVM basis.
     *
     * @param methodIndex the method index
     * @param joinPoint   the join point
     * @return the result from the method invocation
     */
    public Object invokeAdvicePerJvm(final int methodIndex, final JoinPoint joinPoint) {
        Object result = null;
        try {
            if (m_perJvm == null) {
                m_perJvm = (CrossCutting)m_prototype.getAspectClass().newInstance();
                m_perJvm.setCrossCuttingInfo(CrossCuttingInfo.newInstance(m_prototype));
            }
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
    public Object invokeAdvicePerClass(final int methodIndex, final JoinPoint joinPoint) {
        final Class targetClass = joinPoint.getTargetClass();
        Object result = null;
        try {
            if (!m_perClass.containsKey(targetClass)) {
                synchronized (m_perClass) {
                    CrossCutting aspect = (CrossCutting)m_prototype.getAspectClass().newInstance();
                    CrossCuttingInfo info = CrossCuttingInfo.newInstance(m_prototype);
                    info.setTargetClass(targetClass);
                    aspect.setCrossCuttingInfo(info);
                    m_perClass.put(targetClass, aspect);
                }
            }
            result = m_adviceRepository[methodIndex].invoke(
                    m_perClass.get(targetClass),
                    new Object[]{joinPoint}
            );
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
    public Object invokeAdvicePerInstance(final int methodIndex, final JoinPoint joinPoint) {
        Object result = null;
        Object targetInstance = joinPoint.getTargetInstance();

        if (targetInstance == null) { // can be null if f.e. an aspect has deployment model perInstance and has caller side pointcuts defined
            return invokeAdvicePerClass(methodIndex, joinPoint);
        }

        try {
            if (!m_perInstance.containsKey(targetInstance)) {
                synchronized (m_perInstance) {
                    CrossCutting aspect = (CrossCutting)m_prototype.getAspectClass().newInstance();
                    CrossCuttingInfo info = CrossCuttingInfo.newInstance(m_prototype);
                    info.setTargetInstance(targetInstance);
                    aspect.setCrossCuttingInfo(info);
                    m_perInstance.put(targetInstance, aspect);
                }
            }
            result = m_adviceRepository[methodIndex].invoke(
                    m_perInstance.get(targetInstance),
                    new Object[]{joinPoint}
            );
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
    public Object invokeAdvicePerThread(final int methodIndex, final JoinPoint joinPoint) {
        Object result;
        try {
            final Thread currentThread = Thread.currentThread();
            if (!m_perThread.containsKey(currentThread)) {
                synchronized (m_perThread) {
                    CrossCutting aspect = (CrossCutting)m_prototype.getAspectClass().newInstance();
                    aspect.setCrossCuttingInfo(CrossCuttingInfo.newInstance(m_prototype));
                    m_perThread.put(currentThread, aspect);
                }
            }
            Method method = m_adviceRepository[methodIndex];
            result = method.invoke(
                    m_perThread.get(currentThread),
                    new Object[]{joinPoint}
            );
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
     * Returns all advice.
     *
     * @return the method
     */
    public Method[] getAdvice() {
        return m_adviceRepository;
    }

    /**
     * Returns the sole per JVM aspect.
     *
     * @return the aspect
     */
    public CrossCutting getPerJvmAspect() {
        if (m_perJvm == null) {
            try {
                m_perJvm = (CrossCutting)m_prototype.getAspectClass().newInstance();
                m_perJvm.setCrossCuttingInfo(CrossCuttingInfo.newInstance(m_prototype));
            }
            catch (Exception e) {
                throw new WrappedRuntimeException(e);
            }
        }
        return m_perJvm;
    }

    /**
     * Returns the aspect for the current class.
     *
     * @return the aspect
     */
    public CrossCutting getPerClassAspect(final Class callingClass) {
        if (!m_perClass.containsKey(callingClass)) {
            synchronized (m_perClass) {
                try {
                    CrossCutting aspect = (CrossCutting)m_prototype.getAspectClass().newInstance();
                    CrossCuttingInfo info = CrossCuttingInfo.newInstance(m_prototype);
                    info.setTargetClass(callingClass);
                    aspect.setCrossCuttingInfo(info);
                    m_perClass.put(callingClass, aspect);
                }
                catch (Exception e) {
                    throw new WrappedRuntimeException(e);
                }
            }
        }
        return (CrossCutting)m_perClass.get(callingClass);
    }

    /**
     * Returns the aspect for the current instance.
     *
     * @return the aspect
     */
    public CrossCutting getPerInstanceAspect(final Object callingInstance) {
        if (callingInstance == null) {
            return getPerClassAspect(callingInstance.getClass());
        }
        if (!m_perInstance.containsKey(callingInstance)) {
            synchronized (m_perInstance) {
                try {
                    CrossCutting aspect = (CrossCutting)m_prototype.getAspectClass().newInstance();
                    CrossCuttingInfo info = CrossCuttingInfo.newInstance(m_prototype);
                    info.setTargetInstance(callingInstance);
                    aspect.setCrossCuttingInfo(info);
                    m_perInstance.put(callingInstance, aspect);
                }
                catch (Exception e) {
                    throw new WrappedRuntimeException(e);
                }
            }
        }
        return (CrossCutting)m_perInstance.get(callingInstance);
    }

    /**
     * Returns the aspect for the current thread.
     *
     * @return the aspect
     */
    public CrossCutting getPerThreadAspect() {
        final Thread currentThread = Thread.currentThread();
        if (!m_perThread.containsKey(currentThread)) {
            synchronized (m_perThread) {
                try {
                    CrossCutting aspect = (CrossCutting)m_prototype.getAspectClass().newInstance();
                    aspect.setCrossCuttingInfo(CrossCuttingInfo.newInstance(m_prototype));
                    m_perThread.put(currentThread, aspect);
                }
                catch (Exception e) {
                    throw new WrappedRuntimeException(e);
                }
            }
        }
        return (CrossCutting)m_perThread.get(currentThread);
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
