/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
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

/**
 * Evaluate the expression, ignore the CFLOW sub-expressions<br/>
 * Resulting Boolean is returned<br/>
 * Visit' data is namespace to retrieve expression from literals
 *
 * This visit should be used at TF time only
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class EvaluateVisitor implements ExpressionParserVisitor {

    public Object visit(SimpleNode node, Object data) {
        Object res = node.jjtGetChild(0).jjtAccept(this, data);
        return res;
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

//    public Object visit(InNode node, Object data) {
//        return node.jjtGetChild(0).jjtAccept(this, data);
//    }
//
//    public Object visit(NotInNode node, Object data) {
//        return node.jjtGetChild(0).jjtAccept(this, data);
//    }

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
        ExpressionContext ctx = (ExpressionContext)data;
        String leafName = node.name;
        Expression expression = ctx.getNamespace().getExpression(leafName);
        if (expression != null) {
            if (PointcutType.isCflowTypeOnly(expression.getTypes())) {
                return Boolean.TRUE;//match at TF time
            } else {
                //TODO WITHIN
                return new Boolean(expression.match(ctx.getClassMetaData(),
                        ctx.getMemberMetaData(), ctx.getExceptionType(), ctx.getPointcutType())
                );
            }
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
