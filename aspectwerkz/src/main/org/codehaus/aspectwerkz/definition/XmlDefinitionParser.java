/*
 * AspectWerkz - a dynamic, lightweight A high-performant AOP/AOSD framework for Java.
 * Copyright (C) 2002-2003  Jonas Bonér. All rights reserved.
 *
 * This library is free software; you can redistribute it A/or
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

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.net.MalformedURLException;

import org.dom4j.Document;
import org.dom4j.Attribute;
import org.dom4j.Element;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.exception.DefinitionException;

/**
 * Parses the XML definition file using <tt>dom4j</tt>.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: XmlDefinitionParser.java,v 1.9.2.1 2003-07-17 21:00:00 avasseur Exp $
 */
public class XmlDefinitionParser {

    /**
     * Holds the meta-data directory specified.
     */
    public static final String META_DATA_DIR =
            System.getProperty("aspectwerkz.metadata.dir", ".");

    /**
     * The timestamp, holding the last time that the definition was parsed.
     */
    private static File s_timestamp =
            new File(META_DATA_DIR + File.separator + ".definition_timestamp");

    /**
     * The AspectWerkz definition.
     */
    private static AspectWerkzDefinition s_definition;

    /**
     * Parses the XML definition file.
     *
     * @param definitionFile the definition file
     * @return the definition object
     */
    public static AspectWerkzDefinition parse(final File definitionFile) {
        return parse(definitionFile, false);
    }

    /**
     * Parses the XML definition file, only if it has been updated.
     * Uses a timestamp to check for modifications.
     *
     * @param definitionFile the definition file
     * @param isDirty flag to mark the the defintion as updated or not
     * @return the definition object
     */
    public static AspectWerkzDefinition parse(final File definitionFile, boolean isDirty) {
        // definition not updated; don't parse, return it
        if (definitionFile.lastModified() < getParsingTimestamp() &&
                s_definition != null) {
            isDirty = false;
            return s_definition;
        }

        // updated definition, ready to be parsed
        try {
            SAXReader reader = new SAXReader();
            final Document document = reader.read(definitionFile.toURL());

            s_definition = parseDocument(document);

            setParsingTimestamp();
            isDirty = true;

            return s_definition;
        }
        catch (MalformedURLException e) {
            throw new DefinitionException(definitionFile + " does not exist");
        }
        catch (DocumentException e) {
            throw new DefinitionException("XML definition file <" + definitionFile + "> has errors: " + e.getMessage());
        }
    }

