/*
 * AspectWerkz - a dynamic, lightweight and high-performant AOP/AOSD framework for Java.
 * Copyright (C) 2002-2003  Jonas Bon�r. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR and PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.codehaus.aspectwerkz.definition;

import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.io.ObjectInputStream;

import org.apache.commons.jexl.JexlContext;
import org.apache.commons.jexl.JexlHelper;
import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.ExpressionFactory;

import org.codehaus.aspectwerkz.regexp.PointcutPatternTuple;
import org.codehaus.aspectwerkz.regexp.MethodPattern;
import org.codehaus.aspectwerkz.regexp.FieldPattern;
import org.codehaus.aspectwerkz.regexp.ThrowsPattern;
import org.codehaus.aspectwerkz.regexp.CallerSidePattern;
import org.codehaus.aspectwerkz.metadata.MethodMetaData;
import org.codehaus.aspectwerkz.metadata.FieldMetaData;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;

/**
 * Handles the advice weaving rule definition.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 * @version $Id: AdviceWeavingRule.java,v 1.6 2003-07-08 11:43:35 jboner Exp $
 */
public class AdviceWeavingRule implements WeavingRule {

    /**
     * The pointcut expression.
     */
    private String m_expression;

    /**
     * The Jexl expression.
     */
    private transient Expression m_jexlExpr;

    /**
     * The cflow pointcut expression.
     */
    private String m_cflowExpression;

    /**
     * The cflow Jexl expression.
     */
    private transient Expression m_jexlCFlowExpr;

    /**
     * The advices references.
     */
    private List m_adviceRefs = new ArrayList();

    /**
     * The advice stack references.
     */
    private List m_adviceStackRefs = new ArrayList();

    /**
     * The pointcut definition references.
     */
    private List m_pointcutRefs = null;

    /**
     * The method pointcut definitions referenced in the m_expression.
     * Mapped to the name of the pointcut definition.
     */
    private Map m_methodPointcutPatterns = new HashMap();

    /**
     * The set field pointcut definitions referenced in the m_expression.
     * Mapped to the name of the pointcut definition.
     */
    private Map m_setFieldPointcutPatterns = new HashMap();

    /**
     * The get field pointcut definitions referenced in the m_expression.
     * Mapped to the name of the pointcut definition.
     */
    private Map m_getFieldPointcutPatterns = new HashMap();

    /**
     * The throws pointcut definitions referenced in the m_expression.
     * Mapped to the name of the pointcut definition.
     */
    private Map m_throwsPointcutPatterns = new HashMap();

    /**
     * The caller side pointcut definitions referenced in the m_expression.
     * Mapped to the name of the pointcut definition.
     */
    private Map m_callerSidePointcutPatterns = new HashMap();

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
        String tmp = expression.
                replaceAll(" AND ", " && ").replaceAll(" and ", " && ").
                replaceAll(" OR ", " || ").replaceAll(" or ", " || ");

