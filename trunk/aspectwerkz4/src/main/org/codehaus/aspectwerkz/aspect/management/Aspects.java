/**************************************************************************************
 * Copyright (c) Jonas BonŽr, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.aspect.management;

import org.codehaus.aspectwerkz.aspect.AspectContainer;
import org.codehaus.aspectwerkz.aspect.DefaultAspectContainerStrategy;
import org.codehaus.aspectwerkz.AspectContext;
import org.codehaus.aspectwerkz.util.ContextClassLoader;
import org.codehaus.aspectwerkz.definition.AspectDefinition;
import org.codehaus.aspectwerkz.definition.SystemDefinition;
import org.codehaus.aspectwerkz.definition.SystemDefinitionContainer;
import org.codehaus.aspectwerkz.exception.DefinitionException;

import java.util.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import gnu.trove.TIntObjectHashMap;

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
     * Map of TIntHashMap, whose key is containerClass. The TIntHashMap maps container instance, whith qName.hashCode()
     * as a key.
     * TODO: still not perfect: uuid / qName handled correctly, but when DefaultContainer is used,
     * we end up in having one entry with a list that strong refs the container instances
     * ie leaks since the DefaultContainer leaves in system CL.
     */
    private static Map ASPECT_CONTAINER_LISTS = new WeakHashMap();

    /**
     * Returns the aspect container class for the given aspect class qName.
     * The qName is returned since we may have only the aspect class name upon lookup
     *
     * @param visibleFrom class loader to look from
     * @param qName
     * @return the container class
     */
    public static String[] getAspectQNameContainerClassName(final ClassLoader visibleFrom, final String qName) {
        AspectDefinition aspectDefinition = lookupAspectDefinition(visibleFrom, qName);
        return new String[]{aspectDefinition.getQualifiedName(), aspectDefinition.getContainerClassName()};
    }

    /**
     * Returns or create the aspect container for the given container class with the given aspect qualified name
     *
     * @param visibleFrom class loader hosting the advised class ie from where all is visible
     * @param containerClass
     * @param qName
     * @return
     */
    public static AspectContainer getContainerQNamed(final ClassLoader visibleFrom, final Class containerClass, final String qName) {
        synchronized (ASPECT_CONTAINER_LISTS) {
            TIntObjectHashMap containers = (TIntObjectHashMap) ASPECT_CONTAINER_LISTS.get(containerClass);
            if (containers == null) {
                containers = new TIntObjectHashMap();
                ASPECT_CONTAINER_LISTS.put(containerClass, containers);
            }
            AspectContainer container = (AspectContainer) containers.get(qName.hashCode());
            if (container == null) {
                container = createAspectContainer(visibleFrom, containerClass, qName);
                containers.put(qName.hashCode(), container);
            }
            return container;
        }
    }

    /**
     * Returns the singleton aspect instance for the aspect with the given qualified name.
     * The aspect is looked up from the thread context classloader.
     *
     * @param qName the qualified name of the aspect
     * @return the singleton aspect instance
     */
    public static Object aspectOf(final String qName) {
        return aspectOf(Thread.currentThread().getContextClassLoader(), qName);
    }

    /**
     * Returns the singleton aspect instance for the given aspect class.
     * Consider using aspectOf(visibleFrom, qName) if the aspect is used more than once
     * or if it is used in a class loader which is child of its own classloader.
     *
     * @param aspectClass the class of the aspect
     * @return the singleton aspect instance
     */
    public static Object aspectOf(final Class aspectClass) {
        String aspectClassName = aspectClass.getName().replace('/', '.');
        return aspectOf(aspectClass.getClassLoader(), aspectClassName);
    }

    /**
     * Returns the singleton aspect instance for given aspect qName, with visibility from the given class loader
     *
     * @param visibleFrom the class loader from where aspect is visible, likely to be the class loader of the
     * advised classes, or the one where the system hosting the aspect is deployed.
     * @return the singleton aspect instance
     */
    public static Object aspectOf(final ClassLoader visibleFrom, final String qName) {
        String[] qNameContainerClassName = getAspectQNameContainerClassName(visibleFrom, qName);
        return aspect$Of(visibleFrom, qNameContainerClassName[0], qNameContainerClassName[1]);
    }

    /**
     * Returns the per class aspect attached to targetClass
     * Consider using aspectOf(qName, targetClass) if the aspect is used more than once
     *
     * @param aspectClass the name of the aspect
     * @param targetClass the target class
     * @return the per class aspect instance
     */
    public static Object aspectOf(final Class aspectClass, final Class targetClass) {
        String aspectClassName = aspectClass.getName().replace('/', '.');
        return aspectOf(aspectClassName, targetClass);
    }

    /**
     * Returns the per class aspect instance for the aspect with the given qualified name for targetClass
     *
     * @param qName        the qualified name of the aspect
     * @param targetClass the target class
     * @return the per class aspect instance
     */
    public static Object aspectOf(final String qName, final Class targetClass) {
        // look up from the targetClass loader is enough in that case
        String[] qNameContainerClassName = getAspectQNameContainerClassName(targetClass.getClassLoader(), qName);
        return aspect$Of(qNameContainerClassName[0], qNameContainerClassName[1], targetClass);
    }

    /**
     * Returns the per instance aspect attached to targetInstance
     * Consider using aspectOf(qName, targetInstance) if the aspect is used more than once
     *
     * @param aspectClass the name of the aspect
     * @param targetInstance the target instance
     * @return the per class aspect instance
     */
    public static Object aspectOf(final Class aspectClass, final Object targetInstance) {
        String aspectClassName = aspectClass.getName().replace('/', '.');
        return aspectOf(aspectClassName, targetInstance);
    }

    /**
     * Returns the per instance aspect attached to targetInstance
     *
     * @param qName the qualified name of the aspect
     * @param targetInstance the target instance
     * @return the per class aspect instance
     */
    public static Object aspectOf(final String qName, final Object targetInstance) {
        // look up from the targetInstance loader is enough in that case
        String[] qNameContainerClassName = getAspectQNameContainerClassName(targetInstance.getClass().getClassLoader(), qName);
        return aspect$Of(qNameContainerClassName[0], qNameContainerClassName[1], targetInstance);
    }

    //---------- weaver exposed

    public static Object aspect$Of(ClassLoader loader, String qName, String containerClassName) {
        try {
            Class containerClass = ContextClassLoader.forName(loader, containerClassName);
            return getContainerQNamed(loader, containerClass, qName).aspectOf();
        } catch (Throwable t) {
            throw new NoAspectBoundException(t, qName);
        }
    }

    public static Object aspect$Of(String qName, String containerClassName, final Class perClass) {
        try {
            ClassLoader loader = perClass.getClassLoader();
            Class containerClass = ContextClassLoader.forName(loader, containerClassName);
            return getContainerQNamed(loader, containerClass, qName).aspectOf(perClass);
        } catch (Throwable t) {
            throw new NoAspectBoundException(t, qName);
        }
    }

    public static Object aspect$Of(String qName, String containerClassName, final Object perInstance) {
        try {
            ClassLoader loader = perInstance.getClass().getClassLoader();
            Class containerClass = ContextClassLoader.forName(loader, containerClassName);
            return getContainerQNamed(loader, containerClass, qName).aspectOf(perInstance);
        } catch (Throwable t) {
            throw new NoAspectBoundException(t, qName);
        }
    }




    //---------- helpers

    /**
     * Creates a new aspect container.
     *
     * @param visibleFrom class loader of the advised class from all is visible
     * @param containerClass the container class
     * @param qName the aspect qualified name
     */
    private static AspectContainer createAspectContainer(final ClassLoader visibleFrom, final Class containerClass, final String qName) {
        AspectDefinition aspectDefinition = lookupAspectDefinition(visibleFrom, qName);

        Class aspectClass = null;
        try {
            aspectClass = ContextClassLoader.forName(visibleFrom, aspectDefinition.getClassName());
        } catch (Throwable t) {
            throw new NoAspectBoundException(t, qName);
        }

        try {
            Constructor constructor = containerClass.getConstructor(new Class[]{AspectContext.class});
            final AspectContext aspectContext = new AspectContext(
                    aspectDefinition.getSystemDefinition().getUuid(),
                    aspectClass,
                    aspectDefinition.getName(),
                    aspectDefinition.getDeploymentModel(),
                    aspectDefinition,
                    aspectDefinition.getParameters()
            );
            final AspectContainer container = (AspectContainer) constructor.newInstance(new Object[]{aspectContext});
            aspectContext.setContainer(container);
            return container;
        } catch (InvocationTargetException e) {
            throw new NoAspectBoundException(e, qName);
        } catch (NoSuchMethodException e) {
            throw new NoAspectBoundException(
                    "AspectContainer does not have a valid constructor ["
                    + containerClass.getName()
                    + "] need to take an AspectContext instance as its only parameter: "
                    + e.toString(),
                    qName
            );
        } catch (Throwable e) {
            StringBuffer cause = new StringBuffer();
            cause.append("Could not create AspectContainer using the implementation specified [");
            cause.append(containerClass.getName());
            cause.append("] for ").append(qName);
            throw new NoAspectBoundException(e, cause.toString());
        }
    }

    /**
     * Lookup the aspect definition with the given qName, visible from the given loader.
     * If qName is a class name only, the fallback will ensure only one aspect use is found.
     *
     * @param visibleFrom
     * @param qName
     * @return
     */
    private static AspectDefinition lookupAspectDefinition(final ClassLoader visibleFrom, final String qName) {
        AspectDefinition aspectDefinition = null;

        Set definitions = SystemDefinitionContainer.getDefinitionsFor(visibleFrom);
        if (qName.indexOf('/')>0) {
            // has system uuid ie real qName
            for (Iterator iterator = definitions.iterator(); iterator.hasNext();) {
                SystemDefinition systemDefinition = (SystemDefinition) iterator.next();
                for (Iterator iterator1 = systemDefinition.getAspectDefinitions().iterator(); iterator1.hasNext();) {
                    AspectDefinition aspectDef = (AspectDefinition) iterator1.next();
                    if (qName.equals(aspectDef.getQualifiedName())) {
                        aspectDefinition = aspectDef;
                        break;
                    }
                }
            }
        } else {
            // fallback on class name lookup
            // must find at most one
            int found = 0;
            for (Iterator iterator = definitions.iterator(); iterator.hasNext();) {
                SystemDefinition systemDefinition = (SystemDefinition) iterator.next();
                for (Iterator iterator1 = systemDefinition.getAspectDefinitions().iterator(); iterator1.hasNext();) {
                    AspectDefinition aspectDef = (AspectDefinition) iterator1.next();
                    if (qName.equals(aspectDef.getClassName())) {
                        aspectDefinition = aspectDef;
                        found++;
                    }
                }
            }
            if (found > 1) {
                throw new NoAspectBoundException("More than one AspectDefinition found, consider using other API methods", qName);
            }

        }

        if (aspectDefinition == null) {
            throw new NoAspectBoundException("Could not find AspectDefinition", qName);
        }

        return aspectDefinition;
    }

//    /**
//     * Looks up the aspect class name, based on the qualified name of the aspect.
//     *
//     * @param loader
//     * @param qualifiedName
//     * @return
//     */
//    private static String lookupAspectClassName(final ClassLoader loader, final String qualifiedName) {
//        if (qualifiedName.indexOf('/') <= 0) {
//            // no uuid
//            return null;
//        }
//
//        final Set definitionsBottomUp = SystemDefinitionContainer.getDefinitionsFor(loader);
//        // TODO: bottom up is broken now
//        //Collections.reverse(definitionsBottomUp);
//
//        for (Iterator iterator = definitionsBottomUp.iterator(); iterator.hasNext();) {
//            SystemDefinition definition = (SystemDefinition) iterator.next();
//            for (Iterator iterator1 = definition.getAspectDefinitions().iterator(); iterator1.hasNext();) {
//                AspectDefinition aspectDefinition = (AspectDefinition) iterator1.next();
//                if (qualifiedName.equals(aspectDefinition.getQualifiedName())) {
//                    return aspectDefinition.getClassName();
//                }
//            }
//        }
//        return null;
//    }

    /**
     * Class is non-instantiable.
     */
    private Aspects() {
    }
}
