/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition;

import java.io.InputStream;
import java.io.File;
import java.net.URL;
import java.util.Enumeration;
import java.util.Set;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.dom4j.Document;
import gnu.trove.TObjectIntHashMap;

import org.codehaus.aspectwerkz.ContextClassLoader;
import org.codehaus.aspectwerkz.xmldef.definition.XmlDefinitionParser;
import org.codehaus.aspectwerkz.xmldef.definition.PointcutDefinition;
import org.codehaus.aspectwerkz.xmldef.definition.AdviceStackDefinition;
import org.codehaus.aspectwerkz.xmldef.definition.AspectDefinition;
import org.codehaus.aspectwerkz.xmldef.definition.AdviceDefinition;
import org.codehaus.aspectwerkz.xmldef.definition.IntroductionDefinition;
import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.metadata.MethodMetaData;
import org.codehaus.aspectwerkz.metadata.FieldMetaData;
import org.codehaus.aspectwerkz.exception.DefinitionException;

/**
 * Abstract base class for the aspectwerkz definition.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public abstract class AbstractAspectWerkzDefinition implements AspectWerkzDefinition {

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
     * The name of the system aspect.
     */
    public static final String SYSTEM_ASPECT = "org/codehaus/aspectwerkz/system";

    /**
     * The aspectwerkz definitions.
     */
    private static Map s_definitions = new HashMap();

    /**
     * Creates, caches and returns new definition. Loads the definition in the file specified.
     *
     * @param document the DOM document containing the definition
     * @return the aspectwerkz definition
     */
    public static AspectWerkzDefinition createDefinition(final Document document) {
        if (document == null) throw new IllegalArgumentException("definition document can not be null");
        final AspectWerkzDefinition definition =
                AbstractAspectWerkzDefinition.loadDefinitionAsDocument(document);
        AbstractAspectWerkzDefinition.s_definitions.put(definition.getUuid(), definition);
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
        if (AbstractAspectWerkzDefinition.s_definitions.containsKey(DEFAULT_SYSTEM)) {
            return (AspectWerkzDefinition)AbstractAspectWerkzDefinition.s_definitions.get(DEFAULT_SYSTEM);
        }
        final boolean isDirty = false;
        final AspectWerkzDefinition definition;
        if (AbstractAspectWerkzDefinition.DEFINITION_FILE == null) {
            // no definition file is specified => try to locate the weave model as a resource on the classpath
            definition = AbstractAspectWerkzDefinition.loadDefinitionAsResource();
        }
        else {
            // definition file is specified => create a weave model in memory
            definition = AbstractAspectWerkzDefinition.loadDefinitionFromFile(isDirty);
        }

        if (isDirty || !AbstractAspectWerkzDefinition.s_definitions.containsKey(DEFAULT_SYSTEM)) {
            synchronized (AbstractAspectWerkzDefinition.s_definitions) {
                AbstractAspectWerkzDefinition.s_definitions.put(DEFAULT_SYSTEM, definition);
            }
        }
        return (AspectWerkzDefinition)AbstractAspectWerkzDefinition.s_definitions.get(DEFAULT_SYSTEM);
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
        if (AbstractAspectWerkzDefinition.s_definitions.containsKey(uuid)) {
            return (AspectWerkzDefinition)AbstractAspectWerkzDefinition.s_definitions.get(uuid);
        }

        final boolean isDirty = false;
        final AspectWerkzDefinition definition;
        if (AbstractAspectWerkzDefinition.DEFINITION_FILE == null) {
            // no definition file is specified => try to locate the definition as a resource on the classpath
            definition = AbstractAspectWerkzDefinition.loadDefinitionAsResource();
        }
        else {
            // definition file is specified => create one in memory
            definition = AbstractAspectWerkzDefinition.loadDefinitionFromFile(isDirty);
        }

        // check the we have found the right definition
        if (!uuid.equals(definition.getUuid())) {
            throw new RuntimeException("could not find definition with UUID <" + uuid + "> (found definition with UUID <" + definition.getUuid() + ">)");
        }

        if (isDirty || !AbstractAspectWerkzDefinition.s_definitions.containsKey(uuid)) {
            synchronized (AbstractAspectWerkzDefinition.s_definitions) {
                AbstractAspectWerkzDefinition.s_definitions.put(uuid, definition);
            }
        }
        return (AspectWerkzDefinition)AbstractAspectWerkzDefinition.s_definitions.get(uuid);
    }

    /**
     * Loads the definition from file.
     *
     * @param useCache use cache
     * @return the definition
     */
    private static AspectWerkzDefinition loadDefinitionFromFile(final boolean useCache) {
        return AbstractAspectWerkzDefinition.loadDefaultDefinitionAsFile(useCache);
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
        InputStream stream = AbstractAspectWerkzDefinition.getDefinitionInputStream();
        if (stream == null) throw new RuntimeException("either you have to specify an XML definition file using the -Daspectwerkz.definition.file=... option or you have to have the XML definition file <aspectwerkz.xml> somewhere on the classpath");
        return AbstractAspectWerkzDefinition.loadDefinitionAsStream(stream);
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
        if (AbstractAspectWerkzDefinition.DEFINITION_FILE == null) {
            URL definition = ContextClassLoader.loadResource(AbstractAspectWerkzDefinition.DEFAULT_DEFINITION_FILE_NAME);
            if (definition == null) throw new DefinitionException("definition file could not be found on classpath (either specify the file by using the -Daspectwerkz.definition.file=.. option or by having a definition file called aspectwerkz.xml somewhere on the classpath)");
            definitionFileName = definition.getFile();
        }
        else {
            definitionFileName = AbstractAspectWerkzDefinition.DEFINITION_FILE;
        }
        return AbstractAspectWerkzDefinition.loadDefinitionAsFile(definitionFileName, isDirty);
    }

    /**
     * Returns the definition.
     *
     * @param definitionFile the definition file
     * @return the definition
     */
    public static AspectWerkzDefinition loadDefinitionAsFile(final String definitionFile) {
        return AbstractAspectWerkzDefinition.loadDefinitionAsFile(definitionFile, false);
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
        return ContextClassLoader.getResourceAsStream(AbstractAspectWerkzDefinition.DEFAULT_DEFINITION_FILE_NAME);
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
     * Loads and merges the definition.
     *
     * @param loader the class loader to use
     * @return the aspectwerkz definition
     */
    public static AspectWerkzDefinition loadAndMergeDefinitions(final ClassLoader loader) {
        AspectWerkzDefinition definition = null;
        try {
            Enumeration definitions = loader.getResources(
                    "META-INF/" +
                    AbstractAspectWerkzDefinition.DEFAULT_DEFINITION_FILE_NAME
            );

            // grab the definition in the current class loader
            Document document = null;
            if (definitions.hasMoreElements()) {
                URL url = (URL)definitions.nextElement();
                document = XmlDefinitionParser.createDocument(url);
            }

            // merge the definition with the definitions in class loaders
            // higher up in the class loader hierachy
            while (definitions.hasMoreElements()) {
                document = XmlDefinitionParser.mergeDocuments(
                        document,
                        XmlDefinitionParser.createDocument((URL)definitions.nextElement())
                );
            }

            // handle the merging of the 'aspectwerkz.xml' definition on the classpath
            // (if there is one)
            InputStream stream = getDefinitionInputStream();
            if (stream != null) {
                document = XmlDefinitionParser.mergeDocuments(
                        document,
                        XmlDefinitionParser.createDocument(stream)
                );
            }

            // handle the merging of the definition file specified using the JVM option
            if (AbstractAspectWerkzDefinition.DEFINITION_FILE != null) {
                document = XmlDefinitionParser.mergeDocuments(
                        document,
                        XmlDefinitionParser.createDocument(new File(AbstractAspectWerkzDefinition.DEFINITION_FILE).toURL())
                );
            }

            // create a new definition based on the merged definition documents
            definition = createDefinition(document);
        }
        catch (Exception e) {
            ;// ignore
        }
        return definition;
    }

    /**
     * Sets the UUID for the definition.
     *
     * @param uuid the UUID
     */
    public abstract void setUuid(String uuid);

    /**
     * Returns the UUID for the definition.
     *
     * @return the UUID
     */
    public abstract String getUuid();

    /**
     * Returns the transformation scopes.
     *
     * @return the transformation scopes
     */
    public abstract Set getTransformationScopes();

    /**
     * Returns a collection with the abstract aspect definitions registered.
     *
     * @return the abstract aspect definitions
     */
    public abstract Collection getAbstractAspectDefinitions();

    /**
     * Returns a collection with the aspect definitions registered.
     *
     * @return the aspect definitions
     */
    public abstract Collection getAspectDefinitions();

    /**
     * Returns a collection with the introduction definitions registered.
     *
     * @return the introduction definitions
     */
    public abstract Collection getIntroductionDefinitions();

    /**
     * Returns a collection with the advice definitions registered.
     *
     * @return the advice definitions
     */
    public abstract Collection getAdviceDefinitions();

    /**
     * Finds an advice stack definition by its name.
     *
     * @param adviceStackName the advice stack name
     * @return the definition
     */
    public abstract AdviceStackDefinition getAdviceStackDefinition(String adviceStackName);

    /**
     * Returns a specific abstract aspect definition.
     *
     * @param name the name of the abstract aspect definition
     * @return the abstract aspect definition
     */
    public abstract AspectDefinition getAbstractAspectDefinition(String name);

    /**
     * Returns a specific aspect definition.
     *
     * @param name the name of the aspect definition
     * @return the aspect definition
     */
    public abstract AspectDefinition getAspectDefinition(String name);

    /**
     * Returns the names of the target classes.
     *
     * @return the names of the target classes
     */
    public abstract String[] getAspectTargetClassNames();

    /**
     * Returns a specific advice definition.
     *
     * @param name the name of the advice definition
     * @return the advice definition
     */
    public abstract AdviceDefinition getAdviceDefinition(String name);

    /**
     * Finds the name of an advice by its attribute.
     *
     * @param attribute the attribute
     * @return the name of the advice
     */
    public abstract String getAdviceNameByAttribute(String attribute);

    /**
     * Finds the name of an introduction by its attribute.
     *
     * @param attribute the attribute
     * @return the name of the introduction
     */
    public abstract String getIntroductionNameByAttribute(String attribute);

    /**
     * Returns the name of the interface for an introduction.
     *
     * @param introductionName the name of the introduction
     * @return the name of the interface
     */
    public abstract String getIntroductionInterfaceName(String introductionName);

    /**
     * Returns the name of the implementation for an introduction.
     *
     * @param introductionName the name of the introduction
     * @return the name of the interface
     */
    public abstract String getIntroductionImplName(String introductionName);

    /**
     * Returns a specific introduction definition.
     *
     * @param introductionName the name of the introduction
     * @return the introduction definition
     */
    public abstract IntroductionDefinition getIntroductionDefinition(String introductionName);

    /**
     * Returns the index for a specific introduction.
     *
     * @param introductionName the name of the introduction
     * @return the index
     */
    public abstract int getIntroductionIndex(String introductionName);

    /**
     * Returns the indexes for the introductions.
     *
     * @return the indexes
     */
    public abstract TObjectIntHashMap getIntroductionIndexes();

    /**
     * Returns the class name for the join point controller, if there is a match.
     * @param classMetaData the class meta-data
     * @param methodMetaData the method meta-data
     * @return the controller class name
     */
    public abstract String getJoinPointController(ClassMetaData classMetaData,
                                                  MethodMetaData methodMetaData);

    /**
     * Returns a set with the aspects to use.
     *
     * @return the aspects to use
     */
    public abstract Set getAspectsToUse();

    /**
     * Adds a new aspect to use.
     *
     * @param className the class name of the aspect
     */
    public abstract void addAspectToUse(String className);

    /**
     * Adds a new transformation scope.
     *
     * @param transformationScope the new scope
     */
    public abstract void addTransformationScope(String transformationScope);

    /**
     * Adds an abstract aspect definition.
     *
     * @param aspect a new abstract aspect definition
     */
    public abstract void addAbstractAspect(AspectDefinition aspect);

    /**
     * Adds an aspect definition.
     *
     * @param aspect a new aspect definition
     */
    public abstract void addAspect(AspectDefinition aspect);

    /**
     * Adds an advice stack definition.
     *
     * @param adviceStackDef the advice stack definition
     */
    public abstract void addAdviceStack(AdviceStackDefinition adviceStackDef);

    /**
     * Adds an advice definition.
     *
     * @param advice the advice definition
     */
    public abstract void addAdvice(AdviceDefinition advice);

    /**
     * Adds a new introductions definition.
     *
     * @param introduction the introduction definition
     */
    public abstract void addIntroduction(IntroductionDefinition introduction);

    /**
     * Checks if there exists an advice with the name specified.
     *
     * @param name the name of the advice
     * @return boolean
     */
    public abstract boolean hasAdvice(String name);

    /**
     * Checks if there exists an introduction with the name specified.
     *
     * @param name the name of the introduction
     * @return boolean
     */
    public abstract boolean hasIntroduction(String name);

    /**
     * Checks if a class has an <tt>Aspect</tt>.
     *
     * @param className the name or the class
     * @return boolean
     */
    public abstract boolean inTransformationScope(String className);

    /**
     * Checks if a class has an <tt>Introduction</tt>.
     *
     * @param className the name or the class
     * @return boolean
     */
    public abstract boolean hasIntroductions(String className);

    /**
     * Checks if a method has a <tt>MethodPointcut</tt>.
     * Only checks for a class match to allow early filtering.
     *
     * @param classMetaData the class meta-data
     * @return boolean
     */
    public abstract boolean hasMethodPointcut(ClassMetaData classMetaData);

    /**
     * Checks if a method has a <tt>MethodPointcut</tt>.
     *
     * @param classMetaData the class meta-data
     * @param methodMetaData the method meta-data
     * @return boolean
     */
    public abstract boolean hasMethodPointcut(ClassMetaData classMetaData,
                                              MethodMetaData methodMetaData);

    /**
     * Checks if a class has a <tt>GetFieldPointcut</tt>.
     * Only checks for a class match to allow early filtering.
     *
     * @param classMetaData the class meta-data
     * @return boolean
     */
    public abstract boolean hasGetFieldPointcut(ClassMetaData classMetaData);

    /**
     * Checks if a class and field has a <tt>GetFieldPointcut</tt>.
     *
     * @param classMetaData the class meta-data
     * @param fieldMetaData the name or the field
     * @return boolean
     */
    public abstract boolean hasGetFieldPointcut(ClassMetaData classMetaData,
                                                FieldMetaData fieldMetaData);

    /**
     * Checks if a class has a <tt>SetFieldPointcut</tt>.
     * Only checks for a class match to allow early filtering.
     *
     * @param classMetaData the class meta-data
     * @return boolean
     */
    public abstract boolean hasSetFieldPointcut(ClassMetaData classMetaData);

    /**
     * Checks if a class and field has a <tt>SetFieldPointcut</tt>.
     *
     * @param classMetaData the class meta-data
     * @param fieldMetaData the name or the field
     * @return boolean
     */
    public abstract boolean hasSetFieldPointcut(ClassMetaData classMetaData,
                                                FieldMetaData fieldMetaData);

    /**
     * Checks if a class and method has a <tt>ThrowsPointcut</tt>.
     * Only checks for a class match to allow early filtering.
     *
     * @param classMetaData the class meta-data
     * @return boolean
     */
    public abstract boolean hasThrowsPointcut(ClassMetaData classMetaData);

    /**
     * Checks if a class and method has a <tt>ThrowsPointcut</tt>.
     *
     * @param classMetaData the class meta-data
     * @param methodMetaData the name or the method
     * @return boolean
     */
    public abstract boolean hasThrowsPointcut(ClassMetaData classMetaData,
                                              MethodMetaData methodMetaData);

    /**
     * Checks if a class should care about advising caller side method invocations.
     *
     * @param classMetaData the class meta-data
     * @return boolean
     */
    public abstract boolean hasCallerSidePointcut(ClassMetaData classMetaData);

    /**
     * Checks if a method is a defined as a caller side method.
     *
     * @param classMetaData the class meta-data
     * @param methodMetaData the name or the method
     * @return boolean
     */
    public abstract boolean isCallerSideMethod(ClassMetaData classMetaData,
                                               MethodMetaData methodMetaData);

    /**
     * Returns the names of the introductions for a certain class.
     *
     * @param className the name of the class
     * @return the names
     */
    public abstract List getIntroductionNames(String className);

    /**
     * Builds up a meta-data repository for the mixins.
     *
     * @param repository the repository
     * @param loader the class loader to use
     */
    public abstract void buildMixinMetaDataRepository(Set repository, ClassLoader loader);
}
