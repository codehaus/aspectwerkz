/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition.expression.visitor;

import java.util.List;

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
import org.codehaus.aspectwerkz.definition.expression.ExpressionNamespace;
import org.codehaus.aspectwerkz.definition.expression.Expression;
import org.codehaus.aspectwerkz.definition.expression.LeafExpression;
import org.codehaus.aspectwerkz.definition.expression.PointcutType;
import org.codehaus.aspectwerkz.definition.expression.CflowExpression;

/**
 * Gather all literal part of a CFLOW typed sub-expression<br/>
 * Build the list of literal in visit' CflowIdentifierLookupVisitorContext
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class CflowIdentifierLookupVisitor implements ExpressionParserVisitor {

//    private static ThreadLocal IN_INORNOTIN_EXPR = new ThreadLocal() {
//        public Object initialValue() {
//            return Boolean.FALSE;
//        }
//    };

    public Object visit(SimpleNode node, Object data) {
//        IN_INORNOTIN_EXPR.set(Boolean.FALSE);
        node.jjtGetChild(0).jjtAccept(this, data);
//        IN_INORNOTIN_EXPR.set(null);
        return data;
    }

    public Object visit(ExpressionScript node, Object data) {
        node.jjtGetChild(0).jjtAccept(this, data);
        return data;
    }

    public Object visit(OrNode node, Object data) {
        node.jjtGetChild(0).jjtAccept(this, data);
        node.jjtGetChild(1).jjtAccept(this, data);
        return data;
    }

//    public Object visit(InNode node, Object data) {
//        node.jjtGetChild(0).jjtAccept(this, data);
//        IN_INORNOTIN_EXPR.set(Boolean.TRUE);
//        node.jjtGetChild(1).jjtAccept(this, data);
//        IN_INORNOTIN_EXPR.set(Boolean.FALSE);
//        return data;
//    }
//
//    public Object visit(NotInNode node, Object data) {
//        node.jjtGetChild(0).jjtAccept(this, data);
//        IN_INORNOTIN_EXPR.set(Boolean.TRUE);
//        node.jjtGetChild(1).jjtAccept(this, data);
//        IN_INORNOTIN_EXPR.set(Boolean.FALSE);
//        return data;
//    }

    public Object visit(AndNode node, Object data) {
        node.jjtGetChild(0).jjtAccept(this, data);
        node.jjtGetChild(1).jjtAccept(this, data);
        return data;
    }

    public Object visit(NotNode node, Object data) {
        node.jjtGetChild(0).jjtAccept(this, data);
        return data;
    }

    public Object visit(Identifier node, Object data) {
//        Boolean isInInOrNotIn = (Boolean)IN_INORNOTIN_EXPR.get();
//        if (isInInOrNotIn.booleanValue()) {
//            ((List)data).add(node.name);
//        }
        CflowIdentifierLookupVisitorContext context = (CflowIdentifierLookupVisitorContext)data;
        ExpressionNamespace space = context.getNamespace();
        Expression expression = space.getExpression(node.name);
        if (expression != null) {
            //TODO nested expression support
            if (! (expression instanceof LeafExpression)) {
                //context.addNames(expression.getCflowExpressions().keySet());
                //throw new RuntimeException("nested expr in Cflow Id visitor");
                return data;//SKIP
            } else {
                LeafExpression leaf = (LeafExpression) expression;
                // LeafExpression has a sole type
                if (leaf.getTypes().contains(PointcutType.CFLOW)) {
                    context.addName(node.name);
                }
            }
        } else {
            throw new RuntimeException("No such registered expression: " + node.name);
        }
        return data;
    }

    public Object visit(BooleanLiteral node, Object data) {
        return data;
    }

    public Object visit(TrueNode node, Object data) {
        return data;
    }

    public Object visit(FalseNode node, Object data) {
        return data;
    }

    public Object visit(Anonymous node, Object data) {
        CflowIdentifierLookupVisitorContext context = (CflowIdentifierLookupVisitorContext)data;
        if (node.name.startsWith("cflow(")) {
            CflowExpression expr = context.getNamespace().createCflowExpression(
                    node.name.substring(6, node.name.length()-1), "", ""
            );
            context.addAnonymous(expr);
        }
        return data;
    }
}
