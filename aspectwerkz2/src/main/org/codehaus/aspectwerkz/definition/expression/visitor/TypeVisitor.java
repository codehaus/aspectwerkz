/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition.expression.visitor;

import org.codehaus.aspectwerkz.definition.expression.Expression;
import org.codehaus.aspectwerkz.definition.expression.ExpressionNamespace;
import org.codehaus.aspectwerkz.definition.expression.PointcutType;
import org.codehaus.aspectwerkz.definition.expression.ast.AndNode;
import org.codehaus.aspectwerkz.definition.expression.ast.BooleanLiteral;
import org.codehaus.aspectwerkz.definition.expression.ast.ExpressionParserVisitor;
import org.codehaus.aspectwerkz.definition.expression.ast.ExpressionScript;
import org.codehaus.aspectwerkz.definition.expression.ast.FalseNode;
import org.codehaus.aspectwerkz.definition.expression.ast.Identifier;
import org.codehaus.aspectwerkz.definition.expression.ast.InNode;
import org.codehaus.aspectwerkz.definition.expression.ast.NotInNode;
import org.codehaus.aspectwerkz.definition.expression.ast.NotNode;
import org.codehaus.aspectwerkz.definition.expression.ast.OrNode;
import org.codehaus.aspectwerkz.definition.expression.ast.SimpleNode;
import org.codehaus.aspectwerkz.definition.expression.ast.TrueNode;

/**
 * Determine expression type and check IN and NOT IN type is CFLOW PointcutType is returned Visit' data is namespace
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class TypeVisitor implements ExpressionParserVisitor {

    public Object visit(SimpleNode node, Object data) {
        Object res = node.jjtGetChild(0).jjtAccept(this, data);
        return res;
    }

    public Object visit(ExpressionScript node, Object data) {
        return node.jjtGetChild(0).jjtAccept(this, data);
    }

    public Object visit(OrNode node, Object data) {
        return getResultingType(node, this, data);
    }

    public Object visit(InNode node, Object data) {
        // assert RHS is of CFLOW type
        // note: anonymous type like "IN true" is assumed valid
        PointcutType rhs = getRightHS(node, this, data);
        if (rhs != null && !rhs.equals(PointcutType.CFLOW))
            throw new RuntimeException("IN type not valid");
        return getLeftHS(node, this, data);
    }

    public Object visit(NotInNode node, Object data) {
        // assert RHS is of CFLOW type
        // note: anonymous type like "IN true" is assumed valid
        PointcutType rhs = getRightHS(node, this, data);
        if (rhs != null && !rhs.equals(PointcutType.CFLOW))
            if (rhs != null && !rhs.equals(PointcutType.CFLOW))
                throw new RuntimeException("NOT IN type not valid");
        return getLeftHS(node, this, data);
    }

    public Object visit(AndNode node, Object data) {
        return getResultingType(node, this, data);
    }

    public Object visit(NotNode node, Object data) {
        return getLeftHS(node, this, data);
    }

    public Object visit(Identifier node, Object data) {
        ExpressionNamespace space = (ExpressionNamespace)data;
        Expression expression = space.getExpression(node.name);
        if (expression != null) {
            return expression.getType();
        }
        else {
            throw new RuntimeException("no such registered expression: " + node.name);
        }
    }

    public Object visit(BooleanLiteral node, Object data) {
        return null;
    }

    public Object visit(TrueNode node, Object data) {
        // never reached
        return null;
    }

    public Object visit(FalseNode node, Object data) {
        // never reached
        return null;
    }


    //------------------------



    private PointcutType getLeftHS(SimpleNode node, ExpressionParserVisitor visitor, Object data) {
        return (PointcutType)node.jjtGetChild(0).jjtAccept(this, data);
    }

    private PointcutType getRightHS(SimpleNode node, ExpressionParserVisitor visitor, Object data) {
        return (PointcutType)node.jjtGetChild(1).jjtAccept(this, data);
    }

    private PointcutType getResultingType(SimpleNode node, ExpressionParserVisitor visitor, Object data) {
        PointcutType lhs = getLeftHS(node, this, data);
        PointcutType rhs = getRightHS(node, this, data);

        if (node.jjtGetChild(0) instanceof BooleanLiteral) {
            // ignore lhs literal
            return rhs;
        }
        else if (node.jjtGetChild(1) instanceof BooleanLiteral) {
            // ignore rhs literal
            return lhs;
        }
        else {
            if (rhs != null && rhs.equals(lhs)) {
                return rhs;
            }
            else {
                return null;
            }
        }
    }
}
