/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
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
        return parseSystemElements(root, getBasePackage(root));
    }

    /**
     * Parses the <tt>system</tt> elements.
     *
     * @param root the root element
     * @param definition the definition
     * @param basePackage the base package
     */
    private static List parseSystemElements(final Element root,
                                            final String basePackage) {
        final List systemDefs = new ArrayList();

        // parse the xmldef definitions
        for (Iterator it1 = root.elementIterator("system"); it1.hasNext();) {
            AspectWerkzDefinition definition =
                    org.codehaus.aspectwerkz.xmldef.definition.DocumentParser.parseSystemElement(
                            ((Element)it1.next()), basePackage
                    );
            if (definition != null) {
                systemDefs.add(definition);
            }
        }

        // parse the attribdef definitions
        for (Iterator it1 = root.elementIterator("system"); it1.hasNext();) {
            AspectWerkzDefinition definition =
                    org.codehaus.aspectwerkz.attribdef.definition.DocumentParser.parseSystemElement(
                            ((Element)it1.next()), basePackage
                    );
            if (definition != null) {
                systemDefs.add(definition);
            }
        }

        return systemDefs;
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
