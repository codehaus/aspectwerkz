/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.expression;

import org.codehaus.aspectwerkz.expression.ast.ASTCall;
import org.codehaus.aspectwerkz.expression.ast.ASTCflow;
import org.codehaus.aspectwerkz.expression.ast.ASTCflowBelow;
import org.codehaus.aspectwerkz.expression.ast.ASTExecution;
import org.codehaus.aspectwerkz.expression.ast.ASTGet;
import org.codehaus.aspectwerkz.expression.ast.ASTHandler;
import org.codehaus.aspectwerkz.expression.ast.ASTPointcutReference;
import org.codehaus.aspectwerkz.expression.ast.ASTRoot;
import org.codehaus.aspectwerkz.expression.ast.ASTSet;
import org.codehaus.aspectwerkz.expression.ast.ASTStaticInitialization;
import org.codehaus.aspectwerkz.expression.ast.ASTWithin;
import org.codehaus.aspectwerkz.expression.ast.ASTWithinCode;
import org.codehaus.aspectwerkz.expression.ast.Node;

/**
 * The advised cflow class filter visitor.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class AdvisedCflowClassFilterExpressionVisitor extends AdvisedClassFilterExpressionVisitor {
    /**
     * Creates a new cflow expression.
     *
     * @param expression the expression as a string
     * @param namespace  the namespace
     * @param root       the AST root
     */
    public AdvisedCflowClassFilterExpressionVisitor(final String expression, final String namespace, final ASTRoot root) {
        super(expression, namespace, root);
    }

    /**
     * Matches the expression context.
     *
     * @param context
     * @return
     */
    public boolean match(final ExpressionContext context) {
        boolean match = ((Boolean)visit(m_root, context)).booleanValue();

        if (context.hasBeenVisitingCflow()) {
            return match;
        } else {
            return false;
        }
    }

    public Object visit(ASTCflow node, Object data) {
        ExpressionContext context = (ExpressionContext)data;

        context.setHasBeenVisitingCflow(true);
        context.setInCflowSubAST(true);

        Node child = node.jjtGetChild(0);
        Object result;

        // if 'call' or 'handler' but no 'within*' then return true
        if (child instanceof ASTCall || child instanceof ASTHandler) {
            result = Boolean.TRUE;
        } else {
            result = child.jjtAccept(this, context);
        }

        context.setInCflowSubAST(false);

        return result;
    }

    public Object visit(ASTCflowBelow node, Object data) {
        ExpressionContext context = (ExpressionContext)data;

        context.setHasBeenVisitingCflow(true);
        context.setInCflowSubAST(true);

        Node child = node.jjtGetChild(0);
        Object result;

        // if 'call' or 'handler' but no 'within*' then return true
        if (child instanceof ASTCall || child instanceof ASTHandler) {
            result = Boolean.TRUE;
        } else {
            result = child.jjtAccept(this, context);
        }

        context.setInCflowSubAST(false);

        return result;
    }

    public Object visit(ASTPointcutReference node, Object data) {
        ExpressionContext context = (ExpressionContext)data;
        ExpressionNamespace namespace = ExpressionNamespace.getNamespace(m_namespace);

        return Boolean.valueOf(namespace.getAdvisedCflowClassExpression(node.getName()).match(context));
    }

    public Object visit(ASTExecution node, Object data) {
        ExpressionContext context = (ExpressionContext)data;

        if (context.inCflowSubAST()) {
            return super.visit(node, data);
        } else {
            return Boolean.FALSE;
        }
    }

    public Object visit(ASTCall node, Object data) {
        ExpressionContext context = (ExpressionContext)data;

        if (context.inCflowSubAST()) {
            return super.visit(node, data);
        } else {
            return Boolean.FALSE;
        }
    }

    public Object visit(ASTSet node, Object data) {
        ExpressionContext context = (ExpressionContext)data;

        if (context.inCflowSubAST()) {
            return super.visit(node, data);
        } else {
            return Boolean.FALSE;
        }
    }

    public Object visit(ASTGet node, Object data) {
        ExpressionContext context = (ExpressionContext)data;

        if (context.inCflowSubAST()) {
            return super.visit(node, data);
        } else {
            return Boolean.FALSE;
        }
    }

    public Object visit(ASTHandler node, Object data) {
        ExpressionContext context = (ExpressionContext)data;

        if (context.inCflowSubAST()) {
            return super.visit(node, data);
        } else {
            return Boolean.FALSE;
        }
    }

    public Object visit(ASTStaticInitialization node, Object data) {
        ExpressionContext context = (ExpressionContext)data;

        if (context.inCflowSubAST()) {
            return super.visit(node, data);
        } else {
            return Boolean.FALSE;
        }
    }

    public Object visit(ASTWithin node, Object data) {
        ExpressionContext context = (ExpressionContext)data;

        if (context.inCflowSubAST()) {
            return super.visit(node, data);
        } else {
            return Boolean.FALSE;
        }
    }

    public Object visit(ASTWithinCode node, Object data) {
        ExpressionContext context = (ExpressionContext)data;

        if (context.inCflowSubAST()) {
            return super.visit(node, data);
        } else {
            return Boolean.FALSE;
        }
    }
}
