/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition;

import java.util.Set;
import java.util.Collection;
import java.util.List;
import java.io.Serializable;

import gnu.trove.TObjectIntHashMap;

import org.codehaus.aspectwerkz.xmldef.definition.AdviceStackDefinition;
import org.codehaus.aspectwerkz.xmldef.definition.AspectDefinition;
import org.codehaus.aspectwerkz.xmldef.definition.AdviceDefinition;
import org.codehaus.aspectwerkz.xmldef.definition.IntroductionDefinition;
import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.metadata.MethodMetaData;
import org.codehaus.aspectwerkz.metadata.FieldMetaData;

/**
 * Interface for the aspectwerkz definition implementations.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public interface AspectWerkzDefinition extends Serializable {

    public static final String PER_JVM = "perJVM";
    public static final String PER_CLASS = "perClass";
    public static final String PER_INSTANCE = "perInstance";
    public static final String PER_THREAD = "perThread";
    public static final String THROWS_DELIMITER = "#";
    public static final String CALLER_SIDE_DELIMITER = "#";

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
    Set getTransformationScopes();

    /**
     * Returns a collection with the abstract aspect definitions registered.
     *
     * @return the abstract aspect definitions
     */
    Collection getAbstractAspectDefinitions();

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
     * Finds an advice stack definition by its name.
     *
     * @param adviceStackName the advice stack name
     * @return the definition
     */
    AdviceStackDefinition getAdviceStackDefinition(String adviceStackName);

    /**
     * Returns a specific abstract aspect definition.
     *
     * @param name the name of the abstract aspect definition
     * @return the abstract aspect definition
     */
    AspectDefinition getAbstractAspectDefinition(String name);

    /**
     * Returns a specific aspect definition.
     *
     * @param name the name of the aspect definition
     * @return the aspect definition
     */
    AspectDefinition getAspectDefinition(String name);

    /**
     * Returns the names of the target classes.
     *
     * @return the names of the target classes
     */
    String[] getAspectTargetClassNames();

    /**
     * Returns a specific advice definition.
     *
     * @param name the name of the advice definition
     * @return the advice definition
     */
    AdviceDefinition getAdviceDefinition(String name);

    /**
     * Finds the name of an advice by its attribute.
     *
     * @param attribute the attribute
     * @return the name of the advice
     */
    String getAdviceNameByAttribute(String attribute);

    /**
     * Finds the name of an introduction by its attribute.
     *
     * @param attribute the attribute
     * @return the name of the introduction
     */
    String getIntroductionNameByAttribute(String attribute);

    /**
     * Returns the name of the interface for an introduction.
     *
     * @param introductionName the name of the introduction
     * @return the name of the interface
     */
    String getIntroductionInterfaceName(String introductionName);

    /**
     * Returns the name of the implementation for an introduction.
     *
     * @param introductionName the name of the introduction
     * @return the name of the interface
     */
    String getIntroductionImplName(String introductionName);

    /**
     * Returns a specific introduction definition.
     *
     * @param introductionName the name of the introduction
     * @return the introduction definition
     */
    IntroductionDefinition getIntroductionDefinition(String introductionName);

    /**
     * Returns the index for a specific introduction.
     *
     * @param introductionName the name of the introduction
     * @return the index
     */
    int getIntroductionIndex(String introductionName);

    /**
     * Returns the indexes for the introductions.
     *
     * @return the indexes
     */
    TObjectIntHashMap getIntroductionIndexes();

    /**
     * Returns the class name for the join point controller, if there is a match.
     * @param classMetaData the class meta-data
     * @param methodMetaData the method meta-data
     * @return the controller class name
     */
    String getJoinPointController(ClassMetaData classMetaData,
                                         MethodMetaData methodMetaData);

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
     * Adds a new transformation scope.
     *
     * @param transformationScope the new scope
     */
    void addTransformationScope(String transformationScope);

    /**
     * Adds an abstract aspect definition.
     *
     * @param aspect a new abstract aspect definition
     */
    void addAbstractAspect(AspectDefinition aspect);

    /**
     * Adds an aspect definition.
     *
     * @param aspect a new aspect definition
     */
    void addAspect(AspectDefinition aspect);

    /**
     * Adds an advice stack definition.
     *
     * @param adviceStackDef the advice stack definition
     */
    void addAdviceStack(AdviceStackDefinition adviceStackDef);

    /**
     * Adds an advice definition.
     *
     * @param advice the advice definition
     */
    void addAdvice(AdviceDefinition advice);

    /**
     * Adds a new introductions definition.
     *
     * @param introduction the introduction definition
     */
    void addIntroduction(IntroductionDefinition introduction);

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
     * Checks if a class has an <tt>Aspect</tt>.
     *
     * @param className the name or the class
     * @return boolean
     */
    boolean inTransformationScope(String className);

    /**
     * Checks if a class has an <tt>Introduction</tt>.
     *
     * @param className the name or the class
     * @return boolean
     */
    boolean hasIntroductions(String className);

    /**
     * Checks if a method has a <tt>MethodPointcut</tt>.
     * Only checks for a class match to allow early filtering.
     *
     * @param classMetaData the class meta-data
     * @return boolean
     */
    boolean hasMethodPointcut(ClassMetaData classMetaData);

    /**
     * Checks if a method has a <tt>MethodPointcut</tt>.
     *
     * @param classMetaData the class meta-data
     * @param methodMetaData the method meta-data
     * @return boolean
     */
    boolean hasMethodPointcut(ClassMetaData classMetaData,
                                     MethodMetaData methodMetaData);

    /**
     * Checks if a class has a <tt>GetFieldPointcut</tt>.
     * Only checks for a class match to allow early filtering.
     *
     * @param classMetaData the class meta-data
     * @return boolean
     */
    boolean hasGetFieldPointcut(ClassMetaData classMetaData);

    /**
     * Checks if a class and field has a <tt>GetFieldPointcut</tt>.
     *
     * @param classMetaData the class meta-data
     * @param fieldMetaData the name or the field
     * @return boolean
     */
    boolean hasGetFieldPointcut(ClassMetaData classMetaData,
                                       FieldMetaData fieldMetaData);

    /**
     * Checks if a class has a <tt>SetFieldPointcut</tt>.
     * Only checks for a class match to allow early filtering.
     *
     * @param classMetaData the class meta-data
     * @return boolean
     */
    boolean hasSetFieldPointcut(ClassMetaData classMetaData);

    /**
     * Checks if a class and field has a <tt>SetFieldPointcut</tt>.
     *
     * @param classMetaData the class meta-data
     * @param fieldMetaData the name or the field
     * @return boolean
     */
    boolean hasSetFieldPointcut(ClassMetaData classMetaData,
                                       FieldMetaData fieldMetaData);

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
    boolean hasThrowsPointcut(ClassMetaData classMetaData,
                                     MethodMetaData methodMetaData);

    /**
     * Checks if a class should care about advising caller side method invocations.
     *
     * @param classMetaData the class meta-data
     * @return boolean
     */
    boolean hasCallerSidePointcut(ClassMetaData classMetaData);

    /**
     * Checks if a method is a defined as a caller side method.
     *
     * @param classMetaData the class meta-data
     * @param methodMetaData the name or the method
     * @return boolean
     */
    boolean isCallerSideMethod(ClassMetaData classMetaData,
                                      MethodMetaData methodMetaData);

    /**
     * Returns the names of the introductions for a certain class.
     *
     * @param className the name of the class
     * @return the names
     */
    List getIntroductionNames(String className);

    /**
     * Builds up a meta-data repository for the mixins.
     *
     * @param repository the repository
     * @param loader the class loader to use
     */
    void buildMixinMetaDataRepository(Set repository, ClassLoader loader);
}

