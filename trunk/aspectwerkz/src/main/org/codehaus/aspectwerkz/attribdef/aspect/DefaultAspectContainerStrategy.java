/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.attribdef.aspect;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.WeakHashMap;
import java.util.Map;
import java.util.List;

import org.codehaus.aspectwerkz.ContainerType;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.attribdef.aspect.Aspect;
import org.codehaus.aspectwerkz.attribdef.aspect.AspectContainer;
import org.codehaus.aspectwerkz.transform.TransformationUtil;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;

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
     * Holds a reference to the sole per JVM introduction.
     */
    protected Object m_perJvm;

    /**
     * Holds references to the per class introductions.
     */
    protected Map m_perClass = new HashMap();//TODO shoould be weak for 0.10

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
    protected Aspect m_prototype;

    /**
     * The methods repository.
     */
    protected Method[] m_methodRepository = new Method[0];

    /**
     * Creates a new transient container strategy.
     *
     * @param prototype the advice prototype
     */
    public DefaultAspectContainerStrategy(final Aspect prototype) {
        if (prototype == null) throw new IllegalArgumentException("aspect prototype can not be null");
        m_prototype = prototype;
        createMethodRepository();
    }

    /**
     * Invokes the advice method on a per JVM basis.
     *
     * @param methodIndex the method index
     * @param joinPoint the join point
     * @return the result from the method invocation
     */
    public Object invokeAdvicePerJvm(final int methodIndex, final JoinPoint joinPoint) {
        Object result = null;
        try {
            if (m_perJvm == null) {
                m_perJvm = Aspect.newInstance(m_prototype);
            }
            Method method = m_methodRepository[methodIndex];
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
     * @param joinPoint the join point
     * @return the result from the method invocation
     */
    public Object invokeAdvicePerClass(final int methodIndex, final JoinPoint joinPoint) {
        final Class targetClass = joinPoint.getTargetClass();
        Object result = null;
        try {
            if (!m_perClass.containsKey(targetClass)) {
                synchronized (m_perClass) {
                    Aspect aspect = Aspect.newInstance(m_prototype);
                    aspect.___AW_setTargetClass(targetClass);
                    m_perClass.put(targetClass, aspect);
                }
            }
            result = m_methodRepository[methodIndex].invoke(
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
     * @param joinPoint the join point
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
                    Aspect aspect = Aspect.newInstance(m_prototype);
                    aspect.___AW_setTargetInstance(targetInstance);
                    m_perInstance.put(targetInstance, aspect);
                }
            }
            result = m_methodRepository[methodIndex].invoke(
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
     * @param joinPoint the join point
     * @return the result from the method invocation
     */
    public Object invokeAdvicePerThread(final int methodIndex, final JoinPoint joinPoint) {
        Object result;
        try {
            final Thread currentThread = Thread.currentThread();
            if (!m_perThread.containsKey(currentThread)) {
                synchronized (m_perThread) {
                    m_perThread.put(currentThread, Aspect.newInstance(m_prototype));
                }
            }
            Method method = m_methodRepository[methodIndex];
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
        return m_methodRepository[index];
    }

    /**
     * Returns the sole per JVM aspect.
     *
     * @return the aspect
     */
    public Aspect getPerJvmAspect() {
        if (m_perJvm == null) {
            try {
                m_perJvm = Aspect.newInstance(m_prototype);
            }
            catch (Exception e) {
                throw new WrappedRuntimeException(e);
            }
        }
        return (Aspect) m_perJvm;
    }

    /**
     * Returns the aspect for the current class.
     *
     * @return the aspect
     */
    public Aspect getPerClassAspect(final Class callingClass) {
        if (!m_perClass.containsKey(callingClass)) {
            synchronized (m_perClass) {
                try {
                    Aspect aspect = Aspect.newInstance(m_prototype);
                    aspect.___AW_setTargetClass(callingClass);
                    m_perClass.put(callingClass, aspect);
                }
                catch (Exception e) {
                    throw new WrappedRuntimeException(e);
                }
            }
        }
        return (Aspect) m_perClass.get(callingClass);
    }

    /**
     * Returns the aspect for the current instance.
     *
     * @return the aspect
     */
    public Aspect getPerInstanceAspect(final Object callingInstance) {
        if (callingInstance == null) {
            return getPerClassAspect(callingInstance.getClass());
        }
        if (!m_perInstance.containsKey(callingInstance)) {
            synchronized (m_perInstance) {
                try {
                    Aspect aspect = Aspect.newInstance(m_prototype);
                    aspect.___AW_setTargetInstance(callingInstance);
                    m_perInstance.put(callingInstance, aspect);
                }
                catch (Exception e) {
                    throw new WrappedRuntimeException(e);
                }
            }
        }
        return (Aspect) m_perInstance.get(callingInstance);
    }

    /**
     * Returns the aspect for the current thread.
     *
     * @return the aspect
     */
    public Aspect getPerThreadAspect() {
        final Thread currentThread = Thread.currentThread();
        if (!m_perThread.containsKey(currentThread)) {
            synchronized (m_perThread) {
                try {
                    m_perThread.put(currentThread, Aspect.newInstance(m_prototype));
                }
                catch (Exception e) {
                    throw new WrappedRuntimeException(e);
                }
            }
        }
        return (Aspect) m_perThread.get(currentThread);
    }

    /**
     * Creates a method repository for the advice methods.
     */
    private void createMethodRepository() {
        synchronized (m_methodRepository) {
            List methodList = TransformationUtil.createSortedMethodList(m_prototype.___AW_getAspectClass());
            m_methodRepository = new Method[methodList.size()];
            for (int i = 0; i < m_methodRepository.length; i++) {
                Method method = (Method)methodList.get(i);
                method.setAccessible(true);
                m_methodRepository[i] = method;
            }
        }
    }

    /**
     * Attach the introduction container to this aspect container
     * to mirror the "aspect contains 0-n introduction"
     * @param name of the introduction
     * @param introContainer introduction container
     */
    public void addIntroductionContainer(String name, IntroductionContainer introContainer) {
        m_introductionContainers.put(name, introContainer);
    }

    /**
     * Returns the introduction container of given name (introduction name)
     * or null if not linked.
     * @param name of the introduction
     * @return introduction container
     */
    public IntroductionContainer getIntroductionContainer(String name) {
        return (IntroductionContainer)m_introductionContainers.get(name);
    }


}
