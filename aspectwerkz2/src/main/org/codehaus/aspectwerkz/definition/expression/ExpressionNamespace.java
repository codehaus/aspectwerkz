/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition.expression;

import java.util.Map;
import java.util.HashMap;
import java.util.WeakHashMap;

import org.codehaus.aspectwerkz.exception.ExpressionException;
import org.codehaus.aspectwerkz.definition.AspectDefinition;

/**
 * Expression Namespace. A namespace is usually defined by the Aspect name.
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @TODO: ALEX enhance for multiple system and freeing
 */
public class ExpressionNamespace {

    /**
     * Default name.
     */
    private static final AspectDefinition DEFAULT_NAMESPACE = new AspectDefinition("DEFAULT_NAMESPACE", "java.lang.Object");

    /**
     * Namespace container.
     */
    private static Map s_namespaces = new WeakHashMap();

    /**
     * Map with all the expression templates in the namespace
     * <p/>
     * name:expression pairs.
     */
    private Map m_expressions = new HashMap();

    /**
     * The namespace.
     */
    private String m_namespace;

    /**
     * Returns the expression namespace for a specific namespace.
     *
     * @param namespace
     * @return the expression namespace
     */
    public static synchronized ExpressionNamespace getExpressionNamespace(final AspectDefinition namespace) {
        if (!s_namespaces.containsKey(namespace)) {
            s_namespaces.put(namespace, new ExpressionNamespace(namespace.toString()));
            //TODO AVAOPC remove toString here
            // AV : I think we need for informational purpose the system object + the aspect def
            // and a convention to say the namespace "string repr" is not unique in the whole VM
            // since uuid is not unique (it is in a CL hierarchy)
            // Then we can set m_namespace to "uuid/aspectName" just for info purpose
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

    /**
     * Creates an new strongly typed leaf expression.
     *
     * @param expression
     * @param type
     * @return the expression
     */
    public Expression createExpression(final String expression, final PointcutType type) {
        return createExpression(expression, "", "", type);
    }

    /**
     * Creates an new expression.
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
     * Creates an expression of a given type (for explicit Leaf Expression creation)
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
     * Create new expression based on the type Note that we check for an ExpressionExpression here as well
     *
     * @param expression       the expression string
     * @param packageNamespace the package namespace that the expression is living in
     * @param name             the name of the pointcut
     * @param type             the pointcut type
     * @return the expression (needs to be casted)
     */
    public Expression createExpression(
            final String expression,
            final String packageNamespace,
            final String name,
            final PointcutType type) {
        if (type.equals(PointcutType.CALL)) {
            return createCallExpression(expression, packageNamespace, name);
        } else if (type.equals(PointcutType.CFLOW)) {
            return createCflowExpression(expression, packageNamespace, name);
        } else if (type.equals(PointcutType.CLASS)) {
            return createClassExpression(expression, packageNamespace, name);
        } else if (type.equals(PointcutType.EXECUTION)) {
            return createExecutionExpression(expression, packageNamespace, name);
        } else if (type.equals(PointcutType.GET)) {
            return createGetExpression(expression, packageNamespace, name);
        } else if (type.equals(PointcutType.HANDLER)) {
            return createHandlerExpression(expression, packageNamespace, name);
        } else if (type.equals(PointcutType.SET)) {
            return createSetExpression(expression, packageNamespace, name);
        } else if (type.equals(PointcutType.ATTRIBUTE)) {
            return createAttributeExpression(expression, name);
        } else {
            throw new RuntimeException("no such expression type: " + type);
        }
    }


    /**
     * Create new expression based on the type Note that we check for an ExpressionExpression here as well
     *
     * @param expression       the expression string
     * @param packageNamespace the package namespace that the expression is living in
     * @param name             the name of the pointcut
     * @return the expression (needs to be casted)
     */
    public Expression createExpression(
            final String expression,
            final String packageNamespace,
            final String name) {
        return new ExpressionExpression(this, expression, name);
    }

    /**
     * Create new execution expression.
     *
     * @param expression       the expression string = attribute name
     * @param name             the name of the pointcut
     * @return the expression
     */
    public AttributeExpression createAttributeExpression(String expression, String name) {
        return new AttributeExpression(this, expression, name);
    }

    /**
     * Create new execution expression.
     *
     * @param expression       the expression string
     * @param packageNamespace the package namespace that the expression is living in
     * @param name             the name of the pointcut
     * @return the expression
     */
    public ExecutionExpression createExecutionExpression(
            final String expression,
            final String packageNamespace,
            final String name) {
        return new ExecutionExpression(this, expression, packageNamespace, name);
    }

    /**
     * Create new call expression.
     *
     * @param expression       the expression string
     * @param packageNamespace the package namespace that the expression is living in
     * @param name             the name of the pointcut
     * @return the expression
     */
    public CallExpression createCallExpression(
            final String expression,
            final String packageNamespace,
            final String name) {
        return new CallExpression(this, expression, packageNamespace, name);
    }

    /**
     * Create new set expression.
     *
     * @param expression       the expression string
     * @param packageNamespace the package namespace that the expression is living in
     * @param name             the name of the pointcut
     * @return the expression
     */
    public SetExpression createSetExpression(
            final String expression,
            final String packageNamespace,
            final String name) {
        return new SetExpression(this, expression, packageNamespace, name);
    }

    /**
     * Create new get expression.
     *
     * @param expression       the expression string
     * @param packageNamespace the package namespace that the expression is living in
     * @param name             the name of the pointcut
     * @return the expression
     */
    public GetExpression createGetExpression(
            final String expression,
            final String packageNamespace,
            final String name) {
        return new GetExpression(this, expression, packageNamespace, name);
    }

    /**
     * Create new cflow expression.
     *
     * @param expression       the expression string
     * @param packageNamespace the package namespace that the expression is living in
     * @param name             the name of the pointcut
     * @return the expression
     */
    public CflowExpression createCflowExpression(
            final String expression,
            final String packageNamespace,
            final String name) {
        return new CflowExpression(this, expression, packageNamespace, name);
    }

    /**
     * Create new handler expression.
     *
     * @param expression       the expression string
     * @param packageNamespace the package namespace that the expression is living in
     * @param name             the name of the pointcut
     * @return the expression
     */
    public HandlerExpression createHandlerExpression(
            final String expression,
            final String packageNamespace,
            final String name) {
        return new HandlerExpression(this, expression, packageNamespace, name);
    }

    /**
     * Create new class expression.
     *
     * @param expression       the expression string
     * @param packageNamespace the package namespace that the expression is living in
     * @param name             the name of the pointcut
     * @return the expression
     */
    public ClassExpression createClassExpression(
            final String expression,
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
        //System.out.println("reg = " + expression.getName());
        //synchronized (m_expressions) {
        //@TODO: ALEX  getName never null ??
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
     * @return the expression
     */
    public Expression registerExpression(
            final String expression,
            final String packageNamespace,
            final String name) {
        return registerExpression(createExpression(expression, packageNamespace, name));
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
    public Expression registerExpression(
            final String expression,
            final String packageNamespace,
            final String name,
            final PointcutType type) {
        return registerExpression(createExpression(expression, packageNamespace, name, type));
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

    public String getNamespaceKey() {
        //TODO AV bad container stuff for AOPC (static)
        // what should be the namespace scope?
        return m_namespace;
    }

    /**
     * Checks if the expression looks like a leaf expression.
     * TODO: unstable if space sep not use
     * Caution: foo.set(..) must not be match as a "set(..)" pc
     * Solution: move pattern to the grammar
     *
     * TODO: unstable: if Class pattern and no package "Foo" => is is a ref or a class name ?
     *
     * @param expression
     * @return true of false
     */
    private static boolean looksLikeLeaf(String expression) {
        boolean notLikeLeaf = (
                expression.indexOf(" AND ") > 0 ||
                expression.indexOf(" and ") > 0 ||
                expression.indexOf(" && ") > 0 ||
                expression.indexOf(" OR ") > 0 ||
                expression.indexOf(" or ") > 0 ||
                expression.indexOf(" || ") > 0);
        boolean likeLeaf = expression.indexOf(".") > 0 || expression.indexOf("->") > 0 || expression.indexOf("#") > 0;
//        return (
//                ! (expression.indexOf("cflow(") > 0 ||
//                   expression.indexOf("execution(") > 0 ||
//                   expression.indexOf("call(") > 0 ||
//                   expression.indexOf("get(") > 0 ||
//                   expression.indexOf("set(") > 0 ||
//                   expression.indexOf("handler(") > 0 ||
//                   expression.indexOf("class(") > 0)
//                && (expression.indexOf(".") > 0 || expression.indexOf("->") > 0 || expression.indexOf("#") > 0));
        return !notLikeLeaf && likeLeaf;
//        boolean result = !notLikeLeaf && likeLeaf;
//        if ( result ) {
//            System.err.println("leaf = " + expression);
//        }
//        return result;
    }
}
