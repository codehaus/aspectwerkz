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

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Iterator;
import java.io.FileInputStream;
import java.io.File;
import java.io.Serializable;
import java.io.ObjectInputStream;
import java.net.URL;

import gnu.trove.TObjectIntHashMap;
import gnu.trove.THashMap;

import org.codehaus.aspectwerkz.definition.AdviceDefinition;
import org.codehaus.aspectwerkz.definition.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.definition.metadata.MetaDataCompiler;
import org.codehaus.aspectwerkz.persistence.DirtyFieldCheckAdvice;
import org.codehaus.aspectwerkz.exception.DefinitionException;

/**
 * Implements the <code>AspectWerkz</code> definition.
 *
 * @author <a href="mailto:jboner@acm.org">Jonas Bonér</a>
 * @version $Id: AspectWerkzDefinition.java,v 1.1.1.1 2003-05-11 15:13:47 jboner Exp $
 */
public class AspectWerkzDefinition implements Serializable {

    public static final String PER_JVM = "perJVM";
    public static final String PER_CLASS = "perClass";
    public static final String PER_INSTANCE = "perInstance";
    public static final String PER_THREAD = "perThread";
    public static final String THROWS_DELIMITER = "#";
    public static final String CALLER_SIDE_DELIMITER = "#";

    /**
     * The introduction definitions.
     */
    private final List m_introductionDefinitions = new ArrayList();

    /**
     * The advice definitions.
     */
    private final List m_adviceDefinitions = new ArrayList();

    /**
     * The aspect definitions.
     */
    private final List m_aspectDefinitions = new ArrayList();

    /**
     * Holds the indexes for the introductions.
     */
    private final TObjectIntHashMap m_introductionIndexes = new TObjectIntHashMap();

    /**
     * Maps the introductions to it's name.
     */
    private final Map m_introductionMap = new THashMap();

    /**
     * Maps the advices to it's name.
     */
    private final Map m_adviceMap = new THashMap();

    /**
     * Maps the aspects to it's name.
     */
    private final Map m_aspectMap = new THashMap();

    /**
     * Maps the advice stacks to it's name.
     */
    private final Map m_adviceStacks = new THashMap();

    /**
     * The path to the definition file.
     */
    public static final String DEFINITION_FILE =
            System.getProperty("aspectwerkz.definition.file", null);

    /**
     * The path to the meta-data dir.
     */
    private static String META_DATA_DIR =
            System.getProperty("aspectwerkz.metadata.dir", null);

    /**
     * The path to the aspectwerkz home directory.
     */
    private static final String ASPECTWERKZ_HOME =
            System.getProperty("aspectwerkz.home", ".");

    /**
     * Default dir for the meta-data.
     */
    public static final String DEFAULT_META_DATA_DIR = "_metaData";

    /**
     * Default name for the definition file.
     */
    public static final String DEFAULT_DEFINITION_FILE_NAME = "aspectwerkz.xml";

    /**
     * Returns the definition.
     *
     * @return the definition
     */
    public static AspectWerkzDefinition getDefinition() {
        return getDefinition(false);
    }

    /**
     * Returns the definition.
     * <p/>
     * If the file name is not specified as a parameter to the JVM it tries
     * to locate a file named 'aspectwerkz.xml' on the classpath.
     *
     * @param isDirty flag to mark the the defintion as updated or not
     * @return the definition
     */
    public static AspectWerkzDefinition getDefinition(boolean isDirty) {
        String definitionFileName;
        if (DEFINITION_FILE == null) {
            URL definition = Thread.currentThread().getContextClassLoader().
                    getResource(DEFAULT_DEFINITION_FILE_NAME);
            if (definition == null) throw new DefinitionException("no definition file specified or found on classpath (either specify the file by using the -Daspectwerkz.definition.file=.. option or by having a definition file called aspectwerkz.xml somewhere on the classpath)");
            definitionFileName = definition.getFile();
        }
        else {
            definitionFileName = DEFINITION_FILE;
        }
        return getDefinition(definitionFileName, isDirty);
    }

    /**
     * Returns the definition.
     *
     * @param definitionFile the definition file
     * @return the definition
     */
    public static AspectWerkzDefinition getDefinition(
            final String definitionFile) {
        return getDefinition(definitionFile, false);
    }

