/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.aspect;

import java.lang.reflect.InvocationTargetException;

import org.codehaus.aspectwerkz.exception.DefinitionException;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.DeploymentModel;

/**
 * Abstract base class for the mixin factory implementations.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public class DefaultMixinFactory extends AbstractMixinFactory {

    /**
     * Creates a new default mixin factory.
     *
     * @param mixinClass
     * @param deploymentModel
     */
    public DefaultMixinFactory(final Class mixinClass, final DeploymentModel deploymentModel) {
        super(mixinClass, deploymentModel);
    }

    /**
     * Creates a new perClass mixin instance.
     *
     * @param klass
     * @return the mixin instance
     */
    public Object mixinOf(final Class klass) {
        if (m_deploymentModel == DeploymentModel.PER_CLASS) {
            try {
                if (m_perClassConstructor != null) {
                    return m_perClassConstructor.newInstance(new Object[]{klass});
                } else if (m_defaultConstructor != null) {
                    return m_defaultConstructor.newInstance(new Object[]{});
                } else {
                    throw new DefinitionException(
                            "no valid constructor found for mixin [" + m_mixinClass.getName() + "]"
                    );
                }
            } catch (InvocationTargetException e) {
                throw new WrappedRuntimeException(e.getTargetException());
            } catch (Exception e) {
                throw new WrappedRuntimeException(e);
            }
        } else {
            throw new DefinitionException(
                    "Mixins.mixinOf(Class) is can not be invoked for mixin deployed using as " +
                    m_deploymentModel
            );
        }
    }

    /**
     * Creates a new perInstance mixin instance.
     *
     * @param instance
     * @return the mixin instance
     */
    public Object mixinOf(final Object instance) {
        if (m_deploymentModel == DeploymentModel.PER_INSTANCE) {
            try {
                if (m_perInstanceConstructor != null) {
                    return m_perInstanceConstructor.newInstance(new Object[]{instance});
                } else if (m_defaultConstructor != null) {
                    return m_defaultConstructor.newInstance(new Object[]{});
                } else {
                    throw new DefinitionException(
                            "no valid constructor found for mixin [" + m_mixinClass.getName() + "]"
                    );
                }
            } catch (InvocationTargetException e) {
                throw new WrappedRuntimeException(e.getTargetException());
            } catch (Exception e) {
                throw new WrappedRuntimeException(e);
            }
        } else {
            throw new DefinitionException(
                    "Mixins.mixinOf(Object) is can not be invoked for mixin deployed using as " +
                    m_deploymentModel
            );
        }
    }
}