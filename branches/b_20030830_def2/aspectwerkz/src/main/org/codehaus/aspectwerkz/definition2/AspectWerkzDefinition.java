/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition2;

import java.util.Set;

import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.metadata.MethodMetaData;
import org.codehaus.aspectwerkz.metadata.FieldMetaData;

/**
 * Interface that all the AspectWerkz definition implementation must implement.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public interface AspectWerkzDefinition {

    String PER_JVM = "perJVM";
    String PER_CLASS = "perClass";
    String PER_INSTANCE = "perInstance";
    String PER_THREAD = "perThread";
    String THROWS_DELIMITER = "#";
    String CALLER_SIDE_DELIMITER = "#";

    /**
     * The path to the definition file.
     */
    String DEFINITION_FILE = System.getProperty("aspectwerkz.definition.file", null);

    /**
     * The default name for the definition file.
     */
    String DEFAULT_DEFINITION_FILE_NAME = "aspectwerkz.xml";

    /**
     * The name of the system aspect.
     */
    String SYSTEM_ASPECT = "org/codehaus/aspectwerkz/system";

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
     * Checks if there exists an advice with the name specified.
     *
     * @param name the name of the advice
     * @return boolean
     */
    boolean hasAdvice(String name);

    /**
     * Checks if a class has an <tt>Aspect</tt>.
     *
     * @param className the name or the class
     * @return boolean
     */
    boolean inTransformationScope(String className);

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
    boolean hasMethodPointcut(ClassMetaData classMetaData, MethodMetaData methodMetaData);

    /**
     * Checks if a class has a <tt>GetFieldPointcut</tt>.
     * Only checks for a class match to allow early filtering.
     *
     * @TODO: how to know if it is a Set of a Get field pointcut
     *
     * @param classMetaData the class meta-data
     * @return boolean
     */
    boolean hasGetFieldPointcut(ClassMetaData classMetaData);

    /**
     * Checks if a class and field has a <tt>GetFieldPointcut</tt>.
     *
     * @TODO: how to know if it is a Set of a Get field pointcut
     *
     * @param classMetaData the class meta-data
     * @param fieldMetaData the name or the field
     * @return boolean
     */
    boolean hasGetFieldPointcut(ClassMetaData classMetaData, FieldMetaData fieldMetaData);

    /**
     * Checks if a class has a <tt>SetFieldPointcut</tt>.
     * Only checks for a class match to allow early filtering.
     *
     * @TODO: how to know if it is a Set of a Get field pointcut
     *
     * @param classMetaData the class meta-data
     * @return boolean
     */
    boolean hasSetFieldPointcut(ClassMetaData classMetaData);

    /**
     * Checks if a class and field has a <tt>SetFieldPointcut</tt>.
     *
     * @TODO: how to know if it is a Set of a Get field pointcut
     *
     * @param classMetaData the class meta-data
     * @param fieldMetaData the name or the field
     * @return boolean
     */
    boolean hasSetFieldPointcut(ClassMetaData classMetaData, FieldMetaData fieldMetaData);

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
    boolean hasCallerSidePointcut(ClassMetaData classMetaData);

    /**
     * Checks if a method is a defined as a caller side method.
     *
     * @param classMetaData the class meta-data
     * @param methodMetaData the name or the method
     * @return boolean
     */
    boolean isCallerSideMethod(ClassMetaData classMetaData, MethodMetaData methodMetaData);
}
