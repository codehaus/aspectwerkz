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
package org.codehaus.aspectwerkz.metadata;

import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.JavaParameter;
import com.thoughtworks.qdox.model.JavaField;
import com.thoughtworks.qdox.model.DocletTag;
import com.thoughtworks.qdox.model.JavaClass;

import org.codehaus.aspectwerkz.metadata.MetaDataCompiler;
import org.codehaus.aspectwerkz.metadata.QDoxParser;
import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.definition.AspectWerkzDefinition;
import org.codehaus.aspectwerkz.definition.PointcutDefinition;
import org.codehaus.aspectwerkz.definition.AdviceDefinition;
import org.codehaus.aspectwerkz.definition.IntroductionDefinition;
import org.codehaus.aspectwerkz.definition.AttributeTag;
import org.codehaus.aspectwerkz.definition.AspectDefinition;
import org.codehaus.aspectwerkz.definition.IntroductionWeavingRule;
import org.codehaus.aspectwerkz.definition.AdviceWeavingRule;
import org.codehaus.aspectwerkz.exception.DefinitionException;
import org.codehaus.aspectwerkz.advice.CFlowAdvice;
import org.codehaus.aspectwerkz.AspectWerkz;

/**
 * Parses a given source tree and compiles meta-data.
 * The meta-data compilation is based on the xml definition definition file
 * as well as "runtime attributes" set as JavaDoc tags throughout the code.
 * <p/>
 * Can be called from the command line.
 *
 * @todo only compile if we have a change in the source file
 * @todo problem with inner classes
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: SourceFileMetaDataCompiler.java,v 1.6 2003-06-30 15:55:25 jboner Exp $
 */
public class SourceFileMetaDataCompiler extends MetaDataCompiler {

    public static final String METHOD_POINTCUT_NAME = "___method_pointcut_";
    public static final String SETFIELD_POINTCUT_NAME = "___setfield_pointcut_";
    public static final String GETFIELD_POINTCUT_NAME = "___getfield_pointcut_";
    public static final String THROWS_POINTCUT_NAME = "___throws_pointcut_";
    public static final String CALLERSIDE_POINTCUT_NAME = "___callerside_pointcut_";
    public static final String CFLOW_POINTCUT_NAME = "___cflow_pointcut_";

    /**
     * Parses a given source tree and creates and stores meta-data for
     * all methods for all the introduced <code>Introduction</code>s as well
     * as parses the runtime attributes defined in the code.
     *
     * @param definitionFile the definition file to use
     * @param sourcePath the path to the sources
     * @param metaDataDir the path to the dir where to store the meta-data
     */
    public static void compile(final String definitionFile,
                               final String sourcePath,
                               final String metaDataDir) {
        compile(definitionFile, sourcePath, metaDataDir, null);
    }

    /**
     * Parses a given source tree and creates and stores meta-data for
     * all methods for all the introduced <code>Introduction</code>s as well
     * as parses the runtime attributes defined in the code.
     *
     * @param definitionFile the definition file to use
     * @param sourcePath the path to the sources
     * @param metaDataDir the path to the dir where to store the meta-data
     * @param uuid the user-defined UUID for the weave model
     */
    public static void compile(final String definitionFile,
                               final String sourcePath,
                               final String metaDataDir,
                               final String uuid) {
        if (definitionFile == null) throw new IllegalArgumentException("definition file can not be null");
        if (sourcePath == null) throw new IllegalArgumentException("source path can not be null");
        if (metaDataDir == null) throw new IllegalArgumentException("meta-data dir can not be null");

        createMetaDataDir(metaDataDir);
        final AspectWerkzDefinition definition =
                AspectWerkzDefinition.getDefinition(definitionFile);

        QDoxParser qdoxParser = new QDoxParser(sourcePath);
        parseAttributeDefinitions(definition, qdoxParser.getAllClassesNames(), qdoxParser);

        final WeaveModel weaveModel = weave(uuid, definition);
        compileIntroductionMetaData(weaveModel, qdoxParser);
        saveWeaveModelToFile(metaDataDir, weaveModel);
    }

