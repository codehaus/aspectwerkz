/**************************************************************************************
 * Copyright (c) Jonas BonŽr, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.aspect.management;

import org.codehaus.aspectwerkz.aspect.AspectContainer;
import org.codehaus.aspectwerkz.aspect.CFlowSystemAspect;
import org.codehaus.aspectwerkz.aspect.DefaultAspectContainerStrategy;
import org.codehaus.aspectwerkz.DeploymentModel;
import org.codehaus.aspectwerkz.AspectContext;
import org.codehaus.aspectwerkz.ContextClassLoader;
import org.codehaus.aspectwerkz.definition.AspectDefinition;
import org.codehaus.aspectwerkz.definition.SystemDefinition;
import org.codehaus.aspectwerkz.definition.SystemDefinitionContainer;
import org.codehaus.aspectwerkz.exception.DefinitionException;

import java.util.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Manages the aspects, registry for the aspect containers (one container per aspect type).
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class Aspects {

    /**
     * The default aspect container class.
     */
    public static final String DEFAULT_ASPECT_CONTAINER = DefaultAspectContainerStrategy.class.getName();

    /**
     * Map with all the aspect containers mapped to the aspects class
     */
    private static final Map ASPECT_CONTAINERS = new WeakHashMap();

    /**
     * Returns the aspect container for the aspect with the given name.
     *
     * @param klass the class of the aspect
     * @return the container, put in cache based on aspect class as a key
     */
    public static AspectContainer getContainer(final Class klass) {
        synchronized (ASPECT_CONTAINERS) {
            AspectContainer container = (AspectContainer) ASPECT_CONTAINERS.get(klass);
            if (container == null) {
                container = createAspectContainer(klass);
                //FIXME support for aspect reused accross systems with different params etc
                //by using a lookup by uuid/aspectNickName
                // right now broken since we have 1 container per aspect CLASS while the definition
                // does allow for some mix (several aspect, several container, same aspect class)
                ASPECT_CONTAINERS.put(klass, container);
            }
            return container;
        }
    }

    /**
     * Returns the singleton aspect instance for the aspect with the given name.
     *
     * @param name the name of the aspect
     * @return the singleton aspect instance
     */
    public static Object aspectOf(final String name) {
        try {
            Class aspectClass = Class.forName(name, false, Thread.currentThread().getContextClassLoader());
            return aspectOf(aspectClass);
        } catch (ClassNotFoundException e) {
            // try to guess it from the system definitions if we have a uuid prefix
            String className = lookupAspectClassName(Thread.currentThread().getContextClassLoader(), name);
            if (className != null) {
                return aspectOf(className);
            } else {
                throw new Error(
                        "Could not load aspect " + name + " from " + Thread.currentThread().getContextClassLoader()
                );
            }
        }
    }

    /**
     * Returns the singleton aspect instance for the aspect with the given name.
     *
     * @param aspectClass the class of the aspect
     * @return the singleton aspect instance
     */
    public static Object aspectOf(final Class aspectClass) {
        return getContainer(aspectClass).aspectOf();
    }

    /**
     * Returns the per class aspect instance for the aspect with the given name for the perTarget model
     *
     * @param name   the name of the aspect
     * @param target the target class
     * @return the per class aspect instance
     */
    public static Object aspectOf(final String name, final Class target) {
        try {
            Class aspectClass = Class.forName(name, false, target.getClassLoader());
            return aspectOf(aspectClass, target);
        } catch (ClassNotFoundException e) {
            // try to guess it from the system definitions if we have a uuid prefix
            String className = lookupAspectClassName(Thread.currentThread().getContextClassLoader(), name);
            if (className != null) {
                return aspectOf(className);
            } else {
                throw new Error("Could not load aspect " + name + " from " + target.getClassLoader());
            }
        }
    }

    /**
     * Returns the per class aspect instance for the aspect with the given name for the perTarget model
     *
     * @param aspectClass the name of the aspect
     * @param target      the target class
     * @return the per class aspect instance
     */
    public static Object aspectOf(final Class aspectClass, final Class target) {
        return getContainer(aspectClass).aspectOf(target);
    }

    /**
     * Creates a new aspect container.
     *
     * @param aspectClass the aspect class
     */
    private static AspectContainer createAspectContainer(final Class aspectClass) {
        AspectDefinition aspectDefinition = null;

        Set definitions = SystemDefinitionContainer.getRegularAndVirtualDefinitionsFor(aspectClass.getClassLoader());
        for (Iterator iterator = definitions.iterator(); iterator.hasNext() && aspectDefinition == null;) {
            SystemDefinition systemDefinition = (SystemDefinition) iterator.next();
            for (Iterator iterator1 = systemDefinition.getAspectDefinitions().iterator(); iterator1.hasNext();) {
                AspectDefinition aspectDef = (AspectDefinition) iterator1.next();
                if (aspectClass.getName().replace('/', '.').equals(aspectDef.getClassName())) {
                    aspectDefinition = aspectDef;
                    break;
                }
            }
        }
        if (aspectDefinition == null) {
            throw new Error("Could not find AspectDefinition for " + aspectClass.getName());
        }

        String containerClassName = aspectDefinition.getContainerClassName();
        try {
            Class containerClass;
            if (containerClassName == null ||
                aspectClass.getName().equals(CFlowSystemAspect.CLASS_NAME)) {
                containerClass = ContextClassLoader.loadClass(aspectClass.getClassLoader(), DEFAULT_ASPECT_CONTAINER);
            } else {
                containerClass = ContextClassLoader.loadClass(aspectClass.getClassLoader(), containerClassName);
            }
            Constructor constructor = containerClass.getConstructor(new Class[]{AspectContext.class});
            final AspectContext aspectContext = new AspectContext(
                    aspectDefinition.getSystemDefinition().getUuid(),
                    aspectClass,
                    aspectDefinition.getName(),
                    DeploymentModel.getDeploymentModelAsInt(aspectDefinition.getDeploymentModel()),
                    aspectDefinition,
                    aspectDefinition.getParameters()
            );
            final AspectContainer container = (AspectContainer) constructor.newInstance(new Object[]{aspectContext});
            aspectContext.setContainer(container);
            return container;
        } catch (InvocationTargetException e) {
            throw new DefinitionException(e.getTargetException().toString());
        } catch (NoSuchMethodException e) {
            throw new DefinitionException(
                    "aspect container does not have a valid constructor ["
                    + containerClassName
                    + "] need to take an AspectContext instance as its only parameter: "
                    + e.toString()
            );
        } catch (Throwable e) {
            StringBuffer cause = new StringBuffer();
            cause.append("could not create aspect container using the implementation specified [");
            cause.append(containerClassName);
            cause.append("] due to: ");
            cause.append(e.toString());
            e.printStackTrace();
            throw new DefinitionException(cause.toString());
        }
    }

    /**
     * Looks up the aspect class name, based on the qualified name of the aspect.
     *
     * @param loader
     * @param qualifiedName
     * @return
     */
    private static String lookupAspectClassName(final ClassLoader loader, final String qualifiedName) {
        if (qualifiedName.indexOf('/') <= 0) {
            // no uuid
            return null;
        }

        final Set definitionsBottomUp = SystemDefinitionContainer.getRegularAndVirtualDefinitionsFor(loader);
        // TODO: bottom up is broken now
        //Collections.reverse(definitionsBottomUp);

        for (Iterator iterator = definitionsBottomUp.iterator(); iterator.hasNext();) {
            SystemDefinition definition = (SystemDefinition) iterator.next();
            for (Iterator iterator1 = definition.getAspectDefinitions().iterator(); iterator1.hasNext();) {
                AspectDefinition aspectDefinition = (AspectDefinition) iterator1.next();
                if (qualifiedName.equals(aspectDefinition.getQualifiedName())) {
                    return aspectDefinition.getClassName();
                }
            }
        }
        return null;
    }

    /**
     * Class is non-instantiable.
     */
    private Aspects() {
    }
}
