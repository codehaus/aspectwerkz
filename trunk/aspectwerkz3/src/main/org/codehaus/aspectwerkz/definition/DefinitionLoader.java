/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition;

import org.codehaus.aspectwerkz.ContextClassLoader;
import org.codehaus.aspectwerkz.exception.DefinitionException;
import org.codehaus.aspectwerkz.util.SequencedHashMap;
import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Handles the loading of the definition in various ways and formats.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @TODO: IMPORTANT - Needs to be cleaned up and refactored a lot. Right now it is a mess.
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
    * The aspectwerkz definition lists mapped to its UUID.
    */
    private static Map s_definitions = new SequencedHashMap();

    /**
    * Loads the aspectwerkz definition from disk based on a specific UUID.
    * <p/>
    * Only loads from the disk if the timestamp for the latest parsing is older than the timestamp for the weave
    * model.
    * <p/>
    * Used in the runtime (not transformation) process only.
    *
    * @param loader the current class loader
    * @param uuid   the uuid for the weave model to load
    * @return the aspectwerkz definition
    */
    public static SystemDefinition getDefinition(final ClassLoader loader, final String uuid) {
        final boolean isDirty = false;
        final List definitions;
        if (DEFINITION_FILE == null) {
            // no definition file is specified => try to locate the definition as a resource on the classpath
            definitions = loadDefinitionsAsResource(loader);
        } else {
            // definition file is specified => create one in memory
            definitions = loadDefinitionsFromFile(loader, isDirty);
        }

        // add the definitions parsed to the cache
        for (Iterator it = definitions.iterator(); it.hasNext();) {
            SystemDefinition definition = (SystemDefinition)it.next();
            if (isDirty || !s_definitions.containsKey(uuid)) {
                synchronized (s_definitions) {
                    s_definitions.put(uuid, definition);
                }
            }
        }
        SystemDefinition defToReturn = (SystemDefinition)s_definitions.get(uuid);
        if (defToReturn == null) {
            throw new RuntimeException("could not find definition with id [" + uuid + ']');
        }
        return defToReturn;
    }

    /**
    * Loads the definitions from disk. Only loads a new model from disk if it has changed.
    *
    * @param loader the current class loader
    * @return the definitions
    */
    private static List loadDefinitionsAsResource(final ClassLoader loader) {
        final InputStream stream = ContextClassLoader.getResourceAsStream(DEFAULT_DEFINITION_FILE_NAME);
        if (stream == null) {
            throw new DefinitionException("either you have to specify an XML definition file using the -Daspectwerkz.definition.file=... option or you have to have the XML definition file <aspectwerkz.xml> somewhere on the classpath");
        }
        return XmlParser.parse(loader, stream);
    }

    /**
    * Loads the definitions from file.
    *
    * @param useCache use cache
    * @return the definition
    */
    private static List loadDefinitionsFromFile(final ClassLoader loader, final boolean useCache) {
        String definitionFileName;
        if (DEFINITION_FILE == null) {
            URL definition = ContextClassLoader.loadResource(DEFAULT_DEFINITION_FILE_NAME);
            if (definition == null) {
                throw new DefinitionException("definition file could not be found on classpath (either specify the file by using the -Daspectwerkz.definition.file=.. option or by having a definition file called aspectwerkz.xml somewhere on the classpath)");
            }
            definitionFileName = definition.getFile();
        } else {
            definitionFileName = DEFINITION_FILE;
        }
        File definitionFile = new File(definitionFileName);
        return XmlParser.parse(loader, definitionFile, useCache);
    }

    /**
    * Loads the definitions from disk. Only loads a new model from disk if it has changed.
    *
    * @return the definitions
    */
    private static List loadAspectClassNamesAsResource() {
        final InputStream stream = ContextClassLoader.getResourceAsStream(DEFAULT_DEFINITION_FILE_NAME);
        if (stream == null) {
            throw new DefinitionException("either you have to specify an XML definition file using the -Daspectwerkz.definition.file=... option or you have to have the XML definition file <aspectwerkz.xml> somewhere on the classpath");
        }
        return XmlParser.getAspectClassNames(stream);
    }

    /**
    * Loads the definitions from file.
    *
    * @return the definition
    */
    private static List loadAspectClassNamesFromFile() {
        String definitionFileName;
        if (DEFINITION_FILE == null) {
            URL definition = ContextClassLoader.loadResource(DEFAULT_DEFINITION_FILE_NAME);
            if (definition == null) {
                throw new DefinitionException("definition file could not be found on classpath (either specify the file by using the -Daspectwerkz.definition.file=.. option or by having a definition file called aspectwerkz.xml somewhere on the classpath)");
            }
            definitionFileName = definition.getFile();
        } else {
            definitionFileName = DEFINITION_FILE;
        }
        return XmlParser.getAspectClassNames(new File(definitionFileName));
    }

    /**
    * Returns the default defintion.
    *
    * @param loader
    * @return the default defintion
    */
    public static List getDefaultDefinition(final ClassLoader loader) {
        if (DEFINITION_FILE != null) {
            File file = new File(DEFINITION_FILE);
            if (file.canRead()) {
                try {
                    return XmlParser.parseNoCache(loader, file.toURL());
                } catch (MalformedURLException e) {
                    System.err.println("<WARN> Cannot read " + DEFINITION_FILE);
                    e.printStackTrace();
                }
            } else {
                System.err.println("<WARN> Cannot read " + DEFINITION_FILE);
            }
        }
        return new ArrayList();
    }

    /**
    * Returns the aspect names in the default definition.
    *
    * @return the aspect names in the default definition
    */
    public static List getDefaultDefinitionAspectNames() {
        if (DEFINITION_FILE != null) {
            File file = new File(DEFINITION_FILE);
            if (file.canRead()) {
                return XmlParser.getAspectClassNames(file);
            } else {
                System.err.println("<WARN> Cannot read " + DEFINITION_FILE);
            }
        }
        return new ArrayList();
    }
}
