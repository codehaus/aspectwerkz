/**************************************************************************************
 * Copyright (c) Jonas Bon?r, Alexandre Vasseur. All rights reserved.                 *
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
import org.codehaus.aspectwerkz.expression.ast.ASTNot;
import org.codehaus.aspectwerkz.expression.ast.ASTPointcutReference;
import org.codehaus.aspectwerkz.expression.ast.ASTRoot;
import org.codehaus.aspectwerkz.expression.ast.ASTSet;
import org.codehaus.aspectwerkz.expression.ast.ASTStaticInitialization;
import org.codehaus.aspectwerkz.expression.ast.ASTWithin;
import org.codehaus.aspectwerkz.expression.ast.ASTWithinCode;
import org.codehaus.aspectwerkz.expression.ast.ASTArgs;
import org.codehaus.aspectwerkz.expression.ast.ASTArgParameter;
import org.codehaus.aspectwerkz.expression.ast.ASTHasField;
import org.codehaus.aspectwerkz.expression.ast.ASTHasMethod;

/**
 * The Cflow expression visitor used at runtime. <p/>This visitor does a parse on a compsosite context, based on the
 * gathered cflow related context AND the joinpoint context. <p/>This allow to parse complex cflow expression like "(pc1
 * AND cf1 AND cf3) OR (pc2 AND cf2)".
 * 
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur </a>
 * @author Michael Nascimento
 */
public class CflowExpressionVisitorRuntime extends ExpressionVisitor {
    /**
     * Creates a new cflow runtime visitor.
     * 
     * @param expression the expression as a string
     * @param namespace the namespace
     * @param root the AST root
     */
    public CflowExpressionVisitorRuntime(final ExpressionInfo expressionInfo,
                                         final String expression,
                                         final String namespace,
                                         final ASTRoot root) {
        super(expressionInfo, expression, namespace, root);
    }

    /**
     * Matches the cflow information stack.
     * 
     * @param contexts the cflow gathered contexts
     * @param jpContext the joinpoint context
     * @return true if parse
     */
    public boolean matchCflowStack(final Object[] contexts, final ExpressionContext jpContext) {
        CompositeContext compositeContext = new CompositeContext();
        ExpressionContext[] ctxs = new ExpressionContext[contexts.length];
        for (int i = 0; i < ctxs.length; i++) {
            ctxs[i] = (ExpressionContext) contexts[i];
        }
        compositeContext.cflowContexts = ctxs;
        compositeContext.expressionContext = jpContext;
        return matchCflowStack(compositeContext);
    }

    public Object visit(ASTNot node, Object data) {
        Boolean match = (Boolean) node.jjtGetChild(0).jjtAccept(this, data);
        if (match.equals(Boolean.TRUE)) {
            return Boolean.FALSE;
        } else {
            return Boolean.TRUE;
        }
    }

    public Object visit(ASTPointcutReference node, Object data) {
        CompositeContext context = (CompositeContext) data;
        ExpressionNamespace namespace = ExpressionNamespace.getNamespace(m_namespace);
        CflowExpressionVisitorRuntime expression = namespace.getCflowExpressionRuntime(node.getName());
        return new Boolean(expression.matchCflowStack(context));
    }

    public Object visit(ASTExecution node, Object data) {
        return super.visit(node, ((CompositeContext) data).getLocalContext());
    }

    public Object visit(ASTCall node, Object data) {
        return super.visit(node, ((CompositeContext) data).getLocalContext());
    }

    public Object visit(ASTSet node, Object data) {
        return super.visit(node, ((CompositeContext) data).getLocalContext());
    }

    public Object visit(ASTGet node, Object data) {
        return super.visit(node, ((CompositeContext) data).getLocalContext());
    }

    public Object visit(ASTHandler node, Object data) {
        return super.visit(node, ((CompositeContext) data).getLocalContext());
    }

    public Object visit(ASTWithin node, Object data) {
        return super.visit(node, ((CompositeContext) data).getLocalContext());
    }

    public Object visit(ASTWithinCode node, Object data) {
        return super.visit(node, ((CompositeContext) data).getLocalContext());
    }

    public Object visit(ASTStaticInitialization node, Object data) {
        return super.visit(node, ((CompositeContext) data).getLocalContext());
    }

    public Object visit(ASTArgs node, Object data) {
        return super.visit(node, ((CompositeContext) data).getLocalContext());
    }

    public Object visit(ASTHasMethod node, Object data) {
        return super.visit(node, ((CompositeContext) data).getLocalContext());
    }

    public Object visit(ASTHasField node, Object data) {
        return super.visit(node, ((CompositeContext) data).getLocalContext());
    }

    public Object visit(ASTCflow node, Object data) {
        CompositeContext compositeContext = (CompositeContext) data;
        try {
            for (int i = 0; i < compositeContext.cflowContexts.length; i++) {
                compositeContext.localContext = compositeContext.cflowContexts[i];
                Boolean match = (Boolean) node.jjtGetChild(0).jjtAccept(this, data);
                if (match.booleanValue()) {
                    return Boolean.TRUE;
                }
            }
            return Boolean.FALSE;
        } finally {
            compositeContext.localContext = null;
        }
    }

    public Object visit(ASTCflowBelow node, Object data) {
        CompositeContext compositeContext = (CompositeContext) data;
        try {
            for (int i = 0; i < compositeContext.cflowContexts.length; i++) {
                compositeContext.localContext = compositeContext.cflowContexts[i];
                Boolean match = (Boolean) node.jjtGetChild(0).jjtAccept(this, data);
                if (match.booleanValue()) {
                    return Boolean.TRUE;
                }
            }
            return Boolean.FALSE;
        } finally {
            compositeContext.localContext = null;
        }
    }

    /**
     * Matches the cflow information stack.
     * 
     * @param compositeContext the composite context
     * @return true if parse
     */
    private boolean matchCflowStack(final CompositeContext compositeContext) {
        return ((Boolean) visit(m_root, compositeContext)).booleanValue();
    }

    // --- Pattern matching is delegated to regular ExpressionVisitor thru the
    // compositeContext.localContext

    /**
     * A composite context for use in cflow evaluation at runtime.
     * 
     * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur </a>
     */
    static class CompositeContext {
        public ExpressionContext expressionContext;

        public ExpressionContext[] cflowContexts;

        public ExpressionContext localContext;

        /**
         * The actual local context is the dependent on where we are in the tree. <p/>Local context is the join point
         * context - when outside of cflow subtree. - else it is one of the cflow contexts that we iterate over.
         * 
         * @return the expression
         */
        public ExpressionContext getLocalContext() {
            return (localContext == null) ? expressionContext : localContext;
        }
    }
}