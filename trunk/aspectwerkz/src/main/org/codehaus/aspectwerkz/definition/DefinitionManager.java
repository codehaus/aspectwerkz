/*
 * AspectWerkz - a dynamic, lightweight and high-performant AOP/AOSD framework for Java.
 * Copyright (C) 2002-2003  Jonas Bonér. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.codehaus.aspectwerkz.definition;

import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.List;
import java.util.Map;
import java.lang.reflect.InvocationTargetException;

import org.codehaus.aspectwerkz.Aspect;
import org.codehaus.aspectwerkz.AspectWerkz;
import org.codehaus.aspectwerkz.DeploymentModel;
import org.codehaus.aspectwerkz.definition.metadata.WeaveModel;
import org.codehaus.aspectwerkz.definition.regexp.ClassPattern;
import org.codehaus.aspectwerkz.definition.regexp.Pattern;
import org.codehaus.aspectwerkz.advice.PreAdvice;
import org.codehaus.aspectwerkz.advice.PostAdvice;
import org.codehaus.aspectwerkz.advice.PersistableAdviceMemoryStrategy;
import org.codehaus.aspectwerkz.advice.TransientAdviceMemoryStrategy;
import org.codehaus.aspectwerkz.advice.AbstractAdvice;
import org.codehaus.aspectwerkz.introduction.Introduction;
import org.codehaus.aspectwerkz.introduction.PersistentIntroductionMemoryStrategy;
import org.codehaus.aspectwerkz.introduction.TransientIntroductionMemoryStrategy;
import org.codehaus.aspectwerkz.persistence.DirtyFieldCheckAdvice;
import org.codehaus.aspectwerkz.persistence.PersistenceManagerFactory;
import org.codehaus.aspectwerkz.persistence.PersistenceManager;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.exception.DefinitionException;

/**
 * Manages the aspect definitions.<p/>
 *
 * Reads the definition, either as a class of as an XML file.
 * <p/>
 * To use your XML definition file pass
 * <code>-Daspectwerkz.definition.file=PathToFile</code>
 * as parameter to the JVM.
 * <p/>
 * If the above given parameter is not specified, the <code>DefinitionManager</code>
 * tries locate a file called <code>aspectwerkz.xml</code> in the classpath
 * and if this fails the last attempt is to use the
 * <code>ASPECTWERKZ_HOME/config/aspectwerkz.xml</code> file (if there is one).
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: DefinitionManager.java,v 1.5 2003-06-09 07:04:13 jboner Exp $
 */
public class DefinitionManager {

    /**
     * The path to the aspectwerkz home directory.
     */
    public static final String ASPECTWERKZ_HOME =
            System.getProperty("aspectwerkz.home", ".");

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
     * The path to the default config directory.
     */
    public static final String CONFIG_DIR = "/config";

    /**
     * Marks the manager as initialized or not.
     */
    private static boolean s_initialized = false;

    /**
     * The aspect definition.
     */
    private static WeaveModel s_weaveModel;

    /**
     * The persistence manager.
     */
    private static PersistenceManager s_persistenceManager;

    /**
     * Loads the system definition.
     *
     * @param uuid the UUID for the weave model to load
     */
    public static void loadDefinition(final String uuid) {
        if (uuid == null) throw new IllegalArgumentException("uuid can not be null");
        if (s_initialized) return;
        s_initialized = true;

        s_weaveModel = WeaveModel.loadModel(uuid);

        createAspects(uuid, s_weaveModel);
        registerDirtyFieldCheckAdvice(uuid);

        registerIntroductions(uuid, s_weaveModel);
        registerAdvices(uuid, s_weaveModel);
        registerPointcuts(uuid, s_weaveModel);
    }

    /**
     * Returns the weave model for the application.
     *
     * @return the weave model
     */
    public static WeaveModel getWeaveModel() {
        if (!s_initialized) throw new IllegalStateException("definition manager is not initialized");
        return s_weaveModel;
    }

    /**
     * Creates and registers the aspects defined.
     *
     * @param uuid the UUID for the AspectWerkz system to use
     * @param weaveModel the weaveModel
     */
    private static void createAspects(final String uuid,
                                      final WeaveModel weaveModel) {
        for (Iterator it = weaveModel.getAspectPatterns().iterator(); it.hasNext();) {
            AspectWerkz.getSystem(uuid).
                    register(new Aspect(uuid, (ClassPattern)it.next()));
        }
    }

