/**************************************************************************************
 * Copyright (c) Jonas BonŽr, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.expression;

import org.codehaus.aspectwerkz.exception.DefinitionException;
import org.codehaus.aspectwerkz.expression.ast.ASTRoot;
import org.codehaus.aspectwerkz.expression.ast.ExpressionParser;
import org.codehaus.aspectwerkz.util.SequencedHashMap;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;

/**
 * Abstraction that holds info about the expression and the different visitors.
 * <br/>
 * We are using a lazy initialization for m_hasCflowPointcut field to allow to fully resolve each expression (that is f.e. on IBM
 * compiler, fields are in the reverse order, thus pointcut reference in aspect defined with annotations
 * may not be resolved until the whole class has been parsed.
 * 
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class ExpressionInfo {
    /**
     * The sole instance of the parser.
     */
    private static final ExpressionParser s_parser = new ExpressionParser(System.in);

    private final ExpressionVisitor m_expression;

    private final ArgsIndexVisitor m_argsIndexMapper;

    private final CflowExpressionVisitor m_cflowExpression;

    private final CflowExpressionVisitorRuntime m_cflowExpressionRuntime;

    private final AdvisedClassFilterExpressionVisitor m_advisedClassFilterExpression;

    private final AdvisedCflowClassFilterExpressionVisitor m_advisedCflowClassFilterExpression;

    private boolean m_hasCflowPointcut;
    private boolean m_hasCflowPointcutKnown = false;

    private final Map m_argsTypeByName = new SequencedHashMap();

    /**
     * Creates a new expression info instance.
     * 
     * @param expression the expression
     * @param namespace the namespace
     */
    public ExpressionInfo(final String expression, final String namespace) {
        try {
            ASTRoot root = s_parser.parse(expression);
            m_expression = new ExpressionVisitor(this, expression, namespace, root);
            m_argsIndexMapper = new ArgsIndexVisitor(this, expression, namespace, root);
            m_advisedClassFilterExpression = new AdvisedClassFilterExpressionVisitor(expression, namespace, root);
            m_cflowExpression = new CflowExpressionVisitor(this, expression, namespace, root);
            m_cflowExpressionRuntime = new CflowExpressionVisitorRuntime(this, expression, namespace, root);
            m_advisedCflowClassFilterExpression = new AdvisedCflowClassFilterExpressionVisitor(
                expression,
                namespace,
                root);
        } catch (Throwable e) {
            throw new DefinitionException("expression is not well-formed [" + expression + "]: " + e.getMessage(), e);
        }
    }

    /**
     * Returns the expression as string.
     * 
     * @return the expression as string
     */
    public String getExpressionAsString() {
        return m_expression.toString();
    }

    /**
     * Returns the regular expression.
     * 
     * @return the regular expression
     */
    public ExpressionVisitor getExpression() {
        return m_expression;
    }

    /**
     * Returns the regular expression.
     * 
     * @return the regular expression
     */
    public ArgsIndexVisitor getArgsIndexMapper() {
        return m_argsIndexMapper;
    }

    /**
     * Returns the cflow expression.
     * 
     * @return the cflow expression
     */
    public CflowExpressionVisitor getCflowExpression() {
        return m_cflowExpression;
    }

    public CflowExpressionVisitorRuntime getCflowExpressionRuntime() {
        return m_cflowExpressionRuntime;
    }

    /**
     * Returns the advised class filter expression.
     * 
     * @return the advised class filter expression
     */
    public AdvisedClassFilterExpressionVisitor getAdvisedClassFilterExpression() {
        return m_advisedClassFilterExpression;
    }

    /**
     * Returns the advised cflow class filter expression.
     * 
     * @return the advised cflow class filter expression
     */
    public AdvisedCflowClassFilterExpressionVisitor getAdvisedCflowClassFilterExpression() {
        return m_advisedCflowClassFilterExpression;
    }

    /**
     * Returns the parser.
     * 
     * @return the parser
     */
    public static ExpressionParser getParser() {
        return s_parser;
    }

    /**
     * Checks if the expression has a cflow pointcut node.
     * 
     * @return
     */
    public boolean hasCflowPointcut() {
        if (!m_hasCflowPointcutKnown) {
            String expression = null;
            try {
                expression = getExpressionAsString();
                m_hasCflowPointcut = new CflowPointcutFinderVisitor(expression, m_expression.m_namespace, s_parser.parse(expression)).hasCflowPointcut();
                m_hasCflowPointcutKnown = true;
            } catch (Throwable e) {
                // should not happen since the m_expression had been accepted
                throw new DefinitionException("expression is not well-formed [" + expression + "]: " + e.getMessage(), e);
            }
        }
        return m_hasCflowPointcut;
    }

    /**
     * Returns the expression as string.
     * 
     * @return the expression as string
     */
    public String toString() {
        return m_expression.toString();
    }

    public void addArgument(final String name, final String className) {
        m_argsTypeByName.put(name, className);
    }

    public String getArgumentType(final String parameterName) {
        return (String) m_argsTypeByName.get(parameterName);
    }

    public int getArgumentIndex(final String parameterName) {
        if (m_argsTypeByName.containsKey(parameterName)) {
            return ((SequencedHashMap) m_argsTypeByName).indexOf(parameterName);
        } else {
            return -1;
        }
    }

    public Set getArgumentNames() {
        return m_argsTypeByName.keySet();
    }

}

