/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.expression;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * The expression namespace as well as a repository for the namespaces.
 * <p/>
 * A namespace is usually defined by the name of the class defining the expression.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public final class ExpressionNamespace {
    /**
     * Namespace container.
     */
    private static final Map s_namespaces = new WeakHashMap();

    /**
     * Map with all the expressions in the namespace, [name:expression] pairs.
     */
    private final Map m_expressions = new HashMap();

    /**
     * The namespace.
     */
    private final String m_namespace;

    /**
     * Creates a new expression namespace.
     *
     * @param namespace
     */
    private ExpressionNamespace(final String namespace) {
        m_namespace = namespace;
    }

    /**
     * Returns the expression namespace for a specific namespace.
     *
     * @param namespace the expression namespace
     * @return the expression namespace abstraction
     */
    public static synchronized ExpressionNamespace getNamespace(final String namespace) {
        if (!s_namespaces.containsKey(namespace)) {
            s_namespaces.put(namespace, new ExpressionNamespace(namespace));
        }
        return (ExpressionNamespace)s_namespaces.get(namespace);
    }

    /**
     * Adds an expression info to the namespace.
     *
     * @param name           the name mapped to the expression
     * @param expressionInfo the expression info to add
     */
    public void addExpressionInfo(final String name, final ExpressionInfo expressionInfo) {
        m_expressions.put(name, expressionInfo);
    }

    /**
     * Returns the expression info with a specific name.
     *
     * @param name the name of the expression
     * @return the expression info
     */
    public ExpressionInfo getExpressionInfo(final String name) {
        int index = name.lastIndexOf('.');
        if (index != -1) {
            return getNamespace(name.substring(0, index)).getExpressionInfo(name.substring(index + 1, name.length()));
        } else {
            return ((ExpressionInfo)m_expressions.get(name));
        }
    }

    /**
     * Returns the expression with a specific name.
     *
     * @param name the name of the expression
     * @return the expression
     */
    public ExpressionVisitor getExpression(final String name) {
        return getExpressionInfo(name).getExpression();
    }

    /**
     * Returns the cflow expression with a specific name.
     *
     * @param name the name of the expression
     * @return the expression
     */
    public CflowExpressionVisitor getCflowExpression(final String name) {
        return getExpressionInfo(name).getCflowExpression();
    }

    /**
     * Returns the advised class expression with a specific name.
     *
     * @param name the name of the expression
     * @return the expression
     */
    public AdvisedClassFilterExpressionVisitor getAdvisedClassExpression(final String name) {
        return getExpressionInfo(name).getAdvisedClassFilterExpression();
    }

    /**
     * Returns the advised cflow class expression witha a specific name.
     *
     * @param name the name of the expression
     * @return the expression
     */
    public AdvisedCflowClassFilterExpressionVisitor getAdvisedCflowClassExpression(final String name) {
        return getExpressionInfo(name).getAdvisedCflowClassFilterExpression();
    }

    /**
     * Returns the name of the namespace.
     *
     * @return the name of the namespace
     */
    public String getName() {
        return m_namespace;
    }
}
