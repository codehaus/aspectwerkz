/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.aspect.management;

import org.codehaus.aspectwerkz.aspect.DefaultMixinFactory;
import org.codehaus.aspectwerkz.aspect.MixinFactory;
import org.codehaus.aspectwerkz.ContextClassLoader;
import org.codehaus.aspectwerkz.definition.SystemDefinition;
import org.codehaus.aspectwerkz.definition.SystemDefinitionContainer;
import org.codehaus.aspectwerkz.definition.MixinDefinition;
import org.codehaus.aspectwerkz.exception.DefinitionException;

import java.util.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Manages the mixins, registry for the mixin factories (one factory per mixin type).
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r </a>
 */
public class Mixins {

    /**
     * The default mixin factory class.
     */
    public static final String DEFAULT_MIXIN_FACTORY = DefaultMixinFactory.class.getName();

    /**
     * Map with all the mixin factories mapped to the mixin class
     */
    private static final Map MIXIN_FACTORIES = new WeakHashMap();

    /**
     * Returns the mixin factory for the mixin with the given name.
     *
     * @param klass the class of the mixin
     * @return the factory, put in cache based on mixin class as a key
     */
    public static MixinFactory getFactory(final Class klass) {
        synchronized (MIXIN_FACTORIES) {
            MixinFactory factory = (MixinFactory) MIXIN_FACTORIES.get(klass);
            if (factory == null) {
                factory = createMixinFactory(klass);
                //by using a lookup by uuid/aspectNickName
                // right now broken since we have 1 container per mixin CLASS while the definition
                // does allow for some mix (several mixin, several container, same mixin class)
                MIXIN_FACTORIES.put(klass, factory);
            }
            return factory;
        }
    }

    /**
     * Returns the per class mixin instance for the mixin with the given name for the perClass model
     *
     * @param name        the name of the mixin
     * @param targetClass the targetClass class
     * @return the per class mixin instance
     */
    public static Object mixinOf(final String name, final Class targetClass) {
        try {
            Class mixinClass = Class.forName(name, false, targetClass.getClassLoader());
            return mixinOf(mixinClass, targetClass);
        } catch (ClassNotFoundException e) {
            throw new Error("could not load mixin " + name + " from " + targetClass.getClassLoader());
        }
    }

    /**
     * Returns the per class mixin instance for the mixin with the given implemnentation class
     * deployed using the perClass model.
     *
     * @param mixinClass  the name of the mixin
     * @param targetClass the targetClass class
     * @return the per class mixin instance
     */
    public static Object mixinOf(final Class mixinClass, final Class targetClass) {
        return getFactory(mixinClass).mixinOf(targetClass);
    }

    /**
     * Returns the per targetClass instance mixin instance for the mixin with the given name for the perInstance model.
     *
     * @param name           the name of the mixin
     * @param targetInstance the targetClass instance, can be null (static method, ctor call)
     * @return the per instance mixin instance, fallback on perClass if targetInstance is null
     */
    public static Object mixinOf(final String name, final Object targetInstance) {
        try {
            Class mixinClass = Class.forName(name, false, targetInstance.getClass().getClassLoader());
            return mixinOf(mixinClass, targetInstance);
        } catch (ClassNotFoundException e) {
            throw new Error("could not load mixin " + name + " from " + targetInstance.getClass().getClassLoader());
        }
    }

    /**
     * Returns the per class mixin instance for the mixin with the given implemnentation class
     * deployed using the perClass model.
     *
     * @param mixinClass     the name of the mixin
     * @param targetInstance the targetClass instance, can be null
     * @return the per targetClass instance mixin instance, fallback to perClass if targetInstance is null
     */
    public static Object mixinOf(final Class mixinClass, final Object targetInstance) {
        return getFactory(mixinClass).mixinOf(targetInstance);
    }

    /**
     * Creates a new mixin factory.
     *
     * @param mixinClass the mixin class
     */
    private static MixinFactory createMixinFactory(final Class mixinClass) {
        MixinDefinition mixinDefinition = null;

        Set definitions = SystemDefinitionContainer.getRegularAndVirtualDefinitionsFor(mixinClass.getClassLoader());
        for (Iterator iterator = definitions.iterator(); iterator.hasNext() && mixinDefinition == null;) {
            SystemDefinition systemDefinition = (SystemDefinition) iterator.next();
            for (Iterator iterator1 = systemDefinition.getMixinDefinitions().iterator(); iterator1.hasNext();) {
                MixinDefinition mixinDef = (MixinDefinition) iterator1.next();
                if (mixinClass.getName().replace('/', '.').equals(mixinDef.getMixinImpl().getName())) {
                    mixinDefinition = mixinDef;
                    break;
                }
            }
        }
        if (mixinDefinition == null) {
            throw new Error("could not find definition for mixin: " + mixinClass.getName());
        }

        String factoryClassName = mixinDefinition.getFactoryClassName();
        try {
            Class containerClass;
            if (factoryClassName == null) {
                containerClass = ContextClassLoader.loadClass(mixinClass.getClassLoader(), DEFAULT_MIXIN_FACTORY);
            } else {
                containerClass = ContextClassLoader.loadClass(mixinClass.getClassLoader(), factoryClassName);
            }
            Constructor constructor = containerClass.getConstructor(new Class[]{Class.class, String.class});
            final MixinFactory factory = (MixinFactory) constructor.newInstance(
                    new Object[]{mixinClass, mixinDefinition.getDeploymentModel()}
            );
            return factory;
        } catch (InvocationTargetException e) {
            throw new DefinitionException(e.getTargetException().toString());
        } catch (NoSuchMethodException e) {
            throw new DefinitionException(
                    "mixin container does not have a valid constructor ["
                    + factoryClassName
                    + "] need to take an AspectContext instance as its only parameter: "
                    + e.toString()
            );
        } catch (Throwable e) {
            StringBuffer cause = new StringBuffer();
            cause.append("could not create mixin container using the implementation specified [");
            cause.append(factoryClassName);
            cause.append("] due to: ");
            cause.append(e.toString());
            e.printStackTrace();
            throw new DefinitionException(cause.toString());
        }
    }

    /**
     * Class is non-instantiable.
     */
    private Mixins() {
    }
}
