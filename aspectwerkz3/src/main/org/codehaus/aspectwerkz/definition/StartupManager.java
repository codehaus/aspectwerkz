/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition;

import org.codehaus.aspectwerkz.AspectSystem;
import org.codehaus.aspectwerkz.ContextClassLoader;
import org.codehaus.aspectwerkz.CrossCuttingInfo;
import org.codehaus.aspectwerkz.DeploymentModel;
import org.codehaus.aspectwerkz.SystemLoader;
import org.codehaus.aspectwerkz.aspect.AspectContainer;
import org.codehaus.aspectwerkz.aspect.CFlowSystemAspect;
import org.codehaus.aspectwerkz.aspect.management.AspectManager;
import org.codehaus.aspectwerkz.aspect.management.Pointcut;
import org.codehaus.aspectwerkz.aspect.management.PointcutManager;
import org.codehaus.aspectwerkz.exception.DefinitionException;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.expression.ExpressionInfo;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Manages the startup procedure, walks through the definition and instantiates the
 * aspects/advices/introduction/pointcuts.
 * <p/>
 * Reads the definition, either as a class of as an XML file.
 * <p/>
 * To use your XML definition file pass <code>-Daspectwerkz.definition.file=PathToFile</code> as parameter to the JVM.
 * <p/>
 * If the above given parameter is not specified, the <code>StartupManager</code> tries locate a file called
 * <code>aspectwerkz.xml</code> in the classpath and if this fails the last attempt is to use the
 * <code>ASPECTWERKZ_HOME/config/aspectwerkz.xml</code> file (if there is one).
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class StartupManager {
    /**
     * The default aspect container class.
     */
    public static final String DEFAULT_ASPECT_CONTAINER = "org.codehaus.aspectwerkz.aspect.DefaultAspectContainerStrategy";

    /**
     * Private constructor to prevent instantiability.
     */
    private StartupManager() {
    }

    /**
     * Loads the system definition.
     *
     * @param aspectManager the aspectManager for the system
     * @param definition    the definition for the system
     */
    public static void initializeSystem(final AspectManager aspectManager, final SystemDefinition definition) {
        // note: initialization check is maintained by AspectRegistry due to a lazy policy
        registerAspects(aspectManager, definition);
        registerPointcuts(aspectManager, definition);
    }

    /**
     * ReLoads the system definition.
     *
     * @param loader     the class loader
     * @param definition the definition for the system
     */
    public static void reinitializeSystem(final ClassLoader loader, final SystemDefinition definition) {
        AspectSystem as = SystemLoader.getSystem(loader);
        AspectManager am = as.getAspectManager(definition.getUuid());

        // TODO better runtime part sync with def part for RW/RuW/HotDeploy
        // when altering existing pc, those needs to be updated manaually (see EWorldUtil)
        registerPointcuts(am, definition);
        return;
    }

    /**
     * Creates a new aspect container.
     *
     * @param crossCuttingInfo the cross-cutting info for the aspect
     */
    public static AspectContainer createAspectContainer(final CrossCuttingInfo crossCuttingInfo) {
        String containerClassName = "";
        try {
            Class klass;
            containerClassName = crossCuttingInfo.getAspectDefinition().getContainerClassName();
            if ((containerClassName == null)
                || crossCuttingInfo.getAspectClass().getName().equals(CFlowSystemAspect.CLASS_NAME)) {
                klass = ContextClassLoader.loadClass(DEFAULT_ASPECT_CONTAINER);
            } else {
                klass = ContextClassLoader.loadClass(containerClassName);
            }
            Constructor constructor = klass.getConstructor(new Class[] { CrossCuttingInfo.class });
            return (AspectContainer)constructor.newInstance(new Object[] { crossCuttingInfo });
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            throw new DefinitionException(e.getTargetException().toString());
        } catch (NoSuchMethodException e) {
            throw new DefinitionException("aspect container does not have a valid constructor [" + containerClassName
                                          + "] (one that takes a CrossCuttingInfo instance as its only parameter): "
                                          + e.toString());
        } catch (Exception e) {
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
     * Creates and registers the aspects defined.
     *
     * @param aspectManager the aspectManager for the system
     * @param definition    the definition
     */
    private static void registerAspects(final AspectManager aspectManager, final SystemDefinition definition) {
        try {
            for (Iterator it = definition.getAspectDefinitions().iterator(); it.hasNext();) {
                AspectDefinition aspectDef = (AspectDefinition)it.next();
                registerAspect(aspectManager, aspectDef, definition.getParameters(aspectDef.getName()));
            }
        } catch (NullPointerException e) {
            throw new DefinitionException("aspects not properly defined");
        } catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Registers and creates a new aspect container for the aspect.
     *
     * @param aspectManager the aspectManager for the system
     * @param aspectDef     the aspect definition
     */
    private static void registerAspect(final AspectManager aspectManager, final AspectDefinition aspectDef,
                                       final Map parameters) {
        try {
            String aspectClassName = aspectDef.getClassName();

            // load the aspect class
            final Class aspectClass;
            try {
                aspectClass = aspectManager.m_system.getDefiningClassLoader().loadClass(aspectClassName);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(aspectClassName + " could not be found on classpath: " + e.toString());
            }

            int deploymentModel;
            if ((aspectDef.getDeploymentModel() == null) || aspectDef.getDeploymentModel().equals("")) {
                deploymentModel = DeploymentModel.PER_JVM;
            } else {
                deploymentModel = DeploymentModel.getDeploymentModelAsInt(aspectDef.getDeploymentModel());
            }

            //TODO: we could set the WHOLE AspectManager that defines the Aspect instead
            final CrossCuttingInfo crossCuttingInfoPrototype = new CrossCuttingInfo(aspectManager.getUuid(),
                                                                                    aspectClass, aspectDef.getName(),
                                                                                    deploymentModel, aspectDef,
                                                                                    parameters);
            final AspectContainer container = createAspectContainer(crossCuttingInfoPrototype);
            crossCuttingInfoPrototype.setContainer(container);

            PointcutManager pointcutManager = new PointcutManager(aspectDef.getName(), deploymentModel);
            aspectManager.register(container, pointcutManager);
        } catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Creates and registers the aspects defined.
     *
     * @param aspectManager the aspectManager for the system
     * @param definition    the AspectWerkz definition
     */
    private static void registerPointcuts(final AspectManager aspectManager, final SystemDefinition definition) {
        for (Iterator it = definition.getAspectDefinitions().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();

            PointcutManager pointcutManager = aspectManager.getPointcutManager(aspectDef.getName());

            for (Iterator it2 = aspectDef.getAroundAdvices().iterator(); it2.hasNext();) {
                AdviceDefinition adviceDef = (AdviceDefinition)it2.next();
                Pointcut pointcut = pointcutManager.getPointcut(adviceDef.getExpressionInfo().toString());
                if (pointcut == null) {
                    pointcut = new Pointcut(aspectManager, adviceDef.getExpressionInfo());
                    pointcutManager.addPointcut(pointcut);
                }

                // add aspect name as prefix to allow advice reuse
                pointcut.addAroundAdvice(aspectDef.getName() + '/' + adviceDef.getName());
            }

            for (Iterator it2 = aspectDef.getBeforeAdvices().iterator(); it2.hasNext();) {
                AdviceDefinition adviceDef = (AdviceDefinition)it2.next();
                Pointcut pointcut = pointcutManager.getPointcut(adviceDef.getExpressionInfo().toString());
                if (pointcut == null) {
                    pointcut = new Pointcut(aspectManager, adviceDef.getExpressionInfo());
                    pointcutManager.addPointcut(pointcut);
                }

                // add aspect name as prefix to allow advice reuse
                pointcut.addBeforeAdvice(aspectDef.getName() + '/' + adviceDef.getName());
            }

            for (Iterator it2 = aspectDef.getAfterAdvices().iterator(); it2.hasNext();) {
                AdviceDefinition adviceDef = (AdviceDefinition)it2.next();
                Pointcut pointcut = pointcutManager.getPointcut(adviceDef.getExpressionInfo().toString());
                if (pointcut == null) {
                    pointcut = new Pointcut(aspectManager, adviceDef.getExpressionInfo());
                    pointcutManager.addPointcut(pointcut);
                }

                // add aspect name as prefix to allow advice reuse
                pointcut.addAfterAdvice(aspectDef.getName() + '/' + adviceDef.getName());
            }
        }

        // registerCflowPointcuts(aspectManager, definition);
    }

    /**
     * Registers the cflow pointcuts.
     *
     * @param aspectManager the aspectManager for the system
     * @param definition    the AspectWerkz definition
     */
    private static void NEW__registerCflowPointcuts(final AspectManager aspectManager, final SystemDefinition definition) {
        // get all aspects definitions
        for (Iterator it1 = definition.getAspectDefinitions().iterator(); it1.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it1.next();
            PointcutManager pointcutManager = aspectManager.getPointcutManager(aspectDef.getName());

            for (Iterator it2 = aspectDef.getAllAdvices().iterator(); it2.hasNext();) {
                AdviceDefinition adviceDef = (AdviceDefinition)it2.next();
                ExpressionInfo expressionInfo = adviceDef.getExpressionInfo();
                Pointcut cflowPointcut = pointcutManager.getCflowPointcut(expressionInfo.toString());

                // register the cflow advices in the system and create the cflow system pointcutManager
                // (if it does not already exist)
                if (!aspectManager.hasAspect(CFlowSystemAspect.NAME)) {
                    AspectDefinition cflowAspectDef = new AspectDefinition(CFlowSystemAspect.NAME,
                                                                           CFlowSystemAspect.CLASS_NAME);

                    PointcutDefinition pointcutDef = new PointcutDefinition(expressionInfo.toString());

                    cflowAspectDef.setDeploymentModel(CFlowSystemAspect.DEPLOYMENT_MODEL);
                    cflowAspectDef.addPointcut(pointcutDef);

                    try {
                        // add the cflow pre advice
                        cflowAspectDef.addBeforeAdvice(new AdviceDefinition(CFlowSystemAspect.PRE_ADVICE,
                                                                            AdviceDefinition.BEFORE_ADVICE,
                                                                            cflowAspectDef.getName(),
                                                                            cflowAspectDef.getClassName(),
                                                                            expressionInfo,
                                                                            CFlowSystemAspect.class.getDeclaredMethod(CFlowSystemAspect.PRE_ADVICE,
                                                                                                                      new Class[] {
                                                                                                                          JoinPoint.class
                                                                                                                      }),
                                                                            CFlowSystemAspect.PRE_ADVICE_INDEX,
                                                                            cflowAspectDef));

                        // add the cflow post advice
                        cflowAspectDef.addAfterAdvice(new AdviceDefinition(CFlowSystemAspect.POST_ADVICE,
                                                                           AdviceDefinition.AFTER_ADVICE,
                                                                           cflowAspectDef.getName(),
                                                                           cflowAspectDef.getClassName(),
                                                                           expressionInfo,
                                                                           CFlowSystemAspect.class.getDeclaredMethod(CFlowSystemAspect.POST_ADVICE,
                                                                                                                     new Class[] {
                                                                                                                         JoinPoint.class
                                                                                                                     }),
                                                                           CFlowSystemAspect.POST_ADVICE_INDEX,
                                                                           cflowAspectDef));
                    } catch (NoSuchMethodException e) {
                        ; // TODO: why ignore exception? ALEX??
                    }

                    // add the advice to the aspectwerkz definition
                    definition.addAspect(cflowAspectDef);

                    // add the advice to the aspectwerkz system
                    registerAspect(aspectManager, cflowAspectDef, new HashMap());
                }

                // add references to the cflow advices to the cflow cflowPointcut
                // add pointcutManager name as prefix to allow advice reuse
                cflowPointcut.addBeforeAdvice(CFlowSystemAspect.NAME + '/' + CFlowSystemAspect.PRE_ADVICE);
                cflowPointcut.addAfterAdvice(CFlowSystemAspect.NAME + '/' + CFlowSystemAspect.POST_ADVICE);

                // add the call cflowPointcut
                pointcutManager.addPointcut(cflowPointcut);
            }
        }
    }
}
