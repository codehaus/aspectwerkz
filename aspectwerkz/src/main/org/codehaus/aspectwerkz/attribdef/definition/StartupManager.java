/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.attribdef.definition;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import org.codehaus.aspectwerkz.AspectMetaData;
import org.codehaus.aspectwerkz.DeploymentModel;
import org.codehaus.aspectwerkz.ContextClassLoader;
import org.codehaus.aspectwerkz.SystemLoader;
import org.codehaus.aspectwerkz.definition.AspectWerkzDefinition;
import org.codehaus.aspectwerkz.definition.expression.PointcutType;
import org.codehaus.aspectwerkz.attribdef.AttribDefSystem;
import org.codehaus.aspectwerkz.attribdef.aspect.Aspect;
import org.codehaus.aspectwerkz.attribdef.aspect.AspectContainer;
import org.codehaus.aspectwerkz.attribdef.definition.AspectDefinition;
import org.codehaus.aspectwerkz.attribdef.definition.AdviceDefinition;
import org.codehaus.aspectwerkz.pointcut.ExecutionPointcut;
import org.codehaus.aspectwerkz.pointcut.GetPointcut;
import org.codehaus.aspectwerkz.pointcut.CallPointcut;
import org.codehaus.aspectwerkz.pointcut.ThrowsPointcut;
import org.codehaus.aspectwerkz.pointcut.SetPointcut;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.exception.DefinitionException;

