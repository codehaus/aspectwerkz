/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition.expression;

import org.codehaus.aspectwerkz.exception.ExpressionException;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.HashMap;

/**
 * Expression Namespace.
 * A namespace is usually defined by the Aspect name.
 * TODO: enhance for multiple system and freeing
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class ExpressionNamespace {

    /**
     * Default name.
     */
    private static final String DEFAULT_NAMESPACE = "DEFAULT_NAMESPACE";

    /**
     * Namespace container.
     */
    private static Map s_namespaces = new WeakHashMap();

    /**
     * Namespace.
     * TODO: never used?
     */
    private String m_namespace;

    /**
     * Map with all the expression templates in the namespace
     * <p/>
     * name:expression pairs.
     */
    private Map m_expressions = new HashMap();

    /**
     * Returns the expression namespace for a specific namespace.
     *
     * @param namespace
     * @return the expression namespace
     */
    public static synchronized ExpressionNamespace getExpressionNamespace(Object namespace) {
        if (!s_namespaces.containsKey(namespace)) {
            s_namespaces.put(namespace, new ExpressionNamespace(namespace.toString()));
        }
        return (ExpressionNamespace)s_namespaces.get(namespace);
    }

    /**
     * Returns the default expression namespace.
     *
     * @return the default expression namespace
     */
    public static ExpressionNamespace getExpressionNamespace() {
        return getExpressionNamespace(DEFAULT_NAMESPACE);
    }

    public Expression createExpression(final String expression, final PointcutType type) {
        return createExpression(expression, "", "", type);
    }

    /**
     * Creates and expression.
     *
     * @param expression
     * @return the expression
     */
    public Expression createExpression(final String expression) {
        return new ExpressionExpression(this, expression);
    }

    /**
     * Creates and expression.
     *
     * @param expression
     * @param name
     * @return the expression
     */
    public Expression createExpression(final String expression, final String name) {
        return new ExpressionExpression(this, expression, name);
    }

    /**
     * Creates and expression.
     *
     * @param expression
     * @param name
     * @param type
     * @return the expression
     */
    public Expression createExpression(final String expression, final String name, final PointcutType type) {
        return createExpression(expression, "", name, type);
    }

    /**
     * Create new expression based on the type
     * Note that we check for an ExpressionExpression here as well
     *
     * @param expression the expression string
     * @param packageNamespace the package namespace that the expression is living in
     * @param name the name of the pointcut
     * @param type the pointcut type
     * @return the expression (needs to be casted)
     */
    public Expression createExpression(final String expression,
                                       final String packageNamespace,
                                       final String name,
                                       final PointcutType type) {
        Expression expr = null;
        if (!looksLikeLeaf(expression)) {
            expr = new ExpressionExpression(this, expression, name);
        }
        else if (type.equals(PointcutType.EXECUTION)) {
            expr = createExecutionExpression(
                    expression, packageNamespace, name
            );
        }
        else if (type.equals(PointcutType.CALL)) {
            expr = createCallExpression(
                    expression, packageNamespace, name
            );
        }
        else if (type.equals(PointcutType.SET)) {
            expr = createSetExpression(
                    expression, packageNamespace, name
            );
        }
        else if (type.equals(PointcutType.GET)) {
            expr = createGetExpression(
                    expression, packageNamespace, name
            );
        }
        else if (type.equals(PointcutType.THROWS)) {
            expr = createThrowsExpression(
                    expression, packageNamespace, name
            );
        }
        else if (type.equals(PointcutType.CFLOW)) {
            expr = createCflowExpression(
                    expression, packageNamespace, name
            );
        }
        else if (type.equals(PointcutType.CLASS)) {
            expr = createClassExpression(
                    expression, packageNamespace, name
            );
        }
        else {
            throw new ExpressionException("poincut type is not supported: " + type);
        }
        return expr;
    }

    /**
     * Create new execution expression.
     *
     * @param expression the expression string
     * @param packageNamespace the package namespace that the expression is living in
     * @param name the name of the pointcut
     * @return the expression
     */
    public ExecutionExpression createExecutionExpression(final String expression,
                                                         final String packageNamespace,
                                                         final String name) {
        return new ExecutionExpression(this, expression, packageNamespace, name);
    }

    /**
     * Create new call expression.
     *
     * @param expression the expression string
     * @param packageNamespace the package namespace that the expression is living in
     * @param name the name of the pointcut
     * @return the expression
     */
    public CallExpression createCallExpression(final String expression,
                                               final String packageNamespace,
                                               final String name) {
        return new CallExpression(this, expression, packageNamespace, name);
    }

    /**
     * Create new set expression.
     *
     * @param expression the expression string
     * @param packageNamespace the package namespace that the expression is living in
     * @param name the name of the pointcut
     * @return the expression
     */
    public SetExpression createSetExpression(final String expression,
                                             final String packageNamespace,
                                             final String name) {
        return new SetExpression(this, expression, packageNamespace, name);
    }

    /**
     * Create new get expression.
     *
     * @param expression the expression string
     * @param packageNamespace the package namespace that the expression is living in
     * @param name the name of the pointcut
     * @return the expression
     */
    public GetExpression createGetExpression(final String expression,
                                             final String packageNamespace,
                                             final String name) {
        return new GetExpression(this, expression, packageNamespace, name);
    }

    /**
     * Create new throws expression.
     *
     * @param expression the expression string
     * @param packageNamespace the package namespace that the expression is living in
     * @param name the name of the pointcut
     * @return the expression
     */
    public ThrowsExpression createThrowsExpression(final String expression,
                                                   final String packageNamespace,
                                                   final String name) {
        return new ThrowsExpression(this, expression, packageNamespace, name);
    }

    /**
     * Create new cflow expression.
     *
     * @param expression the expression string
     * @param packageNamespace the package namespace that the expression is living in
     * @param name the name of the pointcut
     * @return the expression
     */
    public CflowExpression createCflowExpression(final String expression,
                                                 final String packageNamespace,
                                                 final String name) {
        return new CflowExpression(this, expression, packageNamespace, name);
    }

    /**
     * Create new class expression.
     *
     * @param expression the expression string
     * @param packageNamespace the package namespace that the expression is living in
     * @param name the name of the pointcut
     * @return the expression
     */
    public ClassExpression createClassExpression(final String expression,
                                                 final String packageNamespace,
                                                 final String name) {
        return new ClassExpression(this, expression, packageNamespace, name);
    }

    /**
     * Registers an expression template.
     *
     * @param expression the expression to add
     * @return the expression
     */
    public Expression registerExpression(final Expression expression) {
        //synchronized (m_expressions) {
        //todo getName never null ??
        m_expressions.put(expression.getName(), expression);
        expression.m_namespace = this;//namespace swapping
        //}
        return expression;
    }

    /**
     * Registers an expression.
     *
     * @param expression
     * @param packageNamespace
     * @param name
     * @param type
     * @return the expression
     */
    public Expression registerExpression(final String expression,
                                         final String packageNamespace,
                                         final String name,
                                         final PointcutType type) {
        Expression expr = createExpression(expression, packageNamespace, name, type);
        return registerExpression(expr);
    }

    /**
     * Finds and returns an expression template by its name.
     *
     * @param expressionName the name of the expression
     * @return the expression
     */
    public Expression getExpression(final String expressionName) {
        return (Expression)m_expressions.get(expressionName);
    }

    /**
     * Creates a new expression namespace.
     *
     * @param namespace
     */
    private ExpressionNamespace(String namespace) {
        m_namespace = namespace;
    }

    /**
     * Checks if the expression looks like a leaf expression.
     *
     * @param expression
     * @return true of false
     */
    private static boolean looksLikeLeaf(String expression) {
        return (expression.indexOf(".") > 0
                || expression.indexOf("->") > 0
                || expression.indexOf("#") > 0);
    }
}
