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

import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

import com.thoughtworks.qdox.model.Type;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.JavaParameter;
import com.thoughtworks.qdox.model.JavaField;
import com.thoughtworks.qdox.model.DocletTag;
import com.thoughtworks.qdox.model.JavaClass;

import org.codehaus.aspectwerkz.definition.metadata.MetaDataCompiler;
import org.codehaus.aspectwerkz.definition.metadata.QDoxParser;
import org.codehaus.aspectwerkz.definition.metadata.MethodMetaData;
import org.codehaus.aspectwerkz.definition.metadata.FieldMetaData;
import org.codehaus.aspectwerkz.definition.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.definition.AspectWerkzDefinition;
import org.codehaus.aspectwerkz.definition.PointcutDefinition;
import org.codehaus.aspectwerkz.definition.AdviceDefinition;
import org.codehaus.aspectwerkz.definition.IntroductionDefinition;
import org.codehaus.aspectwerkz.transform.TransformationUtil;

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
 * @author <a href="mailto:jboner@acm.org">Jonas Bonér</a>
 * @version $Id: SourceFileMetaDataCompiler.java,v 1.2 2003-05-12 09:20:46 jboner Exp $
 */
public class SourceFileMetaDataCompiler extends MetaDataCompiler {

    /**
     * Parses a given source tree and creates and stores meta-data for
     * all methods for all the introduced <code>Introduction</code>s in
     * meta-data files in the directory specified.
     *
     * @param definitionFile the definition file to use
     * @param sourcePath the path to the sources
     * @param metaDataDir the path to the dir where to store the meta-data
     */
    public static void compile(final String definitionFile,
                               final String sourcePath,
                               final String metaDataDir) {
        if (definitionFile == null) throw new IllegalArgumentException("definition file can not be null");
        if (sourcePath == null) throw new IllegalArgumentException("source path can not be null");
        if (metaDataDir == null) throw new IllegalArgumentException("meta-data dir can not be null");

        createMetaDataDir(metaDataDir);

        final QDoxParser qdoxParser = new QDoxParser(sourcePath);
        final AspectWerkzDefinition definition =
                AspectWerkzDefinition.getDefinition(definitionFile);

        final WeaveModel weaveModel = weave(definition, qdoxParser);

        compileIntroductionMetaData(weaveModel, qdoxParser);

        saveWeaveModelToFile(metaDataDir, weaveModel);
    }

    /**
     * Parses the source tree and creates a weaving model based on the
     * definition file.
     *
     * @param definition the definition
     * @param qdoxParser the QDox parser
     * @return the weave model
     */
    public static WeaveModel weave(final AspectWerkzDefinition definition,
                                   final QDoxParser qdoxParser) {

        final String[] allClassNames = qdoxParser.getAllClassesNames();
        final WeaveModel weaveModel = new WeaveModel(definition);

        weaveAttributeDefinitions(
                definition, allClassNames, qdoxParser, weaveModel);

        WeaveModel.weaveXmlDefinition(definition, weaveModel);
        WeaveModel.addMetaDataToAdvices(definition, weaveModel);
        WeaveModel.addMetaDataToIntroductions(definition, weaveModel);

        return weaveModel;
    }

    /**
     * Parses the attributes and weaves in the advices depending on those.
     *
     * @param definition the definition
     * @param allClasses the classes parsed
     * @param qdoxParser the QDox parser
     * @param weaveModel the weaveModel
     */
    public static void weaveAttributeDefinitions(
            final AspectWerkzDefinition definition,
            final String[] allClasses,
            final QDoxParser qdoxParser,
            final WeaveModel weaveModel) {

        for (int i = 0; i < allClasses.length; i++) {
            String className = allClasses[i];

            if (!qdoxParser.parse(className)) continue;

            weaveAdviceDefinitionAttributes(qdoxParser, weaveModel);
            weaveIntroductionDefinitionAttributes(qdoxParser, weaveModel);

            weaveIntroductionAttributes(
                    definition, className, qdoxParser, weaveModel);
            weaveMethodPointcutAttributes(
                    definition, className, qdoxParser, weaveModel);
            weaveSetFieldPointcutAttributes(
                    definition, className, qdoxParser, weaveModel);
            weaveGetFieldPointcutAttributes(
                    definition, className, qdoxParser, weaveModel);
            weaveThrowsPointcutAttributes(
                    definition, className, qdoxParser, weaveModel);
            weaveCallerSidePointcutAttributes(
                    definition, className, qdoxParser, weaveModel);
        }
    }