    /**
     * Creates and registers the introductions defined.
     *
     * @param uuid the UUID for the AspectWerkz system to use
     * @param weaveModel the weaveModel
     */
    private static void registerIntroductions(final String uuid,
                                              final WeaveModel weaveModel) {
        for (Iterator it = weaveModel.getIntroductionDefinitions().iterator(); it.hasNext();) {

            final IntroductionDefinition def = (IntroductionDefinition)it.next();
            final String implClassName = def.getImplementation();
            final String intfClassName = def.getInterface();

            Class implClass = null;
            Aspect aspectForIntroduction = null;
            if (implClassName != null) {

                // create an aspect for the introduction as well
                aspectForIntroduction = new Aspect(uuid, Pattern.
                        compileClassPattern(implClassName));

                AspectWerkz.getSystem(uuid).register(aspectForIntroduction);

                // load the introduction class
                try {
                    implClass = Thread.currentThread().getContextClassLoader().
                            loadClass(implClassName);
                }
                catch (ClassNotFoundException e) {
                    throw new RuntimeException(implClassName + " could not be found in classpath");
                }
            }
            final Introduction newIntroduction =
                    new Introduction(def.getName(), intfClassName, implClass,
                            DeploymentModel.getDeploymentModelAsInt(def.getDeploymentModel()));

            // persistence stuff
            if (implClassName != null && def.isPersistent()) {
                newIntroduction.setMemoryStrategy(
                        new PersistentIntroductionMemoryStrategy(
                                def.getName(), implClass));

                if (s_persistenceManager == null) {
                    loadPersistenceManager();
                }
                s_persistenceManager.register(def.getName());
            }
            else {
                newIntroduction.setMemoryStrategy(
                        new TransientIntroductionMemoryStrategy(implClass));
            }
            AspectWerkz.getSystem(uuid).register(def.getName(), newIntroduction);
        }
    }

    /**
     * Creates and registers the advices defined.
     *
     * @todo what if the context classloader is NULL? Need a plan B.
     * @param uuid the UUID for the AspectWerkz system to use
     * @param weaveModel the weaveModel
     */
    private static void registerAdvices(final String uuid,
                                        final WeaveModel weaveModel) {

        for (Iterator it = weaveModel.getAdviceDefinitions().iterator(); it.hasNext();) {

            final AdviceDefinition def = (AdviceDefinition)it.next();
            final String adviceClassName = def.getClassName();

            try {
                final Class adviceClass = Thread.currentThread().
                        getContextClassLoader().loadClass(adviceClassName);

                final AbstractAdvice advice = (AbstractAdvice)adviceClass.
                        getConstructor(new Class[]{}).
                        newInstance(new Object[]{});

                int deploymentModel;
                if (def.getDeploymentModel() == null ||
                        def.getDeploymentModel().equals("")) {
                    deploymentModel = DeploymentModel.PER_JVM;
                }
                else {
                    deploymentModel = DeploymentModel.
                            getDeploymentModelAsInt(def.getDeploymentModel());
                }
                advice.setDeploymentModel(deploymentModel);
                advice.setName(def.getName());
                advice.setAdviceClass(adviceClass);

                // handle the parameters passed to the advice
                for (Iterator it2 = def.getParameters().entrySet().iterator(); it2.hasNext();) {
                    Map.Entry entry = (Map.Entry)it2.next();
                    final String name = (String)entry.getKey();
                    final String value = (String)entry.getValue();
                    advice.setParameter(name, value);
                }

                // persistence stuff
                // create an aspect for the advice as well
                Aspect aspectForAdvice = new Aspect(uuid, def.getClassName());

                if (def.isPersistent()) {
                    advice.setMemoryStrategy(new PersistableAdviceMemoryStrategy(advice));
                    if (s_persistenceManager == null) {
                        loadPersistenceManager();
                    }
                    s_persistenceManager.register(advice.getName());
                }
                else {
                    advice.setMemoryStrategy(
                            new TransientAdviceMemoryStrategy(advice));
                }

                AspectWerkz.getSystem(uuid).register(aspectForAdvice);
                AspectWerkz.getSystem(uuid).register(def.getName(), advice);
            }
            catch (ClassNotFoundException e) {
                throw new DefinitionException(adviceClassName + " could not be found in classpath");
            }
            catch (NoSuchMethodException e) {
                throw new DefinitionException(adviceClassName + " must define a constructor that takes an integer as an argument");
            }
            catch (InvocationTargetException e) {
                throw new WrappedRuntimeException(e.getCause());
            }
            catch (Exception e) {
                throw new WrappedRuntimeException(e);
            }
        }
    }

