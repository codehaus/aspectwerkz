/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.aspect;

import java.lang.reflect.Constructor;

import org.codehaus.aspectwerkz.exception.DefinitionException;
import org.codehaus.aspectwerkz.DeploymentModel;

/**
 * Abstract base class for the mixin container implementations.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public abstract class AbstractMixinFactory implements MixinFactory {

    protected final Class m_mixinClass;
    protected final int m_deploymentModel;
    protected Constructor m_defaultConstructor;
    protected Constructor m_perClassConstructor;
    protected Constructor m_perInstanceConstructor;

    public AbstractMixinFactory(final Class mixinClass, final String deploymentModel) {
        m_mixinClass = mixinClass;
        m_deploymentModel = DeploymentModel.getDeploymentModelAsInt(deploymentModel);
        try {
            if (m_deploymentModel == DeploymentModel.PER_CLASS) {
                m_perClassConstructor = m_mixinClass.getConstructor(new Class[]{Class.class});
            } else if (m_deploymentModel == DeploymentModel.PER_INSTANCE) {
                m_perInstanceConstructor = m_mixinClass.getConstructor(new Class[]{Object.class});
            } else {
                throw new DefinitionException(
                        "deployment model for [" + m_mixinClass.getName() + "] is not supported [" +
                        DeploymentModel.getDeploymentModelAsString(m_deploymentModel) + "]"
                );
            }
        } catch (NoSuchMethodException e1) {
            try {
                m_defaultConstructor = m_mixinClass.getConstructor(new Class[]{});
            } catch (NoSuchMethodException e2) {
                throw new DefinitionException(
                        "mixin [" + m_mixinClass.getName() +
                        "] does not have a constructor that matches with its deployment model or a non-argument default constructor"
                );
            }
        }
    }

    /**
     * Creates a new perClass mixin instance.
     *
     * @param klass
     * @return the mixin instance
     */
    public abstract Object mixinOf(Class klass);

    /**
     * Creates a new perInstance mixin instance.
     *
     * @param instance
     * @return the mixin instance
     */
    public abstract Object mixinOf(Object instance);
}