/*
 * AspectWerkz - a dynamic, lightweight and high-performant AOP/AOSD framework for Java.
 * Copyright (C) 2002-2003  Jonas Bonér. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.codehaus.aspectwerkz.definition;

import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.io.ObjectInputStream;
import java.io.Serializable;

import org.apache.commons.jexl.JexlContext;
import org.apache.commons.jexl.JexlHelper;
import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.ExpressionFactory;

import org.codehaus.aspectwerkz.metadata.MethodMetaData;
import org.codehaus.aspectwerkz.regexp.PointcutPatternTuple;
import org.codehaus.aspectwerkz.regexp.MethodPattern;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;

/**
 * Holds the controller definition.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: ControllerDefinition.java,v 1.1.2.1 2003-07-17 21:00:00 avasseur Exp $
 */
public class ControllerDefinition implements Serializable {

    /**
     * The pointcut expression.
     */
    private String m_expression;

    /**
     * The Jexl expression.
     */
    private transient Expression m_jexlExpr;

    /**
     * The controller class name.
     */
    private String m_className;

    /**
     * The method pointcut definitions referenced in the m_expression.
     * Mapped to the name of the pointcut definition.
     */
    private Map m_methodPointcutPatterns = new HashMap();

    /**
     * The pointcut definition references.
     */
    private List m_pointcutRefs = null;

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
        createJexlExpression();
    }

    /**
     * Returns the class name.
     *
     * @return the class name
     */
    public String getClassName() {
        return m_className;
    }

    /**
     * Sets the controller class name
     * @param className
     */
    public void setClassName(final String className) {
        m_className = className;
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
            if (resultMethExpr == null) {
                return false;
            }
            else if (resultMethExpr.equals(Boolean.FALSE)) {
                return false;
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
     * Creates the Jexl expression.
     */
    private void createJexlExpression() {
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
     * @throws java.lang.Exception in case of failure
     */
    private void readObject(final ObjectInputStream stream) throws Exception {
        ObjectInputStream.GetField fields = stream.readFields();

        m_expression = (String)fields.get("m_expression", null);
        m_className = (String)fields.get("m_className", null);
        m_pointcutRefs = (List)fields.get("m_pointcutRefs", null);
        m_methodPointcutPatterns = (Map)fields.get("m_methodPointcutPatterns", null);

        createJexlExpression();
    }
}