/**
 * Manages the startup procedure, walks through the definition and instantiates
 * the aspects/advices/introduction/pointcuts.
 * <p/>
 *
 * Reads the definition, either as a class of as an XML file.
 * <p/>
 * To use your XML definition file pass
 * <code>-Daspectwerkz.definition.file=PathToFile</code>
 * as parameter to the JVM.
 * <p/>
 * If the above given parameter is not specified, the <code>StartupManager</code>
 * tries locate a file called <code>aspectwerkz.xml</code> in the classpath
 * and if this fails the last attempt is to use the
 * <code>ASPECTWERKZ_HOME/config/aspectwerkz.xml</code> file (if there is one).
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class StartupManager {

    /**
     * The path to the aspectwerkz home directory.
     */
    public static final String ASPECTWERKZ_HOME = System.getProperty("aspectwerkz.home", ".");

    /**
     * The path to the definition file.
     */
    public static final String DEFINITION_FILE =
            System.getProperty("aspectwerkz.definition.file", null);

    /**
     * The definition class name.
     */
    public static final String DEFINITION_CLASS_NAME =
            System.getProperty("aspectwerkz.definition.class", null);

    /**
     * The name of the default aspectwerkz definition file.
     */
    public static final String DEFAULT_DEFINITION_FILE = "aspectwerkz.xml";

    /**
     * The default aspect container class.
     */
    public static final String DEFAULT_ASPECT_CONTAINER =
            "org.codehaus.aspectwerkz.attribdef.aspect.DefaultAspectContainerStrategy";

    /**
     * The aspect container class to use.
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
     * @param uuid the UUID for the weave model to load
     * @param definition the definition for the system
     */
    public static void initializeSystem(final String uuid, final AspectWerkzDefinition definition) {
        if (uuid == null) throw new IllegalArgumentException("uuid can not be null");
        if (definition == null) throw new IllegalArgumentException("definition can not be null");

        if (s_initialized) return;
        s_initialized = true;

        definition.loadAspects(ContextClassLoader.getLoader());

        AspectWerkzDefinitionImpl def = (AspectWerkzDefinitionImpl)definition;
        registerAspects(uuid, def);
        registerPointcuts(uuid, def);
    }

    /**
     * Creates a new container for the aspect.
     *
     * @param aspect the aspect's implementation class
     */
    public static AspectContainer createAspectContainer(final Aspect aspect) {
        if (aspect == null) throw new IllegalArgumentException("aspect can not be null");

        try {
            Class klass = ContextClassLoader.loadClass(ASPECT_CONTAINER_IMPLEMENTATION_CLASS);
            Constructor constructor = klass.getConstructor(new Class[]{Aspect.class});
            return (AspectContainer)constructor.newInstance(new Object[]{aspect});
        }
        catch (Exception e) {
            StringBuffer cause = new StringBuffer();
            cause.append("could not create aspect container using specified class [");
            cause.append(ASPECT_CONTAINER_IMPLEMENTATION_CLASS);
            cause.append("]: ");
            cause.append(e.getMessage());
            throw new RuntimeException(cause.toString());
        }
    }

    /**
     * Creates and registers the aspects defined.
     *
     * @param uuid the UUID for the AspectWerkz system to use
     * @param definition the definition
     */
    private static void registerAspects(final String uuid, final AspectWerkzDefinitionImpl definition) {
        try {
            for (Iterator it = definition.getAspectDefinitions().iterator(); it.hasNext();) {
                AspectDefinition aspectDef = (AspectDefinition)it.next();
                registerAspect(uuid, aspectDef, definition.getParameters(aspectDef.getClassName()));
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
     * @param uuid the UUID for the AspectWerkz system to use
     * @param aspectDef the aspect definition
     */
    private static void registerAspect(final String uuid, final AspectDefinition aspectDef, final Map parameters) {
        try {
            String aspectClassName = aspectDef.getClassName();

            // load the aspect class
            final Class aspectClass;
            try {
                aspectClass = ContextClassLoader.loadClass(aspectClassName);
            }
            catch (ClassNotFoundException e) {
                throw new RuntimeException(aspectClassName + " could not be found on classpath");
            }

            // create an instance of the aspect class
            final Aspect aspect;
            try {
                aspect = (Aspect)aspectClass.newInstance();
            }
            catch (Exception e) {
                throw new RuntimeException("could not create a new instance of aspect [" + aspectClassName + "], does the class inherit the [org.codehaus.aspectwerkz.attribdef.aspect.Aspect] class?");
            }

            int deploymentModel;
            if (aspectDef.getDeploymentModel() == null || aspectDef.getDeploymentModel().equals("")) {
                deploymentModel = DeploymentModel.PER_JVM;
            }
            else {
                deploymentModel = DeploymentModel.getDeploymentModelAsInt(aspectDef.getDeploymentModel());
            }

            // set the parameters
            Field field = Aspect.class.getDeclaredField("m_uuid");
            field.setAccessible(true);
            field.set(aspect, uuid);
            aspect.___AW_setName(aspectDef.getName());
            aspect.___AW_setAspectClass(aspectClass);
            aspect.___AW_setDeploymentModel(deploymentModel);
            aspect.___AW_setAspectDef(aspectDef);
            for (Iterator it = parameters.entrySet().iterator(); it.hasNext();) {
                Map.Entry entry = (Map.Entry)it.next();
                aspect.___AW_setParameter((String)entry.getKey(), (String)entry.getValue());
            }

            // TODO: handle parameters for attribdef (needs attribute support)
            // handle the parameters passed to the advice
//            for (Iterator it2 = aspectDef.getParameters().entrySet().iterator(); it2.hasNext();) {
//                Map.Entry entry = (Map.Entry)it2.next();
//                aspect.setParameter((String)entry.getKey(), (String)entry.getValue());
//            }

            // create and set the container for the aspect
            AspectContainer container = createAspectContainer(aspect);
            if (container != null) {
                aspect.___AW_setContainer(container);
            }
            else {
                throw new DefinitionException("could not create aspect container for aspect [" + aspect.___AW_getName() + "]");
            }

            // register the aspect in the system
            AspectMetaData aspectMetaData = new AspectMetaData(
                    uuid, aspectDef.getName(), deploymentModel
            );

            ((AttribDefSystem)SystemLoader.getSystem(uuid)).register(aspect, aspectMetaData);
        }
        catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Creates and registers the aspects defined.
     *
     * @param uuid the UUID for the AspectWerkz system to use
     * @param definition the AspectWerkz definition
     */
    private static void registerPointcuts(final String uuid,
                                          final AspectWerkzDefinitionImpl definition) {
        registerCFlowPointcuts(uuid, definition);
        registerExecutionPointcuts(uuid, definition);
        registerCallPointcuts(uuid, definition);
        registerSetPointcuts(uuid, definition);
        registerGetPointcuts(uuid, definition);
        registerThrowsPointcuts(uuid, definition);
    }

    /**
     * Registers the method pointcuts.
     *
     * @param uuid the UUID for the AspectWerkz system to use
     * @param definition the AspectWerkz definition
     */
    private static void registerExecutionPointcuts(final String uuid,
                                                   final AspectWerkzDefinitionImpl definition) {
        for (Iterator it = definition.getAspectDefinitions().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();

            AspectMetaData aspectMetaData = SystemLoader.getSystem(uuid).getAspectMetaData(aspectDef.getName());

            List aroundAdvices = aspectDef.getAroundAdvices();
            for (Iterator it2 = aroundAdvices.iterator(); it2.hasNext();) {
                AdviceDefinition adviceDef = (AdviceDefinition)it2.next();

                ExecutionPointcut pointcut = aspectMetaData.getExecutionPointcut(
                        adviceDef.getExpression().getExpression()
                );
                if (pointcut == null) {
                    pointcut = new ExecutionPointcut(uuid, adviceDef.getExpression());
                    aspectMetaData.addExecutionPointcut(pointcut);
                }
                pointcut.addAdvice(adviceDef.getName());

                // TODO: how to handle cflow?
//                    methodPointcut.setCFlowExpression(adviceDef.getExpression().getCFlowExpression());
            }
        }
    }

    /**
     * Registers the set field pointcuts.
     *
     * @param uuid the UUID for the AspectWerkz system to use
     * @param definition the AspectWerkz definition
     */
    private static void registerSetPointcuts(final String uuid,
                                             final AspectWerkzDefinitionImpl definition) {
        for (Iterator it = definition.getAspectDefinitions().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();

            AspectMetaData aspectMetaData = SystemLoader.getSystem(uuid).
                    getAspectMetaData(aspectDef.getName());

            List beforeAdvices = aspectDef.getBeforeAdvices();
            for (Iterator it2 = beforeAdvices.iterator(); it2.hasNext();) {
                AdviceDefinition adviceDef = (AdviceDefinition)it2.next();

                SetPointcut pointcut = aspectMetaData.getSetPointcut(
                        adviceDef.getExpression().getExpression()
                );
                if (pointcut == null) {
                    pointcut = new SetPointcut(uuid, adviceDef.getExpression());
                    aspectMetaData.addSetPointcut(pointcut);
                }
                pointcut.addBeforeAdvice(adviceDef.getName());
            }

            List afterAdvices = aspectDef.getAfterAdvices();
            for (Iterator it2 = afterAdvices.iterator(); it2.hasNext();) {
                AdviceDefinition adviceDef = (AdviceDefinition)it2.next();

                SetPointcut pointcut = aspectMetaData.getSetPointcut(
                        adviceDef.getExpression().getExpression()
                );
                if (pointcut == null) {
                    pointcut = new SetPointcut(uuid, adviceDef.getExpression());
                    aspectMetaData.addSetPointcut(pointcut);
                }
                pointcut.addAfterAdvice(adviceDef.getName());
            }
        }
    }

    /**
     * Registers the get field pointcuts.
     *
     * @param uuid the UUID for the AspectWerkz system to use
     * @param definition the AspectWerkz definition
     */
    private static void registerGetPointcuts(final String uuid,
                                             final AspectWerkzDefinitionImpl definition) {
        for (Iterator it = definition.getAspectDefinitions().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();

            AspectMetaData aspectMetaData = SystemLoader.getSystem(uuid).
                    getAspectMetaData(aspectDef.getName());

            List beforeAdvices = aspectDef.getBeforeAdvices();
            for (Iterator it2 = beforeAdvices.iterator(); it2.hasNext();) {
                AdviceDefinition adviceDef = (AdviceDefinition)it2.next();
                GetPointcut pointcut = aspectMetaData.getGetPointcut(
                        adviceDef.getExpression().getExpression()
                );
                if (pointcut == null) {
                    pointcut = new GetPointcut(uuid, adviceDef.getExpression());
                    aspectMetaData.addGetPointcut(pointcut);
                }
                pointcut.addBeforeAdvice(adviceDef.getName());
            }

            List afterAdvices = aspectDef.getAfterAdvices();
            for (Iterator it2 = afterAdvices.iterator(); it2.hasNext();) {
                AdviceDefinition adviceDef = (AdviceDefinition)it2.next();
                GetPointcut pointcut = aspectMetaData.getGetPointcut(
                        adviceDef.getExpression().getExpression()
                );
                if (pointcut == null) {
                    pointcut = new GetPointcut(uuid, adviceDef.getExpression());
                    aspectMetaData.addGetPointcut(pointcut);
                }
                pointcut.addAfterAdvice(adviceDef.getName());
            }
        }
    }

    /**
     * Registers the caller side pointcuts.
     *
     * @param uuid the UUID for the AspectWerkz system to use
     * @param definition the AspectWerkz definition
     */
    private static void registerCallPointcuts(final String uuid,
                                              final AspectWerkzDefinitionImpl definition) {
        // get all aspects definitions
        for (Iterator it1 = definition.getAspectDefinitions().iterator(); it1.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it1.next();
            AspectMetaData aspectMetaData = SystemLoader.getSystem(uuid).
                    getAspectMetaData(aspectDef.getName());

            List beforeAdvices = aspectDef.getBeforeAdvices();
            for (Iterator it2 = beforeAdvices.iterator(); it2.hasNext();) {
                AdviceDefinition adviceDef = (AdviceDefinition)it2.next();

                CallPointcut pointcut = aspectMetaData.getCallPointcut(
                        adviceDef.getExpression().getExpression()
                );
                if (pointcut == null) {
                    pointcut = new CallPointcut(uuid, adviceDef.getExpression());
                    aspectMetaData.addCallPointcut(pointcut);
                }
                pointcut.addBeforeAdvice(adviceDef.getName());
            }

            List afterAdvices = aspectDef.getAfterAdvices();
            for (Iterator it2 = afterAdvices.iterator(); it2.hasNext();) {
                AdviceDefinition adviceDef = (AdviceDefinition)it2.next();

                CallPointcut pointcut = aspectMetaData.getCallPointcut(
                        adviceDef.getExpression().getExpression()
                );
                if (pointcut == null) {
                    pointcut = new CallPointcut(uuid, adviceDef.getExpression());
                    aspectMetaData.addCallPointcut(pointcut);
                }
                pointcut.addAfterAdvice(adviceDef.getName());
            }
        }
    }

    /**
     * Registers the cflow pointcuts.
     *
     * @param uuid the UUID for the AspectWerkz system to use
     * @param definition the AspectWerkz definition
     */
    private static void registerCFlowPointcuts(final String uuid,
                                               final AspectWerkzDefinitionImpl definition) {
        // get all aspects definitions
        for (Iterator it1 = definition.getAspectDefinitions().iterator(); it1.hasNext();) {
            AspectDefinition aspectDefinition = (AspectDefinition)it1.next();
            AspectMetaData aspect = SystemLoader.getSystem(uuid).
                    getAspectMetaData(aspectDefinition.getName());

            // get all bind-advice rules defined in this aspect
            List advices = aspectDefinition.getAllAdvices();
//            for (Iterator it2 = advices.iterator(); it2.hasNext();) {
//                AdviceDefinition advideDef = (AdviceDefinition)it2.next();
//                aspect.getCallPointcut()
//
//
//                Expression expression = bindAdviceRule.getExpression();
//                Expression cflowExpression = bindAdviceRule.getCflowExpression();
//                if ( cflowExpression == null ) {
//                    continue;
//                }
//                // get the referenced cflow poincut definition
//                PointcutDefinition cflowPointcutDef =
//                        aspectDefinition.getPointcutDef(cflowExpression.getName());
//
//                // create call pointcut
//                CallPointcut pointcut = new CallPointcut(uuid, cflowExpression);
//
//                // register the cflow advices in the system (if they does not already exist)
//                //TODO: [alex] clean this - works as well when commented.
//                if (!SystemLoader.getSystem(uuid).hasAspect(CFlowPreAdvice.NAME)) {
//                    AdviceDefinition adviceDef = CFlowPreAdvice.getDefinition();
//                    // add the advice to the aspectwerkz definition
//                    definition.addAdvice(adviceDef);
//                    // add the advice to the aspectwerkz system
//                    registerAdvice(uuid, adviceDef);
//                }
//                if (!SystemLoader.getSystem(uuid).hasAspect(CFlowPostAdvice.NAME)) {
//                    AdviceDefinition adviceDef = CFlowPostAdvice.getDefinition();
//                    // add the advice to the aspectwerkz definition
//                    definition.addAdvice(adviceDef);
//                    // add the advice to the aspectwerkz system
//                    registerAdvice(uuid, adviceDef);
//                }
//
//                // add the pointcut definition to the method pointcut
//                pointcut.addPointcutDef(cflowPointcutDef);
//                // add references to the cflow advices to the cflow pointcut
//                pointcut.addBeforeAdvice(CFlowPreAdvice.NAME);
//                pointcut.addAfterAdvice(CFlowPostAdvice.NAME);
//                // add the method pointcut
//                aspect.addCallPointcut(pointcut);
//
//                aspect.addMethodToCflowExpressionMap(expression, cflowExpression);
//            }
//        }


        // get all aspects definitions
//        for (Iterator it1 = definition.getAspectDefinitions().iterator(); it1.hasNext();) {
//            AspectDefinition aspectDef = (AspectDefinition)it1.next();
//            AspectMetaData aspectMetaData = SystemLoader.getSystem(uuid).
//                    getAspectMetaData(aspectDef.getName());
//
//            try {
//                // get all advice weaving rules defined in this aspectMetaData
//                List advices = aspectDef.getAllAdvices();
//                for (Iterator it2 = advices.iterator(); it2.hasNext();) {
//                    AdviceDefinition adviceDef = (AdviceDefinition)it2.next();
//                    AdviceWeavingRule weavingRule = adviceDef.getExpression();
//
//                    String cflowExpression = weavingRule.getCFlowExpression();
//                    if (cflowExpression == null) {
//                        continue;
//                    }
//
//                    // get the referenced cflow poincut definition
//                    PointcutDefinition cflowPointcutDef = aspectDef.getPointcutDef(cflowExpression);
//
//                    // create caller side pointcut
//                    CallPointcut callerSidePointcut = new CallPointcut(
//                            uuid, cflowExpression
//                    );
//
//                    // check if we have a cflow pointcut
//                    if (!(cflowPointcutDef != null &&
//                            cflowPointcutDef.getType().equalsIgnoreCase(PointcutDefinition.CFLOW))) {
//                        continue;
//                    }
//
//                    // register the cflow advices in the system and create the cflow system aspect
//                    // (if it does not already exist)
//                    if (!SystemLoader.getSystem(uuid).hasAspect(CFlowSystemAspect.NAME)) {
//
//                        AspectDefinition cflowAspect = new AspectDefinition(
//                                CFlowSystemAspect.NAME,
//                                CFlowSystemAspect.CLASS_NAME,
//                                CFlowSystemAspect.DEPLOYMENT_MODEL
//                        );
//                        cflowAspect.addPointcut(cflowPointcutDef);
//
//                        Class cflowAspectClass = CFlowSystemAspect.class;
//
//                        // add the cflow pre advice
//                        cflowAspect.addBeforeAdvice(new AdviceDefinition(
//                                CFlowSystemAspect.PRE_ADVICE,
//                                cflowAspect.getName(),
//                                cflowAspect.getClassName(),
//                                cflowExpression,
//                                cflowAspectClass.getDeclaredMethod(
//                                        CFlowSystemAspect.PRE_ADVICE,
//                                        new Class[]{JoinPoint.class}),
//                                CFlowSystemAspect.PRE_ADVICE_INDEX,
//                                cflowAspect
//                        ));
//
//                        // add the cflow post advice
//                        cflowAspect.addAfterAdvice(new AdviceDefinition(
//                                CFlowSystemAspect.POST_ADVICE,
//                                cflowAspect.getName(),
//                                cflowAspect.getClassName(),
//                                cflowExpression,
//                                cflowAspectClass.getDeclaredMethod(
//                                        CFlowSystemAspect.POST_ADVICE,
//                                        new Class[]{JoinPoint.class}),
//                                CFlowSystemAspect.POST_ADVICE_INDEX,
//                                cflowAspect
//                        ));
//
//                        // add the advice to the aspectwerkz definition
//                        definition.addAspect(cflowAspect);
//
//                        // add the advice to the aspectwerkz system
//                        registerAspect(uuid, cflowAspect);
//                    }
//
//                    // add the pointcut definition to the method pointcut
//                    callerSidePointcut.addPointcutDef(cflowPointcutDef);
//
//                    // add references to the cflow advices to the cflow pointcut
//                    callerSidePointcut.addBeforeAdvice(CFlowSystemAspect.PRE_ADVICE);
//                    callerSidePointcut.addAfterAdvice(CFlowSystemAspect.POST_ADVICE);
//
//                    // add the method pointcut
//                    aspectMetaData.addCallPointcut(callerSidePointcut);
//
//                    // add a mapping between the cflow pattern and the method patterns affected
//                    for (Iterator it3 = weavingRule.getPointcutRefs().iterator(); it3.hasNext();) {
//                        PointcutDefinition pointcutDef = aspectDef.getPointcutDef((String)it3.next());
//                        if (pointcutDef != null && pointcutDef.getType().
//                                equalsIgnoreCase(PointcutDefinition.METHOD)) {
//
//                            aspectMetaData.addMethodToCFlowMethodMap(
//                                    pointcutDef.getPointcutPatternTuple(),
//                                    cflowPointcutDef.getPointcutPatternTuple());
//                        }
//                    }
//                }
//            }
//            catch (NullPointerException e) {
//                throw new DefinitionException("cflow pointcuts in aspect <" + aspectMetaData.getName() + "> are not properly defined");
//            }
//            catch (Exception e) {
//                throw new WrappedRuntimeException(e);
//            }
//        }
    }}

    /**
     * Registers the throws pointcuts.
     *
     * @param uuid the UUID for the AspectWerkz system to use
     * @param definition the AspectWerkz definition
     */
    private static void registerThrowsPointcuts(final String uuid,
                                                final AspectWerkzDefinitionImpl definition) {
        for (Iterator it = definition.getAspectDefinitions().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();

            AspectMetaData aspectMetaData = SystemLoader.getSystem(uuid).getAspectMetaData(aspectDef.getName());

            List aroundAdvices = aspectDef.getAroundAdvices();
            for (Iterator it2 = aroundAdvices.iterator(); it2.hasNext();) {
                AdviceDefinition adviceDef = (AdviceDefinition)it2.next();

                if (adviceDef.getExpression().getType().equals(PointcutType.THROWS)) {
                    ThrowsPointcut pointcut = aspectMetaData.getThrowsPointcut(
                            adviceDef.getExpression().getExpression()
                    );
                    if (pointcut == null) {
                        pointcut = new ThrowsPointcut(uuid, adviceDef.getExpression());
                        aspectMetaData.addThrowsPointcut(pointcut);
                    }
                    pointcut.addAdvice(adviceDef.getName());
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