    /**
     * Weaves the introduction definition attributes.
     *
     * @param qdoxParser the QDox parser
     * @param weaveModel the weaveModel
     */
    private static void weaveIntroductionDefinitionAttributes(
            final QDoxParser qdoxParser,
            final WeaveModel weaveModel) {

        JavaClass javaClass = qdoxParser.getJavaClass();
        DocletTag[] introductionDefTags = javaClass.
                getTagsByName(WeaveModel.ATTRIBUTE_INTRODUCTION_DEF);

        for (int i = 0; i < introductionDefTags.length; i++) {

            if (introductionDefTags[i] == null) continue;

            IntroductionDefinition introductionDefinition =
                    new IntroductionDefinition();
            introductionDefinition.setName(
                    introductionDefTags[i].getNamedParameter("name"));
            introductionDefinition.setInterface(
                    javaClass.getFullyQualifiedName());
            introductionDefinition.setImplementation(
                    introductionDefTags[i].getNamedParameter("implementation"));
            introductionDefinition.setDeploymentModel(
                    introductionDefTags[i].getNamedParameter("deployment-model"));
            introductionDefinition.setIsPersistent(
                    introductionDefTags[i].getNamedParameter("persistent"));
            introductionDefinition.setAttribute(
                    introductionDefTags[i].getNamedParameter("attribute"));

            weaveModel.addIntroductionDefinition(introductionDefinition);
        }
    }

    /**
     * Weaves the advice definition attributes.
     *
     * @param qdoxParser the QDox parser
     * @param weaveModel the weaveModel
     */
    private static void weaveAdviceDefinitionAttributes(
            final QDoxParser qdoxParser,
            final WeaveModel weaveModel) {

        JavaClass javaClass = qdoxParser.getJavaClass();
        DocletTag[] adviceDefTags = javaClass.
                getTagsByName(WeaveModel.ATTRIBUTE_ADVICE_DEF);

        for (int i = 0; i < adviceDefTags.length; i++) {

            if (adviceDefTags[i] == null) continue;

            AdviceDefinition adviceDefinition = new AdviceDefinition();
            adviceDefinition.setName(
                    adviceDefTags[i].getNamedParameter("name"));
            adviceDefinition.setAdvice(
                    javaClass.getFullyQualifiedName());
            adviceDefinition.setDeploymentModel(
                    adviceDefTags[i].getNamedParameter("deployment-model"));
            adviceDefinition.setIsPersistent(
                    adviceDefTags[i].getNamedParameter("persistent"));
            adviceDefinition.setAttribute(
                    adviceDefTags[i].getNamedParameter("attribute"));

            weaveAdviceParamAttributes(javaClass, adviceDefinition);

            weaveModel.addAdviceDefinition(adviceDefinition);
        }
    }

