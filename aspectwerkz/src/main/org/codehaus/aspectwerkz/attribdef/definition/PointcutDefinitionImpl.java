/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.attribdef.definition;

import java.lang.reflect.Field;
import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Iterator;

import org.codehaus.aspectwerkz.regexp.Pattern;
import org.codehaus.aspectwerkz.regexp.ClassPattern;
import org.codehaus.aspectwerkz.regexp.PointcutPatternTuple;
import org.codehaus.aspectwerkz.exception.DefinitionException;
import org.codehaus.aspectwerkz.definition.PointcutDefinition;
import org.codehaus.aspectwerkz.definition.PatternFactory;
import org.codehaus.aspectwerkz.attribdef.Pointcut;
import org.codehaus.aspectwerkz.util.Strings;
import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.metadata.MethodMetaData;

/**
 * Holds the meta-data for the pointcuts.
 *
 * @TODO: now the def. only supports 'one level deep' expressions, e.g. NOT an expression containing other pointcuts. this must be solved. The pointcut should be able to contain an expression build up with other pointcuts. and should be able to resolve the full pattern. E.g. evaluate the AST.
 *
 * @TODO fix '+' patterns
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
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
     * The expression.
     */
    private String m_expression;

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
     * The pointcut definition references.
     */
    private List m_pointcutRefs = null;

    /**
     * The aspect definition.
     */
    private AspectDefinition m_aspectDefinition;

    /**
     * Creates a new pointcut meta-data instance.
     *
     * @TODO: the pointcut mismatch needs to be corrected. i.e. the Execution/MethodPointcut Call/CallerSidePointcut etc.
     *
     * @param name the name of the pointcut
     * @param type the pointcut type (execution, call, set, get ..)
     * @param expression the expression for the pointcut
     * @param aspectDef the aspect definition
     */
    public PointcutDefinitionImpl(final String name,
                                  final String type,
                                  final String expression,
                                  final AspectDefinition aspectDef) {
        if (expression == null) throw new IllegalArgumentException("expression can not be null");

        m_expression = expression;
        m_name = name;
        m_aspectDefinition = aspectDef;

        if (type.equals(Pointcut.EXECUTION)) {
            PatternFactory.createMethodPattern(m_expression, this, "");
            m_regexpPattern = Pattern.compileMethodPattern(m_pattern);
            m_type = METHOD;
        }
        else if (type.equals(Pointcut.SET)) {
            PatternFactory.createFieldPattern(m_expression, this, "");
            m_regexpPattern = Pattern.compileFieldPattern(m_pattern);
            m_type = SET_FIELD;
        }
        else if (type.equals(Pointcut.GET)) {
            PatternFactory.createFieldPattern(m_expression, this, "");
            m_regexpPattern = Pattern.compileFieldPattern(m_pattern);
            m_type = GET_FIELD;
        }
        else if (type.equals(Pointcut.CLASS)) {
            PatternFactory.createClassPattern(m_expression, this, "");
            m_regexpPattern = Pattern.compileClassPattern(m_pattern);
            m_type = CLASS;
        }
        else if (type.equals(Pointcut.THROWS)) {
            PatternFactory.createThrowsPattern(m_expression, this, "");
            m_regexpPattern = Pattern.compileThrowsPattern(m_pattern);
            m_type = THROWS;
        }
        else if (type.equals(Pointcut.CALL)) {
            PatternFactory.createCallerSidePattern(m_expression, this, "");
            m_regexpPattern = Pattern.compileCallerSidePattern(m_pattern);
            m_type = CALLER_SIDE;
        }
        else if (type.equals(Pointcut.CFLOW)) {
            m_expression = "*->" + m_expression; // make a 'match-all' caller side pattern out of the cflow pattern
            PatternFactory.createCallerSidePattern(m_expression, this, "");
            m_regexpPattern = Pattern.compileCallerSidePattern(m_pattern);
            m_type = CFLOW;
        }
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

    /**
     * Checks if the pointcut is nested.
     *
     * @return boolean
     */
    public boolean isNested() {
        return m_pointcutRefs != null && !m_pointcutRefs.isEmpty();
    }

    /**
      * Returns a list with the pointcut references.
      *
      * @return the pointcut references
      */
     public List getPointcutRefs() {
         if (m_pointcutRefs != null) {
             return m_pointcutRefs;
         }
         String expression = Strings.replaceSubString(m_expression, "&&", "");
         expression = Strings.replaceSubString(expression, "||", "");
         expression = Strings.replaceSubString(expression, "!", "");
         expression = Strings.replaceSubString(expression, "(", "");
         expression = Strings.replaceSubString(expression, ")", "");

         m_pointcutRefs = new ArrayList();
         StringTokenizer tokenizer = new StringTokenizer(expression, " ");
         while (tokenizer.hasMoreTokens()) {
             String pointcutRef = tokenizer.nextToken();
             m_pointcutRefs.add(pointcutRef);
         }
         return m_pointcutRefs;
     }
}
