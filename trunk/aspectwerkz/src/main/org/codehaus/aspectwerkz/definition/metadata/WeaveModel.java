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
package org.codehaus.aspectwerkz.definition.metadata;

import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.NoSuchElementException;
import java.io.Serializable;
import java.io.ObjectInputStream;
import java.io.FileInputStream;
import java.io.File;
import java.io.IOException;

import gnu.trove.THashMap;

import org.codehaus.aspectwerkz.definition.AspectWerkzDefinition;
import org.codehaus.aspectwerkz.definition.PointcutDefinition;
import org.codehaus.aspectwerkz.definition.AdviceStackDefinition;
import org.codehaus.aspectwerkz.definition.AspectDefinition;
import org.codehaus.aspectwerkz.definition.AdviceDefinition;
import org.codehaus.aspectwerkz.definition.IntroductionDefinition;
import org.codehaus.aspectwerkz.definition.regexp.MethodPattern;
import org.codehaus.aspectwerkz.definition.regexp.FieldPattern;
import org.codehaus.aspectwerkz.definition.regexp.ClassPattern;
import org.codehaus.aspectwerkz.definition.regexp.Pattern;
import org.codehaus.aspectwerkz.definition.metadata.MetaDataCompiler;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.exception.DefinitionException;
import org.codehaus.aspectwerkz.persistence.DirtyFieldCheckAdvice;

/**
 * Implements the weaving model for the system.
 * The weave model is an abstract object representation of the how the
 * application will be transformed.
 *
 * @author <a href="mailto:jboner@acm.org">Jonas Bonér</a>
 * @version $Id: WeaveModel.java,v 1.1.1.1 2003-05-11 15:14:13 jboner Exp $
 */
public class WeaveModel implements Serializable {

    /**
     * The name of the introduction definition tag.
     */
    public static final String ATTRIBUTE_INTRODUCTION_DEF = "introduction-def";

    /**
     * The name of the advice definition tag.
     */
    public static final String ATTRIBUTE_ADVICE_DEF = "advice-def";

    /**
     * The name of the advice param tag.
     */
    public static final String ATTRIBUTE_ADVICE_PARAM = "advice-param";

    /**
     * The name of the introduction attributes tag.
     */
    public static final String ATTRIBUTE_INTRODUCTION = "introduction";

    /**
     * The name of the method attributes tag.
     */
    public static final String ATTRIBUTE_ADVICE_METHOD = "advice:method";

    /**
     * The name of the set field attributes tag.
     */
    public static final String ATTRIBUTE_ADVICE_SET_FIELD = "advice:setfield";

    /**
     * The name of the get field attributes tag.
     */
    public static final String ATTRIBUTE_ADVICE_GET_FIELD = "advice:getfield";

    /**
     * The name of the throws attributes tag.
     */
    public static final String ATTRIBUTE_ADVICE_THROWS = "advice:throws";

    /**
     * The name of the caller side attributes tag.
     */
    public static final String ATTRIBUTE_ADVICE_CALLER_SIDE = "advice:callerside";

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = -2072601774035191615L;;

    /**
     * The path to the meta-data dir.
     */
    public static String META_DATA_DIR =
            System.getProperty("aspectwerkz.metadata.dir", null);

    /**
     * The timestamp, holding the last time that the weave model was read.
     */
    private static File s_timestamp =
            new File(META_DATA_DIR + File.separator + "model_timestamp");

    /**
     * Holds the weave model.
     */
    private static WeaveModel s_weaveModel;

    /**
     * A map with all the advisable classes mapped to thier meta-data.
     */
    private final Map m_model = new THashMap();

    /**
     * The AspectWerkz definition.
     */
    private final AspectWerkzDefinition m_definition;

    /**
     * A map with all the call side pointcut definitions for
     * caller side class.
     */
    private final Map m_callerSideDefinitions = new THashMap();

    /**
     * Loads the current weave model from disk.
     * Only loads from the disk if the timestamp for the latest parsing is
     * older than the timestamp for the weave model.
     *
     * @return the weave model
     */
    public static WeaveModel loadModel() {
        if (META_DATA_DIR == null) {
            return createModel();
        }
        else {
            return loadModelFromDisk();
        }
    }

