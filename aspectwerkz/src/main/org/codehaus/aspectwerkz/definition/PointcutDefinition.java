/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition;

import java.lang.reflect.Method;

import org.codehaus.aspectwerkz.regexp.Pattern;
import org.codehaus.aspectwerkz.regexp.ClassPattern;
import org.codehaus.aspectwerkz.regexp.PointcutPatternTuple;
import org.codehaus.aspectwerkz.exception.DefinitionException;
import org.codehaus.aspectwerkz.definition.AspectWerkzDefinition;
import org.codehaus.aspectwerkz.definition.attribute.PointcutAttribute;

/**
 * Holds the meta-data for the pointcuts.
 *
 * @TODO: now the def. only supports 'single level' expressions, e.g. NOT an expression of pointcuts. this must be solved. The pointcut should be able to contain an expression build up with other pointcuts. and should be able to resolve the full pattern. E.g. evaluate the AST.
 *
 * @TODO fix '+' patterns
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class PointcutDefinition {

    public static final String TYPE_METHOD = "method";
    public static final String TYPE_GET_FIELD = "getfield";
    public static final String TYPE_SET_FIELD = "setfield";
    public static final String TYPE_THROWS = "throws";
    public static final String TYPE_CALLER_SIDE = "callerside";
    public static final String TYPE_CFLOW = "cflow";
    public static final String TYPE_CLASS = "class";

    /**
     * The name of the pointcut.
     */
    private String m_name = null;

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
     * @param type the pointcut type (execution, call, set, get ..)
     * @param expression the expression for the pointcut
     * @param method the method representing the pointcut
     */
    public PointcutDefinition(final String type, final String expression, final Method method) {
        if (expression == null) throw new IllegalArgumentException("expression can not be null");
        if (method == null) throw new IllegalArgumentException("method can not be null");

        m_expression = expression;
        m_method = method;
        m_name = method.getName();

        if (isMethodPointcut()) {
            AspectWerkzDefinition.createMethodPattern(m_expression, this, "");
            m_regexpPattern = Pattern.compileMethodPattern(m_pattern);
            m_type = TYPE_METHOD;
        }
        else if (isFieldPointcut()) {
            AspectWerkzDefinition.createFieldPattern(m_expression, this, "");
            m_regexpPattern = Pattern.compileFieldPattern(m_pattern);
            if (type.equals(PointcutAttribute.PC_SET)) {
                m_type = TYPE_SET_FIELD;
            }
            else if (type.equals(PointcutAttribute.PC_GET)) {
                m_type = TYPE_GET_FIELD;
            }
        }
        else if (isClassPointcut()) {
            AspectWerkzDefinition.createClassPattern(m_expression, this, "");
            m_regexpPattern = Pattern.compileClassPattern(m_pattern);
            m_type = TYPE_CLASS;
        }
//        else if (m_type.equalsIgnoreCase(TYPE_THROWS)) {
//            AspectWerkzDefinition.createThrowsPattern(m_expression, this, "");
//            m_regexpPattern = Pattern.compileThrowsPattern(m_pattern);
//        }
//        else if (m_type.equalsIgnoreCase(TYPE_CALLER_SIDE)) {
//            AspectWerkzDefinition.createCallerSidePattern(m_expression, this, "");
//            m_regexpPattern = Pattern.compileCallerSidePattern(m_pattern);
//        }
//        else if (m_type.equalsIgnoreCase(TYPE_CFLOW)) {
//            AspectWerkzDefinition.create(m_expression, this, "");
//            m_regexpPattern = Pattern.compile(m_pattern);
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
     * Sets the type of the pointcut.
     *
     * @param type the type
     */
    public void setType(final String type) {
        m_type = type.trim();
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
        return m_name;
    }

    /**
     * Sets the name of the pointcut.
     */
    public void setName(final String name) {
        m_name = name;
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
