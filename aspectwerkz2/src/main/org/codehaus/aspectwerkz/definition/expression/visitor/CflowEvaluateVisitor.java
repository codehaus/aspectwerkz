/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition.expression.visitor;

import org.codehaus.aspectwerkz.definition.expression.Expression;
import org.codehaus.aspectwerkz.definition.expression.ExpressionContext;
import org.codehaus.aspectwerkz.definition.expression.PointcutType;
import org.codehaus.aspectwerkz.definition.expression.ast.AndNode;
import org.codehaus.aspectwerkz.definition.expression.ast.BooleanLiteral;
import org.codehaus.aspectwerkz.definition.expression.ast.ExpressionParserVisitor;
import org.codehaus.aspectwerkz.definition.expression.ast.ExpressionScript;
import org.codehaus.aspectwerkz.definition.expression.ast.FalseNode;
import org.codehaus.aspectwerkz.definition.expression.ast.Identifier;
import org.codehaus.aspectwerkz.definition.expression.ast.NotNode;
import org.codehaus.aspectwerkz.definition.expression.ast.OrNode;
import org.codehaus.aspectwerkz.definition.expression.ast.SimpleNode;
import org.codehaus.aspectwerkz.definition.expression.ast.TrueNode;
import org.codehaus.aspectwerkz.definition.expression.ast.CflowPattern;

/**
 * Evaluates a cflow extracted expression given a cflow metadata stack
 *
 * <pre>
 * stack = { m1, m2}
 * true AND cf
 *  can return true if cf match m1 *OR* cf match m2
 * (match is done on the stack)
 * </pre>
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class CflowEvaluateVisitor implements ExpressionParserVisitor {

    public Object visit(SimpleNode node, Object data) {
        return node.jjtGetChild(0).jjtAccept(this, data);
    }

    public Object visit(ExpressionScript node, Object data) {
        return node.jjtGetChild(0).jjtAccept(this, data);
    }

    public Object visit(OrNode node, Object data) {
        Boolean lhs = (Boolean)node.jjtGetChild(0).jjtAccept(this, data);
        if (lhs.booleanValue()) {
            return Boolean.TRUE;
        }
        Boolean rhs = (Boolean)node.jjtGetChild(1).jjtAccept(this, data);
        return rhs;
    }

    public Object visit(AndNode node, Object data) {
        Boolean lhs = (Boolean)node.jjtGetChild(0).jjtAccept(this, data);
        if (!lhs.booleanValue()) {
            return Boolean.FALSE;
        }
        Boolean rhs = (Boolean)node.jjtGetChild(1).jjtAccept(this, data);
        return rhs;
    }

    public Object visit(NotNode node, Object data) {
        Boolean lhs = (Boolean)node.jjtGetChild(0).jjtAccept(this, data);
        if (lhs.booleanValue()) {
            return Boolean.FALSE;
        }
        else {
            return Boolean.TRUE;
        }
    }

    public Object visit(Identifier node, Object data) {
        CflowExpressionContext ctx = (CflowExpressionContext)data;
        String leafName = node.name;
        Expression expression = ctx.getNamespace().getExpression(leafName);
        if (expression != null) {
            return new Boolean(expression.matchCflow(ctx.getClassNameMethodMetaDataTuples()));
        }
        else {
            throw new RuntimeException("no such registered expression");
        }
    }

    public Object visit(BooleanLiteral node, Object data) {
        return node.jjtGetChild(0).jjtAccept(this, data);
    }

    public Object visit(TrueNode node, Object data) {
        return Boolean.TRUE;
    }

    public Object visit(FalseNode node, Object data) {
        return Boolean.FALSE;
    }


    //------------------------

}
