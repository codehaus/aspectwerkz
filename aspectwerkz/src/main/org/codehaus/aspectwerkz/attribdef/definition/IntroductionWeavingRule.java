/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.attribdef.definition;

import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.io.ObjectInputStream;

import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.ExpressionFactory;

import org.codehaus.aspectwerkz.regexp.PointcutPatternTuple;
import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.util.Strings;
import org.codehaus.aspectwerkz.pointcut.ClassPointcut;
import org.codehaus.aspectwerkz.definition.PointcutDefinition;

/**
 * Handles the introduction weaving rule definition.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class IntroductionWeavingRule implements WeavingRule {

    /**
     * The pointcut expression.
     */
    private String m_expression;

    /**
     * The Jexl expression.
     */
    private transient Expression m_jexlExpr;

    /**
     * The pointcut definition references.
     */
    private List m_pointcutRefs = null;

    /**
     * The class pointcut definitions referenced in the m_expression.
     * Mapped to the name of the pointcut definition.
     */
    private Map m_classPointcutPatterns = new HashMap();

    /**
     * Returns the expression.
     *
     * @return the expression
     */
    public String getExpression() {
        return m_expression;
    }

    /**
     * Sets the expression. Substitutes all "AND" to "&&" and all "OR" to "||".
     *
     * @param expression the expression
     */
    public void setExpression(final String expression) {
        String tmp = Strings.replaceSubString(expression, " AND ", " && ");
        tmp = Strings.replaceSubString(tmp, " and ", " && ");
        tmp = Strings.replaceSubString(tmp, " OR ", " || ");
        tmp = Strings.replaceSubString(tmp, " or ", " || ");
        m_expression = tmp;
        createJexlExpressions();
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

    /**
     * Adds a new class pointcut pattern.
     *
     * @param pointcut the pointcut definition
     */
    public void addClassPointcutPattern(final PointcutDefinition pointcut) {
        m_classPointcutPatterns.put(pointcut.getName(),
                new PointcutPatternTuple(
                        pointcut.getRegexpClassPattern(),
                        pointcut.getRegexpPattern(),
                        pointcut.isHierarchical()));
    }

    /**
     * Checks if the pointcut matches a certain join point.
     * Only checks for a class match to allow early filtering.
     *
     * @param classMetaData the class meta-data
     * @return boolean
     */
    public boolean matchClassPointcut(final ClassMetaData classMetaData) {
        try {
            for (Iterator it = m_classPointcutPatterns.entrySet().iterator(); it.hasNext();) {
                Map.Entry entry = (Map.Entry)it.next();
                String name = (String)entry.getKey();
                PointcutPatternTuple pointcutPattern = (PointcutPatternTuple)entry.getValue();

                // try to find a match somewhere in the class hierarchy (interface or super class)
                if (pointcutPattern.isHierarchical()) {
                    if (ClassPointcut.matchClassPointcutSuperClasses(
                            name, classMetaData, pointcutPattern)) {
                        return true;
                    }
                }
                // match the single class only
                else if (pointcutPattern.getClassPattern().matches(classMetaData.getName())) {
                    return true;
                }
            }
            return false;
        }
        catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Creates the Jexl expressions.
     */
    private void createJexlExpressions() {
        try {
            m_jexlExpr = ExpressionFactory.createExpression(m_expression);
        }
        catch (Exception e) {
            throw new RuntimeException("could not create jexl expression from: " + m_expression);
        }
    }

    /**
     * Provides custom deserialization.
     *
     * @param stream the object input stream containing the serialized object
     * @throws Exception in case of failure
     */
    private void readObject(final ObjectInputStream stream) throws Exception {
        ObjectInputStream.GetField fields = stream.readFields();
        m_expression = (String)fields.get("m_expression", null);
        m_pointcutRefs = (List)fields.get("m_pointcutRefs", null);
        m_classPointcutPatterns = (Map)fields.get("m_classPointcutPatterns", null);
        createJexlExpressions();
    }
}

