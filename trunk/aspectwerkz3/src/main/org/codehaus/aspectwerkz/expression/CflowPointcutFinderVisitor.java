/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.expression;

import org.codehaus.aspectwerkz.expression.ast.ASTAnd;
import org.codehaus.aspectwerkz.expression.ast.ASTAttribute;
import org.codehaus.aspectwerkz.expression.ast.ASTCall;
import org.codehaus.aspectwerkz.expression.ast.ASTCflow;
import org.codehaus.aspectwerkz.expression.ast.ASTCflowBelow;
import org.codehaus.aspectwerkz.expression.ast.ASTClassPattern;
import org.codehaus.aspectwerkz.expression.ast.ASTConstructorPattern;
import org.codehaus.aspectwerkz.expression.ast.ASTExecution;
import org.codehaus.aspectwerkz.expression.ast.ASTExpression;
import org.codehaus.aspectwerkz.expression.ast.ASTFieldPattern;
import org.codehaus.aspectwerkz.expression.ast.ASTGet;
import org.codehaus.aspectwerkz.expression.ast.ASTHandler;
import org.codehaus.aspectwerkz.expression.ast.ASTMethodPattern;
import org.codehaus.aspectwerkz.expression.ast.ASTModifier;
import org.codehaus.aspectwerkz.expression.ast.ASTNot;
import org.codehaus.aspectwerkz.expression.ast.ASTOr;
import org.codehaus.aspectwerkz.expression.ast.ASTParameter;
import org.codehaus.aspectwerkz.expression.ast.ASTPointcutReference;
import org.codehaus.aspectwerkz.expression.ast.ASTRoot;
import org.codehaus.aspectwerkz.expression.ast.ASTSet;
import org.codehaus.aspectwerkz.expression.ast.ASTStaticInitialization;
import org.codehaus.aspectwerkz.expression.ast.ASTWithin;
import org.codehaus.aspectwerkz.expression.ast.ASTWithinCode;
import org.codehaus.aspectwerkz.expression.ast.ExpressionParserVisitor;
import org.codehaus.aspectwerkz.expression.ast.SimpleNode;

/**
 * Checks if the expression has a cflow pointcut.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class CflowPointcutFinderVisitor implements ExpressionParserVisitor {
    protected final ASTRoot m_root;
    protected final String m_expression;
    protected final String m_namespace;

    /**
    * Creates a new finder.
    *
    * @param expression the expression as a string
    * @param namespace  the namespace
    * @param root       the AST root
    */
    public CflowPointcutFinderVisitor(final String expression, final String namespace, final ASTRoot root) {
        m_root = root;
        m_expression = expression;
        m_namespace = namespace;
    }

    /**
    * Checks if the expression has a cflow pointcut.
    *
    * @return
    */
    public boolean hasCflowPointcut() {
        return ((Boolean)visit(m_root, null)).booleanValue();
    }

    // ============ Boot strap =============
    public Object visit(SimpleNode node, Object data) {
        return node.jjtGetChild(0).jjtAccept(this, data);
    }

    public Object visit(ASTRoot node, Object data) {
        return node.jjtGetChild(0).jjtAccept(this, data);
    }

    public Object visit(ASTExpression node, Object data) {
        return node.jjtGetChild(0).jjtAccept(this, data);
    }

    // ============ Logical operators =============
    public Object visit(ASTOr node, Object data) {
        int nrOfChildren = node.jjtGetNumChildren();
        for (int i = 0; i < nrOfChildren; i++) {
            if (((Boolean)node.jjtGetChild(i).jjtAccept(this, data)).booleanValue()) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    public Object visit(ASTAnd node, Object data) {
        int nrOfChildren = node.jjtGetNumChildren();
        for (int i = 0; i < nrOfChildren; i++) {
            if (((Boolean)node.jjtGetChild(i).jjtAccept(this, data)).booleanValue()) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    public Object visit(ASTNot node, Object data) {
        return node.jjtGetChild(0).jjtAccept(this, data);
    }

    // ============ Pointcut types =============
    public Object visit(ASTPointcutReference node, Object data) {
        ExpressionNamespace namespace = ExpressionNamespace.getNamespace(m_namespace);
        ExpressionInfo expressionInfo = namespace.getExpressionInfo(node.getName());
        if (expressionInfo.hasCflowPointcut()) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    public Object visit(ASTExecution node, Object data) {
        return Boolean.FALSE;
    }

    public Object visit(ASTCall node, Object data) {
        return Boolean.FALSE;
    }

    public Object visit(ASTSet node, Object data) {
        return Boolean.FALSE;
    }

    public Object visit(ASTGet node, Object data) {
        return Boolean.FALSE;
    }

    public Object visit(ASTHandler node, Object data) {
        return Boolean.FALSE;
    }

    public Object visit(ASTStaticInitialization node, Object data) {
        return Boolean.FALSE;
    }

    public Object visit(ASTWithin node, Object data) {
        return Boolean.FALSE;
    }

    public Object visit(ASTWithinCode node, Object data) {
        return Boolean.FALSE;
    }

    public Object visit(ASTCflow node, Object data) {
        return Boolean.TRUE;
    }

    public Object visit(ASTCflowBelow node, Object data) {
        return Boolean.TRUE;
    }

    // ============ Patterns =============
    public Object visit(ASTClassPattern node, Object data) {
        return Boolean.FALSE;
    }

    public Object visit(ASTMethodPattern node, Object data) {
        return Boolean.FALSE;
    }

    public Object visit(ASTConstructorPattern node, Object data) {
        return Boolean.FALSE;
    }

    public Object visit(ASTFieldPattern node, Object data) {
        return Boolean.FALSE;
    }

    public Object visit(ASTParameter node, Object data) {
        return Boolean.FALSE;
    }

    public Object visit(ASTAttribute node, Object data) {
        return Boolean.FALSE;
    }

    public Object visit(ASTModifier node, Object data) {
        return Boolean.FALSE;
    }

    /**
    * Returns the string representation of the AST.
    *
    * @return
    */
    public String toString() {
        return m_expression;
    }

    /**
    * Returns the namespace.
    *
    * @return
    */
    public String getNamespace() {
        return m_namespace;
    }
}
