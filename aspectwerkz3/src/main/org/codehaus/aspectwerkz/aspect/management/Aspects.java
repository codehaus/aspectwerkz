/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.aspect.management;

import org.codehaus.aspectwerkz.aspect.AspectContainer;
import org.codehaus.aspectwerkz.aspect.CFlowSystemAspect;
import org.codehaus.aspectwerkz.aspect.AdviceType;
import org.codehaus.aspectwerkz.util.SequencedHashMap;
import org.codehaus.aspectwerkz.expression.ExpressionContext;
import org.codehaus.aspectwerkz.DeploymentModel;
import org.codehaus.aspectwerkz.AspectContext;
import org.codehaus.aspectwerkz.ContextClassLoader;
import org.codehaus.aspectwerkz.AdviceInfo;
import org.codehaus.aspectwerkz.definition.AspectDefinition;
import org.codehaus.aspectwerkz.definition.SystemDefinition;
import org.codehaus.aspectwerkz.definition.AdviceDefinition;
import org.codehaus.aspectwerkz.definition.SystemDefinitionContainer;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.exception.DefinitionException;

import java.util.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Manages the aspects, registry for the aspect containers (one container per aspect type).
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public class Aspects {

    /**
     * The default aspect container class.
     */
    public static final String DEFAULT_ASPECT_CONTAINER = "org.codehaus.aspectwerkz.aspect.DefaultAspectContainerStrategy";

    /**
     * Map with all the aspect containers mapped to the names of the aspects.
     */
    private static final Map ASPECT_CONTAINERS = new SequencedHashMap();

    /**
     * Flags the aspect registry as initialized.
     */
    private static boolean m_isInitialized = false;

    /**
     * Initializes the aspect registry.
     *
     * @param loader
     */
    public static synchronized void initialize(final ClassLoader loader) {
        if (m_isInitialized) {
            return;
        }
        List definitions = SystemDefinitionContainer.getHierarchicalDefs(loader);
        for (Iterator it = definitions.iterator(); it.hasNext();) {
            register((SystemDefinition) it.next());
        }
        m_isInitialized = true;
    }

    /**
     * Creates and registers the aspects defined.
     *
     * @param definition the definition
     */
    private static void register(final SystemDefinition definition) {
        try {
            for (Iterator it = definition.getAspectDefinitions().iterator(); it.hasNext();) {
                AspectDefinition aspectDef = (AspectDefinition) it.next();
                register(aspectDef, definition.getParameters(aspectDef.getName()));
            }
        } catch (NullPointerException e) {
            throw new DefinitionException("aspects not properly defined");
        } catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Registers a new aspect.
     *
     * @param aspectDef  the aspect definition
     * @param parameters the parameters (can be null)
     */
    private static void register(final AspectDefinition aspectDef, final Map parameters) {
        try {
            final Class aspectClass;
            try {
                aspectClass = ContextClassLoader.loadClass(aspectDef.getClassName());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(aspectDef.getClassName() + " not found on classpath: " + e.toString());
            }

            int deploymentModel;
            if ((aspectDef.getDeploymentModel() == null) || aspectDef.getDeploymentModel().equals("")) {
                deploymentModel = DeploymentModel.PER_JVM;
            } else {
                deploymentModel = DeploymentModel.getDeploymentModelAsInt(aspectDef.getDeploymentModel());
            }

            final AspectContext aspectContextPrototype = new AspectContext(
                    aspectDef.getSystemDefinition().getUuid(),
                    aspectClass,
                    aspectDef.getName(),
                    deploymentModel,
                    aspectDef,
                    parameters
            );
            final AspectContainer container = createAspectContainer(aspectContextPrototype);

            register(container);

        } catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Registers a new aspect.
     *
     * @param container the new aspect container
     */
    private static void register(final AspectContainer container) {
        synchronized (ASPECT_CONTAINERS) {
            ASPECT_CONTAINERS.put(container.getContext().getName(), container);
            registerPointcuts(container, container.getContext().getAspectDefinition().getSystemDefinition());
        }
    }

    /**
     * Returns the aspect container for the aspect with the given name.
     *
     * @param name the name of the aspect
     * @return the container
     */
    public static AspectContainer getContainer(final String name) {
        return (AspectContainer) ASPECT_CONTAINERS.get(name);
    }

    /**
     * Returns the singleton aspect instance for the aspect with the given name.
     *
     * @param name the name of the aspect
     * @return the singleton aspect instance
     */
    public static Object aspectOf(final String name) {
        return getContainer(name).aspectOf();
    }

    /**
     * Returns the per class aspect instance for the aspect with the given name.
     *
     * @param name the name of the aspect
     * @return the per class aspect instance
     */
    public static Object aspectOf(final String name, final Class target) {
        return getContainer(name).aspectOf(target);
    }

    /**
     * TODO XXX move to Pointcut Manager when working Returns the pointcut list for the context specified.
     *
     * @param ctx the expression context
     * @return the pointcuts for this join point
     */
    public static List getPointcuts(final ExpressionContext ctx) {
        List pointcuts = new ArrayList();
        for (Iterator it = ASPECT_CONTAINERS.values().iterator(); it.hasNext();) {
            AspectContainer container = (AspectContainer) it.next();
            pointcuts.addAll(container.getPointcutManager().getPointcuts(ctx));
        }
        return pointcuts;
    }

    /**
     * TODO XXX move to Pointcut Manager when working Returns the cflow pointcut list for the context specified.
     *
     * @param ctx the expression context
     * @return the pointcuts for this join point
     */
    public static List getCflowPointcuts(final ExpressionContext ctx) {
        List pointcuts = new ArrayList();
        for (Iterator it = ASPECT_CONTAINERS.values().iterator(); it.hasNext();) {
            AspectContainer container = (AspectContainer) it.next();
            pointcuts.addAll(container.getPointcutManager().getCflowPointcuts(ctx));
        }
        return pointcuts;
    }

    /**
     * Creates a new aspect container.
     *
     * @param aspectContext the aspect context
     */
    public static AspectContainer createAspectContainer(final AspectContext aspectContext) {
        String containerClassName = null;
        try {
            Class klass;
            containerClassName = aspectContext.getAspectDefinition().getContainerClassName();
            if (containerClassName == null ||
                aspectContext.getAspectClass().getName().equals(CFlowSystemAspect.CLASS_NAME)) {
                klass = ContextClassLoader.loadClass(DEFAULT_ASPECT_CONTAINER);
            } else {
                klass = ContextClassLoader.loadClass(containerClassName);
            }
            Constructor constructor = klass.getConstructor(new Class[]{AspectContext.class});
            AspectContainer container = (AspectContainer) constructor.newInstance(new Object[]{aspectContext});
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
     * TODO: if needed, how to handle the system def? Which system def should be used etc.?
     * <p/>
     * Creates and registers new aspect at runtime.
     *
     * @param name            the name of the aspect
     * @param aspectClassName the class name of the aspect
     * @param deploymentModel the deployment model for the aspect (constants in the DeploymemtModel class, e.g. f.e.
     *                        DeploymentModel.PER_JVM)
     * @param loader          an optional class loader (if null it uses the context classloader)
     */
    public static void createAspect(final String name,
                                    final String aspectClassName,
                                    final int deploymentModel,
                                    final ClassLoader loader) {
//        if (name == null) {
//            throw new IllegalArgumentException("aspect name can not be null");
//        }
//        if (aspectClassName == null) {
//            throw new IllegalArgumentException("class name can not be null");
//        }
//        if ((deploymentModel < 0) || (deploymentModel > 3)) {
//            throw new IllegalArgumentException(deploymentModel + " is not a valid deployment model type");
//        }
//        Class aspectClass = null;
//        try {
//            if (loader == null) {
//                aspectClass = ContextClassLoader.loadClass(aspectClassName);
//            } else {
//                aspectClass = loader.loadClass(aspectClassName);
//            }
//        } catch (Exception e) {
//            StringBuffer msg = new StringBuffer();
//            msg.append("could not load aspect class [");
//            msg.append(aspectClassName);
//            msg.append("] with name ");
//            msg.append(name);
//            msg.append(": ");
//            msg.append(e.toString());
//            throw new RuntimeException(msg.toString());
//        }
//
//        // create the aspect definition
//        AspectDefinition aspectDef = new AspectDefinition(aspectClassName, aspectClassName, m_definition);
//        aspectDef.setDeploymentModel(DeploymentModel.getDeploymentModelAsString(deploymentModel));
//
//        // parse the class attributes and create a definition
//        AspectAnnotationParser.parse(aspectClass, aspectDef, m_definition);
//        m_definition.addAspect(aspectDef);
//        AspectContext aspectContext = new AspectContext(
//                null,
//                aspectClass,
//                aspectDef.getName(),
//                deploymentModel,
//                aspectDef,
//                new HashMap()
//        );
//
//        // create the aspect container
//        AspectContainer container = Aspects.createAspectContainer(aspectContext);
//        aspectContext.setContainer(container);
//        Aspects.register(container);
    }

    /**
     * Registers the pointcuts defined.
     *
     * @param container  the aspect container
     * @param definition the system definition
     */
    private static void registerPointcuts(final AspectContainer container, final SystemDefinition definition) {
        for (Iterator it = definition.getAspectDefinitions().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition) it.next();
            if (aspectDef.getName().equals(CFlowSystemAspect.CLASS_NAME)) {
                continue;
            }
            PointcutManager pointcutManager = container.getPointcutManager();

            for (Iterator it2 = aspectDef.getAroundAdviceDefinitions().iterator(); it2.hasNext();) {
                AdviceDefinition adviceDef = (AdviceDefinition) it2.next();
                Pointcut pointcut = pointcutManager.getPointcut(adviceDef.getExpressionInfo().toString());
                if (pointcut == null) {
                    pointcut = new Pointcut(container.getContext(), adviceDef.getExpressionInfo());
                    pointcutManager.addPointcut(pointcut);
                }
                pointcut.addAroundAdvice(AdviceInfo.createAdviceName(aspectDef.getName(), adviceDef.getName()));
            }
            for (Iterator it2 = aspectDef.getBeforeAdviceDefinitions().iterator(); it2.hasNext();) {
                AdviceDefinition adviceDef = (AdviceDefinition) it2.next();
                Pointcut pointcut = pointcutManager.getPointcut(adviceDef.getExpressionInfo().toString());
                if (pointcut == null) {
                    pointcut = new Pointcut(container.getContext(), adviceDef.getExpressionInfo());
                    pointcutManager.addPointcut(pointcut);
                }
                pointcut.addBeforeAdvice(AdviceInfo.createAdviceName(aspectDef.getName(), adviceDef.getName()));
            }
            for (Iterator it2 = aspectDef.getAfterAdviceDefinitions().iterator(); it2.hasNext();) {
                AdviceDefinition adviceDef = (AdviceDefinition) it2.next();
                Pointcut pointcut = pointcutManager.getPointcut(adviceDef.getExpressionInfo().toString());
                if (pointcut == null) {
                    pointcut = new Pointcut(container.getContext(), adviceDef.getExpressionInfo());
                    pointcutManager.addPointcut(pointcut);
                }
                if (adviceDef.getType().equals(AdviceType.AFTER) ||
                    adviceDef.getType().equals(AdviceType.AFTER_FINALLY)) {
                    pointcut.addAfterFinallyAdvices(
                            AdviceInfo.createAdviceName(aspectDef.getName(), adviceDef.getName())
                    );
                }
                if (adviceDef.getType().equals(AdviceType.AFTER_RETURNING)) {
                    pointcut.addAfterReturningAdvices(
                            AdviceInfo.createAdviceName(aspectDef.getName(), adviceDef.getName())
                    );
                }
                if (adviceDef.getType().equals(AdviceType.AFTER_THROWING)) {
                    pointcut.addAfterThrowingAdvices(
                            AdviceInfo.createAdviceName(aspectDef.getName(), adviceDef.getName())
                    );
                }
            }
        }
    }

    /**
     * Class is non-instantiable.
     */
    private Aspects() {
    }
}
