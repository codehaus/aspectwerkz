/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.xmldef.definition;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Collection;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Field;

import org.codehaus.aspectwerkz.SystemLoader;
import org.codehaus.aspectwerkz.AspectMetaData;
import org.codehaus.aspectwerkz.DeploymentModel;
import org.codehaus.aspectwerkz.ContextClassLoader;
import org.codehaus.aspectwerkz.regexp.CompiledPatternTuple;
import org.codehaus.aspectwerkz.pointcut.ExecutionPointcut;
import org.codehaus.aspectwerkz.pointcut.CallPointcut;
import org.codehaus.aspectwerkz.pointcut.SetPointcut;
import org.codehaus.aspectwerkz.pointcut.GetPointcut;
import org.codehaus.aspectwerkz.pointcut.ThrowsPointcut;
import org.codehaus.aspectwerkz.definition.AspectWerkzDefinition;
import org.codehaus.aspectwerkz.definition.PointcutDefinition;
import org.codehaus.aspectwerkz.definition.expression.PointcutType;
import org.codehaus.aspectwerkz.definition.expression.Expression;
import org.codehaus.aspectwerkz.definition.expression.CflowExpression;
import org.codehaus.aspectwerkz.definition.expression.LeafExpression;
import org.codehaus.aspectwerkz.definition.expression.ExpressionExpression;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.exception.DefinitionException;
import org.codehaus.aspectwerkz.xmldef.XmlDefSystem;
import org.codehaus.aspectwerkz.xmldef.advice.PreAdvice;
import org.codehaus.aspectwerkz.xmldef.advice.PostAdvice;
import org.codehaus.aspectwerkz.xmldef.advice.AbstractAdvice;
import org.codehaus.aspectwerkz.xmldef.advice.AdviceContainer;
import org.codehaus.aspectwerkz.xmldef.advice.CFlowPreAdvice;
import org.codehaus.aspectwerkz.xmldef.advice.CFlowPostAdvice;
import org.codehaus.aspectwerkz.xmldef.introduction.Introduction;
import org.codehaus.aspectwerkz.xmldef.introduction.IntroductionContainer;

