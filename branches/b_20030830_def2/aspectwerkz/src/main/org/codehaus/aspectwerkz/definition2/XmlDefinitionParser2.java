/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition2;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.net.MalformedURLException;
import java.net.URL;

import org.dom4j.Document;
import org.dom4j.Attribute;
import org.dom4j.Element;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.exception.DefinitionException;
import org.codehaus.aspectwerkz.AspectWerkz;
import org.codehaus.aspectwerkz.definition2.AspectWerkzDefinition2;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * Parses the XML definition file using <tt>dom4j</tt>.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class XmlDefinitionParser2 {

    /**
     * The current DTD public id. The matching dtd will be searched as a resource.
     */
    private final static String DTD_PUBLIC_ID = "-//AspectWerkz//DTD 0.8//EN";

    /**
     * The timestamp, holding the last time that the definition was parsed.
     */
    private static File s_timestamp = new File(".timestamp");

    /**
     * The AspectWerkz definition.
     */
    private static AspectWerkzDefinition2 s_definition;

    /**
     * Parses the XML definition file.
     *
     * @param definitionFile the definition file
     * @return the definition object
     */
    public static AspectWerkzDefinition2 parse(final File definitionFile) {
        return parse(definitionFile, false);
    }

    /**
     * Parses the XML definition file, only if it has been updated.
     * Uses a timestamp to check for modifications.
     *
     * @param definitionFile the definition file
     * @param isDirty flag to mark the the definition as updated or not
     * @return the definition object
     */
    public static AspectWerkzDefinition2 parse(final File definitionFile, boolean isDirty) {
        if (definitionFile == null) throw new IllegalArgumentException("definition file can not be null");
        if (!definitionFile.exists()) throw new DefinitionException("definition file " + definitionFile.toString() + " does not exist");

        // if definition is not updated; don't parse but return it right away
        if (isNotUpdated(definitionFile)) {
            isDirty = false;
            return s_definition;
        }

        // updated definition, ready to be parsed
        try {
            Document document = createDocument(definitionFile.toURL());
            s_definition = parse(document);

            setParsingTimestamp();
            isDirty = true;

            return s_definition;
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
     * @return the definition object
     */
    public static AspectWerkzDefinition2 parse(final InputStream stream) {
        try {
            Document document = createDocument(stream);
            s_definition = parse(document);
            return s_definition;
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
    public static AspectWerkzDefinition2 parseNoCache(final URL url) {
        try {
            Document document = createDocument(url);
            s_definition = parse(document);
            return s_definition;
        }
        catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Parses the definition DOM document.
     *
     * @param document the defintion as a document
     * @return the definition
     */
    public static AspectWerkzDefinition2 parse(final Document document) {
        final AspectWerkzDefinition2 definition = new AspectWerkzDefinition2();
        final Element root = document.getRootElement();

        String uuid = root.attributeValue("id");
        if (uuid == null || uuid.equals("")) {
            // TODO: log a warning "no id specified in the definition, using default (AspectWerkz.DEFAULT_SYSTEM)"
            uuid = AspectWerkz.DEFAULT_SYSTEM;
        }
        definition.setUuid(uuid);

        // get the base package
        final String basePackage = getBasePackage(root);

        // parse the transformation scopes
        parseTransformationScopes(root, definition, basePackage);

        // parse with package elements
        parsePackageElements(root, definition, basePackage);

        return definition;
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
     * @throws MalformedURLException
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
     * @throws MalformedURLException
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
                if (publicId.equals(DTD_PUBLIC_ID)) {
                    InputStream in = getClass().getResourceAsStream("/aspectwerkz.dtd");
                    return new InputSource(in);
                }
                return null;
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
        return definitionFile.lastModified() < getParsingTimestamp() && s_definition != null;
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

    /**
     * Parses the <tt>transformation-scope</tt> elements.
     *
     * @param root the root element
     * @param definition the definition object
     * @param packageName the package name
     */
    private static void parseTransformationScopes(final Element root,
                                                  final AspectWerkzDefinition2 definition,
                                                  final String packageName) {
        for (Iterator it1 = root.elementIterator("transformation-scope"); it1.hasNext();) {
            String transformationScope = "";
            Element scope = (Element)it1.next();
            for (Iterator it2 = scope.attributeIterator(); it2.hasNext();) {
                Attribute attribute = (Attribute)it2.next();
                if (attribute.getName().trim().equals("package")) {
                    transformationScope = attribute.getValue().trim();
                    if (packageName.endsWith(".*")) {
                        transformationScope = packageName.substring(0, packageName.length() - 2);
                    }
                    else if (packageName.endsWith(".")) {
                        transformationScope = packageName.substring(0, packageName.length() - 1);
                    }
                    transformationScope = packageName + transformationScope;
                    break;
                }
                else {
                    continue;
                }
            }
            if (transformationScope.length() != 0) {
                definition.addTransformationScope(transformationScope);
            }
        }
    }

    /**
     * Parses the definition DOM document.
     *
     * @param root the root element
     * @param definition the definition
     * @param basePackage the base package
     */
    private static void parsePackageElements(final Element root,
                                             final AspectWerkzDefinition2 definition,
                                             final String basePackage) {
        for (Iterator it1 = root.elementIterator("package"); it1.hasNext();) {
            final Element packageElement = ((Element)it1.next());
            final String packageName = basePackage + getPackage(packageElement);
            parseUseAspectElements(packageElement, definition, packageName);
        }
    }

    /**
     * Parses the <tt>use-aspect</tt> elements.
     *
     * @param root the root element
     * @param definition the definition object
     * @param packageName the package name
     */
    private static void parseUseAspectElements(final Element root,
                                               final AspectWerkzDefinition2 definition,
                                               final String packageName) {
        for (Iterator it1 = root.elementIterator("use-aspect"); it1.hasNext();) {

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
            System.out.println("className = " + className);
            definition.addAspectClassName(packageName + className);
        }
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
