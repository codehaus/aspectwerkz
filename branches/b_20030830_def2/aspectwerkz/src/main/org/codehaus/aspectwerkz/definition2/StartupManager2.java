/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition2;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Field;

import org.codehaus.aspectwerkz.Aspect;
import org.codehaus.aspectwerkz.AspectWerkz;
import org.codehaus.aspectwerkz.DeploymentModel;
import org.codehaus.aspectwerkz.ContextClassLoader;
import org.codehaus.aspectwerkz.AbstractAspect;
import org.codehaus.aspectwerkz.AspectContainer;
import org.codehaus.aspectwerkz.definition.PointcutDefinition;
import org.codehaus.aspectwerkz.definition2.AspectWerkzDefinition2;
import org.codehaus.aspectwerkz.definition2.AspectDefinition2;
import org.codehaus.aspectwerkz.definition2.IntroductionDefinition2;
import org.codehaus.aspectwerkz.definition2.AdviceDefinition2;
import org.codehaus.aspectwerkz.definition2.PointcutDefinition2;
import org.codehaus.aspectwerkz.pointcut.MethodPointcut;
import org.codehaus.aspectwerkz.pointcut.FieldPointcut;
import org.codehaus.aspectwerkz.pointcut.ThrowsPointcut;
import org.codehaus.aspectwerkz.pointcut.CallerSidePointcut;
import org.codehaus.aspectwerkz.advice.PreAdvice;
import org.codehaus.aspectwerkz.advice.PostAdvice;
import org.codehaus.aspectwerkz.advice.AbstractAdvice;
import org.codehaus.aspectwerkz.advice.CFlowPreAdvice;
import org.codehaus.aspectwerkz.advice.CFlowPostAdvice;
import org.codehaus.aspectwerkz.introduction.Introduction;
import org.codehaus.aspectwerkz.introduction.IntroductionContainer;
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
 * If the above given parameter is not specified, the <code>StartupManager2</code>
 * tries locate a file called <code>aspectwerkz.xml</code> in the classpath
 * and if this fails the last attempt is to use the
 * <code>ASPECTWERKZ_HOME/config/aspectwerkz.xml</code> file (if there is one).
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class StartupManager2 {

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
            "org.codehaus.aspectwerkz.DefaultAspectContainerStrategy";

    /**
     * The aspect container class to use.
     */
    public static final String ASPECT_CONTAINER_IMPLEMENTATION_CLASS =
            System.getProperty("aspectwerkz.aspect.container.impl", DEFAULT_ASPECT_CONTAINER);

    /**
     * Marks the manager as initialized or not.
     */
    private static boolean s_initialized = false;

    /**
     * Loads the system definition.
     *
     * @param uuid the UUID for the weave model to load
     */
    public static void initializeSystem(final String uuid) {
        if (uuid == null) throw new IllegalArgumentException("uuid can not be null");
        if (s_initialized) return;
        s_initialized = true;
        final AspectWerkzDefinition2 definition = AspectWerkzDefinition2.getDefinition(uuid);
        registerAspects(uuid, definition);
        registerPointcuts(uuid, definition);
    }

    /**
     * Creates a new container for the aspect.
     *
     * @param implClass the aspect's implementation class
     */
    public static AspectContainer createAspectContainer(final AbstractAspect aspect) {
        if (aspect == null) throw new IllegalArgumentException("aspect can not be null");

        try {
            Class klass = ContextClassLoader.loadClass(ASPECT_CONTAINER_IMPLEMENTATION_CLASS);
            Constructor constructor = klass.getConstructor(new Class[]{Class.class});
            return (AspectContainer)constructor.newInstance(new Object[]{aspect});
        }
        catch (Exception e) {
            StringBuffer cause = new StringBuffer();
            cause.append("could not create aspect container using specified class <");
            cause.append(ASPECT_CONTAINER_IMPLEMENTATION_CLASS);
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
    private static void registerAspects(final String uuid, final AspectWerkzDefinition2 definition) {
        try {
            for (Iterator it = definition.getAspectDefinitions().iterator(); it.hasNext();) {
                AspectDefinition2 aspectDef = (AspectDefinition2)it.next();
                AspectWerkz.getSystem(uuid).register(new Aspect(uuid, aspectDef.getName()));
                registerAspect(uuid, aspectDef);
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
    private static void registerAspect(final String uuid, final AspectDefinition2 aspectDef) {

        final Class aspectClass = aspectDef.getKlass();

        try {
            AbstractAspect aspect = (AbstractAspect)aspectClass.newInstance();

            int deploymentModel;
            if (aspectDef.getDeploymentModel() == null || aspectDef.getDeploymentModel().equals("")) {
                deploymentModel = DeploymentModel.PER_JVM;
            }
            else {
                deploymentModel = DeploymentModel.
                        getDeploymentModelAsInt(aspectDef.getDeploymentModel());
            }

            Field field = AbstractAspect.class.getDeclaredField("m_uuid");
            field.setAccessible(true);
            field.set(aspect, uuid);
            aspect.setName(aspectDef.getName());
            aspect.setAspectClass(aspectClass);
            aspect.setDeploymentModel(deploymentModel);

            // handle the parameters passed to the advice
//            for (Iterator it2 = aspectDef.getParameters().entrySet().iterator(); it2.hasNext();) {
//                Map.Entry entry = (Map.Entry)it2.next();
//                aspect.setParameter((String)entry.getKey(), (String)entry.getValue());
//            }

            // create and set the container for the aspect
            AspectContainer container = createAspectContainer(aspect);
            if (container != null) {
                aspect.setContainer(container);
            }

            AspectWerkz.getSystem(uuid).register(aspect.getName(), aspect);
        }
        catch (NullPointerException e) {
            throw new DefinitionException("introduction definitions not properly defined");
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
                                          final AspectWerkzDefinition2 definition) {
        registerCFlowPointcuts(uuid, definition);
        registerMethodPointcuts(uuid, definition);
        registerSetFieldPointcuts(uuid, definition);
        registerGetFieldPointcuts(uuid, definition);
        registerThrowsPointcuts(uuid, definition);
        registerCallerSidePointcuts(uuid, definition);
    }

    /**
     * Registers the method pointcuts.
     *
     * @param uuid the UUID for the AspectWerkz system to use
     * @param definition the AspectWerkz definition
     */
    private static void registerMethodPointcuts2(final String uuid,
                                                 final AspectWerkzDefinition2 definition) {
        for (Iterator it = definition.getAspectDefinitions().iterator(); it.hasNext();) {
            AspectDefinition2 aspectDef = (AspectDefinition2)it.next();
            Set aroundAdvices = aspectDef.getAroundAdvices();
            for (Iterator it2 = aroundAdvices.iterator(); it2.hasNext();) {
                AdviceDefinition2 adviceDef = (AdviceDefinition2)it2.next();
                MethodPointcut methodPointcut = new MethodPointcut(
                        uuid,
                        adviceDef.getPointcut()
                );
                boolean hasMethodPointcut = false;
                List pointcutRefs = adviceDef.getPointcutRefs();
                for (Iterator it3 = pointcutRefs.iterator(); it3.hasNext();) {
                    String pointcutName = (String)it3.next();
                    PointcutDefinition2 pointcutDef = aspectDef.getPointcutDef(pointcutName);
                    if (pointcutDef != null && pointcutDef.getType().
                            equalsIgnoreCase(PointcutDefinition.METHOD)) {
                        methodPointcut.addPointcutDef(pointcutDef);
                        hasMethodPointcut = true;
                    }
                }
                // check if the weaving rule had a method pointcut, if not continue
                if (!hasMethodPointcut) {
                    continue;
                }




                // add advice references
                List adviceRefs = weavingRule.getAdviceRefs();
                for (Iterator it3 = adviceRefs.iterator(); it3.hasNext();) {
                    methodPointcut.addAdvice((String)it3.next());
                }
                // add the method pointcut
                aspect.addMethodPointcut(methodPointcut);
            }
        }
    }

    /**
     * Registers the method pointcuts.
     *
     * @param uuid the UUID for the AspectWerkz system to use
     * @param definition the AspectWerkz definition
     */
    private static void registerMethodPointcuts(final String uuid,
                                                final AspectWerkzDefinition2 definition) {

        // get all aspects definitions
        for (Iterator it1 = definition.getAspectDefinitions().iterator(); it1.hasNext();) {
            AspectDefinition2 aspectDefinition = (AspectDefinition2)it1.next();
            Aspect aspect = AspectWerkz.getSystem(uuid).getAspect(aspectDefinition.getName());

            try {
                // get all advice weaving rules defined in this aspect
                List adviceWeavingRules = aspectDefinition.getAdviceWeavingRules();
                for (Iterator it2 = adviceWeavingRules.iterator(); it2.hasNext();) {
                    AdviceWeavingRule weavingRule = (AdviceWeavingRule)it2.next();

                    // create method pointcut
                    MethodPointcut methodPointcut = new MethodPointcut(
                            uuid,
                            weavingRule.getExpression()
                    );

                    // add all referenced method poincuts definitions
                    boolean hasMethodPointcut = false;
                    List methodPointcutRefs = weavingRule.getPointcutRefs();
                    for (Iterator it3 = methodPointcutRefs.iterator(); it3.hasNext();) {
                        String pointcutName = (String)it3.next();
                        PointcutDefinition pointcutDefinition =
                                aspectDefinition.getPointcutDef(pointcutName);
                        if (pointcutDefinition != null && pointcutDefinition.getType().
                                equalsIgnoreCase(PointcutDefinition.METHOD)) {
                            methodPointcut.addPointcutDef(pointcutDefinition);
                            hasMethodPointcut = true;
                        }
                    }
                    // check if the weaving rule had a method pointcut, if not continue
                    if (!hasMethodPointcut) {
                        continue;
                    }
                    // add advice references
                    List adviceRefs = weavingRule.getAdviceRefs();
                    for (Iterator it3 = adviceRefs.iterator(); it3.hasNext();) {
                        methodPointcut.addAdvice((String)it3.next());
                    }
                    // add advices from advice stacks
                    List adviceStackRefs = weavingRule.getAdviceStackRefs();
                    for (Iterator it3 = adviceStackRefs.iterator(); it3.hasNext();) {
                        AdviceStackDefinition adviceStackDefinition =
                                definition.getAdviceStackDefinition((String)it3.next());

                        List advices = adviceStackDefinition.getAdviceRefs();
                        for (Iterator it4 = advices.iterator(); it4.hasNext();) {
                            methodPointcut.addAdvice((String)it4.next());
                        }
                    }
                    // add the method pointcut
                    aspect.addMethodPointcut(methodPointcut);
                }
            }
            catch (NullPointerException e) {
                throw new DefinitionException("method pointcuts in aspect <" + aspect.getName() + "> are not properly defined");
            }
            catch (Exception e) {
                throw new WrappedRuntimeException(e);
            }
        }
    }

    /**
     * Registers the set field pointcuts.
     *
     * @param uuid the UUID for the AspectWerkz system to use
     * @param definition the AspectWerkz definition
     */
    private static void registerSetFieldPointcuts(
            final String uuid,
            final AspectWerkzDefinition2 definition) {

        // get all aspects definitions
        for (Iterator it1 = definition.getAspectDefinitions().iterator(); it1.hasNext();) {
            AspectDefinition2 aspectDefinition = (AspectDefinition2)it1.next();
            Aspect aspect = AspectWerkz.getSystem(uuid).getAspect(aspectDefinition.getName());

            try {
                // get all advice weaving rules defined in this aspect
                List adviceWeavingRules = aspectDefinition.getAdviceWeavingRules();
                for (Iterator it2 = adviceWeavingRules.iterator(); it2.hasNext();) {
                    AdviceWeavingRule weavingRule = (AdviceWeavingRule)it2.next();

                    // create set field pointcut
                    FieldPointcut pointcut = new FieldPointcut(uuid, weavingRule.getExpression());

                    // add all referenced poincuts definitions
                    boolean hasSetFieldPointcut = false;
                    List pointcutRefs = weavingRule.getPointcutRefs();
                    for (Iterator it3 = pointcutRefs.iterator(); it3.hasNext();) {
                        PointcutDefinition pointcutDefinition =
                                aspectDefinition.getPointcutDef((String)it3.next());

                        if (pointcutDefinition != null && pointcutDefinition.getType().
                                equalsIgnoreCase(PointcutDefinition.SET_FIELD)) {
                            pointcut.addPointcutDef(pointcutDefinition);
                            hasSetFieldPointcut = true;
                        }
                    }

                    // check if the weaving rule had a set field pointcut, if not continue
                    if (!hasSetFieldPointcut) {
                        continue;
                    }

                    // add pre and post advices
                    List adviceRefs = weavingRule.getAdviceRefs();
                    for (Iterator it3 = adviceRefs.iterator(); it3.hasNext();) {
                        String adviceRef = (String)it3.next();
                        if (AspectWerkz.getSystem(uuid).
                                getAdvice(adviceRef) instanceof PreAdvice) {
                            pointcut.addPreAdvice(adviceRef);
                        }
                        else if (AspectWerkz.getSystem(uuid).
                                getAdvice(adviceRef) instanceof PostAdvice) {
                            pointcut.addPostAdvice(adviceRef);
                        }
                    }

                    // add advices from advice stacks
                    List adviceStackRefs = weavingRule.getAdviceStackRefs();
                    for (Iterator it3 = adviceStackRefs.iterator(); it3.hasNext();) {
                        AdviceStackDefinition adviceStackDefinition =
                                definition.getAdviceStackDefinition((String)it3.next());

                        adviceRefs = adviceStackDefinition.getAdviceRefs();
                        for (Iterator it4 = adviceRefs.iterator(); it4.hasNext();) {
                            String adviceRef = (String)it4.next();
                            if (AspectWerkz.getSystem(uuid).
                                    getAdvice(adviceRef) instanceof PreAdvice) {
                                pointcut.addPreAdvice(adviceRef);
                            }
                            else if (AspectWerkz.getSystem(uuid).
                                    getAdvice(adviceRef) instanceof PostAdvice) {
                                pointcut.addPostAdvice(adviceRef);
                            }
                        }
                    }

                    // add the set field pointcut
                    aspect.addSetFieldPointcut(pointcut);
                }
            }
            catch (NullPointerException e) {
                throw new DefinitionException("set field pointcuts in aspect <" + aspect.getName() + "> are not properly defined");
            }
            catch (Exception e) {
                throw new WrappedRuntimeException(e);
            }
        }
    }

    /**
     * Registers the get field pointcuts.
     *
     * @param uuid the UUID for the AspectWerkz system to use
     * @param definition the AspectWerkz definition
     */
    private static void registerGetFieldPointcuts(
            final String uuid,
            final AspectWerkzDefinition2 definition) {

        // get all aspects definitions
        for (Iterator it1 = definition.getAspectDefinitions().iterator(); it1.hasNext();) {
            AspectDefinition2 aspectDefinition = (AspectDefinition2)it1.next();
            Aspect aspect = AspectWerkz.getSystem(uuid).getAspect(aspectDefinition.getName());

            try {
                // get all advice weaving rules defined in this aspect
                List adviceWeavingRules = aspectDefinition.getAdviceWeavingRules();
                for (Iterator it2 = adviceWeavingRules.iterator(); it2.hasNext();) {
                    AdviceWeavingRule weavingRule = (AdviceWeavingRule)it2.next();

                    // create get field pointcut
                    FieldPointcut pointcut = new FieldPointcut(uuid, weavingRule.getExpression());

                    // add all referenced poincuts definitions
                    boolean hasGetFieldPointcut = false;
                    List pointcutRefs = weavingRule.getPointcutRefs();
                    for (Iterator it3 = pointcutRefs.iterator(); it3.hasNext();) {
                        PointcutDefinition pointcutDefinition =
                                aspectDefinition.getPointcutDef((String)it3.next());
                        if (pointcutDefinition != null && pointcutDefinition.getType().
                                equalsIgnoreCase(PointcutDefinition.GET_FIELD)) {
                            pointcut.addPointcutDef(pointcutDefinition);
                            hasGetFieldPointcut = true;
                        }
                    }

                    // check if the weaving rule had a get field pointcut, if not continue
                    if (!hasGetFieldPointcut) {
                        continue;
                    }

                    // add pre and post advices
                    List adviceRefs = weavingRule.getAdviceRefs();
                    for (Iterator it3 = adviceRefs.iterator(); it3.hasNext();) {
                        String adviceRef = (String)it3.next();
                        if (AspectWerkz.getSystem(uuid).
                                getAdvice(adviceRef) instanceof PreAdvice) {
                            pointcut.addPreAdvice(adviceRef);
                        }
                        else if (AspectWerkz.getSystem(uuid).
                                getAdvice(adviceRef) instanceof PostAdvice) {
                            pointcut.addPostAdvice(adviceRef);
                        }
                    }

                    // add advices from advice stacks
                    List adviceStackRefs = weavingRule.getAdviceStackRefs();
                    for (Iterator it3 = adviceStackRefs.iterator(); it3.hasNext();) {
                        AdviceStackDefinition adviceStackDefinition =
                                definition.getAdviceStackDefinition((String)it3.next());

                        adviceRefs = adviceStackDefinition.getAdviceRefs();
                        for (Iterator it4 = adviceRefs.iterator(); it4.hasNext();) {
                            String adviceRef = (String)it4.next();
                            if (AspectWerkz.getSystem(uuid).getAdvice(adviceRef) instanceof PreAdvice) {
                                pointcut.addPreAdvice(adviceRef);
                            }
                            else if (AspectWerkz.getSystem(uuid).getAdvice(adviceRef) instanceof PostAdvice) {
                                pointcut.addPostAdvice(adviceRef);
                            }
                        }
                    }

                    // add the get field pointcut
                    aspect.addGetFieldPointcut(pointcut);
                }
            }
            catch (NullPointerException e) {
                throw new DefinitionException("get field pointcuts in aspect <" + aspect.getName() + "> are not properly defined");
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
    private static void registerThrowsPointcuts(
            final String uuid,
            final AspectWerkzDefinition2 definition) {

        // get all aspects definitions
        for (Iterator it1 = definition.getAspectDefinitions().iterator(); it1.hasNext();) {
            AspectDefinition2 aspectDefinition = (AspectDefinition2)it1.next();
            Aspect aspect = AspectWerkz.getSystem(uuid).getAspect(aspectDefinition.getName());

            try {
                // get all advice weaving rules defined in this aspect
                List adviceWeavingRules = aspectDefinition.getAdviceWeavingRules();
                for (Iterator it2 = adviceWeavingRules.iterator(); it2.hasNext();) {
                    AdviceWeavingRule weavingRule = (AdviceWeavingRule)it2.next();

                    // create throws pointcut
                    ThrowsPointcut pointcut = new ThrowsPointcut(uuid, weavingRule.getExpression());

                    // add all referenced poincuts definitions
                    boolean hasThrowsPointcut = false;
                    List pointcutRefs = weavingRule.getPointcutRefs();
                    for (Iterator it3 = pointcutRefs.iterator(); it3.hasNext();) {
                        PointcutDefinition pointcutDefinition =
                                aspectDefinition.getPointcutDef((String)it3.next());
                        if (pointcutDefinition != null && pointcutDefinition.getType().
                                equalsIgnoreCase(PointcutDefinition.THROWS)) {
                            pointcut.addPointcutDef(pointcutDefinition);
                            hasThrowsPointcut = true;
                        }
                    }

                    // check if the weaving rule had a throws pointcut, if not continue
                    if (!hasThrowsPointcut) {
                        continue;
                    }

                    // add advices
                    List adviceRefs = weavingRule.getAdviceRefs();
                    for (Iterator it3 = adviceRefs.iterator(); it3.hasNext();) {
                        String asdf = (String)it3.next();
                        pointcut.addAdvice(asdf);
                    }

                    // add advices from advice stacks
                    List adviceStackRefs = weavingRule.getAdviceStackRefs();
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
     * Registers the caller side pointcuts.
     *
     * @param uuid the UUID for the AspectWerkz system to use
     * @param definition the AspectWerkz definition
     */
    private static void registerCallerSidePointcuts(
            final String uuid,
            final AspectWerkzDefinition2 definition) {

        // get all aspects definitions
        for (Iterator it1 = definition.getAspectDefinitions().iterator(); it1.hasNext();) {
            AspectDefinition2 aspectDefinition = (AspectDefinition2)it1.next();
            Aspect aspect = AspectWerkz.getSystem(uuid).getAspect(aspectDefinition.getName());

            try {
                // get all advice weaving rules defined in this aspect
                List adviceWeavingRules = aspectDefinition.getAdviceWeavingRules();
                for (Iterator it2 = adviceWeavingRules.iterator(); it2.hasNext();) {
                    AdviceWeavingRule weavingRule = (AdviceWeavingRule)it2.next();

                    // create caller side pointcut
                    CallerSidePointcut pointcut = new CallerSidePointcut(
                            uuid, weavingRule.getExpression());

                    // add all referenced poincuts definitions
                    boolean hasCallerSidePointcut = false;
                    List pointcutRefs = weavingRule.getPointcutRefs();
                    for (Iterator it3 = pointcutRefs.iterator(); it3.hasNext();) {
                        PointcutDefinition pointcutDefinition =
                                aspectDefinition.getPointcutDef((String)it3.next());
                        if (pointcutDefinition != null && pointcutDefinition.getType().
                                equalsIgnoreCase(PointcutDefinition.CALLER_SIDE)) {
                            pointcut.addPointcutDef(pointcutDefinition);
                            hasCallerSidePointcut = true;
                        }
                    }

                    // check if the weaving rule had a caller side pointcut, if not continue
                    if (!hasCallerSidePointcut) {
                        continue;
                    }

                    // add pre and post advices
                    List adviceRefs = weavingRule.getAdviceRefs();
                    for (Iterator it3 = adviceRefs.iterator(); it3.hasNext();) {
                        String adviceRef = (String)it3.next();
                        if (AspectWerkz.getSystem(uuid).
                                getAdvice(adviceRef) instanceof PreAdvice) {
                            pointcut.addPreAdvice(adviceRef);
                        }
                        else if (AspectWerkz.getSystem(uuid).
                                getAdvice(adviceRef) instanceof PostAdvice) {
                            pointcut.addPostAdvice(adviceRef);
                        }
                    }

                    // add advices from advice stacks
                    List adviceStackRefs = weavingRule.getAdviceStackRefs();
                    for (Iterator it3 = adviceStackRefs.iterator(); it3.hasNext();) {
                        AdviceStackDefinition adviceStackDefinition =
                                definition.getAdviceStackDefinition((String)it3.next());

                        adviceRefs = adviceStackDefinition.getAdviceRefs();
                        for (Iterator it4 = adviceRefs.iterator(); it4.hasNext();) {
                            String adviceRef = (String)it4.next();
                            if (AspectWerkz.getSystem(uuid).getAdvice(adviceRef) instanceof PreAdvice) {
                                pointcut.addPreAdvice(adviceRef);
                            }
                            else if (AspectWerkz.getSystem(uuid).getAdvice(adviceRef) instanceof PostAdvice) {
                                pointcut.addPostAdvice(adviceRef);
                            }
                        }
                    }
                    // add the caller side pointcut
                    aspect.addCallerSidePointcut(pointcut);
                }
            }
            catch (NullPointerException e) {
                throw new DefinitionException("caller side pointcuts in aspect <" + aspect.getName() + "> are not properly defined");
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
    private static void registerCFlowPointcuts(
            final String uuid,
            final AspectWerkzDefinition2 definition) {

        // get all aspects definitions
        for (Iterator it1 = definition.getAspectDefinitions().iterator(); it1.hasNext();) {
            AspectDefinition2 aspectDefinition = (AspectDefinition2)it1.next();
            Aspect aspect = AspectWerkz.getSystem(uuid).getAspect(aspectDefinition.getName());

            try {
                // get all advice weaving rules defined in this aspect
                List adviceWeavingRules = aspectDefinition.getAdviceWeavingRules();
                for (Iterator it2 = adviceWeavingRules.iterator(); it2.hasNext();) {
                    AdviceWeavingRule weavingRule = (AdviceWeavingRule)it2.next();

                    String cflowExpression = weavingRule.getCFlowExpression();
                    if (cflowExpression == null) {
                        continue;
                    }

                    // get the referenced cflow poincut definition
                    PointcutDefinition cflowPointcutDef =
                            aspectDefinition.getPointcutDef(cflowExpression);

                    // create caller side pointcut
                    CallerSidePointcut callerSidePointcut =
                            new CallerSidePointcut(uuid, cflowExpression);
                    if (!(cflowPointcutDef != null && cflowPointcutDef.getType().
                            equalsIgnoreCase(PointcutDefinition.CFLOW))) {
                        continue;
                    }
                    // register the cflow advices in the system (if they does not already exist)
                    if (!AspectWerkz.getSystem(uuid).hasAspect(CFlowPreAdvice.NAME)) {
                        AdviceDefinition2 adviceDef = CFlowPreAdvice.getDefinition();
                        // add the advice to the aspectwerkz definition
                        definition.addAdvice(adviceDef);
                        // add the advice to the aspectwerkz system
                        registerAdvice(uuid, adviceDef);
                    }
                    if (!AspectWerkz.getSystem(uuid).hasAspect(CFlowPostAdvice.NAME)) {
                        AdviceDefinition2 adviceDef = CFlowPostAdvice.getDefinition();
                        // add the advice to the aspectwerkz definition
                        definition.addAdvice(adviceDef);
                        // add the advice to the aspectwerkz system
                        registerAdvice(uuid, adviceDef);
                    }
                    // add the pointcut definition to the method pointcut
                    callerSidePointcut.addPointcutDef(cflowPointcutDef);
                    // add references to the cflow advices to the cflow pointcut
                    callerSidePointcut.addPreAdvice(CFlowPreAdvice.NAME);
                    callerSidePointcut.addPostAdvice(CFlowPostAdvice.NAME);
                    // add the method pointcut
                    aspect.addCallerSidePointcut(callerSidePointcut);

                    // add a mapping between the cflow pattern and the method patterns affected
                    for (Iterator it3 = weavingRule.getPointcutRefs().iterator(); it3.hasNext();) {
                        PointcutDefinition pointcutDef =
                                aspectDefinition.getPointcutDef((String)it3.next());
                        if (pointcutDef != null && pointcutDef.getType().
                                equalsIgnoreCase(PointcutDefinition.METHOD)) {

                            aspect.addMethodToCFlowMethodMap(
                                    pointcutDef.getPointcutPatternTuple(),
                                    cflowPointcutDef.getPointcutPatternTuple());
                        }
                    }
                }
            }
            catch (NullPointerException e) {
                throw new DefinitionException("method pointcuts in aspect <" + aspect.getName() + "> are not properly defined");
            }
            catch (Exception e) {
                throw new WrappedRuntimeException(e);
            }
        }
    }

    /**
     * Private constructor to prevent instantiability.
     */
    private StartupManager2() {
    }
}