    /**
     * Parses the attributes and creates definitions for the matching attributes.
     *
     * @param definition the definition
     * @param allClasses the classes parsed
     * @param qdoxParser the QDox parser
     */
    public static void parseAttributeDefinitions(
            final AspectWerkzDefinition definition,
            final String[] allClasses,
            final QDoxParser qdoxParser) {

        // add the cflow advice to the system
        definition.addAdvice(CFlowAdvice.getDefinition());

        // handle the definition attributes
        for (int i = 0; i < allClasses.length; i++) {
            String className = allClasses[i];
            if (!qdoxParser.parse(className)) {
                continue;
            }
            weaveIntroductionDefinitionAttributes(qdoxParser, definition);
            weaveAdviceDefinitionAttributes(qdoxParser, definition);
        }

        // handle the definition references
        for (int i = 0; i < allClasses.length; i++) {
            String className = allClasses[i];
            if (!qdoxParser.parse(className)) {
                continue;
            }
            weaveCFlowPointcutAttributes(definition, className, qdoxParser);
            weaveIntroductionAttributes(definition, className, qdoxParser);
            weaveMethodPointcutAttributes(definition, className, qdoxParser);
            weaveSetFieldPointcutAttributes(definition, className, qdoxParser);
            weaveGetFieldPointcutAttributes(definition, className, qdoxParser);
            weaveThrowsPointcutAttributes(definition, className, qdoxParser);
            weaveCallerSidePointcutAttributes(definition, className, qdoxParser);
        }
    }

    /**
     * Weaves the introduction definition attributes.
     *
     * @param qdoxParser the QDox parser
     * @param definition the definition
     */
    private static void weaveIntroductionDefinitionAttributes(
            final QDoxParser qdoxParser,
            final AspectWerkzDefinition definition) {

        JavaClass javaClass = qdoxParser.getJavaClass();
        DocletTag[] introductionDefTags = javaClass.getTagsByName(AttributeTag.INTRODUCTION_DEF);

        for (int i = 0; i < introductionDefTags.length; i++) {
            if (introductionDefTags[i] == null) {
                continue;
            }

            IntroductionDefinition introDef = new IntroductionDefinition();
            introDef.setName(introductionDefTags[i].getNamedParameter("name"));
            introDef.setInterface(javaClass.getFullyQualifiedName());
            introDef.setImplementation(introductionDefTags[i].getNamedParameter("implementation"));
            introDef.setDeploymentModel(introductionDefTags[i].getNamedParameter("deployment-model"));
            introDef.setIsPersistent(introductionDefTags[i].getNamedParameter("persistent"));
            introDef.setAttribute(introductionDefTags[i].getNamedParameter("attribute"));

            definition.addIntroduction(introDef);
        }
    }

    /**
     * Weaves the advice definition attributes.
     *
     * @param qdoxParser the QDox parser
     * @param definition the definition
     */
    private static void weaveAdviceDefinitionAttributes(
            final QDoxParser qdoxParser,
            final AspectWerkzDefinition definition) {

        JavaClass javaClass = qdoxParser.getJavaClass();
        DocletTag[] adviceDefTags = javaClass.getTagsByName(AttributeTag.ADVICE_DEF);

        for (int i = 0; i < adviceDefTags.length; i++) {
            if (adviceDefTags[i] == null) {
                continue;
            }

            AdviceDefinition adviceDef = new AdviceDefinition();
            adviceDef.setName(adviceDefTags[i].getNamedParameter("name"));
            adviceDef.setAdviceClassName(javaClass.getFullyQualifiedName());
            adviceDef.setDeploymentModel(adviceDefTags[i].getNamedParameter("deployment-model"));
            adviceDef.setIsPersistent(adviceDefTags[i].getNamedParameter("persistent"));
            adviceDef.setAttribute(adviceDefTags[i].getNamedParameter("attribute"));

            weaveAdviceParamAttributes(javaClass, adviceDef);

            definition.addAdvice(adviceDef);
        }
    }

    /**
     * Weaves the advice param attributes.
     *
     * @param qdoxParser the QDox parser
     * @param definition the definition
     */
    private static void weaveAdviceParamAttributes(
            final JavaClass javaClass,
            final AdviceDefinition adviceDefinition) {

        DocletTag[] adviceDefTags = javaClass.getTagsByName(AttributeTag.ADVICE_PARAM);

        for (int i = 0; i < adviceDefTags.length; i++) {
            if (adviceDefTags[i] == null) {
                continue;
            }

            String adviceRef = adviceDefTags[i].getNamedParameter("advice-ref");
            if (adviceRef.equals(adviceDefinition.getName())) {
                adviceDefinition.addParameter(
                        adviceDefTags[i].getNamedParameter("name"),
                        adviceDefTags[i].getNamedParameter("value"));
            }
        }
    }

