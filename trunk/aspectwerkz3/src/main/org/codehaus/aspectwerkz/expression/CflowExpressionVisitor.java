/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.expression;

import org.codehaus.aspectwerkz.expression.ast.ASTAnd;
import org.codehaus.aspectwerkz.expression.ast.ASTCflow;
import org.codehaus.aspectwerkz.expression.ast.ASTCflowBelow;
import org.codehaus.aspectwerkz.expression.ast.ASTNot;
import org.codehaus.aspectwerkz.expression.ast.ASTOr;
import org.codehaus.aspectwerkz.expression.ast.ASTPointcutReference;
import org.codehaus.aspectwerkz.expression.ast.ASTRoot;
import java.io.ObjectInputStream;
import java.io.Serializable;

/**
 * The Cflow visitor.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class CflowExpressionVisitor extends ExpressionVisitor implements Serializable {
    /**
     * Do we have a cflow pointcut in the expression?
     */
    private boolean m_hasCflowPointcut = true;

    /**
     * Creates a new cflow expression.
     *
     * @param expression the expression as a string
     * @param namespace  the namespace
     * @param root       the AST root
     */
    public CflowExpressionVisitor(final String expression, final String namespace, final ASTRoot root) {
        super(expression, namespace, root);
    }

    /**
     * Checks if the expression has a cflow pointcut.
     *
     * @return
     */
    public boolean hasCflowPointcut() {
        return m_hasCflowPointcut;
    }

    /**
     * Matches the cflow epression
     *
     * @param context
     * @return
     */
    public boolean match(final ExpressionContext context) {
        Boolean match = (Boolean)visit(m_root, context);
        if (context.hasBeenVisitingCflow()) {
            // we have been visiting and evaluated a cflow sub expression
            m_hasCflowPointcut = true;
            return context.getCflowEvaluation();
        } else if (context.inCflowSubAST()) {
            // we are in a referenced expression within a cflow subtree
            return match.booleanValue();
        }
        return false;
    }

    // ============ Logical operators =============
    public Object visit(ASTOr node, Object data) {
        ExpressionContext context = (ExpressionContext)data;
        int nrOfChildren = node.jjtGetNumChildren();
        if (context.inCflowSubAST()) {
            return super.visit(node, data);
        } else {
            for (int i = 0; i < nrOfChildren; i++) {
                node.jjtGetChild(i).jjtAccept(this, data);
            }
            return Boolean.TRUE;
        }
    }

    public Object visit(ASTAnd node, Object data) {
        ExpressionContext context = (ExpressionContext)data;
        int nrOfChildren = node.jjtGetNumChildren();
        if (context.inCflowSubAST()) {
            return super.visit(node, data);
        } else {
            for (int i = 0; i < nrOfChildren; i++) {
                node.jjtGetChild(i).jjtAccept(this, data);
            }
            return Boolean.TRUE;
        }
    }

    public Object visit(ASTNot node, Object data) {
        Boolean match = (Boolean)node.jjtGetChild(0).jjtAccept(this, data);
        if (match.equals(Boolean.TRUE)) {
            return Boolean.FALSE;
        } else {
            return Boolean.TRUE;
        }
    }

    // ============ Cflow pointcut types =============
    public Object visit(ASTCflow node, Object data) {
        ExpressionContext context = (ExpressionContext)data;
        context.setInCflowSubAST(true);
        Boolean result = (Boolean)node.jjtGetChild(0).jjtAccept(this, context);
        if (context.getCflowEvaluation() == false)
            context.setCflowEvaluation(result.booleanValue());
        context.setHasBeenVisitingCflow(true);
        context.setInCflowSubAST(false);
        return new Boolean(context.getCflowEvaluation());
    }

    public Object visit(ASTCflowBelow node, Object data) {
        ExpressionContext context = (ExpressionContext)data;
        context.setInCflowSubAST(true);
        Boolean result = (Boolean)node.jjtGetChild(0).jjtAccept(this, context);
        if (context.getCflowEvaluation() == false)
            context.setCflowEvaluation(result.booleanValue());
        context.setHasBeenVisitingCflow(true);
        context.setInCflowSubAST(false);
        return new Boolean(context.getCflowEvaluation());
    }

    // ============ Pointcut reference  =============
    public Object visit(ASTPointcutReference node, Object data) {
        ExpressionContext context = (ExpressionContext)data;
        ExpressionNamespace namespace = ExpressionNamespace.getNamespace(m_namespace);
        return new Boolean(namespace.getCflowExpression(node.getName()).match(context));
    }

    /**
     * Provides custom deserialization.
     *
     * @param stream the object input stream containing the serialized object
     * @throws Exception in case of failure
     */
    private void readObject(final ObjectInputStream stream) throws Exception {
        ObjectInputStream.GetField fields = stream.readFields();
        m_expression = (String)fields.get("m_expression", null);
        m_namespace = (String)fields.get("m_namespace", null);
        m_hasCflowPointcut = fields.get("m_namespace", false);
        m_root = ExpressionInfo.getParser().parse(m_expression);
    }
}
