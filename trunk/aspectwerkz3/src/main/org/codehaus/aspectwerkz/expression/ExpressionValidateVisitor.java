/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.expression;

import org.codehaus.aspectwerkz.annotation.AnnotationInfo;
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
import org.codehaus.aspectwerkz.expression.ast.ASTArgs;
import org.codehaus.aspectwerkz.expression.ast.ASTArgParameter;
import org.codehaus.aspectwerkz.expression.regexp.TypePattern;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.ConstructorInfo;
import org.codehaus.aspectwerkz.reflect.FieldInfo;
import org.codehaus.aspectwerkz.reflect.MemberInfo;
import org.codehaus.aspectwerkz.reflect.MethodInfo;
import org.codehaus.aspectwerkz.reflect.ReflectionInfo;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.codehaus.aspectwerkz.expression.ast.ASTHasField;
import org.codehaus.aspectwerkz.expression.ast.ASTHasMethod;
import org.codehaus.aspectwerkz.expression.ast.ASTTarget;
import org.codehaus.aspectwerkz.expression.ast.ASTThis;
import org.codehaus.aspectwerkz.util.Strings;

/**
 * The visitor that extract all possible arguments referenced by the expression.
 *
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur </a>
 */
public class ExpressionValidateVisitor implements ExpressionParserVisitor {

    protected ASTRoot m_root;
    protected String m_expression;
    protected String m_namespace;

    /**
     * Creates a new expression.
     *
     * @param expression the expression as a string
     * @param namespace  the namespace
     * @param root       the AST root
     */
    public ExpressionValidateVisitor(final String expression,
                                     final String namespace,
                                     final ASTRoot root) {
        m_expression = expression;
        m_namespace = namespace;
        m_root = root;
    }

    /**
     * Populate data with the possible arguments
     *
     * @param data a list to feed with Strings
     */
    public void populate(List data) {
        visit(m_root, data);
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
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            List args = (List) node.jjtGetChild(i).jjtAccept(this, data);
            //((List) data).addAll(args);
        }
        return data;
    }

    public Object visit(ASTAnd node, Object data) {
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).jjtAccept(this, data);
        }
        return data;
    }

    public Object visit(ASTNot node, Object data) {
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).jjtAccept(this, data);
        }
        return data;
    }

    // ============ Pointcut types =============
    public Object visit(ASTPointcutReference node, Object data) {
        // visit the args - if any
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).jjtAccept(this, data);
        }
        return data;
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

    public Object visit(ASTStaticInitialization node, Object data) {
        return data;
    }

    public Object visit(ASTWithin node, Object data) {
        return data;
    }

    public Object visit(ASTWithinCode node, Object data) {
        return data;
    }


    public Object visit(ASTHasMethod node, Object data) {
        return data;
    }

    public Object visit(ASTHasField node, Object data) {
        return data;
    }

    public Object visit(ASTCflow node, Object data) {
        return data;
    }

    public Object visit(ASTCflowBelow node, Object data) {
        return data;
    }

    public Object visit(ASTTarget node, Object data) {
        ((List) data).add(node.getIdentifier());
        return data;
    }

    public Object visit(ASTThis node, Object data) {
        ((List) data).add(node.getIdentifier());
        return data;
    }

    // ============ Patterns =============
    public Object visit(ASTClassPattern node, Object data) {
        return data;
    }

    public Object visit(ASTMethodPattern node, Object data) {
        return data;
    }

    public Object visit(ASTConstructorPattern node, Object data) {
        return data;
    }

    public Object visit(ASTFieldPattern node, Object data) {
        return data;
    }

    public Object visit(ASTParameter node, Object data) {
        return data;
    }

    public Object visit(ASTArgs node, Object data) {
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            List args = (List) node.jjtGetChild(i).jjtAccept(this, data);
            ((List) data).addAll(args);
        }
        return data;
    }

    public Object visit(ASTArgParameter node, Object data) {
        TypePattern typePattern = node.getTypePattern();
        ((List) data).add(typePattern.getPattern());
        return data;
    }

    public Object visit(ASTAttribute node, Object data) {
        return data;
    }

    public Object visit(ASTModifier node, Object data) {
        return data;
    }

    /**
     * Returns the string representation of the expression.
     *
     * @return
     */
    public String toString() {
        return m_expression;
    }

}