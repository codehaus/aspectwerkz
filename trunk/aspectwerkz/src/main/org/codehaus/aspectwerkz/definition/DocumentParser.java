/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import org.dom4j.Document;
import org.dom4j.Attribute;
import org.dom4j.Element;

/**
 * Parses the XML definition file using <tt>dom4j</tt>.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class DocumentParser {

    /**
     * Parses the definition DOM document.
     *
     * @param document the defintion as a document
     * @return the definitions
     */
    public static List parse(final Document document) {
        final Element root = document.getRootElement();

        // parse the transformation scopes
        return parseSystemElements(root);
    }

    /**
     * Parses the <tt>include</tt> elements.
     *
     * @param root the root element
     * @param definition the definition object
     * @param packageName the package name
     */
    public static void parseIncludePackageElements(final Element root,
                                                   final AspectWerkzDefinition definition,
                                                   final String packageName) {
        for (Iterator it1 = root.elementIterator("include"); it1.hasNext();) {
            String includePackage = "";
            Element includeElement = (Element)it1.next();
            for (Iterator it2 = includeElement.attributeIterator(); it2.hasNext();) {
                Attribute attribute = (Attribute)it2.next();
                if (attribute.getName().trim().equals("package")) {

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
     * @param root the root element
     * @param definition the definition object
     * @param packageName the package name
     */
    public static void parseExcludePackageElements(final Element root,
                                                   final AspectWerkzDefinition definition,
                                                   final String packageName) {
        for (Iterator it1 = root.elementIterator("exclude"); it1.hasNext();) {
            String excludePackage = "";
            Element excludeElement = (Element)it1.next();
            for (Iterator it2 = excludeElement.attributeIterator(); it2.hasNext();) {
                Attribute attribute = (Attribute)it2.next();
                if (attribute.getName().trim().equals("package")) {

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
     * Parses the <tt>system</tt> elements.
     *
     * @param root the root element
     */
    private static List parseSystemElements(final Element root) {
        final List systemDefs = new ArrayList();

        // parse the xmldef definitions
        for (Iterator it1 = root.elementIterator("system"); it1.hasNext();) {
            Element system = (Element)it1.next();
            AspectWerkzDefinition definition =
                    org.codehaus.aspectwerkz.xmldef.definition.DocumentParser.parseSystemElement(
                            system, getBasePackage(system)
                    );
            if (definition != null) {
                systemDefs.add(definition);
            }
        }

        // parse the attribdef definitions
        for (Iterator it1 = root.elementIterator("system"); it1.hasNext();) {
            Element system = (Element)it1.next();
            AspectWerkzDefinition definition =
                    org.codehaus.aspectwerkz.attribdef.definition.DocumentParser.parseSystemElement(
                            system, getBasePackage(system)
                    );
            if (definition != null) {
                systemDefs.add(definition);
            }
        }

        // handle backward compatibility with old XML definition
        if (systemDefs.size() == 0) {
            System.err.println("[AspectWerkz:WARN] using old XML style - please update");
            String uuid = root.attributeValue("id");
            AspectWerkzDefinition definition =
                    org.codehaus.aspectwerkz.xmldef.definition.DocumentParser.parseElements(
                            root, getBasePackage(root), uuid
                    );
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
            if (attribute.getName().trim().equals("base-package")) {
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
