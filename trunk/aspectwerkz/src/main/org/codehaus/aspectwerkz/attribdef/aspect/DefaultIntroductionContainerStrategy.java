/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.attribdef.aspect;

import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.exception.DefinitionException;
import org.codehaus.aspectwerkz.attribdef.definition.IntroductionDefinition;
import org.codehaus.aspectwerkz.transform.TransformationUtil;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.WeakHashMap;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;

/**
 * Container for Introductions
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class DefaultIntroductionContainerStrategy implements IntroductionContainer {

    /**
     * Holds a reference to the sole per JVM introduction.
     */
    protected Introduction m_perJvm;

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
     * The introduction prototype.
     */
    protected Introduction m_prototype;

    /**
     * The methods repository.
     */
    protected Method[] m_methodRepository = new Method[0];

    /**
     * Creates a new container strategy.
     *
     * @param prototype the advice prototype
     */
    public DefaultIntroductionContainerStrategy(final Introduction prototype) {
        if (prototype == null) throw new IllegalArgumentException("introduction prototype can not be null");
        m_prototype = prototype;
        createMethodRepository();
    }

    /**
     * Invokes the method on a per JVM basis.
     *
     * @param methodIndex the method index
     * @param parameters the parameters for the invocation
     * @return the result from the method invocation
     */
    public Object invokeIntroductionPerJvm(final int methodIndex, final Object[] parameters) {
        Object result = null;
        try {
            if (m_perJvm == null) {
                Aspect perJVMAspect = m_prototype.getAspect().___AW_getContainer().getPerJvmAspect();
                m_perJvm = Introduction.newInstance(m_prototype, perJVMAspect);
            }
            result = m_methodRepository[methodIndex].invoke(m_perJvm.___AW_getImplementation(), parameters);
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
     * @param targetInstance a reference to the calling object
     * @param methodIndex the method index
     * @param parameters the parameters for the invocation
     * @return the result from the method invocation
     */
    public Object invokeIntroductionPerClass(final Object targetInstance,
                                             final int methodIndex,
                                             final Object[] parameters) {
        final Class targetClass = targetInstance.getClass();
        Object result = null;
        try {
            if (!m_perClass.containsKey(targetClass)) {
                synchronized (m_perClass) {
                    Aspect perClassAspect = m_prototype.getAspect().___AW_getContainer().getPerClassAspect(targetClass);
                    Introduction perClassIntroduction = Introduction.newInstance(m_prototype, perClassAspect);
                    m_perClass.put(targetClass, perClassIntroduction);
                }
            }
            result = m_methodRepository[methodIndex].invoke(
                    ((Introduction)m_perClass.get(targetClass)).___AW_getImplementation(),
                    parameters
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
     * Invokes the method on a per instance basis.
     *
     * @param targetInstance a reference to the target instance
     * @param methodIndex the method index
     * @param parameters the parameters for the invocation
     * @return the result from the method invocation
     */
    public Object invokeIntroductionPerInstance(final Object targetInstance,
                                                final int methodIndex,
                                                final Object[] parameters) {
        Object result = null;
        try {
            if (!m_perInstance.containsKey(targetInstance)) {
                synchronized (m_perInstance) {
                    Aspect perInstanceAspect = m_prototype.getAspect().___AW_getContainer().getPerInstanceAspect(targetInstance);
                    Introduction perInstanceIntroduction = Introduction.newInstance(m_prototype, perInstanceAspect);
                    m_perInstance.put(targetInstance, perInstanceIntroduction);
                }
            }
            result = m_methodRepository[methodIndex].invoke(
                    ((Introduction)m_perInstance.get(targetInstance)).___AW_getImplementation(),
                    parameters
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
     * Invokes the method on a per thread basis.
     *
     * @param methodIndex the method index
     * @param parameters the parameters for the invocation
     * @return the result from the method invocation
     */
    public Object invokeIntroductionPerThread(final int methodIndex, final Object[] parameters) {
        Object result;
        try {
            final Thread currentThread = Thread.currentThread();
            if (!m_perThread.containsKey(currentThread)) {
                synchronized (m_perThread) {
                    Aspect perThread = m_prototype.getAspect().___AW_getContainer().getPerThreadAspect();
                    m_perThread.put(currentThread, Introduction.newInstance(m_prototype, perThread));
                }
            }
            result = m_methodRepository[methodIndex].invoke(
                    ((Introduction)m_perThread.get(currentThread)).___AW_getImplementation(),
                    parameters
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
     * Swaps the current mixin implementation.
     *
     * @param newImplementationClass the class of the new implementation to use
     */
    public void swapImplementation(final Class newImplementationClass) {
        if (newImplementationClass == null) throw new IllegalArgumentException("new implementation class class can not be null");

        // check compatibility
        IntroductionDefinition def = m_prototype.getIntroductionDefinition();
        for (Iterator intfs = def.getInterfaceClassNames().iterator(); intfs.hasNext();) {
            //todo findInterface does not work when A impl I and B extend A: B does not match I
            if ( ! findInterface(newImplementationClass, (String) intfs.next()) ) {
                throw new DefinitionException("new implementation class is not compatible");
            }
        }

        synchronized (this) {
            try {
                // create the new introduction to replace the current one
                m_prototype.swapImplementation(newImplementationClass);
                createMethodRepository();

                // clear the current introduction storages
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
     * TODO double code from xmldef Introduction
     * Recursively traverse the interface hierarchy implemented by the given root class in
     * order to find one that matches the given name.
     *
     * @param root is the class or interface to start the search at.
     * @param requiredInterface that we are looking for.
     * @return <code>true</code> if we found the interface, <code>false</code> otherwise.
     */
    private static boolean findInterface(final Class root, final String requiredInterface) {

        // The implementation uses a single loop over the directly
        // implemented interfaces. In the loop, first we check if the
        // current interface is the one we're looking for, A then if not
        // we call this same method starting at that current interface.
        Class[] interfaces = root.getInterfaces();

        for (int i = 0; i < interfaces.length; i++) {
            Class implemented = interfaces[i];
            if (implemented.getName().equals(requiredInterface)
                    || findInterface(implemented, requiredInterface)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates a method repository for the introduced methods.
     */
    private void createMethodRepository() {
        synchronized (m_methodRepository) {
            List methodList = TransformationUtil.createSortedMethodList(m_prototype.___AW_getImplementation().getClass());
            m_methodRepository = new Method[methodList.size()];
            for (int i = 0; i < m_methodRepository.length; i++) {
                Method method = (Method)methodList.get(i);
                method.setAccessible(true);
                m_methodRepository[i] = method;
            }
        }
    }


}