/**
 * Manages the startup procedure, walks through the definition and instantiates
 * the aspects/advices/introduction/pointcuts.
 * <p/>
 * Reads the definition, either as a class of as an XML file.
 * <p/>
 * To use your XML definition file pass
 * <code>-Daspectwerkz.definition.file=PathToFile</code> as parameter to the JVM.
 * <p/>
 * If the above given parameter is not specified, the <code>StartupManager</code>
 * tries locate a file called <code>aspectwerkz.xml</code> in the classpath
 * and if this fails the last attempt is to use the
 * <code>ASPECTWERKZ_HOME/config/aspectwerkz.xml</code> file (if there is one).
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
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
     * The default introduction container class.
     */
    public static final String DEFAULT_INTRODUCTION_CONTAINER =
            "org.codehaus.aspectwerkz.xmldef.introduction.DefaultIntroductionContainerStrategy";

    /**
     * The default advice container class.
     */
    public static final String DEFAULT_ADVICE_CONTAINER =
            "org.codehaus.aspectwerkz.xmldef.advice.DefaultAdviceContainerStrategy";

    /**
     * The introduction container class to use.
     */
    public static final String INTRODUCTION_CONTAINER_IMPLEMENTATION_CLASS =
            System.getProperty("aspectwerkz.introduction.container.impl",
                    DEFAULT_INTRODUCTION_CONTAINER
            );

    /**
     * The advice container class to use.
     */
    public static final String ADVICE_CONTAINER_IMPLEMENTATION_CLASS =
            System.getProperty("aspectwerkz.advice.container.impl",
                    DEFAULT_ADVICE_CONTAINER
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

        AspectWerkzDefinitionImpl def = (AspectWerkzDefinitionImpl)definition;
        createAspects(uuid, def);
        registerIntroductions(uuid, def);
        registerAdvices(uuid, def);
        registerPointcuts(uuid, def);
        addIntroductionReferencesToAspects(uuid, def);
    }

    /**
     * Creates a new container for the introduction.
     *
     * @param implClass the introduction's implementation class
     */
    public static IntroductionContainer createIntroductionContainer(final Class implClass) {
        if (implClass == null) return null; // interface introduction; skip

        try {
            Class klass = ContextClassLoader.loadClass(INTRODUCTION_CONTAINER_IMPLEMENTATION_CLASS);
            Constructor constructor = klass.getConstructor(new Class[]{Class.class});
            return (IntroductionContainer)constructor.newInstance(new Object[]{implClass});
        }
        catch (Exception e) {
            StringBuffer cause = new StringBuffer();
            cause.append("could not create introduction container using specified class <");
            cause.append(INTRODUCTION_CONTAINER_IMPLEMENTATION_CLASS);
            cause.append(">: ");
            cause.append(e.getMessage());
            throw new RuntimeException(cause.toString());
        }
    }

    /**
     * Creates a new container for the advice.
     *
     * @param prototype the advice's prototype
     */
    public static AdviceContainer createAdviceContainer(final AbstractAdvice prototype) {
        if (prototype == null) throw new IllegalArgumentException("advice prototype can not be null");

        try {
            Class klass = ContextClassLoader.loadClass(ADVICE_CONTAINER_IMPLEMENTATION_CLASS);
            Constructor constructor = klass.getConstructor(new Class[]{AbstractAdvice.class});
            return (AdviceContainer)constructor.newInstance(new Object[]{prototype});
        }
        catch (Exception e) {
            StringBuffer cause = new StringBuffer();
            cause.append("could not create advice container using specified class <");
            cause.append(ADVICE_CONTAINER_IMPLEMENTATION_CLASS);
            cause.append(">: ");
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
    private static void createAspects(final String uuid,
                                      final AspectWerkzDefinitionImpl definition) {
        try {
            for (Iterator it = definition.getAspectDefinitions().iterator(); it.hasNext();) {
                AspectDefinition aspectDefinition = (AspectDefinition)it.next();
                ((XmlDefSystem)SystemLoader.getSystem(uuid)).register(
                        new AspectMetaData(uuid, aspectDefinition.getName())
                );
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
     * Registers the introductions.
     *
     * @param uuid the UUID for the AspectWerkz system to use
     * @param definition the aspectwerkz definition
     */
    private static void registerIntroductions(final String uuid,
                                              final AspectWerkzDefinitionImpl definition) {
        // get all introduction definitions
        for (Iterator it1 = definition.getIntroductionDefinitions().iterator(); it1.hasNext();) {
            registerIntroduction(uuid, (IntroductionDefinition)it1.next());
        }
    }

    /**
     * Registers the introduction specified.
     *
     * @param uuid the UUID for the AspectWerkz system to use
     * @param introDef the introduction definition
     */
    private static void registerIntroduction(final String uuid,
                                             final IntroductionDefinition introDef) {

        final String implClassName = introDef.getImplementation();
        final String intfClassName = introDef.getInterface();

        try {
            Class implClass = null;
            if (implClassName != null) { // we have an implementation introduction
                // load the introduction class
                try {
                    implClass = ContextClassLoader.loadClass(implClassName);
                }
                catch (ClassNotFoundException e) {
                    throw new RuntimeException(implClassName + " could not be found on classpath");
                }
            }
            final Introduction newIntroduction = new Introduction(
                    introDef.getName(),
                    intfClassName,
                    implClass,
                    DeploymentModel.getDeploymentModelAsInt(introDef.getDeploymentModel()));

            // create and set the container for the introduction
            IntroductionContainer container = createIntroductionContainer(implClass);
            if (container != null) {
                newIntroduction.setContainer(container);
            }

            ((XmlDefSystem)SystemLoader.getSystem(uuid)).
                    register(introDef.getName(), newIntroduction);
        }
        catch (NullPointerException e) {
            throw new DefinitionException("introduction definitions not properly defined");
        }
        catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Adds the introduction references to the aspects.
     *
     * @param uuid the UUID for the AspectWerkz system to use
     * @param definition the definition
     */
    private static void addIntroductionReferencesToAspects(final String uuid,
                                                           final AspectWerkzDefinitionImpl definition) {

        try {
            // get all aspects definitions
            for (Iterator it1 = definition.getAspectDefinitions().iterator(); it1.hasNext();) {
                AspectDefinition aspectDef = (AspectDefinition)it1.next();
                List weavingRules = aspectDef.getBindIntroductionRules();
                for (Iterator it2 = weavingRules.iterator(); it2.hasNext();) {
                    BindIntroductionRule weavingRule = (BindIntroductionRule)it2.next();

                    List introductionRefs = weavingRule.getIntroductionRefs();
                    for (Iterator it3 = introductionRefs.iterator(); it3.hasNext();) {
                        IntroductionDefinition def =
                                definition.getIntroductionDefinition((String)it3.next());

                        // add the introdution
                        SystemLoader.getSystem(uuid).
                                getAspectMetaData(aspectDef.getName()).
                                addIntroduction(def.getName());
                    }
                }
            }
        }
        catch (NullPointerException e) {
            throw new DefinitionException("introduction definitions not properly defined");
        }
        catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }

    }

    /**
     * Creates and registers the advices defined.
     *
     * @param uuid the UUID for the AspectWerkz system to use
     * @param definition the aspectwerkz definition
     */
    private static void registerAdvices(final String uuid,
                                        final AspectWerkzDefinitionImpl definition) {
        for (Iterator it = definition.getAdviceDefinitions().iterator(); it.hasNext();) {
            registerAdvice(uuid, (AdviceDefinition)it.next());
        }
    }

    /**
     * Creates and registers the advice specified.
     *
     * @param uuid the UUID for the AspectWerkz system to use
     */
    private static void registerAdvice(final String uuid, final AdviceDefinition def) {
        final String adviceClassName = def.getAdviceClassName();
        final String name = def.getName();

        try {
            final Class adviceClass = ContextClassLoader.loadClass(adviceClassName);

            final AbstractAdvice newAdvice = (AbstractAdvice)adviceClass.
                    getConstructor(new Class[]{}).
                    newInstance(new Object[]{});

            int deploymentModel;
            if (def.getDeploymentModel() == null || def.getDeploymentModel().equals("")) {
                deploymentModel = DeploymentModel.PER_JVM;
            }
            else {
                deploymentModel = DeploymentModel.getDeploymentModelAsInt(def.getDeploymentModel());
            }

            // TODO: use custom security to protect the [Aspect.m_uuid] field from getting modified by the user, setting the field opens up for changes in other AspectWerkz system running in the same JVM
            Field field = AbstractAdvice.class.getDeclaredField("m_uuid");
            field.setAccessible(true);
            field.set(newAdvice, uuid);
            newAdvice.setName(def.getName());
            newAdvice.setAdviceClass(adviceClass);
            newAdvice.setDeploymentModel(deploymentModel);

            // handle the parameters passed to the advice
            for (Iterator it2 = def.getParameters().entrySet().iterator(); it2.hasNext();) {
                Map.Entry entry = (Map.Entry)it2.next();
                newAdvice.setParameter((String)entry.getKey(), (String)entry.getValue());
            }

            // create and set the container for the advice
            newAdvice.setContainer(createAdviceContainer(newAdvice));

            ((XmlDefSystem)SystemLoader.getSystem(uuid)).register(name, newAdvice);
        }
        catch (ClassNotFoundException e) {
            throw new DefinitionException(adviceClassName + " could not be found in classpath");
        }
        catch (NoSuchMethodException e) {
            throw new DefinitionException(adviceClassName + " must define a constructor that takes an integer as an argument");
        }
        catch (InvocationTargetException e) {
            throw new WrappedRuntimeException(e.getTargetException());
        }
        catch (NullPointerException e) {
            throw new DefinitionException("advice definitions not properly defined");
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
     * Registers the execution pointcuts.
     *
     * @param uuid the UUID for the AspectWerkz system to use
     * @param definition the AspectWerkz definition
     */
    private static void registerExecutionPointcuts(final String uuid,
                                                   final AspectWerkzDefinitionImpl definition) {
        // get all aspects definitions
        for (Iterator it1 = definition.getAspectDefinitions().iterator(); it1.hasNext();) {
            AspectDefinition aspectDefinition = (AspectDefinition)it1.next();
            AspectMetaData aspect = SystemLoader.getSystem(uuid).
                    getAspectMetaData(aspectDefinition.getName());

            try {
                // get all bind-advice rules defined in this aspect
                List bindAdviceRules = aspectDefinition.getBindAdviceRules();
                for (Iterator it2 = bindAdviceRules.iterator(); it2.hasNext();) {
                    BindAdviceRule bindAdviceRule = (BindAdviceRule)it2.next();

                    // create execution pointcut
                    Expression expression = bindAdviceRule.getExpression();
                    if (!expression.getType().equals(PointcutType.EXECUTION)) {
                        continue;
                    }
                    ExecutionPointcut pointcut = new ExecutionPointcut(uuid, expression);

                    // add advice references
                    List adviceRefs = bindAdviceRule.getAdviceRefs();
                    for (Iterator it3 = adviceRefs.iterator(); it3.hasNext();) {
                        pointcut.addAdvice((String)it3.next());
                    }
                    // add advices from advice stacks
                    List adviceStackRefs = bindAdviceRule.getAdviceStackRefs();
                    for (Iterator it3 = adviceStackRefs.iterator(); it3.hasNext();) {
                        AdviceStackDefinition adviceStackDefinition =
                                definition.getAdviceStackDefinition((String)it3.next());

                        List advices = adviceStackDefinition.getAdviceRefs();
                        for (Iterator it4 = advices.iterator(); it4.hasNext();) {
                            pointcut.addAdvice((String)it4.next());
                        }
                    }
                    // add the method pointcut
                    aspect.addExecutionPointcut(pointcut);
                }
            }
            catch (NullPointerException e) {
                throw new DefinitionException("execution pointcuts in aspect <" + aspect.getName() + "> are not properly defined");
            }
            catch (Exception e) {
                throw new WrappedRuntimeException(e);
            }
        }
    }

    /**
     * Registers the call pointcuts.
     *
     * @param uuid the UUID for the AspectWerkz system to use
     * @param definition the AspectWerkz definition
     */
    private static void registerCallPointcuts(final String uuid,
                                              final AspectWerkzDefinitionImpl definition) {
        // get all aspects definitions
        for (Iterator it1 = definition.getAspectDefinitions().iterator(); it1.hasNext();) {
            AspectDefinition aspectDefinition = (AspectDefinition)it1.next();
            AspectMetaData aspect = SystemLoader.getSystem(uuid).
                    getAspectMetaData(aspectDefinition.getName());

            try {
                // get all bind-advice rules defined in this aspect
                List bindAdviceRules = aspectDefinition.getBindAdviceRules();
                for (Iterator it2 = bindAdviceRules.iterator(); it2.hasNext();) {
                    BindAdviceRule bindAdviceRule = (BindAdviceRule)it2.next();

                    // create call pointcut
                    Expression expression = bindAdviceRule.getExpression();
                    if (!expression.getType().equals(PointcutType.CALL)) {
                        continue;
                    }
                    CallPointcut pointcut = new CallPointcut(uuid, expression);

                    // add before and after advices
                    List adviceRefs = bindAdviceRule.getAdviceRefs();
                    for (Iterator it3 = adviceRefs.iterator(); it3.hasNext();) {
                        String adviceRef = (String)it3.next();
                        if (((XmlDefSystem)SystemLoader.getSystem(uuid)).
                                getAdvice(adviceRef) instanceof PreAdvice) {
                            pointcut.addBeforeAdvice(adviceRef);
                        }
                        else if (((XmlDefSystem)SystemLoader.getSystem(uuid)).
                                getAdvice(adviceRef) instanceof PostAdvice) {
                            pointcut.addAfterAdvice(adviceRef);
                        }
                    }

                    // add advices from advice stacks
                    List adviceStackRefs = bindAdviceRule.getAdviceStackRefs();
                    for (Iterator it3 = adviceStackRefs.iterator(); it3.hasNext();) {
                        AdviceStackDefinition adviceStackDefinition =
                                definition.getAdviceStackDefinition((String)it3.next());

                        adviceRefs = adviceStackDefinition.getAdviceRefs();
                        for (Iterator it4 = adviceRefs.iterator(); it4.hasNext();) {
                            String adviceRef = (String)it4.next();
                            if (((XmlDefSystem)SystemLoader.getSystem(uuid)).
                                    getAdvice(adviceRef) instanceof PreAdvice) {
                                pointcut.addBeforeAdvice(adviceRef);
                            }
                            else if (((XmlDefSystem)SystemLoader.getSystem(uuid)).
                                    getAdvice(adviceRef) instanceof PostAdvice) {
                                pointcut.addAfterAdvice(adviceRef);
                            }
                        }
                    }
                    // add the call pointcut
                    aspect.addCallPointcut(pointcut);
                }
            }
            catch (NullPointerException e) {
                throw new DefinitionException("call pointcuts in aspect <" + aspect.getName() + "> are not properly defined");
            }
            catch (Exception e) {
                throw new WrappedRuntimeException(e);
            }
        }
    }

    /**
     * Registers the set pointcuts.
     *
     * @param uuid the UUID for the AspectWerkz system to use
     * @param definition the AspectWerkz definition
     */
    private static void registerSetPointcuts(final String uuid,
                                             final AspectWerkzDefinitionImpl definition) {
        // get all aspects definitions
        for (Iterator it1 = definition.getAspectDefinitions().iterator(); it1.hasNext();) {
            AspectDefinition aspectDefinition = (AspectDefinition)it1.next();
            AspectMetaData aspect = SystemLoader.getSystem(uuid).
                    getAspectMetaData(aspectDefinition.getName());

            try {
                // get all bind-advice rules defined in this aspect
                List bindAdviceRules = aspectDefinition.getBindAdviceRules();
                for (Iterator it2 = bindAdviceRules.iterator(); it2.hasNext();) {
                    BindAdviceRule bindAdviceRule = (BindAdviceRule)it2.next();

                    // create set pointcut
                    Expression expression = bindAdviceRule.getExpression();
                    if (!expression.getType().equals(PointcutType.SET)) {
                        continue;
                    }
                    SetPointcut pointcut = new SetPointcut(uuid, expression);

                    // add before and after advices
                    List adviceRefs = bindAdviceRule.getAdviceRefs();
                    for (Iterator it3 = adviceRefs.iterator(); it3.hasNext();) {
                        String adviceRef = (String)it3.next();
                        if (((XmlDefSystem)SystemLoader.getSystem(uuid)).
                                getAdvice(adviceRef) instanceof PreAdvice) {
                            pointcut.addBeforeAdvice(adviceRef);
                        }
                        else if (((XmlDefSystem)SystemLoader.getSystem(uuid)).
                                getAdvice(adviceRef) instanceof PostAdvice) {
                            pointcut.addAfterAdvice(adviceRef);
                        }
                    }

                    // add advices from advice stacks
                    List adviceStackRefs = bindAdviceRule.getAdviceStackRefs();
                    for (Iterator it3 = adviceStackRefs.iterator(); it3.hasNext();) {
                        AdviceStackDefinition adviceStackDefinition =
                                definition.getAdviceStackDefinition((String)it3.next());

                        adviceRefs = adviceStackDefinition.getAdviceRefs();
                        for (Iterator it4 = adviceRefs.iterator(); it4.hasNext();) {
                            String adviceRef = (String)it4.next();
                            if (((XmlDefSystem)SystemLoader.getSystem(uuid)).
                                    getAdvice(adviceRef) instanceof PreAdvice) {
                                pointcut.addBeforeAdvice(adviceRef);
                            }
                            else if (((XmlDefSystem)SystemLoader.getSystem(uuid)).
                                    getAdvice(adviceRef) instanceof PostAdvice) {
                                pointcut.addAfterAdvice(adviceRef);
                            }
                        }
                    }

                    // add the set pointcut
                    aspect.addSetPointcut(pointcut);
                }
            }
            catch (NullPointerException e) {
                throw new DefinitionException("set pointcuts in aspect <" + aspect.getName() + "> are not properly defined");
            }
            catch (Exception e) {
                throw new WrappedRuntimeException(e);
            }
        }
    }

    /**
     * Registers the get pointcuts.
     *
     * @param uuid the UUID for the AspectWerkz system to use
     * @param definition the AspectWerkz definition
     */
    private static void registerGetPointcuts(final String uuid,
                                             final AspectWerkzDefinitionImpl definition) {
        // get all aspects definitions
        for (Iterator it1 = definition.getAspectDefinitions().iterator(); it1.hasNext();) {
            AspectDefinition aspectDefinition = (AspectDefinition)it1.next();
            AspectMetaData aspect = SystemLoader.getSystem(uuid).
                    getAspectMetaData(aspectDefinition.getName());

            try {
                // get all bind-advice rules defined in this aspect
                List bindAdviceRules = aspectDefinition.getBindAdviceRules();
                for (Iterator it2 = bindAdviceRules.iterator(); it2.hasNext();) {
                    BindAdviceRule bindAdviceRule = (BindAdviceRule)it2.next();

                    // create get pointcut
                    Expression expression = bindAdviceRule.getExpression();
                    if (!expression.getType().equals(PointcutType.GET)) {
                        continue;
                    }
                    GetPointcut pointcut = new GetPointcut(uuid, expression);

                    // add before and after advices
                    List adviceRefs = bindAdviceRule.getAdviceRefs();
                    for (Iterator it3 = adviceRefs.iterator(); it3.hasNext();) {
                        String adviceRef = (String)it3.next();
                        if (((XmlDefSystem)SystemLoader.getSystem(uuid)).
                                getAdvice(adviceRef) instanceof PreAdvice) {
                            pointcut.addBeforeAdvice(adviceRef);
                        }
                        else if (((XmlDefSystem)SystemLoader.getSystem(uuid)).
                                getAdvice(adviceRef) instanceof PostAdvice) {
                            pointcut.addAfterAdvice(adviceRef);
                        }
                    }

                    // add advices from advice stacks
                    List adviceStackRefs = bindAdviceRule.getAdviceStackRefs();
                    for (Iterator it3 = adviceStackRefs.iterator(); it3.hasNext();) {
                        AdviceStackDefinition adviceStackDefinition =
                                definition.getAdviceStackDefinition((String)it3.next());

                        adviceRefs = adviceStackDefinition.getAdviceRefs();
                        for (Iterator it4 = adviceRefs.iterator(); it4.hasNext();) {
                            String adviceRef = (String)it4.next();
                            if (((XmlDefSystem)SystemLoader.getSystem(uuid)).
                                    getAdvice(adviceRef) instanceof PreAdvice) {
                                pointcut.addBeforeAdvice(adviceRef);
                            }
                            else if (((XmlDefSystem)SystemLoader.getSystem(uuid)).
                                    getAdvice(adviceRef) instanceof PostAdvice) {
                                pointcut.addAfterAdvice(adviceRef);
                            }
                        }
                    }

                    // add the get pointcut
                    aspect.addGetPointcut(pointcut);
                }
            }
            catch (NullPointerException e) {
                throw new DefinitionException("get pointcuts in aspect <" + aspect.getName() + "> are not properly defined");
            }
            catch (Exception e) {
                throw new WrappedRuntimeException(e);
            }
        }
    }

    /**
     * Registers the throws pointcuts.
     *
     * @param uuid the UUID for the AspectWerkz system to use
     * @param definition the AspectWerkz definition
     */
    private static void registerThrowsPointcuts(final String uuid,
                                                final AspectWerkzDefinitionImpl definition) {
        // get all aspects definitions
        for (Iterator it1 = definition.getAspectDefinitions().iterator(); it1.hasNext();) {
            AspectDefinition aspectDefinition = (AspectDefinition)it1.next();
            AspectMetaData aspect = SystemLoader.getSystem(uuid).
                    getAspectMetaData(aspectDefinition.getName());

            try {
                // get all bind-advice rules defined in this aspect
                List bindAdviceRules = aspectDefinition.getBindAdviceRules();
                for (Iterator it2 = bindAdviceRules.iterator(); it2.hasNext();) {
                    BindAdviceRule bindAdviceRule = (BindAdviceRule)it2.next();

                    // create throws pointcut
                    Expression expression = bindAdviceRule.getExpression();
                    if (!expression.getType().equals(PointcutType.THROWS)) {
                        continue;
                    }
                    ThrowsPointcut pointcut = new ThrowsPointcut(uuid, expression);

                    // add advices
                    List adviceRefs = bindAdviceRule.getAdviceRefs();
                    for (Iterator it3 = adviceRefs.iterator(); it3.hasNext();) {
                        String asdf = (String)it3.next();
                        pointcut.addAdvice(asdf);
                    }

                    // add advices from advice stacks
                    List adviceStackRefs = bindAdviceRule.getAdviceStackRefs();
                    for (Iterator it3 = adviceStackRefs.iterator(); it3.hasNext();) {
                        AdviceStackDefinition adviceStackDefinition =
                                definition.getAdviceStackDefinition((String)it3.next());

                        List advices = adviceStackDefinition.getAdviceRefs();
                        for (Iterator it4 = advices.iterator(); it4.hasNext();) {
                            pointcut.addAdvice((String)it4.next());
                        }
                    }

                    // add the throws pointcut
                    aspect.addThrowsPointcut(pointcut);
                }
            }
            catch (NullPointerException e) {
                throw new DefinitionException("throws pointcuts in aspect <" + aspect.getName() + "> are not properly defined");
            }
            catch (Exception e) {
                throw new WrappedRuntimeException(e);
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
            List bindAdviceRules = aspectDefinition.getBindAdviceRules();
            for (Iterator it2 = bindAdviceRules.iterator(); it2.hasNext();) {
                BindAdviceRule bindAdviceRule = (BindAdviceRule)it2.next();

                Expression expression = bindAdviceRule.getExpression();
                for (Iterator it3 = expression.getCflowExpressions().entrySet().iterator(); it3.hasNext();) {
                    Map.Entry entry = (Map.Entry) it3.next();
                    Expression value = (Expression) entry.getValue();
                    if (value instanceof ExpressionExpression) {
                        // recursive
                        //TODO exprexpr using exprexpr
                        // like pc cflow = "a or b"
                        // .. pc exec = "c IN cflow"
                        (new Exception("todo")).printStackTrace();
                    } else {
                        // get the referenced cflow poincut definition
                        PointcutDefinition cflowPointcutDef =
                                aspectDefinition.getPointcutDef(value.getName());

                        // create call pointcut
                        CallPointcut pointcut = new CallPointcut(uuid, value);

                        // register the cflow advices in the system (if they does not already exist)
                        //TODO: [alex] clean this - works as well when commented.
                        if (!SystemLoader.getSystem(uuid).hasAspect(CFlowPreAdvice.NAME)) {
                            AdviceDefinition adviceDef = CFlowPreAdvice.getDefinition();
                            // add the advice to the aspectwerkz definition
                            definition.addAdvice(adviceDef);
                            // add the advice to the aspectwerkz system
                            registerAdvice(uuid, adviceDef);
                        }
                        if (!SystemLoader.getSystem(uuid).hasAspect(CFlowPostAdvice.NAME)) {
                            AdviceDefinition adviceDef = CFlowPostAdvice.getDefinition();
                            // add the advice to the aspectwerkz definition
                            definition.addAdvice(adviceDef);
                            // add the advice to the aspectwerkz system
                            registerAdvice(uuid, adviceDef);
                        }

                        // add the pointcut definition to the method pointcut
                        pointcut.addPointcutDef(cflowPointcutDef);
                        // add references to the cflow advices to the cflow pointcut
                        pointcut.addBeforeAdvice(CFlowPreAdvice.NAME);
                        pointcut.addAfterAdvice(CFlowPostAdvice.NAME);
                        // add the method pointcut
                        aspect.addCallPointcut(pointcut);

                        //TODO USELESS - does not support NOT IN
                        // impl a visitor
                        aspect.addMethodToCflowExpressionMap(expression, value);
                    }
                }
//                //expression.
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
            }
        }
    }

    /**
     * Private constructor to prevent instantiability.
     */
    private StartupManager() {
    }
}
