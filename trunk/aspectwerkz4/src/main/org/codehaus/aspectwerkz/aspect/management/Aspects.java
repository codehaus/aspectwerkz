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
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

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
     * @param aspectClass the class of the aspect
     * @return the container, put in cache based on aspect class as a key
     */
    public static AspectContainer getContainer(final Class aspectClass) {
        return getContainerQNamed(aspectClass, null);
    }

    private static AspectContainer getContainerQNamed(final Class aspectClass, final String qName) {
        ContainerKey key = ContainerKey.get(aspectClass, qName);
        synchronized (ASPECT_CONTAINERS) {
            AspectContainer container = (AspectContainer) ASPECT_CONTAINERS.get(key);
            if (container == null) {
                container = createAspectContainer(aspectClass, qName);
                ASPECT_CONTAINERS.put(key, container);
            }
            return container;
        }

    }

    /**
     * Returns the singleton aspect instance for the aspect with the given qualified name.
     *
     * @param qName the qualified name of the aspect
     * @return the singleton aspect instance
     */
    public static Object aspectOf(final String qName) {
        return aspectOf(Thread.currentThread().getContextClassLoader(), qName);
    }

    /**
     * Returns the singleton aspect instance for the aspect with the given qualified name.
     *
     * @param loader the classloader to look from
     * @param qName the qualified name of the aspect
     * @return the singleton aspect instance
     */
    public static Object aspectOf(final ClassLoader loader, final String qName) {
        try {
            Class aspectClass = ContextClassLoader.forName(loader, qName);
            return aspectOfQNamed(aspectClass, qName);
        } catch (ClassNotFoundException e) {
            // try to guess it from the system definitions if we have a uuid prefix
            String className = lookupAspectClassName(loader, qName);
            if (className != null) {
                try {
                    Class aspectClass = ContextClassLoader.forName(loader, className);
                    return aspectOfQNamed(aspectClass, qName);
                } catch (ClassNotFoundException ee) {
                    throw new Error("Could not load aspect " + qName + " from " + loader);
                }
            } else {
                // can occur when the jointpoint target is a rt.jar class
                // f.e. System.out and field get since loader will be null
                // in such a case, trim the uuid...
                int index = qName.lastIndexOf("/");
                if (index > 0) {
                    className = qName.substring(index+1);
                    try {
                        Class aspectClass = ContextClassLoader.forName(loader, className);
                        return aspectOfQNamed(aspectClass, qName);
                    } catch (ClassNotFoundException ee) {
                        throw new Error("Could not load aspect " + qName + " from " + loader);
                    }
                } else {
                    throw new Error("Could not load aspect " + qName + " from " + loader);
                }
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

    private static Object aspectOfQNamed(final Class aspectClass, final String qName) {
        return getContainerQNamed(aspectClass, qName).aspectOf();
    }

    /**
     * Returns the per class aspect instance for the aspect with the given qualified name for the perTarget model
     *
     * @param qName        the qualified name of the aspect
     * @param targetClass the targetClass class
     * @return the per class aspect instance
     */
    public static Object aspectOf(final String qName, final Class targetClass) {
        try {
            Class aspectClass = ContextClassLoader.forName(targetClass.getClassLoader(), qName);
            return aspectOf(aspectClass, targetClass);
        } catch (ClassNotFoundException e) {
            // try to guess it from the system definitions if we have a uuid prefix
            String className = lookupAspectClassName(targetClass.getClassLoader(), qName);
            if (className != null) {
                try {
                    Class aspectClass = ContextClassLoader.forName(targetClass.getClassLoader(), className);
                    return aspectOfQNamed(aspectClass, qName, targetClass);
                } catch (ClassNotFoundException e2) {
                    throw new Error("Could not load aspect " + qName + " from " + targetClass.getClassLoader());
                }
            } else {
                throw new Error("Could not load aspect " + qName + " from " + targetClass.getClassLoader());
            }
        }
    }

    /**
     * Returns the per class aspect instance for the aspect with the given name for the perTarget model
     *
     * @param aspectClass the name of the aspect
     * @param targetClass the targetClass class
     * @return the per class aspect instance
     */
    public static Object aspectOf(final Class aspectClass, final Class targetClass) {
        return getContainer(aspectClass).aspectOf(targetClass);
    }
    private static Object aspectOfQNamed(final Class aspectClass, final String qName, final Class targetClass) {
        return getContainerQNamed(aspectClass, qName).aspectOf(targetClass);
    }

    /**
     * Returns the per targetClass instance aspect instance for the aspect with the given qualified name for the perTarget model
     *
     * @param qName           the qualified name of the aspect
     * @param targetInstance the targetClass instance, can be null (static method, ctor call)
     * @return the per instance aspect instance, fallback on perClass if targetInstance is null
     */
    public static Object aspectOf(final String qName, final Object targetInstance) {
        try {
            Class aspectClass = ContextClassLoader.forName(targetInstance.getClass().getClassLoader(), qName);
            return aspectOf(aspectClass, targetInstance);
        } catch (ClassNotFoundException e) {
            // try to guess it from the system definitions if we have a uuid prefix
            String className = lookupAspectClassName(targetInstance.getClass().getClassLoader(), qName);
            if (className != null) {
                try {
                    Class aspectClass = ContextClassLoader.forName(targetInstance.getClass().getClassLoader(), qName);
                    return aspectOfQNamed(aspectClass, qName, targetInstance);
                } catch (ClassNotFoundException e2) {
                    throw new Error(
                            "Could not load aspect " + qName + " from " + targetInstance.getClass().getClassLoader()
                    );
                }
            } else {
                throw new Error(
                        "Could not load aspect " + qName + " from " + targetInstance.getClass().getClassLoader()
                );
            }
        }
    }

    /**
     * Returns the per targetClass instance aspect instance for the aspect with the given name for the perTarget model
     *
     * @param aspectClass    the name of the aspect
     * @param targetInstance the targetClass instance, can be null
     * @return the per targetClass instance aspect instance, fallback to perClass if targetInstance is null
     */
    public static Object aspectOf(final Class aspectClass, final Object targetInstance) {
        return getContainer(aspectClass).aspectOf(targetInstance);
    }
    private static Object aspectOfQNamed(final Class aspectClass, final String qName, final Object targetInstance) {
        return getContainerQNamed(aspectClass, qName).aspectOf(targetInstance);
    }

    /**
     * Creates a new aspect container.
     *
     * @param aspectClass the aspect class
     * @param qName the aspect qualified name or null
     */
    private static AspectContainer createAspectContainer(final Class aspectClass, final String qName) {
        AspectDefinition aspectDefinition = null;

        Set definitions = SystemDefinitionContainer.getDefinitionsFor(aspectClass.getClassLoader());
        int found = 0;
        for (Iterator iterator = definitions.iterator(); iterator.hasNext() && aspectDefinition == null;) {
            SystemDefinition systemDefinition = (SystemDefinition) iterator.next();
            for (Iterator iterator1 = systemDefinition.getAspectDefinitions().iterator(); iterator1.hasNext();) {
                AspectDefinition aspectDef = (AspectDefinition) iterator1.next();
                // if no qName, lookup is made on aspectClass name and me must find only one
                if (qName == null) {
                    if (aspectClass.getName().replace('/', '.').equals(aspectDef.getClassName())) {
                        if (found == 0) {
                            // keep the first def
                            aspectDefinition = aspectDef;
                        }
                        found++;
                    }
                } else {
                    if (qName.equals(aspectDef.getQualifiedName())) {
                        aspectDefinition = aspectDef;
                        break;
                    }
                }
            }
        }

        if (qName == null && found > 1) {
            throw new Error("Could not find AspectDefinition for " + aspectClass.getName()
                            + " using unqualified name. Found " + found + " definitions");
        }

        if (aspectDefinition == null) {
            throw new Error("Could not find AspectDefinition for " + aspectClass.getName() + " (" + qName+")");
        }

        String containerClassName = aspectDefinition.getContainerClassName();
        try {
            Class containerClass;
            if (containerClassName == null) {
                containerClass = ContextClassLoader.forName(aspectClass.getClassLoader(), DEFAULT_ASPECT_CONTAINER);
            } else {
                containerClass = ContextClassLoader.forName(aspectClass.getClassLoader(), containerClassName);
            }
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

        final Set definitionsBottomUp = SystemDefinitionContainer.getDefinitionsFor(loader);
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

    private static class ContainerKey {
        Reference aspectClassRef;
        String qName;
        private long aspectClassHash;

        private ContainerKey(final Class aspectClass, final String qName) {
            this.aspectClassRef = new WeakReference(aspectClass);
            this.qName = qName;
            this.aspectClassHash = aspectClass.hashCode();
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ContainerKey)) return false;

            final ContainerKey containerKey = (ContainerKey) o;

            if (aspectClassHash != containerKey.aspectClassHash) return false;
            if (qName != null ? !qName.equals(containerKey.qName) : containerKey.qName != null) return false;

            return true;
        }

        public int hashCode() {
            int result;
            result = (qName != null ? qName.hashCode() : 0);
            result = 29 * result + (int) (aspectClassHash ^ (aspectClassHash >>> 32));
            return result;
        }

        static ContainerKey get(final Class aspectClass, final String qName) {
            return new ContainerKey(aspectClass, qName);
        }

    }
}
