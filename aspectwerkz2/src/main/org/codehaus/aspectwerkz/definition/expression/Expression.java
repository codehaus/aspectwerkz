/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition.expression;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.metadata.MemberMetaData;

/**
 * Base abstract class for the expressions. <p/>An expression is wether an ExpressionExpression (algebra) wether a
 * LeafExpression <p/>Evaluates nested pointcut patterns with unlimited depth. <p/>Uses the composite pattern.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 * @TODO: implement readObject() for the subclasses
 * @TODO: add serialVersionUID field to the subclasses
 */
public abstract class Expression implements Serializable {

    /**
     * The name of the pointcut for this expression (if the expression is a top level expression, e.g. is bound to a
     * named pointcut).
     */
    protected String m_name;

    /**
     * The namespace for the expression.
     */
    protected ExpressionNamespace m_namespace;

    /**
     * The string representation of the expression.
     */
    protected String m_expression;

    /**
     * The expression types (polymorphic for orthogonality).
     */
    protected Set m_types = new HashSet();

    /**
     * The package namespace that the expression is living in.
     */
    protected String m_package = "";

    /**
     * Creates a new expression.
     *
     * @param namespace  the namespace for the expression
     * @param expression the expression as a string
     * @param name       the name of the expression
     * @param type       the expression type
     */
    Expression(
            final ExpressionNamespace namespace,
            final String expression,
            final String name,
            final PointcutType type) {
        this(namespace, expression, "", name, type);
    }

    /**
     * Creates a new expression.
     *
     * @param namespace        the namespace for the expression
     * @param expression       the expression as a string
     * @param packageNamespace the package namespace that the expression is living in
     * @param name             the name of the expression
     * @param type             the expression type
     */
    Expression(
            final ExpressionNamespace namespace,
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

        m_namespace = namespace;
        m_expression = expression;
        m_name = name;
        if (type!=null) {
            // avoid having null type registered
            // see ExpressionExpressoin late type registration
            m_types.add(type);
        }
        if (packageNamespace == null) {
            m_package = packageNamespace;
        }
        else {
            m_package = "";
        }
    }

    /**
     * Returns the name for the expression (if available, else null).
     *
     * @return the name
     */
    public String getName() {
        return m_name;
    }

    /**
     * Returns the expression as a string.
     *
     * @return the expression as a string
     */
    public String getExpression() {
        return m_expression;
    }

    /**
     * Returns the namespace for the expression.
     *
     * @return the namespace for the expression
     */
    public ExpressionNamespace getNamespace() {
        return m_namespace;
    }

    /**
     * Check if the expression is of given type.
     *
     * @return true if expression is of given type
     */
    public boolean isOfType(PointcutType type) {
        return m_types.contains(type);
    }

    /**
     * Returns the expression types.
     *
     * @return the expression types
     */
    public Set getTypes() {
        return m_types;
    }

    /**
     * Checks if the expression matches a certain join point. <p/>Only checks for a class match to allow early
     * filtering. <p/>Only does a qualified guess, does not evaluate the whole expression since doing it only on class
     * level would give the false results.
     *
     * @param classMetaData the class meta-data
     * @param assumedType the expression type we match with (for orthogonal support)
     * @return boolean
     */
    public abstract boolean match(final ClassMetaData classMetaData, final PointcutType assumedType);

    /**
     * Checks if the expression matches a certain join point. <p/>Only checks for a class match to allow early
     * filtering. <p/>Only does a qualified guess, does not evaluate the whole expression since doing it only on class
     * level would give the false results.
     *
     * If the Expression is not polymorphic, the type assumed is the single type of the expression.
     * Else match will silently fail
     *
     * @param classMetaData the class meta-data
     * @return boolean
     */
    public abstract boolean match(final ClassMetaData classMetaData);

    /**
     * Checks if the expression matches a certain join point as regards the IN and NOT IN parts if any. Each IN / NOT IN
     * part is evaluated independantly from the boolean algebra (TF time)
     * <p/>
     * <p/>Only checks for a class match to allow early filtering. <p/>Only does a qualified guess, does not evaluate
     * the whole expression since doing it only on class level would give the false results.
     *
     * @param classMetaData the class meta-data
     * @return boolean
     */
    public abstract boolean matchInOrNotIn(final ClassMetaData classMetaData);

    /**
     * Checks if the expression matches a certain join point.
     *
     * @param classMetaData  the class meta-data
     * @param memberMetaData the meta-data for the member
     * @param assumedType the expression type we match with (for orthogonal support)
     * @return boolean
     */
    public abstract boolean match(final ClassMetaData classMetaData, final MemberMetaData memberMetaData, PointcutType assumedType);

    /**
     * Checks if the expression matches a certain join point.
     *
     * If the Expression is not polymorphic, the type assumed is the single type of the expression.
     * Else match will silently fail
     *
     * @param classMetaData  the class meta-data
     * @param memberMetaData the meta-data for the member
     * @return boolean
     */
    public abstract boolean match(final ClassMetaData classMetaData, final MemberMetaData memberMetaData);

    /**
     * Checks if the expression matches a certain join point as regards IN / NOT IN parts Each IN / NOT IN part is
     * evaluated independantly from the boolean algebra (TF time)
     *
     * @param classMetaData  the class meta-data
     * @param memberMetaData the meta-data for the member
     * @return boolean
     */
    public abstract boolean matchInOrNotIn(final ClassMetaData classMetaData, final MemberMetaData memberMetaData);

    /**
     * Checks if the expression matches a certain join point. <p/>Special case in the API which tries to match exception
     * types as well.
     *
     * @param classMetaData  the class meta-data
     * @param memberMetaData the meta-data for the member
     * @param exceptionType  the exception type (null => match all)
     * @param assumedType the expression type we match with (for orthogonal support)
     * @return boolean
     */
    public abstract boolean match(
            final ClassMetaData classMetaData,
            final MemberMetaData memberMetaData,
            final String exceptionType,
            final PointcutType assumedType);

    /**
     * Return a Map(name->Expression) of expression involved in the IN and NOT IN sub-expression of this Expression (can
     * be empty)
     *
     * @return Map(name->Expression)
     */
    public abstract Map getCflowExpressions();

    /**
     * Overridden toString.
     *
     * @return the string representation of the Expression instance
     */
    public String toString() {
        return '[' + super.toString() + ": " +
               m_name + ',' +
               m_namespace + ',' +
               m_package + ',' +
               m_expression + ']';
    }
}