    /**
     * Returns the definition.
     *
     * @param definitionFile the definition file
     * @param isDirty flag to mark the the defintion as updated or not
     * @return the definition
     */
    public static AspectWerkzDefinition getDefinition(final String definitionFile,
                                                      boolean isDirty) {
        AspectWerkzDefinition definition =
                Dom4jXmlDefinitionParser.parse(
                        new File(definitionFile), isDirty);
        return definition;
    }

    /**
     * Creates a new qdox parser.
     */
    public AspectWerkzDefinition() {
        if (META_DATA_DIR == null) {
            locateMetaDataDir();
        }
    }

    /**
     * Returns a list with the introduction definitions registered.
     *
     * @return the introduction definitions
     */
    public List getIntroductionDefinitions() {
        return m_introductionDefinitions;
    }

    /**
     * Adds a new introductions definition.
     *
     * @param introduction the introduction definition
     */
    public void addIntroduction(final IntroductionDefinition introduction) {
        // handle the indexes
        final int index = m_introductionDefinitions.size() + 1;
        m_introductionIndexes.put(introduction.getName(), index);

        m_introductionDefinitions.add(introduction);
        m_introductionMap.put(introduction.getName(), introduction);
        final AspectDefinition aspectDefinition = new AspectDefinition();

        // if persistent; advise it with the DirtyFieldCheckAdvice
        if (introduction.isPersistent()) {
            registerDirtyFieldCheckAdvice();
            aspectDefinition.addPointcut(createDirtyFieldCheckPointcut());
        }

        // create an aspect for the introduction
        if (introduction.getImplementation() != null) {
            m_aspectMap.put(introduction.getImplementation(), aspectDefinition);
        }
    }

    /**
     * Returns a list with the advice definitions registered.
     *
     * @return the advice definitions
     */
    public List getAdviceDefinitions() {
        return m_adviceDefinitions;
    }

    /**
     * Adds an advice definition.
     *
     * @param advice the advice definition
     */
    public void addAdvice(final AdviceDefinition advice) {
        m_adviceDefinitions.add(advice);
        m_adviceMap.put(advice.getName(), advice);

        final AspectDefinition aspectDefinition = new AspectDefinition();

        // if persistent; advise it with the DirtyFieldCheckAdvice
        if (advice.isPersistent()) {
            registerDirtyFieldCheckAdvice();
            aspectDefinition.addPointcut(createDirtyFieldCheckPointcut());
        }

        // create an aspect for the advice
        m_aspectMap.put(advice.getClassName(), aspectDefinition);
    }

    /**
     * Finds an advice stack definition by its name.
     *
     * @param adviceStackName the advice stack name
     * @return the definition
     */
    public AdviceStackDefinition getAdviceStackDefinition(
            final String adviceStackName) {
        return (AdviceStackDefinition)m_adviceStacks.get(adviceStackName);
    }

    /**
     * Adds an advice stack definition.
     *
     * @param adviceStackDef the advice stack definition
     */
    public void addAdviceStack(final AdviceStackDefinition adviceStackDef) {
        m_adviceStacks.put(adviceStackDef.getName(), adviceStackDef);
    }

    /**
     * Returns a list with the aspect definitions registered.
     *
     * @return the aspect definitions
     */
    public List getAspectDefinitions() {
        return m_aspectDefinitions;
    }

    /**
     * Adds an aspect definition.
     *
     * @param aspect a new aspect definition
     */
    public void addAspect(final AspectDefinition aspect) {
        m_aspectDefinitions.add(aspect);
        m_aspectMap.put(aspect.getPattern(), aspect);
    }

    /**
     * Returns the names of the target classes.
     *
     * @return the names of the target classes
     */
    public String[] getAspectTargetClassNames() {
        String[] classNames = new String[m_aspectMap.keySet().size()];
        int i = 0;
        for (Iterator it = m_aspectMap.keySet().iterator(); it.hasNext();) {
            String key = (String)it.next();
            classNames[i++] = key;
        }
        return classNames;
    }

    /**
     * Finds the name of an advice by its attribute.
     *
     * @param attribute the attribute
     * @return the name of the advice
     */
    public String getAdviceNameByAttribute(final String attribute) {
        if (attribute == null) return null;
        for (Iterator it = m_adviceDefinitions.iterator(); it.hasNext();) {
            AdviceDefinition adviceDefinition = (AdviceDefinition)it.next();
            if (adviceDefinition.getAttribute().equals(attribute)) {
                return adviceDefinition.getName();
            }
        }
        return null;
    }

