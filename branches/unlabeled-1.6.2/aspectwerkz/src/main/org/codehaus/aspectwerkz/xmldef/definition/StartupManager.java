/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition;

import java.util.Iterator;
import java.util.List;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import org.codehaus.aspectwerkz.aspect.AspectMetaData;
import org.codehaus.aspectwerkz.AspectWerkz;
import org.codehaus.aspectwerkz.DeploymentModel;
import org.codehaus.aspectwerkz.ContextClassLoader;
import org.codehaus.aspectwerkz.aspect.AbstractAspect;
import org.codehaus.aspectwerkz.aspect.AspectContainer;
import org.codehaus.aspectwerkz.definition.AspectWerkzDefinition;
import org.codehaus.aspectwerkz.definition.AspectDefinition;
import org.codehaus.aspectwerkz.definition.AdviceDefinition;
import org.codehaus.aspectwerkz.pointcut.MethodPointcut;
import org.codehaus.aspectwerkz.pointcut.FieldPointcut;
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
            "org.codehaus.aspectwerkz.aspect.DefaultAspectContainerStrategy";

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
     */
    public static void initializeSystem(final String uuid) {
        if (uuid == null) throw new IllegalArgumentException("uuid can not be null");
        if (s_initialized) return;
        s_initialized = true;

        final AspectWerkzDefinition definition = AspectWerkzDefinition.getDefinition(uuid);
        definition.loadAspects(ContextClassLoader.getLoader());

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
            Constructor constructor = klass.getConstructor(new Class[]{AbstractAspect.class});
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
    private static void registerAspects(final String uuid, final AspectWerkzDefinition definition) {
        try {
            for (Iterator it = definition.getAspectDefinitions().iterator(); it.hasNext();) {
                AspectDefinition aspectDef = (AspectDefinition)it.next();
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
    private static void registerAspect(final String uuid, final AspectDefinition aspectDef) {
        try {
            String aspectClassName = aspectDef.getClassName();

            // load the aspect class
            Class aspectClass = null;
            try {
                aspectClass = ContextClassLoader.loadClass(aspectClassName);
            }
            catch (ClassNotFoundException e) {
                throw new RuntimeException(aspectClassName + " could not be found on classpath");
            }

            // create an instance of the aspect class
            final AbstractAspect aspect = (AbstractAspect)aspectClass.newInstance();

            int deploymentModel;
            if (aspectDef.getDeploymentModel() == null || aspectDef.getDeploymentModel().equals("")) {
                deploymentModel = DeploymentModel.PER_JVM;
            }
            else {
                deploymentModel = DeploymentModel.
                        getDeploymentModelAsInt(aspectDef.getDeploymentModel());
            }

            // set the parameters
            Field field = AbstractAspect.class.getDeclaredField("m_uuid");
            field.setAccessible(true);
            field.set(aspect, uuid);
            aspect.___AW_setName(aspectDef.getName());
            aspect.___AW_setAspectClass(aspectClass);
            aspect.___AW_setDeploymentModel(deploymentModel);
            aspect.___AW_setAspectDef(aspectDef);

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
            AspectMetaData aspectMetaData = new AspectMetaData(uuid, aspectDef.getName());

            AspectWerkz.getSystem(uuid).register(aspect, aspectMetaData);
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
                                          final AspectWerkzDefinition definition) {
//        registerCFlowPointcuts(uuid, definition);
        registerMethodPointcuts(uuid, definition);
        registerSetFieldPointcuts(uuid, definition);
        registerGetFieldPointcuts(uuid, definition);
//        registerThrowsPointcuts(uuid, definition);
//        registerCallerSidePointcuts(uuid, definition);
    }

    /**
     * Registers the method pointcuts.
     *
     * @param uuid the UUID for the AspectWerkz system to use
     * @param definition the AspectWerkz definition
     */
    private static void registerMethodPointcuts(final String uuid,
                                                final AspectWerkzDefinition definition) {

        for (Iterator it = definition.getAspectDefinitions().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();

            AspectMetaData aspectMetaData =
                    AspectWerkz.getSystem(uuid).getAspectMetaData(aspectDef.getName());

            List aroundAdvices = aspectDef.getAroundAdvices();
            for (Iterator it2 = aroundAdvices.iterator(); it2.hasNext();) {
                AdviceDefinition adviceDef = (AdviceDefinition)it2.next();
                MethodPointcut methodPointcut = new MethodPointcut(
                        uuid,
                        adviceDef.getPointcut()
                );
                boolean hasPointcut = false;
                List pointcutRefs = adviceDef.getPointcutRefs();
                for (Iterator it3 = pointcutRefs.iterator(); it3.hasNext();) {
                    String pointcutName = (String)it3.next();
                    PointcutDefinition pointcutDef = aspectDef.getPointcut(pointcutName);
                    if (pointcutDef != null && pointcutDef.getType().
                            equalsIgnoreCase(PointcutDefinition.TYPE_METHOD)) {
                        methodPointcut.addPointcutDef(pointcutDef);
                        hasPointcut = true;
                    }
                }
                // check if the weaving rule had a method pointcut, if not continue
                if (!hasPointcut) {
                    continue;
                }
                methodPointcut.addAdvice(adviceDef.getName());
                aspectMetaData.addMethodPointcut(methodPointcut);
            }
        }
    }

    /**
     * Registers the set field pointcuts.
     *
     * @param uuid the UUID for the AspectWerkz system to use
     * @param definition the AspectWerkz definition
     */
    private static void registerSetFieldPointcuts(final String uuid,
                                                  final AspectWerkzDefinition definition) {
        for (Iterator it = definition.getAspectDefinitions().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();

            AspectMetaData aspectMetaData =
                    AspectWerkz.getSystem(uuid).getAspectMetaData(aspectDef.getName());

            List preAdvices = aspectDef.getPreAdvices();
            for (Iterator it2 = preAdvices.iterator(); it2.hasNext();) {
                AdviceDefinition adviceDef = (AdviceDefinition)it2.next();
                FieldPointcut fieldPointcut = new FieldPointcut(
                        uuid,
                        adviceDef.getPointcut()
                );
                boolean hasPointcut = false;
                List pointcutRefs = adviceDef.getPointcutRefs();
                for (Iterator it3 = pointcutRefs.iterator(); it3.hasNext();) {
                    String pointcutName = (String)it3.next();
                    PointcutDefinition pointcutDef = aspectDef.getPointcut(pointcutName);
                    if (pointcutDef != null && pointcutDef.getType().
                            equalsIgnoreCase(PointcutDefinition.TYPE_SET_FIELD)) {
                        fieldPointcut.addPointcutDef(pointcutDef);
                        hasPointcut = true;
                    }
                }
                // check if the weaving rule had a method pointcut, if not continue
                if (!hasPointcut) {
                    continue;
                }
                fieldPointcut.addPreAdvice(adviceDef.getName());
                aspectMetaData.addSetFieldPointcut(fieldPointcut);
            }

            List postAdvices = aspectDef.getPreAdvices();
            for (Iterator it2 = postAdvices.iterator(); it2.hasNext();) {
                AdviceDefinition adviceDef = (AdviceDefinition)it2.next();
                FieldPointcut fieldPointcut = new FieldPointcut(
                        uuid,
                        adviceDef.getPointcut()
                );
                boolean hasPointcut = false;
                List pointcutRefs = adviceDef.getPointcutRefs();
                for (Iterator it3 = pointcutRefs.iterator(); it3.hasNext();) {
                    String pointcutName = (String)it3.next();
                    PointcutDefinition pointcutDef = aspectDef.getPointcut(pointcutName);
                    if (pointcutDef != null && pointcutDef.getType().
                            equalsIgnoreCase(PointcutDefinition.TYPE_SET_FIELD)) {
                        fieldPointcut.addPointcutDef(pointcutDef);
                        hasPointcut = true;
                    }
                }
                // check if the weaving rule had a method pointcut, if not continue
                if (!hasPointcut) {
                    continue;
                }
                fieldPointcut.addPostAdvice(adviceDef.getName());
                aspectMetaData.addSetFieldPointcut(fieldPointcut);
            }
        }
    }

    /**
     * Registers the get field pointcuts.
     *
     * @param uuid the UUID for the AspectWerkz system to use
     * @param definition the AspectWerkz definition
     */
    private static void registerGetFieldPointcuts(final String uuid,
                                                  final AspectWerkzDefinition definition) {
        for (Iterator it = definition.getAspectDefinitions().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();

            AspectMetaData aspectMetaData =
                    AspectWerkz.getSystem(uuid).getAspectMetaData(aspectDef.getName());

            List preAdvices = aspectDef.getPreAdvices();
            for (Iterator it2 = preAdvices.iterator(); it2.hasNext();) {
                AdviceDefinition adviceDef = (AdviceDefinition)it2.next();
                FieldPointcut fieldPointcut = new FieldPointcut(
                        uuid,
                        adviceDef.getPointcut()
                );
                boolean hasPointcut = false;
                List pointcutRefs = adviceDef.getPointcutRefs();
                for (Iterator it3 = pointcutRefs.iterator(); it3.hasNext();) {
                    String pointcutName = (String)it3.next();
                    PointcutDefinition pointcutDef = aspectDef.getPointcut(pointcutName);
                    if (pointcutDef != null && pointcutDef.getType().
                            equalsIgnoreCase(PointcutDefinition.TYPE_GET_FIELD)) {
                        fieldPointcut.addPointcutDef(pointcutDef);
                        hasPointcut = true;
                    }
                }
                // check if the weaving rule had a method pointcut, if not continue
                if (!hasPointcut) {
                    continue;
                }
                fieldPointcut.addPreAdvice(adviceDef.getName());
                aspectMetaData.addGetFieldPointcut(fieldPointcut);
            }

            List postAdvices = aspectDef.getPostAdvices();
            for (Iterator it2 = postAdvices.iterator(); it2.hasNext();) {
                AdviceDefinition adviceDef = (AdviceDefinition)it2.next();
                FieldPointcut fieldPointcut = new FieldPointcut(
                        uuid,
                        adviceDef.getPointcut()
                );
                boolean hasPointcut = false;
                List pointcutRefs = adviceDef.getPointcutRefs();
                for (Iterator it3 = pointcutRefs.iterator(); it3.hasNext();) {
                    String pointcutName = (String)it3.next();
                    PointcutDefinition pointcutDef = aspectDef.getPointcut(pointcutName);
                    if (pointcutDef != null && pointcutDef.getType().
                            equalsIgnoreCase(PointcutDefinition.TYPE_GET_FIELD)) {
                        fieldPointcut.addPointcutDef(pointcutDef);
                        hasPointcut = true;
                    }
                }
                // check if the weaving rule had a method pointcut, if not continue
                if (!hasPointcut) {
                    continue;
                }
                fieldPointcut.addPostAdvice(adviceDef.getName());
                aspectMetaData.addGetFieldPointcut(fieldPointcut);
            }
        }
    }

    /**
     * Registers the throws pointcuts.
     *
     * @param uuid the UUID for the AspectWerkz system to use
     * @param definition the AspectWerkz definition
     *
     private static void registerThrowsPointcuts(
     final String uuid,
     final AspectWerkzDefinition definition) {

     // get all aspects definitions
     for (Iterator it1 = definition.getAspectDefinitions().iterator(); it1.hasNext();) {
     AspectDefinition aspectDefinition = (AspectDefinition)it1.next();
     AspectMetaData aspect = AspectWerkz.getSystem(uuid).getAspect(aspectDefinition.getName());

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
     org.codehaus.aspectwerkz.definition.version1.PointcutDefinition1 pointcutDefinition =
     aspectDefinition.getPointcut((String)it3.next());
     if (pointcutDefinition != null && pointcutDefinition.getType().
     equalsIgnoreCase(org.codehaus.aspectwerkz.definition.version1.PointcutDefinition1.THROWS)) {
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
     */
    /**
     * Registers the caller side pointcuts.
     *
     * @param uuid the UUID for the AspectWerkz system to use
     * @param definition the AspectWerkz definition
     *
     private static void registerCallerSidePointcuts(
     final String uuid,
     final AspectWerkzDefinition definition) {

     // get all aspects definitions
     for (Iterator it1 = definition.getAspectDefinitions().iterator(); it1.hasNext();) {
     AspectDefinition aspectDefinition = (AspectDefinition)it1.next();
     AspectMetaData aspect = AspectWerkz.getSystem(uuid).getAspect(aspectDefinition.getName());

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
     org.codehaus.aspectwerkz.definition.version1.PointcutDefinition1 pointcutDefinition =
     aspectDefinition.getPointcut((String)it3.next());
     if (pointcutDefinition != null && pointcutDefinition.getType().
     equalsIgnoreCase(org.codehaus.aspectwerkz.definition.version1.PointcutDefinition1.CALLER_SIDE)) {
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
     */
    /**
     * Registers the cflow pointcuts.
     *
     * @param uuid the UUID for the AspectWerkz system to use
     * @param definition the AspectWerkz definition
     *
     private static void registerCFlowPointcuts(
     final String uuid,
     final AspectWerkzDefinition definition) {

     // get all aspects definitions
     for (Iterator it1 = definition.getAspectDefinitions().iterator(); it1.hasNext();) {
     AspectDefinition aspectDefinition = (AspectDefinition)it1.next();
     AspectMetaData aspect = AspectWerkz.getSystem(uuid).getAspect(aspectDefinition.getName());

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
     org.codehaus.aspectwerkz.definition.version1.PointcutDefinition1 cflowPointcutDef =
     aspectDefinition.getPointcut(cflowExpression);

     // create caller side pointcut
     CallerSidePointcut callerSidePointcut =
     new CallerSidePointcut(uuid, cflowExpression);
     if (!(cflowPointcutDef != null && cflowPointcutDef.getType().
     equalsIgnoreCase(org.codehaus.aspectwerkz.definition.version1.PointcutDefinition1.CFLOW))) {
     continue;
     }
     // register the cflow advices in the system (if they does not already exist)
     if (!AspectWerkz.getSystem(uuid).hasAspect(CFlowPreAdvice.NAME)) {
     AdviceDefinition adviceDef = CFlowPreAdvice.getDefinition();
     // add the advice to the aspectwerkz definition
     definition.addAdvice(adviceDef);
     // add the advice to the aspectwerkz system
     registerAdvice(uuid, adviceDef);
     }
     if (!AspectWerkz.getSystem(uuid).hasAspect(CFlowPostAdvice.NAME)) {
     AdviceDefinition adviceDef = CFlowPostAdvice.getDefinition();
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
     org.codehaus.aspectwerkz.definition.PointcutDefinition pointcutDef =
     aspectDefinition.getPointcut((String)it3.next());
     if (pointcutDef != null && pointcutDef.getType().
     equalsIgnoreCase(org.codehaus.aspectwerkz.definition.version1.PointcutDefinition1.METHOD)) {

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
     */

    /**
     * Private constructor to prevent instantiability.
     */
    private StartupManager() {
    }
}
