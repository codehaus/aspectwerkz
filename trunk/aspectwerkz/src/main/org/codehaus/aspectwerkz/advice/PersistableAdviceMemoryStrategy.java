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
package org.codehaus.aspectwerkz.advice;

import org.codehaus.aspectwerkz.DeploymentModel;
import org.codehaus.aspectwerkz.Identifiable;
import org.codehaus.aspectwerkz.MetaDataKeys;
import org.codehaus.aspectwerkz.MetaDataEnhanceable;
import org.codehaus.aspectwerkz.MemoryType;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.persistence.PersistenceManager;
import org.codehaus.aspectwerkz.persistence.PersistenceManagerFactory;
import org.codehaus.aspectwerkz.advice.AdviceMemoryStrategy;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;

/**
 * Implements a persistent version of the advice memory strategy.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: PersistableAdviceMemoryStrategy.java,v 1.2 2003-06-09 07:04:13 jboner Exp $
 */
public class PersistableAdviceMemoryStrategy extends AdviceMemoryStrategy {

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
     * Creates a new distribution strategy.
     *
     * @param prototype the advice prototype
     */
    public PersistableAdviceMemoryStrategy(final AbstractAdvice prototype) {
        super(prototype);
    }

    /**
     * Returns the advice per JVM basis.
     *
     * @param joinPoint the joint point
     * @return the advice
     */
    public Object getPerJvmAdvice(final JoinPoint joinPoint) {
        // try to get the introduction from the db
        m_perJvm = s_persistenceManager.retrieve(
                m_prototype.getName(), null, DeploymentModel.PER_JVM);

        // if null; create a new one
        if (m_perJvm == null) {
            try {
                m_perJvm = AbstractAdvice.newInstance(m_prototype);

                ((MetaDataEnhanceable)m_perJvm).___hidden$addMetaData(
                        MetaDataKeys.ASPECT_COMPONENT_UUID,
                        m_prototype.getName());

                ((MetaDataEnhanceable)m_perJvm).___hidden$addMetaData(
                        MetaDataKeys.DEPLOYMENT_MODEL,
                        new Integer(DeploymentModel.PER_JVM));

                s_persistenceManager.create(
                        m_perJvm, null, DeploymentModel.PER_JVM);
            }
            catch (Exception e) {
                throw new WrappedRuntimeException(e);
            }
        }
        return m_perJvm;
    }

    /**
     * Returns the advice per class basis.
     *
     * @param joinPoint the joint point
     * @return the advice
     */
    public Object getPerClassAdvice(final JoinPoint joinPoint) {
        Object targetObject = joinPoint.getTargetObject();
        if (targetObject == null) throw new RuntimeException("persistent perClass or perInstance advice can not be applied to static context");
        String index = ((Identifiable)targetObject).getUuid();

        Object advice = null;
        if (!m_perClass.containsKey(index)) {

            // try to get the advice from the db
            advice = s_persistenceManager.retrieve(
                    m_prototype.getName(), index, DeploymentModel.PER_CLASS);

            // if null; create a new one
            if (advice == null) {
                try {
                    advice = AbstractAdvice.newInstance(m_prototype);

                    ((MetaDataEnhanceable)advice).___hidden$addMetaData(
                            MetaDataKeys.ASPECT_COMPONENT_UUID,
                            m_prototype.getName());

                    ((MetaDataEnhanceable)advice).___hidden$addMetaData(
                            MetaDataKeys.TARGET_OBJECT_UUID, index);

                    ((MetaDataEnhanceable)advice).___hidden$addMetaData(
                            MetaDataKeys.DEPLOYMENT_MODEL,
                            new Integer(DeploymentModel.PER_CLASS));

                    s_persistenceManager.create(
                            advice, index, DeploymentModel.PER_CLASS);
                }
                catch (Exception e) {
                    throw new WrappedRuntimeException(e);
                }
            }
            synchronized (m_perClass) {
                m_perClass.put(index, advice);
            }
        }
        else {
            advice = m_perClass.get(index);
        }
        return advice;
    }

    /**
     * Returns the advice per instance basis.
     *
     * @param joinPoint the joint point
     * @return the advice
     */
    public Object getPerInstanceAdvice(final JoinPoint joinPoint) {
        Object targetObject = joinPoint.getTargetObject();
        if (targetObject == null) throw new RuntimeException("persistent perClass or perInstance advice can not be applied to static context");
        String index = ((Identifiable)targetObject).getUuid();

        Object advice = null;
        if (!m_perInstance.containsKey(index)) {
            // try to get the advice from the db
            advice = s_persistenceManager.retrieve(
                    m_prototype.getName(), index, DeploymentModel.PER_INSTANCE);

            // if null; create a new one
            if (advice == null) {
                try {
                    advice = AbstractAdvice.newInstance(m_prototype);

                    ((MetaDataEnhanceable)advice).___hidden$addMetaData(
                            MetaDataKeys.ASPECT_COMPONENT_UUID,
                            m_prototype.getName());

                    ((MetaDataEnhanceable)advice).___hidden$addMetaData(
                            MetaDataKeys.TARGET_OBJECT_UUID, index);

                    ((MetaDataEnhanceable)advice).___hidden$addMetaData(
                            MetaDataKeys.DEPLOYMENT_MODEL,
                            new Integer(DeploymentModel.PER_INSTANCE));

                    s_persistenceManager.create(
                            advice, index, DeploymentModel.PER_INSTANCE);
                }
                catch (Exception e) {
                    throw new WrappedRuntimeException(e);
                }
            }
            synchronized (m_perInstance) {
                m_perInstance.put(index, advice);
            }
        }
        else {
            advice = m_perClass.get(index);
        }
        return advice;
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
