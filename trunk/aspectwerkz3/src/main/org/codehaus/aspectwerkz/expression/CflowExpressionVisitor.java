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
import org.codehaus.aspectwerkz.expression.ast.ASTOr;
import org.codehaus.aspectwerkz.expression.ast.ASTPointcutReference;
import org.codehaus.aspectwerkz.expression.ast.ASTRoot;

/**
 * The Cflow visitor.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class CflowExpressionVisitor extends ExpressionVisitor {
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
     * Matches the expression context.
     *
     * @param context
     * @return
     */
    public boolean match(final ExpressionContext context) {
        Boolean match = (Boolean)visit(m_root, context);

        if (context.hasBeenVisitingCflow()) {
            // the case if we have been visiting and evaluated a cflow sub expression
            return context.getCflowEvaluation();
        } else if (context.inCflowSubAST()) {
            // the case if we are in a referenced expression within a cflow subtree
            return match.booleanValue();
        } else {
            // no cflow subtree has been evaluated
            return false;
        }
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

            return Boolean.FALSE;
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

            return Boolean.FALSE;
        }
    }

    // ============ Cflow pointcut types =============
    public Object visit(ASTCflow node, Object data) {
        ExpressionContext context = (ExpressionContext)data;

        context.setInCflowSubAST(true);

        Boolean result = (Boolean)node.jjtGetChild(0).jjtAccept(this, context);

        context.setCflowEvaluation(result.booleanValue());
        context.setHasBeenVisitingCflow(true);
        context.setInCflowSubAST(false);

        return Boolean.FALSE;
    }

    public Object visit(ASTCflowBelow node, Object data) {
        ExpressionContext context = (ExpressionContext)data;

        context.setInCflowSubAST(true);

        Boolean result = (Boolean)node.jjtGetChild(0).jjtAccept(this, context);

        context.setCflowEvaluation(result.booleanValue());
        context.setHasBeenVisitingCflow(true);
        context.setInCflowSubAST(false);

        return Boolean.FALSE;
    }

    // ============ Pointcut reference  =============
    public Object visit(ASTPointcutReference node, Object data) {
        ExpressionContext context = (ExpressionContext)data;
        ExpressionNamespace namespace = ExpressionNamespace.getNamespace(m_namespace);

        return Boolean.valueOf(namespace.getCflowExpression(node.getName()).match(context));
    }
}
