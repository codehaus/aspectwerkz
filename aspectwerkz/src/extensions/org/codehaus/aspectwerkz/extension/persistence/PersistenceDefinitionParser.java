/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.extension.persistence;

import java.io.File;
import java.util.Iterator;

import org.dom4j.io.SAXReader;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Attribute;

import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;

import org.codehaus.aspectwerkz.extension.persistence.definition.PersistenceDefinition;
import org.codehaus.aspectwerkz.extension.persistence.definition.IndexDefinition;
import org.codehaus.aspectwerkz.extension.persistence.definition.PersistenceManagerDefinition;
import org.codehaus.aspectwerkz.extension.persistence.definition.IndexRefDefinition;
import org.codehaus.aspectwerkz.extension.persistence.definition.ParameterDefinition;
import org.codehaus.aspectwerkz.extension.persistence.definition.PersistentObjectDefinition;

/**
 * Parses the persistence definition file.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class PersistenceDefinitionParser {

    /**
     * The persistence definition.
     */
    private static PersistenceDefinition s_definition;

    /**
     * Parses the XML definition file not using the cache.
     *
     * @param definitionFile the definition file
     * @return the definition object
     */
    public static PersistenceDefinition parse(final File definitionFile) {
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
     * Parses the definition DOM document.
     *
     * @param document the defintion as a document
     * @return the definition object
     */
    private static PersistenceDefinition parseDocument(final Document document) {
        final PersistenceDefinition definition = new PersistenceDefinition();

        final Element root = document.getRootElement();

        parseIndexElements(root, definition);
        parsePersistenceManagerElements(root, definition);
        parsePersistentElements(root, definition);

        return definition;
    }

    /**
     * Parses the <tt>index</tt> elements.
     *
     * @param root the root element
     * @param definition the definition object
     */
    private static void parseIndexElements(
            final Element root,
            final PersistenceDefinition definition) {
        for (Iterator it1 = root.elementIterator("index"); it1.hasNext();) {
            IndexDefinition indexDef = new IndexDefinition();

            Element introduction = (Element)it1.next();
            for (Iterator it2 = introduction.attributeIterator(); it2.hasNext();) {
                Attribute attribute = (Attribute)it2.next();

                String name = attribute.getName().trim();
                String value = attribute.getValue().trim();
                if (name.equals("name")) {
                    indexDef.setName(value);
                    continue;
                }
                else if (name.equals("type")) {
                    indexDef.setType(value);
                    continue;
                }
            }
            definition.addIndex(indexDef);
        }
    }

    /**
     * Parses the <tt>peristence-manager</tt> elements.
     *
     * @param root the root element
     * @param definition the definition object
     */
    private static void parsePersistenceManagerElements(
            final Element root,
            final PersistenceDefinition definition) {
        for (Iterator it1 = root.elementIterator("persistence-manager"); it1.hasNext();) {

            PersistenceManagerDefinition pmDef = new PersistenceManagerDefinition();

            final Element pm = (Element)it1.next();
            for (Iterator it2 = pm.attributeIterator(); it2.hasNext();) {
                Attribute attribute = (Attribute)it2.next();
                String name = attribute.getName().trim();
                String value = attribute.getValue().trim();
                if (name.equals("class")) {
                    pmDef.setClassName(value);
                }
                else if (name.equals("active")) {
                    pmDef.setActive(value);
                }
            }
            for (Iterator it2 = pm.elementIterator(); it2.hasNext();) {
                Element nestedAdviceElement = (Element)it2.next();

                if (nestedAdviceElement.getName().trim().equals("index-ref")) {
                    IndexRefDefinition indexDef = new IndexRefDefinition();
                    indexDef.setName(nestedAdviceElement.attributeValue("name"));
                    pmDef.addIndexRef(indexDef);
                }
                else if (nestedAdviceElement.getName().trim().equals("param")) {
                    ParameterDefinition paramDef = new ParameterDefinition();
                    paramDef.setName(nestedAdviceElement.attributeValue("name"));
                    paramDef.setValue(nestedAdviceElement.getText());
                    pmDef.addParameter(paramDef);
                }
            }

            definition.addPersistenceManager(pmDef);
        }
    }

    /**
     * Parses the <tt>persistent</tt> elements.
     *
     * @param root the root element
     * @param definition the definition object
     */
    private static void parsePersistentElements(
            final Element root,
            final PersistenceDefinition definition) {
        for (Iterator it1 = root.elementIterator("persistent"); it1.hasNext();) {
            final PersistentObjectDefinition persistentDef =
                    new PersistentObjectDefinition();

            final Element persistent = (Element)it1.next();
            for (Iterator it2 = persistent.attributeIterator(); it2.hasNext();) {
                Attribute attribute = (Attribute)it2.next();
                String name = attribute.getName().trim();
                String value = attribute.getValue().trim();
                if (name.equals("class")) {
                    persistentDef.setClassName(value);
                    continue;
                }
            }
            for (Iterator it2 = persistent.elementIterator(); it2.hasNext();) {
                Element nestedAdviceElement = (Element)it2.next();

                if (nestedAdviceElement.getName().trim().equals("index-ref")) {
                    IndexRefDefinition indexDef = new IndexRefDefinition();
                    indexDef.setName(nestedAdviceElement.attributeValue("name"));
                    indexDef.setMethod(nestedAdviceElement.attributeValue("method"));
                    persistentDef.addIndexRef(indexDef);
                }
            }

            definition.addPersistentObject(persistentDef);
        }
    }
}
