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
import java.util.HashMap;
import java.util.Map;
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
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * Parses the XML definition file using <tt>dom4j</tt>.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class XmlDefinitionParser {

    /**
     * The supported DTD public id. The matching dtd will be searched as a resource.
     */
    private final static Map DTD_PUBLIC_IDS = new HashMap();
    static {
        DTD_PUBLIC_IDS.put("-//AspectWerkz//DTD//EN", "/aspectwerkz.dtd");
        DTD_PUBLIC_IDS.put("-//AspectWerkz//DTD 0.8//EN", "/aspectwerkz.dtd");
        DTD_PUBLIC_IDS.put("-//AspectWerkz//DTD 0.8.1//EN", "/aspectwerkz.dtd");
    }

    /**
     * The timestamp, holding the last time that the definition was parsed.
     */
    private static File s_timestamp = new File(".timestamp");

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
     * @param isDirty flag to mark the the definition as updated or not
     * @return the definition object
     */
    public static AspectWerkzDefinition parse(final File definitionFile, boolean isDirty) {
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
    public static AspectWerkzDefinition parse(final InputStream stream) {
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
    public static AspectWerkzDefinition parseNoCache(final URL url) {
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
    public static AspectWerkzDefinition parse(final Document document) {
        final AspectWerkzDefinition definition = new AspectWerkzDefinition();
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

        // parse without package elements
        parseIntroductionElements(root, definition, basePackage);
        parseAdviceElements(root, definition, basePackage);
        parseAdviceStackElements(root, definition);
        parseAspectElements(root, definition, basePackage);

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
                if (DTD_PUBLIC_IDS.containsKey(publicId)) {
                    InputStream in = getClass().getResourceAsStream((String)DTD_PUBLIC_IDS.get(publicId));
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
                                                  final AspectWerkzDefinition definition,
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
                                             final AspectWerkzDefinition definition,
                                             final String basePackage) {
        for (Iterator it1 = root.elementIterator("package"); it1.hasNext();) {
            final Element packageElement = ((Element)it1.next());
            final String packageName = basePackage + getPackage(packageElement);

            parseUseAspectElements(packageElement, definition, packageName);
            parseIntroductionElements(packageElement, definition, packageName);
            parseAdviceElements(packageElement, definition, packageName);
            parseAdviceStackElements(packageElement, definition);
            parseAspectElements(packageElement, definition, packageName);
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
                                               final AspectWerkzDefinition definition,
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
            definition.addAspectToUse(packageName + className);
        }
    }

    /**
     * Parses the <tt>introduction</tt> elements.
     *
     * @param root the root element
     * @param definition the definition object
     * @param packageName the package name
     */
    private static void parseIntroductionElements(final Element root,
                                                  final AspectWerkzDefinition definition,
                                                  final String packageName) {
        for (Iterator it1 = root.elementIterator("introduction-def"); it1.hasNext();) {
            final IntroductionDefinition introDef = new IntroductionDefinition();

            Element introduction = (Element)it1.next();
            for (Iterator it2 = introduction.attributeIterator(); it2.hasNext();) {
                Attribute attribute = (Attribute)it2.next();

                final String name = attribute.getName().trim();
                final String value = attribute.getValue().trim();
                if (name.equals("name")) {
                    introDef.setName(value);
                }
                else if (name.equals("interface")) {
                    introDef.setInterface(packageName + value);
                }
                else if (name.equals("implementation")) {
                    introDef.setImplementation(packageName + value);
                }
                else if (name.equals("deploymentModel") ||
                        name.equals("deployment-model")) {
                    introDef.setDeploymentModel(value);
                }
                else if (name.equals("persistent")) {
                    throw new DefinitionException("persistent introductions are not supported");
//                    introDef.setIsPersistent(value);
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
     * @param packageName the package name
     */
    private static void parseAdviceElements(final Element root,
                                            final AspectWerkzDefinition definition,
                                            final String packageName) {
        for (Iterator it1 = root.elementIterator("advice-def"); it1.hasNext();) {
            final AdviceDefinition adviceDef = new AdviceDefinition();

            Element advice = (Element)it1.next();
            for (Iterator it2 = advice.attributeIterator(); it2.hasNext();) {
                Attribute attribute = (Attribute)it2.next();
                final String name = attribute.getName().trim();
                final String value = attribute.getValue().trim();

                if (name.equals("name")) {
                    adviceDef.setName(value);
                }
                else if (name.equals("advice") || (name.equals("class"))) {
                    adviceDef.setAdviceClassName(packageName + value);
                }
                else if (name.equals("deployment-model") ||
                        name.equals("deploymentModel")) {
                    adviceDef.setDeploymentModel(value);
                }
                else if (name.equals("persistent")) {
                    throw new DefinitionException("persistent advices are not supported");
//                    adviceDef.setIsPersistent(value);
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
     * @param packageName the package name
     */
    private static void parseAspectElements(final Element root,
                                            final AspectWerkzDefinition definition,
                                            final String packageName) {
        for (Iterator it1 = root.elementIterator("abstract-aspect"); it1.hasNext();) {
            final AspectDefinition aspectDef = new AspectDefinition();
            aspectDef.setAbstract(true);

            final Element aspect = (Element)it1.next();
            for (Iterator it2 = aspect.attributeIterator(); it2.hasNext();) {
                Attribute attribute = (Attribute)it2.next();
                final String name = attribute.getName().trim();
                final String value = attribute.getValue().trim();
                if (name.equals("name")) {
                    aspectDef.setName(value);
                }
            }
            parsePointcutElements(aspect, aspectDef, packageName);
            parseControllerElements(aspect, aspectDef);
            parseBindIntroductionElements(aspect, aspectDef, packageName);
            parseBindAdviceElements(aspect, aspectDef);

            definition.addAbstractAspect(aspectDef);
        }
        for (Iterator it1 = root.elementIterator("aspect"); it1.hasNext();) {
            final AspectDefinition aspectDef = new AspectDefinition();
            final Element aspect = (Element)it1.next();
            for (Iterator it2 = aspect.attributeIterator(); it2.hasNext();) {
                Attribute attribute = (Attribute)it2.next();
                final String name = attribute.getName().trim();
                final String value = attribute.getValue().trim();
                if (name.equals("name")) {
                    aspectDef.setName(value);
                }
                else if (name.equals("extends")) {
                    aspectDef.setExtends(value);
                    continue;
                }
            }

            parsePointcutElements(aspect, aspectDef, packageName);
            parseControllerElements(aspect, aspectDef);
            parseBindIntroductionElements(aspect, aspectDef, packageName);
            parseBindAdviceElements(aspect, aspectDef);

            handleAbstractAspectDependencies(aspectDef, definition);

            definition.addAspect(aspectDef);
        }
    }

    /**
     * Handles the abstract dependencies for a concrete aspect.
     *
     * @param aspectDef the aspect definition
     * @param definition the aspectwerkz definition
     */
    private static void handleAbstractAspectDependencies(final AspectDefinition aspectDef,
                                                         final AspectWerkzDefinition definition) {
        String extendsRef = aspectDef.getExtends();
        if (extendsRef != null) {
            final AspectDefinition abstractAspect = definition.getAbstractAspectDefinition(extendsRef);
            if (abstractAspect == null) {
                throw new DefinitionException("abstract aspect <" + aspectDef.getExtends() + "> is not defined");
            }
            for (Iterator it = abstractAspect.getPointcutDefs().iterator(); it.hasNext();) {
                final PointcutDefinition pointcutDef = (PointcutDefinition)it.next();
                aspectDef.addPointcutDef(pointcutDef);
            }
            for (Iterator it = abstractAspect.getAdviceWeavingRules().iterator(); it.hasNext();) {
                final AdviceWeavingRule weavingRule = (AdviceWeavingRule)it.next();
                for (Iterator it2 = aspectDef.getPointcutDefs().iterator(); it2.hasNext();) {
                    addPointcutPattern((PointcutDefinition)it2.next(), weavingRule);
                }
                aspectDef.addAdviceWeavingRule(weavingRule);
            }
            for (Iterator it = abstractAspect.getIntroductionWeavingRules().iterator(); it.hasNext();) {
                final IntroductionWeavingRule weavingRule = (IntroductionWeavingRule)it.next();
                aspectDef.addIntroductionWeavingRule(weavingRule);
            }
        }
    }

    /**
     * Parses the pointcut elements.
     *
     * @param aspect the aspect element
     * @param aspectDef the aspect definition
     * @param packageName the name of the package
     */
    private static void parsePointcutElements(final Element aspect,
                                              final AspectDefinition aspectDef,
                                              final String packageName) {
        for (Iterator it2 = aspect.elementIterator(); it2.hasNext();) {
            final Element nestedAdviceElement = (Element)it2.next();
            if (nestedAdviceElement.getName().trim().equals("pointcut-def") ||
                    nestedAdviceElement.getName().trim().equals("pointcut")) {
                try {
                    final PointcutDefinition pointcutDef = new PointcutDefinition();

                    for (Iterator it3 = nestedAdviceElement.attributeIterator(); it3.hasNext();) {
                        Attribute attribute = (Attribute)it3.next();
                        final String name = attribute.getName().trim();
                        final String value = attribute.getValue().trim();
                        if (name.equals("name")) {
                            pointcutDef.setName(value);
                        }
                        else if (name.equals("type")) {
                            pointcutDef.setType(value);
                        }
                        else if (name.equals("non-reentrant")) {
                            pointcutDef.setNonReentrant(value);
                        }
                    }

                    // handle the pointcut pattern, split the pattern in a class and
                    // a method/field/throws/callerside pattern
                    final String pattern = nestedAdviceElement.attributeValue("pattern");
                    try {
                        if (pointcutDef.getType().equalsIgnoreCase(PointcutDefinition.METHOD)) {
                            AspectWerkzDefinition.createMethodPattern(pattern, pointcutDef, packageName);
                        }
                        else if (pointcutDef.getType().equalsIgnoreCase(PointcutDefinition.CFLOW)) {
                            // make a 'match all caller side classes' pattern out of the regular method pattern
                            AspectWerkzDefinition.createCallerSidePattern(pattern, pointcutDef, packageName);
                        }
                        else if (pointcutDef.getType().equalsIgnoreCase(PointcutDefinition.GET_FIELD) ||
                                pointcutDef.getType().equalsIgnoreCase(PointcutDefinition.SET_FIELD)) {
                            AspectWerkzDefinition.createFieldPattern(pattern, pointcutDef, packageName);
                        }
                        else if (pointcutDef.getType().equalsIgnoreCase(PointcutDefinition.THROWS)) {
                            AspectWerkzDefinition.createThrowsPattern(pattern, pointcutDef, packageName);
                        }
                        else if (pointcutDef.getType().equalsIgnoreCase(PointcutDefinition.CALLER_SIDE)) {
                            AspectWerkzDefinition.createCallerSidePattern(pattern, pointcutDef, packageName);
                        }
                    }
                    catch (Exception e) {
                        throw new WrappedRuntimeException(e);
                    }
                    aspectDef.addPointcutDef(pointcutDef);
                }
                catch (Exception e) {
                    throw new DefinitionException("pointcut definition in aspect " + aspectDef.getName() + " is not well-formed: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Parses the controller elements.
     *
     * @param aspect the aspect element
     * @param aspectDef the aspect definition
     * @param packageName the package name
     */
    private static void parseControllerElements(final Element aspect,
                                                final AspectDefinition aspectDef) {
        for (Iterator it2 = aspect.elementIterator(); it2.hasNext();) {
            final Element nestedAdviceElement = (Element)it2.next();
            if (nestedAdviceElement.getName().trim().equals("controller-def") ||
                    nestedAdviceElement.getName().trim().equals("controller")) {
                try {
                    final ControllerDefinition controllerDef = new ControllerDefinition();

                    for (Iterator it3 = nestedAdviceElement.attributeIterator(); it3.hasNext();) {
                        Attribute attribute = (Attribute)it3.next();
                        final String name = attribute.getName().trim();
                        final String value = attribute.getValue().trim();
                        if (name.equals("pointcut") || name.equals("expression")) {
                            controllerDef.setExpression(value);
                        }
                        else if (name.equals("class")) {
                            controllerDef.setClassName(value);
                        }
                    }
                    // add the pointcut patterns to simplify the matching
                    if (!aspectDef.isAbstract()) {
                        for (Iterator it = aspectDef.getPointcutDefs().iterator(); it.hasNext();) {
                            final PointcutDefinition pointcutDef = (PointcutDefinition)it.next();
                            if (pointcutDef.getType().equalsIgnoreCase(PointcutDefinition.METHOD)) {
                                controllerDef.addMethodPointcutPattern(pointcutDef);
                            }
                        }
                    }
                    aspectDef.addControllerDef(controllerDef);
                }
                catch (Exception e) {
                    throw new DefinitionException("controller definition in aspect " + aspectDef.getName() + " is not well-formed: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Parses the introduce elements.
     *
     * @param aspect the aspect element
     * @param aspectDef the aspect definition
     * @param packageName the name of the package
     */
    private static void parseBindIntroductionElements(final Element aspect,
                                                      final AspectDefinition aspectDef,
                                                      final String packageName) {
        for (Iterator it2 = aspect.elementIterator(); it2.hasNext();) {
            final Element nestedAdviceElement = (Element)it2.next();
            if (nestedAdviceElement.getName().trim().equals("bind-introduction") ||
                    nestedAdviceElement.getName().trim().equals("introduction")) {
                try {
                    final IntroductionWeavingRule introWeavingRule = new IntroductionWeavingRule();

                    for (Iterator it3 = nestedAdviceElement.attributeIterator(); it3.hasNext();) {
                        Attribute attribute = (Attribute)it3.next();
                        final String name = attribute.getName().trim();
                        final String value = attribute.getValue().trim();
                        if (name.equals("class")) {
                            introWeavingRule.setClassPattern(packageName + value);
                        }
                        else if (name.equals("introduction-ref")) {
                            introWeavingRule.addIntroductionRef(value);
                            break; // if introduction-ref as an attribute => no more introductions
                        }
                    }
                    parseIntroductionWeavingRuleNestedElements(nestedAdviceElement, introWeavingRule);
                    aspectDef.addIntroductionWeavingRule(introWeavingRule);
                }
                catch (Exception e) {
                    throw new DefinitionException("introduction definition in aspect " + aspectDef.getName() + " is not well-formed: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Parses the advise elements.
     *
     * @param aspect the aspect element
     * @param aspectDef the aspect definition
     */
    private static void parseBindAdviceElements(final Element aspect,
                                                final AspectDefinition aspectDef) {
        for (Iterator it2 = aspect.elementIterator(); it2.hasNext();) {
            final Element nestedAdviceElement = (Element)it2.next();
            if (nestedAdviceElement.getName().trim().equals("bind-advice") ||
                    nestedAdviceElement.getName().trim().equals("advice")) {
                try {
                    final AdviceWeavingRule adviceWeavingRule = new AdviceWeavingRule();

                    for (Iterator it3 = nestedAdviceElement.attributeIterator(); it3.hasNext();) {
                        Attribute attribute = (Attribute)it3.next();
                        final String name = attribute.getName().trim();
                        final String value = attribute.getValue().trim();
                        if (name.equals("cflow")) {
                            adviceWeavingRule.setCFlowExpression(value);
                        }
                        else if (name.equals("pointcut") || name.equals("expression")) {
                            adviceWeavingRule.setExpression(value);
                        }
                        else if (name.equals("advice-ref")) {
                            adviceWeavingRule.addAdviceRef(value);
                            break; // if advice-ref as an attribute => no more advices
                        }
                    }
                    parseAdviceWeavingRuleNestedElements(nestedAdviceElement, adviceWeavingRule);
                    aspectDef.addAdviceWeavingRule(adviceWeavingRule);

                    // add the pointcut patterns to simplify the matching
                    if (!aspectDef.isAbstract()) {
                        for (Iterator it = aspectDef.getPointcutDefs().iterator(); it.hasNext();) {
                            addPointcutPattern((PointcutDefinition)it.next(), adviceWeavingRule);
                        }
                    }
                }
                catch (Exception e) {
                    throw new DefinitionException("advice definition in aspect " + aspectDef.getName() + " is not well-formed: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Adds a pointcut pattern to the weaving rule.
     *
     * @param pointcutDef the pointcut definition
     * @param adviceWeavingRule the weaving rule
     */
    private static void addPointcutPattern(final PointcutDefinition pointcutDef,
                                           final AdviceWeavingRule adviceWeavingRule) {
        if (pointcutDef.getType().equalsIgnoreCase(PointcutDefinition.METHOD)) {
            adviceWeavingRule.addMethodPointcutPattern(pointcutDef);
        }
        else if (pointcutDef.getType().equalsIgnoreCase(PointcutDefinition.SET_FIELD)) {
            adviceWeavingRule.addSetFieldPointcutPattern(pointcutDef);
        }
        else if (pointcutDef.getType().equalsIgnoreCase(PointcutDefinition.GET_FIELD)) {
            adviceWeavingRule.addGetFieldPointcutPattern(pointcutDef);
        }
        else if (pointcutDef.getType().equalsIgnoreCase(PointcutDefinition.THROWS)) {
            adviceWeavingRule.addThrowsPointcutPattern(pointcutDef);
        }
        else if (pointcutDef.getType().equalsIgnoreCase(PointcutDefinition.CALLER_SIDE)) {
            adviceWeavingRule.addCallerSidePointcutPattern(pointcutDef);
        }
        else if (pointcutDef.getType().equalsIgnoreCase(PointcutDefinition.CFLOW)) {
            adviceWeavingRule.addCallerSidePointcutPattern(pointcutDef);
        }
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
    private static void parseAdviceStackElements(final Element root,
                                                 final AspectWerkzDefinition definition) {
        for (Iterator it1 = root.elementIterator("advices-def"); it1.hasNext();) {
            final AdviceStackDefinition adviceStackDef = new AdviceStackDefinition();

            Element adviceStack = (Element)it1.next();
            for (Iterator it2 = adviceStack.attributeIterator(); it2.hasNext();) {
                Attribute attribute = (Attribute)it2.next();
                final String name = attribute.getName().trim();
                final String value = attribute.getValue().trim();
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
