/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition2;

import java.lang.reflect.Method;
import java.io.Serializable;

import org.codehaus.aspectwerkz.regexp.Pattern;
import org.codehaus.aspectwerkz.regexp.ClassPattern;
import org.codehaus.aspectwerkz.regexp.PointcutPatternTuple;
import org.codehaus.aspectwerkz.exception.DefinitionException;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;

/**
 * Holds the meta-data for the pointcuts.
 *
 * @TODO fix '+' patterns
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public class PointcutDefinition2 implements Serializable {

    public static final String TYPE_METHOD = "method";
    public static final String TYPE_FIELD = "field";
    public static final String TYPE_THROWS = "throws";
    public static final String TYPE_CALLER_SIDE = "callerside";
    public static final String TYPE_CFLOW = "cflow";
    public static final String TYPE_CLASS = "class";

    /**
     * The type for the pointcut.
     */
    private String m_type = null;

    /**
     * The expression.
     */
    private final String m_expression;

    /**
     * The method representing the pointcut.
     */
    private final Method m_method;

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
     * Creates a new pointcut meta-data instance.
     *
     * @param expression the expression for the pointcut
     * @param method the method representing the pointcut
     */
    public PointcutDefinition2(final String expression, final Method method) {
        if (expression == null) throw new IllegalArgumentException("expression can not be null");
        if (method == null) throw new IllegalArgumentException("method can not be null");

        m_expression = expression;
        m_method = method;
        if (isMethodPointcut()) {
            m_type = TYPE_METHOD;
            AspectWerkzDefinition2.createMethodPattern(m_expression, this, "");
        }
        else if (isFieldPointcut()) {
            m_type = TYPE_FIELD;
            AspectWerkzDefinition2.createFieldPattern(m_expression, this, "");
        }
        else if (isClassPointcut()) {
            m_type = TYPE_CLASS;
            AspectWerkzDefinition2.createClassPattern(m_expression, this, "");
        }
//        else if (isThrowsPointcut()) {
//            m_type = TYPE_THROWS;
//            AspectWerkzDefinition2.createThrowsPattern(m_expression, this, "");
//        }
//        else if (isCallerSidePointcut()) {
//            m_type = TYPE_CALLER_SIDE;
//            AspectWerkzDefinition2.createCallerSidePattern(m_expression, this, "");
//        }
//        else if (isCflowPointcut()) {
//            m_type = TYPE_CFLOW;
//            AspectWerkzDefinition2.create (m_expression, this, "");
//        }
        else {
            throw new DefinitionException("pointcut expression is not valid [" + m_expression + "] for pointcut with name [" + getName() + "]");
        }
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
     * Returns the expression for the pointcut.
     *
     * @return the expression for the pointcut
     */
    public String getExpression() {
        return m_expression;
    }

    /**
     * Returns the method representing the pointcut.
     *
     * @return the method
     */
    public Method getMethod() {
        return m_method;
    }

    /**
     * Returns the name of the pointcut.
     *
     * @return the name
     */
    public String getName() {
        return m_method.getName();
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
                if (m_type.equalsIgnoreCase(TYPE_METHOD)) {
                    m_regexpPattern = Pattern.compileMethodPattern(m_pattern);
                }
                else if (m_type.equalsIgnoreCase(TYPE_FIELD)) {
                    m_regexpPattern = Pattern.compileFieldPattern(m_pattern);
                }
//                else if (m_type.equalsIgnoreCase(TYPE_THROWS)) {
//                    m_regexpPattern = Pattern.compileThrowsPattern(m_pattern);
//                }
//                else if (m_type.equalsIgnoreCase(TYPE_CALLER_SIDE)) {
//                    m_regexpPattern = Pattern.compileCallerSidePattern(m_pattern);
//                }
//                else if (m_type.equalsIgnoreCase(TYPE_CFLOW)) {
//                    m_regexpPattern = Pattern.compileCallerSidePattern(m_pattern);
//                }
                else if (m_type.equalsIgnoreCase(TYPE_CLASS)) {
                    m_regexpPattern = Pattern.compileClassPattern(m_pattern);
                }
                else {
                    throw new IllegalStateException("pointcut has an undefined type: " + m_type);
                }
            }
            catch (DefinitionException e) {
                throw new DefinitionException("pattern in pointcut definition <" + m_method.getName() + "> is not valid: " + m_pattern);
            }
            catch (Exception e) {
                throw new WrappedRuntimeException(e);
            }
        }
        return m_regexpPattern;
    }

    /**
     * Returns the pointcut pattern tuple for the pre-compiled class and method pattern.
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
        if (m_expression.indexOf('(') != -1 &&
                m_expression.indexOf(' ') != -1 &&
                m_expression.indexOf("&&") == -1 &&
                m_expression.indexOf("||") == -1)
            return true;
        else
            return false;
    }

    /**
     * Check if the pointcut is a field pointcut.
     *
     * @return boolean
     */
    public boolean isFieldPointcut() {
        if (m_expression.indexOf(' ') != -1 &&
                m_expression.indexOf('(') == -1 &&
                m_expression.indexOf("&&") == -1 &&
                m_expression.indexOf("||") == -1)
            return true;
        else
            return false;
    }

    /**
     * Check if the pointcut is a class pointcut.
     *
     * @return boolean
     */
    public boolean isClassPointcut() {
        if (m_expression.indexOf('(') == -1 &&
                m_expression.indexOf(' ') == -1 &&
                m_expression.indexOf("&&") == -1 &&
                m_expression.indexOf("||") == -1)
            return true;
        else
            return false;
    }
}