    /**
     * Weaves the advice param attributes.
     *
     * @param qdoxParser the QDox parser
     * @param weaveModel the weaveModel
     */
    private static void weaveAdviceParamAttributes(
            final JavaClass javaClass,
            final AdviceDefinition adviceDefinition) {

        DocletTag[] adviceDefTags = javaClass.
                getTagsByName(WeaveModel.ATTRIBUTE_ADVICE_PARAM);

        for (int i = 0; i < adviceDefTags.length; i++) {

            if (adviceDefTags[i] == null) continue;

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
     * @param weaveModel the weaveModel
     */
    private static void weaveIntroductionAttributes(
            final AspectWerkzDefinition definition,
            final String className,
            final QDoxParser qdoxParser,
            final WeaveModel weaveModel) {

        JavaClass javaClass = qdoxParser.getJavaClass();
        DocletTag[] introductionTags = javaClass.
                getTagsByName(WeaveModel.ATTRIBUTE_INTRODUCTION);

        for (int i = 0; i < introductionTags.length; i++) {

            if (introductionTags[i] == null) continue;

            final List introductions = new ArrayList();
            String[] attributes = introductionTags[i].getParameters();
            for (int j = 0; j < attributes.length; j++) {
                String attribute = attributes[j];

                final String introductionAttribute = definition.
                        getIntroductionNameByAttribute(attribute);
                if (introductionAttribute == null) continue;
                introductions.add(introductionAttribute);
            }
            weaveModel.createWeaveMetaData(className);
            weaveModel.getWeaveMetaData(className).
                    addIntroductions(introductions);
        }
    }

    /**
     * Weaves the method attributes.
     *
     * @param definition the definition
     * @param className the name of the parsed class
     * @param qdoxParser the QDox parser
     * @param weaveModel the weaveModel
     */
    private static void weaveMethodPointcutAttributes(
            final AspectWerkzDefinition definition,
            final String className,
            final QDoxParser qdoxParser,
            final WeaveModel weaveModel) {

        final JavaMethod[] javaMethods = qdoxParser.getJavaMethods();
        for (int i = 0; i < javaMethods.length; i++) {

            DocletTag[] methodTags = javaMethods[i].
                    getTagsByName(WeaveModel.ATTRIBUTE_ADVICE_METHOD);
            for (int j = 0; j < methodTags.length; j++) {

                if (methodTags[j] == null) continue;

                String[] attributes = methodTags[j].getParameters();
                for (int k = 0; k < attributes.length; k++) {
                    String attribute = attributes[k];

                    for (Iterator it2 = definition.getAdviceDefinitions().iterator(); it2.hasNext();) {
                        String adviceAttribute = ((AdviceDefinition)it2.next()).getAttribute();
                        if (adviceAttribute == null) continue;

                        if (adviceAttribute.equals(attribute)) {

                            final PointcutDefinition pointcutDefinition =
                                    new PointcutDefinition();

                            // create a pattern for the method
                            String pattern = createMethodPattern(javaMethods[i]);

                            pointcutDefinition.addPattern(pattern);
                            pointcutDefinition.setType(PointcutDefinition.METHOD);

                            final String adviceName = definition.
                                    getAdviceNameByAttribute(adviceAttribute);

                            if (adviceName == null) continue;
                            pointcutDefinition.addAdvice(adviceName);

                            weaveModel.createWeaveMetaData(className);
                            weaveModel.getWeaveMetaData(className).
                                    addMethodPointcut(
                                            pattern, pointcutDefinition);
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
     * @param weaveModel the weaveModel
     */
    private static void weaveSetFieldPointcutAttributes(
            final AspectWerkzDefinition definition,
            final String className,
            final QDoxParser qdoxParser,
            final WeaveModel weaveModel) {

        final JavaField[] javaFields = qdoxParser.getJavaFields();
        for (int i = 0; i < javaFields.length; i++) {

            DocletTag[] setFieldTags = javaFields[i].
                    getTagsByName(WeaveModel.ATTRIBUTE_ADVICE_SET_FIELD);

            for (int j = 0; j < setFieldTags.length; j++) {

                if (setFieldTags[j] == null) continue;

                String[] setFieldAttributes = setFieldTags[j].getParameters();
                for (int k = 0; k < setFieldAttributes.length; k++) {

                    String attribute = setFieldAttributes[k];
                    for (Iterator it2 = definition.getAdviceDefinitions().iterator(); it2.hasNext();) {
                        AdviceDefinition adviceDefinition = (AdviceDefinition)it2.next();

                        String adviceAttribute = adviceDefinition.getAttribute();
                        if (adviceAttribute == null) continue;

                        if (adviceAttribute.equals(attribute)) {
                            final PointcutDefinition pointcutDefinition =
                                    new PointcutDefinition();

                            String fieldPattern = createFieldPattern(javaFields[i]);

                            pointcutDefinition.addPattern(fieldPattern);
                            pointcutDefinition.setType(PointcutDefinition.SET_FIELD);

                            final String adviceName = definition.
                                    getAdviceNameByAttribute(adviceAttribute);

                            if (adviceName == null) continue;
                            pointcutDefinition.addAdvice(adviceName);

                            weaveModel.createWeaveMetaData(className);
                            weaveModel.getWeaveMetaData(className).
                                    addSetFieldPointcut(
                                            fieldPattern,
                                            pointcutDefinition);
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
     * @param weaveModel the weaveModel
     */
    private static void weaveGetFieldPointcutAttributes(
            final AspectWerkzDefinition definition,
            final String className,
            final QDoxParser qdoxParser,
            final WeaveModel weaveModel) {

        final JavaField[] javaFields = qdoxParser.getJavaFields();

        for (int i = 0; i < javaFields.length; i++) {

            DocletTag[] getFieldTags = javaFields[i].
                    getTagsByName(WeaveModel.ATTRIBUTE_ADVICE_GET_FIELD);
            for (int j = 0; j < getFieldTags.length; j++) {

                if (getFieldTags[j] == null) continue;

                String[] getFieldAttributes = getFieldTags[j].getParameters();
                for (int k = 0; k < getFieldAttributes.length; k++) {
                    String attribute = getFieldAttributes[k];

                    for (Iterator it2 = definition.getAdviceDefinitions().iterator(); it2.hasNext();) {
                        String adviceAttribute = ((AdviceDefinition)it2.next()).getAttribute();
                        if (adviceAttribute == null) continue;

                        if (adviceAttribute.equals(attribute)) {
                            final PointcutDefinition pointcutDefinition =
                                    new PointcutDefinition();

                            String fieldPattern = createFieldPattern(javaFields[i]);

                            pointcutDefinition.addPattern(fieldPattern);
                            pointcutDefinition.setType(PointcutDefinition.GET_FIELD);

                            final String adviceName = definition.
                                    getAdviceNameByAttribute(adviceAttribute);

                            if (adviceName == null) continue;
                            pointcutDefinition.addAdvice(adviceName);

                            weaveModel.createWeaveMetaData(className);
                            weaveModel.getWeaveMetaData(className).
                                    addGetFieldPointcut(
                                            fieldPattern,
                                            pointcutDefinition);
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
     * @param weaveModel the weaveModel
     */
    private static void weaveThrowsPointcutAttributes(
            final AspectWerkzDefinition definition,
            final String className,
            final QDoxParser qdoxParser,
            final WeaveModel weaveModel) {

        final JavaMethod[] javaMethods = qdoxParser.getJavaMethods();
        for (int i = 0; i < javaMethods.length; i++) {

            DocletTag[] methodTags = javaMethods[i].
                    getTagsByName(WeaveModel.ATTRIBUTE_ADVICE_THROWS);

            for (int j = 0; j < methodTags.length; j++) {

                if (methodTags[j] == null) continue;

                String[] attributes = methodTags[j].getParameters();
                for (int k = 0; k < attributes.length; k++) {
                    String attribute = attributes[k];

                    for (Iterator it2 = definition.getAdviceDefinitions().iterator(); it2.hasNext();) {
                        String adviceAttribute = ((AdviceDefinition)it2.next()).getAttribute();

                        if (adviceAttribute.equals(attribute)) {

                            final PointcutDefinition pointcutDefinition =
                                    new PointcutDefinition();

                            String methodPattern = createMethodPattern(javaMethods[i]);

                            pointcutDefinition.addPattern(methodPattern);
                            pointcutDefinition.setType(PointcutDefinition.THROWS);

                            final String adviceName = definition.
                                    getAdviceNameByAttribute(adviceAttribute);

                            if (adviceName == null) continue;
                            pointcutDefinition.addAdvice(adviceName);

                            weaveModel.createWeaveMetaData(className);
                            weaveModel.getWeaveMetaData(className).
                                    addThrowsPointcut(
                                            methodPattern,
                                            pointcutDefinition);
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
     * @param weaveModel the weaveModel
     */
    private static void weaveCallerSidePointcutAttributes(
            final AspectWerkzDefinition definition,
            final String className,
            final QDoxParser qdoxParser,
            final WeaveModel weaveModel) {

        final JavaMethod[] javaMethods = qdoxParser.getJavaMethods();
        for (int i = 0; i < javaMethods.length; i++) {

            DocletTag[] callerSideTags = javaMethods[i].
                    getTagsByName(WeaveModel.ATTRIBUTE_ADVICE_CALLER_SIDE);
            for (int j = 0; j < callerSideTags.length; j++) {

                if (callerSideTags[j] == null) continue;

                String callerSideTargetPattern =
                        callerSideTags[j].getNamedParameter("pattern");

                String[] callerSideAttributes = callerSideTags[j].getParameters();
                for (int k = 0; k < callerSideAttributes.length; k++) {
                    String attribute = callerSideAttributes[k];
                    if (attribute.startsWith("pattern=")) continue;

                    for (Iterator it2 = definition.getAdviceDefinitions().iterator(); it2.hasNext();) {
                        String adviceAttribute = ((AdviceDefinition)it2.next()).getAttribute();
                        if (adviceAttribute == null) continue;

                        if (adviceAttribute.equals(attribute)) {
                            final PointcutDefinition pointcutDefinition =
                                    new PointcutDefinition();

                            String callerSidePattern = createCallerSidePattern(
                                    className, javaMethods[i]);
                            pointcutDefinition.addPattern(callerSidePattern);
                            pointcutDefinition.setCallerSidePattern(
                                    callerSideTargetPattern);
                            pointcutDefinition.setType(
                                    PointcutDefinition.CALLER_SIDE);

                            final String adviceName = definition.
                                    getAdviceNameByAttribute(adviceAttribute);

                            if (adviceName == null) continue;
                            pointcutDefinition.addAdvice(adviceName);

                            weaveModel.addCallSidePointcut(
                                    callerSidePattern,
                                    pointcutDefinition);

                            // create a regular WeaveMetaData instance for the
                            // caller side pattern, needed since attribute based
                            // caller side definitions doesn't have an aspect
                            weaveModel.createWeaveMetaData(
                                    pointcutDefinition.getCallerSidePattern());

                            break;
                        }
                    }
                }
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
        final List introductions = model.getIntroductionDefinitions();

        for (Iterator it = introductions.iterator(); it.hasNext();) {
            final String introduction =
                    ((IntroductionDefinition)it.next()).
                    getImplementation();
            if (introduction == null) continue; // interface introduction
            for (Iterator it1 = parsedClasses.iterator(); it1.hasNext();) {
                final String className = (String)it1.next();

                if (introduction.equals(className)) {
                    ClassMetaData classMetaData = parseClass(qdoxParser, className);
                    model.addIntroductionMetaData(classMetaData);
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

        if (!qdoxParser.parse(classToParse)) return null;

        final JavaMethod[] methods = qdoxParser.getJavaMethods();
        final JavaField[] fields = qdoxParser.getJavaFields();

        final List methodList = new ArrayList(methods.length);
        for (int i = 0; i < methods.length; i++) {
            methodList.add(createMethodMetaData(methods[i]));
        }

        final List fieldList = new ArrayList(fields.length);
        for (int i = 0; i < fields.length; i++) {
            fieldList.add(createFieldMetaData(fields[i]));
        }

        final ClassMetaData classMetaData = new ClassMetaData();
        classMetaData.setName(classToParse);
        classMetaData.setMethods(methodList);
        classMetaData.setFields(fieldList);

        return classMetaData;
    }

    /**
     * Create a new <code>MethodMetaData</code> based on the QDox
     * <code>JavaMethod</code passed as parameter.
     *
     * @param method the QDox method
     * @return the method meta-data
     */
    public static MethodMetaData createMethodMetaData(final JavaMethod method) {
        final MethodMetaData methodMetaData = new MethodMetaData();

        methodMetaData.setName(method.getName());
        methodMetaData.setModifiers(TransformationUtil.
                getModifiersAsInt(method.getModifiers()));

        if (method.getReturns() != null) {
            methodMetaData.setReturnType(method.getReturns().getValue());
        }

        JavaParameter[] parameters = method.getParameters();
        String[] parameterTypes = new String[parameters.length];
        for (int j = 0; j < parameters.length; j++) {
            parameterTypes[j] = parameters[j].getType().getValue();
        }
        methodMetaData.setParameterTypes(parameterTypes);

        Type[] exceptions = method.getExceptions();
        String[] exceptionTypes = new String[exceptions.length];
        for (int j = 0; j < exceptions.length; j++) {
            exceptionTypes[j] = exceptions[j].getValue();
        }
        methodMetaData.setExceptionTypes(exceptionTypes);
        return methodMetaData;
    }

    /**
     * Create a new <code>FieldMetaData</code> based on the QDox
     * <code>JavaField</code passed as parameter.
     *
     * @param field the QDox field
     * @return the field meta-data
     */
    private static FieldMetaData createFieldMetaData(final JavaField field) {
        final FieldMetaData fieldMetaData = new FieldMetaData();

        fieldMetaData.setName(field.getName());
        fieldMetaData.setModifiers(TransformationUtil.
                getModifiersAsInt(field.getModifiers()));
        fieldMetaData.setType(field.getType().getValue());

        return fieldMetaData;
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
            System.out.println("usage: java [options...] org.codehaus.aspectwerkz.definition.metadata.SourceFileMetaDataCompiler <pathToDefinitionFile> <pathToSrcDir> <pathToMetaDataDir>");
            System.exit(0);
        }
        System.out.println("compiling weave model...");
        SourceFileMetaDataCompiler.compile(args[0], args[1], args[2]);
        System.out.println("weave model for classes in " + args[1] + " have been compiled to " + args[2]);
    }
}
