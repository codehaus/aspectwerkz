/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition;

import java.util.Set;
import java.util.Collection;
import java.io.Serializable;

import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.metadata.MethodMetaData;
import org.codehaus.aspectwerkz.metadata.FieldMetaData;

/**
 * Interface for the aspectwerkz definition implementations.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public interface AspectWerkzDefinition extends Serializable {

    public static final String PER_JVM = "perJVM";
    public static final String PER_CLASS = "perClass";
    public static final String PER_INSTANCE = "perInstance";
    public static final String PER_THREAD = "perThread";
    public static final String THROWS_DELIMITER = "#";
    public static final String CALLER_SIDE_DELIMITER = "->";

    /**
     * XML definition flag.
     */
    public static final int DEF_TYPE_XML_DEF = 0;

    /**
     * Attrib definition flag.
     */
    public static final int DEF_TYPE_ATTRIB_DEF = 1;

    /**
     * The name of the system aspect.
     */
    public static final String SYSTEM_ASPECT = "org/codehaus/aspectwerkz/system";

    /**
     * Checks if the definition is of type attribute definition.
     *
     * @return boolean
     */
    boolean isAttribDef();

    /**
     * Checks if the definition is of type XML definition.
     *
     * @return boolean
     */
    boolean isXmlDef();

    /**
     * Sets the UUID for the definition.
     *
     * @param uuid the UUID
     */
    void setUuid(String uuid);

    /**
     * Returns the UUID for the definition.
     *
     * @return the UUID
     */
    String getUuid();

    /**
     * Returns the transformation scopes.
     *
     * @return the transformation scopes
     */
    Set getIncludePackages();

    /**
     * Returns a collection with the aspect definitions registered.
     *
     * @return the aspect definitions
     */
    Collection getAspectDefinitions();

    /**
     * Returns a collection with the introduction definitions registered.
     *
     * @return the introduction definitions
     */
    Collection getIntroductionDefinitions();

    /**
     * Returns a collection with the advice definitions registered.
     *
     * @return the advice definitions
     */
    Collection getAdviceDefinitions();

    /**
     * Returns the name of the implementation for an introduction.
     *
     * @param introductionName the name of the introduction
     * @return the name of the interface
     */
    String getIntroductionImplName(String introductionName);

    /**
     * Returns the class name for the join point controller, if there is a match.
     * @param classMetaData the class meta-data
     * @param methodMetaData the method meta-data
     * @return the controller class name
     */
    String getJoinPointController(ClassMetaData classMetaData, MethodMetaData methodMetaData);

    /**
     * Returns a set with the aspects to use.
     *
     * @return the aspects to use
     */
    Set getAspectsToUse();

    /**
     * Adds a new aspect to use.
     *
     * @param className the class name of the aspect
     */
    void addAspectToUse(String className);

    /**
     * Adds a new include package.
     *
     * @param includePackage the package to include
     */
    void addIncludePackage(String includePackage);

    /**
     * Adds a new exclude package.
     *
     * @param excludePackage the package to exclude
     */
    void addExcludePackage(String excludePackage);

    /**
     * Adds a new prepare package.
     *
     * @param preparePackage the package to exclude
     */
    void addPreparePackage(String preparePackage);

    /**
     * Checks if there exists an advice with the name specified.
     *
     * @param name the name of the advice
     * @return boolean
     */
    boolean hasAdvice(String name);

    /**
     * Checks if there exists an introduction with the name specified.
     *
     * @param name the name of the introduction
     * @return boolean
     */
    boolean hasIntroduction(String name);

    /**
     * Checks if a class is in include declaration
     *
     * @param className the name or the class
     * @return boolean
     */
    boolean inIncludePackage(String className);

    /**
     * Checks if a class is in exclude declaration
     *
     * @param className the name or the class
     * @return boolean
     */
    boolean inExcludePackage(String className);

    /**
     * Checks if a class is in prepare declaration
     *
     * @param className the name or the class
     * @return boolean
     */
    boolean inPreparePackage(String className);

    /**
     * Checks if a class has an <tt>Mixin</tt>.
     *
     * @param classMetaData the class meta-data
     * @return boolean
     */
    boolean hasIntroductions(ClassMetaData classMetaData);

    /**
     * Checks if a method has a <tt>MethodPointcut</tt>.
     * Only checks for a class match to allow early filtering.
     *
     * @param classMetaData the class meta-data
     * @return boolean
     */
    boolean hasExecutionPointcut(ClassMetaData classMetaData);

    /**
     * Checks if a method has a <tt>MethodPointcut</tt>.
     *
     * @param classMetaData the class meta-data
     * @param methodMetaData the method meta-data
     * @return boolean
     */
    boolean hasExecutionPointcut(ClassMetaData classMetaData, MethodMetaData methodMetaData);

    /**
     * Checks if a class has a <tt>GetFieldPointcut</tt>.
     * Only checks for a class match to allow early filtering.
     *
     * @param classMetaData the class meta-data
     * @return boolean
     */
    boolean hasGetPointcut(ClassMetaData classMetaData);

    /**
     * Checks if a class and field has a <tt>GetFieldPointcut</tt>.
     *
     * @param classMetaData the class meta-data
     * @param fieldMetaData the name or the field
     * @return boolean
     */
    boolean hasGetPointcut(ClassMetaData classMetaData, FieldMetaData fieldMetaData);

    /**
     * Checks if a class has a <tt>SetFieldPointcut</tt>.
     * Only checks for a class match to allow early filtering.
     *
     * @param classMetaData the class meta-data
     * @return boolean
     */
    boolean hasSetPointcut(ClassMetaData classMetaData);

    /**
     * Checks if a class and field has a <tt>SetFieldPointcut</tt>.
     *
     * @param classMetaData the class meta-data
     * @param fieldMetaData the name or the field
     * @return boolean
     */
    boolean hasSetPointcut(ClassMetaData classMetaData, FieldMetaData fieldMetaData);

    /**
     * Checks if a class and method has a <tt>ThrowsPointcut</tt>.
     * Only checks for a class match to allow early filtering.
     *
     * @param classMetaData the class meta-data
     * @return boolean
     */
    boolean hasThrowsPointcut(ClassMetaData classMetaData);

    /**
     * Checks if a class and method has a <tt>ThrowsPointcut</tt>.
     *
     * @param classMetaData the class meta-data
     * @param methodMetaData the name or the method
     * @return boolean
     */
    boolean hasThrowsPointcut(ClassMetaData classMetaData, MethodMetaData methodMetaData);

    /**
     * Checks if a class should care about advising caller side method invocations.
     *
     * @param classMetaData the class meta-data
     * @return boolean
     */
    boolean hasCallPointcut(ClassMetaData classMetaData);

    /**
     * Checks if a method is a defined as a caller side method.
     *
     * @param classMetaData the class meta-data
     * @param methodMetaData the name or the method
     * @return boolean
     */
    boolean isPickedOutByCallPointcut(ClassMetaData classMetaData, MethodMetaData methodMetaData);

    /**
     * Builds up a meta-data repository for the mixins.
     *
     * @param repository the repository
     * @param loader the class loader to use
     */
    void buildMixinMetaDataRepository(Set repository, ClassLoader loader);

    /**
     * Loads the aspects.
     *
     * @param loader the class loader to use to load the aspects
     */
    void loadAspects(ClassLoader loader);
}