    /**
     * Finds the name of an introduction by its attribute.
     *
     * @param attribute the attribute
     * @return the name of the introduction
     */
    public String getIntroductionNameByAttribute(final String attribute) {
        if (attribute == null) return null;
        for (Iterator it = m_introductionDefinitions.iterator(); it.hasNext();) {
            IntroductionDefinition introductionDefinition = (IntroductionDefinition)it.next();
            if (introductionDefinition.getAttribute().equals(attribute)) {
                return introductionDefinition.getName();
            }
        }
        return null;
    }

    /**
     * Returns the name of the interface for an introduction.
     *
     * @param introductionName the name of the introduction
     * @return the name of the interface
     */
    public String getIntroductionInterfaceName(final String introductionName) {
        if (introductionName == null) throw new IllegalArgumentException("introduction name can not be null");
        if (!m_introductionMap.containsKey(introductionName)) {
            return null;
        }
        return ((IntroductionDefinition)m_introductionMap.
                get(introductionName)).getInterface();
    }

    /**
     * Returns the index for a specific introduction.
     *
     * @param introductionName the name of the introduction
     * @return the index
     */
    public int getIntroductionIndexFor(final String introductionName) {
        if (introductionName == null) throw new IllegalArgumentException("introduction name can not be null");
        return m_introductionIndexes.get(introductionName);
    }

    /**
     * Returns the methods meta-data for a specific introduction.
     *
     * @param introductionName the name of the introduction
     * @return a list with the methods meta-data
     */
    public List getIntroductionMethodsMetaData(final String introductionName) {
        if (introductionName == null) throw new IllegalArgumentException("introduction name can not be null");

        List methodMetaDataList = new ArrayList();
        final String implClassName = ((IntroductionDefinition)m_introductionMap.
                get(introductionName)).getImplementation();

        if (implClassName == null) { // interface introduction
            return methodMetaDataList;
        }

        final StringBuffer filename = new StringBuffer();
        filename.append(META_DATA_DIR);
        filename.append(File.separator);
        filename.append(implClassName);
        filename.append(MetaDataCompiler.META_DATA_FILE_SUFFIX);

        try {
            File file = new File(filename.toString());
            ObjectInputStream in = new ObjectInputStream(
                    new FileInputStream(file));

            final ClassMetaData classMetaData = (ClassMetaData)in.readObject();
            in.close();

            methodMetaDataList = classMetaData.getMethods();
        }
        catch (Exception e) {
            StringBuffer cause = new StringBuffer();
            cause.append("meta-data file ");
            cause.append(implClassName);
            cause.append(MetaDataCompiler.META_DATA_FILE_SUFFIX);
            cause.append(" could not be found in specified directory ");
            cause.append(META_DATA_DIR);
            cause.append(" (have you forgot to compile meta-data or specified the correct class name in the definition?)");
            cause.append(':');
            cause.append(e);
            throw new DefinitionException(cause.toString());
        }

        return methodMetaDataList;
    }

    /**
     * Tries to locate the meta-data dir.
     */
    private void locateMetaDataDir() {
        final StringBuffer metaDataDir = new StringBuffer();
        metaDataDir.append(ASPECTWERKZ_HOME);
        metaDataDir.append(File.separator);
        metaDataDir.append(DEFAULT_META_DATA_DIR);
        File dir = new File(metaDataDir.toString());
        if (!dir.exists()) {
            throw new DefinitionException("could not locate meta-data dir");
        }
        else {
            META_DATA_DIR = metaDataDir.toString();
        }
    }

    /**
     * Creates and registers the <tt>DirtyFieldCheckAdvice</tt> if it is not
     * already registered.
     */
    private void registerDirtyFieldCheckAdvice() {
        if (!m_adviceMap.containsKey(DirtyFieldCheckAdvice.NAME)) {
            final AdviceDefinition def = new AdviceDefinition();
            def.setName(DirtyFieldCheckAdvice.NAME);
            def.setAdvice(DirtyFieldCheckAdvice.CLASS);

            m_adviceMap.put(DirtyFieldCheckAdvice.NAME, def);
            m_adviceDefinitions.add(def);
        }
    }

