/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.codehaus.aspectwerkz.aspect.CFlowSystemAspect;
import org.codehaus.aspectwerkz.aspect.AspectContainer;
import org.codehaus.aspectwerkz.definition.expression.Expression;
import org.codehaus.aspectwerkz.definition.expression.ExpressionExpression;
import org.codehaus.aspectwerkz.definition.expression.PointcutType;
import org.codehaus.aspectwerkz.exception.DefinitionException;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.aspect.management.PointcutManager;
import org.codehaus.aspectwerkz.aspect.management.Pointcut;
import org.codehaus.aspectwerkz.aspect.management.AspectManager;
import org.codehaus.aspectwerkz.*;

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
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class StartupManager {

    /**
     * The path to the aspectwerkz home directory.
     */
    public static final String ASPECTWERKZ_HOME = java.lang.System.getProperty("aspectwerkz.home", ".");

    /**
     * The path to the definition file.
     */
    public static final String DEFINITION_FILE =
            java.lang.System.getProperty("aspectwerkz.definition.file", null);

    /**
     * The definition class name.
     */
    public static final String DEFINITION_CLASS_NAME =
            java.lang.System.getProperty("aspectwerkz.definition.class", null);

    /**
     * The name of the default aspectwerkz definition file.
     */
    public static final String DEFAULT_DEFINITION_FILE = "aspectwerkz.xml";

    /**
     * The default aspect container class.
     */
    public static final String DEFAULT_ASPECT_CONTAINER =
            "org.codehaus.aspectwerkz.aspect.DefaultAspectContainerStrategy";

    /**
     * Loads the system definition (one AspectManager = one SystemDefinition). The aspect container class to use.
     *
     * @TODO: does NOT work with AOPC. Needs to be specified in XML.
     */
    public static final String ASPECT_CONTAINER_IMPLEMENTATION_CLASS =
            System.getProperty(
                    "aspectwerkz.aspect.container.impl",
                    DEFAULT_ASPECT_CONTAINER
            );
    /**
     * Marks the manager as initialized or not.
     */
    private static boolean s_initialized = false;

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
//        if (! s_initialized.containsKey(uuid)) {
//            initializeSystem(uuid, definition);
//        } else {
//            if (uuid == null) {
//                throw new IllegalArgumentException("uuid can not be null");
//            }
//            if (definition == null) {
//                throw new IllegalArgumentException("definition can not be null");
//            }
//
//            //registerAspects(uuid, definition);
//            registerPointcuts(uuid, definition);
//        }
    }

    /**
     * Creates a new aspect container.
     *
     * @param crossCuttingInfo the cross-cutting info for the aspect
     */
    public static AspectContainer createAspectContainer(final CrossCuttingInfo crossCuttingInfo) {
        try {
            Class klass;
            if (crossCuttingInfo.getAspectClass().getName().equals(CFlowSystemAspect.class.getName())) {
                // system aspects should always be use the default container
                klass = ContextClassLoader.loadClass(DEFAULT_ASPECT_CONTAINER);
            }
            else {
                klass = ContextClassLoader.loadClass(ASPECT_CONTAINER_IMPLEMENTATION_CLASS);
            }
            Constructor constructor = klass.getConstructor(new Class[]{CrossCuttingInfo.class});
            return (AspectContainer)constructor.newInstance(new Object[]{crossCuttingInfo});
        }
        catch (InvocationTargetException e) {
            throw new DefinitionException(e.getTargetException().toString());
        }
        catch (NoSuchMethodException e) {
            throw new DefinitionException(
                    "aspect container does not have a valid constructor [" + ASPECT_CONTAINER_IMPLEMENTATION_CLASS +
                    "] (one that takes a CrossCuttingInfo instance as its only parameter): " +
                    e.toString()
            );
        }
        catch (Exception e) {
            StringBuffer cause = new StringBuffer();
            cause.append("could not create aspect container using the implementation specified [");
            cause.append(ASPECT_CONTAINER_IMPLEMENTATION_CLASS);
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
        }
        catch (NullPointerException e) {
            throw new DefinitionException("aspects not properly defined");
        }
        catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Registers and creates a new aspect container for the aspect.
     *
     * @param aspectManager the aspectManager for the system
     * @param aspectDef     the aspect definition
     */
    private static void registerAspect(
            final AspectManager aspectManager, final AspectDefinition aspectDef, final Map parameters) {
        try {
            String aspectClassName = aspectDef.getClassName();

            // load the aspect class
            final Class aspectClass;
            try {
                aspectClass = aspectManager.m_system.getDefiningClassLoader().loadClass(aspectClassName);
            }
            catch (ClassNotFoundException e) {
                throw new RuntimeException(aspectClassName + " could not be found on classpath: " + e.toString());
            }

            int deploymentModel;
            if (aspectDef.getDeploymentModel() == null || aspectDef.getDeploymentModel().equals("")) {
                deploymentModel = DeploymentModel.PER_JVM;
            }
            else {
                deploymentModel = DeploymentModel.getDeploymentModelAsInt(aspectDef.getDeploymentModel());
            }

            final CrossCuttingInfo crossCuttingInfoPrototype = new CrossCuttingInfo(
                    aspectManager.getUuid(), //AVAOPC////TODO: we could set the WHOLE AspectManager that defines the Aspect instead
                    aspectClass,
                    aspectDef.getName(),
                    deploymentModel,
                    aspectDef,
                    parameters
            );

            final AspectContainer container = createAspectContainer(crossCuttingInfoPrototype);
            crossCuttingInfoPrototype.setContainer(container);

            PointcutManager pointcutManager = new PointcutManager(aspectDef.getName(), deploymentModel);
            aspectManager.register(container, pointcutManager);
        }
        catch (Exception e) {
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
                Pointcut pointcut = pointcutManager.getPointcut(adviceDef.getExpression().getExpression());
                if (pointcut == null) {
                    pointcut = new Pointcut(aspectManager, adviceDef.getExpression());
                    pointcutManager.addPointcut(pointcut);
                }
                // add aspect name as prefix to allow advice reuse
                pointcut.addAroundAdvice(aspectDef.getName() + "/" + adviceDef.getName());
            }

            for (Iterator it2 = aspectDef.getBeforeAdvices().iterator(); it2.hasNext();) {
                AdviceDefinition adviceDef = (AdviceDefinition)it2.next();
                Pointcut pointcut = pointcutManager.getPointcut(adviceDef.getExpression().getExpression());
                if (pointcut == null) {
                    pointcut = new Pointcut(aspectManager, adviceDef.getExpression());
                    pointcutManager.addPointcut(pointcut);
                }
                // add aspect name as prefix to allow advice reuse
                pointcut.addBeforeAdvice(aspectDef.getName() + "/" + adviceDef.getName());
                //TODO - check me: Handler PC supports only beforeAdvice
                //TODO - .. this is not explicit here
            }

            for (Iterator it2 = aspectDef.getAfterAdvices().iterator(); it2.hasNext();) {
                AdviceDefinition adviceDef = (AdviceDefinition)it2.next();
                Pointcut pointcut = pointcutManager.getPointcut(adviceDef.getExpression().getExpression());
                if (pointcut == null) {
                    pointcut = new Pointcut(aspectManager, adviceDef.getExpression());
                    pointcutManager.addPointcut(pointcut);
                }
                // add aspect name as prefix to allow advice reuse
                pointcut.addAfterAdvice(aspectDef.getName() + "/" + adviceDef.getName());
            }
        }

        registerCFlowPointcuts(aspectManager, definition);
    }

    /**
     * Registers the cflow pointcuts.
     *
     * @param aspectManager the aspectManager for the system
     * @param definition    the AspectWerkz definition
     */
    private static void registerCFlowPointcuts(final AspectManager aspectManager, final SystemDefinition definition) {
        // get all aspects definitions
        for (Iterator it1 = definition.getAspectDefinitions().iterator(); it1.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it1.next();
            PointcutManager aspect = aspectManager.getPointcutManager(aspectDef.getName());

            for (Iterator it2 = aspectDef.getAllAdvices().iterator(); it2.hasNext();) {
                AdviceDefinition adviceDef = (AdviceDefinition)it2.next();
                Expression expression = adviceDef.getExpression();

                for (Iterator it3 = expression.getCflowExpressions().entrySet().iterator(); it3.hasNext();) {
                    Map.Entry entry = (Map.Entry)it3.next();
                    Expression value = (Expression)entry.getValue();

                    if (value instanceof ExpressionExpression) {

                        // recursive
                        // TODO ALEX exprexpr using exprexpr
                        // like pc cflow = "a or b"
                        // .. pc exec = "c IN cflow"
                        (new Exception("todo")).printStackTrace();
                    }
                    else {
                        // get the referenced cflow poincut definition
                        PointcutDefinition cflowPointcutDef = aspectDef.getPointcutDef(value.getName());

                        // if null, it is an anonymous cflow like in "execution(..) AND cflow(...)"
                        // create a new PoincutDef lately to bind it
                        // TODO check me - not needed since anonymous are autonamed ?
                        if (cflowPointcutDef == null) {
                            cflowPointcutDef = new PointcutDefinition();
                            cflowPointcutDef.setName(value.getName());
                            cflowPointcutDef.setType(PointcutType.CFLOW);
                            cflowPointcutDef.setExpression(value.getExpression());
                        }

                        // create call pointcut
                        Pointcut pointcut = new Pointcut(aspectManager, value);

                        // register the cflow advices in the system and create the cflow system aspect
                        // (if it does not already exist)
                        if (!aspectManager.hasAspect(CFlowSystemAspect.NAME)) {
                            AspectDefinition cflowAspect = new AspectDefinition(
                                    CFlowSystemAspect.NAME,
                                    CFlowSystemAspect.CLASS_NAME
                            );
                            cflowAspect.setDeploymentModel(CFlowSystemAspect.DEPLOYMENT_MODEL);
                            cflowAspect.addPointcut(cflowPointcutDef);

                            Class cflowAspectClass = CFlowSystemAspect.class;
                            try {
                                // add the cflow pre advice
                                cflowAspect.addBeforeAdvice(
                                        new AdviceDefinition(
                                                CFlowSystemAspect.PRE_ADVICE,
                                                AdviceDefinition.BEFORE_ADVICE,
                                                cflowAspect.getName(),
                                                cflowAspect.getClassName(),
                                                value,
                                                cflowAspectClass.getDeclaredMethod(
                                                        CFlowSystemAspect.PRE_ADVICE,
                                                        new Class[]{JoinPoint.class}
                                                ),
                                                CFlowSystemAspect.PRE_ADVICE_INDEX,
                                                cflowAspect
                                        )
                                );

                                // add the cflow post advice
                                cflowAspect.addAfterAdvice(
                                        new AdviceDefinition(
                                                CFlowSystemAspect.POST_ADVICE,
                                                AdviceDefinition.AFTER_ADVICE,
                                                cflowAspect.getName(),
                                                cflowAspect.getClassName(),
                                                value,
                                                cflowAspectClass.getDeclaredMethod(
                                                        CFlowSystemAspect.POST_ADVICE,
                                                        new Class[]{JoinPoint.class}
                                                ),
                                                CFlowSystemAspect.POST_ADVICE_INDEX,
                                                cflowAspect
                                        )
                                );
                            }
                            catch (NoSuchMethodException e) {
                                ; // TODO: why ignore exception? ALEX??
                            }

                            // add the advice to the aspectwerkz definition
                            definition.addAspect(cflowAspect);

                            // add the advice to the aspectwerkz system
                            registerAspect(aspectManager, cflowAspect, new HashMap());
                        }

                        // add references to the cflow advices to the cflow pointcut
                        // add aspect name as prefix to allow advice reuse
                        pointcut.addBeforeAdvice(CFlowSystemAspect.NAME + "/" + CFlowSystemAspect.PRE_ADVICE);
                        pointcut.addAfterAdvice(CFlowSystemAspect.NAME + "/" + CFlowSystemAspect.POST_ADVICE);

                        // add the call pointcut
                        aspect.addPointcut(pointcut);
                    }
                }
            }
        }
    }

    /**
     * Private constructor to prevent instantiability.
     */
    private StartupManager() {
    }
}
