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
package org.codehaus.aspectwerkz;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collections;
import java.lang.reflect.Method;

import gnu.trove.TObjectIntHashMap;
import gnu.trove.THashMap;

import org.codehaus.aspectwerkz.advice.Advice;
import org.codehaus.aspectwerkz.advice.TransientAdviceMemoryStrategy;
import org.codehaus.aspectwerkz.advice.AbstractAdvice;
import org.codehaus.aspectwerkz.introduction.Introduction;
import org.codehaus.aspectwerkz.definition.DefinitionManager;
import org.codehaus.aspectwerkz.definition.regexp.ClassPattern;
import org.codehaus.aspectwerkz.definition.regexp.Pattern;
import org.codehaus.aspectwerkz.transform.TransformationUtil;
import org.codehaus.aspectwerkz.exception.DefinitionException;

/**
 * Manages the aspects in the AspectWerkz system.<br/>
 * Handles the initialization and configuration of the system.<br/>
 * Stores and indexes the aspects defined in the system.<br/>
 * Stores and indexes the advised methods.<br/>
 * Stores and indexes the introduced methods.<br/>
 *
 * @author <a href="mailto:jboner@acm.org">Jonas Bonér</a>
 * @version $Id: AspectWerkz.java,v 1.1.1.1 2003-05-11 15:13:31 jboner Exp $
 */
public final class AspectWerkz {

    /**
     * Holds references to all the the aspects in the system.
     */
    private static final Map s_aspects = new THashMap();

    /**
     * A cache for the aspects, maps the fully qualified name of a class
     * to its aspects.
     *
     * @todo when unweaving (and reordering) of aspects is supported then this cache must have a way of being invalidated.
     */
    private static final Map s_aspectCache = new THashMap();

    /**
     * Holds references to all the the advised methods in the system.
     */
    private static final Map s_methods = new THashMap();

    /**
     * Holds references to all the the advices in the system.
     */
    private static Advice[] s_advices = new Advice[0];

    /**
     * Holds the indexes for the advices.
     */
    private static final TObjectIntHashMap s_adviceIndexes =
            new TObjectIntHashMap();

    /**
     * Holds references to all the the introductions in the system.
     */
    private static Introduction[] s_introductions = new Introduction[0];

    /**
     * Holds the indexes for the introductions.
     */
    private static final TObjectIntHashMap s_introductionIndexes =
            new TObjectIntHashMap();

    /**
     * Marks the system as initialized.
     */
    private static boolean s_initialized = false;

    /**
     * Registers a new aspect for a specific class.
     *
     * @param aspect the aspect to register
     */
    public static void register(final Aspect aspect) {
        if (aspect == null) throw new IllegalArgumentException("aspect can not be null");
        if (aspect.getPattern() == null) throw new IllegalArgumentException("aspect name can not be null");
        synchronized (s_aspects) {
            s_aspects.put(aspect.getClassPattern(), aspect);
        }
    }

    /**
     * Registers a new advice and maps it to a name.
     *
     * @param name the name to map the advice to
     * @param advice the advice to register
     */
    public static void register(final String name, final Advice advice) {
        synchronized (s_adviceIndexes) {
            synchronized (s_advices) {
                final int index = s_advices.length + 1;
                s_adviceIndexes.put(name, index);

                final Advice[] tmp = new Advice[s_advices.length + 1];
                System.arraycopy(s_advices, 0, tmp, 0, s_advices.length);

                tmp[s_advices.length] = advice;

                s_advices = new Advice[s_advices.length + 1];
                System.arraycopy(tmp, 0, s_advices, 0, tmp.length);
            }
        }
    }

    /**
     * Registers an introduction and maps it to a name.
     *
     * @param name the name to map the introduction to
     * @param introduction the introduction to register
     */
    public static void register(final String name,
                                final Introduction introduction) {
        synchronized (s_introductions) {
            synchronized (s_introductionIndexes) {

                final int index = s_introductions.length + 1;
                s_introductionIndexes.put(name, index);

                Introduction[] tmp = new Introduction[s_introductions.length + 1];
                System.arraycopy(s_introductions, 0, tmp, 0, s_introductions.length);

                tmp[s_introductions.length] = introduction;

                s_introductions = new Introduction[s_introductions.length + 1];
                System.arraycopy(tmp, 0, s_introductions, 0, tmp.length);
            }
        }
    }

