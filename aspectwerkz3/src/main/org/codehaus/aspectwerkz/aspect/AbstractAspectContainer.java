/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.aspect;

import org.codehaus.aspectwerkz.CrossCuttingInfo;
import org.codehaus.aspectwerkz.DeploymentModel;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.transform.TransformationUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Abstract base class for the aspect container implementations.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public abstract class AbstractAspectContainer implements AspectContainer {
    public static final int ASPECT_CONSTRUCTION_TYPE_UNKNOWN = 0;
    public static final int ASPECT_CONSTRUCTION_TYPE_DEFAULT = 1;
    public static final int ASPECT_CONSTRUCTION_TYPE_CROSS_CUTTING_INFO = 2;
    public static final Object[] EMPTY_OBJECT_ARRAY = new Object[]{};

    /**
     * The aspect construction type.
     */
    protected int m_constructionType = ASPECT_CONSTRUCTION_TYPE_UNKNOWN;

    /**
     * Introduction container containing introduction declared by this aspect, keys by introduction names
     */
    protected final Map m_introductionContainers = new HashMap();

    /**
     * The cross-cutting info prototype.
     */
    protected final CrossCuttingInfo m_infoPrototype;

    /**
     * An array with the single cross-cutting info, needed to save one array creation per invocation.
     */
    protected final Object[] arrayWithSingleCrossCuttingInfo = new Object[1];

    /**
     * The aspect instance prototype.
     */
    protected final Object m_aspectPrototype;

    /**
     * Holds a reference to the sole per JVM aspect instance.
     */
    protected Object m_perJvm;

    /**
     * Holds references to the per class aspect instances.
     */
    protected final Map m_perClass = new WeakHashMap();

    /**
     * Holds references to the per instance aspect instances.
     */
    protected final Map m_perInstance = new WeakHashMap();

    /**
     * Holds references to the per thread aspect instances.
     */
    protected final Map m_perThread = new WeakHashMap();

    /**
     * The advice repository.
     */
    protected Method[] m_adviceRepository = new Method[0];

    /**
     * Creates a new aspect container strategy.
     *
     * @param crossCuttingInfo the cross-cutting info
     */
    public AbstractAspectContainer(final CrossCuttingInfo crossCuttingInfo) {
        if (crossCuttingInfo == null) {
            throw new IllegalArgumentException("cross-cutting info can not be null");
        }
        m_infoPrototype = crossCuttingInfo;
        arrayWithSingleCrossCuttingInfo[0] = m_infoPrototype;
        m_aspectPrototype = createAspect();
        createAdviceRepository();
    }

    /**
     * Invokes an introduced method with the index specified.
     *
     * @param methodIndex the method index
     * @param joinPoint   the join point
     * @return the result from the invocation
     */
    public Object invokeAdvice(final int methodIndex, final JoinPoint joinPoint) throws Throwable {
        Object result = null;
        switch (m_infoPrototype.getDeploymentModel()) {
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
                throw new RuntimeException("invalid deployment model: " + m_infoPrototype.getDeploymentModel());
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
        return m_infoPrototype;
    }

    /**
     * Invokes the advice method on a per JVM basis.
     *
     * @param methodIndex the method index
     * @param joinPoint   the join point
     * @return the result from the method invocation
     */
    private Object invokeAdvicePerJvm(final int methodIndex, final JoinPoint joinPoint) throws Throwable {
        Object result;
        try {
            createPerJvmAspect();
            Method method = m_adviceRepository[methodIndex];
            result = method.invoke(m_perJvm, new Object[]{joinPoint});
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        } catch (Exception e) {
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
    private Object invokeAdvicePerClass(final int methodIndex, final JoinPoint joinPoint) throws Throwable {
        final Class targetClass = joinPoint.getTargetClass();
        Object result;
        try {
            createPerClassAspect(targetClass);
            result = m_adviceRepository[methodIndex].invoke(m_perClass.get(targetClass), new Object[]{joinPoint});
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        } catch (Exception e) {
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
    private Object invokeAdvicePerInstance(final int methodIndex, final JoinPoint joinPoint) throws Throwable {
        Object result = null;
        Object targetInstance = joinPoint.getTargetInstance();
        if (targetInstance == null) { // can be null if f.e. an aspect has deployment model perInstance and has caller side pointcuts defined
            return invokeAdvicePerClass(methodIndex, joinPoint);
        }
        try {
            createPerInstanceAspect(targetInstance);
            result = m_adviceRepository[methodIndex].invoke(
                    m_perInstance.get(targetInstance),
                    new Object[]{joinPoint}
            );
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        } catch (Exception e) {
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
    private Object invokeAdvicePerThread(final int methodIndex, final JoinPoint joinPoint) throws Throwable {
        Object result;
        try {
            final Thread currentThread = Thread.currentThread();
            createPerThreadAspect(currentThread);
            Method method = m_adviceRepository[methodIndex];
            result = method.invoke(m_perThread.get(currentThread), new Object[]{joinPoint});
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        } catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
        return result;
    }

    /**
     * Creates a new perJVM cross-cutting instance, if it already exists then return it.
     *
     * @return the cross-cutting instance
     */
    public Object createPerJvmAspect() {
        if (m_perJvm == null) {
            m_perJvm = createAspect();
        }
        return m_perJvm;
    }

    /**
     * Creates a new perClass cross-cutting instance, if it already exists then return it.
     *
     * @param callingClass
     * @return the cross-cutting instance
     */
    public Object createPerClassAspect(final Class callingClass) {
        synchronized (m_perClass) {
            if (!m_perClass.containsKey(callingClass)) {
                m_perClass.put(callingClass, createAspect());
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
    public Object createPerInstanceAspect(final Object callingInstance) {
        if (callingInstance == null) {
            return m_perJvm;
        }
        synchronized (m_perInstance) {
            if (!m_perInstance.containsKey(callingInstance)) {
                m_perInstance.put(callingInstance, createAspect());
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
    public Object createPerThreadAspect(final Thread thread) {
        synchronized (m_perThread) {
            if (!m_perThread.containsKey(thread)) {
                m_perThread.put(thread, createAspect());
            }
        }
        return m_perThread.get(thread);
    }

    /**
     * Attach the introduction container to this aspect container to mirror the "aspect contains 0-n introduction"
     *
     * @param name           of the introduction
     * @param introContainer introduction container
     */
    public void addIntroductionContainer(final String name, final IntroductionContainer introContainer) {
        m_introductionContainers.put(name, introContainer);
    }

    /**
     * Returns the introduction container of given name (introduction name) or null if not linked.
     *
     * @param name of the introduction
     * @return introduction container
     */
    public IntroductionContainer getIntroductionContainer(final String name) {
        return (IntroductionContainer)m_introductionContainers.get(name);
    }

    /**
     * Creates a repository for the advice methods.
     */
    protected void createAdviceRepository() {
        synchronized (m_adviceRepository) {
            List methodList = TransformationUtil.createSortedMethodList(m_infoPrototype.getAspectClass());
            m_adviceRepository = new Method[methodList.size()];
            for (int i = 0; i < m_adviceRepository.length; i++) {
                Method method = (Method)methodList.get(i);
                method.setAccessible(true);
                m_adviceRepository[i] = method;
            }
        }
    }

    /**
     * To be implemented by the concrete aspect containers.
     * <p/>
     * Should return a new aspect instance.
     *
     * @return a new aspect instance
     */
    protected abstract Object createAspect();
}
