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
import org.codehaus.aspectwerkz.expression.ast.Node;
import org.codehaus.aspectwerkz.expression.ast.SimpleNode;
import org.codehaus.aspectwerkz.expression.regexp.TypePattern;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.ClassInfoHelper;
import org.codehaus.aspectwerkz.reflect.MemberInfo;
import org.codehaus.aspectwerkz.reflect.ReflectionInfo;

/**
 * The advised class filter visitor.
 * 
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public class AdvisedClassFilterExpressionVisitor implements ExpressionParserVisitor {
    protected final ASTRoot m_root;

    protected final String m_expression;

    protected final String m_namespace;

    /**
     * Creates a new expression.
     * 
     * @param expression the expression as a string
     * @param namespace the namespace
     * @param root the AST root
     */
    public AdvisedClassFilterExpressionVisitor(final String expression,
                                               final String namespace,
                                               final ASTRoot root) {
        m_root = root;
        m_expression = expression;
        m_namespace = namespace;
    }

    /**
     * Matches the expression context.
     * 
     * @param context
     * @return
     */
    public boolean match(final ExpressionContext context) {
        return ((Boolean) visit(m_root, context)).booleanValue();
    }

    // ============ Boot strap =============
    public Object visit(SimpleNode node, Object data) {
        return node.jjtGetChild(0).jjtAccept(this, data);
    }

    public Object visit(ASTRoot node, Object data) {
        Node child = node.jjtGetChild(0);

        // if 'call' or 'handler' but no 'within*' then return true
        if (child instanceof ASTCall || child instanceof ASTHandler) {
            return Boolean.TRUE;
        }
        return child.jjtAccept(this, data);
    }

    public Object visit(ASTExpression node, Object data) {
        Node child = node.jjtGetChild(0);

        // if 'call' or 'handler' but no 'within*' then return true
        if (child instanceof ASTCall || child instanceof ASTHandler) {
            return Boolean.TRUE;
        }
        return child.jjtAccept(this, data);
    }

    // ============ Logical operators =============
    public Object visit(ASTOr node, Object data) {
        int nrOfChildren = node.jjtGetNumChildren();
        for (int i = 0; i < nrOfChildren; i++) {
            Boolean match = (Boolean) node.jjtGetChild(i).jjtAccept(this, data);
            if (match.equals(Boolean.TRUE)) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    public Object visit(ASTAnd node, Object data) {
        boolean hasCallOrHandlerPc = false;
        boolean hasWithinPc = false;
        boolean hasNotPc = false;
        int notPcIndex = -1;

        // handle 'call', with and without 'within*'
        int nrOfChildren = node.jjtGetNumChildren();
        for (int i = 0; i < nrOfChildren; i++) {
            Node child = node.jjtGetChild(i);
            if (child instanceof ASTNot) {
                hasNotPc = true;
                notPcIndex = i;
            } else if (child instanceof ASTCall || child instanceof ASTHandler) {
                hasCallOrHandlerPc = true;
            } else if (child instanceof ASTWithin || child instanceof ASTWithinCode) {
                hasWithinPc = true;
            }
        }

        // check the child of the 'not' node
        if (hasCallOrHandlerPc && hasNotPc) {
            Node childChild = node.jjtGetChild(notPcIndex).jjtGetChild(0);
            if (childChild instanceof ASTWithin || childChild instanceof ASTWithinCode) {
                if (Boolean.TRUE.equals(childChild.jjtAccept(this, data))) {
                    return Boolean.FALSE;
                } else {
                    return Boolean.TRUE;
                }
            }
        } else if (hasCallOrHandlerPc && !hasWithinPc) {
            return Boolean.TRUE;
        }

        // if not a 'call' or 'handler' pointcut
        for (int i = 0; i < nrOfChildren; i++) {
            Boolean match = (Boolean) node.jjtGetChild(i).jjtAccept(this, data);
            if (match.equals(Boolean.TRUE)) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    public Object visit(ASTNot node, Object data) {
        return (Boolean) node.jjtGetChild(0).jjtAccept(this, data);
    }

    // ============ Pointcut types =============
    public Object visit(ASTPointcutReference node, Object data) {
        ExpressionContext context = (ExpressionContext) data;
        ExpressionNamespace namespace = ExpressionNamespace.getNamespace(m_namespace);
        AdvisedClassFilterExpressionVisitor expression = namespace.getAdvisedClassExpression(node
                .getName());
        return new Boolean(expression.match(context));
    }

    public Object visit(ASTExecution node, Object data) {
        ExpressionContext context = (ExpressionContext) data;
        if (context.hasAnyPointcut() || context.hasExecutionPointcut()) {
            return node.jjtGetChild(0).jjtAccept(this, context.getReflectionInfo());
        } else {
            return Boolean.FALSE;
        }
    }

    public Object visit(ASTCall node, Object data) {
        return Boolean.FALSE;
    }

    public Object visit(ASTSet node, Object data) {
        ExpressionContext context = (ExpressionContext) data;
        if (context.hasAnyPointcut() || context.hasSetPointcut()) {
            return node.jjtGetChild(0).jjtAccept(this, context.getReflectionInfo());
        } else {
            return Boolean.FALSE;
        }
    }

    public Object visit(ASTGet node, Object data) {
        ExpressionContext context = (ExpressionContext) data;
        if (context.hasAnyPointcut() || context.hasGetPointcut()) {
            return node.jjtGetChild(0).jjtAccept(this, context.getReflectionInfo());
        } else {
            return Boolean.FALSE;
        }
    }

    public Object visit(ASTHandler node, Object data) {
        return Boolean.FALSE;
    }

    public Object visit(ASTStaticInitialization node, Object data) {
        ExpressionContext context = (ExpressionContext) data;
        if (context.hasAnyPointcut() || context.hasStaticInitializationPointcut()) {
            return node.jjtGetChild(0).jjtAccept(this, context.getReflectionInfo());
        } else {
            return Boolean.FALSE;
        }
    }

    public Object visit(ASTWithin node, Object data) {
        ExpressionContext context = (ExpressionContext) data;
        ReflectionInfo reflectionInfo = context.getReflectionInfo();
        if (reflectionInfo instanceof MemberInfo) {
            return node.jjtGetChild(0).jjtAccept(
                this,
                ((MemberInfo) reflectionInfo).getDeclaringType());
        } else if (reflectionInfo instanceof ClassInfo) {
            return node.jjtGetChild(0).jjtAccept(this, reflectionInfo);
        } else {
            return Boolean.FALSE;
        }
    }

    public Object visit(ASTWithinCode node, Object data) {
        ExpressionContext context = (ExpressionContext) data;
        return node.jjtGetChild(0).jjtAccept(this, context.getReflectionInfo());
    }

    public Object visit(ASTCflow node, Object data) {
        return Boolean.FALSE;
    }

    public Object visit(ASTCflowBelow node, Object data) {
        return Boolean.FALSE;
    }

    // ============ Patterns =============
    public Object visit(ASTClassPattern node, Object data) {
        ClassInfo classInfo = (ClassInfo) data;
        TypePattern typePattern = node.getTypePattern();
        if (ClassInfoHelper.matchType(typePattern, classInfo)) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    public Object visit(ASTMethodPattern node, Object data) {
        if (data instanceof ClassInfo) {
            ClassInfo classInfo = (ClassInfo) data;
            if (ClassInfoHelper.matchType(node.getDeclaringTypePattern(), classInfo)) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    public Object visit(ASTConstructorPattern node, Object data) {
        if (data instanceof ClassInfo) {
            ClassInfo classInfo = (ClassInfo) data;
            if (ClassInfoHelper.matchType(node.getDeclaringTypePattern(), classInfo)) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    public Object visit(ASTFieldPattern node, Object data) {
        if (data instanceof ClassInfo) {
            ClassInfo classInfo = (ClassInfo) data;
            if (ClassInfoHelper.matchType(node.getDeclaringTypePattern(), classInfo)) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    public Object visit(ASTParameter node, Object data) {
        ClassInfo parameterType = (ClassInfo) data;
        if (ClassInfoHelper.matchType(node.getDeclaringClassPattern(), parameterType)) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    public Object visit(ASTAttribute node, Object data) {
        return Boolean.TRUE;
    }

    public Object visit(ASTModifier node, Object data) {
        return Boolean.TRUE;
    }

    /**
     * Returns the string representation of the AST.
     * 
     * @return
     */
    public String toString() {
        return m_expression;
    }
}