    /**
     * Creates a new aspect for the class specified.
     *
     * @param klass the class
     * @return the aspect
     */
    public static Aspect createAspect(final ClassPattern classPattern) {
        if (classPattern == null) throw new IllegalArgumentException("class pattern can not be null");
        if (s_aspects.containsKey(classPattern)) {
            return (Aspect)s_aspects.get(classPattern);
        }
        else {
            synchronized (s_aspects) {
                final Aspect aspect = new Aspect(classPattern);
                s_aspects.put(classPattern, aspect);
                return aspect;
            }
        }
    }

    /**
     * Creates and registers new advice at runtime.
     *
     * @todo to enable persistent advices the weaveModel must be modified before the class is loaded (to enable transformation of the advice)
     *
     * @param name the name of the advice
     * @param className the class name of the advice
     * @param deploymentModel the deployment model for the advice
     * @param loader an optional class loader (if null it uses the context classloader)
     */
    public static void createAdvice(final String name,
                                    final String className,
                                    final String deploymentModel,
//                                    final boolean isPersistent,
                                    final ClassLoader loader) {
        AbstractAdvice advice = null;
        Class adviceClass = null;
        try {
            if (loader == null) {
                adviceClass = Thread.currentThread().
                        getContextClassLoader().loadClass(className);
            }
            else {
                adviceClass = loader.loadClass(className);
            }
            advice = (AbstractAdvice)adviceClass.newInstance();
        }
        catch (Exception e) {
            StringBuffer cause = new StringBuffer();
            cause.append("could not deploy new advice with name ");
            cause.append(name);
            cause.append(" and class ");
            cause.append(className);
            cause.append(" due to: ");
            cause.append(e);
            throw new RuntimeException(cause.toString());
        }

        advice.setDeploymentModel(DeploymentModel.
                getDeploymentModelAsInt(deploymentModel));
        advice.setName(name);
        advice.setAdviceClass(advice.getClass());

        ClassPattern classPattern = Pattern.compileClassPattern(className);

        // create an aspect for the advice
        AspectWerkz.createAspect(classPattern);

        // TODO: for persistent advices: update the weaveModel to support weaving of the new advice here...

        // if the advice should be persistent
//        if (isPersistent) {
//            advice.setMemoryStrategy(
//                    new PersistableAdviceMemoryStrategy(advice));
//
//            AspectWerkz.getAspect(classPattern).
//                    createSetFieldPointcut(DirtyFieldCheckAdvice.PATTERN).
//                    addPostAdvice(DirtyFieldCheckAdvice.NAME);
//
//            PersistenceManagerFactory.getFactory(
//                    PersistenceManagerFactory.getPersistenceManagerType()).
//                    createPersistenceManager().register(advice.getName());
//        }
//        else {
           advice.setMemoryStrategy(
                    new TransientAdviceMemoryStrategy(advice));
//        }

        // register the advice
        AspectWerkz.register(name, advice);
    }

    /**
     * Returns the aspect for the pattern specified.
     *
     * @param pattern the pattern for the aspect
     * @return the aspect
     */
    public static Aspect getAspect(final String pattern) {
        if (pattern == null) throw new IllegalArgumentException("class pattern can not be null");
        ClassPattern classPattern = Pattern.compileClassPattern(pattern);

        if (s_aspects.containsKey(classPattern)) {
            return (Aspect)s_aspects.get(classPattern);
        }
        else {
            initialize();
            if (s_aspects.containsKey(classPattern)) {
                return (Aspect)s_aspects.get(classPattern);
            }
            else {
                throw new DefinitionException(classPattern.getPattern() + " does not have any aspects defined");
            }
        }
    }

