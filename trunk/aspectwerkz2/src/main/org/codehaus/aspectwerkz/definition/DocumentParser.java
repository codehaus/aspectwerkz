/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.codehaus.aspectwerkz.ContextClassLoader;
import org.codehaus.aspectwerkz.System;
import org.codehaus.aspectwerkz.DeploymentModel;
import org.codehaus.aspectwerkz.definition.attribute.AspectAttributeParser;
import org.codehaus.aspectwerkz.definition.attribute.AttributeParser;
import org.codehaus.aspectwerkz.definition.attribute.IntroduceAttribute;
import org.codehaus.aspectwerkz.definition.expression.PointcutType;
import org.codehaus.aspectwerkz.exception.DefinitionException;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.transform.TransformationUtil;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;

/**
 * Parses the attribdef XML definition using <tt>dom4j</tt>.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class DocumentParser {

    /**
     * The attribute parser, retrieves the custom attributes from the bytecode of the classes.
     *
     * @TODO: should use factory, to be able to support othere implementations, f.e. JSR-175
     */
    private static final AttributeParser s_attributeParser = new AspectAttributeParser();

    /**
     * Parses the definition DOM document.
     *
     * @param loader   the current class loader
     * @param document the defintion as a document
     * @return the definitions
     */
    public static List parse(final ClassLoader loader, final Document document) {
        final Element root = document.getRootElement();

        // parse the transformation scopes
        return parseSystemElements(loader, root);
    }

    /**
     * Parses the <tt>system</tt> elements.
     *
     * @param loader        the current class loader
     * @param systemElement the system element
     * @param basePackage   the base package
     * @return the definition for the system
     */
    public static SystemDefinition parseSystemElement(
            final ClassLoader loader,
            final Element systemElement,
            final String basePackage) {

        final SystemDefinition definition = new SystemDefinition();

        String uuid = systemElement.attributeValue("id");
        if (uuid == null || uuid.equals("")) {
            // TODO: log a warning "no id specified in the definition, using default (AspectWerkz.DEFAULT_SYSTEM)"
            uuid = System.DEFAULT_SYSTEM;
        }
        definition.setUuid(uuid);

        // parse the include, exclude and prepare elements
        parseIncludePackageElements(systemElement, definition, basePackage);
        parseExcludePackageElements(systemElement, definition, basePackage);
        parsePrepareElements(systemElement, definition, basePackage);

        boolean hasDef = false;

        // parse without package elements
        if (parseAspectElements(loader, systemElement, definition, basePackage)) {
            hasDef = true;
        }
        // parse with package elements
        if (parsePackageElements(loader, systemElement, definition, basePackage)) {
            hasDef = true;
        }
        if (hasDef) {
            return definition;
        }
        else {
            return null;
        }
    }

    /**
     * Parses the definition DOM document.
     *
     * @param loader        the current class loader
     * @param systemElement the system element
     * @param definition    the definition
     * @param basePackage   the base package
     * @return flag that says if we have a definition of this kind or not
     */
    private static boolean parsePackageElements(
            final ClassLoader loader,
            final Element systemElement,
            final SystemDefinition definition,
            final String basePackage) {
        boolean hasDef = false;
        for (Iterator it1 = systemElement.elementIterator("package"); it1.hasNext();) {
            final Element packageElement = ((Element)it1.next());
            final String packageName = basePackage + getPackage(packageElement);

            if (parseAspectElements(loader, packageElement, definition, packageName)) {
                hasDef = true;
            }
        }
        return hasDef;
    }

    /**
     * Parses the <tt>aspect</tt> elements.
     *
     * @param loader        the current class loader
     * @param systemElement the system element
     * @param definition    the definition object
     * @param packageName   the package name
     * @return flag that says if we have a definition of this kind or not
     * @TODO: Passing in the CL all the way down here, but uses the context CL anyway. Will not work with AOPC.
     */
    private static boolean parseAspectElements(
            final ClassLoader loader,
            final Element systemElement,
            final SystemDefinition definition,
            final String packageName) {

        boolean hasDef = false;
        for (Iterator it1 = systemElement.elementIterator("aspect"); it1.hasNext();) {

            String aspectName = null;
            String className = null;
            String deploymentModel = null;
            Element aspect = (Element)it1.next();
            for (Iterator it2 = aspect.attributeIterator(); it2.hasNext();) {
                Attribute attribute = (Attribute)it2.next();

                final String name = attribute.getName().trim();
                final String value = attribute.getValue().trim();
                if (name.equalsIgnoreCase("class")) {
                    className = value;
                }
                else if (name.equalsIgnoreCase("deployment-model")) {
                    deploymentModel = value;
                }
                else if (name.equalsIgnoreCase("name")) {
                    aspectName = value;
                }
            }
            if (deploymentModel == null) {
                deploymentModel = "perJVM"; // default is perJVM
            }
            String aspectClassName = packageName + className;
            if (aspectName == null) {
                aspectName = aspectClassName;
            }

            // create the aspect definition
            AspectDefinition aspectDef = new AspectDefinition(aspectName, aspectClassName, deploymentModel);

            Class aspectClass = null;
            try {
                aspectClass = ContextClassLoader.loadClass(aspectClassName);
//                aspectClass = loader.loadClass(aspectClassName);
            }
            catch (ClassNotFoundException e) {
                throw new WrappedRuntimeException(e);
            }

            s_attributeParser.parse(aspectClass, aspectDef, definition);
            definition.addAspect(aspectDef);

            parseParameterElements(aspect, definition, aspectDef);
            parsePointcutElements(aspect, aspectDef);
            parseAdviceElements(loader, aspect, aspectDef, aspectClass);
            parseIntroductionElements(loader, aspect, aspectDef, aspectClass, packageName);

            // register introduction of aspect into the system (?? optim for TF ?) TODO check why
            for (Iterator mixins = aspectDef.getInterfaceIntroductions().iterator(); mixins.hasNext();) {
                definition.addInterfaceIntroductionDefinition((InterfaceIntroductionDefinition)mixins.next());
            }
            for (Iterator mixins = aspectDef.getIntroductions().iterator(); mixins.hasNext();) {
                definition.addIntroductionDefinition((IntroductionDefinition)mixins.next());
            }

            hasDef = true;
        }
        return hasDef;
    }

    /**
     * Parses the aspectElement parameters.
     * <p/>
     * TODO: should perhaps move the parameters to the aspect def instead of the system def
     *
     * @param aspectElement the aspect element
     * @param def           the system definition
     * @param aspectDef     the aspect def
     */
    private static void parseParameterElements(
            final Element aspectElement,
            final SystemDefinition def,
            final AspectDefinition aspectDef) {
        for (Iterator it2 = aspectElement.elementIterator(); it2.hasNext();) {
            Element parameterElement = (Element)it2.next();
            if (parameterElement.getName().trim().equals("param")) {
                def.addParameter(
                        aspectDef.getName(),
                        parameterElement.attributeValue("name"),
                        parameterElement.attributeValue("value")
                );
            }
        }
    }

    /**
     * Parses the pointcuts.
     *
     * @param aspectElement the aspect element
     * @param aspectDef     the system definition
     */
    private static void parsePointcutElements(final Element aspectElement, final AspectDefinition aspectDef) {
        for (Iterator it2 = aspectElement.elementIterator(); it2.hasNext();) {
            Element pointcutElement = (Element)it2.next();
            if (pointcutElement.getName().trim().equals("pointcut")) {
                String name = pointcutElement.attributeValue("name");
                String pattern = pointcutElement.attributeValue("pattern");
                DefinitionParserHelper.createAndAddPointcutDefToAspectDef(name, pattern, aspectDef);
            }
        }
    }

    /**
     * Parses the advices.
     *
     * @param loader        the current class loader
     * @param aspectElement the aspect element
     * @param aspectDef     the system definition
     * @param aspectClass   the aspect class
     */
    private static void parseAdviceElements(
            final ClassLoader loader,
            final Element aspectElement,
            final AspectDefinition aspectDef,
            final Class aspectClass) {

        List methodList = TransformationUtil.createSortedMethodList(aspectClass);

        for (Iterator it2 = aspectElement.elementIterator(); it2.hasNext();) {
            Element adviceElement = (Element)it2.next();
            if (adviceElement.getName().trim().equals("advice")) {
                String name = adviceElement.attributeValue("name");
                String type = adviceElement.attributeValue("type");
                String bindTo = adviceElement.attributeValue("bind-to");

                String adviceName = aspectClass.getName() + '.' + name;

                int methodIndex = 0;
                Method method = null;
                for (Iterator it3 = methodList.iterator(); it3.hasNext(); methodIndex++) {
                    method = (Method)it3.next();
                    if (method.getName().equals(name)) {
                        break;
                    }
                }
                createAndAddAdviceDefsToAspectDef(type, bindTo, adviceName, method, methodIndex, aspectDef);

                for (Iterator it1 = adviceElement.elementIterator("bind-to"); it1.hasNext();) {
                    Element bindToElement = (Element)it1.next();
                    String pointcut = bindToElement.attributeValue("pointcut");

                    createAndAddAdviceDefsToAspectDef(type, pointcut, adviceName, method, methodIndex, aspectDef);
                }
            }
        }
    }

    /**
     * Parses the introduction.
     *
     * @param loader        the current class loader
     * @param aspectElement the aspect element
     * @param aspectDef     the system definition
     * @param aspectClass   the aspect class
     * @param packageName
     */
    private static void parseIntroductionElements(
            final ClassLoader loader,
            final Element aspectElement,
            final AspectDefinition aspectDef,
            final Class aspectClass,
            final String packageName) {

        for (Iterator it2 = aspectElement.elementIterator(); it2.hasNext();) {
            Element adviceElement = (Element)it2.next();
            if (adviceElement.getName().trim().equals("introduction")) {
                String klass = adviceElement.attributeValue("class");
                String ynterface = adviceElement.attributeValue("interface");
                String name = adviceElement.attributeValue("name");
                String bindTo = adviceElement.attributeValue("bind-to");
                String deploymentModel = adviceElement.attributeValue("deployment-model");

                // make sure at least class or interface is specified
                if (klass == null && ynterface == null) {
                    throw new DefinitionException("class or interface should be defined for introduction:"
                            + aspectDef.getName());
                }

                // deployment-model defaults to perJVM
                if (deploymentModel == null || deploymentModel.length() <= 0) {
                    deploymentModel = DeploymentModel.getDeploymentModelAsString(DeploymentModel.PER_JVM);
                }

                // pure interface introduction
                if (klass == null || klass.length() <= 0) {
                    if (name == null || name.length() <= 0) {
                        name = packageName + ynterface;
                    }
                    DefinitionParserHelper.createAndAddInterfaceIntroductionDefToAspectDef(
                            bindTo, name, packageName + ynterface, aspectDef
                    );
                } else {
                    // mixin introduction
                    if (name == null || name.length() <= 0) {
                        name = packageName + klass;
                    }
                    Class mixin = null;
                    try {
                        mixin = aspectClass.getClassLoader().loadClass(packageName + klass);
                    }
                    catch (ClassNotFoundException e) {
                        throw new DefinitionException("could not find mixin implementation: "
                                + packageName + klass + " " + e.getMessage());
                    }
                    List introducedInterfaceNames = new ArrayList();
                    if (ynterface == null || ynterface.length() <= 0) {
                        Class[] introduced = mixin.getInterfaces();
                        for (int i=0; i < introduced.length; i++) {
                            introducedInterfaceNames.add(introduced[i].getName());
                        }
                    }
                    else {
                        introducedInterfaceNames.add(packageName + ynterface);
                    }
                    Method[] methods = (Method[])TransformationUtil.createSortedMethodList(mixin).toArray(new Method[]{});//gatherMixinSortedMethods(mixin, introduceAttr.getIntroducedInterfaceNames());
                    DefinitionParserHelper.createAndAddIntroductionDefToAspectDef(
                            bindTo, name, (String[])introducedInterfaceNames.toArray(new String[]{}), methods,
                            deploymentModel, aspectDef
                    );
                }
            }
        }
    }

    /**
     * Creates the advice definitions and adds them to the aspect definition.
     *
     * @param type        the type of advice
     * @param bindTo      the pointcut expresion
     * @param name        the name of the advice
     * @param method      the method implementing the advice
     * @param methodIndex the method index
     * @param aspectDef   the aspect definition
     */
    private static void createAndAddAdviceDefsToAspectDef(
            final String type,
            final String bindTo,
            final String name,
            final Method method,
            final int methodIndex,
            final AspectDefinition aspectDef) {
        if (type.equalsIgnoreCase("around")) {
            DefinitionParserHelper.createAndAddAroundAdviceDefToAspectDef(
                    bindTo, name, aspectDef.getName(), aspectDef.getClassName(),
                    method, methodIndex, aspectDef
            );
        }
        else if (type.equalsIgnoreCase("before")) {
            DefinitionParserHelper.createAndAddBeforeAdviceDefToAspectDef(
                    bindTo, name, aspectDef.getName(), aspectDef.getClassName(),
                    method, methodIndex, aspectDef
            );
        }
        else if (type.equalsIgnoreCase("after")) {
            DefinitionParserHelper.createAndAddAfterAdviceDefToAspectDef(
                    bindTo, name, aspectDef.getName(), aspectDef.getClassName(),
                    method, methodIndex, aspectDef
            );
        }
        else if (type.equalsIgnoreCase("afterFinally")) {
            // TODO: impl. afterFinally
        }
        else if (type.equalsIgnoreCase("afterReturning")) {
            // TODO: impl. afterReturning
        }
        else if (type.equalsIgnoreCase("afterThrowing")) {
            // TODO: impl. afterThrowing
        }
    }

    /**
     * Retrieves and returns the package.
     *
     * @param packageElement the package element
     * @return the package as a string ending with DOT, or empty string
     */
    private static String getPackage(final Element packageElement) {
        String packageName = "";
        for (Iterator it2 = packageElement.attributeIterator(); it2.hasNext();) {
            Attribute attribute = (Attribute)it2.next();
            if (attribute.getName().trim().equalsIgnoreCase("name")) {
                packageName = attribute.getValue().trim();
                if (packageName.endsWith(".*")) {
                    packageName = packageName.substring(0, packageName.length() - 1);
                }
                else if (packageName.endsWith(".")) {
                    ;// skip
                }
                else {
                    packageName += ".";
                }
                break;
            }
            else {
                continue;
            }
        }
        return packageName;
    }

    /**
     * Parses the <tt>include</tt> elements.
     *
     * @param root        the root element
     * @param definition  the definition object
     * @param packageName the package name
     */
    private static void parseIncludePackageElements(
            final Element root,
            final SystemDefinition definition,
            final String packageName) {
        for (Iterator it1 = root.elementIterator("include"); it1.hasNext();) {
            String includePackage = "";
            Element includeElement = (Element)it1.next();
            for (Iterator it2 = includeElement.attributeIterator(); it2.hasNext();) {
                Attribute attribute = (Attribute)it2.next();
                if (attribute.getName().trim().equalsIgnoreCase("package")) {

                    // handle base package
                    if (packageName.endsWith(".*")) {
                        includePackage = packageName.substring(0, packageName.length() - 2);
                    }
                    else if (packageName.endsWith(".")) {
                        includePackage = packageName.substring(0, packageName.length() - 1);
                    }

                    // handle exclude package
                    includePackage = packageName + attribute.getValue().trim();
                    if (includePackage.endsWith(".*")) {
                        includePackage = includePackage.substring(0, includePackage.length() - 2);
                    }
                    else if (includePackage.endsWith(".")) {
                        includePackage = includePackage.substring(0, includePackage.length() - 1);
                    }
                    break;
                }
                else {
                    continue;
                }
            }
            if (includePackage.length() != 0) {
                definition.addIncludePackage(includePackage);
            }
        }
    }

    /**
     * Parses the <tt>exclude</tt> elements.
     *
     * @param root        the root element
     * @param definition  the definition object
     * @param packageName the package name
     */
    private static void parseExcludePackageElements(
            final Element root,
            final SystemDefinition definition,
            final String packageName) {
        for (Iterator it1 = root.elementIterator("exclude"); it1.hasNext();) {
            String excludePackage = "";
            Element excludeElement = (Element)it1.next();
            for (Iterator it2 = excludeElement.attributeIterator(); it2.hasNext();) {
                Attribute attribute = (Attribute)it2.next();
                if (attribute.getName().trim().equalsIgnoreCase("package")) {

                    // handle base package
                    if (packageName.endsWith(".*")) {
                        excludePackage = packageName.substring(0, packageName.length() - 2);
                    }
                    else if (packageName.endsWith(".")) {
                        excludePackage = packageName.substring(0, packageName.length() - 1);
                    }

                    // handle exclude package
                    excludePackage = packageName + attribute.getValue().trim();
                    if (excludePackage.endsWith(".*")) {
                        excludePackage = excludePackage.substring(0, excludePackage.length() - 2);
                    }
                    else if (excludePackage.endsWith(".")) {
                        excludePackage = excludePackage.substring(0, excludePackage.length() - 1);
                    }
                    break;
                }
                else {
                    continue;
                }
            }
            if (excludePackage.length() != 0) {
                definition.addExcludePackage(excludePackage);
            }
        }
    }

    /**
     * Parses the <tt>prepare</tt> elements.
     *
     * @param root        the root element
     * @param definition  the definition object
     * @param packageName the base package name
     */
    public static void parsePrepareElements(
            final Element root,
            final SystemDefinition definition,
            final String packageName) {
        for (Iterator it1 = root.elementIterator("prepare"); it1.hasNext();) {
            String preparePackage = "";
            Element prepareElement = (Element)it1.next();
            for (Iterator it2 = prepareElement.attributeIterator(); it2.hasNext();) {
                Attribute attribute = (Attribute)it2.next();
                if (attribute.getName().trim().equals("package")) {

                    // handle base package
                    if (packageName.endsWith(".*")) {
                        preparePackage = packageName.substring(0, packageName.length() - 2);
                    }
                    else if (packageName.endsWith(".")) {
                        preparePackage = packageName.substring(0, packageName.length() - 1);
                    }

                    // handle prepare package
                    preparePackage = packageName + attribute.getValue().trim();
                    if (preparePackage.endsWith(".*")) {
                        preparePackage = preparePackage.substring(0, preparePackage.length() - 2);
                    }
                    else if (preparePackage.endsWith(".")) {
                        preparePackage = preparePackage.substring(0, preparePackage.length() - 1);
                    }
                    break;
                }
                else {
                    continue;
                }
            }
            if (preparePackage.length() != 0) {
                definition.addPreparePackage(preparePackage);
            }
        }
    }

    /**
     * Parses the <tt>system</tt> elements.
     *
     * @param loader the current class loader
     * @param root   the root element
     */
    private static List parseSystemElements(final ClassLoader loader, final Element root) {
        final List systemDefs = new ArrayList();

        for (Iterator it1 = root.elementIterator("system"); it1.hasNext();) {
            Element system = (Element)it1.next();
            SystemDefinition definition = parseSystemElement(loader, system, getBasePackage(system));
            if (definition != null) {
                systemDefs.add(definition);
            }
        }

        return systemDefs;
    }

    /**
     * Retrieves and returns the base package for a system element
     *
     * @param system a system element
     * @return the base package
     */
    private static String getBasePackage(final Element system) {
        String basePackage = "";
        for (Iterator it2 = system.attributeIterator(); it2.hasNext();) {
            Attribute attribute = (Attribute)it2.next();
            if (attribute.getName().trim().equalsIgnoreCase("base-package")) {
                basePackage = attribute.getValue().trim();
                if (basePackage.endsWith(".*")) {
                    basePackage = basePackage.substring(0, basePackage.length() - 1);
                }
                else if (basePackage.endsWith(".")) {
                    ; // skip
                }
                else {
                    basePackage += ".";
                }
                break;
            }
            else {
                continue;
            }
        }
        return basePackage;
    }
}