    /**
     * Creates a new weave model in memory, is not written to disk.
     * Only creates a new model if the XML definition has changed.
     *
     * @return the weave model
     */
    public static WeaveModel createModel() {
        boolean isDirty = false;

        final AspectWerkzDefinition definition =
                AspectWerkzDefinition.
                getDefinition(isDirty);

        if (isDirty || s_weaveModel == null) {
            s_weaveModel = new WeaveModel(definition);

            weaveXmlDefinition(definition, s_weaveModel);

            addMetaDataToAdvices(definition, s_weaveModel);
            addMetaDataToIntroductions(definition, s_weaveModel);
        }
        return s_weaveModel;
    }

    /**
     * Loads an existing weave model from disk.
     * Only loads a new model from disk if it has changed.
     *
     * @return the weave model
     */
    public static WeaveModel loadModelFromDisk() {
        if (!new File(META_DATA_DIR).exists()) throw new RuntimeException(META_DATA_DIR + " meta-data directory does not exist. Create a meta-data dir and specify it with the -Daspectwerkz.metadata.dir=... option (or remove the option completely)");

        final StringBuffer filename = new StringBuffer();
        filename.append(META_DATA_DIR);
        filename.append(File.separator);
        filename.append(MetaDataCompiler.WEAVE_MODEL);
        filename.append(MetaDataCompiler.META_DATA_FILE_SUFFIX);

        // get the timestamp of the weave model file
        final long weaveModelTimestamp =
                new File(filename.toString()).lastModified();

        if (weaveModelTimestamp == 0) {
            return createModel(); // no weave model; fall back on creating one in memory
        }

        // weave model is not updated; don't read, return old version
        if (weaveModelTimestamp < getTimestamp() && s_weaveModel != null) {
            return s_weaveModel;
        }

        // read weave model from disk
        try {
            File file = new File(filename.toString());
            ObjectInputStream in =
                    new ObjectInputStream(new FileInputStream(file));

            s_weaveModel = (WeaveModel)in.readObject();

            in.close();
            setTimestamp();

            return s_weaveModel;
        }
        catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Parses the xml definition and weaves in the advices depending this.
     *
     * @param definition the definition
     * @param allClassNames the classes parsed
     * @param qdoxParser the QDox parser
     * @param weaveModel the weaveModel
     */
    public static void weaveXmlDefinition(
            final AspectWerkzDefinition definition,
            final WeaveModel weaveModel) {

        final List aspects = definition.getAspectDefinitions();

        // loop over all aspect definitions
        for (Iterator it = aspects.iterator(); it.hasNext();) {

            final AspectDefinition aspectDefinition = (AspectDefinition)it.next();

            ClassPattern classPattern =
                    Pattern.compileClassPattern(aspectDefinition.getPattern());

            weaveModel.createClassMetaData(classPattern);
            final WeaveModel.ClassMetaData classMetaData =
                    weaveModel.getClassMetaData(
                            classPattern);

            classMetaData.addIntroductions(
                    aspectDefinition.getIntroductions());

            // add the pointcuts
            for (Iterator it3 = aspectDefinition.getPointcuts().iterator(); it3.hasNext();) {
                final PointcutDefinition pointcutDefinition =
                        (PointcutDefinition)it3.next();

                if (pointcutDefinition.getType().
                        equalsIgnoreCase(PointcutDefinition.METHOD)) {
                    for (Iterator it4 = pointcutDefinition.getPatterns().iterator(); it4.hasNext();) {
                        classMetaData.addMethodPointcut(
                                (String)it4.next(),
                                pointcutDefinition);
                    }
                }
                else if (pointcutDefinition.getType().
                        equalsIgnoreCase(PointcutDefinition.SET_FIELD)) {
                    for (Iterator it4 = pointcutDefinition.getPatterns().iterator(); it4.hasNext();) {
                        classMetaData.addSetFieldPointcut(
                                (String)it4.next(),
                                pointcutDefinition);
                    }
                }
                else if (pointcutDefinition.getType().
                        equalsIgnoreCase(PointcutDefinition.GET_FIELD)) {
                    for (Iterator it4 = pointcutDefinition.getPatterns().iterator(); it4.hasNext();) {
                        classMetaData.addGetFieldPointcut(
                                (String)it4.next(),
                                pointcutDefinition);
                    }
                }
                else if (pointcutDefinition.getType().
                        equalsIgnoreCase(PointcutDefinition.THROWS)) {
                    for (Iterator it4 = pointcutDefinition.getPatterns().iterator(); it4.hasNext();) {
                        classMetaData.addThrowsPointcut(
                                (String)it4.next(),
                                pointcutDefinition);
                    }
                }
                else if (pointcutDefinition.getType().
                        equalsIgnoreCase(PointcutDefinition.CALLER_SIDE)) {
                    for (Iterator it4 = pointcutDefinition.getPatterns().iterator(); it4.hasNext();) {
                        weaveModel.addCallSidePointcut(
                                (String)it4.next(),
                                pointcutDefinition);
                    }
                }
                else {
                    // skip
                }
            }
        }
    }

    /**
     * Enhances the advices with meta-data.
     *
     * @param definition the definition
     * @param weaveModel the weave model
     * @param dirtyFieldPointcutDef the dirty field checker pointcut
     */
    public static void addMetaDataToAdvices(
            final AspectWerkzDefinition definition,
            final WeaveModel weaveModel) {

        // create the dirty field check advice pointcut
        PointcutDefinition dirtyFieldPointcutDef = new PointcutDefinition();
        dirtyFieldPointcutDef.addPattern(DirtyFieldCheckAdvice.PATTERN);
        dirtyFieldPointcutDef.setType(PointcutDefinition.SET_FIELD);
        dirtyFieldPointcutDef.addAdvice(DirtyFieldCheckAdvice.NAME);

        // create aspects for the advices,
        // to enable persistence and meta-data management
        final List adviceDefinitions = definition.getAdviceDefinitions();
        for (Iterator it = adviceDefinitions.iterator(); it.hasNext();) {

            final AdviceDefinition adviceDefinition = (AdviceDefinition)it.next();
            final String className = adviceDefinition.getClassName();

            weaveModel.createClassMetaData(className);
            final WeaveModel.ClassMetaData classMetaData =
                    weaveModel.getClassMetaDataExactMatch(className);

            if (adviceDefinition.isPersistent()) {
                classMetaData.addSetFieldPointcut(
                        DirtyFieldCheckAdvice.PATTERN,
                        dirtyFieldPointcutDef);
            }
        }
    }

    /**
     * Enhances the introductions with meta-data.
     *
     * @param definition the definition
     * @param weaveModel the weave model
     * @param dirtyFieldPointcutDef the dirty field checker pointcut
     */
    public static void addMetaDataToIntroductions(
            final AspectWerkzDefinition definition,
            final WeaveModel weaveModel) {

        // create the dirty field check advice pointcut
        PointcutDefinition dirtyFieldPointcutDef = new PointcutDefinition();
        dirtyFieldPointcutDef.addPattern(DirtyFieldCheckAdvice.PATTERN);
        dirtyFieldPointcutDef.setType(PointcutDefinition.SET_FIELD);
        dirtyFieldPointcutDef.addAdvice(DirtyFieldCheckAdvice.NAME);

        // create aspects for the introduced implementations,
        // to enable persistence and meta-data management
        final List introductionDefinitions =
                definition.getIntroductionDefinitions();
        for (Iterator it = introductionDefinitions.iterator(); it.hasNext();) {

            final IntroductionDefinition introductionDefinition =
                    (IntroductionDefinition)it.next();
            final String className = introductionDefinition.getImplementation();

            if (className == null) continue; // interface introduction

            weaveModel.createClassMetaData(className);
            final WeaveModel.ClassMetaData classMetaData =
                    weaveModel.getClassMetaDataExactMatch(className);

            if (introductionDefinition.isPersistent()) {
                classMetaData.addSetFieldPointcut(
                        DirtyFieldCheckAdvice.PATTERN,
                        dirtyFieldPointcutDef);
            }
        }
    }

    /**
     * Creates a new weave model. Sets the aspectwerkz definition.
     *
     * @param definition the definition
     */
    public WeaveModel(final AspectWerkzDefinition definition) {
        m_definition = definition;
    }

    /**
     * Returns the meta-data container for the class specified, if not found
     * it creates a new one.
     *
     * @param classPattern the pattern for the class
     * @return the meta-data
     */
    public void createClassMetaData(final ClassPattern classPattern) {
        if (classPattern == null) throw new IllegalArgumentException("class pattern can not be null");
        if (!m_model.containsKey(classPattern)) {
            m_model.put(classPattern, new ClassMetaData(classPattern.getPattern()));
        }
    }

    /**
     * Creates a new the meta-data container for the class specified.
     *
     * @param className the name of the class
     * @return the meta-data
     */
    public void createClassMetaData(final String className) {
        if (className == null) throw new IllegalArgumentException("class name can not be null");
        ClassPattern pattern = Pattern.compileClassPattern(className);
        if (!m_model.containsKey(pattern)) {
            m_model.put(pattern, new ClassMetaData(className));
        }
    }

    /**
     * Returns the meta-data container for the class specified, if not found
     * it creates a new one.
     *
     * @param classPattern the pattern for the class
     * @return the meta-data
     */
    public ClassMetaData getClassMetaData(final ClassPattern classPattern) {
        if (classPattern == null) throw new IllegalArgumentException("class pattern can not be null");
        if (!m_model.containsKey(classPattern)) {
            throw new RuntimeException("no weave model for " + classPattern.getPattern());
        }
        return (ClassMetaData)m_model.get(classPattern);
    }

    /**
     * Returns the meta-data container for the class specified.
     *
     * @param className the name of the class
     * @return the meta-data
     */
    public ClassMetaData getClassMetaData(final String className) {
        if (className == null) throw new IllegalArgumentException("class name can not be null");
        for (Iterator it = m_model.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry)it.next();
            ClassPattern classPattern = (ClassPattern)entry.getKey();
            if (classPattern.matches(className)) {
                return (ClassMetaData)entry.getValue();
            }
        }
        throw new RuntimeException("no weave model for " + className);
    }