    /**
     * Weaves the introduction attributes.
     *
     * @param definition the definition
     * @param className the name of the parsed class
     * @param qdoxParser the QDox parser
     */
    private static void weaveIntroductionAttributes(
            final AspectWerkzDefinition definition,
            final String className,
            final QDoxParser qdoxParser) {

        AspectDefinition aspectDefinition =
                definition.getAspectDefinition(AspectWerkzDefinition.SYSTEM_ASPECT);

        JavaClass javaClass = qdoxParser.getJavaClass();
        DocletTag[] introductionTags = javaClass.getTagsByName(AttributeTag.INTRODUCTION);

        IntroductionWeavingRule weavingRule = new IntroductionWeavingRule();
        weavingRule.setClassPattern(className);

        for (int i = 0; i < introductionTags.length; i++) {
            if (introductionTags[i] == null) {
                continue;
            }
            String[] attributes = introductionTags[i].getParameters();
            for (int j = 0; j < attributes.length; j++) {
                final String introductionRef =
                        definition.getIntroductionNameByAttribute(attributes[j]);
                if (introductionRef == null) {
                    continue;
                }
                weavingRule.addIntroductionRef(introductionRef);
            }
            aspectDefinition.addIntroductionWeavingRule(weavingRule);
        }
    }

    /**
     * Weaves the method attributes.
     *
     * @param definition the definition
     * @param className the name of the parsed class
     * @param qdoxParser the QDox parser
     */
    private static void weaveMethodPointcutAttributes(
            final AspectWerkzDefinition definition,
            final String className,
            final QDoxParser qdoxParser) {
        String pointcutName = METHOD_POINTCUT_NAME + className.replaceAll("\\.", "_");

        int counter = 0;
        final JavaMethod[] javaMethods = qdoxParser.getJavaMethods();
        for (int i = 0; i < javaMethods.length; i++) {

            DocletTag[] methodTags = javaMethods[i].getTagsByName(AttributeTag.METHOD);
            for (int j = 0; j < methodTags.length; j++) {
                if (methodTags[j] == null) {
                    continue;
                }

                String cflowRef = methodTags[j].getNamedParameter("cflow");

                String[] attributes = methodTags[j].getParameters();
                for (int k = 0; k < attributes.length; k++) {
                    String attribute = attributes[k];
                    if (attribute.startsWith("cflow=")) {
                        continue;
                    }

                    for (Iterator it2 = definition.getAdviceDefinitions().iterator(); it2.hasNext();) {
                        String expression = pointcutName + counter;

                        // create and add a new pointcut def
                        PointcutDefinition pointcutDef = new PointcutDefinition();
                        pointcutDef.setName(expression);
                        pointcutDef.setClassPattern(className);
                        pointcutDef.setPattern(createMethodPattern(javaMethods[i]));
                        pointcutDef.setType(PointcutDefinition.METHOD);
                        definition.getAspectDefinition(AspectWerkzDefinition.SYSTEM_ASPECT).
                                addPointcut(pointcutDef);

                        String adviceAttribute = ((AdviceDefinition)it2.next()).getAttribute();
                        if (adviceAttribute == null) {
                            continue;
                        }
                        if (adviceAttribute.equals(attribute)) {
                            // get the advice ref
                            String adviceRef = definition.getAdviceNameByAttribute(adviceAttribute);
                            if (adviceRef == null) {
                                continue; // attribute not mapped to an advice
                            }
                            // create and add a new weaving rule def
                            AdviceWeavingRule weavingRule = new AdviceWeavingRule();
                            weavingRule.setExpression(expression);
                            weavingRule.setCFlowExpression(cflowRef);
                            weavingRule.addAdviceRef(adviceRef);
                            definition.getAspectDefinition(AspectWerkzDefinition.SYSTEM_ASPECT).
                                    addAdviceWeavingRule(weavingRule);

                            counter++;
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * Weaves the set field pointcut attributes.
     *
     * @param definition the definition
     * @param className the name of the parsed class
     * @param qdoxParser the QDox parser
     */
    private static void weaveSetFieldPointcutAttributes(
            final AspectWerkzDefinition definition,
            final String className,
            final QDoxParser qdoxParser) {
        String pointcutName = SETFIELD_POINTCUT_NAME + className.replaceAll("\\.", "_");

        int counter = 0;
        final JavaField[] javaFields = qdoxParser.getJavaFields();
        for (int i = 0; i < javaFields.length; i++) {

            DocletTag[] setFieldTags = javaFields[i].getTagsByName(AttributeTag.SET_FIELD);
            for (int j = 0; j < setFieldTags.length; j++) {
                if (setFieldTags[j] == null) {
                    continue;
                }

                String[] setFieldAttributes = setFieldTags[j].getParameters();
                for (int k = 0; k < setFieldAttributes.length; k++) {

                    String attribute = setFieldAttributes[k];
                    for (Iterator it2 = definition.getAdviceDefinitions().iterator(); it2.hasNext();) {
                        String expression = pointcutName + counter;

                        // create and add a new pointcut def
                        PointcutDefinition pointcutDef = new PointcutDefinition();
                        pointcutDef.setName(expression);
                        pointcutDef.setClassPattern(className);
                        pointcutDef.setPattern(createFieldPattern(javaFields[i]));
                        pointcutDef.setType(PointcutDefinition.SET_FIELD);
                        definition.getAspectDefinition(AspectWerkzDefinition.SYSTEM_ASPECT).
                                addPointcut(pointcutDef);

                        String adviceAttribute = ((AdviceDefinition)it2.next()).getAttribute();
                        if (adviceAttribute == null) {
                            continue;
                        }
                        if (adviceAttribute.equals(attribute)) {
                            // get the advice ref
                            String adviceRef = definition.getAdviceNameByAttribute(adviceAttribute);
                            if (adviceRef == null) {
                                continue; // attribute not mapped to an advice
                            }

                            // create and add a new weaving rule def
                            AdviceWeavingRule weavingRule = new AdviceWeavingRule();
                            weavingRule.setExpression(expression);
                            weavingRule.addAdviceRef(adviceRef);
                            definition.getAspectDefinition(AspectWerkzDefinition.SYSTEM_ASPECT).
                                    addAdviceWeavingRule(weavingRule);

                            counter++;
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * Weaves the get field pointcut attributes.
     *
     * @param definition the definition
     * @param className the name of the parsed class
     * @param qdoxParser the QDox parser
     */
    private static void weaveGetFieldPointcutAttributes(
            final AspectWerkzDefinition definition,
            final String className,
            final QDoxParser qdoxParser) {
        String pointcutName = GETFIELD_POINTCUT_NAME + className.replaceAll("\\.", "_");

        int counter = 0;
        final JavaField[] javaFields = qdoxParser.getJavaFields();
        for (int i = 0; i < javaFields.length; i++) {

            DocletTag[] getFieldTags = javaFields[i].getTagsByName(AttributeTag.GET_FIELD);
            for (int j = 0; j < getFieldTags.length; j++) {
                if (getFieldTags[j] == null) {
                    continue;
                }

                String[] getFieldAttributes = getFieldTags[j].getParameters();
                for (int k = 0; k < getFieldAttributes.length; k++) {
                    String attribute = getFieldAttributes[k];

                    for (Iterator it2 = definition.getAdviceDefinitions().iterator(); it2.hasNext();) {
                        String expression = pointcutName + counter;

                        // create and add a new pointcut def
                        PointcutDefinition pointcutDef = new PointcutDefinition();
                        pointcutDef.setName(expression);
                        pointcutDef.setClassPattern(className);
                        pointcutDef.setPattern(createFieldPattern(javaFields[i]));
                        pointcutDef.setType(PointcutDefinition.GET_FIELD);
                        definition.getAspectDefinition(AspectWerkzDefinition.SYSTEM_ASPECT).
                                addPointcut(pointcutDef);

                        String adviceAttribute = ((AdviceDefinition)it2.next()).getAttribute();
                        if (adviceAttribute == null) {
                            continue;
                        }
                        if (adviceAttribute.equals(attribute)) {
                            // get the advice ref
                            String adviceRef = definition.getAdviceNameByAttribute(adviceAttribute);
                            if (adviceRef == null) {
                                continue; // attribute not mapped to an advice
                            }

                            // create and add a new weaving rule def
                            AdviceWeavingRule weavingRule = new AdviceWeavingRule();
                            weavingRule.setExpression(expression);
                            weavingRule.addAdviceRef(adviceRef);
                            definition.getAspectDefinition(AspectWerkzDefinition.SYSTEM_ASPECT).
                                    addAdviceWeavingRule(weavingRule);

                            counter++;
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * Weaves the throws attributes.
     *
     * @param definition the definition
     * @param className the name of the parsed class
     * @param qdoxParser the QDox parser
     */
    private static void weaveThrowsPointcutAttributes(
            final AspectWerkzDefinition definition,
            final String className,
            final QDoxParser qdoxParser) {
        String pointcutName = THROWS_POINTCUT_NAME + className.replaceAll("\\.", "_");

        int counter = 0;
        final JavaMethod[] javaMethods = qdoxParser.getJavaMethods();
        for (int i = 0; i < javaMethods.length; i++) {

            DocletTag[] throwsTags = javaMethods[i].getTagsByName(AttributeTag.THROWS);
            for (int j = 0; j < throwsTags.length; j++) {
                if (throwsTags[j] == null) {
                    continue;
                }

                String exceptionClassPattern = throwsTags[j].getNamedParameter("exception");

                if (exceptionClassPattern == null) {
                    throw new DefinitionException("exception class not specified for throws attribute at method <" + javaMethods[i].getName() + ">");
                }

                String[] attributes = throwsTags[j].getParameters();
                for (int k = 0; k < attributes.length; k++) {
                    String attribute = attributes[k];
                    if (attribute.startsWith("exception=")) {
                        continue;
                    }
                    for (Iterator it2 = definition.getAdviceDefinitions().iterator(); it2.hasNext();) {
                        String expression = pointcutName + counter;

                        // create and add a new pointcut def
                        PointcutDefinition pointcutDef = new PointcutDefinition();
                        pointcutDef.setName(expression);
                        pointcutDef.setClassPattern(className);
                        pointcutDef.setPattern(createThrowsPattern(
                                exceptionClassPattern, javaMethods[i]));
                        pointcutDef.setType(PointcutDefinition.THROWS);
                        definition.getAspectDefinition(AspectWerkzDefinition.SYSTEM_ASPECT).
                                addPointcut(pointcutDef);

                        String adviceAttribute = ((AdviceDefinition)it2.next()).getAttribute();
                        if (adviceAttribute == null) {
                            continue;
                        }
                        if (adviceAttribute.equals(attribute)) {
                            // get the advice ref
                            String adviceRef = definition.getAdviceNameByAttribute(adviceAttribute);
                            if (adviceRef == null) {
                                continue; // attribute not mapped to an advice
                            }

                            // create and add a new weaving rule def
                            AdviceWeavingRule weavingRule = new AdviceWeavingRule();
                            weavingRule.setExpression(expression);
                            weavingRule.addAdviceRef(adviceRef);
                            definition.getAspectDefinition(AspectWerkzDefinition.SYSTEM_ASPECT).
                                    addAdviceWeavingRule(weavingRule);

                            counter++;
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * Weaves the caller side pointcut attributes.
     *
     * @param definition the definition
     * @param className the name of the parsed class
     * @param qdoxParser the QDox parser
     */
    private static void weaveCallerSidePointcutAttributes(
            final AspectWerkzDefinition definition,
            final String className,
            final QDoxParser qdoxParser) {
        String pointcutName = CALLERSIDE_POINTCUT_NAME + className.replaceAll("\\.", "_");

        int counter = 0;
        final JavaMethod[] javaMethods = qdoxParser.getJavaMethods();
        for (int i = 0; i < javaMethods.length; i++) {

            DocletTag[] callerSideTags = javaMethods[i].getTagsByName(AttributeTag.CALLER_SIDE);
            for (int j = 0; j < callerSideTags.length; j++) {
                if (callerSideTags[j] == null) {
                    continue;
                }
                String callerClassPattern = callerSideTags[j].getNamedParameter("callerclass");
                if (callerClassPattern == null) {
                    throw new DefinitionException("caller class not specified for caller side attribute at method <" + javaMethods[i].getName() + ">");
                }

                String[] callerSideAttributes = callerSideTags[j].getParameters();
                for (int k = 0; k < callerSideAttributes.length; k++) {
                    String attribute = callerSideAttributes[k];
                    if (attribute.startsWith("callerclass=")) {
                        continue;
                    }
                    for (Iterator it2 = definition.getAdviceDefinitions().iterator(); it2.hasNext();) {
                        String expression = pointcutName + counter;

                        // create and add a new pointcut def
                        PointcutDefinition pointcutDef = new PointcutDefinition();
                        pointcutDef.setName(expression);
                        pointcutDef.setClassPattern(callerClassPattern);
                        pointcutDef.setPattern(createCallerSidePattern(className, javaMethods[i]));
                        pointcutDef.setType(PointcutDefinition.CALLER_SIDE);
                        definition.getAspectDefinition(AspectWerkzDefinition.SYSTEM_ASPECT).
                                addPointcut(pointcutDef);

                        String adviceAttribute = ((AdviceDefinition)it2.next()).getAttribute();
                        if (adviceAttribute == null) {
                            continue;
                        }

                        if (adviceAttribute.equals(attribute)) {
                            // get the advice ref
                            String adviceRef = definition.getAdviceNameByAttribute(adviceAttribute);
                            if (adviceRef == null) {
                                continue; // attribute not mapped to an advice
                            }

                            // create and add a new weaving rule def
                            AdviceWeavingRule weavingRule = new AdviceWeavingRule();
                            weavingRule.setExpression(expression);
                            weavingRule.addAdviceRef(adviceRef);
                            definition.getAspectDefinition(AspectWerkzDefinition.SYSTEM_ASPECT).
                                    addAdviceWeavingRule(weavingRule);

                            counter++;
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * Weaves the cflow attributes.
     *
     * @param definition the definition
     * @param className the name of the parsed class
     * @param qdoxParser the QDox parser
     */
    private static void weaveCFlowPointcutAttributes(
            final AspectWerkzDefinition definition,
            final String className,
            final QDoxParser qdoxParser) {

        final JavaMethod[] javaMethods = qdoxParser.getJavaMethods();
        for (int i = 0; i < javaMethods.length; i++) {

            DocletTag[] cflowTags = javaMethods[i].getTagsByName(AttributeTag.CFLOW);
            for (int j = 0; j < cflowTags.length; j++) {
                if (cflowTags[j] == null) {
                    continue;
                }
                String[] attributes = cflowTags[j].getParameters();
                if (attributes.length == 0) {
                    continue;
                }

                // get the user defined name for the cflow pointcut
                String name = attributes[0];

                // create and add a new pointcut def
                PointcutDefinition pointcutDef = new PointcutDefinition();
                pointcutDef.setName(name);
                pointcutDef.setClassPattern(className);
                pointcutDef.setPattern(createMethodPattern(javaMethods[i]));
                pointcutDef.setType(PointcutDefinition.CFLOW);

                definition.getAspectDefinition(AspectWerkzDefinition.SYSTEM_ASPECT).
                        addPointcut(pointcutDef);

                // create and add a new weaving rule def
                AdviceWeavingRule weavingRule = new AdviceWeavingRule();
                weavingRule.setExpression(name);
                weavingRule.addAdviceRef(CFlowAdvice.NAME);
                definition.getAspectDefinition(AspectWerkzDefinition.SYSTEM_ASPECT).
                        addAdviceWeavingRule(weavingRule);
                break;
            }
        }
    }

    /**
     * Compiles the class list.
     *
     * @param qdoxParser the QDox parser
     * @return the class list
     */
    private static List compileClassList(final QDoxParser qdoxParser) {
        final String[] allClassNames = qdoxParser.getAllClassesNames();
        final List fullClassNames = new ArrayList(allClassNames.length);
        for (int i = 0; i < allClassNames.length; i++) {
            fullClassNames.add(allClassNames[i]);
        }
        return fullClassNames;
    }

    /**
     * Compiles the class meta-data for all introduced implementations.
     *
     * @param model the weave model
     * @param qdoxParser the QDox parser
     * @param metaDataDir the meta-data dir
     */
    private static void compileIntroductionMetaData(final WeaveModel model,
                                                    final QDoxParser qdoxParser) {
        final List parsedClasses = compileClassList(qdoxParser);

        for (Iterator it = model.getDefinition().getIntroductionDefinitions().iterator(); it.hasNext();) {
            String introduction = ((IntroductionDefinition)it.next()).getImplementation();
            if (introduction == null) {
                continue; // interface introduction
            }
            for (Iterator it1 = parsedClasses.iterator(); it1.hasNext();) {
                final String className = (String)it1.next();
                if (introduction.equals(className)) {
                    model.addIntroductionMetaData(parseClass(qdoxParser, className));
                }
            }
        }
    }

    /**
     * Parses a class, retrieves, wrappes up and returns it's meta-data.
     *
     * @param qdoxParser the QDox parser
     * @param classToParse the name of the class to compile
     * @return the meta-data for the class
     */
    private static ClassMetaData parseClass(final QDoxParser qdoxParser,
                                            final String classToParse) {
        if (!qdoxParser.parse(classToParse)) {
            return null;
        }

        final JavaMethod[] methods = qdoxParser.getJavaMethods();
        final JavaField[] fields = qdoxParser.getJavaFields();

        final List methodList = new ArrayList(methods.length);
        for (int i = 0; i < methods.length; i++) {
            methodList.add(QDoxMetaDataMaker.createMethodMetaData(methods[i]));
        }

        final List fieldList = new ArrayList(fields.length);
        for (int i = 0; i < fields.length; i++) {
            fieldList.add(QDoxMetaDataMaker.createFieldMetaData(fields[i]));
        }

        final ClassMetaData classMetaData = new ClassMetaData();
        classMetaData.setName(classToParse);
        classMetaData.setMethods(methodList);
        classMetaData.setFields(fieldList);

        return classMetaData;
    }

    /**
     * Creates a method regular expression pattern.
     *
     * @param javaMethod the method
     * @return the pattern
     */
    private static String createMethodPattern(final JavaMethod javaMethod) {
        final StringBuffer pattern = new StringBuffer();
        pattern.append(javaMethod.getReturns().getValue());
        pattern.append(' ');
        pattern.append(javaMethod.getName());
        pattern.append('(');
        JavaParameter[] parameters = javaMethod.getParameters();
        for (int l = 0; l < parameters.length; l++) {
            JavaParameter parameter = parameters[l];
            pattern.append(parameter.getType().getValue());
            if (l != parameters.length - 1) {
                pattern.append(',');
            }
        }
        pattern.append(')');
        return pattern.toString();
    }

    /**
     * Creates a field regular expression pattern.
     *
     * @param javaField the field
     * @return the pattern
     */
    private static String createFieldPattern(final JavaField javaField) {
        final StringBuffer pattern = new StringBuffer();
        pattern.append(javaField.getType().getValue());
        pattern.append(' ');
        pattern.append(javaField.getName());
        return pattern.toString();
    }

    /**
     * Creates a throws regular expression pattern.
     *
     * @param exceptionClassPattern the name of the exception class
     * @param javaMethod the method
     * @return the pattern
     */
    private static String createThrowsPattern(
            final String exceptionClassPattern,
            final JavaMethod javaMethod) {
        StringBuffer throwsPattern = new StringBuffer();
        throwsPattern.append(createMethodPattern(javaMethod));
        throwsPattern.append('#');
        throwsPattern.append(exceptionClassPattern);
        return throwsPattern.toString();
    }

    /**
     * Creates a caller side regular expression pattern.
     *
     * @param className the name of the class
     * @param javaMethod the method
     * @return the pattern
     */
    private static String createCallerSidePattern(final String className,
                                                  final JavaMethod javaMethod) {
        StringBuffer callerSidePattern = new StringBuffer();
        callerSidePattern.append(className);
        callerSidePattern.append('#');
        callerSidePattern.append(createMethodPattern(javaMethod));
        return callerSidePattern.toString();
    }

    /**
     * Runs the compiler from the command line.
     *
     * @param args
     */
    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("usage: java [options...] org.codehaus.aspectwerkz.metadata.SourceFileMetaDataCompiler <pathToDefinitionFile> <pathToSrcDir> <pathToMetaDataDir> <uuidForWeaveModel>");
            System.out.println("       <uuidForWeaveModel> is optional (if not specified one will be generated)");
            System.exit(0);
        }
        System.out.println("compiling weave model...");
        if (args.length == 4) {
            SourceFileMetaDataCompiler.compile(args[0], args[1], args[2], args[3]);
        }
        else {
            SourceFileMetaDataCompiler.compile(args[0], args[1], args[2]);
        }
        System.out.println("weave model for classes in " + args[1] + " have been compiled to " + args[2]);
    }
}
