/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition;

import java.util.Map;
import java.util.Iterator;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.Serializable;
import java.io.InputStream;
import java.net.URL;

import org.dom4j.Document;
import gnu.trove.TObjectIntHashMap;

import org.codehaus.aspectwerkz.exception.DefinitionException;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.metadata.MethodMetaData;
import org.codehaus.aspectwerkz.metadata.FieldMetaData;
import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.metadata.ReflectionMetaDataMaker;
import org.codehaus.aspectwerkz.ContextClassLoader;
import org.codehaus.aspectwerkz.AspectWerkz;
import org.codehaus.aspectwerkz.definition.AspectDefinition;
import org.codehaus.aspectwerkz.definition.AdviceDefinition;
import org.codehaus.aspectwerkz.definition.attribute.AspectAttributeParser;
import org.codehaus.aspectwerkz.definition.attribute.DefaultAspectAttributeParser;
import org.codehaus.aspectwerkz.util.SequencedHashMap;

/**
 * Implements the <code>AspectWerkz</code> definition for definition style two.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class AspectWerkzDefinition implements Serializable {

    public static final String PER_JVM = "perJVM";
    public static final String PER_CLASS = "perClass";
    public static final String PER_INSTANCE = "perInstance";
    public static final String PER_THREAD = "perThread";
    public static final String THROWS_DELIMITER = "#";
    public static final String CALLER_SIDE_DELIMITER = "#";

    /**
     * The path to the definition file.
     */
    public static final String DEFINITION_FILE = System.getProperty("aspectwerkz.definition.file", null);

    /**
     * The default name for the definition file.
     */
    public static final String DEFAULT_DEFINITION_FILE_NAME = "aspectwerkz.xml";

    /**
     * The name of the system aspect.
     */
    public static final String SYSTEM_ASPECT = "org/codehaus/aspectwerkz/system";

    /**
     * The aspectwerkz definitions.
     */
    private static Map s_definitions = new HashMap();

    /**
     * Holds the indexes for the aspects. The aspect indexes are needed here (instead of in the
     * AspectWerkz class like the advice indexes) since they need to be available to the
     * transformers before the AspectWerkz system has been initialized.
     */
    private final TObjectIntHashMap m_aspectIndexes = new TObjectIntHashMap();

    /**
     * Set with the aspect class names.
     */
    private Set m_aspectClassNames = new HashSet();

    /**
     * Maps the aspects to it's name.
     */
    private final Map m_aspectMap = new SequencedHashMap();

    /**
     * The UUID for this definition.
     */
    private String m_uuid = "default";

    /**
     * The transformation scopes.
     */
    private final Set m_transformationScopeSet = new HashSet();

    /**
     * The default attribute parser.
     * @TODO: make customizable (-D..)
     * @TODO: use factory
     */
    private final AspectAttributeParser m_attributeParser = new DefaultAspectAttributeParser();

    /**
     * Marks the definition as initialized.
     */
    private boolean m_aspectsLoaded = false;

    /**
     * Creates, caches and returns new definition. Loads the definition in the file specified.
     *
     * @param document the DOM document containing the definition
     * @return the aspectwerkz definition
     */
    public static AspectWerkzDefinition createDefinition(final Document document) {
        if (document == null) throw new IllegalArgumentException("definition document can not be null");
        final AspectWerkzDefinition definition =
                AspectWerkzDefinition.loadDefinitionAsDocument(document);
        s_definitions.put(definition.getUuid(), definition);
        return definition;
    }

    /**
     * Loads the aspectwerkz definition from disk.
     * Used by the transformers.
     * Grabs the first one it finds (should only by one in the transformations process).
     *
     * @todo must be reimplemented when we need support for multiple definition in 'online' mode, should then return the merged definition for the current classloader (see createDefinition(..))
     *
     * @return the aspectwerkz definition
     */
    public static AspectWerkzDefinition getDefinitionForTransformation() {
        if (s_definitions.containsKey(AspectWerkz.DEFAULT_SYSTEM)) {
            return (AspectWerkzDefinition)s_definitions.get(AspectWerkz.DEFAULT_SYSTEM);
        }
        final boolean isDirty = false;
        final AspectWerkzDefinition definition;
        if (DEFINITION_FILE == null) {
            // no definition file is specified => try to locate the weave model as a resource on the classpath
            definition = loadDefinitionAsResource();
        }
        else {
            // definition file is specified => create a weave model in memory
            definition = loadDefinitionFromFile(isDirty);
        }

        if (isDirty || !s_definitions.containsKey(AspectWerkz.DEFAULT_SYSTEM)) {
            synchronized (s_definitions) {
                s_definitions.put(AspectWerkz.DEFAULT_SYSTEM, definition);
            }
        }
        return (AspectWerkzDefinition)s_definitions.get(AspectWerkz.DEFAULT_SYSTEM);
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
        final AspectWerkzDefinition definition;
        if (DEFINITION_FILE == null) {
            // no definition file is specified => try to locate the definition as a resource on the classpath
            definition = loadDefinitionAsResource();
        }
        else {
            // definition file is specified => create one in memory
            definition = loadDefinitionFromFile(isDirty);
        }

        // check the we have found the right definition
        if (!uuid.equals(definition.getUuid())) {
            throw new RuntimeException("could not find definition with UUID <" + uuid + "> (found definition with UUID <" + definition.getUuid() + ">)");
        }

        if (isDirty || !s_definitions.containsKey(uuid)) {
            synchronized (s_definitions) {
                s_definitions.put(uuid, definition);
            }
        }
        return (AspectWerkzDefinition)s_definitions.get(uuid);
    }

    /**
     * Loads the definition from file.
     *
     * @param useCache use cache
     * @return the definition
     */
    private static AspectWerkzDefinition loadDefinitionFromFile(final boolean useCache) {
        return AspectWerkzDefinition.loadDefaultDefinitionAsFile(useCache);
    }

    /**
     * Loads a definition from disk.
     * Only loads a new model from disk if it has changed.
     *
     * @param uuid the uuid for the definition to load
     * @param uuid the uuid for the definition to load
     * @return the definition
     */
    public static AspectWerkzDefinition loadDefinitionAsResource() {
        InputStream stream = getDefinitionInputStream();
        if (stream == null) throw new RuntimeException("either you have to specify an XML definition file using the -Daspectwerkz.definition.file=... option or you have to have the XML definition file <aspectwerkz.xml> somewhere on the classpath");
        return AspectWerkzDefinition.loadDefinitionAsStream(stream);
    }

    /**
     * Returns the definition.
     * <p/>
     * If the file name is not specified as a parameter to the JVM it tries
     * to locate a file named 'aspectwerkz.xml' on the classpath.
     *
     * @param isDirty flag to mark the the defintion as updated or not
     * @return the definition
     */
    public static AspectWerkzDefinition loadDefaultDefinitionAsFile(boolean isDirty) {
        String definitionFileName;
        if (DEFINITION_FILE == null) {
            URL definition = ContextClassLoader.loadResource(DEFAULT_DEFINITION_FILE_NAME);
            if (definition == null) throw new DefinitionException("definition file could not be found on classpath (either specify the file by using the -Daspectwerkz.definition.file=.. option or by having a definition file called aspectwerkz.xml somewhere on the classpath)");
            definitionFileName = definition.getFile();
        }
        else {
            definitionFileName = DEFINITION_FILE;
        }
        return loadDefinitionAsFile(definitionFileName, isDirty);
    }

    /**
     * Returns the definition.
     *
     * @param definitionFile the definition file
     * @return the definition
     */
    public static AspectWerkzDefinition loadDefinitionAsFile(final String definitionFile) {
        return loadDefinitionAsFile(definitionFile, false);
    }

    /**
     * Returns the definition.
     *
     * @param definitionFile the definition file
     * @param isDirty flag to mark the the defintion as updated or not
     * @return the definition
     */
    public static AspectWerkzDefinition loadDefinitionAsFile(final String definitionFile,
                                                             boolean isDirty) {
        return XmlDefinitionParser.parse(new File(definitionFile), isDirty);
    }

    /**
     * Returns the definition.
     *
     * @param stream the stream containing the definition file
     * @return the definition
     */
    public static AspectWerkzDefinition loadDefinitionAsStream(final InputStream stream) {
        return XmlDefinitionParser.parse(stream);
    }

    /**
     * Returns the definition.
     *
     * @param document the DOM document containing the definition file
     * @return the definition
     */
    public static AspectWerkzDefinition loadDefinitionAsDocument(final Document document) {
        return XmlDefinitionParser.parse(document);
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
     * Creates a class pattern and adds it to the pointcut definition.
     *
     * @param pattern the pattern
     * @param pointcutDef the pointcut definition
     */
    public static void createClassPattern(final String pattern,
                                          final PointcutDefinition pointcutDef) {
        String classPattern = pattern;
        if (classPattern.endsWith("+")) {
            classPattern = classPattern.substring(0, classPattern.length() - 1);
            pointcutDef.markAsHierarchical();
        }
        pointcutDef.setPattern(classPattern);
        pointcutDef.setClassPattern(classPattern);
    }

    /**
     * Creates a method pattern and adds it to the pointcut definition.
     *
     * @param pattern the pattern
     * @param pointcutDef the pointcut definition
     */
    public static void createMethodPattern(final String pattern,
                                           final PointcutDefinition pointcutDef) {
        int indexFirstSpace = pattern.indexOf(' ');
        String returnType = pattern.substring(0, indexFirstSpace + 1);
        String classNameWithMethodName = pattern.substring(
                indexFirstSpace, pattern.indexOf('(')).trim();
        String parameterTypes = pattern.substring(
                pattern.indexOf('('), pattern.length()).trim();
        int indexLastDot = classNameWithMethodName.lastIndexOf('.');

        final String methodPattern = classNameWithMethodName.substring(
                indexLastDot + 1, classNameWithMethodName.length()).trim();
        String classPattern = classNameWithMethodName.substring(0, indexLastDot);
        if (classPattern.endsWith("+")) {
            classPattern = classPattern.substring(0, classPattern.length() - 1);
            pointcutDef.markAsHierarchical();
        }

        StringBuffer buf = new StringBuffer();
        buf.append(returnType);
        buf.append(methodPattern);
        buf.append(parameterTypes);
        pointcutDef.setPattern(buf.toString());
        pointcutDef.setClassPattern(classPattern);
    }

    /**
     * Creates a field pattern and adds it to the pointcut definition.
     *
     * @param pattern the pattern
     * @param pointcutDef the pointcut definition
     */
    public static void createFieldPattern(final String pattern,
                                          final PointcutDefinition pointcutDef) {
        int indexFirstSpace = pattern.indexOf(' ');
        String fieldType = pattern.substring(0, indexFirstSpace + 1);
        String classNameWithFieldName = pattern.substring(
                indexFirstSpace, pattern.length()).trim();
        int indexLastDot = classNameWithFieldName.lastIndexOf('.');

        final String fieldPattern = classNameWithFieldName.substring(
                indexLastDot + 1, classNameWithFieldName.length()).trim();
        String classPattern = classNameWithFieldName.substring(0, indexLastDot).trim();
        if (classPattern.endsWith("+")) {
            classPattern = classPattern.substring(0, classPattern.length() - 1);
            pointcutDef.markAsHierarchical();
        }

        StringBuffer buf = new StringBuffer();
        buf.append(fieldType);
        buf.append(fieldPattern);
        pointcutDef.setPattern(buf.toString());
        pointcutDef.setClassPattern(classPattern);
    }

    /**
     * Creates a throws pattern and adds it to the pointcut definition.
     *
     * @param pattern the pattern
     * @param pointcutDef the pointcut definition
     */
    public static void createThrowsPattern(final String pattern,
                                           final PointcutDefinition pointcutDef) {
        String classAndMethodName = pattern.substring(0, pattern.indexOf('#')).trim();
        final String exceptionName = pattern.substring(pattern.indexOf('#') + 1).trim();
        int indexFirstSpace = classAndMethodName.indexOf(' ');
        final String returnType = classAndMethodName.substring(0, indexFirstSpace + 1);
        String classNameWithMethodName = classAndMethodName.substring(
                indexFirstSpace, classAndMethodName.indexOf('(')).trim();
        final String parameterTypes = classAndMethodName.substring(
                classAndMethodName.indexOf('('), classAndMethodName.length()).trim();
        int indexLastDot = classNameWithMethodName.lastIndexOf('.');
        final String methodPattern = classNameWithMethodName.substring(
                indexLastDot + 1, classNameWithMethodName.length()).trim();
        String classPattern = classNameWithMethodName.substring(0, indexLastDot);
        if (classPattern.endsWith("+")) {
            classPattern = classPattern.substring(0, classPattern.length() - 1);
            pointcutDef.markAsHierarchical();
        }

        StringBuffer buf = new StringBuffer();
        buf.append(returnType);
        buf.append(methodPattern);
        buf.append(parameterTypes);
        buf.append('#');
        buf.append(exceptionName);
        pointcutDef.setClassPattern(classPattern);
        pointcutDef.setPattern(buf.toString());
    }

    /**
     * Creates a caller side pattern and adds it to the pointcut definition.
     *
     * @param pattern the pattern
     * @param pointcutDef the pointcut definition
     * @param packageName the name of the package
     */
    public static void createCallerSidePattern(String pattern,
                                               final PointcutDefinition pointcutDef) {
        if (pattern.indexOf('>') == -1) {
            pattern = "*->" + pattern; // if no caller side pattern is specified => default to *
        }

        String callerClassPattern = pattern.substring(0, pattern.indexOf('-')).trim();
        if (callerClassPattern.endsWith("+")) {
            callerClassPattern = callerClassPattern.substring(0, callerClassPattern.length() - 1);
            pointcutDef.markAsHierarchical();
        }

        String calleePattern = pattern.substring(pattern.indexOf('>') + 1).trim();
        int indexFirstSpace = calleePattern.indexOf(' ');
        String returnType = calleePattern.substring(0, indexFirstSpace + 1);
        String classNameWithMethodName = calleePattern.substring(
                indexFirstSpace, calleePattern.indexOf('(')).trim();
        String parameterTypes = calleePattern.substring(
                calleePattern.indexOf('('), calleePattern.length()).trim();
        int indexLastDot = classNameWithMethodName.lastIndexOf('.');
        String calleeMethodPattern = classNameWithMethodName.substring(
                indexLastDot + 1, classNameWithMethodName.length()).trim();
        String calleeClassPattern = classNameWithMethodName.substring(0, indexLastDot);

        if (calleeClassPattern.endsWith("+")) {
            calleeClassPattern = calleeClassPattern.substring(0, calleeClassPattern.length() - 1);
            pointcutDef.markAsHierarchical();
        }
        calleeMethodPattern = returnType + calleeMethodPattern + parameterTypes;

        StringBuffer buf = new StringBuffer();
        buf.append(calleeClassPattern);
        buf.append('#');
        buf.append(calleeMethodPattern);
        pointcutDef.setPattern(buf.toString());
        pointcutDef.setClassPattern(callerClassPattern);
    }

    /**
     * Creates a new instance, creates and sets the system aspect.
     */
    public AspectWerkzDefinition() {
//        AspectDefinition systemAspect = new AspectDefinition();
//        systemAspect.setName(SYSTEM_ASPECT);
//        synchronized (m_aspectMap) {
//            m_aspectMap.put(SYSTEM_ASPECT, systemAspect);
//        }
    }

    /**
     * Loads the aspects.
     *
     * @param loader the class loader to use to load the aspects
     */
    public void loadAspects(final ClassLoader loader) {
        if (m_aspectsLoaded) return;
        m_aspectsLoaded = true;
        for (Iterator it = getDefinedAspectClassNames().iterator(); it.hasNext();) {
            loadAspect((String)it.next(), loader);
        }
    }

    /**
     * Sets the UUID for the definition.
     *
     * @param uuid the UUID
     */
    public void setUuid(final String uuid) {
        m_uuid = uuid;
    }

    /**
     * Returns the UUID for the definition.
     *
     * @return the UUID
     */
    public String getUuid() {
        return m_uuid;
    }

    /**
     * Returns the transformation scopes.
     *
     * @return the transformation scopes
     */
    public Set getTransformationScopes() {
        return m_transformationScopeSet;
    }

    /**
     * Returns a collection with the aspect definitions registered.
     *
     * @return the aspect definitions
     */
    public Collection getAspectDefinitions() {
        if (!m_aspectsLoaded) throw new IllegalStateException("aspects are not loaded");
        Collection clone = new ArrayList(m_aspectMap.size());
        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            clone.add(it.next());
        }
        return clone;
    }

    /**
     * Returns a collection with the introduction definitions registered.
     *
     * @return the introduction definitions
     */
    public Collection getIntroductionDefinitions() {
        if (!m_aspectsLoaded) throw new IllegalStateException("aspects are not loaded");
        final Collection introductionDefs = new ArrayList();
        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();
            introductionDefs.addAll(aspectDef.getMethodIntroductions());
        }
        return introductionDefs;
    }

    /**
     * Returns a collection with the advice definitions registered.
     *
     * @return the advice definitions
     */
    public Collection getAdviceDefinitions() {
        if (!m_aspectsLoaded) throw new IllegalStateException("aspects are not loaded");
        final Collection adviceDefs = new ArrayList();
        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();
            adviceDefs.addAll(aspectDef.getAroundAdvices());
            adviceDefs.addAll(aspectDef.getBeforeAdvices());
            adviceDefs.addAll(aspectDef.getAfterAdvices());
        }
        return adviceDefs;
    }

    /**
     * Returns a specific aspect definition.
     *
     * @param name the name of the aspect definition
     * @return the aspect definition
     */
    public AspectDefinition getAspectDefinition(final String name) {
        if (!m_aspectsLoaded) throw new IllegalStateException("aspects are not loaded");
        return (AspectDefinition)m_aspectMap.get(name);
    }

    /**
     * Returns a specific advice definition.
     *
     * @param name the name of the advice definition
     * @return the advice definition
     */
    public AdviceDefinition getAdviceDefinition(final String name) {
        if (!m_aspectsLoaded) throw new IllegalStateException("aspects are not loaded");
        Collection adviceDefs = getAdviceDefinitions();
        for (Iterator it = adviceDefs.iterator(); it.hasNext();) {
            AdviceDefinition adviceDef = (AdviceDefinition)it.next();
            if (adviceDef.getName().equals(name)) {
                return adviceDef;
            }
        }
        return null;
    }

    /**
     * Finds the name of an advice by its attribute.
     *
     * @param attribute the attribute
     * @return the name of the advice
     */
