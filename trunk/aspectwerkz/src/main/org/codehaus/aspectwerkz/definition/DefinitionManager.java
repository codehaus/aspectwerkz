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
 * @author <a href="mailto:jboner@acm.org">Jonas Bonér</a>
 * @version $Id: DefinitionManager.java,v 1.2 2003-05-12 09:41:29 jboner Exp $
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
     * The persistence manager to use.
     */
    private static PersistenceManager s_persistenceManager;

    /**
     * Loads the persistence manager.
     */
    static {
        s_persistenceManager = PersistenceManagerFactory.getFactory(
                PersistenceManagerFactory.getPersistenceManagerType()).
                createPersistenceManager();
    }

    /**
     * Loads the system definition.
     */
    public static void loadDefinition() {
        if (s_initialized) return;
        s_initialized = true;

        s_weaveModel = WeaveModel.loadModel();

        createAspects(s_weaveModel);
        registerDirtyFieldCheckAdvice();

        registerIntroductions(s_weaveModel);
        registerAdvices(s_weaveModel);
        registerPointcuts(s_weaveModel);
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
     * @param weaveModel the weaveModel
     */
    private static void createAspects(final WeaveModel weaveModel) {
        for (Iterator it = weaveModel.getAspectPatterns().iterator(); it.hasNext();) {
            AspectWerkz.register(new Aspect((ClassPattern)it.next()));
        }
    }

    /**
     * Creates and registers the introductions defined.
     *
     * @param weaveModel the weaveModel
     */
    private static void registerIntroductions(final WeaveModel weaveModel) {
        for (Iterator it = weaveModel.getIntroductionDefinitions().iterator(); it.hasNext();) {

            final IntroductionDefinition def = (IntroductionDefinition)it.next();
            final String implClassName = def.getImplementation();
            final String intfClassName = def.getInterface();

            Class implClass = null;
            if (implClassName != null) {

                // create an aspect for the introduction as well
                AspectWerkz.register(new Aspect(
                        Pattern.compileClassPattern(implClassName)));

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

                List aspects = AspectWerkz.getAspects(implClassName);
                for (Iterator it2 = aspects.iterator(); it2.hasNext();) {
                    Aspect aspect = (Aspect)it2.next();
                    aspect.createSetFieldPointcut(DirtyFieldCheckAdvice.PATTERN).
                            addPostAdvice(DirtyFieldCheckAdvice.NAME);
                }
                s_persistenceManager.register(def.getName());
            }
            else {
                newIntroduction.setMemoryStrategy(
                        new TransientIntroductionMemoryStrategy(implClass));
            }
            AspectWerkz.register(def.getName(), newIntroduction);
        }
    }

    /**
     * Creates and registers the advices defined.
     *
     * @todo what if the context classloader is NULL? Need a plan B.
     * @param weaveModel the weaveModel
     */
    private static void registerAdvices(final WeaveModel weaveModel) {

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
                AspectWerkz.register(new Aspect(def.getClassName()));

                if (def.isPersistent()) {
                    advice.setMemoryStrategy(
                            new PersistableAdviceMemoryStrategy(advice));

                    List aspects = AspectWerkz.getAspects(def.getClassName());
                    for (Iterator it2 = aspects.iterator(); it2.hasNext();) {
                        Aspect aspect = (Aspect)it2.next();
                        aspect.createSetFieldPointcut(DirtyFieldCheckAdvice.PATTERN).
                                addPostAdvice(DirtyFieldCheckAdvice.NAME);
                    }
                    s_persistenceManager.register(advice.getName());
                }
                else {
                    advice.setMemoryStrategy(
                            new TransientAdviceMemoryStrategy(advice));
                }

                AspectWerkz.register(def.getName(), advice);
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
     * @param weaveModel the weaveModel
     */
    private static void registerPointcuts(final WeaveModel weaveModel) {
        boolean isThreadSafe = true;

        for (Iterator it1 = weaveModel.getAspectPatterns().iterator(); it1.hasNext();) {
            final ClassPattern aspectPattern = (ClassPattern)it1.next();
            registerIntroductions(weaveModel, aspectPattern);
            registerMethodPointcuts(aspectPattern, isThreadSafe, weaveModel);
            registerSetFieldPointcuts(aspectPattern, isThreadSafe, weaveModel);
            registerGetFieldPointcuts(aspectPattern, isThreadSafe, weaveModel);
            registerThrowsPointcuts(aspectPattern, isThreadSafe, weaveModel);
            registerCallerSidePointcuts(aspectPattern, isThreadSafe, weaveModel);
        }
    }

    /**
     * Registers the introductions.
     *
     * @param className the class name
     * @param weaveModel the weave model
     */
    private static void registerIntroductions(
            final WeaveModel weaveModel,
            final ClassPattern aspectPattern) {

        for (Iterator it2 = weaveModel.getIntroductionNames(aspectPattern).iterator(); it2.hasNext();) {
            AspectWerkz.getAspect(aspectPattern).addIntroduction((String)it2.next());
        }
    }

    /**
     * Registers the method pointcuts.
     *
     * @param className the class name
     * @param threadSafe thread-safe or not
     * @param weaveModel the weave model
     */
    private static void registerMethodPointcuts(
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

            AspectWerkz.getAspect(aspectPattern).
                    createMethodPointcut(methodPattern, threadSafe);

            for (Iterator it2 = pointcuts.iterator(); it2.hasNext();) {
                PointcutDefinition pointcutDef = (PointcutDefinition)it2.next();
                addAdvicesToMethodPointcut(
                        aspectPattern, methodPattern,
                        pointcutDef.getAdvices());
                addAdviceStacksToMethodPointcut(
                        aspectPattern, methodPattern,
                        pointcutDef.getAdviceStacks(),
                        weaveModel);
            }
        }
    }

    /**
     * Registers the set field pointcuts.
     *
     * @param className the class name
     * @param threadSafe thread-safe or not
     * @param weaveModel the weave model
     */
    private static void registerSetFieldPointcuts(
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

                AspectWerkz.getAspect(aspectPattern).
                        createSetFieldPointcut(fieldPattern, threadSafe);

                addAdvicesToSetFieldPointcut(
                        aspectPattern, fieldPattern,
                        pointcutDef.getAdvices());

                addAdviceStacksToSetFieldPointcut(
                        aspectPattern, fieldPattern,
                        pointcutDef.getAdviceStacks(),
                        weaveModel);
            }
        }
    }

    /**
     * Registers the get field pointcuts.
     *
     * @param className the class name
     * @param threadSafe thread-safe or not
     * @param weaveModel the weave model
     */
    private static void registerGetFieldPointcuts(
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

                AspectWerkz.getAspect(aspectPattern).
                        createGetFieldPointcut(fieldPattern, threadSafe);

                addAdvicesToGetFieldPointcut(
                        aspectPattern, fieldPattern,
                        pointcutDef.getAdvices());

                addAdviceStacksToGetFieldPointcut(
                        aspectPattern, fieldPattern,
                        pointcutDef.getAdviceStacks(),
                        weaveModel);
            }
        }
    }

    /**
     * Registers the throws pointcuts.
     *
     * @param className the class name
     * @param threadSafe thread-safe or not
     * @param weaveModel the weave model
     */
    private static void registerThrowsPointcuts(
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
                    AspectWerkz.getAspect(aspectPattern).createThrowsPointcut(
                            pattern, threadSafe);

                    addAdvicesToThrowsPointcut(
                            aspectPattern, pattern,
                            pointcutDef.getAdvices());

                    addAdviceStacksToThrowsPointcut(
                            aspectPattern, pattern,
                            pointcutDef.getAdviceStacks(),
                            weaveModel);
                }
            }
        }
    }

    /**
     * Registers the caller side pointcuts.
     *
     * @param className the class name
     * @param threadSafe thread-safe or not
     * @param weaveModel the weave model
     */
    private static void registerCallerSidePointcuts(
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

                    AspectWerkz.getAspect(aspectPattern).
                            createCallerSidePointcut(targetMethodName, threadSafe);

                    addAdvicesToCallerSidePointcut(
                            aspectPattern, targetMethodName,
                            pointcutDef.getAdvices());

                    addAdviceStacksToCallerSidePointcut(
                            aspectPattern, targetMethodName,
                            pointcutDef.getAdviceStacks(),
                            weaveModel);
                }
            }
        }
    }

    /**
     * Creates and registers an advice for checking if fields in target objects
     * are "dirty", used by the persistence engine.
     */
    private static void registerDirtyFieldCheckAdvice() {
        AspectWerkz.register(
                DirtyFieldCheckAdvice.NAME,
                new DirtyFieldCheckAdvice());
    }

    /**
     * Adds advices to a method pointcut.
     *
     * @param className the class name
     * @param methodPattern the method pattern
     * @param advices the advices
     */
    private static void addAdvicesToMethodPointcut(
            final ClassPattern classPattern,
            final String methodPattern,
            final List advices) {

        for (Iterator it = advices.iterator(); it.hasNext();) {
            String advice = (String)it.next();
            AspectWerkz.getAspect(classPattern).
                    getMethodPointcut(methodPattern).
                    addAdvice(advice);
        }
    }

    /**
     * Adds advices to a set field pointcut.
     *
     * @param className the class name
     * @param fieldPattern the field pattern
     * @param advices the advices
     */
    private static void addAdvicesToSetFieldPointcut(
            final ClassPattern classPattern,
            final String fieldPattern,
            final List advices) {

        for (Iterator it = advices.iterator(); it.hasNext();) {

            final String adviceName = (String)it.next();
            if (AspectWerkz.getAdvice(adviceName) instanceof PreAdvice) {
                AspectWerkz.getAspect(classPattern).
                        getSetFieldPointcut(fieldPattern).
                        addPreAdvice(adviceName);
            }
            else if (AspectWerkz.getAdvice(adviceName) instanceof PostAdvice) {
                AspectWerkz.getAspect(classPattern).
                        getSetFieldPointcut(fieldPattern).
                        addPostAdvice(adviceName);
            }
        }
    }

    /**
     * Adds advices to a set field pointcut.
     *
     * @param className the class name
     * @param fieldPattern the field pattern
     * @param advices the advices
     */
    private static void addAdvicesToGetFieldPointcut(
            final ClassPattern classPattern,
            final String fieldPattern,
            final List advices) {

        for (Iterator it = advices.iterator(); it.hasNext();) {

            final String adviceName = (String)it.next();
            if (AspectWerkz.getAdvice(adviceName) instanceof PreAdvice) {
                AspectWerkz.getAspect(classPattern).
                        getGetFieldPointcut(fieldPattern).
                        addPreAdvice(adviceName);
            }
            else if (AspectWerkz.getAdvice(adviceName) instanceof PostAdvice) {
                AspectWerkz.getAspect(classPattern).
                        getGetFieldPointcut(fieldPattern).
                        addPostAdvice(adviceName);
            }
        }
    }

    /**
     * Adds advices to a throws pointcut.
     *
     * @param className the class name holding the aspect
     * @param throwsPattern the throws pattern
     * @param advices the advices
     */
    private static void addAdvicesToThrowsPointcut(
            final ClassPattern classPattern,
            final String throwsPattern,
            final List advices) {

        for (Iterator it = advices.iterator(); it.hasNext();) {
            AspectWerkz.getAspect(classPattern).
                    getThrowsPointcut(throwsPattern).
                    addAdvice((String)it.next());
        }
    }

    /**
     * Adds advices to a caller side pointcut.
     *
     * @param className the class name
     * @param methodPattern the method pattern
     * @param advices the advices
     */
    private static void addAdvicesToCallerSidePointcut(
            final ClassPattern classPattern,
            final String methodPattern,
            final List advices) {

        for (Iterator it = advices.iterator(); it.hasNext();) {

            final String adviceName = (String)it.next();
            if (AspectWerkz.getAdvice(adviceName) instanceof PreAdvice) {
                AspectWerkz.getAspect(classPattern).
                        getCallerSidePointcut(methodPattern).
                        addPreAdvice(adviceName);
            }
            else if (AspectWerkz.getAdvice(adviceName) instanceof PostAdvice) {
                AspectWerkz.getAspect(classPattern).
                        getCallerSidePointcut(methodPattern).
                        addPostAdvice(adviceName);
            }
        }
    }

    /**
     * Adds advice stacks to a method pointcut.
     *
     * @param className the class name
     * @param methodPattern the method pattern
     * @param adviceStacks the advice stacks
     * @param weaveModel the weave model
     */
    private static void addAdviceStacksToMethodPointcut(
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
                AspectWerkz.getAspect(classPattern).
                        getMethodPointcut(methodPattern).
                        addAdvice((String)it2.next());
            }
        }
    }

    /**
     * Adds advice stacks to a set field pointcut.
     *
     * @param className the class name
     * @param fieldPattern the field pattern
     * @param adviceStacks the advice stacks
     * @param weaveModel the weave model
     */
    private static void addAdviceStacksToSetFieldPointcut(
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
                if (AspectWerkz.getAdvice(adviceName) instanceof PreAdvice) {
                    AspectWerkz.getAspect(classPattern).
                            getSetFieldPointcut(fieldPattern).
                            addPreAdvice(adviceName);
                }
                else if (AspectWerkz.getAdvice(adviceName) instanceof PostAdvice) {
                    AspectWerkz.getAspect(classPattern).
                            getSetFieldPointcut(fieldPattern).
                            addPostAdvice(adviceName);
                }
            }
        }
    }

    /**
     * Adds advice stacks to a get field pointcut.
     *
     * @param className the class name
     * @param fieldPattern the field pattern
     * @param adviceStacks the advice stacks
     * @param weaveModel the weave model
     */
    private static void addAdviceStacksToGetFieldPointcut(
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
                if (AspectWerkz.getAdvice(adviceName) instanceof PreAdvice) {
                    AspectWerkz.getAspect(classPattern).
                            getGetFieldPointcut(fieldPattern).
                            addPreAdvice(adviceName);
                }
                else if (AspectWerkz.getAdvice(adviceName) instanceof PostAdvice) {
                    AspectWerkz.getAspect(classPattern).
                            getGetFieldPointcut(fieldPattern).
                            addPostAdvice(adviceName);
                }
            }
        }
    }

    /**
     * Adds advice stacks to a throws pointcut.
     *
     * @param className the class name
     * @param throwsPattern the throws pattern
     * @param adviceStacks the advice stacks
     * @param weaveModel the weave model
     */
    private static void addAdviceStacksToThrowsPointcut(
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
                AspectWerkz.getAspect(classPattern).
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
                if (AspectWerkz.getAdvice(adviceName) instanceof PreAdvice) {
                    AspectWerkz.getAspect(classPattern).
                            getCallerSidePointcut(methodPattern).
                            addPreAdvice(adviceName);
                }
                else if (AspectWerkz.getAdvice(adviceName) instanceof PostAdvice) {
                    AspectWerkz.getAspect(classPattern).
                            getCallerSidePointcut(methodPattern).
                            addPostAdvice(adviceName);
                }
            }
        }
    }

    /**
     * Private constructor to prevent instantiabiliy.
     */
    private DefinitionManager() {
    }
}
