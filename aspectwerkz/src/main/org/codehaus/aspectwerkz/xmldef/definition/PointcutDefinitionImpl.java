/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.xmldef.definition;

import org.codehaus.aspectwerkz.regexp.Pattern;
import org.codehaus.aspectwerkz.regexp.ClassPattern;
import org.codehaus.aspectwerkz.regexp.PointcutPatternTuple;
import org.codehaus.aspectwerkz.exception.DefinitionException;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.definition.PointcutDefinition;

/**
 * Holds the pointcut definition.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class PointcutDefinitionImpl implements PointcutDefinition {

    /**
     * The name of the pointcut.
     */
    private String m_name = null;

    /**
     * The type for the pointcut.
     */
    private String m_type = null;

    /**
     * The class pattern the pointcut should matches.
     */
    private String m_classPattern = null;

    /**
     * The pattern the pointcut should matches.
     */
    private String m_pattern = null;

    /**
     * A pre-compiled regexp pattern for this pointcut.
     */
    private Pattern m_regexpPattern = null;

    /**
     * A pre-compiled regexp class pattern for this pointcut.
     */
    private ClassPattern m_regexpClassPattern = null;

    /**
     * Hierarchical flag.
     */
    private boolean m_hierarchical = false;

    /**
     * Marks the pointcut as reentrant.
     */
    private String m_isNonReentrant = "false";

    /**
     * Returns the name of the pointcut.
     *
     * @return the name of the pointcut
     */
    public String getName() {
        return m_name;
    }

    /**
     * Sets the name of the pointcut.
     */
    public void setName(final String name) {
        m_name = name;
    }

    /**
     * Returns the type of the pointcut.
     *
     * @return the type
     */
    public String getType() {
        return m_type;
    }

    /**
     * Sets the type of the pointcut.
     *
     * @param type the type
     */
    public void setType(final String type) {
        m_type = type.trim();
    }

    /**
     * Returns the class pattern for the pointcut.
     *
     * @return the class pattern
     */
    public String getClassPattern() {
        return m_classPattern;
    }

    /**
     * Adds a class pattern for the pointcut.
     *
     * @param pattern the class pattern
     */
    public void setClassPattern(final String classPattern) {
        m_classPattern = classPattern.trim();
        m_regexpClassPattern = Pattern.compileClassPattern(m_classPattern);
    }

    /**
     * Returns the pattern for the pointcut.
     *
     * @return the pattern
     */
    public String getPattern() {
        return m_pattern;
    }

    /**
     * Adds a pattern for the pointcut.
     *
     * @param pattern the pattern
     */
    public void setPattern(final String pattern) {
        m_pattern = pattern.trim();
    }

    /**
     * Returns a pre-compiled Pattern for the class pattern.
     *
     * @return a pre-compiled Pattern for the class pattern
     */
    public ClassPattern getRegexpClassPattern() {
        return m_regexpClassPattern;
    }

    /**
     * Returns a pre-compiled Pattern for the pattern.
     *
     * @return a pre-compiled Pattern for the pattern
     */
    public Pattern getRegexpPattern() {
        if (m_regexpPattern == null) {
            try {
                if (m_type.equalsIgnoreCase(METHOD)) {
                    m_regexpPattern = Pattern.compileMethodPattern(m_pattern);
                }
                else if (m_type.equalsIgnoreCase(GET_FIELD)) {
                    m_regexpPattern = Pattern.compileFieldPattern(m_pattern);
                }
                else if (m_type.equalsIgnoreCase(SET_FIELD)) {
                    m_regexpPattern = Pattern.compileFieldPattern(m_pattern);
                }
                else if (m_type.equalsIgnoreCase(THROWS)) {
                    m_regexpPattern = Pattern.compileThrowsPattern(m_pattern);
                }
                else if (m_type.equalsIgnoreCase(CALLER_SIDE)) {
                    m_regexpPattern = Pattern.compileCallerSidePattern(m_pattern);
                }
                else if (m_type.equalsIgnoreCase(CFLOW)) {
                    m_regexpPattern = Pattern.compileCallerSidePattern(m_pattern);
                }
                else {
                    throw new IllegalStateException("pointcut has an undefined type: " + m_type);
                }
            }
            catch (DefinitionException e) {
                throw new DefinitionException("pattern in pointcut definition <" + m_name + "> is not valid: " + m_pattern);
            }
            catch (Exception e) {
                throw new WrappedRuntimeException(e);
            }
        }
        return m_regexpPattern;
    }

    /**
     * Returns the pointcut pattern tuple for the pre-compiled class A method pattern.
     *
     * @return the pointcut pattern tuple
     */
    public PointcutPatternTuple getPointcutPatternTuple() {
        return new PointcutPatternTuple(
                getRegexpClassPattern(),
                getRegexpPattern(),
                m_hierarchical);
    }

    /**
     * Marks the pointcut as hierarchical.
     */
    public void markAsHierarchical() {
        m_hierarchical = true;
    }

    /**
     * Checks if the pointcut is hierarchical.
     *
     * @return the flag
     */
    public boolean isHierarchical() {
        return m_hierarchical;
    }

    /**
     * Sets the non-reentrancy flag.
     *
     * @param isNonReentrant
     */
    public void setNonReentrant(final String isNonReentrant) {
        m_isNonReentrant = isNonReentrant;
    }

    /**
     * Returns the string representation of the non-reentrancy flag.
     *
     * @return the non-reentrancy flag
     */
    public String getNonReentrant() {
        return m_isNonReentrant;
    }

    /**
     * Checks if the pointcut is non-reentrant or not.
     *
     * @return the non-reentrancy flag
     */
    public boolean isNonReentrant() {
        return "true".equalsIgnoreCase(m_isNonReentrant);
    }

    /**
     * Check if the pointcut is a method pointcut.
     *
     * @return boolean
     */
    public boolean isMethodPointcut() {
        return m_type == METHOD;
    }

    /**
     * Check if the pointcut is a field pointcut.
     *
     * @return boolean
     */
    public boolean isFieldPointcut() {
        return m_type == SET_FIELD || m_type == GET_FIELD;
    }

    /**
     * Check if the pointcut is a class pointcut.
     *
     * @return boolean
     */
    public boolean isClassPointcut() {
        return m_type == CLASS;
    }

    /**
     * Checks if the pointcut is a caller side pointcut.
     *
     * @return boolean
     */
    public boolean isCallerSidePointcut() {
        return m_type == CALLER_SIDE;
    }

    /**
     * Checks if the pointcut is a cflow pointcut.
     *
     * @return boolean
     */
    public boolean isCFlowPointcut() {
        return m_type == CFLOW;
    }

    /**
     * Checks if the pointcut is a throws pointcut.
     *
     * @return boolean
     */
    public boolean isThrowsPointcut() {
        return m_type == THROWS;
    }
}