//    public String getAdviceNameByAttribute(final String attribute) {
//    if (!m_aspectsLoaded) throw new IllegalStateException("aspects are not loaded");
//        if (attribute == null) return null;
//        for (Iterator it = m_adviceMap.values().iterator(); it.hasNext();) {
//            AdviceDefinition adviceDefinition = (AdviceDefinition)it.next();
//            if (adviceDefinition.getAttribute().equals(attribute)) {
//                return adviceDefinition.getName();
//            }
//        }
//        return null;
//    }

    /**
     * Finds the name of an introduction by its attribute.
     *
     * @param attribute the attribute
     * @return the name of the introduction
     */
//    public String getIntroductionNameByAttribute(final String attribute) {
//    if (!m_aspectsLoaded) throw new IllegalStateException("aspects are not loaded");
//        if (attribute == null) {
//            return null;
//        }
//        for (Iterator it = m_introductionMap.values().iterator(); it.hasNext();) {
//            IntroductionDefinition1 introductionDefinition = (IntroductionDefinition1)it.next();
//            if (introductionDefinition.getAttribute().equals(attribute)) {
//                return introductionDefinition.getName();
//            }
//        }
//        return null;
//    }

    /**
     * Returns the name of the interface for an introduction.
     *
     * @TODO: how to solve the interface introduction (base it on IntroductionDefintion or not?)
     *
     * @param introductionName the name of the introduction
     * @return the name of the interface
     */
    public String getIntroductionInterfaceName(final String introductionName) {
        if (!m_aspectsLoaded) throw new IllegalStateException("aspects are not loaded");

        return null;
    }

    /**
     * Returns the name of the implementation for an introduction.
     *
     * @param introductionName the name of the introduction
     * @return the name of the interface
     */
    public String getIntroductionImplName(final String introductionName) {
        if (!m_aspectsLoaded) throw new IllegalStateException("aspects are not loaded");
        if (introductionName == null) throw new IllegalArgumentException("introduction name can not be null");

        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();
            List introductions = aspectDef.getMethodIntroductions();
            for (Iterator it2 = introductions.iterator(); it2.hasNext();) {
                MethodIntroductionDefinition introDef = (MethodIntroductionDefinition)it2.next();
                if (introDef.getName().equals(introductionName)) {
                    return introDef.getAspectClassName();
                }
            }
        }
        return null;
    }

    /**
     * Returns a specific introduction definition.
     *
     * @TODO: needed??
     *
     * @param introductionName the name of the introduction
     * @return the introduction definition
     */
    public MethodIntroductionDefinition getIntroductionDefinition(final String introductionName) {
        if (!m_aspectsLoaded) throw new IllegalStateException("aspects are not loaded");

        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();
            List introductions = aspectDef.getMethodIntroductions();
            for (Iterator it2 = introductions.iterator(); it2.hasNext();) {
                MethodIntroductionDefinition introDef = (MethodIntroductionDefinition)it2.next();
                if (introDef.getName().equals(introductionName)) {
                    return introDef;
                }
            }
        }
        return null;
    }

    /**
     * Returns the introduction definitions for a specific class.
     *
     * @param classMetaData the class meta-data
     * @return a list with the introduction definitions
     */
    public List getIntroductionDefinitionsForClass(final ClassMetaData classMetaData) {
        if (!m_aspectsLoaded) throw new IllegalStateException("aspects are not loaded");
        final List introDefs = new ArrayList();
        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();
            for (Iterator it2 = aspectDef.getMethodIntroductions().iterator(); it2.hasNext();) {
                MethodIntroductionDefinition introDef = (MethodIntroductionDefinition)it2.next();
                if (introDef.getWeavingRule().matchClassPointcut(classMetaData)) {
                    introDefs.add(introDef);
                }
            }
        }
        return introDefs;
    }

    /**
     * Returns the index for a specific introduction.
     *
     * @param aspectName the name of the aspect
     * @return the index
     */
    public int getAspectIndexByName(final String aspectName) {
        if (!m_aspectsLoaded) throw new IllegalStateException("aspects are not loaded");
        if (aspectName == null) throw new IllegalArgumentException("aspect name can not be null");
        int index = m_aspectIndexes.get(aspectName);
        if (index < 1) throw new RuntimeException("aspect [" + aspectName + "] does not exist, failed in retrieving aspect index");
        return index;
    }

    /**
     * Returns the indexes for the introductions.
     *
     * @return the indexes
     */
    public TObjectIntHashMap getAspectIndexes() {
        if (!m_aspectsLoaded) throw new IllegalStateException("aspects are not loaded");
        return m_aspectIndexes;
    }

    /**
     * Returns the class name for the join point controller, if there is a match.
     *
     * @param classMetaData the class meta-data
     * @param methodMetaData the method meta-data
     * @return the controller class name
     */
    public String getJoinPointController(final ClassMetaData classMetaData,
                                         final MethodMetaData methodMetaData) {
        if (!m_aspectsLoaded) throw new IllegalStateException("aspects are not loaded");
        if (classMetaData == null) throw new IllegalArgumentException("class meta-data can not be null");
        if (methodMetaData == null) throw new IllegalArgumentException("method meta-data can not be null");

        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();
            Collection controllerDefs = aspectDef.getControllers();
            for (Iterator it2 = controllerDefs.iterator(); it2.hasNext();) {
                ControllerDefinition controllerDef = (ControllerDefinition)it2.next();
                if (controllerDef.matchMethodPointcut(classMetaData, methodMetaData)) {
                    return controllerDef.getClassName();
                }
            }
        }
        return null;
    }

    /**
     * Returns a set with the aspects to use.
     *
     * @return the aspects to use
     */
    public Set getDefinedAspectClassNames() {
        if (!m_aspectsLoaded) throw new IllegalStateException("aspects are not loaded");
        return m_aspectClassNames;
    }

    /**
     * Adds a new aspect definition.
     *
     * @param aspect the aspect definition
     */
    public void addAspect(final AspectDefinition aspectDef) {
        if (aspectDef == null) throw new IllegalArgumentException("aspect definition can not be null");
        if (m_aspectIndexes.containsKey(aspectDef.getName())) {
            return;
        }
        synchronized (m_aspectMap) {
            synchronized (m_aspectIndexes) {
                final int index = m_aspectMap.values().size() + 1;
                m_aspectIndexes.put(aspectDef.getName(), index);
                m_aspectMap.put(aspectDef.getName(), aspectDef);
            }
        }
    }

    /**
     * Adds a new aspect to use.
     *
     * @param className the class name of the aspect
     */
    public void addAspectClassName(final String className) {
        synchronized (m_aspectClassNames) {
            m_aspectClassNames.add(className);
        }
    }

    /**
     * Adds a new transformation scope.
     *
     * @param transformationScope the new scope
     */
    public void addTransformationScope(final String transformationScope) {
        synchronized (m_transformationScopeSet) {
            m_transformationScopeSet.add(transformationScope);
        }
    }

    /**
     * Checks if there exists an advice with the name specified.
     *
     * @param name the name of the advice
     * @return boolean
     */
    public boolean hasAdvice(final String name) {
        Collection adviceDefs = getAdviceDefinitions();
        for (Iterator it = adviceDefs.iterator(); it.hasNext();) {
            AdviceDefinition adviceDef = (AdviceDefinition)it.next();
            if (adviceDef.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if there exists an introduction with the name specified.
     *
     * @TODO: should I just check for the aspect with the specified name?
     *
     * @param name the name of the introduction
     * @return boolean
     */
    public boolean hasIntroduction(final String name) {
        if (!m_aspectsLoaded) throw new IllegalStateException("aspects are not loaded");
        return m_aspectMap.containsKey(name);
    }

    /**
     * Checks if a class has an <tt>AspectMetaData</tt>.
     *
     * @param className the name or the class
     * @return boolean
     */
    public boolean inTransformationScope(final String className) {
        if (className == null) throw new IllegalArgumentException("class name can not be null");
        if (m_transformationScopeSet.isEmpty()) {
            return true;
        }
        for (Iterator it = m_transformationScopeSet.iterator(); it.hasNext();) {
            String packageName = (String)it.next();
            if (className.startsWith(packageName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a class has an <tt>Mixin</tt>.
     *
     * @param classMetaData the class meta-data
     * @return boolean
     */
    public boolean hasIntroductions(final ClassMetaData classMetaData) {
        if (!m_aspectsLoaded) throw new IllegalStateException("aspects are not loaded");
        if (classMetaData == null) throw new IllegalArgumentException("class meta-data can not be null");

        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();
            for (Iterator it2 = aspectDef.getMethodIntroductions().iterator(); it2.hasNext();) {
                MethodIntroductionDefinition introDef = (MethodIntroductionDefinition)it2.next();

                IntroductionWeavingRule weavingRule = introDef.getWeavingRule();
                if (weavingRule.matchClassPointcut(classMetaData)) {
                    return true;
                }
            }
            for (Iterator it2 = aspectDef.getInterfaceIntroductions().iterator(); it2.hasNext();) {
                InterfaceIntroductionDefinition introDef = (InterfaceIntroductionDefinition)it2.next();
                if (introDef.getWeavingRule().matchClassPointcut(classMetaData)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a method has a <tt>MethodPointcut</tt>.
     * Only checks for a class match to allow early filtering.
     *
     * @param classMetaData the class meta-data
     * @return boolean
     */
    public boolean hasMethodPointcut(final ClassMetaData classMetaData) {
        if (!m_aspectsLoaded) throw new IllegalStateException("aspects are not loaded");
        if (classMetaData == null) throw new IllegalArgumentException("class meta-data can not be null");

        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();

            for (Iterator it2 = aspectDef.getAllAdvices().iterator(); it2.hasNext();) {
                AdviceDefinition adviceDef = (AdviceDefinition)it2.next();
                if (adviceDef.getWeavingRule().matchMethodPointcut(classMetaData)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a method has a <tt>MethodPointcut</tt>.
     *
     * @param classMetaData the class meta-data
     * @param methodMetaData the method meta-data
     * @return boolean
     */
    public boolean hasMethodPointcut(final ClassMetaData classMetaData,
                                     final MethodMetaData methodMetaData) {
        if (!m_aspectsLoaded) throw new IllegalStateException("aspects are not loaded");
        if (classMetaData == null) throw new IllegalArgumentException("class meta-data can not be null");
        if (methodMetaData == null) throw new IllegalArgumentException("method meta-data can not be null");

        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();
            for (Iterator it2 = aspectDef.getAllAdvices().iterator(); it2.hasNext();) {
                AdviceDefinition adviceDef = (AdviceDefinition)it2.next();
                if (adviceDef.getWeavingRule().matchMethodPointcut(classMetaData, methodMetaData)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a class has a <tt>GetFieldPointcut</tt>.
     * Only checks for a class match to allow early filtering.
     *
     * @TODO: how to know if it is a Set of a Get field pointcut
     *
     * @param classMetaData the class meta-data
     * @return boolean
     */
    public boolean hasGetFieldPointcut(final ClassMetaData classMetaData) {
        if (!m_aspectsLoaded) throw new IllegalStateException("aspects are not loaded");
        if (classMetaData == null) throw new IllegalArgumentException("class meta-data can not be null");

        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();
            for (Iterator it2 = aspectDef.getAllAdvices().iterator(); it2.hasNext();) {
                AdviceDefinition adviceDef = (AdviceDefinition)it2.next();
                if (adviceDef.getWeavingRule().matchGetFieldPointcut(classMetaData)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a class and field has a <tt>GetFieldPointcut</tt>.
     *
     * @TODO: how to know if it is a Set of a Get field pointcut
     *
     * @param classMetaData the class meta-data
     * @param fieldMetaData the name or the field
     * @return boolean
     */
    public boolean hasGetFieldPointcut(final ClassMetaData classMetaData,
                                       final FieldMetaData fieldMetaData) {
        if (!m_aspectsLoaded) throw new IllegalStateException("aspects are not loaded");
        if (classMetaData == null) throw new IllegalArgumentException("class meta-data can not be null");
        if (fieldMetaData == null) throw new IllegalArgumentException("field meta-data can not be null");

        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();
            for (Iterator it2 = aspectDef.getAllAdvices().iterator(); it2.hasNext();) {
                AdviceDefinition adviceDef = (AdviceDefinition)it2.next();
                if (adviceDef.getWeavingRule().matchGetFieldPointcut(classMetaData, fieldMetaData)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a class has a <tt>SetFieldPointcut</tt>.
     * Only checks for a class match to allow early filtering.
     *
     * @TODO: how to know if it is a Set of a Get field pointcut
     *
     * @param classMetaData the class meta-data
     * @return boolean
     */
    public boolean hasSetFieldPointcut(final ClassMetaData classMetaData) {
        if (!m_aspectsLoaded) throw new IllegalStateException("aspects are not loaded");
        if (classMetaData == null) throw new IllegalArgumentException("class meta-data can not be null");

        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();
            for (Iterator it2 = aspectDef.getAllAdvices().iterator(); it2.hasNext();) {
                AdviceDefinition adviceDef = (AdviceDefinition)it2.next();
                if (adviceDef.getWeavingRule().matchSetFieldPointcut(classMetaData)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a class and field has a <tt>SetFieldPointcut</tt>.
     *
     * @TODO: how to know if it is a Set of a Get field pointcut
     *
     * @param classMetaData the class meta-data
     * @param fieldMetaData the name or the field
     * @return boolean
     */
    public boolean hasSetFieldPointcut(final ClassMetaData classMetaData,
                                       final FieldMetaData fieldMetaData) {
        if (!m_aspectsLoaded) throw new IllegalStateException("aspects are not loaded");
        if (classMetaData == null) throw new IllegalArgumentException("class meta-data can not be null");
        if (fieldMetaData == null) throw new IllegalArgumentException("field meta-data can not be null");

        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();
            for (Iterator it2 = aspectDef.getAllAdvices().iterator(); it2.hasNext();) {
                AdviceDefinition adviceDef = (AdviceDefinition)it2.next();
                if (adviceDef.getWeavingRule().matchSetFieldPointcut(classMetaData, fieldMetaData)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a class and method has a <tt>ThrowsPointcut</tt>.
     * Only checks for a class match to allow early filtering.
     *
     * @param classMetaData the class meta-data
     * @return boolean
     */
    public boolean hasThrowsPointcut(final ClassMetaData classMetaData) {
        if (!m_aspectsLoaded) throw new IllegalStateException("aspects are not loaded");
        if (classMetaData == null) throw new IllegalArgumentException("class meta-data can not be null");

        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();
            for (Iterator it2 = aspectDef.getAllAdvices().iterator(); it2.hasNext();) {
                AdviceDefinition adviceDef = (AdviceDefinition)it2.next();
                if (adviceDef.getWeavingRule().matchThrowsPointcut(classMetaData)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a class and method has a <tt>ThrowsPointcut</tt>.
     *
     * @param classMetaData the class meta-data
     * @param methodMetaData the name or the method
     * @return boolean
     */
    public boolean hasThrowsPointcut(final ClassMetaData classMetaData,
                                     final MethodMetaData methodMetaData) {
        if (!m_aspectsLoaded) throw new IllegalStateException("aspects are not loaded");
        if (classMetaData == null) throw new IllegalArgumentException("class meta-data can not be null");
        if (methodMetaData == null) throw new IllegalArgumentException("method meta-data can not be null");

        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();
            for (Iterator it2 = aspectDef.getAllAdvices().iterator(); it2.hasNext();) {
                AdviceDefinition adviceDef = (AdviceDefinition)it2.next();
                if (adviceDef.getWeavingRule().matchThrowsPointcut(classMetaData, methodMetaData)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a class should care about advising caller side method invocations.
     * This method matches the caller class (when the isCallerSideMethod matches the callee class)
     *
     * @param classMetaData the class meta-data
     * @return boolean
     */
    public boolean hasCallerSidePointcut(final ClassMetaData classMetaData) {
        if (!m_aspectsLoaded) throw new IllegalStateException("aspects are not loaded");
        if (classMetaData == null) throw new IllegalArgumentException("class meta-data can not be null");

        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();
            for (Iterator it2 = aspectDef.getAllAdvices().iterator(); it2.hasNext();) {
                AdviceDefinition adviceDef = (AdviceDefinition)it2.next();
                if (adviceDef.getWeavingRule().matchCallerSidePointcut(classMetaData)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a method is a defined as a caller side method.
     * This method matches the callee class (when the hasCallerSideMethod matches the caller class)
     *
     * @param classMetaData the class meta-data
     * @param methodMetaData the name or the method
     * @return boolean
     */
    public boolean isCallerSideMethod(final ClassMetaData classMetaData,
                                      final MethodMetaData methodMetaData) {
        if (!m_aspectsLoaded) throw new IllegalStateException("aspects are not loaded");
        if (classMetaData == null) throw new IllegalArgumentException("class meta-data can not be null");
        if (methodMetaData == null) throw new IllegalArgumentException("method meta-data can not be null");

        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();
            for (Iterator it2 = aspectDef.getAllAdvices().iterator(); it2.hasNext();) {
                AdviceDefinition adviceDef = (AdviceDefinition)it2.next();
                if (adviceDef.getWeavingRule().matchCallerSidePointcut(
                        classMetaData, methodMetaData)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns the interface introductions for a certain class.
     *
     * @param classMetaData the class meta-data
     * @return the names
     */
    public List getInterfaceIntroductions(final ClassMetaData classMetaData) {
        if (!m_aspectsLoaded) throw new IllegalStateException("aspects are not loaded");
        if (classMetaData == null) throw new IllegalArgumentException("class meta-data can not be null");

        List introductionDefs = new ArrayList();
        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();
            for (Iterator it2 = aspectDef.getInterfaceIntroductions().iterator(); it2.hasNext();) {
                InterfaceIntroductionDefinition introDef = (InterfaceIntroductionDefinition)it2.next();
                if (introDef.getWeavingRule().matchClassPointcut(classMetaData)) {
                    introductionDefs.add(introDef);
                }
            }
        }
        return introductionDefs;
    }

    /**
     * Returns the names of the introductions for a certain class.
     *
     * @param className the name of the class
     * @return the names
     */
    public List getIntroductionNamesForClass(final String className) {
        if (!m_aspectsLoaded) throw new IllegalStateException("aspects are not loaded");
        throw new UnsupportedOperationException("method not support by this definition implementation");
//        if (className == null) throw new IllegalArgumentException("class name can not be null");
//        List introductionNames = new ArrayList();
//        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
//            AspectDefinition definition = (AspectDefinition)it.next();
//            List weavingRules = definition.getIntroductionWeavingRules();
//            for (Iterator it2 = weavingRules.iterator(); it2.hasNext();) {
//                IntroductionWeavingRule weavingRule = (IntroductionWeavingRule)it2.next();
//                if (weavingRule.getRegexpClassPattern().matches(className)) {
//                    introductionNames.addAll(weavingRule.getIntroductionRefs());
//                }
//            }
//        }
//        return introductionNames;
    }

    /**
     * Builds up a meta-data repository for the mixins.
     *
     * @param repository the repository
     * @param loader the class loader to use
     */
    public void buildMixinMetaDataRepository(final Set repository, final ClassLoader loader) {
        loadAspects(loader);

        Set definedAspects = getDefinedAspectClassNames();
        for (Iterator it = definedAspects.iterator(); it.hasNext();) {
            String className = (String)it.next();
            try {
                Class mixin = loader.loadClass(className);
                ClassMetaData metaData = ReflectionMetaDataMaker.createClassMetaData(mixin);
                repository.add(metaData);
            }
            catch (ClassNotFoundException e) {
                ;// ignore
            }
        }
    }

    /**
     * Loads and parser the aspect.
     *
     * @param aspectClassName the class name of the aspect
     * @param loader the class loader to use
     */
    private void loadAspect(final String aspectClassName, final ClassLoader loader) {
        try {
            Class klass = loader.loadClass(aspectClassName);
            AspectDefinition aspectDef = m_attributeParser.parse(klass);
            addAspect(aspectDef);
        }
        catch (ClassNotFoundException e) {
            throw new WrappedRuntimeException(e);
        }
    }
}

