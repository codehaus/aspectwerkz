/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition.expression.visitor;

import org.codehaus.aspectwerkz.definition.expression.ast.ExpressionParserVisitor;
import org.codehaus.aspectwerkz.definition.expression.ast.SimpleNode;
import org.codehaus.aspectwerkz.definition.expression.ast.ExpressionScript;
import org.codehaus.aspectwerkz.definition.expression.ast.OrNode;
import org.codehaus.aspectwerkz.definition.expression.ast.InNode;
import org.codehaus.aspectwerkz.definition.expression.ast.NotInNode;
import org.codehaus.aspectwerkz.definition.expression.ast.AndNode;
import org.codehaus.aspectwerkz.definition.expression.ast.NotNode;
import org.codehaus.aspectwerkz.definition.expression.ast.Identifier;
import org.codehaus.aspectwerkz.definition.expression.ast.BooleanLiteral;
import org.codehaus.aspectwerkz.definition.expression.ast.TrueNode;
import org.codehaus.aspectwerkz.definition.expression.ast.FalseNode;
import org.codehaus.aspectwerkz.definition.expression.PointcutType;
import org.codehaus.aspectwerkz.definition.expression.ExpressionNamespace;
import org.codehaus.aspectwerkz.definition.expression.Expression;

import java.util.Map;
import java.util.List;

/**
 * Gather all literal (including part of an IN or NOT IN sub-expression)
 * Build the list of literal in visit' data
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class IdentifierLookupVisitor implements ExpressionParserVisitor {

    public Object visit(SimpleNode node, Object data) {
        node.jjtGetChild(0).jjtAccept(this, data);
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

    public Object visit(InNode node, Object data) {
        node.jjtGetChild(0).jjtAccept(this, data);
        node.jjtGetChild(1).jjtAccept(this, data);
        return data;
    }

    public Object visit(NotInNode node, Object data) {
        node.jjtGetChild(0).jjtAccept(this, data);
        node.jjtGetChild(1).jjtAccept(this, data);
        return data;
    }

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
        ((List) data).add(node.name);
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
}