    /**
     * Creates a new pointcut and adds the <tt>DirtyFieldCheckAdvice</tt> to it.
     *
     * @return the pointcut
     */
    private PointcutDefinition createDirtyFieldCheckPointcut() {
        final PointcutDefinition dirtyDef = new PointcutDefinition();
        dirtyDef.addAdvice(DirtyFieldCheckAdvice.NAME);
        dirtyDef.addPattern(DirtyFieldCheckAdvice.PATTERN);
        dirtyDef.setType(PointcutDefinition.SET_FIELD);
        return dirtyDef;
    }
}

/**
 * Checks if a method pointcut is thread-safe.
 *
 * @param className the name of the class
 * @param methodName the name of the method
 * @return boolean
 */
//    public boolean isMethodPointcutThreadSafe(final String className,
//                                              final String methodName) {
//        if (className == null) throw new IllegalArgumentException("class name can not be null");
//        if (methodName == null) throw new IllegalArgumentException("method name can not be null");
//        if (!m_aspectMap.containsKey(className)) {
//            return false;
//        }
//        List pointcuts = ((AspectDefinition)m_aspectMap.
//                get(className)).getPointcuts();
//        for (Iterator it = pointcuts.iterator(); it.hasNext();) {
//            PointcutDefinition pointcut = (PointcutDefinition)it.next();
//            if (pointcut.getType().equals(PointcutDefinition.METHOD) &&
//                    pointcut.isThreadSafe()) {
//                MethodPattern pattern =
//                        (MethodPattern)pointcut.getRegexpPatterns();
//                if (pattern.matchMethodName(methodName)) {
//                    return true;
//                }
//            }
//        }
//        return false;
//    }

/**
 * Checks if a throws pointcut is thread-safe.
 *
 * @param className the name of the class
 * @param methodName the name of the method
 * @return boolean
 */
//    public boolean isThrowsPointcutThreadSafe(final String className,
//                                              final String methodName) {
//        if (className == null) throw new IllegalArgumentException("class name can not be null");
//        if (methodName == null) throw new IllegalArgumentException("method name can not be null");
//        if (!m_aspectMap.containsKey(className)) {
//            return false;
//        }
//        List pointcuts = ((AspectDefinition)m_aspectMap.
//                get(className)).getPointcuts();
//        for (Iterator it = pointcuts.iterator(); it.hasNext();) {
//            PointcutDefinition pointcut = (PointcutDefinition)it.next();
//            if (pointcut.getType().equals(PointcutDefinition.THROWS) &&
//                    pointcut.isThreadSafe()) {
//                MethodPattern pattern =
//                        (MethodPattern)pointcut.getRegexpPatterns();
//                if (pattern.matchMethodName(methodName)) {
//                    return true;
//                }
//            }
//        }
//        return false;
//    }

/**
 * Checks if a throws pointcut is thread-safe.
 *
 * @param className the name of the class
 * @param methodName the name of the method
 * @return boolean
 */
//    public boolean isThrowsPointcutThreadSafe(final String className,
//                                              final String methodName,
//                                              final String exceptionName) {
//        if (className == null) throw new IllegalArgumentException("class name can not be null");
//        if (methodName == null) throw new IllegalArgumentException("method name can not be null");
//        if (exceptionName == null) throw new IllegalArgumentException("exception name can not be null");
//        if (!m_aspectMap.containsKey(className)) {
//            return false;
//        }
//        List pointcuts = ((AspectDefinition)m_aspectMap.
//                get(className)).getPointcuts();
//        for (Iterator it = pointcuts.iterator(); it.hasNext();) {
//            PointcutDefinition pointcut = (PointcutDefinition)it.next();
//            if (pointcut.getType().equals(PointcutDefinition.THROWS) &&
//                    pointcut.isThreadSafe()) {
//                StringTokenizer tokenizer = new StringTokenizer(
//                        pointcut.getPattern(),
//                        THROWS_DELIMITER);
//                Pattern methodPattern = new Pattern(tokenizer.nextToken());
//                Pattern exceptionPattern = new Pattern(tokenizer.nextToken());
//                if (methodPattern.contains(methodName) &&
//                        exceptionPattern.contains(exceptionName)) {
//                    return true;
//                }
//            }
//        }
//        return false;
//    }

