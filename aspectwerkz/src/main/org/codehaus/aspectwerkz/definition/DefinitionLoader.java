/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;
import java.net.URL;
import java.io.InputStream;
import java.io.File;

import org.dom4j.Document;

import org.codehaus.aspectwerkz.ContextClassLoader;
import org.codehaus.aspectwerkz.exception.DefinitionException;

/**
 * Handles the loading of the definition in various ways and formats.
 *
 * @TODO: some methods needs to be refactored to work properly (due to def 2 refactoring)
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class DefinitionLoader {

    /**
     * The UUID of the single AspectWerkz system if only one definition is used.
     */
    public static final String DEFAULT_SYSTEM = "default";

    /**
     * The path to the definition file.
     */
    public static final String DEFINITION_FILE = System.getProperty("aspectwerkz.definition.file", null);

    /**
     * The default name for the definition file.
     */
    public static final String DEFAULT_DEFINITION_FILE_NAME = "aspectwerkz.xml";

    /**
     * The aspectwerkz definitions.
     */
    private static Map s_definitions = new HashMap();

    /**
     * Creates, caches and returns new definition. Loads the definition in the file specified.
     *
     * @param document the DOM document containing the definition
     * @return the definitions
     */
    public static List createDefinition(final Document document) {
        if (document == null) throw new IllegalArgumentException("definition document can not be null");
        final List definitions = loadDefinitionsFromDocument(document);
        for (Iterator it = definitions.iterator(); it.hasNext();) {
            AspectWerkzDefinition definition = (AspectWerkzDefinition)it.next();
            s_definitions.put(definition.getUuid(), definition);
        }
        return definitions;
    }

    /**
     * Loads the aspectwerkz definition from disk.
     * Used by the transformers.
     * Grabs the first one it finds (should only by one in the transformations process).
     * @todo must be reimplemented when we need support for multiple definition in 'online' mode, should then return the merged definition for the current classloader (see createDefinition(..))
     *
     * @return the aspectwerkz definition
     */
    public static List getDefinitionsForTransformation() {
        final List definitions;
        if (DEFINITION_FILE == null) {
            // no definition file is specified => try to locate the weave model as a resource on the classpath
            definitions = loadDefinitionsAsResource();
        }
        else {
            // definition file is specified => create a weave model in memory
            definitions = loadDefinitionsFromFile(false);
        }
        return definitions;
    }

    /**
     * Loads the aspectwerkz definition from disk based on a specific UUID.
     * Only loads from the disk if the timestamp for the latest parsing is
     * older than the timestamp for the weave model.
     *
     * @param uuid the uuid for the weave model to load (null is allowed if only XML definition is used)
     * @return the aspectwerkz definition
     */
    public static AspectWerkzDefinition getDefinition(final String uuid) {
        if (s_definitions.containsKey(uuid)) {
            return (AspectWerkzDefinition)s_definitions.get(uuid);
        }

        final boolean isDirty = false;
        final List definitions;
        if (DEFINITION_FILE == null) {
            // no definition file is specified => try to locate the definition as a resource on the classpath
            definitions = loadDefinitionsAsResource();
        }
        else {
            // definition file is specified => create one in memory
            definitions = loadDefinitionsFromFile(isDirty);
        }

        // add the definitions parsed to the cache
        for (Iterator it = definitions.iterator(); it.hasNext();) {
            AspectWerkzDefinition definition = (AspectWerkzDefinition)it.next();
            if (isDirty || !s_definitions.containsKey(uuid)) {
                synchronized (s_definitions) {
                    s_definitions.put(uuid, definition);
                }
            }
        }

        AspectWerkzDefinition defToReturn = (AspectWerkzDefinition)s_definitions.get(uuid);
        if (defToReturn == null) {
            throw new RuntimeException("could not find definition with id [" + uuid + "]");
        }
        return defToReturn;
    }

    /**
     * Loads the definitions from file.
     *
     * @param useCache use cache
     * @return the definition
     */
    private static List loadDefinitionsFromFile(final boolean useCache) {
        return loadDefaultDefinitionsFromFile(useCache);
    }

    /**
     * Loads the definitions from disk.
     * Only loads a new model from disk if it has changed.
     *
     * @return the definitions
     */
    public static List loadDefinitionsAsResource() {
        InputStream stream = getDefinitionInputStream();
        if (stream == null) throw new RuntimeException("either you have to specify an XML definition file using the -Daspectwerkz.definition.file=... option or you have to have the XML definition file <aspectwerkz.xml> somewhere on the classpath");
        return loadDefinitionsFromStream(stream);
    }

    /**
     * Returns the definitions.
     * <p/>
     * If the file name is not specified as a parameter to the JVM it tries
     * to locate a file named 'aspectwerkz.xml' on the classpath.
     *
     * @param isDirty flag to mark the the defintion as updated or not
     * @return the definitions
     */
    public static List loadDefaultDefinitionsFromFile(boolean isDirty) {
        String definitionFileName;
        if (DEFINITION_FILE == null) {
            URL definition = ContextClassLoader.loadResource(DEFAULT_DEFINITION_FILE_NAME);
            if (definition == null) throw new DefinitionException("definition file could not be found on classpath (either specify the file by using the -Daspectwerkz.definition.file=.. option or by having a definition file called aspectwerkz.xml somewhere on the classpath)");
            definitionFileName = definition.getFile();
        }
        else {
            definitionFileName = DEFINITION_FILE;
        }
        return loadDefinitionsFromFile(definitionFileName, isDirty);
    }

    /**
     * Returns the definitions.
     *
     * @param definitionFile the definition file
     * @return the definitions
     */
    public static List loadDefinitionsFromFile(final String definitionFile) {
        return loadDefinitionsFromFile(definitionFile, false);
    }

    /**
     * Returns an input stream to the definition if found on classpath.
     *
     * @return the input stream to the definition
     */
    public static InputStream getDefinitionInputStream() {
        return ContextClassLoader.getResourceAsStream(DEFAULT_DEFINITION_FILE_NAME);
    }

    /**
     * Returns the definitions.
     *
     * @param definitionFile the definition file
     * @param isDirty flag to mark the the defintion as updated or not
     * @return the definitions
     */
    public static List loadDefinitionsFromFile(final String definitionFile,
                                               boolean isDirty) {
        return XmlParser.parse(new File(definitionFile), isDirty);
    }

    /**
     * Returns the definitions.
     *
     * @param stream the stream containing the definition file
     * @return the definitions
     */
    public static List loadDefinitionsFromStream(final InputStream stream) {
        return XmlParser.parse(stream);
    }

    /**
     * Returns the definition.
     *
     * @param document the DOM document containing the definition file
     * @return the definitions
     */
    public static List loadDefinitionsFromDocument(final Document document) {
        return XmlParser.parse(document);
    }

    /**
     * Loads and merges the definition.
     *
     * @TODO: to be implemented
     *
     * @param loader the class loader to use
     * @return the aspectwerkz definition
     */
    public static AspectWerkzDefinition loadAndMergeDefinitions(final ClassLoader loader) {
        AspectWerkzDefinition definition = null;
//        try {
//            Enumeration definitions = loader.getResources(
//                    "META-INF/" +
//                    DEFAULT_DEFINITION_FILE_NAME
//            );
//
//            // grab the definition in the current class loader
//            Document document = null;
//            if (definitions.hasMoreElements()) {
//                URL url = (URL)definitions.nextElement();
//                document = XmlParser.createDocument(url);
//            }
//
//            // merge the definition with the definitions in class loaders
//            // higher up in the class loader hierachy
//            while (definitions.hasMoreElements()) {
//                document = XmlParser.mergeDocuments(
//                        document,
//                        XmlParser.createDocument((URL)definitions.nextElement())
//                );
//            }
//
//            // handle the merging of the 'aspectwerkz.xml' definition on the classpath
//            // (if there is one)
//            InputStream stream = getDefinitionInputStream();
//            if (stream != null) {
//                document = XmlParser.mergeDocuments(
//                        document,
//                        XmlParser.createDocument(stream)
//                );
//            }
//
//            // handle the merging of the definition file specified using the JVM option
//            if (DEFINITION_FILE != null) {
//                document = XmlParser.mergeDocuments(
//                        document,
//                        XmlParser.createDocument(new File(DEFINITION_FILE).toURL())
//                );
//            }
//
//            // create a new definition based on the merged definition documents
//            definition = createDefinition(document);
//        }
//        catch (Exception e) {
//            ;// ignore
//        }
        return definition;
    }
}
