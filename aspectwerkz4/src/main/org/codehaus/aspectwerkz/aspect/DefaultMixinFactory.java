/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.aspect;

import java.lang.reflect.InvocationTargetException;
import java.util.WeakHashMap;
import java.util.Map;

import org.codehaus.aspectwerkz.exception.DefinitionException;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.DeploymentModel;

/**
 * Abstract base class for the mixin factory implementations.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r </a>
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class DefaultMixinFactory extends AbstractMixinFactory {

    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    private Object m_perJVM = null;

    private Map m_perClassMixins = new WeakHashMap();

    private Map m_perInstanceMixins = new WeakHashMap();

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
     * Creates a new perJVM mixin instance.
     *
     * @return the mixin instance
     */
    public Object mixinOf() {
        if (m_perJVM != null) {
            return m_perJVM;
        }
        synchronized (this) {
            final Object mixin;
            if (m_deploymentModel == DeploymentModel.PER_JVM) {
                try {
                    mixin = m_defaultConstructor.newInstance(EMPTY_OBJECT_ARRAY);
                } catch (InvocationTargetException e) {
                    throw new WrappedRuntimeException(e.getTargetException());
                } catch (Exception e) {
                    throw new WrappedRuntimeException(e);
                }
            } else {
                throw new DefinitionException(
                        "Mixins.mixinOf() is can not be invoked for mixin deployed using as " +
                        m_deploymentModel
                );
            }
            m_perJVM = mixin;
        }
        return m_perJVM;
    }

    /**
     * Creates a new perClass mixin instance.
     *
     * @param klass
     * @return the mixin instance
     */
    public Object mixinOf(final Class klass) {
        if (m_perClassMixins.containsKey(klass)) {
            return m_perClassMixins.get(klass);
        }
        synchronized (m_perClassMixins) {
            if (!m_perClassMixins.containsKey(klass)) {
                final Object mixin;
                if (m_deploymentModel == DeploymentModel.PER_CLASS) {
                    try {
                        if (m_perClassConstructor != null) {
                            mixin = m_perClassConstructor.newInstance(new Object[]{klass});
                        } else if (m_defaultConstructor != null) {
                            mixin = m_defaultConstructor.newInstance(new Object[]{});
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
                m_perClassMixins.put(klass, mixin);
            }
            return m_perClassMixins.get(klass);
        }
    }

    /**
     * Creates a new perInstance mixin instance.
     *
     * @param instance
     * @return the mixin instance
     */
    public Object mixinOf(final Object instance) {
        if (m_perInstanceMixins.containsKey(instance)) {
            return m_perInstanceMixins.get(instance);
        }
        synchronized (m_perInstanceMixins) {
            if (!m_perInstanceMixins.containsKey(instance)) {
                final Object mixin;
                if (m_deploymentModel == DeploymentModel.PER_INSTANCE) {
                    try {
                        if (m_perInstanceConstructor != null) {
                            mixin = m_perInstanceConstructor.newInstance(new Object[]{instance});
                        } else if (m_defaultConstructor != null) {
                            mixin = m_defaultConstructor.newInstance(new Object[]{});
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
                m_perInstanceMixins.put(instance, mixin);
            }
            return m_perInstanceMixins.get(instance);
        }
    }
}