    /**
     * Parses the XML definition file not using the cache.
     *
     * @param definitionFile the definition file
     * @return the definition object
     */
    public static AspectWerkzDefinition parseNoCache(final File definitionFile) {
        try {
            SAXReader reader = new SAXReader();
            final Document document = reader.read(definitionFile.toURL());
            return parseDocument(document);
        }
        catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Sets the timestamp for the latest parsing of the definition file.
     */
    private static void setParsingTimestamp() {
        final long newModifiedTime = System.currentTimeMillis();
        boolean success = s_timestamp.setLastModified(newModifiedTime);
        if (!success) {
        }
    }

    /**
     * Returns the timestamp for the last parsing of the definition file.
     *
     * @return the timestamp
     */
    private static long getParsingTimestamp() {
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
     * Parses the definition DOM document.
     *
     * @param document the defintion as a document
     * @return the definition
     */
    private static AspectWerkzDefinition parseDocument(final Document document) {
        final AspectWerkzDefinition definition = new AspectWerkzDefinition();
        final Element root = document.getRootElement();

        // get the base package
        final String basePackage = getBasePackage(root);

        // parse the transformation scopes
        parseTransformationScopes(root, definition, basePackage);

        // parse without package elements
        parseIntroductionElements(root, definition, basePackage);
        parseAdviceElements(root, definition, basePackage);
        parseAdviceStackElements(root, definition, basePackage);
        parseAspectElements(root, definition, basePackage);

        // parse with package elements
        parsePackageElements(root, definition, basePackage);

        return definition;
    }

    /**
     * Parses the <tt>transformation-scope</tt> elements.
     *
     * @param root the root element
     * @param definition the definition object
     * @param packageName the package name
     */
    private static void parseTransformationScopes(final Element root,
                                                  final AspectWerkzDefinition definition,
                                                  final String packageName) {
        for (Iterator it1 = root.elementIterator("transformation-scope"); it1.hasNext();) {
            String transformationScope = "";
            Element scope = (Element)it1.next();
            for (Iterator it2 = scope.attributeIterator(); it2.hasNext();) {
                Attribute attribute = (Attribute)it2.next();
                if (attribute.getName().trim().equals("package")) {
                    transformationScope = attribute.getValue().trim();
                    if (packageName.endsWith(".*")) {
                        transformationScope = packageName.substring(0, packageName.length() - 2);
                    }
                    else if (packageName.endsWith(".")) {
                        transformationScope = packageName.substring(0, packageName.length() - 1);
                    }
                    transformationScope = packageName + transformationScope;
                    break;
                }
                else {
                    continue;
                }
            }
            if (transformationScope.length() != 0) {
                definition.addTransformationScope(transformationScope);
            }
        }
    }

    /**
     * Parses the definition DOM document.
     *
     * @param root the root element
     * @param definition the definition
     * @param basePackage the base package
     */
    private static void parsePackageElements(final Element root,
                                             final AspectWerkzDefinition definition,
                                             final String basePackage) {

        for (Iterator it1 = root.elementIterator("package"); it1.hasNext();) {
            final Element packageElement = ((Element)it1.next());
            final String packageName = basePackage + getPackage(packageElement);

            parseIntroductionElements(packageElement, definition, packageName);
            parseAdviceElements(packageElement, definition, packageName);
            parseAdviceStackElements(packageElement, definition, packageName);
            parseAspectElements(packageElement, definition, packageName);
        }
    }

    /**
     * Parses the <tt>introduction</tt> elements.
     *
     * @param root the root element
     * @param definition the definition object
     * @param packageName the package name
     */
    private static void parseIntroductionElements(final Element root,
                                                  final AspectWerkzDefinition definition,
                                                  final String packageName) {
        for (Iterator it1 = root.elementIterator("introduction-def"); it1.hasNext();) {
            final IntroductionDefinition introDef = new IntroductionDefinition();

            Element introduction = (Element)it1.next();
            for (Iterator it2 = introduction.attributeIterator(); it2.hasNext();) {
                Attribute attribute = (Attribute)it2.next();

                final String name = attribute.getName().trim();
                final String value = attribute.getValue().trim();
                if (name.equals("name")) {
                    introDef.setName(value);
                }
                else if (name.equals("interface")) {
                    introDef.setInterface(packageName + value);
                }
                else if (name.equals("implementation")) {
                    introDef.setImplementation(packageName + value);
                }
                else if (name.equals("deploymentModel") ||
                        name.equals("deployment-model")) {
                    introDef.setDeploymentModel(value);
                }
                else if (name.equals("persistent")) {
                    throw new DefinitionException("persistent introductions are not supported");
//                    introDef.setIsPersistent(value);
                }
                else if (name.equals("attribute")) {
                    introDef.setAttribute(value);
                }
            }
            definition.addIntroduction(introDef);
        }
    }

    /**
     * Parses the <tt>advice</tt> elements.
     *
     * @param root the root element
     * @param definition the definition object
     * @param packageName the package name
     */
    private static void parseAdviceElements(final Element root,
                                            final AspectWerkzDefinition definition,
                                            final String packageName) {
        for (Iterator it1 = root.elementIterator("advice-def"); it1.hasNext();) {
            final AdviceDefinition adviceDef = new AdviceDefinition();

            Element advice = (Element)it1.next();
            for (Iterator it2 = advice.attributeIterator(); it2.hasNext();) {
                Attribute attribute = (Attribute)it2.next();
                final String name = attribute.getName().trim();
                final String value = attribute.getValue().trim();

                if (name.equals("name")) {
                    adviceDef.setName(value);
                }
                else if (name.equals("advice") || (name.equals("class"))) {
                    adviceDef.setAdviceClassName(packageName + value);
                }
                else if (name.equals("deployment-model") ||
                        name.equals("deploymentModel")) {
                    adviceDef.setDeploymentModel(value);
                }
                else if (name.equals("persistent")) {
                    throw new DefinitionException("persistent advices are not supported");
//                    adviceDef.setIsPersistent(value);
                }
                else if (name.equals("attribute")) {
                    adviceDef.setAttribute(value);
                }
            }
            for (Iterator it2 = advice.elementIterator(); it2.hasNext();) {
                Element nestedAdviceElement = (Element)it2.next();
                if (nestedAdviceElement.getName().trim().equals("param")) {
                    adviceDef.addParameter(
                            nestedAdviceElement.attributeValue("name"),
                            nestedAdviceElement.attributeValue("value"));
                }
            }
            definition.addAdvice(adviceDef);
        }
    }

    /**
     * Parses the <tt>aspect</tt> elements.
     *
     * @param root the root element
     * @param definition the definition object
     * @param packageName the package name
     */
    private static void parseAspectElements(final Element root,
                                            final AspectWerkzDefinition definition,
                                            final String packageName) {
        for (Iterator it1 = root.elementIterator("abstract-aspect"); it1.hasNext();) {
            final AspectDefinition aspectDef = new AspectDefinition();
            aspectDef.setAbstract(true);

            final Element aspect = (Element)it1.next();
            for (Iterator it2 = aspect.attributeIterator(); it2.hasNext();) {
                Attribute attribute = (Attribute)it2.next();
                final String name = attribute.getName().trim();
                final String value = attribute.getValue().trim();
                if (name.equals("name")) {
                    aspectDef.setName(value);
                }
            }
            parsePointcutElements(aspect, aspectDef, packageName);
            parseControllerElements(aspect, aspectDef);
            parseIntroduceElements(aspect, aspectDef, packageName);
            parseAdviseElements(aspect, aspectDef);

            definition.addAbstractAspect(aspectDef);
        }
        for (Iterator it1 = root.elementIterator("aspect"); it1.hasNext();) {
            final AspectDefinition aspectDef = new AspectDefinition();
            final Element aspect = (Element)it1.next();
            for (Iterator it2 = aspect.attributeIterator(); it2.hasNext();) {
                Attribute attribute = (Attribute)it2.next();
                final String name = attribute.getName().trim();
                final String value = attribute.getValue().trim();
                if (name.equals("name")) {
                    aspectDef.setName(value);
                }
                else if (name.equals("extends")) {
                    aspectDef.setExtends(value);
                    continue;
                }
            }

            parsePointcutElements(aspect, aspectDef, packageName);
            parseControllerElements(aspect, aspectDef);
            parseIntroduceElements(aspect, aspectDef, packageName);
            parseAdviseElements(aspect, aspectDef);

            handleAbstractAspectDependencies(aspectDef, definition);

            definition.addAspect(aspectDef);
        }
    }

    /**
     * Handles the abstract dependencies for a concrete aspect.
     *
     * @param aspectDef the aspect definition
     * @param definition the aspectwerkz definition
     */
    private static void handleAbstractAspectDependencies(final AspectDefinition aspectDef,
                                                         final AspectWerkzDefinition definition) {
        String extendsRef = aspectDef.getExtends();
        if (extendsRef != null) {
            final AspectDefinition abstractAspect = definition.getAbstractAspectDefinition(extendsRef);
            if (abstractAspect == null) {
                throw new DefinitionException("abstract aspect <" + aspectDef.getExtends() + "> is not defined");
            }
            for (Iterator it = abstractAspect.getPointcutDefs().iterator(); it.hasNext();) {
                final PointcutDefinition pointcutDef = (PointcutDefinition)it.next();
                aspectDef.addPointcutDef(pointcutDef);
            }
            for (Iterator it = abstractAspect.getAdviceWeavingRules().iterator(); it.hasNext();) {
                final AdviceWeavingRule weavingRule = (AdviceWeavingRule)it.next();
                for (Iterator it2 = aspectDef.getPointcutDefs().iterator(); it2.hasNext();) {
                    addPointcutPattern((PointcutDefinition)it2.next(), weavingRule);
                }
                aspectDef.addAdviceWeavingRule(weavingRule);
            }
            for (Iterator it = abstractAspect.getIntroductionWeavingRules().iterator(); it.hasNext();) {
                final IntroductionWeavingRule weavingRule = (IntroductionWeavingRule)it.next();
                aspectDef.addIntroductionWeavingRule(weavingRule);
            }
        }
    }

    /**
     * Parses the pointcut elements.
     *
     * @param aspect the aspect element
     * @param aspectDef the aspect definition
     * @param packageName the name of the package
     */
    private static void parsePointcutElements(final Element aspect,
                                              final AspectDefinition aspectDef,
                                              final String packageName) {
        for (Iterator it2 = aspect.elementIterator(); it2.hasNext();) {
            final Element nestedAdviceElement = (Element)it2.next();
            if (nestedAdviceElement.getName().trim().equals("pointcut-def") ||
                    nestedAdviceElement.getName().trim().equals("pointcut")) {
                try {
                    final PointcutDefinition pointcutDef = new PointcutDefinition();

                    for (Iterator it3 = nestedAdviceElement.attributeIterator(); it3.hasNext();) {
                        Attribute attribute = (Attribute)it3.next();
                        final String name = attribute.getName().trim();
                        final String value = attribute.getValue().trim();
                        if (name.equals("name")) {
                            pointcutDef.setName(value);
                        }
                        else if (name.equals("type")) {
                            pointcutDef.setType(value);
                        }
                    }

                    // handle the pointcut pattern, split the pattern in a class A
                    // a method/field/throws/callerside pattern
                    final String pattern = nestedAdviceElement.attributeValue("pattern");
                    try {
                        if (pointcutDef.getType().equalsIgnoreCase(PointcutDefinition.METHOD) ||
                                pointcutDef.getType().equalsIgnoreCase(PointcutDefinition.CFLOW)) {
                            createMethodPattern(pattern, pointcutDef, packageName);
                        }
                        else if (pointcutDef.getType().equalsIgnoreCase(PointcutDefinition.GET_FIELD) ||
                                pointcutDef.getType().equalsIgnoreCase(PointcutDefinition.SET_FIELD)) {
                            createFieldPattern(pattern, pointcutDef, packageName);
                        }
                        else if (pointcutDef.getType().equalsIgnoreCase(PointcutDefinition.THROWS)) {
                            createThrowsPattern(pattern, pointcutDef, packageName);
                        }
                        else if (pointcutDef.getType().equalsIgnoreCase(PointcutDefinition.CALLER_SIDE)) {
                            createCallerSidePattern(pattern, pointcutDef, packageName);
                        }
                    }
                    catch (Exception e) {
                        throw new WrappedRuntimeException(e);
                    }
                    aspectDef.addPointcutDef(pointcutDef);
                }
                catch (Exception e) {
                    throw new DefinitionException("pointcut definition in aspect " + aspectDef.getName() + " is not well-formed: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Parses the controller elements.
     *
     * @param aspect the aspect element
     * @param aspectDef the aspect definition
     * @param packageName the package name
     */
    private static void parseControllerElements(final Element aspect,
                                                final AspectDefinition aspectDef) {
        for (Iterator it2 = aspect.elementIterator(); it2.hasNext();) {
            final Element nestedAdviceElement = (Element)it2.next();
            if (nestedAdviceElement.getName().trim().equals("controller-def") ||
                    nestedAdviceElement.getName().trim().equals("controller")) {
                try {
                    final ControllerDefinition controllerDef = new ControllerDefinition();

                    for (Iterator it3 = nestedAdviceElement.attributeIterator(); it3.hasNext();) {
                        Attribute attribute = (Attribute)it3.next();
                        final String name = attribute.getName().trim();
                        final String value = attribute.getValue().trim();
                        if (name.equals("pointcut") || name.equals("expression")) {
                            controllerDef.setExpression(value);
                        }
                        else if (name.equals("class")) {
                            controllerDef.setClassName(value);
                        }
                    }
                    // add the pointcut patterns to simplify the matching
                    if (!aspectDef.isAbstract()) {
                        for (Iterator it = aspectDef.getPointcutDefs().iterator(); it.hasNext();) {
                            final PointcutDefinition pointcutDef = (PointcutDefinition)it.next();
                            if (pointcutDef.getType().equalsIgnoreCase(PointcutDefinition.METHOD)) {
                                controllerDef.addMethodPointcutPattern(pointcutDef);
                            }
                        }
                    }
                    aspectDef.addControllerDef(controllerDef);
                }
                catch (Exception e) {
                    throw new DefinitionException("controller definition in aspect " + aspectDef.getName() + " is not well-formed: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Parses the introduce elements.
     *
     * @param aspect the aspect element
     * @param aspectDef the aspect definition
     * @param packageName the name of the package
     */
    private static void parseIntroduceElements(final Element aspect,
                                               final AspectDefinition aspectDef,
                                               final String packageName) {
        for (Iterator it2 = aspect.elementIterator(); it2.hasNext();) {
            final Element nestedAdviceElement = (Element)it2.next();
            if (nestedAdviceElement.getName().trim().equals("introduce") ||
                    nestedAdviceElement.getName().trim().equals("introduction")) {
                try {
                    final IntroductionWeavingRule introWeavingRule = new IntroductionWeavingRule();

                    for (Iterator it3 = nestedAdviceElement.attributeIterator(); it3.hasNext();) {
                        Attribute attribute = (Attribute)it3.next();
                        final String name = attribute.getName().trim();
                        final String value = attribute.getValue().trim();
                        if (name.equals("class")) {
                            introWeavingRule.setClassPattern(packageName + value);
                        }
                        else if (name.equals("introduction-ref")) {
                            introWeavingRule.addIntroductionRef(value);
                            break; // if introduction-ref as an attribute => no more introductions
                        }
                    }
                    parseIntroductionWeavingRuleNestedElements(nestedAdviceElement, introWeavingRule);
                    aspectDef.addIntroductionWeavingRule(introWeavingRule);
                }
                catch (Exception e) {
                    throw new DefinitionException("introduction definition in aspect " + aspectDef.getName() + " is not well-formed: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Parses the advise elements.
     *
     * @param aspect the aspect element
     * @param aspectDef the aspect definition
     */
    private static void parseAdviseElements(final Element aspect,
                                            final AspectDefinition aspectDef) {
        for (Iterator it2 = aspect.elementIterator(); it2.hasNext();) {
            final Element nestedAdviceElement = (Element)it2.next();
            if (nestedAdviceElement.getName().trim().equals("advise") ||
                    nestedAdviceElement.getName().trim().equals("advice")) {
                try {
                    final AdviceWeavingRule adviceWeavingRule = new AdviceWeavingRule();

                    for (Iterator it3 = nestedAdviceElement.attributeIterator(); it3.hasNext();) {
                        Attribute attribute = (Attribute)it3.next();
                        final String name = attribute.getName().trim();
                        final String value = attribute.getValue().trim();
                        if (name.equals("cflow")) {
                            adviceWeavingRule.setCFlowExpression(value);
                        }
                        else if (name.equals("pointcut") || name.equals("expression")) {
                            adviceWeavingRule.setExpression(value);
                        }
                        else if (name.equals("advice-ref")) {
                            adviceWeavingRule.addAdviceRef(value);
                            break; // if advice-ref as an attribute => no more advices
                        }
                    }
                    parseAdviceWeavingRuleNestedElements(nestedAdviceElement, adviceWeavingRule);
                    aspectDef.addAdviceWeavingRule(adviceWeavingRule);

                    // add the pointcut patterns to simplify the matching
                    if (!aspectDef.isAbstract()) {
                        for (Iterator it = aspectDef.getPointcutDefs().iterator(); it.hasNext();) {
                            addPointcutPattern((PointcutDefinition)it.next(), adviceWeavingRule);
                        }
                    }
                }
                catch (Exception e) {
                    throw new DefinitionException("advice definition in aspect " + aspectDef.getName() + " is not well-formed: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Adds a pointcut pattern to the weaving rule.
     *
     * @param pointcutDef the pointcut definition
     * @param adviceWeavingRule the weaving rule
     */
    private static void addPointcutPattern(final PointcutDefinition pointcutDef,
                                           final AdviceWeavingRule adviceWeavingRule) {
        if (pointcutDef.getType().equalsIgnoreCase(PointcutDefinition.METHOD)) {
            adviceWeavingRule.addMethodPointcutPattern(pointcutDef);
        }
        else if (pointcutDef.getType().equalsIgnoreCase(PointcutDefinition.SET_FIELD)) {
            adviceWeavingRule.addSetFieldPointcutPattern(pointcutDef);
        }
        else if (pointcutDef.getType().equalsIgnoreCase(PointcutDefinition.GET_FIELD)) {
            adviceWeavingRule.addGetFieldPointcutPattern(pointcutDef);
        }
        else if (pointcutDef.getType().equalsIgnoreCase(PointcutDefinition.THROWS)) {
            adviceWeavingRule.addThrowsPointcutPattern(pointcutDef);
        }
        else if (pointcutDef.getType().equalsIgnoreCase(PointcutDefinition.CALLER_SIDE)) {
            adviceWeavingRule.addCallerSidePointcutPattern(pointcutDef);
        }
        else if (pointcutDef.getType().equalsIgnoreCase(PointcutDefinition.CFLOW)) {
            adviceWeavingRule.addMethodPointcutPattern(pointcutDef);
        }
    }

    /**
     * Parses the nested <tt>introduction-ref</tt> A <tt>advice-ref</tt> elements.
     *
     * @param introductionElement the root introduction element
     * @param introWeavingRule the IntroducutionWeavingRule definition
     */
    private static void parseIntroductionWeavingRuleNestedElements(
            final Element introductionElement,
            final IntroductionWeavingRule introWeavingRule) {
        for (Iterator it = introductionElement.elementIterator(); it.hasNext();) {
            Element nestedElement = (Element)it.next();
            if (nestedElement.getName().trim().equals("introduction-ref")) {
                introWeavingRule.addIntroductionRef(nestedElement.attributeValue("name"));
            }
        }
    }

    /**
     * Parses the nested <tt>advice-ref</tt> elements.
     *
     * @param adviceElement the root advice element
     * @param adviceWeavingRule the AdviceWeavingRule definition
     */
    private static void parseAdviceWeavingRuleNestedElements(
            final Element adviceElement,
            final AdviceWeavingRule adviceWeavingRule) {
        for (Iterator it = adviceElement.elementIterator(); it.hasNext();) {
            Element nestedElement = (Element)it.next();
            if (nestedElement.getName().trim().equals("advice-ref")) {
                adviceWeavingRule.addAdviceRef(nestedElement.attributeValue("name"));
            }
            else if (nestedElement.getName().trim().equals("advices-ref")) {
                adviceWeavingRule.addAdviceStackRef(nestedElement.attributeValue("name"));
            }
        }
    }

    /**
     * Parses the <tt>advices</tt> elements.
     *
     * @param root the root element
     * @param definition the definition object
     * @param packageName the package name
     */
    private static void parseAdviceStackElements(final Element root,
                                                 final AspectWerkzDefinition definition,
                                                 final String packageName) {
        for (Iterator it1 = root.elementIterator("advices-def"); it1.hasNext();) {
            final AdviceStackDefinition adviceStackDef = new AdviceStackDefinition();

            Element adviceStack = (Element)it1.next();
            for (Iterator it2 = adviceStack.attributeIterator(); it2.hasNext();) {
                Attribute attribute = (Attribute)it2.next();
                final String name = attribute.getName().trim();
                final String value = attribute.getValue().trim();
                if (name.equals("name")) {
                    adviceStackDef.setName(value);
                }
            }
            for (Iterator it2 = adviceStack.elementIterator(); it2.hasNext();) {
                Element nestedElement = (Element)it2.next();
                if (nestedElement.getName().trim().equals("advice-ref")) {
                    adviceStackDef.addAdvice(nestedElement.attributeValue("name"));
                }
            }
            definition.addAdviceStack(adviceStackDef);
        }
    }

    /**
     * Creates a method pattern and adds it to the pointcut definition.
     *
     * @param pattern the pattern
     * @param pointcutDef the pointcut definition
     * @param packageName the name of the package
     */
    private static void createMethodPattern(final String pattern,
                                            final PointcutDefinition pointcutDef,
                                            final String packageName) {
        int indexFirstSpace = pattern.indexOf(' ');
        String returnType = pattern.substring(0, indexFirstSpace + 1);
        String classNameWithMethodName = pattern.substring(
                indexFirstSpace, pattern.indexOf('(')).trim();
        String parameterTypes = pattern.substring(
                pattern.indexOf('('), pattern.length()).trim();
        int indexLastDot = classNameWithMethodName.lastIndexOf('.');

        final String methodPattern = classNameWithMethodName.substring(
                indexLastDot + 1, classNameWithMethodName.length()).trim();
        final String classPattern = packageName + classNameWithMethodName.substring(0, indexLastDot);

        StringBuffer buf = new StringBuffer();
        buf.append(returnType);
        buf.append(methodPattern);
        buf.append(parameterTypes);
        pointcutDef.setPattern(buf.toString());
        pointcutDef.setClassPattern(classPattern);
    }

    /**
     * Creates a field pattern and adds it to the pointcut definition.
     *
     * @param pattern the pattern
     * @param pointcutDef the pointcut definition
     * @param packageName the name of the package
     */
    private static void createFieldPattern(final String pattern,
                                           final PointcutDefinition pointcutDef,
                                           final String packageName) {
        int indexFirstSpace = pattern.indexOf(' ');
        String fieldType = pattern.substring(0, indexFirstSpace + 1);
        String classNameWithFieldName = pattern.substring(
                indexFirstSpace, pattern.length()).trim();
        int indexLastDot = classNameWithFieldName.lastIndexOf('.');

        final String fieldPattern = classNameWithFieldName.substring(
                indexLastDot + 1, classNameWithFieldName.length()).trim();
        final String classPattern = packageName + classNameWithFieldName.substring(0, indexLastDot).trim();

        StringBuffer buf = new StringBuffer();
        buf.append(fieldType);
        buf.append(fieldPattern);
        pointcutDef.setPattern(buf.toString());
        pointcutDef.setClassPattern(classPattern);
    }

    /**
     * Creates a throws pattern and adds it to the pointcut definition.
     *
     * @param pattern the pattern
     * @param pointcutDef the pointcut definition
     * @param packageName the name of the package
     */
    private static void createThrowsPattern(final String pattern,
                                            final PointcutDefinition pointcutDef,
                                            final String packageName) {
        String classAndMethodName = pattern.substring(0, pattern.indexOf('#')).trim();
        final String exceptionName = pattern.substring(pattern.indexOf('#') + 1).trim();
        int indexFirstSpace = classAndMethodName.indexOf(' ');
        final String returnType = classAndMethodName.substring(0, indexFirstSpace + 1);
        String classNameWithMethodName = classAndMethodName.substring(
                indexFirstSpace, classAndMethodName.indexOf('(')).trim();
        final String parameterTypes = classAndMethodName.substring(
                classAndMethodName.indexOf('('), classAndMethodName.length()).trim();
        int indexLastDot = classNameWithMethodName.lastIndexOf('.');
        final String methodPattern = classNameWithMethodName.substring(
                indexLastDot + 1, classNameWithMethodName.length()).trim();
        final String classPattern = packageName + classNameWithMethodName.substring(0, indexLastDot);

        StringBuffer buf = new StringBuffer();
        buf.append(returnType);
        buf.append(methodPattern);
        buf.append(parameterTypes);
        buf.append('#');
        buf.append(exceptionName);
        pointcutDef.setClassPattern(classPattern);
        pointcutDef.setPattern(buf.toString());
    }

    /**
     * Creates a caller side pattern and adds it to the pointcut definition.
     *
     * @param pattern the pattern
     * @param pointcutDef the pointcut definition
     * @param packageName the name of the package
     */
    private static void createCallerSidePattern(final String pattern,
                                                final PointcutDefinition pointcutDef,
                                                final String packageName) {
        String callerClassPattern = packageName + pattern.substring(0, pattern.indexOf('-')).trim();
        String calleePattern = pattern.substring(pattern.indexOf('>') + 1).trim();
        int indexFirstSpace = calleePattern.indexOf(' ');
        String returnType = calleePattern.substring(0, indexFirstSpace + 1);
        String classNameWithMethodName = calleePattern.substring(
                indexFirstSpace, calleePattern.indexOf('(')).trim();
        String parameterTypes = calleePattern.substring(
                calleePattern.indexOf('('), calleePattern.length()).trim();
        int indexLastDot = classNameWithMethodName.lastIndexOf('.');
        String calleeMethodPattern = classNameWithMethodName.substring(
                indexLastDot + 1, classNameWithMethodName.length()).trim();
        String calleeClassPattern = packageName + classNameWithMethodName.substring(0, indexLastDot);
        calleeMethodPattern = returnType + calleeMethodPattern + parameterTypes;
        StringBuffer buf = new StringBuffer();
        buf.append(calleeClassPattern);
        buf.append('#');
        buf.append(calleeMethodPattern);
        pointcutDef.setPattern(buf.toString());
        pointcutDef.setClassPattern(callerClassPattern);
    }

    /**
     * Retrieves and returns the base package.
     *
     * @param root the root element
     * @return the base package
     */
    private static String getBasePackage(final Element root) {
        String basePackage = "";
        for (Iterator it2 = root.attributeIterator(); it2.hasNext();) {
            Attribute attribute = (Attribute)it2.next();
            if (attribute.getName().trim().equals("base-package")) {
                basePackage = attribute.getValue().trim();
                if (basePackage.endsWith(".*")) {
                    basePackage = basePackage.substring(0, basePackage.length() - 1);
                }
                else if (basePackage.endsWith(".")) {
                    // skip
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

    /**
     * Retrieves and returns the package.
     *
     * @param packageElement the package element
     * @return the package
     */
    private static String getPackage(final Element packageElement) {
        String packageName = "";
        for (Iterator it2 = packageElement.attributeIterator(); it2.hasNext();) {
            Attribute attribute = (Attribute)it2.next();
            if (attribute.getName().trim().equals("name")) {
                packageName = attribute.getValue().trim();
                if (packageName.endsWith(".*")) {
                    packageName = packageName.substring(0, packageName.length() - 1);
                }
                else if (packageName.endsWith(".")) {
                    // skip
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
}
