/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
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
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.StaticJoinPoint;

import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

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

    private final static String FQN_JOIN_POINT_CLASS = JoinPoint.class.getName();
    private final static String FQN_STATIC_JOIN_POINT_CLASS = StaticJoinPoint.class.getName();
    private final static String JOINPOINT = "JoinPoint";

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

    /**
     * Ordered map of the pointcut arguments type, indexed by their name.
     */
    private final Map m_argsTypeByName = new SequencedHashMap();

    /**
     * List<String> of possible arguments names/references that appear in the expression.
     * This list is lasily populated once using the ExpressionValidateVisitor.
     * Note that "types" are part of the populated list:
     * <br/>pointcutRef(x) ==> "x"
     * <br/>execution(...) && args(x, int) ==> "x", "int"
     * <br/>this(..), target(..)
     */
    private List m_possibleArguments = null;

    /**
     * Creates a new expression info instance.
     *
     * @param expression the expression
     * @param namespace  the namespace
     */
    public ExpressionInfo(final String expression, final String namespace) {
        try {
            ASTRoot root = s_parser.parse(expression);
            m_expression = new ExpressionVisitor(this, expression, namespace, root);
            m_argsIndexMapper = new ArgsIndexVisitor(this, expression, namespace, root);
            m_advisedClassFilterExpression = new AdvisedClassFilterExpressionVisitor(this, expression, namespace, root);
            m_cflowExpression = new CflowExpressionVisitor(this, expression, namespace, root);
            m_cflowExpressionRuntime = new CflowExpressionVisitorRuntime(this, expression, namespace, root);
            m_advisedCflowClassFilterExpression = new AdvisedCflowClassFilterExpressionVisitor(
                    this,
                    expression,
                    namespace,
                    root
            );
        } catch (Throwable e) {
            throw new DefinitionException("expression is not well-formed [" + expression + "]: " + e.getMessage(), e);
        }
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
     * Returns the namespace
     *
     * @return
     */
    public String getNamespace() {
        return m_expression.m_namespace;
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

    /**
     * Returns the runtime cflow expression.
     *
     * @return the cflow expression
     */
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
            try {
                m_hasCflowPointcut = new CflowPointcutFinderVisitor(
                        toString(),
                        m_expression.m_namespace,
                        s_parser.parse(toString())
                ).hasCflowPointcut();
                m_hasCflowPointcutKnown = true;
            } catch (Throwable e) {
                // should not happen since the m_expression had been accepted
                throw new DefinitionException(
                        "expression is not well-formed [" + toString() + "]: " + e.getMessage(), e
                );
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

    /**
     * Add an argument extracted from the call signature of the expression info.
     * Check is made to ensure that the argument is part of an args(..) or pointcutReference(..) subexpression.
     * TODO: support this() target()
     *
     * @param name
     * @param className
     */
    public void addArgument(final String name, final String className) {
        //AW-241
        // Note: we do not check the signature and we ignore JoinPoint parameters types
        String expression = toString();
        // fast check if we have a parenthesis
        if (expression.indexOf('(') > 0) {
            // fast check if the given argument (that appears in the advice signature) is part of the pointcut expression
            if (!(FQN_JOIN_POINT_CLASS.equals(className) ||
                  FQN_STATIC_JOIN_POINT_CLASS.equals(className) ||
                  JOINPOINT.equals(className))) {
                if (toString().indexOf(name) < 0) {
                    throw new DefinitionException(
                            "Pointcut is missing a parameter that has been encountered in the Advice: '"
                            + toString() + "' - '" + name + "' of type '" + className +
                            "' missing in '" +
                            getExpression().m_namespace +
                            "'"
                    );
                } else {
                    // lazily populate the possible argument list
                    if (m_possibleArguments == null) {
                        m_possibleArguments = new ArrayList();
                        new ExpressionValidateVisitor(toString(), getNamespace(), getExpression().m_root)
                                .populate(m_possibleArguments);
                    }
                    if (!m_possibleArguments.contains(name)) {
                        throw new DefinitionException(
                                "Pointcut is missing a parameter that has been encountered in the Advice: '"
                                + toString() + "' - '" + name + "' of type '" +
                                className +
                                "' missing in '" +
                                getExpression().m_namespace +
                                "'"
                        );
                    }
                }
            }
        }
        m_argsTypeByName.put(name, className);
    }

    /**
     * Returns the argumen type.
     *
     * @param parameterName
     * @return
     */
    public String getArgumentType(final String parameterName) {
        return (String) m_argsTypeByName.get(parameterName);
    }

    /**
     * Returns the argument index.
     *
     * @param parameterName
     * @return
     */
    public int getArgumentIndex(final String parameterName) {
        if (m_argsTypeByName.containsKey(parameterName)) {
            return ((SequencedHashMap) m_argsTypeByName).indexOf(parameterName);
        } else {
            return -1;
        }
    }

    /**
     * Returns the argument at the given index.
     *
     * @param index
     * @return paramName
     */
    public String getArgumentNameAtIndex(final int index) {
        if (index >= m_argsTypeByName.size()) {
            throw new ArrayIndexOutOfBoundsException("cannot get argument at index " + index + " in " + m_expression.toString());
        }
        return (String) m_argsTypeByName.keySet().toArray()[index];
    }

    /**
     * Returns all argument names.
     *
     * @return
     */
    public Set getArgumentNames() {
        return m_argsTypeByName.keySet();
    }

}