    /**
     * Creates and registers the aspects defined.
     *
     * @todo use or remove the threadSafe="false" facility (now turned off)
     * @param uuid the UUID for the AspectWerkz system to use
     * @param weaveModel the weaveModel
     */
    private static void registerPointcuts(final String uuid,
                                          final WeaveModel weaveModel) {
        boolean isThreadSafe = true;

        for (Iterator it1 = weaveModel.getAspectPatterns().iterator(); it1.hasNext();) {
            final ClassPattern aspectPattern = (ClassPattern)it1.next();
            registerIntroductions(uuid, weaveModel, aspectPattern);
            registerMethodPointcuts(uuid, aspectPattern, isThreadSafe, weaveModel);
            registerSetFieldPointcuts(uuid, aspectPattern, isThreadSafe, weaveModel);
            registerGetFieldPointcuts(uuid, aspectPattern, isThreadSafe, weaveModel);
            registerThrowsPointcuts(uuid, aspectPattern, isThreadSafe, weaveModel);
            registerCallerSidePointcuts(uuid, aspectPattern, isThreadSafe, weaveModel);
        }
    }

    /**
     * Registers the introductions.
     *
     * @param uuid the UUID for the AspectWerkz system to use
     * @param className the class name
     * @param weaveModel the weave model
     */
    private static void registerIntroductions(
            final String uuid,
            final WeaveModel weaveModel,
            final ClassPattern aspectPattern) {
        for (Iterator it2 = weaveModel.getIntroductionNames(aspectPattern).iterator(); it2.hasNext();) {
            AspectWerkz.getSystem(uuid).getAspect(aspectPattern).
                    addIntroduction((String)it2.next());
        }
    }

    /**
     * Registers the method pointcuts.
     *
     * @param uuid the UUID for the AspectWerkz system to use
     * @param className the class name
     * @param threadSafe thread-safe or not
     * @param weaveModel the weave model
     */
    private static void registerMethodPointcuts(
            final String uuid,
            final ClassPattern aspectPattern,
            final boolean threadSafe,
            final WeaveModel weaveModel) {

        final WeaveModel.WeaveMetaData classMetaData =
                weaveModel.getWeaveMetaData(aspectPattern);

        final Map methods = classMetaData.getMethodPointcuts();
        for (Iterator it = methods.entrySet().iterator(); it.hasNext();) {

            Map.Entry entry = (Map.Entry)it.next();
            String methodPattern = (String)entry.getKey();
            List pointcuts = (List)entry.getValue();

            AspectWerkz.getSystem(uuid).getAspect(aspectPattern).
                    createMethodPointcut(methodPattern, threadSafe);

            for (Iterator it2 = pointcuts.iterator(); it2.hasNext();) {
                PointcutDefinition pointcutDef = (PointcutDefinition)it2.next();
                addAdvicesToMethodPointcut(
                        uuid, aspectPattern, methodPattern,
                        pointcutDef.getAdvices());
                addAdviceStacksToMethodPointcut(
                        uuid, aspectPattern, methodPattern,
                        pointcutDef.getAdviceStacks(),
                        weaveModel);
            }
        }
    }

    /**
     * Registers the set field pointcuts.
     *
     * @param uuid the UUID for the AspectWerkz system to use
     * @param className the class name
     * @param threadSafe thread-safe or not
     * @param weaveModel the weave model
     */
    private static void registerSetFieldPointcuts(
            final String uuid,
            final ClassPattern aspectPattern,
            final boolean threadSafe,
            final WeaveModel weaveModel) {

        final WeaveModel.WeaveMetaData classMetaData =
                weaveModel.getWeaveMetaData(aspectPattern);

        final Map setFields = classMetaData.getSetFieldPointcuts();
        for (Iterator it = setFields.entrySet().iterator(); it.hasNext();) {

            Map.Entry entry = (Map.Entry)it.next();
            String fieldPattern = (String)entry.getKey();

            List pointcuts = (List)entry.getValue();
            for (Iterator it2 = pointcuts.iterator(); it2.hasNext();) {
                PointcutDefinition pointcutDef = (PointcutDefinition)it2.next();

                AspectWerkz.getSystem(uuid).getAspect(aspectPattern).
                        createSetFieldPointcut(fieldPattern, threadSafe);

                addAdvicesToSetFieldPointcut(
                        uuid, aspectPattern, fieldPattern,
                        pointcutDef.getAdvices());

                addAdviceStacksToSetFieldPointcut(
                        uuid, aspectPattern, fieldPattern,
                        pointcutDef.getAdviceStacks(),
                        weaveModel);
            }
        }
    }

