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

import org.dom4j.Document;
import org.dom4j.Attribute;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.exception.DefinitionException;

/**
 * Parses the XML definition file using <tt>dom4j</tt>.
 *
 * @todo Implement the abstract factory pattern for the XML definition parser
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: Dom4jXmlDefinitionParser.java,v 1.3 2003-06-09 07:04:13 jboner Exp $
 */
public class Dom4jXmlDefinitionParser {

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
        catch (Exception e) {
            throw new DefinitionException("XML definition file <" + definitionFile + "> does not exist");
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
        for (Iterator it1 = root.elementIterator("introduction"); it1.hasNext();) {
            IntroductionDefinition introDef = new IntroductionDefinition();

            Element introduction = (Element)it1.next();
            for (Iterator it2 = introduction.attributeIterator(); it2.hasNext();) {
                Attribute attribute = (Attribute)it2.next();

                String name = attribute.getName().trim();
                String value = attribute.getValue().trim();
                if (name.equals("name")) {
                    introDef.setName(value);
                    continue;
                }
                else if (name.equals("interface")) {
                    introDef.setInterface(value);
                    continue;
                }
                else if (name.equals("implementation")) {
                    introDef.setImplementation(value);
                    continue;
                }
                else if (name.equals("deploymentModel") ||
                        name.equals("deployment-model")) {
                    introDef.setDeploymentModel(value);
                    continue;
                }
                else if (name.equals("persistent")) {
                    introDef.setIsPersistent(value);
                    continue;
                }
                else if (name.equals("attribute")) {
                    introDef.setAttribute(value);
                    continue;
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
        for (Iterator it1 = root.elementIterator("advice"); it1.hasNext();) {
            AdviceDefinition adviceDef = new AdviceDefinition();

            Element advice = (Element)it1.next();
            for (Iterator it2 = advice.attributeIterator(); it2.hasNext();) {
                Attribute attribute = (Attribute)it2.next();

                String name = attribute.getName().trim();
                String value = attribute.getValue().trim();
                if (name.equals("name")) {
                    adviceDef.setName(value);
                    continue;
                }
                else if (name.equals("advice") || (name.equals("class"))) {
                    adviceDef.setAdvice(value);
                    continue;
                }
                else if (name.equals("deployment-model") ||
                        name.equals("deploymentModel")) {
                    adviceDef.setDeploymentModel(value);
                    continue;
                }
                else if (name.equals("persistent")) {
                    adviceDef.setIsPersistent(value);
                    continue;
                }
                else if (name.equals("attribute")) {
                    adviceDef.setAttribute(value);
                    continue;
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
        for (Iterator it1 = root.elementIterator("aspect"); it1.hasNext();) {
            final AspectDefinition aspectDef = new AspectDefinition();

            final Element aspect = (Element)it1.next();
            for (Iterator it2 = aspect.attributeIterator(); it2.hasNext();) {
                Attribute attribute = (Attribute)it2.next();
                String name = attribute.getName().trim();
                String value = attribute.getValue().trim();
                if (name.equals("class")) {
                    aspectDef.setPattern(value);
                    continue;
                }
            }

            for (Iterator it2 = aspect.elementIterator(); it2.hasNext();) {
                Element nestedAdviceElement = (Element)it2.next();

                if (nestedAdviceElement.getName().trim().equals("introduction-ref")) {
                    aspectDef.addIntroduction(nestedAdviceElement.attributeValue("name"));
                }
                else if (nestedAdviceElement.getName().trim().equals("pointcut")) {
                    PointcutDefinition pointcutDef = new PointcutDefinition();

                    for (Iterator it3 = nestedAdviceElement.attributeIterator();
                         it3.hasNext();) {
                        Attribute attribute = (Attribute)it3.next();
                        String name = attribute.getName().trim();
                        String value = attribute.getValue().trim();
                        if (name.equals("type")) {
                            pointcutDef.setType(value);
                            continue;
                        }
                        else if (name.equals("pattern")) {
                            pointcutDef.addPattern(value);
                            continue;
                        }
                        else if (name.equals("threadSafe")) {
                            pointcutDef.setThreadSafe(value);
                            continue;
                        }
                    }
                    parsePointcutNestedElements(nestedAdviceElement, pointcutDef);
                    aspectDef.addPointcut(pointcutDef);
                }
            }

            definition.addAspect(aspectDef);
        }
    }

    /**
     * Parses the nested <tt>advice</tt> and <tt>pattern</tt> elements.
     *
     * @param pointcutElement the root pointcutElement
     * @param pointcutDef the pointcut definition object
     */
    private static void parsePointcutNestedElements(
            final Element pointcutElement,
            final PointcutDefinition pointcutDef) {

        for (Iterator it = pointcutElement.elementIterator(); it.hasNext();) {
            Element nestedElement = (Element)it.next();

            if (nestedElement.getName().trim().equals("advice-ref")) {
                pointcutDef.addAdvice(nestedElement.attributeValue("name"));
            }
            else if (nestedElement.getName().trim().equals("advices-ref")) {
                pointcutDef.addAdviceStack(nestedElement.attributeValue("name"));
            }
            else if (nestedElement.getName().trim().equals("pattern")) {
                pointcutDef.addPattern(nestedElement.getTextTrim());
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
        for (Iterator it1 = root.elementIterator("advices"); it1.hasNext();) {
            AdviceStackDefinition adviceStackDef = new AdviceStackDefinition();

            Element adviceStack = (Element)it1.next();
            for (Iterator it2 = adviceStack.attributeIterator(); it2.hasNext();) {
                Attribute attribute = (Attribute)it2.next();

                String name = attribute.getName().trim();
                String value = attribute.getValue().trim();
                if (name.equals("name")) {
                    adviceStackDef.setName(value);
                    continue;
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
