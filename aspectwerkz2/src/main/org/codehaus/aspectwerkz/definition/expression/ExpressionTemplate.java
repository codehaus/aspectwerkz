/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition.expression;

/**
 * Template for the expressions. TO BE REMOVED
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class ExpressionTemplate {

    /**
     * The name of the pointcut for this expression (if the expression is a top level expression, e.g. is bound to a
     * named pointcut).
     */
    private final String m_name;

    /**
     * The namespace for the expression.
     */
    private final String m_namespace;

    /**
     * The string representation of the expression.
     */
    private final String m_expression;

    /**
     * The expression type.
     */
    private final PointcutType m_type;

    /**
     * The package namespace that the expression is living in.
     */
    private String m_package = "";

    /**
     * Creates a new expression template.
     *
     * @param namespace        the namespace for the expression
     * @param expression       the expression string
     * @param packageNamespace the package namespace
     * @param name             the name of the expression
     * @param type             the type of the expression
     */
    ExpressionTemplate(
            final String namespace,
            final String expression,
            final String packageNamespace,
            final String name,
            final PointcutType type) {
        if (namespace == null) {
            throw new IllegalArgumentException("namespace can not be null");
        }
        if (expression == null) {
            throw new IllegalArgumentException("expression can not be null");
        }
        if (name == null) {
            throw new IllegalArgumentException("name can not be null");
        }
        if (type == null) {
            throw new IllegalArgumentException("type can not be null");
        }

        m_namespace = namespace;
        m_expression = expression;
        m_name = name;
        m_type = type;
        if (packageNamespace != null) {
            m_package = packageNamespace;
        }
        else {
            m_package = "";
        }
    }

    /**
     * Returns the namespace for the expression.
     *
     * @return the namespace for the expression
     */
    public String getNamespace() {
        return m_namespace;
    }

    /**
     * Returns the expression pattern as a string.
     *
     * @return the expression pattern as a string
     */
    public String getExpression() {
        return m_expression;
    }

    /**
     * Returns the name for the expression.
     *
     * @return the name
     */
    public String getName() {
        return m_name;
    }

    /**
     * Returns the expression type.
     *
     * @return the expression type
     */
    public PointcutType getType() {
        return m_type;
    }

    /**
     * Returns the package for the expression.
     *
     * @return the package
     */
    public String getPackage() {
        return m_package;
    }
}
