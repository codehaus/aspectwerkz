/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.aspect;

import org.codehaus.aspectwerkz.DeploymentModel;
import org.codehaus.aspectwerkz.definition.IntroductionDefinition;
import org.codehaus.aspectwerkz.exception.DefinitionException;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.transform.TransformationUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Container for Introductions.
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public class IntroductionContainer
{
    /**
     * Holds a reference to the sole per JVM introduction.
     */
    protected Introduction m_perJvm;

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
    public IntroductionContainer(final Introduction prototype,
        final AspectContainer definingAspectContainer)
    {
        if (prototype == null)
        {
            throw new IllegalArgumentException(
                "introduction prototype can not be null");
        }

        m_prototype = prototype;
        createMethodRepository();

        // link it to the aspect container
        definingAspectContainer.addIntroductionContainer(prototype.getName(),
            this);
    }

    /**
     * Invokes the method on a per JVM basis.
     *
     * @param methodIndex the method index
     * @param parameters  the parameters for the invocation
     * @return the result from the method invocation
     */
    public Object invokeIntroductionPerJvm(final int methodIndex,
        final Object[] parameters)
    {
        Object result = null;

        try
        {
            if (m_perJvm == null)
            {
                // only compatible aspect deployment is perJVM
                m_perJvm = Introduction.newInstance(m_prototype,
                        m_prototype.getCrossCuttingInfo());
            }

            result = m_methodRepository[methodIndex].invoke(m_perJvm
                    .getImplementation(), parameters);
        }
        catch (InvocationTargetException e)
        {
            if (e.getTargetException() instanceof ClassCastException)
            {
                System.err.println(
                    "WARNING: ClassCastException has been thrown from introduced method - this can occur if you cast 'this' to CrossCutting instead of casting 'OuterAspectClass.this'");
            }

            throw new WrappedRuntimeException(e.getTargetException());
        }
        catch (Exception e)
        {
            throw new WrappedRuntimeException(e);
        }

        return result;
    }

    /**
     * Invokes the method on a per class basis.
     *
     * @param targetInstance a reference to the calling object
     * @param methodIndex    the method index
     * @param parameters     the parameters for the invocation
     * @return the result from the method invocation
     */
    public Object invokeIntroductionPerClass(final Object targetInstance,
        final int methodIndex, final Object[] parameters)
    {
        final Class targetClass = targetInstance.getClass();
        Object result = null;

        try
        {
            if (!m_perClass.containsKey(targetClass))
            {
                synchronized (m_perClass)
                {
                    // only compatible aspect deployments are perJVM and perClass
                    Introduction perClassIntroduction = Introduction
                        .newInstance(m_prototype,
                            m_prototype.getCrossCuttingInfo());

                    m_perClass.put(targetClass, perClassIntroduction);
                }
            }

            result = m_methodRepository[methodIndex].invoke(((Introduction) m_perClass
                    .get(targetClass)).getImplementation(), parameters);
        }
        catch (InvocationTargetException e)
        {
            if (e.getTargetException() instanceof ClassCastException)
            {
                System.err.println(
                    "WARNING: ClassCastException has been thrown from introduced method - this can occur if you cast 'this' to CrossCutting instead of casting 'OuterAspectClass.this'");
            }

            throw new WrappedRuntimeException(e.getTargetException());
        }
        catch (Exception e)
        {
            throw new WrappedRuntimeException(e);
        }

        return result;
    }

    /**
     * Invokes the method on a per instance basis.
     *
     * @param targetInstance a reference to the target instance
     * @param methodIndex    the method index
     * @param parameters     the parameters for the invocation
     * @return the result from the method invocation
     */
    public Object invokeIntroductionPerInstance(final Object targetInstance,
        final int methodIndex, final Object[] parameters)
    {
        Object result = null;

        try
        {
            if (!m_perInstance.containsKey(targetInstance))
            {
                synchronized (m_perInstance)
                {
                    // only compatible aspect deployments are perJVM and perClass
                    Introduction perInstanceIntroduction = Introduction
                        .newInstance(m_prototype,
                            m_prototype.getCrossCuttingInfo());

                    m_perInstance.put(targetInstance, perInstanceIntroduction);
                }
            }

            result = m_methodRepository[methodIndex].invoke(((Introduction) m_perInstance
                    .get(targetInstance)).getImplementation(), parameters);
        }
        catch (InvocationTargetException e)
        {
            if (e.getTargetException() instanceof ClassCastException)
            {
                System.err.println(
                    "WARNING: ClassCastException has been thrown from introduced method - this can occur if you cast 'this' to CrossCutting instead of casting 'OuterAspectClass.this'");
            }

            throw new WrappedRuntimeException(e.getTargetException());
        }
        catch (Exception e)
        {
            throw new WrappedRuntimeException(e);
        }

        return result;
    }

    /**
     * Invokes the method on a per thread basis.
     *
     * @param methodIndex the method index
     * @param parameters  the parameters for the invocation
     * @return the result from the method invocation
     */
    public Object invokeIntroductionPerThread(final int methodIndex,
        final Object[] parameters)
    {
        Object result;

        try
        {
            final Thread currentThread = Thread.currentThread();

            if (!m_perThread.containsKey(currentThread))
            {
                synchronized (m_perThread)
                {
                    // only compatible aspect deployments is perThread
                    m_perThread.put(currentThread,
                        Introduction.newInstance(m_prototype,
                            m_prototype.getCrossCuttingInfo()));
                }
            }

            result = m_methodRepository[methodIndex].invoke(((Introduction) m_perThread
                    .get(currentThread)).getImplementation(), parameters);
        }
        catch (InvocationTargetException e)
        {
            if (e.getTargetException() instanceof ClassCastException)
            {
                System.err.println(
                    "WARNING: ClassCastException has been thrown from introduced method - this can occur if you cast 'this' to CrossCutting instead of casting 'OuterAspectClass.this'");
            }

            throw new WrappedRuntimeException(e.getTargetException());
        }
        catch (Exception e)
        {
            throw new WrappedRuntimeException(e);
        }

        return result;
    }

    /**
     * Retrieve the related aspect instance from the aspect container The mixin deployment model is tight to the aspect
     * deployment model as follows: Mixin            Aspect possible models perJVM            perJVM perClass            perJVM,perClass
     * perInstance        perJVM,perClass,perInstance perThread        perThread
     *
     * @param referent (null, targetClass, targetInstance or currentThread depending of mixin deployment model)
     * @return related cross-cutting info
     */

    //    private CrossCuttingInfo getRelatedCrossCuttingInfo() {
    //        CrossCuttingInfo info = m_prototype.getCrossCuttingInfos();
    //
    //        switch (m_prototype.getDeploymentModel()) {
    //            case (DeploymentModel.PER_JVM):
    //                return info.getContainer().getCrossCuttingInfos();
    //
    //            case (DeploymentModel.PER_CLASS):
    //                if (info.getDeploymentModel() == DeploymentModel.PER_CLASS) {
    //                    return info.getContainer().getCrossCuttingInfos((Class)referent);
    //                }
    //                else {//PER_JVM
    //                    return info.getContainer().getCrossCuttingInfos();
    //                }
    //
    //            case (DeploymentModel.PER_INSTANCE):
    //                if (info.getDeploymentModel() == DeploymentModel.PER_INSTANCE) {
    //                    return info.getContainer().getCrossCuttingInfos(referent);
    //                }
    //                else if (info.getDeploymentModel() == DeploymentModel.PER_CLASS) {
    //                    return info.getContainer().getCrossCuttingInfos((Class)referent.getClass());
    //                }
    //                else {//PER_JVM
    //                    return info.getContainer().getCrossCuttingInfos();
    //                }
    //
    //            case (DeploymentModel.PER_THREAD):
    //                return info.getContainer().getCrossCuttingInfos(Thread.currentThread());
    //
    //        }
    //        throw new RuntimeException("this point should never be reached");
    //    }

    /**
     * Swaps the current mixin implementation.
     *
     * @param newImplementationClass the class of the new implementation to use
     */
    public void swapImplementation(final Class newImplementationClass)
    {
        if (newImplementationClass == null)
        {
            throw new IllegalArgumentException(
                "new implementation class class can not be null");
        }

        // check compatibility
        IntroductionDefinition def = m_prototype.getIntroductionDefinition();

        for (Iterator intfs = def.getInterfaceClassNames().iterator();
            intfs.hasNext();)
        {
            if (!findInterfaceInHierarchy(newImplementationClass,
                    (String) intfs.next()))
            {
                throw new DefinitionException(
                    "new implementation class is not compatible");
            }
        }

        synchronized (this)
        {
            try
            {
                // create the new introduction to replace the current one
                m_prototype.swapImplementation(newImplementationClass);
                createMethodRepository();

                // clear the current introduction storages
                m_perJvm = null;
                m_perClass = new HashMap(m_perClass.size());
                m_perInstance = new WeakHashMap(m_perClass.size());
                m_perThread = new WeakHashMap(m_perClass.size());
            }
            catch (Exception e)
            {
                new WrappedRuntimeException(e);
            }
        }
    }

    /**
     * Recursively traverse the interface hierarchy implemented by the given root class in order to find one that
     * matches the given name. Looks in the class hierarchy as well.
     *
     * @param root              is the class or interface to start the search at.
     * @param requiredInterface that we are looking for.
     * @return <code>true</code> if we found the interface, <code>false</code> otherwise.
     */
    private static boolean findInterfaceInHierarchy(final Class root,
        final String requiredInterface)
    {
        if (root == null)
        {
            return false;
        }

        // looks in directly implemented interface first
        Class[] interfaces = root.getInterfaces();

        for (int i = 0; i < interfaces.length; i++)
        {
            Class implemented = interfaces[i];

            if (implemented.getName().equals(requiredInterface)
                || findInterfaceInHierarchy(implemented, requiredInterface))
            {
                return true;
            }
        }

        return findInterfaceInHierarchy(root.getSuperclass(), requiredInterface);
    }

    /**
     * Creates a method repository for the introduced methods.
     */
    private void createMethodRepository()
    {
        synchronized (m_methodRepository)
        {
            List methodList = TransformationUtil.createSortedMethodList(m_prototype.getImplementation()
                                                                                   .getClass());

            m_methodRepository = new Method[methodList.size()];

            for (int i = 0; i < m_methodRepository.length; i++)
            {
                Method method = (Method) methodList.get(i);

                method.setAccessible(true);
                m_methodRepository[i] = method;
            }
        }
    }

    /**
     * Returns the target instance from an introduction
     *
     * @param mixinImpl aka "this" from the mixin impl
     * @return the target instance or null (if not perInstance deployed mixin)
     */
    public Object getTargetInstance(Object mixinImpl)
    {
        Object targetInstance = null;

        if (m_prototype.getDeploymentModel() == DeploymentModel.PER_INSTANCE)
        {
            for (Iterator i = m_perInstance.entrySet().iterator(); i.hasNext();)
            {
                Map.Entry entry = (Map.Entry) i.next();
                Object mixin = ((Introduction) entry.getValue())
                    .getImplementation();

                if (mixinImpl.equals(mixin))
                {
                    targetInstance = entry.getKey();

                    break;
                }
            }
        }

        return targetInstance;
    }

    /**
     * Returns the target class from an introduction
     *
     * @param mixinImpl aka "this" from the mixin impl
     * @return the target instance or null (if not perInstance or perClas deployed mixin)
     */
    public Class getTargetClass(Object mixinImpl)
    {
        Class targetClass = null;

        if (m_prototype.getDeploymentModel() == DeploymentModel.PER_INSTANCE)
        {
            Object instance = getTargetInstance(mixinImpl);

            if (instance != null)
            {
                targetClass = instance.getClass();
            }
        }
        else if (m_prototype.getDeploymentModel() == DeploymentModel.PER_CLASS)
        {
            for (Iterator i = m_perClass.entrySet().iterator(); i.hasNext();)
            {
                Map.Entry entry = (Map.Entry) i.next();
                Object mixin = ((Introduction) entry.getValue())
                    .getImplementation();

                if (mixinImpl.equals(mixin))
                {
                    targetClass = (Class) entry.getKey();

                    break;
                }
            }
        }

        return targetClass;
    }
}