    /**
     * Returns the meta-data container for the class specified.
     * Does not find by pattern, needs an exact match.
     *
     * @param className the name of the class
     * @return the meta-data
     */
    public ClassMetaData getClassMetaDataExactMatch(final String className) {
        if (className == null) throw new IllegalArgumentException("class name can not be null");
        for (Iterator it = m_model.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry)it.next();
            ClassPattern classPattern = (ClassPattern)entry.getKey();
            if (classPattern.getPattern().equals(className)) {
                return (ClassMetaData)entry.getValue();
            }
        }
        throw new RuntimeException("no weave model for " + className);
    }

    /**
     * Returns the class meta-data map.
     *
     * @return the class meta-data map
     */
    public Map getClassMetaDataMap() {
        return m_model;
    }

    /**
     * Returns the patterns for the aspects.
     *
     * @return the patterns for the aspects
     */
    public List getAspectPatterns() {
        List aspectPatterns = new ArrayList(m_model.keySet().size());
        for (Iterator it = m_model.keySet().iterator(); it.hasNext();) {
            aspectPatterns.add(it.next());
        }
        return aspectPatterns;
    }

    /**
     * Returns a list with the aspect definitions.
     *
     * @return the aspect definitions
     */
    public List getAspectDefinitions() {
        return m_definition.getAspectDefinitions();
    }

    /**
     * Returns a list with the introduction definitions.
     *
     * @return the introduction definitions
     */
    public List getIntroductionDefinitions() {
        return m_definition.getIntroductionDefinitions();
    }

    /**
     * Returns a list with the advice definitions.
     *
     * @return the advice definitions
     */
    public List getAdviceDefinitions() {
        return m_definition.getAdviceDefinitions();
    }

    /**
     * Finds an advice stack definition by its name.
     *
     * @param adviceStackName the advice stack name
     * @return the definition
     */
    public AdviceStackDefinition getAdviceStackDefinition(
            final String adviceStackName) {
        return m_definition.getAdviceStackDefinition(adviceStackName);
    }

    /**
     * Returns the names of the introductions.
     *
     * @param classPattern the pattern for the class
     * @return the names
     */
    public List getIntroductionNames(final ClassPattern aspectPattern) {
        if (aspectPattern == null) throw new IllegalArgumentException("aspect pattern can not be null");
        if (!m_model.containsKey(aspectPattern)) {
            return new ArrayList();
        }
        return ((ClassMetaData)m_model.get(aspectPattern)).getIntroductions();
    }

    /**
     * Returns the names of the introductions.
     *
     * @param className the name of the class
     * @return the names
     */
    public List getIntroductionNames(final String className) {
        if (className == null) throw new IllegalArgumentException("class name can not be null");
        for (Iterator it = m_model.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry)it.next();
            ClassPattern classPattern = (ClassPattern)entry.getKey();

            // find a meta-data container that matches the classname
            // as well as has a non-empty introduction list
            if (classPattern.matches(className) &&
                    !((ClassMetaData)entry.getValue()).
                    getIntroductions().isEmpty()) {
                return ((ClassMetaData)entry.getValue()).getIntroductions();
            }
        }
        // if not found
        return new ArrayList();
    }

    /**
     * Returns the name of the interface for an introduction.
     *
     * @param introductionName the name of the introduction
     * @return the name of the interface
     */
    public String getIntroductionInterfaceName(final String introductionName) {
        if (introductionName == null) throw new IllegalArgumentException("introduction name can not be null");
        return m_definition.getIntroductionInterfaceName(introductionName);
    }

    /**
     * Returns the index for a specific introduction.
     *
     * @param introductionName the name of the introduction
     * @return the index
     */
    public int getIntroductionIndexFor(final String introductionName) {
        if (introductionName == null) throw new IllegalArgumentException("introduction name can not be null");
        return m_definition.getIntroductionIndexFor(introductionName);
    }

    /**
     * Returns the methods meta-data for a specific introduction.
     *
     * @param introductionName the name of the introduction
     * @return a list with the methods meta-data
     */
    public List getIntroductionMethodsMetaData(final String introductionName) {
        if (introductionName == null) throw new IllegalArgumentException("introduction name can not be null");
        return m_definition.getIntroductionMethodsMetaData(introductionName);
    }

    /**
     * Returns the callerSide pointcuts.
     *
     * @return the callerSide pointcuts
     */
    public Map getCallerSidePointcuts() {
        return m_callerSideDefinitions;
    }

    /**
     * Adds a new introduction definition.
     *
     * @param introductionDefinition the new introduction definition
     */
    public void addIntroductionDefinition(
            final IntroductionDefinition introductionDefinition) {
        m_definition.addIntroduction(introductionDefinition);
    }

    /**
     * Adds a new advice definition.
     *
     * @param adviceDefinition the new advice definition
     */
    public void addAdviceDefinition(final AdviceDefinition adviceDefinition) {
        m_definition.addAdvice(adviceDefinition);
    }

    /**
     * Adds a new pointcut definition to the callside pointcut.
     *
     * @param methodPattern the method pattern
     * @param pointcut the pointcut definition
     */
    public void addCallSidePointcut(final String methodPattern,
                                    final PointcutDefinition pointcut) {
        if (!m_callerSideDefinitions.containsKey(methodPattern)) {
            m_callerSideDefinitions.put(methodPattern, new ArrayList());
        }
        ((List)m_callerSideDefinitions.get(methodPattern)).add(pointcut);
    }

    /**
     * Checks if a class has an <tt>Aspect</tt>.
     *
     * @param className the name or the class
     * @return boolean
     */
    public boolean hasAspect(final String className) {
        if (className == null) throw new IllegalArgumentException("class name can not be null");
        for (Iterator it = m_model.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry)it.next();
            ClassPattern classPattern = (ClassPattern)entry.getKey();
            if (classPattern.matches(className)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a class has an <tt>Introduction</tt>.
     *
     * @param className the name or the class
     * @return boolean
     */
    public boolean hasIntroductions(final String className) {
        if (className == null) throw new IllegalArgumentException("class name can not be null");
        for (Iterator it = m_model.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry)it.next();
            ClassPattern classPattern = (ClassPattern)entry.getKey();
            if (classPattern.matches(className) &&
                    !((ClassMetaData)entry.getValue()).
                    getIntroductions().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a method has a <tt>MethodPointcut</tt>.
     *
     * @param className the name or the class
     * @param methodMetaData the method meta-data
     * @return boolean
     */
    public boolean hasMethodPointcut(final String className,
                                     final MethodMetaData methodMetaData) {
        if (className == null) throw new IllegalArgumentException("class name can not be null");
        if (methodMetaData == null) throw new IllegalArgumentException("method meta-data can not be null");
        for (Iterator it = m_model.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry)it.next();
            ClassPattern classPattern = (ClassPattern)entry.getKey();
            if (classPattern.matches(className) &&
                    ((ClassMetaData)entry.getValue()).
                    hasMethodPointcut(methodMetaData)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a class and method has a <tt>ThrowsPointcut</tt>.
     *
     * @param className the name or the class
     * @param methodMetaData the name or the method
     * @return boolean
     */
    public boolean hasThrowsPointcut(final String className,
                                     final MethodMetaData methodMetaData) {
        if (className == null) throw new IllegalArgumentException("class name can not be null");
        if (methodMetaData == null) throw new IllegalArgumentException("method meta-data can not be null");
        for (Iterator it = m_model.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry)it.next();
            ClassPattern classPattern = (ClassPattern)entry.getKey();
            if (classPattern.matches(className) &&
                    ((ClassMetaData)entry.getValue()).
                    hasThrowsPointcut(methodMetaData)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a class and field has a <tt>GetFieldPointcut</tt>.
     *
     * @param className the name or the class
     * @param fieldMetaData the name or the field
     * @return boolean
     */
    public boolean hasGetFieldPointcut(final String className,
                                       final FieldMetaData fieldMetaData) {
        if (className == null) throw new IllegalArgumentException("class name can not be null");
        if (fieldMetaData == null) throw new IllegalArgumentException("field meta-data can not be null");
        for (Iterator it = m_model.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry)it.next();
            ClassPattern classPattern = (ClassPattern)entry.getKey();
            if (classPattern.matches(className) &&
                    ((ClassMetaData)entry.getValue()).
                    hasGetFieldPointcut(fieldMetaData)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a class and field has a <tt>SetFieldPointcut</tt>.
     *
     * @param className the name or the class
     * @param fieldMetaData the name or the field
     * @return boolean
     */
    public boolean hasSetFieldPointcut(final String className,
                                       final FieldMetaData fieldMetaData) {
        if (className == null) throw new IllegalArgumentException("class name can not be null");
        if (fieldMetaData == null) throw new IllegalArgumentException("field meta-data can not be null");
        for (Iterator it = m_model.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry)it.next();
            ClassPattern classPattern = (ClassPattern)entry.getKey();
            if (classPattern.matches(className) &&
                    ((ClassMetaData)entry.getValue()).
                    hasSetFieldPointcut(fieldMetaData)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a class should care about advising caller side method invocations.
     *
     * @param className the name or the class
     * @return boolean
     */
    public boolean hasCallerSidePointcut(final String className) {
        if (className == null) throw new IllegalArgumentException("class name can not be null");

        for (Iterator it = m_callerSideDefinitions.values().iterator(); it.hasNext();) {
            for (Iterator it2 = ((List)it.next()).iterator(); it2.hasNext();) {
                PointcutDefinition def = (PointcutDefinition)it2.next();

                ClassPattern classPattern =
                        Pattern.compileClassPattern(
                                def.getCallerSidePattern());

                if (classPattern.matches(className)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a method is a defined as a caller side method.
     *
     * @param className the name or the class
     * @param methodMetaData the name or the method
     * @return boolean
     */
    public boolean isCallerSideMethod(final String className,
                                      final MethodMetaData methodMetaData) {
        if (className == null) throw new IllegalArgumentException("class name can not be null");
        if (methodMetaData == null) throw new IllegalArgumentException("method meta-data can not be null");

        for (Iterator it = m_callerSideDefinitions.values().iterator(); it.hasNext();) {
            for (Iterator it2 = ((List)it.next()).iterator(); it2.hasNext();) {
                PointcutDefinition def = (PointcutDefinition)it2.next();

                for (Iterator it3 = def.getPatterns().iterator(); it3.hasNext();) {
                    String pattern = (String)it3.next();
                    try {
                        final StringTokenizer tokenizer = new StringTokenizer(
                                pattern,
                                AspectWerkzDefinition.CALLER_SIDE_DELIMITER);
                        String classNamePattern = tokenizer.nextToken();
                        String methodNamePattern = tokenizer.nextToken();

                        ClassPattern classPattern =
                                Pattern.compileClassPattern(classNamePattern);
                        MethodPattern methodPattern =
                                Pattern.compileMethodPattern(methodNamePattern);
                        if (classPattern.matches(className) &&
                                methodPattern.matches(methodMetaData)) {
                            return true;
                        }
                    }
                    catch (NoSuchElementException e) {
                        throw new DefinitionException("caller side pattern is not well-formed: " + pattern);
                    }

                }
            }
        }
        return false;
    }

    /**
     * Sets the timestamp for the latest version of the weave model.
     */
    private static void setTimestamp() {
        final long newModifiedTime = System.currentTimeMillis();
        boolean success = s_timestamp.setLastModified(newModifiedTime);
        if (!success) {
        }
    }

    /**
     * Returns the timestamp for the last version of the weave model.
     *
     * @return the timestamp
     */
    private static long getTimestamp() {
        final long modifiedTime = s_timestamp.lastModified();
        if (modifiedTime == 0L) {
            // no timestamp
            try {
                s_timestamp.createNewFile();
            }
            catch (IOException e) {
                throw new RuntimeException("could not create timestamp file: " + s_timestamp.getAbsolutePath());
            }
        }
        return modifiedTime;
    }

    /**
     * Holds the weave meta-data for each class.
     *
     * @author <a href="mailto:jboner@acm.org">Jonas Bonér</a>
     * @version $Id: WeaveModel.java,v 1.1.1.1 2003-05-11 15:14:13 jboner Exp $
     */
    public static class ClassMetaData implements Serializable {

        /**
         * Serial version UID.
         */
        private static final long serialVersionUID = -2867205712192699416L;

        /**
         * The name of the class.
         */
        private final String m_className;

        /**
         * The name of the introductions.
         */
        private final List m_introductions = new ArrayList();

        /**
         * A map with the method name mapped to the pointcut definitions
         * for this method.
         */
        private final Map m_methodDefinitions = new THashMap();

        /**
         * A map with the field name mapped to the setField pointcut
         * definitions for this field.
         */
        private final Map m_setFieldDefinitions = new THashMap();

        /**
         * A map with the field name mapped to the getField pointcut
         * definitions for this field.
         */
        private final Map m_getFieldDefinitions = new THashMap();

        /**
         * A map with the method name mapped to the throws pointcut
         * definitions for this method.
         */
        private final Map m_throwsDefinitions = new THashMap();

        /**
         * Creates a new instance.
         *
         * @param className
         */
        public ClassMetaData(final String className) {
            m_className = className;
        }

        /**
         * Adds a list with introductions.
         *
         * @param introductions the introductions
         */
        public void addIntroductions(final List introductions) {
            m_introductions.addAll(introductions);
        }

        /**
         * Adds a new pointcut definition to the method pointcut.
         *
         * @param methodPattern the method pattern
         * @param pointcut the pointcut definition
         */
        public void addMethodPointcut(final String methodPattern,
                                      final PointcutDefinition pointcut) {
            if (!m_methodDefinitions.containsKey(methodPattern)) {
                m_methodDefinitions.put(methodPattern, new ArrayList());
            }
            ((List)m_methodDefinitions.get(methodPattern)).add(pointcut);
        }

        /**
         * Adds a new pointcut definition to the setfield pointcut.
         *
         * @param fieldPattern the field pattern
         * @param pointcut the pointcut definition
         */
        public void addSetFieldPointcut(final String fieldPattern,
                                        final PointcutDefinition pointcut) {
            if (!m_setFieldDefinitions.containsKey(fieldPattern)) {
                m_setFieldDefinitions.put(fieldPattern, new ArrayList());
            }
            ((List)m_setFieldDefinitions.get(fieldPattern)).add(pointcut);
        }

        /**
         * Adds a new pointcut definition to the getfield pointcut.
         *
         * @param fieldPattern the field pattern
         * @param pointcut the pointcut definition
         */
        public void addGetFieldPointcut(final String fieldPattern,
                                        final PointcutDefinition pointcut) {
            if (!m_getFieldDefinitions.containsKey(fieldPattern)) {
                m_getFieldDefinitions.put(fieldPattern, new ArrayList());
            }
            ((List)m_getFieldDefinitions.get(fieldPattern)).add(pointcut);
        }

        /**
         * Adds a new pointcut definition to the throws pointcut.
         *
         * @param methodPattern the method pattern
         * @param pointcut the pointcut definition
         */
        public void addThrowsPointcut(final String methodPattern,
                                      final PointcutDefinition pointcut) {
            if (!m_throwsDefinitions.containsKey(methodPattern)) {
                m_throwsDefinitions.put(methodPattern, new ArrayList());
            }
            ((List)m_throwsDefinitions.get(methodPattern)).add(pointcut);
        }

        /**
         * Returns the class name.
         *
         * @return the class name
         */
        public String getClassName() {
            return m_className;
        }

        /**
         * Returns the introductions.
         *
         * @return the introductions
         */
        public List getIntroductions() {
            return m_introductions;
        }

        /**
         * Returns the methods pointcut map.
         *
         * @return the methods pointcut map
         */
        public Map getMethodPointcuts() {
            return m_methodDefinitions;
        }

        /**
         * Returns the set fields pointcut map.
         *
         * @return the set fields pointcut map
         */
        public Map getSetFieldPointcuts() {
            return m_setFieldDefinitions;
        }

        /**
         * Returns the get fields pointcut map.
         *
         * @return the get fields pointcut map
         */
        public Map getGetFieldPointcuts() {
            return m_getFieldDefinitions;
        }

        /**
         * Returns the throws pointcut map.
         *
         * @return the throws pointcut map
         */
        public Map getThrowsPointcuts() {
            return m_throwsDefinitions;
        }

        /**
         * Checks if the method has a method pointcut.
         *
         * @param methodMetaData the method meta-data
         * @return boolean
         */
        public boolean hasMethodPointcut(final MethodMetaData methodMetaData) {
            for (Iterator it = m_methodDefinitions.values().iterator(); it.hasNext();) {
                for (Iterator it2 = ((List)it.next()).iterator(); it2.hasNext();) {
                    PointcutDefinition def = (PointcutDefinition)it2.next();
                    for (Iterator it3 = def.getRegexpPatterns().iterator(); it3.hasNext();) {
                        MethodPattern methodPattern = (MethodPattern)it3.next();
                        if (methodPattern.matches(methodMetaData)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        /**
         * Checks if the field has a setField pointcut.
         *
         * @param fieldMetaData the field meta-data
         * @return boolean
         */
        public boolean hasSetFieldPointcut(final FieldMetaData fieldMetaData) {
            for (Iterator it = m_setFieldDefinitions.values().iterator(); it.hasNext();) {
                for (Iterator it2 = ((List)it.next()).iterator(); it2.hasNext();) {
                    PointcutDefinition def = (PointcutDefinition)it2.next();
                    for (Iterator it3 = def.getRegexpPatterns().iterator(); it3.hasNext();) {
                        FieldPattern fieldPattern = (FieldPattern)it3.next();
                        if (fieldPattern.matches(fieldMetaData)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        /**
         * Checks if the field has a getField pointcut.
         *
         * @param fieldMetaData the method meta-data
         * @return boolean
         */
        public boolean hasGetFieldPointcut(final FieldMetaData fieldMetaData) {
            for (Iterator it = m_getFieldDefinitions.values().iterator(); it.hasNext();) {
                for (Iterator it2 = ((List)it.next()).iterator(); it2.hasNext();) {
                    PointcutDefinition def = (PointcutDefinition)it2.next();
                    for (Iterator it3 = def.getRegexpPatterns().iterator(); it3.hasNext();) {
                        FieldPattern fieldPattern = (FieldPattern)it3.next();
                        if (fieldPattern.matches(fieldMetaData)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        /**
         * Checks if the method has a throws pointcut.
         *
         * @param methodMetaData the method meta-data
         * @return boolean
         */
        public boolean hasThrowsPointcut(final MethodMetaData methodMetaData) {
            for (Iterator it = m_throwsDefinitions.values().iterator(); it.hasNext();) {
                for (Iterator it2 = ((List)it.next()).iterator(); it2.hasNext();) {
                    PointcutDefinition def = (PointcutDefinition)it2.next();
                    for (Iterator it3 = def.getRegexpPatterns().iterator(); it3.hasNext();) {
                        MethodPattern methodPattern = (MethodPattern)it3.next();
                        if (methodPattern.matches(methodMetaData)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }
    }
}
