/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.attribdef.definition;

import java.util.Iterator;

import org.dom4j.Attribute;
import org.dom4j.Element;

import org.codehaus.aspectwerkz.System;
import org.codehaus.aspectwerkz.definition.AspectWerkzDefinition;

/**
 * Parses the attribdef XML definition using <tt>dom4j</tt>.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class DocumentParser {

    /**
     * Parses the <tt>system</tt> elements.
     *
     * @param systemElement the system element
     * @param basePackage the base package
     * @return the definition for the system
     */
    public static AspectWerkzDefinition parseSystemElement(final Element systemElement,
                                                           final String basePackage) {
        final AspectWerkzDefinitionImpl definition = new AspectWerkzDefinitionImpl();

        String uuid = systemElement.attributeValue("id");
        if (uuid == null || uuid.equals("")) {
            // TODO: log a warning "no id specified in the definition, using default (AspectWerkz.DEFAULT_SYSTEM)"
            uuid = System.DEFAULT_SYSTEM;
        }
        definition.setUuid(uuid);

        // parse the include and exclude elements
        org.codehaus.aspectwerkz.definition.DocumentParser.parseIncludePackageElements(
                systemElement, definition, basePackage
        );
        org.codehaus.aspectwerkz.definition.DocumentParser.parseExcludePackageElements(
                systemElement, definition, basePackage
        );

        boolean hasDef = false;
        // parse useaspect elements
        if (parseUseAspectElements(systemElement, definition, basePackage)) {
            hasDef = true;
        }

        // parse with package elements
        if (parsePackageElements(systemElement, definition, basePackage)) {
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
     * @param systemElement the system element
     * @param definition the definition
     * @param basePackage the base package
     * @return flag that says if we have a definition of this kind or not
     */
    private static boolean parsePackageElements(final Element systemElement,
                                                final AspectWerkzDefinition definition,
                                                final String basePackage) {
        boolean hasDef = false;
        for (Iterator it1 = systemElement.elementIterator("package"); it1.hasNext();) {
            final Element packageElement = ((Element)it1.next());
            final String packageName = basePackage + getPackage(packageElement);

            if (parseUseAspectElements(packageElement, definition, packageName)) hasDef = true;
        }
        return hasDef;
    }

    /**
     * Parses the <tt>use-aspect</tt> elements.
     *
     * @param systemElement the system element
     * @param definition the definition object
     * @param packageName the package name
     * @return flag that says if we have a definition of this kind or not
     */
    private static boolean parseUseAspectElements(final Element systemElement,
                                                  final AspectWerkzDefinition definition,
                                                  final String packageName) {
        boolean hasDef = false;
        for (Iterator it1 = systemElement.elementIterator("use-aspect"); it1.hasNext();) {

            String className = null;
            Element aspect = (Element)it1.next();
            for (Iterator it2 = aspect.attributeIterator(); it2.hasNext();) {
                Attribute attribute = (Attribute)it2.next();

                final String name = attribute.getName().trim();
                final String value = attribute.getValue().trim();
                if (name.equals("class")) {
                    className = value;
                    break;
                }
            }
            String aspectClassName = packageName + className;
            AspectWerkzDefinitionImpl def = (AspectWerkzDefinitionImpl)definition;
            for (Iterator it2 = aspect.elementIterator(); it2.hasNext();) {
                Element nestedAdviceElement = (Element)it2.next();
                if (nestedAdviceElement.getName().trim().equals("param")) {
                    def.addParameter(
                            aspectClassName,
                            nestedAdviceElement.attributeValue("name"),
                            nestedAdviceElement.attributeValue("value")
                    );
                }
            }
            definition.addAspectToUse(aspectClassName);
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
