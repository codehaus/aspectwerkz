/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition.expression;

import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.metadata.MemberMetaData;
import org.codehaus.aspectwerkz.definition.expression.ast.ExpressionParser;
import org.codehaus.aspectwerkz.definition.expression.ast.SimpleNode;
import org.codehaus.aspectwerkz.definition.expression.ast.ParseException;
import org.codehaus.aspectwerkz.definition.expression.ast.ExpressionParserVisitor;
import org.codehaus.aspectwerkz.definition.expression.visitor.TypeVisitor;
import org.codehaus.aspectwerkz.definition.expression.visitor.IdentifierLookupVisitor;
import org.codehaus.aspectwerkz.definition.expression.visitor.EvaluateVisitor;
import org.codehaus.aspectwerkz.definition.expression.visitor.CflowIdentifierLookupVisitor;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;

import java.io.StringReader;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Class for sub-expression
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class ExpressionExpression extends Expression {

    /**
     * Map with the references to the pointcuts referenced in the expression.
     */
    protected final Map m_expressionRefs = new HashMap();

    /**
     * Map with the references to the pointcuts referenced in the IN or NOT IN parts of expression
     */
    protected final Map m_cflowExpressionRefs = new HashMap();

    /**
     * Type guessing visitor
     */
    private static ExpressionParserVisitor TYPE_VISITOR = new TypeVisitor();

    /**
     * Identifier lookup visitor
     */
    private static ExpressionParserVisitor IDENTIFIER_VISITOR = new IdentifierLookupVisitor();

    /**
     * Identifier involved in IN and NOT IN lookup visitor
     */
    private static ExpressionParserVisitor CFLOWIDENTIFIER_VISITOR = new CflowIdentifierLookupVisitor();

    /**
     * Expression evaluation, ignores IN and NOT IN
     */
    private static ExpressionParserVisitor EVALUATE_VISITOR = new EvaluateVisitor();

    /**
     * AST root
     */
    private SimpleNode root;

    /**
     * Create an anonymous expression
     *
     * @param namespace
     * @param expression
     */
    public ExpressionExpression(ExpressionNamespace namespace, String expression) {
        this(namespace, expression, "");
    }

    /**
     * Create a named expression (for expression nesting)
     *
     * @param namespace
     * @param expression
     * @param name
     */
    public ExpressionExpression(ExpressionNamespace namespace, String expression, String name) {
        super(namespace, expression, "", name, null);
        try {
            ExpressionParser parser = new ExpressionParser(new StringReader(expression));
            root = parser.ExpressionScript();
        }
        catch (ParseException pe) {
            throw new WrappedRuntimeException(pe);
        }

        m_type = determineTypeFromAST();
        if (m_type == null) {
            throw new RuntimeException("unable to determine type from " + expression);
        }

        initializeLeafExpressionMapFromAST();
        initializeCflowExpressionMapFromAST();
    }

    /**
     * Type determination of AST
     * @return PointcutType of expression (or null)
     */
    private PointcutType determineTypeFromAST() {
        return (PointcutType)root.jjtAccept(TYPE_VISITOR, m_namespace);
    }

    /**
     * Identifier lookup
     */
    private void initializeLeafExpressionMapFromAST() {
        //TODO do we really need name->expr mapping ?
        // ..and not only name list
        //TODO: NOTE: "false AND subNodeMap" NODES should be ignored
        // ..but can be complex due to (NOT true) AND subNodeMap syntax style

        List leafNames = new ArrayList();
        root.jjtAccept(IDENTIFIER_VISITOR, leafNames);
        String leafName = null;
        for (Iterator i = leafNames.iterator(); i.hasNext();) {
            leafName = (String)i.next();
            m_expressionRefs.put(leafName, m_namespace.getExpression(leafName));
        }
    }

    /**
     * Cflow identifier lookup
     */
    private void initializeCflowExpressionMapFromAST() {
        //TODO do we really need name->expr mapping ?
        // ..and not only name list
        //TODO: NOTE: "false AND subNodeMap" NODES should be ignored
        // ..but can be complex due to (NOT true) AND subNodeMap syntax style

        List cflowNames = new ArrayList();
        root.jjtAccept(CFLOWIDENTIFIER_VISITOR, cflowNames);
        String cflowName = null;
        for (Iterator i = cflowNames.iterator(); i.hasNext();) {
            cflowName = (String)i.next();
            m_cflowExpressionRefs.put(cflowName, m_namespace.getExpression(cflowName));
        }
        if (m_cflowExpressionRefs.size() > 1)
            throw new RuntimeException("complex cflow expression not supported yet");
    }

    public boolean match(final ClassMetaData classMetaData) {
        for (Iterator it = m_expressionRefs.values().iterator(); it.hasNext();) {
            Expression expression = (Expression)it.next();
            if (expression.match(classMetaData)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the expression matches a certain join point as regards the IN and NOT IN parts if any.
     * Each IN / NOT IN part is evaluated independantly from the boolean algebra (TF time)
     *
     * <p/>Only checks for a class match to allow early filtering.
     * <p/>Only does a qualified guess, does not evaluate the whole expression since doing it only on class
     * level would give the false results.
     *
     * @param classMetaData the class meta-data
     * @return boolean
     */
    public boolean matchInOrNotIn(final ClassMetaData classMetaData) {
        for (Iterator it = m_cflowExpressionRefs.values().iterator(); it.hasNext();) {
            Expression expression = (Expression)it.next();
            if (expression.match(classMetaData)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the expression matches a certain join point as regards IN / NOT IN parts
     * Each IN / NOT IN part is evaluated independantly from the boolean algebra (TF time)
     *
     * @param classMetaData the class meta-data
     * @param memberMetaData the meta-data for the member
     * @return boolean
     */
    public boolean matchInOrNotIn(final ClassMetaData classMetaData, final MemberMetaData memberMetaData) {
        for (Iterator it = m_cflowExpressionRefs.values().iterator(); it.hasNext();) {
            Expression expression = (Expression)it.next();
            if (expression.match(classMetaData, memberMetaData)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the expression matches a certain join point.
     * <p/>Special case in the API which tries to match exception types as well.
     *
     * @param classMetaData the class meta-data
     * @param memberMetaData the meta-data for the member
     * @param exceptionType the exception type (null => match all)
     * @return boolean
     */
    public boolean match(final ClassMetaData classMetaData,
                         final MemberMetaData memberMetaData,
                         final String exceptionType) {
        if (exceptionType != null) {
            throw new RuntimeException("expression of type " + m_type.toString() + "cannot evaluate exception type");
        }
        ExpressionContext ctx = new ExpressionContext(
                m_type, m_namespace, classMetaData, memberMetaData, exceptionType
        );
        return ((Boolean)root.jjtAccept(EVALUATE_VISITOR, ctx)).booleanValue();
    }

    /**
     * Checks if the expression matches a certain join point.
     *
     * @param classMetaData the class meta-data
     * @param memberMetaData the meta-data for the member
     * @return boolean
     */
    public boolean match(final ClassMetaData classMetaData, final MemberMetaData memberMetaData) {
        return match(classMetaData, memberMetaData, null);
    }

    /**
     * Return a Map(name->Expression) of expression involved in the
     * IN and NOT IN sub-expression of this Expression
     * (can be empty)
     * @return Map(name->Expression)
     */
    public Map getCflowExpressions() {
        return m_cflowExpressionRefs;
    }
}
