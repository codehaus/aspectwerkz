/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
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
import org.codehaus.aspectwerkz.expression.ast.ASTHasField;
import org.codehaus.aspectwerkz.expression.ast.ASTHasMethod;
import org.codehaus.aspectwerkz.expression.ast.ASTPointcutReference;
import org.codehaus.aspectwerkz.expression.ast.ASTRoot;
import org.codehaus.aspectwerkz.expression.ast.ASTSet;
import org.codehaus.aspectwerkz.expression.ast.ASTStaticInitialization;
import org.codehaus.aspectwerkz.expression.ast.ASTWithin;
import org.codehaus.aspectwerkz.expression.ast.ASTWithinCode;
import org.codehaus.aspectwerkz.expression.ast.Node;
import org.codehaus.aspectwerkz.expression.ast.ASTNot;

/**
 * The advised cflow class filter visitor.
 * If the expression does not contains any cflow, it returns FALSE.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r </a>
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 * @author Michael Nascimento
 */
public class AdvisedCflowClassFilterExpressionVisitor extends AdvisedClassFilterExpressionVisitor {

    /**
     * Creates a new cflow expression.
     *
     * @param expression the expression as a string
     * @param namespace  the namespace
     * @param root       the AST root
     */
    public AdvisedCflowClassFilterExpressionVisitor(final ExpressionInfo expressionInfo, final String expression,
                                                    final String namespace, final ASTRoot root) {
        super(expressionInfo, expression, namespace, root);
    }

    /**
     * Matches the expression context.
     *
     * @param context
     * @return
     */
    public boolean match(final ExpressionContext context) {
        if (!m_expressionInfo.hasCflowPointcut()) {
            return false;
        }
        Boolean match = ((Boolean) visit(m_root, context));
        if (context.hasBeenVisitingCflow()) {
            // undeterministic is assumed to be "true" at this stage
            // since it won't be composed anymore with a NOT (unless
            // thru pointcut reference ie a new visitor)
            return (match != null) ? match.booleanValue() : true;
        } else {
            return true;
        }
    }

    public Object visit(ASTNot node, Object data) {
        ExpressionContext context = (ExpressionContext) data;
        if (context.inCflowSubAST()) {
            return super.visit(node, data);
        } else {
            // ignore the NOT
            return Boolean.TRUE;
        }
    }


    public Object visit(ASTCflow node, Object data) {
        ExpressionContext context = (ExpressionContext) data;
        context.setHasBeenVisitingCflow(true);
        context.setInCflowSubAST(true);
        Node child = node.jjtGetChild(0);
        Object result;

//        // if 'call' or 'handler' but no 'within*' then return true
//        if (child instanceof ASTCall || child instanceof ASTHandler) {
//            result = Boolean.TRUE;
//        } else {
//            result = child.jjtAccept(this, context);
//        }
        result = child.jjtAccept(this, context);
        context.setInCflowSubAST(false);
        return result;
    }

    public Object visit(ASTCflowBelow node, Object data) {
        ExpressionContext context = (ExpressionContext) data;
        context.setHasBeenVisitingCflow(true);
        context.setInCflowSubAST(true);
        Node child = node.jjtGetChild(0);
        Object result;

//        // if 'call' or 'handler' but no 'within*' then return true
//        if (child instanceof ASTCall || child instanceof ASTHandler) {
//            result = Boolean.TRUE;
//        } else {
//            result = child.jjtAccept(this, context);
//        }
        result = child.jjtAccept(this, context);
        context.setInCflowSubAST(false);
        return result;
    }

    public Object visit(ASTPointcutReference node, Object data) {
        ExpressionContext context = (ExpressionContext) data;
        ExpressionNamespace namespace = ExpressionNamespace.getNamespace(m_namespace);
        AdvisedCflowClassFilterExpressionVisitor reference = namespace.getAdvisedCflowClassExpression(node.getName());
        if (!reference.m_expressionInfo.hasCflowPointcut()) {
            // ignore sub expression without cflow(...) expressions
            return Boolean.TRUE;
        }
        return new Boolean(reference.match(context));
    }

    public Object visit(ASTExecution node, Object data) {
        ExpressionContext context = (ExpressionContext) data;
        if (context.inCflowSubAST()) {
            return super.visit(node, data);
        } else {
            return Boolean.TRUE;
        }
    }

    public Object visit(ASTCall node, Object data) {
        ExpressionContext context = (ExpressionContext) data;
        if (context.inCflowSubAST()) {
            return super.visit(node, data);
        } else {
            return Boolean.TRUE;
        }
    }

    public Object visit(ASTSet node, Object data) {
        ExpressionContext context = (ExpressionContext) data;
        if (context.inCflowSubAST()) {
            return super.visit(node, data);
        } else {
            return Boolean.TRUE;
        }
    }

    public Object visit(ASTGet node, Object data) {
        ExpressionContext context = (ExpressionContext) data;
        if (context.inCflowSubAST()) {
            return super.visit(node, data);
        } else {
            return Boolean.TRUE;
        }
    }

    public Object visit(ASTHandler node, Object data) {
        ExpressionContext context = (ExpressionContext) data;
        if (context.inCflowSubAST()) {
            return super.visit(node, data);
        } else {
            return Boolean.TRUE;
        }
    }

    public Object visit(ASTStaticInitialization node, Object data) {
        ExpressionContext context = (ExpressionContext) data;
        if (context.inCflowSubAST()) {
            return super.visit(node, data);
        } else {
            return Boolean.TRUE;
        }
    }

    public Object visit(ASTWithin node, Object data) {
        ExpressionContext context = (ExpressionContext) data;
        if (context.inCflowSubAST()) {
            return super.visit(node, data);
        } else {
            return Boolean.TRUE;
        }
    }

    public Object visit(ASTWithinCode node, Object data) {
        ExpressionContext context = (ExpressionContext) data;
        if (context.inCflowSubAST()) {
            return super.visit(node, data);
        } else {
            return Boolean.TRUE;
        }
    }

    public Object visit(ASTHasMethod node, Object data) {
        ExpressionContext context = (ExpressionContext) data;
        if (context.inCflowSubAST()) {
            return super.visit(node, data);
        } else {
            return Boolean.TRUE;
        }
    }

    public Object visit(ASTHasField node, Object data) {
        ExpressionContext context = (ExpressionContext) data;
        if (context.inCflowSubAST()) {
            return super.visit(node, data);
        } else {
            return Boolean.TRUE;
        }
    }
}