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

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.WeakHashMap;

import org.codehaus.aspectwerkz.Identifiable;
import org.codehaus.aspectwerkz.MetaDataKeys;
import org.codehaus.aspectwerkz.MetaDataEnhanceable;
import org.codehaus.aspectwerkz.DeploymentModel;
import org.codehaus.aspectwerkz.MemoryType;
import org.codehaus.aspectwerkz.persistence.PersistenceManager;
import org.codehaus.aspectwerkz.persistence.PersistenceManagerFactory;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;

/**
 * Implements a persistent version of the introduction memory strategy.
 *
 * @author <a href="mailto:jboner@acm.org">Jonas Bonér</a>
 * @version $Id: PersistentIntroductionMemoryStrategy.java,v 1.1.1.1 2003-05-11 15:14:23 jboner Exp $
 */
public class PersistentIntroductionMemoryStrategy
        extends IntroductionMemoryStrategy {

    /**
     * The UUID for this aspect component.
     */
    protected String m_uuid;

    /**
     * The persistence manager to use.
     */
    protected static PersistenceManager s_persistenceManager;

    /**
     * Loads the persistence manager to use.
     */
    static {
        s_persistenceManager = PersistenceManagerFactory.getFactory(
                PersistenceManagerFactory.getPersistenceManagerType()).
                createPersistenceManager();
    }

    /**
     * Creates a new persistent distribution strategy.
     *
     * @param uuid the uuid for the introduction
     * @param implClass the implementation class
     */
    public PersistentIntroductionMemoryStrategy(final String uuid,
                                                final Class implClass) {
        super(implClass);
        m_uuid = uuid;
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
            // try to get the introduction from the db
            m_perJvm = s_persistenceManager.retrieve(
                    m_uuid, null, DeploymentModel.PER_JVM);

            // if null; create a new one
            if (m_perJvm == null) {
                m_perJvm = m_implClass.newInstance();

                ((MetaDataEnhanceable)m_perJvm).___hidden$addMetaData(
                        MetaDataKeys.ASPECT_COMPONENT_UUID,
                        m_uuid);

                ((MetaDataEnhanceable)m_perJvm).___hidden$addMetaData(
                        MetaDataKeys.DEPLOYMENT_MODEL,
                        new Integer(DeploymentModel.PER_JVM));

                s_persistenceManager.create(
                        m_perJvm, null,
                        DeploymentModel.PER_JVM);
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
     * @param callingObjectUuid the UUID for the calling object
     * @param methodIndex the method index
     * @param parameters the parameters for the invocation
     * @return the result from the method invocation
     */
    public Object invokePerClass(final Object callingObject,
                                 final Object callingObjectUuid,
                                 final int methodIndex,
                                 final Object[] parameters) {

        Class callingClass = callingObject.getClass();

        Object introduction = null;
        if (!m_perClass.containsKey(callingClass)) {

            // try to get the advice from the db
            introduction = s_persistenceManager.retrieve(
                    m_uuid, callingClass, DeploymentModel.PER_CLASS);

            // if null; create a new one
            if (introduction == null) {
                try {
                    introduction = m_implClass.newInstance();

                    ((MetaDataEnhanceable)introduction).___hidden$addMetaData(
                            MetaDataKeys.ASPECT_COMPONENT_UUID, m_uuid);

                    ((MetaDataEnhanceable)introduction).___hidden$addMetaData(
                            MetaDataKeys.TARGET_OBJECT_UUID, callingClass);

                    ((MetaDataEnhanceable)introduction).___hidden$addMetaData(
                            MetaDataKeys.DEPLOYMENT_MODEL,
                            new Integer(DeploymentModel.PER_CLASS));

                    s_persistenceManager.create(
                            introduction, callingClass, DeploymentModel.PER_CLASS);
                }
                catch (Exception e) {
                    throw new WrappedRuntimeException(e);
                }
            }
            synchronized (m_perClass) {
                m_perClass.put(callingClass, introduction);
            }
        }
        else {
            introduction = m_perClass.get(callingClass);
        }

        Object result = null;
        try {
            result = m_methods[methodIndex].invoke(introduction, parameters);
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
     * @param callingObjectUuid the UUID for the calling object
     * @param methodIndex the method index
     * @param parameters the parameters for the invocation
     * @return the result from the method invocation
     */
    public Object invokePerInstance(final Object callingObject,
                                    final Object callingObjectUuid,
                                    final int methodIndex,
                                    final Object[] parameters) {

        String index = ((Identifiable)callingObject).getUuid();

        Object introduction = null;
        if (!m_perInstance.containsKey(index)) {

            // try to get the advice from the db
            introduction = s_persistenceManager.retrieve(
                    m_uuid, index, DeploymentModel.PER_INSTANCE);

            // if null; create a new one
            if (introduction == null) {
                try {
                    introduction = m_implClass.newInstance();

                    ((MetaDataEnhanceable)introduction).___hidden$addMetaData(
                            MetaDataKeys.ASPECT_COMPONENT_UUID, m_uuid);

                    ((MetaDataEnhanceable)introduction).___hidden$addMetaData(
                            MetaDataKeys.TARGET_OBJECT_UUID, index);

                    ((MetaDataEnhanceable)introduction).___hidden$addMetaData(
                            MetaDataKeys.DEPLOYMENT_MODEL,
                            new Integer(DeploymentModel.PER_INSTANCE));

                    s_persistenceManager.create(
                            introduction, index, DeploymentModel.PER_INSTANCE);
                }
                catch (Exception e) {
                    throw new WrappedRuntimeException(e);
                }
            }
            synchronized (m_perInstance) {
                m_perInstance.put(index, introduction);
            }
        }
        else {
            introduction = m_perInstance.get(index);
        }

        Object result = null;
        try {
            result = m_methods[methodIndex].invoke(introduction, parameters);
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

                s_persistenceManager.removeAll(
                        m_uuid, DeploymentModel.PER_JVM);
                s_persistenceManager.removeAll(
                        m_uuid, DeploymentModel.PER_CLASS);
                s_persistenceManager.removeAll(
                        m_uuid, DeploymentModel.PER_INSTANCE);
            }
            catch (Exception e) {
                new WrappedRuntimeException(e);
            }
        }
    }

    /**
     * Returns the memory type.
     *
     * @return the memory type
     */
    public MemoryType getMemoryType() {
        return MemoryType.PERSISTENT;
    }
}
