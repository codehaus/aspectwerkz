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

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Collection;
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
 * @version $Id: XmlDefinitionParser.java,v 1.2 2003-06-17 16:07:54 jboner Exp $
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
    private static File s_timestamp = new File(
            META_DATA_DIR + File.separator + "definition_timestamp");

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
    public static AspectWerkzDefinition parse(final File definitionFile,
                                              boolean isDirty) {
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
     * @return the definition object
     */
    private static AspectWerkzDefinition parseDocument(final Document document) {
        final AspectWerkzDefinition definition = new AspectWerkzDefinition();

        final Element root = document.getRootElement();

        parseIntroductionElements(root, definition);
        parseAdviceElements(root, definition);
        parseAdviceStackElements(root, definition);
        parseAspectElements(root, definition);

        return definition;
    }

    /**
     * Parses the <tt>introduction</tt> elements.
     *
     * @param root the root element
     * @param definition the definition object
     */
    private static void parseIntroductionElements(
            final Element root,
            final AspectWerkzDefinition definition) {
        for (Iterator it1 = root.elementIterator("introduction-def"); it1.hasNext();) {
            IntroductionDefinition introDef = new IntroductionDefinition();

            Element introduction = (Element)it1.next();
            for (Iterator it2 = introduction.attributeIterator(); it2.hasNext();) {
                Attribute attribute = (Attribute)it2.next();

                String name = attribute.getName().trim();
                String value = attribute.getValue().trim();
                if (name.equals("name")) {
                    introDef.setName(value);
                }
                else if (name.equals("interface")) {
                    introDef.setInterface(value);
                }
                else if (name.equals("implementation")) {
                    introDef.setImplementation(value);
                }
                else if (name.equals("deploymentModel") ||
                        name.equals("deployment-model")) {
                    introDef.setDeploymentModel(value);
                }
                else if (name.equals("persistent")) {
                    introDef.setIsPersistent(value);
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
     */
    private static void parseAdviceElements(
            final Element root,
            final AspectWerkzDefinition definition) {
        for (Iterator it1 = root.elementIterator("advice-def"); it1.hasNext();) {
            AdviceDefinition adviceDef = new AdviceDefinition();

            Element advice = (Element)it1.next();
            for (Iterator it2 = advice.attributeIterator(); it2.hasNext();) {
                Attribute attribute = (Attribute)it2.next();
                String name = attribute.getName().trim();
                String value = attribute.getValue().trim();
                if (name.equals("name")) {
                    adviceDef.setName(value);
                }
                else if (name.equals("advice") || (name.equals("class"))) {
                    adviceDef.setAdvice(value);
                }
                else if (name.equals("deployment-model") ||
                        name.equals("deploymentModel")) {
                    adviceDef.setDeploymentModel(value);
                }
                else if (name.equals("persistent")) {
                    adviceDef.setIsPersistent(value);
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
     */
    private static void parseAspectElements(
            final Element root,
            final AspectWerkzDefinition definition) {
        for (Iterator it1 = root.elementIterator("abstract-aspect"); it1.hasNext();) {
            final AspectDefinition aspectDef = new AspectDefinition();
            final Element aspect = (Element)it1.next();
            for (Iterator it2 = aspect.attributeIterator(); it2.hasNext();) {
                Attribute attribute = (Attribute)it2.next();
                String name = attribute.getName().trim();
                String value = attribute.getValue().trim();
                if (name.equals("name")) {
                    aspectDef.setName(value);
                }
            }
            parseAspectNestedElements(aspect, aspectDef);
            definition.addAbstractAspect(aspectDef);
        }
        for (Iterator it1 = root.elementIterator("aspect"); it1.hasNext();) {
            final AspectDefinition aspectDef = new AspectDefinition();
            final Element aspect = (Element)it1.next();
            for (Iterator it2 = aspect.attributeIterator(); it2.hasNext();) {
                Attribute attribute = (Attribute)it2.next();
                String name = attribute.getName().trim();
                String value = attribute.getValue().trim();
                if (name.equals("name")) {
                    aspectDef.setName(value);
                }
                else if (name.equals("extends")) {
                    aspectDef.setExtends(value);
                    continue;
                }
            }

            // handle the abstract advice dependencies
            String extendsRef = aspectDef.getExtends();
            if (extendsRef != null) {
                AspectDefinition abstractAspect =
                        definition.getAbstractAspectDefinition(extendsRef);
                if (abstractAspect == null) {
                    throw new DefinitionException("abstract aspect <" + aspectDef.getExtends() + "> is not defined");
                }
                for (Iterator it = abstractAspect.getPointcuts().iterator(); it.hasNext();) {
                    aspectDef.addPointcut((PointcutDefinition)it.next());
                }
                for (Iterator it = abstractAspect.getAdviceWeavingRules().iterator(); it.hasNext();) {
                    aspectDef.addAdviceWeavingRule((AdviceWeavingRule)it.next());
                }
                for (Iterator it = abstractAspect.getIntroductionWeavingRules().iterator(); it.hasNext();) {
                    aspectDef.addIntroductionWeavingRule((IntroductionWeavingRule)it.next());
                }
            }
            parseAspectNestedElements(aspect, aspectDef);
            definition.addAspect(aspectDef);
        }
    }

    /**
     * Parses the nested aspect elements.
     *
     * @param aspect the aspect element
     * @param aspectDef the aspect definition
     */
    private static void parseAspectNestedElements(
            final Element aspect,
            final AspectDefinition aspectDef) {
        for (Iterator it2 = aspect.elementIterator(); it2.hasNext();) {
            Element nestedAdviceElement = (Element)it2.next();
            if (nestedAdviceElement.getName().trim().equals("pointcut-def") ||
                    nestedAdviceElement.getName().trim().equals("pointcut")) {
                parsePointcutElements(nestedAdviceElement, aspectDef);
            }
            else if (nestedAdviceElement.getName().trim().equals("introduce") ||
                    nestedAdviceElement.getName().trim().equals("introduction")) {
                parseIntroduceElements(nestedAdviceElement, aspectDef);
            }
            else if (nestedAdviceElement.getName().trim().equals("advise") ||
                    nestedAdviceElement.getName().trim().equals("advice")) {
                parseAdviseElements(nestedAdviceElement, aspectDef);
            }
        }
    }

    /**
     * Parses the <tt>pointcut</tt> elements.
     *
     * @param element the element
     * @param aspectDef the aspect definition
     */
    private static void parsePointcutElements(
            final Element element,
            final AspectDefinition aspectDef) {
        PointcutDefinition pointcutDefinition = new PointcutDefinition();

        for (Iterator it3 = element.attributeIterator();
             it3.hasNext();) {
            Attribute attribute = (Attribute)it3.next();
            String name = attribute.getName().trim();
            String value = attribute.getValue().trim();
            if (name.equals("name")) {
                pointcutDefinition.setName(value);
            }
            else if (name.equals("type")) {
                pointcutDefinition.setType(value);
            }
        }

        // handle the pointcut pattern, split the pattern in a class and
        // a method/field/throws/callerside pattern
        String pattern = element.attributeValue("pattern");
        try {
            if (pointcutDefinition.getType().
                    equalsIgnoreCase(PointcutDefinition.METHOD)) {
                int indexFirstSpace = pattern.indexOf(' ');
                String returnType = pattern.substring(
                        0, indexFirstSpace + 1);
                String classNameWithMethodName = pattern.substring(
                        indexFirstSpace, pattern.indexOf('(')).trim();
                String parameterTypes = pattern.substring(
                        pattern.indexOf('('), pattern.length()).trim();
                int indexLastDot = classNameWithMethodName.lastIndexOf('.');
                String methodPattern = classNameWithMethodName.substring(
                        indexLastDot + 1, classNameWithMethodName.length()).trim();
                String classPattern = classNameWithMethodName.substring(
                        0, indexLastDot);

                StringBuffer buf = new StringBuffer();
                buf.append(returnType);
                buf.append(methodPattern);
                buf.append(parameterTypes);
                pointcutDefinition.setPattern(buf.toString());
                pointcutDefinition.setClassPattern(classPattern);
            }
            else if (pointcutDefinition.getType().
                    equalsIgnoreCase(PointcutDefinition.GET_FIELD) ||
                    pointcutDefinition.getType().
                    equalsIgnoreCase(PointcutDefinition.SET_FIELD)) {
                int indexFirstSpace = pattern.indexOf(' ');
                String fieldType = pattern.substring(
                        0, indexFirstSpace + 1);
                String classNameWithFieldName = pattern.substring(
                        indexFirstSpace, pattern.length()).trim();
                int indexLastDot = classNameWithFieldName.lastIndexOf('.');
                String fieldPattern = classNameWithFieldName.substring(
                        indexLastDot + 1, classNameWithFieldName.length()).trim();
                String classPattern = classNameWithFieldName.substring(
                        0, indexLastDot).trim();

                StringBuffer buf = new StringBuffer();
                buf.append(fieldType);
                buf.append(fieldPattern);
                pointcutDefinition.setPattern(buf.toString());
                pointcutDefinition.setClassPattern(classPattern);
            }
            else if (pointcutDefinition.getType().
                    equalsIgnoreCase(PointcutDefinition.THROWS)) {
                String classAndMethodName = pattern.substring(
                        0, pattern.indexOf('#')).trim();
                String exceptionName = pattern.substring(
                        pattern.indexOf('#') + 1).trim();

                int indexFirstSpace = classAndMethodName.indexOf(' ');
                String returnType = classAndMethodName.substring(
                        0, indexFirstSpace + 1);
                String classNameWithMethodName = classAndMethodName.substring(
                        indexFirstSpace, classAndMethodName.indexOf('(')).trim();
                String parameterTypes = classAndMethodName.substring(
                        classAndMethodName.indexOf('('), classAndMethodName.length()).trim();
                int indexLastDot = classNameWithMethodName.lastIndexOf('.');
                String methodPattern = classNameWithMethodName.substring(
                        indexLastDot + 1, classNameWithMethodName.length()).trim();
                String classPattern = classNameWithMethodName.substring(
                        0, indexLastDot);

                StringBuffer buf = new StringBuffer();
                buf.append(returnType);
                buf.append(methodPattern);
                buf.append(parameterTypes);
                buf.append('#');
                buf.append(exceptionName);
                pointcutDefinition.setClassPattern(classPattern);
                pointcutDefinition.setPattern(buf.toString());
            }
            else if (pointcutDefinition.getType().
                    equalsIgnoreCase(PointcutDefinition.CALLER_SIDE)) {
                String callerClassPattern = pattern.substring(
                        0, pattern.indexOf('-')).trim();
                String calleePattern = pattern.substring(
                        pattern.indexOf('>') + 1).trim();
                int indexFirstSpace = calleePattern.indexOf(' ');
                String returnType = calleePattern.substring(
                        0, indexFirstSpace + 1);
                String classNameWithMethodName = calleePattern.substring(
                        indexFirstSpace, calleePattern.indexOf('(')).trim();
                String parameterTypes = calleePattern.substring(
                        calleePattern.indexOf('('), calleePattern.length()).trim();
                int indexLastDot = classNameWithMethodName.lastIndexOf('.');
                String calleeMethodPattern = classNameWithMethodName.substring(
                        indexLastDot + 1, classNameWithMethodName.length()).trim();
                String calleeClassPattern = classNameWithMethodName.substring(
                        0, indexLastDot);
                calleeMethodPattern = returnType + calleeMethodPattern + parameterTypes;

                StringBuffer buf = new StringBuffer();
                buf.append(calleeClassPattern);
                buf.append('#');
                buf.append(calleeMethodPattern);
                pointcutDefinition.setPattern(buf.toString());
                pointcutDefinition.setClassPattern(callerClassPattern);
            }
        }
        catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
        aspectDef.addPointcut(pointcutDefinition);
    }

    /**
     * Parses the <tt>introduce</tt> elements.
     *
     * @param element the element
     * @param aspectDef the aspect definition
     */
    private static void parseIntroduceElements(
            final Element element,
            final AspectDefinition aspectDef) {
        IntroductionWeavingRule introWeavingRule = new IntroductionWeavingRule();

        for (Iterator it3 = element.attributeIterator();
             it3.hasNext();) {
            Attribute attribute = (Attribute)it3.next();
            String name = attribute.getName().trim();
            String value = attribute.getValue().trim();
            if (name.equals("class")) {
                introWeavingRule.setClassPattern(value);
            }
            else if (name.equals("introduction-ref")) {
                introWeavingRule.addIntroductionRef(value);
                break; // if introduction-ref as an attribute => no more introductions
            }
        }
        parseIntroductionWeavingRuleNestedElements(
                element, introWeavingRule);
        aspectDef.addIntroductionWeavingRule(introWeavingRule);
    }

    /**
     * Parses the <tt>advise</tt> elements.
     *
     * @param element the element
     * @param aspectDef the aspect definition
     */
    private static void parseAdviseElements(
            final Element element,
            final AspectDefinition aspectDef) {
        AdviceWeavingRule adviceWeavingRule = new AdviceWeavingRule();

        for (Iterator it3 = element.attributeIterator();
             it3.hasNext();) {
            Attribute attribute = (Attribute)it3.next();
            String name = attribute.getName().trim();
            String value = attribute.getValue().trim();
            if (name.equals("pointcut") || name.equals("expression")) {
                adviceWeavingRule.setExpression(value);
            }
            else if (name.equals("advice-ref")) {
                adviceWeavingRule.addAdviceRef(value);
                break; // if advice-ref as an attribute => no more advices
            }
        }
        parseAdviceWeavingRuleNestedElements(
                element, adviceWeavingRule);
        aspectDef.addAdviceWeavingRule(adviceWeavingRule);
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
     */
    private static void parseAdviceStackElements(
            final Element root,
            final AspectWerkzDefinition definition) {
        for (Iterator it1 = root.elementIterator("advices-def"); it1.hasNext();) {
            AdviceStackDefinition adviceStackDef = new AdviceStackDefinition();

            Element adviceStack = (Element)it1.next();
            for (Iterator it2 = adviceStack.attributeIterator(); it2.hasNext();) {
                Attribute attribute = (Attribute)it2.next();
                String name = attribute.getName().trim();
                String value = attribute.getValue().trim();
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
}
