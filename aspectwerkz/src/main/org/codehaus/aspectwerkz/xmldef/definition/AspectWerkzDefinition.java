/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.xmldef.definition;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Iterator;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.io.File;
import java.io.Serializable;
import java.io.InputStream;
import java.net.URL;

import gnu.trove.TObjectIntHashMap;
import org.dom4j.Document;

import org.codehaus.aspectwerkz.xmldef.AspectWerkz;
import org.codehaus.aspectwerkz.exception.DefinitionException;
import org.codehaus.aspectwerkz.metadata.MethodMetaData;
import org.codehaus.aspectwerkz.metadata.FieldMetaData;
import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.ContextClassLoader;
import org.codehaus.aspectwerkz.util.SequencedHashMap;

/**
 * Implements the <code>AspectWerkz</code> definition.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
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
     * Holds the indexes for the introductions. The introduction indexes are needed here
     * (instead of in the AspectWerkz class like the advice indexes) since they need to
     * be available to the transformers before the AspectWerkz system has been initialized.
     */
    private final TObjectIntHashMap m_introductionIndexes = new TObjectIntHashMap();

    /**
     * Set with all the class names of the aspects to use.
     */
    private final Set m_aspectsToUse = new HashSet();

    /**
     * Maps the introductions to it's name.
     */
    private final Map m_introductionMap = new SequencedHashMap();

    /**
     * Maps the advices to it's name.
     */
    private final Map m_adviceMap = new HashMap();

    /**
     * Maps the aspects to it's name.
     */
    private final Map m_aspectMap = new SequencedHashMap();

    /**
     * The abstract advice definitions.
     */
    private final Map m_abstractAdviceMap = new HashMap();

    /**
     * Maps the advice stacks to it's name.
     */
    private final Map m_adviceStackMap = new HashMap();

    /**
     * The UUID for this definition.
     */
    private String m_uuid = "default";

    /**
     * The transformation scopes.
     */
    private final Set m_transformationScopeSet = new HashSet();

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
     * @param packageName the name of the package
     */
    public static void createClassPattern(final String pattern,
                                          final PointcutDefinition pointcutDef,
                                          final String packageName) {
        String classPattern = packageName + "." + pattern;
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
     * @param packageName the name of the package
     */
    public static void createMethodPattern(final String pattern,
                                           final PointcutDefinition pointcutDef,
                                           final String packageName) {
        int indexFirstSpace = pattern.indexOf(' ');
        String returnType = pattern.substring(0, indexFirstSpace + 1);
        String classNameWithMethodName = pattern.substring(
                indexFirstSpace, pattern.indexOf('(')).trim();
        String parameterTypes = pattern.substring(
                pattern.indexOf('('), pattern.length()).trim();
        int indexLastDot = classNameWithMethodName.lastIndexOf('.');

        final String methodPattern = classNameWithMethodName.substring(
                indexLastDot + 1, classNameWithMethodName.length()).trim();
        String classPattern = packageName + classNameWithMethodName.substring(0, indexLastDot);
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
     * @param packageName the name of the package
     */
    public static void createFieldPattern(final String pattern,
                                          final PointcutDefinition pointcutDef,
                                          final String packageName) {
        int indexFirstSpace = pattern.indexOf(' ');
        String fieldType = pattern.substring(0, indexFirstSpace + 1);
        String classNameWithFieldName = pattern.substring(
                indexFirstSpace, pattern.length()).trim();
        int indexLastDot = classNameWithFieldName.lastIndexOf('.');

        final String fieldPattern = classNameWithFieldName.substring(
                indexLastDot + 1, classNameWithFieldName.length()).trim();
        String classPattern = packageName + classNameWithFieldName.substring(0, indexLastDot).trim();
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
     * @param packageName the name of the package
     */
    public static void createThrowsPattern(final String pattern,
                                           final PointcutDefinition pointcutDef,
                                           final String packageName) {
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
        String classPattern = packageName + classNameWithMethodName.substring(0, indexLastDot);
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
                                               final PointcutDefinition pointcutDef,
                                               final String packageName) {
        if (pattern.indexOf('>') == -1) {
            pattern = "*->" + pattern; // if no caller side pattern is specified => default to *
        }

        String callerClassPattern = packageName + pattern.substring(0, pattern.indexOf('-')).trim();
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
        String calleeClassPattern = packageName + classNameWithMethodName.substring(0, indexLastDot);

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
        AspectDefinition systemAspect = new AspectDefinition();
        systemAspect.setName(SYSTEM_ASPECT);
        synchronized (m_aspectMap) {
            m_aspectMap.put(SYSTEM_ASPECT, systemAspect);
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
     * Returns a collection with the abstract aspect definitions registered.
     *
     * @return the abstract aspect definitions
     */
    public Collection getAbstractAspectDefinitions() {
        return m_abstractAdviceMap.values();
    }

    /**
     * Returns a collection with the aspect definitions registered.
     *
     * @return the aspect definitions
     */
    public Collection getAspectDefinitions() {
        return m_aspectMap.values();
    }

    /**
     * Returns a collection with the introduction definitions registered.
     *
     * @return the introduction definitions
     */
    public Collection getIntroductionDefinitions() {
        return m_introductionMap.values();
    }

    /**
     * Returns a collection with the advice definitions registered.
     *
     * @return the advice definitions
     */
    public Collection getAdviceDefinitions() {
        return m_adviceMap.values();
    }

    /**
     * Finds an advice stack definition by its name.
     *
     * @param adviceStackName the advice stack name
     * @return the definition
     */
    public AdviceStackDefinition getAdviceStackDefinition(final String adviceStackName) {
        return (AdviceStackDefinition)m_adviceStackMap.get(adviceStackName);
    }

    /**
     * Returns a specific abstract aspect definition.
     *
     * @param name the name of the abstract aspect definition
     * @return the abstract aspect definition
     */
    public AspectDefinition getAbstractAspectDefinition(final String name) {
        return (AspectDefinition)m_abstractAdviceMap.get(name);
    }

    /**
     * Returns a specific aspect definition.
     *
     * @param name the name of the aspect definition
     * @return the aspect definition
     */
    public AspectDefinition getAspectDefinition(final String name) {
        return (AspectDefinition)m_aspectMap.get(name);
    }

    /**
     * Returns the names of the target classes.
     *
     * @return the names of the target classes
     */
    public String[] getAspectTargetClassNames() {
        String[] classNames = new String[m_aspectMap.keySet().size()];
        int i = 0;
        for (Iterator it = m_aspectMap.keySet().iterator(); it.hasNext();) {
            String key = (String)it.next();
            classNames[i++] = key;
        }
        return classNames;
    }

    /**
     * Returns a specific advice definition.
     *
     * @param name the name of the advice definition
     * @return the advice definition
     */
    public AdviceDefinition getAdviceDefinition(final String name) {
        for (Iterator it = m_adviceMap.values().iterator(); it.hasNext();) {
            AdviceDefinition adviceDefinition = (AdviceDefinition)it.next();
            if (adviceDefinition.getName().equals(name)) {
                return adviceDefinition;
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
    public String getAdviceNameByAttribute(final String attribute) {
        if (attribute == null) return null;
        for (Iterator it = m_adviceMap.values().iterator(); it.hasNext();) {
            AdviceDefinition adviceDefinition = (AdviceDefinition)it.next();
            if (adviceDefinition.getAttribute().equals(attribute)) {
                return adviceDefinition.getName();
            }
        }
        return null;
    }

    /**
     * Finds the name of an introduction by its attribute.
     *
     * @param attribute the attribute
     * @return the name of the introduction
     */
    public String getIntroductionNameByAttribute(final String attribute) {
        if (attribute == null) {
            return null;
        }
        for (Iterator it = m_introductionMap.values().iterator(); it.hasNext();) {
            IntroductionDefinition introductionDefinition = (IntroductionDefinition)it.next();
            if (introductionDefinition.getAttribute().equals(attribute)) {
                return introductionDefinition.getName();
            }
        }
        return null;
    }

    /**
     * Returns the name of the interface for an introduction.
     *
     * @param introductionName the name of the introduction
     * @return the name of the interface
     */
    public String getIntroductionInterfaceName(final String introductionName) {
        if (introductionName == null) throw new IllegalArgumentException("introduction name can not be null");
        if (!m_introductionMap.containsKey(introductionName)) {
            return null;
        }
        return ((IntroductionDefinition)m_introductionMap.get(introductionName)).getInterface();
    }

    /**
     * Returns the name of the implementation for an introduction.
     *
     * @param introductionName the name of the introduction
     * @return the name of the interface
     */
    public String getIntroductionImplName(final String introductionName) {
        if (introductionName == null) throw new IllegalArgumentException("introduction name can not be null");
        if (!m_introductionMap.containsKey(introductionName)) {
            return null;
        }
        return ((IntroductionDefinition)m_introductionMap.get(introductionName)).getImplementation();
    }

    /**
     * Returns a specific introduction definition.
     *
     * @param introductionName the name of the introduction
     * @return the introduction definition
     */
    public IntroductionDefinition getIntroductionDefinition(final String introductionName) {
        if (introductionName == null) throw new IllegalArgumentException("introduction name can not be null");
        return (IntroductionDefinition)m_introductionMap.get(introductionName);
    }

    /**
     * Returns the index for a specific introduction.
     *
     * @param introductionName the name of the introduction
     * @return the index
     */
    public int getIntroductionIndex(final String introductionName) {
        if (introductionName == null) throw new IllegalArgumentException("introduction name can not be null");
        return m_introductionIndexes.get(introductionName);
    }

    /**
     * Returns the indexes for the introductions.
     *
     * @return the indexes
     */
    public TObjectIntHashMap getIntroductionIndexes() {
        return m_introductionIndexes;
    }

    /**
     * Returns the class name for the join point controller, if there is a match.
     * @param classMetaData the class meta-data
     * @param methodMetaData the method meta-data
     * @return the controller class name
     */
    public String getJoinPointController(final ClassMetaData classMetaData,
                                         final MethodMetaData methodMetaData) {
        if (classMetaData == null) throw new IllegalArgumentException("class meta-data can not be null");
        if (methodMetaData == null) throw new IllegalArgumentException("method meta-data can not be null");

        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();
            if (aspectDef.isAbstract()) {
                continue;
            }
            Collection controllerDefs = aspectDef.getControllerDefs();
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
    public Set getAspectsToUse() {
        return m_aspectsToUse;
    }

    /**
     * Adds a new aspect to use.
     *
     * @param className the class name of the aspect
     */
    public void addAspectToUse(final String className) {
        m_aspectsToUse.add(className);
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
     * Adds an abstract aspect definition.
     *
     * @param aspect a new abstract aspect definition
     */
    public void addAbstractAspect(final AspectDefinition aspect) {
        synchronized (m_abstractAdviceMap) {
            m_abstractAdviceMap.put(aspect.getName(), aspect);
        }
    }

    /**
     * Adds an aspect definition.
     *
     * @param aspect a new aspect definition
     */
    public void addAspect(final AspectDefinition aspect) {
        synchronized (m_aspectMap) {
            m_aspectMap.put(aspect.getName(), aspect);
        }
    }

    /**
     * Adds an advice stack definition.
     *
     * @param adviceStackDef the advice stack definition
     */
    public void addAdviceStack(final AdviceStackDefinition adviceStackDef) {
        synchronized (m_adviceStackMap) {
            m_adviceStackMap.put(adviceStackDef.getName(), adviceStackDef);
        }
    }

    /**
     * Adds an advice definition.
     *
     * @param advice the advice definition
     */
    public void addAdvice(final AdviceDefinition advice) {
        synchronized (m_adviceMap) {
            m_adviceMap.put(advice.getName(), advice);
        }
    }

    /**
     * Adds a new introductions definition.
     *
     * @param introduction the introduction definition
     */
    public void addIntroduction(final IntroductionDefinition introduction) {
        if (m_introductionIndexes.containsKey(introduction.getName())) {
            return;
        }
        synchronized (m_introductionMap) {
            synchronized (m_introductionIndexes) {
                final int index = m_introductionMap.values().size() + 1;
                m_introductionIndexes.put(introduction.getName(), index);
                m_introductionMap.put(introduction.getName(), introduction);
            }
        }
    }

    /**
     * Checks if there exists an advice with the name specified.
     *
     * @param name the name of the advice
     * @return boolean
     */
    public boolean hasAdvice(final String name) {
        return m_adviceMap.containsKey(name);
    }

    /**
     * Checks if there exists an introduction with the name specified.
     *
     * @param name the name of the introduction
     * @return boolean
     */
    public boolean hasIntroduction(final String name) {
        return m_introductionMap.containsKey(name);
    }

    /**
     * Checks if a class has an <tt>Aspect</tt>.
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
     * Checks if a class has an <tt>Introduction</tt>.
     *
     * @param className the name or the class
     * @return boolean
     */
    public boolean hasIntroductions(final String className) {
        if (className == null) throw new IllegalArgumentException("class name can not be null");
        for (Iterator it1 = m_aspectMap.values().iterator(); it1.hasNext();) {
            AspectDefinition aspectDefinition = (AspectDefinition)it1.next();
            List weavingRules = aspectDefinition.getIntroductionWeavingRules();
            for (Iterator it2 = weavingRules.iterator(); it2.hasNext();) {
                IntroductionWeavingRule weavingRule = (IntroductionWeavingRule)it2.next();
                if (weavingRule.getRegexpClassPattern().matches(className)) {
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
        if (classMetaData == null) throw new IllegalArgumentException("class meta-data can not be null");

        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();
            if (aspectDef.isAbstract()) {
                continue;
            }
            List weavingRules = aspectDef.getAdviceWeavingRules();
            for (Iterator it2 = weavingRules.iterator(); it2.hasNext();) {
                AdviceWeavingRule weavingRule = (AdviceWeavingRule)it2.next();
                if (weavingRule.matchMethodPointcut(classMetaData)) {
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
        if (classMetaData == null) throw new IllegalArgumentException("class meta-data can not be null");
        if (methodMetaData == null) throw new IllegalArgumentException("method meta-data can not be null");

        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();
            if (aspectDef.isAbstract()) {
                continue;
            }
            List weavingRules = aspectDef.getAdviceWeavingRules();
            for (Iterator it2 = weavingRules.iterator(); it2.hasNext();) {
                AdviceWeavingRule weavingRule = (AdviceWeavingRule)it2.next();
                if (weavingRule.matchMethodPointcut(classMetaData, methodMetaData)) {
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
     * @param classMetaData the class meta-data
     * @return boolean
     */
    public boolean hasGetFieldPointcut(final ClassMetaData classMetaData) {
        if (classMetaData == null) throw new IllegalArgumentException("class meta-data can not be null");

        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();
            if (aspectDef.isAbstract()) {
                continue;
            }
            List weavingRules = aspectDef.getAdviceWeavingRules();
            for (Iterator it2 = weavingRules.iterator(); it2.hasNext();) {
                AdviceWeavingRule weavingRule = (AdviceWeavingRule)it2.next();
                if (weavingRule.matchGetFieldPointcut(classMetaData)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a class and field has a <tt>GetFieldPointcut</tt>.
     *
     * @param classMetaData the class meta-data
     * @param fieldMetaData the name or the field
     * @return boolean
     */
    public boolean hasGetFieldPointcut(final ClassMetaData classMetaData,
                                       final FieldMetaData fieldMetaData) {
        if (classMetaData == null) throw new IllegalArgumentException("class meta-data can not be null");
        if (fieldMetaData == null) throw new IllegalArgumentException("field meta-data can not be null");

        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();
            if (aspectDef.isAbstract()) {
                continue;
            }
            List weavingRules = aspectDef.getAdviceWeavingRules();
            for (Iterator it2 = weavingRules.iterator(); it2.hasNext();) {
                AdviceWeavingRule weavingRule = (AdviceWeavingRule)it2.next();
                if (weavingRule.matchGetFieldPointcut(classMetaData, fieldMetaData)) {
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
     * @param classMetaData the class meta-data
     * @return boolean
     */
    public boolean hasSetFieldPointcut(final ClassMetaData classMetaData) {
        if (classMetaData == null) throw new IllegalArgumentException("class meta-data can not be null");

        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();
            if (aspectDef.isAbstract()) {
                continue;
            }
            List weavingRules = aspectDef.getAdviceWeavingRules();
            for (Iterator it2 = weavingRules.iterator(); it2.hasNext();) {
                AdviceWeavingRule weavingRule = (AdviceWeavingRule)it2.next();
                if (weavingRule.matchSetFieldPointcut(classMetaData)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a class and field has a <tt>SetFieldPointcut</tt>.
     *
     * @param classMetaData the class meta-data
     * @param fieldMetaData the name or the field
     * @return boolean
     */
    public boolean hasSetFieldPointcut(final ClassMetaData classMetaData,
                                       final FieldMetaData fieldMetaData) {
        if (classMetaData == null) throw new IllegalArgumentException("class meta-data can not be null");
        if (fieldMetaData == null) throw new IllegalArgumentException("field meta-data can not be null");

        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();
            if (aspectDef.isAbstract()) {
                continue;
            }
            List weavingRules = aspectDef.getAdviceWeavingRules();
            for (Iterator it2 = weavingRules.iterator(); it2.hasNext();) {
                AdviceWeavingRule weavingRule = (AdviceWeavingRule)it2.next();
                if (weavingRule.matchSetFieldPointcut(classMetaData, fieldMetaData)) {
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
        if (classMetaData == null) throw new IllegalArgumentException("class meta-data can not be null");

        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();
            if (aspectDef.isAbstract()) {
                continue;
            }
            List weavingRules = aspectDef.getAdviceWeavingRules();
            for (Iterator it2 = weavingRules.iterator(); it2.hasNext();) {
                AdviceWeavingRule weavingRule = (AdviceWeavingRule)it2.next();
                if (weavingRule.matchThrowsPointcut(classMetaData)) {
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
        if (classMetaData == null) throw new IllegalArgumentException("class meta-data can not be null");
        if (methodMetaData == null) throw new IllegalArgumentException("method meta-data can not be null");

        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();
            if (aspectDef.isAbstract()) {
                continue;
            }
            List weavingRules = aspectDef.getAdviceWeavingRules();
            for (Iterator it2 = weavingRules.iterator(); it2.hasNext();) {
                AdviceWeavingRule weavingRule = (AdviceWeavingRule)it2.next();
                if (weavingRule.matchThrowsPointcut(classMetaData, methodMetaData)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a class should care about advising caller side method invocations.
     *
     * @param classMetaData the class meta-data
     * @return boolean
     */
    public boolean hasCallerSidePointcut(final ClassMetaData classMetaData) {
        if (classMetaData == null) throw new IllegalArgumentException("class meta-data can not be null");
        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDefinition = (AspectDefinition)it.next();
            Collection pointcuts = aspectDefinition.getPointcutDefs();
            for (Iterator it2 = pointcuts.iterator(); it2.hasNext();) {
                PointcutDefinition pointcutDefinition = (PointcutDefinition)it2.next();
                if ((pointcutDefinition.getType().equalsIgnoreCase(PointcutDefinition.CALLER_SIDE) ||
                        pointcutDefinition.getType().equalsIgnoreCase(PointcutDefinition.CFLOW)) &&
                        pointcutDefinition.getRegexpClassPattern().matches(classMetaData.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a method is a defined as a caller side method.
     *
     * @param classMetaData the class meta-data
     * @param methodMetaData the name or the method
     * @return boolean
     */
    public boolean isCallerSideMethod(final ClassMetaData classMetaData,
                                      final MethodMetaData methodMetaData) {
        if (classMetaData == null) throw new IllegalArgumentException("class meta-data can not be null");
        if (methodMetaData == null) throw new IllegalArgumentException("method meta-data can not be null");

        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition aspectDef = (AspectDefinition)it.next();
            if (aspectDef.isAbstract()) {
                continue;
            }
            List weavingRules = aspectDef.getAdviceWeavingRules();
            for (Iterator it2 = weavingRules.iterator(); it2.hasNext();) {
                AdviceWeavingRule weavingRule = (AdviceWeavingRule)it2.next();
                if (weavingRule.matchCallerSidePointcut(classMetaData, methodMetaData)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns the names of the introductions for a certain class.
     *
     * @param className the name of the class
     * @return the names
     */
    public List getIntroductionNames(final String className) {
        if (className == null) throw new IllegalArgumentException("class name can not be null");
        List introductionNames = new ArrayList();
        for (Iterator it = m_aspectMap.values().iterator(); it.hasNext();) {
            AspectDefinition definition = (AspectDefinition)it.next();
            List weavingRules = definition.getIntroductionWeavingRules();
            for (Iterator it2 = weavingRules.iterator(); it2.hasNext();) {
                IntroductionWeavingRule weavingRule = (IntroductionWeavingRule)it2.next();
                if (weavingRule.getRegexpClassPattern().matches(className)) {
                    introductionNames.addAll(weavingRule.getIntroductionRefs());
                }
            }
        }
        return introductionNames;
    }
}

