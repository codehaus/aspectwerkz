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
import java.lang.reflect.Field;

import org.codehaus.aspectwerkz.ContextClassLoader;
import org.codehaus.aspectwerkz.SystemLoader;
import org.codehaus.aspectwerkz.CrossCutting;
import org.codehaus.aspectwerkz.DeploymentModel;
import org.codehaus.aspectwerkz.CrossCuttingInfo;
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
     * Marks the manager as initialized or not.
     */
    private static boolean s_initialized = false;

    /**
     * Loads the system definition.
     *
     * @param uuid       the UUID for the weave model to load
     * @param definition the definition for the system
     */
    public static void initializeSystem(final String uuid, final SystemDefinition definition) {
        if (uuid == null) {
            throw new IllegalArgumentException("uuid can not be null");
        }
        if (definition == null) {
            throw new IllegalArgumentException("definition can not be null");
        }

        if (s_initialized) {
            return;
        }
        s_initialized = true;

        registerAspects(uuid, definition);
        registerPointcuts(uuid, definition);
    }

    /**
     * ReLoads the system definition.
     *
     * @param uuid       the UUID for the weave model to load
     * @param definition the definition for the system
     */
    public static void reinitializeSystem(final String uuid, final SystemDefinition definition) {
        if (!s_initialized) {
            initializeSystem(uuid, definition);
        }
        else {
            if (uuid == null) {
                throw new IllegalArgumentException("uuid can not be null");
            }
            if (definition == null) {
                throw new IllegalArgumentException("definition can not be null");
            }

            //registerAspects(uuid, definition);
            registerPointcuts(uuid, definition);
        }
    }

    /**
     * Creates and registers the aspects defined.
     *
     * @param uuid       the UUID for the AspectWerkz system to use
     * @param definition the definition
     */
    private static void registerAspects(final String uuid, final SystemDefinition definition) {
        try {
            for (Iterator it = definition.getAspectDefinitions().iterator(); it.hasNext();) {
                AspectDefinition aspectDef = (AspectDefinition)it.next();
                registerAspect(uuid, aspectDef, definition.getParameters(aspectDef.getName()));
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
     * @param uuid      the UUID for the AspectWerkz system to use
     * @param aspectDef the aspect definition
     */
    private static void registerAspect(
            final String uuid,
            final AspectDefinition aspectDef,
            final Map parameters) {
        try {
            String aspectClassName = aspectDef.getClassName();

            // load the aspect class
            final Class aspectClass;
            try {
                aspectClass = ContextClassLoader.loadClass(aspectClassName);
            }
            catch (ClassNotFoundException e) {
                throw new RuntimeException(aspectClassName + " could not be found on classpath: " + e.toString());
            }

            CrossCutting aspect;
            try {
                aspect = (CrossCutting)aspectClass.newInstance();
            }
            catch (Exception e) {
                throw new RuntimeException("could not create a new instance of aspect [" + aspectClassName + "]: " + e.toString());
            }

            int deploymentModel;
            if (aspectDef.getDeploymentModel() == null || aspectDef.getDeploymentModel().equals("")) {
                deploymentModel = DeploymentModel.PER_JVM;
            }
            else {
                deploymentModel = DeploymentModel.getDeploymentModelAsInt(aspectDef.getDeploymentModel());
            }

            CrossCuttingInfo crossCuttingInfo = new CrossCuttingInfo();
            crossCuttingInfo.setUuid(uuid);
            crossCuttingInfo.setName(aspectDef.getName());
            crossCuttingInfo.setAspectClass(aspectClass);
            crossCuttingInfo.setDeploymentModel(deploymentModel);
            crossCuttingInfo.setAspectDef(aspectDef);
            crossCuttingInfo.setContainer(new AspectContainer(crossCuttingInfo));
            for (Iterator it = parameters.entrySet().iterator(); it.hasNext();) {
                Map.Entry entry = (Map.Entry)it.next();
                crossCuttingInfo.setParameter((String)entry.getKey(), (String)entry.getValue());
            }
            aspect.setCrossCuttingInfo(crossCuttingInfo);

            PointcutManager pointcutManager = new PointcutManager(uuid, aspectDef.getName(), deploymentModel);

            // register the aspect in the system
            SystemLoader.getSystem(uuid).getAspectManager().register(aspect, pointcutManager);
        }
        catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Creates and registers the aspects defined.
     *
     * @param uuid       the UUID for the AspectWerkz system to use
     * @param definition the AspectWerkz definition
     */
    private static void registerPointcuts(final String uuid, final SystemDefinition definition) {
        for (Iterator it = definition.getAspectDefinitions().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();

            PointcutManager pointcutManager = SystemLoader.getSystem(uuid).
                    getAspectManager().getPointcutManager(aspectDef.getName());

            for (Iterator it2 = aspectDef.getAroundAdvices().iterator(); it2.hasNext();) {
                AdviceDefinition adviceDef = (AdviceDefinition)it2.next();
                Pointcut pointcut = pointcutManager.getPointcut(adviceDef.getExpression().getExpression());
                if (pointcut == null) {
                    pointcut = new Pointcut(uuid, adviceDef.getExpression());
                    pointcutManager.addPointcut(pointcut);
                }
                pointcut.addAroundAdvice(adviceDef.getName());
            }

            for (Iterator it2 = aspectDef.getBeforeAdvices().iterator(); it2.hasNext();) {
                AdviceDefinition adviceDef = (AdviceDefinition)it2.next();
                Pointcut pointcut = pointcutManager.getPointcut(adviceDef.getExpression().getExpression());
                if (pointcut == null) {
                    pointcut = new Pointcut(uuid, adviceDef.getExpression());
                    pointcutManager.addPointcut(pointcut);
                }
                pointcut.addBeforeAdvice(adviceDef.getName());
                //TODO - check me: Handler PC supports only beforeAdvice
                //TODO - .. this is not explicit here
            }

            for (Iterator it2 = aspectDef.getAfterAdvices().iterator(); it2.hasNext();) {
                AdviceDefinition adviceDef = (AdviceDefinition)it2.next();
                Pointcut pointcut = pointcutManager.getPointcut(adviceDef.getExpression().getExpression());
                if (pointcut == null) {
                    pointcut = new Pointcut(uuid, adviceDef.getExpression());
                    pointcutManager.addPointcut(pointcut);
                }
                pointcut.addAfterAdvice(adviceDef.getName());
            }
        }

        registerCFlowPointcuts(uuid, definition);
    }

    /**
     * Registers the cflow pointcuts.
     *
     * @param uuid       the UUID for the AspectWerkz system to use
     * @param definition the AspectWerkz definition
     */
    private static void registerCFlowPointcuts(
            final String uuid,
            final SystemDefinition definition) {
        // get all aspects definitions
        for (Iterator it1 = definition.getAspectDefinitions().iterator(); it1.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it1.next();
            PointcutManager aspect = SystemLoader.getSystem(uuid).getAspectManager().
                    getPointcutManager(aspectDef.getName());

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
                        Pointcut pointcut = new Pointcut(uuid, value);

                        // register the cflow advices in the system and create the cflow system aspect
                        // (if it does not already exist)
                        if (!SystemLoader.getSystem(uuid).getAspectManager().hasAspect(CFlowSystemAspect.NAME)) {
                            AspectDefinition cflowAspect = new AspectDefinition(
                                    CFlowSystemAspect.NAME,
                                    CFlowSystemAspect.CLASS_NAME,
                                    CFlowSystemAspect.DEPLOYMENT_MODEL
                            );
                            cflowAspect.addPointcut(cflowPointcutDef);

                            Class cflowAspectClass = CFlowSystemAspect.class;
                            try {
                                // add the cflow pre advice
                                cflowAspect.addBeforeAdvice(
                                        new AdviceDefinition(
                                                CFlowSystemAspect.PRE_ADVICE,
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
                                ;
                            }

                            // add the advice to the aspectwerkz definition
                            definition.addAspect(cflowAspect);

                            // add the advice to the aspectwerkz system
                            registerAspect(uuid, cflowAspect, new HashMap());
                        }

                        // add references to the cflow advices to the cflow pointcut
                        pointcut.addBeforeAdvice(CFlowSystemAspect.PRE_ADVICE);
                        pointcut.addAfterAdvice(CFlowSystemAspect.POST_ADVICE);

                        // add the call pointcut
                        aspect.addPointcut(pointcut);
                    }
                }
                //TODO ALEX - is this commented code needed?

//                    // add a mapping between the cflow pattern and the method patterns affected
//                    for (Iterator it3 = weavingRule.getPointcutRefs().iterator(); it3.hasNext();) {
//                        PointcutDefinition pointcutDef = aspectDef.getPointcutDef((String)it3.next());
//                        if (pointcutDef != null && pointcutDef.getType().
//                                equalsIgnoreCase(PointcutDefinition.METHOD)) {
//
//                            pointcutManager.addMethodToCFlowMethodMap(
//                                    pointcutDef.getPointcutPatternTuple(),
//                                    cflowPointcutDef.getPointcutPatternTuple());
//                        }
//                    }
//                }
//            }
//            catch (NullPointerException e) {
//                throw new DefinitionException("cflow pointcuts in aspect <" + pointcutManager.getName() + "> are not properly defined");
//            }
//            catch (Exception e) {
//                throw new WrappedRuntimeException(e);
//            }
//        }
            }
        }
    }

    /**
     * Private constructor to prevent instantiability.
     */
    private StartupManager() {
    }
}