    /**
     * Returns the aspect for the class pattern specified.
     *
     * @param classPattern the class pattern
     * @return the aspect
     */
    public static Aspect getAspect(final ClassPattern classPattern) {
        if (classPattern == null) throw new IllegalArgumentException("class pattern can not be null");
        if (s_aspects.containsKey(classPattern)) {
            return (Aspect)s_aspects.get(classPattern);
        }
        else {
            initialize();
            if (s_aspects.containsKey(classPattern)) {
                return (Aspect)s_aspects.get(classPattern);
            }
            else {
                throw new DefinitionException(classPattern.getPattern() + " does not have any aspects defined");
            }
        }
    }

    /**
     * Returns the aspect list for the class specified.
     * Caches the aspect list, needed since the actual method call is
     * expensive and is made each time a new instance of an advised class is
     * created.
     *
     * @param klass the class
     * @return the aspect
     */
    public static List getAspects(final String className) {
        if (className == null) throw new IllegalArgumentException("class name can not be null");
        initialize();

        // if cached; return the cached list
        if (s_aspectCache.containsKey(className)) {
            return (List)s_aspectCache.get(className);
        }

        List aspects = new ArrayList();
        for (Iterator it = s_aspects.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry)it.next();
            ClassPattern classPattern = (ClassPattern)entry.getKey();
            if (classPattern.matches(className)) {
                aspects.add(entry.getValue());
            }
        }

        synchronized (s_aspectCache) {
            s_aspectCache.put(className, aspects);
        }

