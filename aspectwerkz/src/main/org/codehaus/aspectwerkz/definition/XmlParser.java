/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.net.MalformedURLException;
import java.net.URL;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.exception.DefinitionException;

/**
 * Parses the XML definition file using <tt>dom4j</tt>.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public class XmlParser {

    public static final String MODEL_TYPE_ATTRIB = "attrib";
    public static final String MODEL_TYPE_XML = "xml";

    /**
     * The current DTD public id. The matching dtd will be searched as a resource.
     */
    private final static String DTD_PUBLIC_ID = "-//AspectWerkz//DTD 0.9//EN";
    private final static String DTD_PUBLIC_ID_ALIAS = "-//AspectWerkz//DTD//EN";

    /**
     * The timestamp, holding the last time that the definition was parsed.
     */
    private static File s_timestamp = new File(".timestamp");

    /**
     * The AspectWerkz definitions.
     */
    private static List s_definitions = null;

    /**
     * Parses the XML definition file.
     *
     * @param definitionFile the definition file
     * @return the definitions
     */
    public static List parse(final File definitionFile) {
        return parse(definitionFile, false);
    }

    /**
     * Parses the XML definition file, only if it has been updated.
     * Uses a timestamp to check for modifications.
     *
     * @param definitionFile the definition file
     * @param isDirty flag to mark the the definition as updated or not
     * @return the definitions
     */
    public static List parse(final File definitionFile, boolean isDirty) {
        if (definitionFile == null) throw new IllegalArgumentException("definition file can not be null");
        if (!definitionFile.exists()) throw new DefinitionException("definition file " + definitionFile.toString() + " does not exist");

        // if definition is not updated; don't parse but return it right away
        if (isNotUpdated(definitionFile)) {
            isDirty = false;
            return s_definitions;
        }

        // updated definition, ready to be parsed
        try {
            Document document = createDocument(definitionFile.toURL());
            s_definitions = parse(document);

            setParsingTimestamp();
            isDirty = true;

            return s_definitions;
        }
        catch (MalformedURLException e) {
            throw new DefinitionException(definitionFile + " does not exist");
        }
        catch (DocumentException e) {
            e.printStackTrace();
            throw new DefinitionException("XML definition file <" + definitionFile + "> has errors: " + e.getMessage());
        }
    }

    /**
     * Parses the XML definition file retrieved from an input stream.
     *
     * @param stream the input stream containing the document
     * @return the definitions
     */
    public static List parse(final InputStream stream) {
        try {
            Document document = createDocument(stream);
            s_definitions = parse(document);
            return s_definitions;
        }
        catch (DocumentException e) {
            throw new DefinitionException("XML definition file on classpath has errors: " + e.getMessage());
        }
    }

    /**
     * Parses the XML definition file not using the cache.
     *
     * @param url the URL to the definition file
     * @return the definition object
     */
    public static List parseNoCache(final URL url) {
        try {
            Document document = createDocument(url);
            s_definitions = parse(document);
            return s_definitions;
        }
        catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Parses the definition DOM document.
     *
     * @param document the defintion as a document
     * @return the definitions
     */
    public static List parse(final Document document) {
        return DocumentParser.parse(document);
    }

    /**
     * Merges two DOM documents.
     *
     * @param document1 the first document
     * @param document2 the second document
     * @return the definition merged document
     */
    public static Document mergeDocuments(final Document document1, final Document document2) {
        if (document2 == null && document1 != null) return document1;
        if (document1 == null && document2 != null) return document2;
        if (document1 == null && document2 == null) return null;
        try {

            Element root1 = document1.getRootElement();
            Element root2 = document2.getRootElement();
            for (Iterator it1 = root2.elementIterator(); it1.hasNext();) {
                Element element = (Element)it1.next();
                element.setParent(null);
                root1.add(element);
            }
        }
        catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
        return document1;
    }

    /**
     * Creates a DOM document.
     *
     * @param url the URL to the file containing the XML
     * @return the DOM document
     * @throws DocumentException
     * @throws DocumentException
     */
    public static Document createDocument(final URL url) throws DocumentException {
        SAXReader reader = new SAXReader();
        setEntityResolver(reader);
        return reader.read(url);
    }

    /**
     * Creates a DOM document.
     *
     * @param stream the stream containing the XML
     * @return the DOM document
     * @throws DocumentException
     * @throws DocumentException
     */
    public static Document createDocument(final InputStream stream) throws DocumentException {
        SAXReader reader = new SAXReader();
        setEntityResolver(reader);
        return reader.read(stream);
    }

    /**
     * Sets the entity resolver which is created based on the DTD from in the root
     * dir of the AspectWerkz distribution.
     *
     * @param reader the reader to set the resolver in
     */
    private static void setEntityResolver(final SAXReader reader) {
        EntityResolver resolver = new EntityResolver() {
            public InputSource resolveEntity(String publicId, String systemId) {
                if (publicId.equals(DTD_PUBLIC_ID) || publicId.equals(DTD_PUBLIC_ID_ALIAS)) {
                    InputStream in = getClass().getResourceAsStream("/aspectwerkz.dtd");
                    return new InputSource(in);
                } else {
                    System.err.println("AspectWerkz - WARN - unsupported DTD " + publicId + " - consider upgrading to " + DTD_PUBLIC_ID);
                    return null;
                }
            }
        };
        reader.setEntityResolver(resolver);
    }

    /**
     * Checks if the definition file has been updated since the last parsing.
     *
     * @param definitionFile the definition file
     * @return boolean
     */
    private static boolean isNotUpdated(final File definitionFile) {
        return definitionFile.lastModified() < getParsingTimestamp() && s_definitions != null;
    }

    /**
     * Sets the timestamp for the latest parsing of the definition file.
     */
    private static void setParsingTimestamp() {
        final long newModifiedTime = System.currentTimeMillis();
        s_timestamp.setLastModified(newModifiedTime);
    }

    /**
     * Returns the timestamp for the last parsing of the definition file.
     *
     * @return the timestamp
     */
    private static long getParsingTimestamp() {
        final long modifiedTime = s_timestamp.lastModified();
        if (modifiedTime == 0L) {
            // no timestamp, create a new one
            try {
                s_timestamp.createNewFile();
            }
            catch (IOException e) {
                throw new RuntimeException("could not create timestamp file: " + s_timestamp.getAbsolutePath());
            }
        }
        return modifiedTime;
    }
}