    /**
     * Registers the get field pointcuts.
     *
     * @param uuid the UUID for the AspectWerkz system to use
     * @param className the class name
     * @param threadSafe thread-safe or not
     * @param weaveModel the weave model
     */
    private static void registerGetFieldPointcuts(
            final String uuid,
            final ClassPattern aspectPattern,
            final boolean threadSafe,
            final WeaveModel weaveModel) {

        final WeaveModel.WeaveMetaData classMetaData =
                weaveModel.getWeaveMetaData(aspectPattern);

        final Map getFields = classMetaData.getGetFieldPointcuts();
        for (Iterator it = getFields.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry)it.next();
            String fieldPattern = (String)entry.getKey();

            List pointcuts = (List)entry.getValue();
            for (Iterator it2 = pointcuts.iterator(); it2.hasNext();) {
                PointcutDefinition pointcutDef = (PointcutDefinition)it2.next();

                AspectWerkz.getSystem(uuid).getAspect(aspectPattern).
                        createGetFieldPointcut(fieldPattern, threadSafe);

                addAdvicesToGetFieldPointcut(
                        uuid, aspectPattern, fieldPattern,
                        pointcutDef.getAdvices());

                addAdviceStacksToGetFieldPointcut(
                        uuid, aspectPattern, fieldPattern,
                        pointcutDef.getAdviceStacks(),
                        weaveModel);
            }
        }
    }

    /**
     * Registers the throws pointcuts.
     *
     * @param uuid the UUID for the AspectWerkz system to use
     * @param className the class name
     * @param threadSafe thread-safe or not
     * @param weaveModel the weave model
     */
    private static void registerThrowsPointcuts(
            final String uuid,
            final ClassPattern aspectPattern,
            final boolean threadSafe,
            final WeaveModel weaveModel) {

        final WeaveModel.WeaveMetaData classMetaData =
                weaveModel.getWeaveMetaData(aspectPattern);

        final Map throwsMethods = classMetaData.getThrowsPointcuts();
        for (Iterator it = throwsMethods.values().iterator(); it.hasNext();) {
            List pointcuts = (List)it.next();
            for (Iterator it2 = pointcuts.iterator(); it2.hasNext();) {
                PointcutDefinition pointcutDef = (PointcutDefinition)it2.next();

                for (Iterator it3 = pointcutDef.getPatterns().iterator(); it3.hasNext();) {
                    String pattern = (String)it3.next();
                    AspectWerkz.getSystem(uuid).getAspect(aspectPattern).createThrowsPointcut(
                            pattern, threadSafe);

                    addAdvicesToThrowsPointcut(
                            uuid, aspectPattern, pattern,
                            pointcutDef.getAdvices());

                    addAdviceStacksToThrowsPointcut(
                            uuid, aspectPattern, pattern,
                            pointcutDef.getAdviceStacks(),
                            weaveModel);
                }
            }
        }
    }

    /**
     * Registers the caller side pointcuts.
     *
     * @param uuid the UUID for the AspectWerkz system to use
     * @param className the class name
     * @param threadSafe thread-safe or not
     * @param weaveModel the weave model
     */
    private static void registerCallerSidePointcuts(
            final String uuid,
            final ClassPattern aspectPattern,
            final boolean threadSafe,
            final WeaveModel weaveModel) {

        final Map callerSideMethods = weaveModel.getCallerSidePointcuts();
        for (Iterator it = callerSideMethods.values().iterator(); it.hasNext();) {
            for (Iterator it2 = ((List)it.next()).iterator(); it2.hasNext();) {
                PointcutDefinition pointcutDef = (PointcutDefinition)it2.next();

                if (!aspectPattern.getPattern().equals(
                        pointcutDef.getCallerSidePattern()))
                    continue;

                for (Iterator it3 = pointcutDef.getPatterns().iterator(); it3.hasNext();) {
                    final StringTokenizer tokenizer = new StringTokenizer(
                            (String)it3.next(),
                            AspectWerkzDefinition.CALLER_SIDE_DELIMITER);
                    tokenizer.nextToken();
                    final String targetMethodName = tokenizer.nextToken();

                    AspectWerkz.getSystem(uuid).getAspect(aspectPattern).
                            createCallerSidePointcut(targetMethodName, threadSafe);

                    addAdvicesToCallerSidePointcut(
                            uuid, aspectPattern, targetMethodName,
                            pointcutDef.getAdvices());

                    addAdviceStacksToCallerSidePointcut(
                            uuid, aspectPattern, targetMethodName,
                            pointcutDef.getAdviceStacks(),
                            weaveModel);
                }
            }
        }
    }

    /**
     * Creates and registers an advice for checking if fields in target objects
     * are "dirty", used by the persistence engine.
     *
     * @param uuid the UUID for the AspectWerkz system to use
     */
    private static void registerDirtyFieldCheckAdvice(final String uuid) {
        AspectWerkz.getSystem(uuid).register(
                DirtyFieldCheckAdvice.NAME,
                new DirtyFieldCheckAdvice());
    }

    /**
     * Adds advices to a method pointcut.
     *
     * @param uuid the UUID for the AspectWerkz system to use
     * @param className the class name
     * @param methodPattern the method pattern
     * @param advices the advices
     */
    private static void addAdvicesToMethodPointcut(
            final String uuid,
            final ClassPattern classPattern,
            final String methodPattern,
            final List advices) {

        for (Iterator it = advices.iterator(); it.hasNext();) {
            String advice = (String)it.next();
            AspectWerkz.getSystem(uuid).getAspect(classPattern).
                    getMethodPointcut(methodPattern).
                    addAdvice(advice);
        }
    }

    /**
     * Adds advices to a set field pointcut.
     *
     * @param uuid the UUID for the AspectWerkz system to use
     * @param className the class name
     * @param fieldPattern the field pattern
     * @param advices the advices
     */
    private static void addAdvicesToSetFieldPointcut(
            final String uuid,
            final ClassPattern classPattern,
            final String fieldPattern,
            final List advices) {

        for (Iterator it = advices.iterator(); it.hasNext();) {

            final String adviceName = (String)it.next();
            if (AspectWerkz.getSystem(uuid).getAdvice(adviceName) instanceof PreAdvice) {
                AspectWerkz.getSystem(uuid).getAspect(classPattern).
                        getSetFieldPointcut(fieldPattern).
                        addPreAdvice(adviceName);
            }
            else if (AspectWerkz.getSystem(uuid).getAdvice(adviceName) instanceof PostAdvice) {
                AspectWerkz.getSystem(uuid).getAspect(classPattern).
                        getSetFieldPointcut(fieldPattern).
                        addPostAdvice(adviceName);
            }
        }
    }

    /**
     * Adds advices to a set field pointcut.
     *
     * @param uuid the UUID for the AspectWerkz system to use
     * @param className the class name
     * @param fieldPattern the field pattern
     * @param advices the advices
     */
    private static void addAdvicesToGetFieldPointcut(
            final String uuid,
            final ClassPattern classPattern,
            final String fieldPattern,
            final List advices) {

        for (Iterator it = advices.iterator(); it.hasNext();) {

            final String adviceName = (String)it.next();
            if (AspectWerkz.getSystem(uuid).getAdvice(adviceName) instanceof PreAdvice) {
                AspectWerkz.getSystem(uuid).getAspect(classPattern).
                        getGetFieldPointcut(fieldPattern).
                        addPreAdvice(adviceName);
            }
            else if (AspectWerkz.getSystem(uuid).getAdvice(adviceName) instanceof PostAdvice) {
                AspectWerkz.getSystem(uuid).getAspect(classPattern).
                        getGetFieldPointcut(fieldPattern).
                        addPostAdvice(adviceName);
            }
        }
    }

    /**
     * Adds advices to a throws pointcut.
     *
     * @param uuid the UUID for the AspectWerkz system to use
     * @param className the class name holding the aspect
     * @param throwsPattern the throws pattern
     * @param advices the advices
     */
    private static void addAdvicesToThrowsPointcut(
            final String uuid,
            final ClassPattern classPattern,
            final String throwsPattern,
            final List advices) {

        for (Iterator it = advices.iterator(); it.hasNext();) {
            AspectWerkz.getSystem(uuid).getAspect(classPattern).
                    getThrowsPointcut(throwsPattern).
                    addAdvice((String)it.next());
        }
    }

    /**
     * Adds advices to a caller side pointcut.
     *
     * @param uuid the UUID for the AspectWerkz system to use
     * @param className the class name
     * @param methodPattern the method pattern
     * @param advices the advices
     */
    private static void addAdvicesToCallerSidePointcut(
            final String uuid,
            final ClassPattern classPattern,
            final String methodPattern,
            final List advices) {

        for (Iterator it = advices.iterator(); it.hasNext();) {

            final String adviceName = (String)it.next();
            if (AspectWerkz.getSystem(uuid).getAdvice(adviceName) instanceof PreAdvice) {
                AspectWerkz.getSystem(uuid).getAspect(classPattern).
                        getCallerSidePointcut(methodPattern).
                        addPreAdvice(adviceName);
            }
            else if (AspectWerkz.getSystem(uuid).getAdvice(adviceName) instanceof PostAdvice) {
                AspectWerkz.getSystem(uuid).getAspect(classPattern).
                        getCallerSidePointcut(methodPattern).
                        addPostAdvice(adviceName);
            }
        }
    }

    /**
     * Adds advice stacks to a method pointcut.
     *
     * @param uuid the UUID for the AspectWerkz system to use
     * @param className the class name
     * @param methodPattern the method pattern
     * @param adviceStacks the advice stacks
     * @param weaveModel the weave model
     */
    private static void addAdviceStacksToMethodPointcut(
            final String uuid,
            final ClassPattern classPattern,
            final String methodPattern,
            final List adviceStacks,
            final WeaveModel weaveModel) {

        for (Iterator it1 = adviceStacks.iterator(); it1.hasNext();) {
            String adviceStackName = (String)it1.next();
            AdviceStackDefinition adviceStackDef =
                    weaveModel.getAdviceStackDefinition(adviceStackName);
            if (adviceStackDef == null) {
                throw new DefinitionException("no advice stack defined for: " + classPattern + "." + methodPattern);
            }
            for (Iterator it2 = adviceStackDef.getAdvices().iterator(); it2.hasNext();) {
                AspectWerkz.getSystem(uuid).getAspect(classPattern).
                        getMethodPointcut(methodPattern).
                        addAdvice((String)it2.next());
            }
        }
    }

    /**
     * Adds advice stacks to a set field pointcut.
     *
     * @param uuid the UUID for the AspectWerkz system to use
     * @param className the class name
     * @param fieldPattern the field pattern
     * @param adviceStacks the advice stacks
     * @param weaveModel the weave model
     */
    private static void addAdviceStacksToSetFieldPointcut(
            final String uuid,
            final ClassPattern classPattern,
            final String fieldPattern,
            final List adviceStacks,
            final WeaveModel weaveModel) {

        for (Iterator it1 = adviceStacks.iterator(); it1.hasNext();) {
            String adviceStackName = (String)it1.next();
            AdviceStackDefinition adviceStackDef =
                    weaveModel.getAdviceStackDefinition(adviceStackName);
            if (adviceStackDef == null) {
                throw new DefinitionException("no advice stack defined for: " + classPattern + "." + fieldPattern);
            }
            for (Iterator it2 = adviceStackDef.getAdvices().iterator(); it2.hasNext();) {
                final String adviceName = (String)it2.next();
                if (AspectWerkz.getSystem(uuid).getAdvice(adviceName) instanceof PreAdvice) {
                    AspectWerkz.getSystem(uuid).getAspect(classPattern).
                            getSetFieldPointcut(fieldPattern).
                            addPreAdvice(adviceName);
                }
                else if (AspectWerkz.getSystem(uuid).getAdvice(adviceName) instanceof PostAdvice) {
                    AspectWerkz.getSystem(uuid).getAspect(classPattern).
                            getSetFieldPointcut(fieldPattern).
                            addPostAdvice(adviceName);
                }
            }
        }
    }

    /**
     * Adds advice stacks to a get field pointcut.
     *
     * @param uuid the UUID for the AspectWerkz system to use
     * @param className the class name
     * @param fieldPattern the field pattern
     * @param adviceStacks the advice stacks
     * @param weaveModel the weave model
     */
    private static void addAdviceStacksToGetFieldPointcut(
            final String uuid,
            final ClassPattern classPattern,
            final String fieldPattern,
            final List adviceStacks,
            final WeaveModel weaveModel) {

        for (Iterator it1 = adviceStacks.iterator(); it1.hasNext();) {
            String adviceStackName = (String)it1.next();
            AdviceStackDefinition adviceStackDef =
                    weaveModel.getAdviceStackDefinition(adviceStackName);
            if (adviceStackDef == null) {
                throw new DefinitionException("no advice stack defined for: " + classPattern + "." + fieldPattern);
            }
            for (Iterator it2 = adviceStackDef.getAdvices().iterator(); it2.hasNext();) {
                final String adviceName = (String)it2.next();
                if (AspectWerkz.getSystem(uuid).getAdvice(adviceName) instanceof PreAdvice) {
                    AspectWerkz.getSystem(uuid).getAspect(classPattern).
                            getGetFieldPointcut(fieldPattern).
                            addPreAdvice(adviceName);
                }
                else if (AspectWerkz.getSystem(uuid).getAdvice(adviceName) instanceof PostAdvice) {
                    AspectWerkz.getSystem(uuid).getAspect(classPattern).
                            getGetFieldPointcut(fieldPattern).
                            addPostAdvice(adviceName);
                }
            }
        }
    }

    /**
     * Adds advice stacks to a throws pointcut.
     *
     * @param uuid the UUID for the AspectWerkz system to use
     * @param className the class name
     * @param throwsPattern the throws pattern
     * @param adviceStacks the advice stacks
     * @param weaveModel the weave model
     */
    private static void addAdviceStacksToThrowsPointcut(
            final String uuid,
            final ClassPattern classPattern,
            final String throwsPattern,
            final List adviceStacks,
            final WeaveModel weaveModel) {

        for (Iterator it1 = adviceStacks.iterator(); it1.hasNext();) {
            String adviceStackName = (String)it1.next();
            AdviceStackDefinition adviceStackDef =
                    weaveModel.getAdviceStackDefinition(adviceStackName);
            if (adviceStackDef == null) {
                throw new DefinitionException("no advice stack defined for: " + classPattern + "." + throwsPattern);
            }
            for (Iterator it2 = adviceStackDef.getAdvices().iterator(); it2.hasNext();) {
                AspectWerkz.getSystem(uuid).getAspect(classPattern).
                        getThrowsPointcut(throwsPattern).
                        addAdvice((String)it2.next());
            }
        }
    }

    /**
     * Adds advice stacks to a caller side pointcut.
     *
     * @param className the class name
     * @param methodPattern the method pattern
     * @param adviceStacks the advice stacks
     * @param weaveModel the weave model
     */
    private static void addAdviceStacksToCallerSidePointcut(
            final String uuid,
            final ClassPattern classPattern,
            final String methodPattern,
            final List adviceStacks,
            final WeaveModel weaveModel) {

        for (Iterator it1 = adviceStacks.iterator(); it1.hasNext();) {
            String adviceStackName = (String)it1.next();
            AdviceStackDefinition adviceStackDef =
                    weaveModel.getAdviceStackDefinition(adviceStackName);
            if (adviceStackDef == null) {
                throw new DefinitionException("no advice stack defined for: " + classPattern + "." + methodPattern);
            }
            for (Iterator it2 = adviceStackDef.getAdvices().iterator(); it2.hasNext();) {
                final String adviceName = (String)it2.next();
                if (AspectWerkz.getSystem(uuid).getAdvice(adviceName) instanceof PreAdvice) {
                    AspectWerkz.getSystem(uuid).getAspect(classPattern).
                            getCallerSidePointcut(methodPattern).
                            addPreAdvice(adviceName);
                }
                else if (AspectWerkz.getSystem(uuid).getAdvice(adviceName) instanceof PostAdvice) {
                    AspectWerkz.getSystem(uuid).getAspect(classPattern).
                            getCallerSidePointcut(methodPattern).
                            addPostAdvice(adviceName);
                }
            }
        }
    }

    /**
     * Creates the persistence manager.
     */
    private static void loadPersistenceManager() {
        s_persistenceManager = PersistenceManagerFactory.getFactory(
                PersistenceManagerFactory.getPersistenceManagerType()).
                createPersistenceManager();
    }

    /**
     * Private constructor to prevent instantiabiliy.
     */
    private DefinitionManager() {
    }
}
