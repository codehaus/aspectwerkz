/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.cflow;

import org.codehaus.aspectwerkz.expression.ast.ExpressionParserVisitor;
import org.codehaus.aspectwerkz.expression.ast.SimpleNode;
import org.codehaus.aspectwerkz.expression.ast.ASTRoot;
import org.codehaus.aspectwerkz.expression.ast.ASTExpression;
import org.codehaus.aspectwerkz.expression.ast.ASTAnd;
import org.codehaus.aspectwerkz.expression.ast.ASTOr;
import org.codehaus.aspectwerkz.expression.ast.ASTNot;
import org.codehaus.aspectwerkz.expression.ast.ASTPointcutReference;
import org.codehaus.aspectwerkz.expression.ast.ASTExecution;
import org.codehaus.aspectwerkz.expression.ast.ASTCall;
import org.codehaus.aspectwerkz.expression.ast.ASTSet;
import org.codehaus.aspectwerkz.expression.ast.ASTGet;
import org.codehaus.aspectwerkz.expression.ast.ASTHandler;
import org.codehaus.aspectwerkz.expression.ast.ASTWithin;
import org.codehaus.aspectwerkz.expression.ast.ASTWithinCode;
import org.codehaus.aspectwerkz.expression.ast.ASTStaticInitialization;
import org.codehaus.aspectwerkz.expression.ast.ASTCflow;
import org.codehaus.aspectwerkz.expression.ast.ASTCflowBelow;
import org.codehaus.aspectwerkz.expression.ast.ASTArgs;
import org.codehaus.aspectwerkz.expression.ast.ASTHasMethod;
import org.codehaus.aspectwerkz.expression.ast.ASTHasField;
import org.codehaus.aspectwerkz.expression.ast.ASTTarget;
import org.codehaus.aspectwerkz.expression.ast.ASTThis;
import org.codehaus.aspectwerkz.expression.ast.ASTClassPattern;
import org.codehaus.aspectwerkz.expression.ast.ASTMethodPattern;
import org.codehaus.aspectwerkz.expression.ast.ASTConstructorPattern;
import org.codehaus.aspectwerkz.expression.ast.ASTFieldPattern;
import org.codehaus.aspectwerkz.expression.ast.ASTParameter;
import org.codehaus.aspectwerkz.expression.ast.ASTArgParameter;
import org.codehaus.aspectwerkz.expression.ast.ASTAttribute;
import org.codehaus.aspectwerkz.expression.ast.ASTModifier;
import org.codehaus.aspectwerkz.expression.ast.Node;
import org.codehaus.aspectwerkz.expression.Undeterministic;
import org.codehaus.aspectwerkz.expression.ExpressionContext;
import org.codehaus.aspectwerkz.expression.ExpressionNamespace;
import org.codehaus.aspectwerkz.expression.ExpressionVisitor;
import org.codehaus.aspectwerkz.expression.ExpressionInfo;

import java.util.List;

/**
 * A visitor to create the bindings between cflow aspect and cflow subexpression.
 * For each visited cflow / cflowbelow node, one CflowBinding is created
 * with the cflow(below) subexpression as expressionInfo.
 *
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class CflowAspectExpressionVisitor implements ExpressionParserVisitor {

    private Node m_root;
    private String m_namespace;

    public CflowAspectExpressionVisitor(Node root, String namespace) {
        m_root = root;
        m_namespace = namespace;
    }

    public List populateCflowAspectBindings(List bindings) {
        visit(m_root, bindings);
        return bindings;
    }

    public Object visit(Node node, Object data) {
        return node.jjtGetChild(0).jjtAccept(this, data);
    }

    public Object visit(SimpleNode node, Object data) {
        return node.jjtGetChild(0).jjtAccept(this, data);
    }

    public Object visit(ASTRoot node, Object data) {
        return node.jjtGetChild(0).jjtAccept(this, data);
    }

    public Object visit(ASTExpression node, Object data) {
        return node.jjtGetChild(0).jjtAccept(this, data);
    }

    public Object visit(ASTAnd node, Object data) {
        // the AND and OR can have more than 2 nodes [see jjt grammar]
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).jjtAccept(this, data);
        }
        return data;
    }

    public Object visit(ASTOr node, Object data) {
        // the AND and OR can have more than 2 nodes [see jjt grammar]
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).jjtAccept(this, data);
        }
        return data;
    }

    public Object visit(ASTNot node, Object data) {
        return node.jjtGetChild(0).jjtAccept(this, data);
    }

    public Object visit(ASTPointcutReference node, Object data) {
        ExpressionNamespace namespace = ExpressionNamespace.getNamespace(m_namespace);
        CflowAspectExpressionVisitor expression = namespace.getExpressionInfo(node.getName()).getCflowAspectExpression();
        return expression.populateCflowAspectBindings((List)data);
    }

    public Object visit(ASTExecution node, Object data) {
        return data;
    }

    public Object visit(ASTCall node, Object data) {
        return data;
    }

    public Object visit(ASTSet node, Object data) {
        return data;
    }

    public Object visit(ASTGet node, Object data) {
        return data;
    }

    public Object visit(ASTHandler node, Object data) {
        return data;
    }

    public Object visit(ASTWithin node, Object data) {
        return data;
    }

    public Object visit(ASTWithinCode node, Object data) {
        return data;
    }

    public Object visit(ASTStaticInitialization node, Object data) {
        return data;
    }

    public Object visit(ASTCflow node, Object data) {
        int cflowID = node.hashCode();
        Node subNode = node.jjtGetChild(0);
        ExpressionInfo subExpression = new ExpressionInfo(subNode, m_namespace);
        ((List)data).add(new CflowBinding(cflowID, subExpression, false));
        return data;
    }

    public Object visit(ASTCflowBelow node, Object data) {
        int cflowID = node.hashCode();
        Node subNode = node.jjtGetChild(0);
        ExpressionInfo subExpression = new ExpressionInfo(subNode, m_namespace);
        ((List)data).add(new CflowBinding(cflowID, subExpression, true));
        return data;
    }

    public Object visit(ASTArgs node, Object data) {
        return data;
    }

    public Object visit(ASTHasMethod node, Object data) {
        return data;
    }

    public Object visit(ASTHasField node, Object data) {
        return data;
    }

    public Object visit(ASTTarget node, Object data) {
        return data;
    }

    public Object visit(ASTThis node, Object data) {
        return data;
    }

    public Object visit(ASTClassPattern node, Object data) {
        throw new UnsupportedOperationException("Should not be reached");
    }

    public Object visit(ASTMethodPattern node, Object data) {
        throw new UnsupportedOperationException("Should not be reached");
    }

    public Object visit(ASTConstructorPattern node, Object data) {
        throw new UnsupportedOperationException("Should not be reached");
    }

    public Object visit(ASTFieldPattern node, Object data) {
        throw new UnsupportedOperationException("Should not be reached");
    }

    public Object visit(ASTParameter node, Object data) {
        throw new UnsupportedOperationException("Should not be reached");
    }

    public Object visit(ASTArgParameter node, Object data) {
        throw new UnsupportedOperationException("Should not be reached");
    }

    public Object visit(ASTAttribute node, Object data) {
        throw new UnsupportedOperationException("Should not be reached");
    }

    public Object visit(ASTModifier node, Object data) {
        throw new UnsupportedOperationException("Should not be reached");
    }
}
