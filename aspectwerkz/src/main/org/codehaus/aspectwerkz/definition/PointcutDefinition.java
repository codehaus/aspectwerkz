/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition;

import java.io.Serializable;

import org.codehaus.aspectwerkz.regexp.ClassPattern;
import org.codehaus.aspectwerkz.regexp.Pattern;
import org.codehaus.aspectwerkz.regexp.PointcutPatternTuple;

/**
 * Interface that the different pointcut implementations must implement.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public interface PointcutDefinition extends Serializable {

    String METHOD = "method";
    String GET_FIELD = "getfield";
    String SET_FIELD = "setfield";
    String THROWS = "throws";
    String CALLER_SIDE = "callerside";
    String CFLOW = "cflow";
    String CLASS = "class";

    /**
     * Returns the name of the pointcut.
     *
     * @return the name
     */
    String getName();

    /**
     * Sets the name of the pointcut.
     */
    void setName(String name);

    /**
     * Returns the type of the pointcut.
     *
     * @return the type
     */
    String getType();

    /**
     * Sets the type of the pointcut.
     *
     * @param type the type
     */
    void setType(String type);

    /**
     * Returns the class pattern for the pointcut.
     *
     * @return the class pattern
     */
    String getClassPattern();

    /**
     * Adds a class pattern for the pointcut.
     *
     * @param pattern the class pattern
     */
    void setClassPattern(String classPattern);

    /**
     * Returns the pattern for the pointcut.
     *
     * @return the pattern
     */
    String getPattern();

    /**
     * Adds a pattern for the pointcut.
     *
     * @param pattern the pattern
     */
    void setPattern(String pattern);

    /**
     * Returns a pre-compiled Pattern for the class pattern.
     *
     * @return a pre-compiled Pattern for the class pattern
     */
    ClassPattern getRegexpClassPattern();

    /**
     * Returns a pre-compiled Pattern for the pattern.
     *
     * @return a pre-compiled Pattern for the pattern
     */
    Pattern getRegexpPattern();

    /**
     * Returns the pointcut pattern tuple for the pre-compiled class and method pattern.
     *
     * @return the pointcut pattern tuple
     */
    PointcutPatternTuple getPointcutPatternTuple();

    /**
     * Marks the pointcut as hierarchical.
     */
    void markAsHierarchical();

    /**
     * Checks if the pointcut is hierarchical.
     *
     * @return the flag
     */
    boolean isHierarchical();

    /**
     * Sets the non-reentrancy flag.
     *
     * @param isNonReentrant
     */
    void setNonReentrant(String isNonReentrant);

    /**
     * Returns the string representation of the non-reentrancy flag.
     *
     * @return the non-reentrancy flag
     */
    String getNonReentrant();

    /**
     * Checks if the pointcut is non-reentrant or not.
     *
     * @return the non-reentrancy flag
     */
    boolean isNonReentrant();

    /**
     * Check if the pointcut is a method pointcut.
     *
     * @return boolean
     */
    boolean isMethodPointcut();

    /**
     * Check if the pointcut is a field pointcut.
     *
     * @return boolean
     */
    boolean isFieldPointcut();

    /**
     * Check if the pointcut is a class pointcut.
     *
     * @return boolean
     */
    boolean isClassPointcut();

    /**
     * Checks if the pointcut is a caller side pointcut.
     *
     * @return boolean
     */
    boolean isCallerSidePointcut();

    /**
     * Checks if the pointcut is a cflow pointcut.
     *
     * @return boolean
     */
    boolean isCFlowPointcut();

    /**
     * Checks if the pointcut is a throws pointcut.
     *
     * @return boolean
     */
    boolean isThrowsPointcut();
}