        m_expression = tmp;
        createJexlExpressions();
    }

    /**
     * Returns the cflow expression.
     *
     * @return the cflow expression
     */
    public String getCFlowExpression() {
        return m_cflowExpression;
    }

    /**
     * Sets the cflow expression.
     *
     * @param cflowExpression the cflow expression
     */
    public void setCFlowExpression(final String cflowExpression) {
        m_cflowExpression = cflowExpression;
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
        String expression = m_expression.
                replaceAll("&&", "").replaceAll("\\|\\|", "").replaceAll("!", "").
                replaceAll("\\(", "").replaceAll("\\)", "");

        m_pointcutRefs = new ArrayList();
        StringTokenizer tokenizer = new StringTokenizer(expression, " ");
        while (tokenizer.hasMoreTokens()) {
            String pointcutRef = tokenizer.nextToken();
            m_pointcutRefs.add(pointcutRef);
        }
        return m_pointcutRefs;
    }

    /**
     * Returns a list with all the advice references.
     *
     * @return the advice references
     */
    public List getAdviceRefs() {
        return m_adviceRefs;
    }

    /**
     * Adds a new advice reference.
     *
     * @param adviceRef the advice reference
     */
    public void addAdviceRef(final String adviceRef) {
        m_adviceRefs.add(adviceRef);
    }

    /**
     * Returns a list with all the advice stack references.
     *
     * @return the advice stack references
     */
    public List getAdviceStackRefs() {
        return m_adviceStackRefs;
    }

    /**
     * Adds a new advice stack reference.
     *
     * @param adviceStackRef the advice stack reference
     */
    public void addAdviceStackRef(final String adviceStackRef) {
        m_adviceStackRefs.add(adviceStackRef);
    }

    /**
     * Adds a new method pointcut pattern.
     *
     * @param pointcut the pointcut definition
     */
    public void addMethodPointcutPattern(final PointcutDefinition pointcut) {
        m_methodPointcutPatterns.put(pointcut.getName(),
                new PointcutPatternTuple(
                        pointcut.getRegexpClassPattern(),
                        pointcut.getRegexpPattern()));
    }

    /**
     * Adds a new set field pointcut pattern.
     *
     * @param pointcut the pointcut definition
     */
    public void addSetFieldPointcutPattern(final PointcutDefinition pointcut) {
        m_setFieldPointcutPatterns.put(pointcut.getName(),
                new PointcutPatternTuple(
                        pointcut.getRegexpClassPattern(),
                        pointcut.getRegexpPattern()));
    }

    /**
     * Adds a new get field pointcut pattern.
     *
     * @param pointcut the pointcut definition
     */
    public void addGetFieldPointcutPattern(final PointcutDefinition pointcut) {
        m_getFieldPointcutPatterns.put(pointcut.getName(),
                new PointcutPatternTuple(
                        pointcut.getRegexpClassPattern(),
                        pointcut.getRegexpPattern()));
    }

    /**
     * Adds a new throws pointcut pattern.
     *
     * @param pointcut the pointcut definition
     */
    public void addThrowsPointcutPattern(final PointcutDefinition pointcut) {
        m_throwsPointcutPatterns.put(pointcut.getName(),
                new PointcutPatternTuple(
                        pointcut.getRegexpClassPattern(),
                        pointcut.getRegexpPattern()));
    }

    /**
     * Adds a new caller side pointcut pattern.
     *
     * @param pointcut the pointcut definition
     */
    public void addCallerSidePointcutPattern(final PointcutDefinition pointcut) {
        m_callerSidePointcutPatterns.put(pointcut.getName(),
                new PointcutPatternTuple(
                        pointcut.getRegexpClassPattern(),
                        pointcut.getRegexpPattern()));
    }

    /**
     * Checks if the pointcut matches a certain join point.
     * Tries to match both method and cflow expressions.
     *
     * @param className the name of the class
     * @param methodMetaData the meta-data for the method
     * @return boolean
     */
    public boolean matchMethodPointcut(final String className,
                                       final MethodMetaData methodMetaData) {
        try {
            JexlContext jexlContext = JexlHelper.createContext();

            for (Iterator it = m_methodPointcutPatterns.entrySet().iterator(); it.hasNext();) {
                Map.Entry entry = (Map.Entry)it.next();
                String name = (String)entry.getKey();
                PointcutPatternTuple pointcutPattern = (PointcutPatternTuple)entry.getValue();

                if (pointcutPattern.getClassPattern().matches(className) &&
                        ((MethodPattern)pointcutPattern.getPattern()).matches(methodMetaData)) {
                    jexlContext.getVars().put(name, Boolean.TRUE);
                }
                else {
                    jexlContext.getVars().put(name, Boolean.FALSE);
                }
            }

            // first try the method expression
            Boolean resultMethExpr = (Boolean)m_jexlExpr.evaluate(jexlContext);

            boolean resultExpr = (resultMethExpr == null || !resultMethExpr.booleanValue());
            if (resultExpr && m_jexlCFlowExpr != null) {

                // second try the cflow expression
                Boolean resultCFlowExpr = (Boolean)m_jexlCFlowExpr.evaluate(jexlContext);

                if (resultCFlowExpr == null || !resultCFlowExpr.booleanValue()) {
                    return false; // no match at all
                }
                else {
                    return true; // cflow expression matches
                }
            }
            else if (resultExpr) {
                return false; // method expression does not match
            }
            else {
                return true; // method expression matches
            }
        }
        catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Checks if the pointcut matches a certain join point.
     *
     * @param className the name of the class
     * @param fieldMetaData the meta-data for the field
     * @return boolean
     */
    public boolean matchSetFieldPointcut(final String className,
                                         final FieldMetaData fieldMetaData) {
        try {
            JexlContext jexlContext = JexlHelper.createContext();

            for (Iterator it = m_setFieldPointcutPatterns.entrySet().iterator(); it.hasNext();) {
                Map.Entry entry = (Map.Entry)it.next();
                String name = (String)entry.getKey();
                PointcutPatternTuple pointcutPattern = (PointcutPatternTuple)entry.getValue();

                if (pointcutPattern.getClassPattern().matches(className) &&
                        ((FieldPattern)pointcutPattern.getPattern()).matches(fieldMetaData)) {
                    jexlContext.getVars().put(name, Boolean.TRUE);
                }
                else {
                    jexlContext.getVars().put(name, Boolean.FALSE);
                }
            }

            Boolean result = (Boolean)m_jexlExpr.evaluate(jexlContext);

            if (result == null) {
                return false;
            }
            if (result.booleanValue()) {
                return true;
            }
            else {
                return false;
            }
        }
        catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Checks if the pointcut matches a certain join point.
     *
     * @param className the name of the class
     * @param fieldMetaData the meta-data for the field
     * @return boolean
     */
    public boolean matchGetFieldPointcut(final String className,
                                         final FieldMetaData fieldMetaData) {
        try {
            JexlContext jexlContext = JexlHelper.createContext();

            for (Iterator it = m_getFieldPointcutPatterns.entrySet().iterator(); it.hasNext();) {
                Map.Entry entry = (Map.Entry)it.next();
                String name = (String)entry.getKey();
                PointcutPatternTuple pointcutPattern = (PointcutPatternTuple)entry.getValue();

                if (pointcutPattern.getClassPattern().matches(className) &&
                        ((FieldPattern)pointcutPattern.getPattern()).matches(fieldMetaData)) {
                    jexlContext.getVars().put(name, Boolean.TRUE);
                }
                else {
                    jexlContext.getVars().put(name, Boolean.FALSE);
                }
            }
            Boolean result = (Boolean)m_jexlExpr.evaluate(jexlContext);

            if (result == null) {
                return false;
            }
            if (result.booleanValue()) {
                return true;
            }
            else {
                return false;
            }
        }
        catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Checks if the pointcut matches a certain join point.
     *
     * @param className the name of the class
     * @param methodMetaData the meta-data for the method
     * @return boolean
     */
    public boolean matchThrowsPointcut(final String className,
                                       final MethodMetaData methodMetaData) {
        try {
            JexlContext jexlContext = JexlHelper.createContext();

            for (Iterator it = m_throwsPointcutPatterns.entrySet().iterator(); it.hasNext();) {
                Map.Entry entry = (Map.Entry)it.next();
                String name = (String)entry.getKey();
                PointcutPatternTuple pointcutPattern = (PointcutPatternTuple)entry.getValue();

                if (pointcutPattern.getClassPattern().matches(className) &&
                        ((ThrowsPattern)pointcutPattern.getPattern()).matches(methodMetaData)) {
                    jexlContext.getVars().put(name, Boolean.TRUE);
                }
                else {
                    jexlContext.getVars().put(name, Boolean.FALSE);
                }
            }
            Boolean result = (Boolean)m_jexlExpr.evaluate(jexlContext);

            if (result == null) {
                return false;
            }
            if (result.booleanValue()) {
                return true;
            }
            else {
                return false;
            }
        }
        catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Checks if the pointcut matches a certain join point.
     *
     * @param className the name of the class
     * @param methodMetaData the meta-data for the method
     * @return boolean
     */
    public boolean matchCallerSidePointcut(final String className,
                                           final MethodMetaData methodMetaData) {
        try {
            JexlContext jexlContext = JexlHelper.createContext();

            for (Iterator it = m_callerSidePointcutPatterns.entrySet().iterator(); it.hasNext();) {
                Map.Entry entry = (Map.Entry)it.next();
                String name = (String)entry.getKey();
                PointcutPatternTuple pointcutPattern = (PointcutPatternTuple)entry.getValue();

                if (((CallerSidePattern)pointcutPattern.getPattern()).
                        matches(className, methodMetaData)) {
                    jexlContext.getVars().put(name, Boolean.TRUE);
                }
                else {
                    jexlContext.getVars().put(name, Boolean.FALSE);
                }
            }
            Boolean result = (Boolean)m_jexlExpr.evaluate(jexlContext);

            if (result == null) {
                return false;
            }
            if (result.booleanValue()) {
                return true;
            }
            else {
                return false;
            }
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
        try {
            if (m_cflowExpression != null) {
                m_jexlCFlowExpr = ExpressionFactory.createExpression(m_cflowExpression);
            }
        }
        catch (Exception e) {
            throw new RuntimeException("could not create jexl expression from: " + m_jexlCFlowExpr);
        }
    }

    /**
     * Provides custom deserialization.
     *
     * @param stream the object input stream containing the serialized object
     * @throws java.lang.Exception in case of failure
     */
    private void readObject(final ObjectInputStream stream) throws Exception {
        ObjectInputStream.GetField fields = stream.readFields();

        m_expression = (String)fields.get("m_expression", null);
        m_cflowExpression = (String)fields.get("m_cflowExpression", null);
        m_adviceRefs = (List)fields.get("m_adviceRefs", null);
        m_adviceStackRefs = (List)fields.get("m_adviceStackRefs", null);
        m_pointcutRefs = (List)fields.get("m_pointcutRefs", null);
        m_methodPointcutPatterns = (Map)fields.get("m_methodPointcutPatterns", null);
        m_setFieldPointcutPatterns = (Map)fields.get("m_setFieldPointcutPatterns", null);
        m_getFieldPointcutPatterns = (Map)fields.get("m_getFieldPointcutPatterns", null);
        m_throwsPointcutPatterns = (Map)fields.get("m_throwsPointcutPatterns", null);
        m_callerSidePointcutPatterns = (Map)fields.get("m_callerSidePointcutPatterns", null);

        createJexlExpressions();
    }
}

