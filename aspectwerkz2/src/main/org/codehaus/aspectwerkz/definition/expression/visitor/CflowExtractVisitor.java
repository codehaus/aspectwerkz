/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition.expression.visitor;

import org.codehaus.aspectwerkz.definition.expression.Expression;
import org.codehaus.aspectwerkz.definition.expression.ExpressionContext;
import org.codehaus.aspectwerkz.definition.expression.PointcutType;
import org.codehaus.aspectwerkz.definition.expression.LeafExpression;
import org.codehaus.aspectwerkz.definition.expression.CflowExpression;
import org.codehaus.aspectwerkz.definition.expression.ExpressionExpression;
import org.codehaus.aspectwerkz.definition.expression.ExpressionNamespace;
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
import org.codehaus.aspectwerkz.definition.expression.ast.Anonymous;

import java.util.Set;
import java.util.HashSet;

/**
 * Evaluate the expression, and build a simplified representation of it
 * with only TRUE / FALSE and the CFLOW sub-expressions<br/>
 * Resulting expression string is returned<br/>
 * Visit' data is evaluation context to retrieve expression from literals
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class CflowExtractVisitor implements ExpressionParserVisitor {

    private final StringBuffer TRUE = new StringBuffer("true");
    private final StringBuffer FALSE = new StringBuffer("false");

    public Object visit(SimpleNode node, Object data) {
        return node.jjtGetChild(0).jjtAccept(this, data);
    }

    public Object visit(ExpressionScript node, Object data) {
        return node.jjtGetChild(0).jjtAccept(this, data);
    }

    public Object visit(OrNode node, Object data) {
        StringBuffer lhs = (StringBuffer)node.jjtGetChild(0).jjtAccept(this, data);
        StringBuffer  rhs = (StringBuffer)node.jjtGetChild(1).jjtAccept(this, data);
        StringBuffer expr = new StringBuffer("").append(lhs.toString()).append(" OR ").append(rhs.toString()).append("");
        return expr;
    }

    public Object visit(AndNode node, Object data) {
        StringBuffer lhs = (StringBuffer)node.jjtGetChild(0).jjtAccept(this, data);
        StringBuffer rhs = (StringBuffer)node.jjtGetChild(1).jjtAccept(this, data);
        StringBuffer expr = new StringBuffer("").append(lhs.toString()).append(" AND ").append(rhs.toString()).append("");
        return expr;
    }

    public Object visit(NotNode node, Object data) {
        StringBuffer lhs = (StringBuffer)node.jjtGetChild(0).jjtAccept(this, data);
        StringBuffer expr = new StringBuffer("NOT ").append(lhs.toString()).append("");
        return expr;
    }

    public Object visit(Identifier node, Object data) {
        ExpressionContext ctx = (ExpressionContext)data;
        String leafName = node.name;
        Expression expression = ctx.getNamespace().getExpression(leafName);
        if (expression != null) {
            if (expression instanceof CflowExpression) {
                return new StringBuffer(leafName);
            }
            else if (expression instanceof LeafExpression) {
                if (expression.match(ctx.getClassMetaData(),
                        ctx.getMemberMetaData(), ctx.getExceptionType(), ctx.getPointcutType())) {
                    return TRUE;
                } else {
                    return FALSE;
                }
            }
            else {
                // recursive extraction
                ExpressionExpression expr = (ExpressionExpression)expression;
                StringBuffer referenced = (StringBuffer)(expr.getRoot().jjtAccept(this, data));
                return referenced.insert(0, "(").append(")");//TODO is that SAFE ?
            }
        } else {
            throw new RuntimeException("no such registered expression");
        }
    }

    public Object visit(BooleanLiteral node, Object data) {
        return node.jjtGetChild(0).jjtAccept(this, data);
    }

    public Object visit(TrueNode node, Object data) {
        return TRUE;
    }

    public Object visit(FalseNode node, Object data) {
        return FALSE;
    }

    public Object visit(Anonymous node, Object data) {
        String expr = node.name;
        if (expr.startsWith("cflow(")) {
            return new StringBuffer(expr);
        } else {
            Expression expression = null;
            ExpressionContext ctx = (ExpressionContext)data;
            ExpressionNamespace ns = ctx.getNamespace();
            if (expr.startsWith("execution(")) {
                expression = ns.createExecutionExpression(
                        expr.substring(10, expr.length()-1),
                        "",""
                );
            } else if (expr.startsWith("call(")) {
                expression = ns.createCallExpression(
                        expr.substring(5, expr.length()-1),
                        "",""
                );
            } else if (expr.startsWith("set(")) {
                expression = ns.createSetExpression(
                        expr.substring(4, expr.length()-1),
                        "",""
                );
            } else if (expr.startsWith("get(")) {
                expression = ns.createGetExpression(
                        expr.substring(4, expr.length()-1),
                        "",""
                );
            } else if (expr.startsWith("class(")) {
                expression = ns.createClassExpression(
                        expr.substring(6, expr.length()-1),
                        "",""
                );
            } else if (expr.startsWith("handler(")) {
                expression = ns.createHandlerExpression(
                        expr.substring(8, expr.length()-1),
                        "",""
                );
            } else {
                throw new RuntimeException("unknown anonymous: "+expr);
            }
            if (expression.match(
                    ctx.getClassMetaData(), ctx.getMemberMetaData(), ctx.getExceptionType(), ctx.getPointcutType())) {
                return TRUE;
            } else {
                return FALSE;
            }
        }
    }

    //------------------------

}
