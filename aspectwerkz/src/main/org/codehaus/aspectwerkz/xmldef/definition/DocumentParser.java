/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.xmldef.definition;

import java.util.Iterator;

import org.dom4j.Attribute;
import org.dom4j.Element;

import org.codehaus.aspectwerkz.System;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.exception.DefinitionException;
import org.codehaus.aspectwerkz.definition.PointcutDefinition;
import org.codehaus.aspectwerkz.definition.AspectWerkzDefinition;
import org.codehaus.aspectwerkz.definition.PatternFactory;

/**
 * Parses the xmldef XML definition file using <tt>dom4j</tt>.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class DocumentParser {

    /**
     * Parses the <tt>system</tt> element.
     *
     * @param systemElement the system element
     * @param basePackage the base package
     * @return the definition for the system
     */
    public static AspectWerkzDefinition parseSystemElement(final Element systemElement,
                                                           final String basePackage) {
        String uuid = systemElement.attributeValue("id");
        if (uuid == null || uuid.equals("")) {
            // TODO: LOG a warning "no id specified in the definition, using default (AspectWerkz.DEFAULT_SYSTEM)"
            uuid = System.DEFAULT_SYSTEM;
        }
        return parseElements(systemElement, basePackage, uuid);
    }

    /**
     * Parses the definition elements.
     *
     * @param systemElement the system element
     * @param basePackage
     * @param uuid the definition UUID
     * @return the definition for the system
     */
    public static AspectWerkzDefinition parseElements(final Element systemElement,
                                                      final String basePackage,
                                                      final String uuid) {
        final AspectWerkzDefinitionImpl definition = new AspectWerkzDefinitionImpl();

        definition.setUuid(uuid);

        // parse the include and exclude elements
        org.codehaus.aspectwerkz.definition.DocumentParser.parseIncludePackageElements(
                systemElement, definition, basePackage
        );
        org.codehaus.aspectwerkz.definition.DocumentParser.parseExcludePackageElements(
                systemElement, definition, basePackage
        );

        boolean hasDef = false;
        // parse without package elements
        if (parseIntroductionElements(systemElement, definition, basePackage)) hasDef = true;
        if (parseAdviceElements(systemElement, definition, basePackage)) hasDef = true;
        if (parseAdviceStackElements(systemElement, definition)) hasDef = true;
        if (parseAspectElements(systemElement, definition, basePackage)) hasDef = true;

        // parse with package elements
        if (parsePackageElements(systemElement, definition, basePackage)) hasDef = true;

        if (hasDef) {
            return definition;
        }
        else {
            return null;
        }
    }

    /**
     * Parses the <tt>package</tt> elements.
     *
     * @param systemElement the system element
     * @param definition the definition
     * @param basePackage the base package
     * @return flag that says if we have a definition of this kind or not
     */
    private static boolean parsePackageElements(final Element systemElement,
                                                final AspectWerkzDefinitionImpl definition,
                                                final String basePackage) {
        boolean hasDef = false;
        for (Iterator it1 = systemElement.elementIterator("package"); it1.hasNext();) {
            final Element packageElement = ((Element)it1.next());
            final String packageName = basePackage + getPackage(packageElement);

            // parse without package elements
            if (parseIntroductionElements(packageElement, definition, packageName)) hasDef = true;
            if (parseAdviceElements(packageElement, definition, packageName)) hasDef = true;
            if (parseAdviceStackElements(packageElement, definition)) hasDef = true;
            if (parseAspectElements(packageElement, definition, packageName)) hasDef = true;
        }
        return hasDef;
    }

    /**
     * Parses the <tt>introduction</tt> elements.
     *
     * @param root the root element
     * @param definition the definition object
     * @param packageName the package name
     * @return flag that says if we have a definition of this kind or not
     */
    private static boolean parseIntroductionElements(final Element root,
                                                     final AspectWerkzDefinitionImpl definition,
                                                     final String packageName) {
        boolean hasDef = false;
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
                else if (name.equals("deployment-model")) {
                    introDef.setDeploymentModel(value);
                }
                else if (name.equals("attribute")) {
                    introDef.setAttribute(value);
                }
            }
            definition.addIntroduction(introDef);
            hasDef = true;
        }
        return hasDef;
    }

    /**
     * Parses the <tt>advice</tt> elements.
     *
     * @param root the root element
     * @param definition the definition object
     * @param packageName the package name
     * @return flag that says if we have a definition of this kind or not
     */
    private static boolean parseAdviceElements(final Element root,
                                               final AspectWerkzDefinitionImpl definition,
                                               final String packageName) {
        boolean hasDef = false;
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
                else if (name.equals("deployment-model")) {
                    adviceDef.setDeploymentModel(value);
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
            hasDef = true;
        }
        return hasDef;
    }

    /**
     * Parses the <tt>aspect</tt> elements.
     *
     * @param root the root element
     * @param definition the definition object
     * @param packageName the package name
     * @return flag that says if we have a definition of this kind or not
     */
    private static boolean parseAspectElements(final Element root,
                                               final AspectWerkzDefinitionImpl definition,
                                               final String packageName) {
        boolean hasDef = false;
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
            parseBindIntroductionElements(aspect, aspectDef, packageName);
            parseBindAdviceElements(aspect, aspectDef);

            definition.addAbstractAspect(aspectDef);
            hasDef = true;
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
            parseBindIntroductionElements(aspect, aspectDef, packageName);
            parseBindAdviceElements(aspect, aspectDef);

            handleAbstractAspectDependencies(aspectDef, definition);

            definition.addAspect(aspectDef);
            hasDef = true;
        }
        return hasDef;
    }

    /**
     * Handles the abstract dependencies for a concrete aspect.
     *
     * @param aspectDef the aspect definition
     * @param definition the aspectwerkz definition
     */
    private static void handleAbstractAspectDependencies(final AspectDefinition aspectDef,
                                                         final AspectWerkzDefinitionImpl definition) {
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
                    final PointcutDefinition pointcutDef = new PointcutDefinitionImpl();

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
                        else if (name.equals("non-reentrant")) {
                            pointcutDef.setNonReentrant(value);
                        }
                    }

                    // handle the pointcut pattern, split the pattern in a class and
                    // a method/field/throws/callerside pattern
                    final String pattern = nestedAdviceElement.attributeValue("pattern");
                    try {
                        if (pointcutDef.getType().equalsIgnoreCase(PointcutDefinition.METHOD)) {
                            PatternFactory.createMethodPattern(pattern, pointcutDef, packageName);
                        }
                        else if (pointcutDef.getType().equalsIgnoreCase(PointcutDefinition.CFLOW)) {
                            // make a 'match all caller side classes' pattern out of the regular method pattern
                            PatternFactory.createCallerSidePattern(pattern, pointcutDef, packageName);
                        }
                        else if (pointcutDef.getType().equalsIgnoreCase(PointcutDefinition.GET_FIELD) ||
                                pointcutDef.getType().equalsIgnoreCase(PointcutDefinition.SET_FIELD)) {
                            PatternFactory.createFieldPattern(pattern, pointcutDef, packageName);
                        }
                        else if (pointcutDef.getType().equalsIgnoreCase(PointcutDefinition.THROWS)) {
                            PatternFactory.createThrowsPattern(pattern, pointcutDef, packageName);
                        }
                        else if (pointcutDef.getType().equalsIgnoreCase(PointcutDefinition.CALLER_SIDE)) {
                            PatternFactory.createCallerSidePattern(pattern, pointcutDef, packageName);
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
    private static void parseBindIntroductionElements(final Element aspect,
                                                      final AspectDefinition aspectDef,
                                                      final String packageName) {
        for (Iterator it2 = aspect.elementIterator(); it2.hasNext();) {
            final Element nestedAdviceElement = (Element)it2.next();
            if (nestedAdviceElement.getName().trim().equals("bind-introduction") ||
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
    private static void parseBindAdviceElements(final Element aspect,
                                                final AspectDefinition aspectDef) {
        for (Iterator it2 = aspect.elementIterator(); it2.hasNext();) {
            final Element nestedAdviceElement = (Element)it2.next();
            if (nestedAdviceElement.getName().trim().equals("bind-advice") ||
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
            adviceWeavingRule.addCallerSidePointcutPattern(pointcutDef);
        }
    }

    /**
     * Parses the nested <tt>introduction-ref</tt> and <tt>advice-ref</tt> elements.
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
     * @return flag that says if we have a definition of this kind or not
     */
    private static boolean parseAdviceStackElements(final Element root,
                                                    final AspectWerkzDefinitionImpl definition) {
        boolean hasDef = false;
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
            hasDef = true;
        }
        return hasDef;
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
}