        return aspects;
    }

    /**
     * Returns the index for a specific name to advice mapping.
     *
     * @param name the name of the advice
     * @return the index of the advice
     */
    public static int getAdviceIndexFor(final String name) {
        final int index = s_adviceIndexes.get(name);
        if (index == 0) throw new DefinitionException("advice " + name + " is not properly defined (this also occurs if you have introductions defined in your definition but have not specified a meta-data dir for the pre-compiled definition)");
        return index;
    }

    /**
     * Retrieves a specific advice based setfield's index.
     *
     * @param index the index of the advice
     * @return the advice
     */
    public static Advice getAdvice(final int index) {
        Advice advice;
        try {
            advice = s_advices[index - 1];
        }
        catch (ArrayIndexOutOfBoundsException e) {
            initialize();
            try {
                advice = s_advices[index - 1];
            }
            catch (ArrayIndexOutOfBoundsException e1) {
                throw new DefinitionException("no advice with index " + index);
            }
        }
        return advice;
    }

    /**
     * Returns the advice for a specific name.
     *
     * @param name the name of the advice
     * @return the the advice
     */
    public static Advice getAdvice(final String name) {
        Advice advice;
        try {
            advice = s_advices[s_adviceIndexes.get(name) - 1];
        }
        catch (ArrayIndexOutOfBoundsException e1) {
            initialize();
            try {
                advice = s_advices[s_adviceIndexes.get(name) - 1];
            }
            catch (ArrayIndexOutOfBoundsException e2) {
                throw new DefinitionException("advice " + name + " is not properly defined");
            }
        }
        return advice;
    }

    /**
     * Returns the index for a specific name to introduction mapping.
     *
     * @param name the name of the introduction
     * @return the index of the introduction
     */
    public static int getIntroductionIndexFor(final String name) {
        final int index = s_introductionIndexes.get(name);
        if (index == 0) throw new DefinitionException("introduction " + name + " is not properly defined");
        return index;
    }

    /**
     * Retrieves a specific introduction based it's index.
     *
     * @param index the index of the introduction
     * @return the introduction
     */
    public static Introduction getIntroduction(final int index) {
        Introduction introduction;
        try {
            introduction = s_introductions[index - 1];
        }
        catch (ArrayIndexOutOfBoundsException e1) {
            initialize();
            try {
                introduction = s_introductions[index - 1];
            }
            catch (ArrayIndexOutOfBoundsException e2) {
                throw new DefinitionException("no introduction with index " + index);
            }
        }
        return introduction;
    }

    /**
     * Returns the introduction for a specific name.
     *
     * @param name the name of the introduction
     * @return the the introduction
     */
    public static Introduction getIntroduction(final String name) {
        Introduction introduction;
        try {
            introduction = s_introductions[s_introductionIndexes.get(name) - 1];
        }
        catch (ArrayIndexOutOfBoundsException e1) {
            initialize();
            try {
                introduction = s_introductions[s_introductionIndexes.get(name) - 1];
            }
            catch (ArrayIndexOutOfBoundsException e2) {
                throw new DefinitionException("no introduction with name " + name);
            }
        }
        return introduction;
    }

    /**
     * Returns a specific method by the class and the method index.
     *
     * @param klass the class housing the method
     * @param index the method index
     * @return the method
     */
    public static Method getMethod(final Class klass, final int index) {
        if (klass == null) throw new IllegalArgumentException("class can not be null");
        if (index < 0) throw new IllegalArgumentException("method index can not be less than 0");
        Method method;
        try {
            // create the method repository lazily
            if (!s_methods.containsKey(klass)) {
                createMethodRepository(klass);
            }
            method = ((Method[])s_methods.get(klass))[index];
        }
        catch (NullPointerException e1) {
            initialize();
            try {
                method = ((Method[])s_methods.get(klass))[index];
            }
            catch (NullPointerException e2) {
                throw new DefinitionException(klass + " does not have any aspects defined");
            }
        }
        return method;
    }

    /**
     * Checks if a specific class has an aspect defined.
     *
     * @param klass the class
     * @return boolean true if the class has an aspect defined
     */
    public static boolean hasAspect(final String className) {
        if (className == null) throw new IllegalArgumentException("class name can not be null");
        if (className == null) throw new IllegalArgumentException("class name can not be null");
        initialize();

        // if cached => has aspects
        if (s_aspectCache.containsKey(className)) {
            return true;
        }

        for (Iterator it = s_aspects.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry)it.next();
            ClassPattern classPattern = (ClassPattern)entry.getKey();
            if (classPattern.matches(className)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Removes the AspectWerkz specific elements from the stack trace.
     *
     * @param exception the Throwable to modify the stack trace on
     * @param className the name of the fake origin class of the exception
     */
    public static void fakeStackTrace(final Throwable exception,
                                      final String className) {
        if (exception == null) throw new IllegalArgumentException("exception can not be null");
        if (className == null) throw new IllegalArgumentException("class name can not be null");
        final List newStackTraceList = new ArrayList();
        final StackTraceElement[] stackTrace = exception.getStackTrace();
        int i;
        for (i = 1; i < stackTrace.length; i++) {
            if (stackTrace[i].getClassName().equals(className)) break;
        }
        for (int j = i; j < stackTrace.length; j++) {
            newStackTraceList.add(stackTrace[j]);
        }
        final StackTraceElement[] newStackTrace =
                new StackTraceElement[newStackTraceList.size()];
        int k = 0;
        for (Iterator it = newStackTraceList.iterator(); it.hasNext(); k++) {
            final StackTraceElement element = (StackTraceElement)it.next();
            newStackTrace[k] = element;
        }
        exception.setStackTrace(newStackTrace);
    }

    /**
     * Initializes the system.
     */
    public synchronized static void initialize() {
        if (s_initialized) return;
        s_initialized = true;
        DefinitionManager.loadDefinition();
    }

    /**
     * Creates a new method repository for the class specified.
     *
     * @param klass the class
     */
    protected static void createMethodRepository(final Class klass) {
        if (klass == null) throw new IllegalArgumentException("class can not be null");

        final List toSort = new ArrayList();
        final Method[] declaredMethods = klass.getDeclaredMethods();

        for (int i = 0; i < declaredMethods.length; i++) {
            // add only the advised original methods to the lookup table
            // => method pairs, original:proxy
            if (declaredMethods[i].getName().startsWith(
                    TransformationUtil.ORIGINAL_METHOD_PREFIX)) {
                toSort.add(declaredMethods[i]);
            }
        }
        Collections.sort(toSort, MethodComparator.getInstance(
                MethodComparator.PREFIXED_METHOD));

        final Method[] sortedMethods = new Method[toSort.size()];
        for (int i = 0; i < sortedMethods.length; i++) {
            sortedMethods[i] = (Method)toSort.get(i);
        }
        synchronized (s_methods) {
            s_methods.put(klass, sortedMethods);
        }
    }
}
