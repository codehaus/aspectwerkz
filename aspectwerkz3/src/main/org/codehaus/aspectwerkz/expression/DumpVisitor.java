/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
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
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public class DumpVisitor implements ExpressionParserVisitor {
    private ASTRoot m_root;
    private int indent = 0;

    private DumpVisitor(final ASTRoot root) {
        m_root = root;
    }

    public static void dumpAST(final ASTRoot root) {
        DumpVisitor dumper = new DumpVisitor(root);

        dumper.visit(dumper.m_root, null);
    }

    public Object visit(SimpleNode node, Object data) {
        System.out.println(indentString() + node);
        ++indent;
        data = node.jjtGetChild(0).jjtAccept(this, data);
        --indent;

        return data;
    }

    public Object visit(ASTRoot node, Object data) {
        System.out.println(indentString() + node);
        ++indent;
        data = node.jjtGetChild(0).jjtAccept(this, data);
        --indent;

        return data;
    }

    public Object visit(ASTExpression node, Object data) {
        System.out.println(indentString() + node);
        ++indent;
        data = node.jjtGetChild(0).jjtAccept(this, data);
        --indent;

        return data;
    }

    public Object visit(ASTOr node, Object data) {
        System.out.println(indentString() + node);
        ++indent;
        data = node.jjtGetChild(0).jjtAccept(this, data);
        data = (Boolean)node.jjtGetChild(1).jjtAccept(this, data);
        --indent;

        return data;
    }

    public Object visit(ASTAnd node, Object data) {
        System.out.println(indentString() + node);
        ++indent;
        data = node.jjtGetChild(0).jjtAccept(this, data);
        data = (Boolean)node.jjtGetChild(1).jjtAccept(this, data);
        --indent;

        return data;
    }

    public Object visit(ASTNot node, Object data) {
        System.out.println(indentString() + node);
        ++indent;
        data = node.jjtGetChild(0).jjtAccept(this, data);
        --indent;

        return data;
    }

    public Object visit(ASTExecution node, Object data) {
        System.out.println(indentString() + node);
        ++indent;
        data = node.jjtGetChild(0).jjtAccept(this, data);
        --indent;

        return data;
    }

    public Object visit(ASTCall node, Object data) {
        System.out.println(indentString() + node);
        ++indent;
        data = node.jjtGetChild(0).jjtAccept(this, data);
        --indent;

        return data;
    }

    public Object visit(ASTSet node, Object data) {
        System.out.println(indentString() + node);
        ++indent;
        data = node.jjtGetChild(0).jjtAccept(this, data);
        --indent;

        return data;
    }

    public Object visit(ASTGet node, Object data) {
        System.out.println(indentString() + node);
        ++indent;
        data = node.jjtGetChild(0).jjtAccept(this, data);
        --indent;

        return data;
    }

    public Object visit(ASTHandler node, Object data) {
        System.out.println(indentString() + node);
        ++indent;
        data = node.jjtGetChild(0).jjtAccept(this, data);
        --indent;

        return data;
    }

    public Object visit(ASTWithin node, Object data) {
        System.out.println(indentString() + node);
        ++indent;
        data = node.jjtGetChild(0).jjtAccept(this, data);
        --indent;

        return data;
    }

    public Object visit(ASTWithinCode node, Object data) {
        System.out.println(indentString() + node);
        ++indent;
        data = node.jjtGetChild(0).jjtAccept(this, data);
        --indent;

        return data;
    }

    public Object visit(ASTStaticInitialization node, Object data) {
        System.out.println(indentString() + node);
        ++indent;
        data = node.jjtGetChild(0).jjtAccept(this, data);
        --indent;

        return data;
    }

    public Object visit(ASTCflow node, Object data) {
        System.out.println(indentString() + node);
        ++indent;
        data = node.jjtGetChild(0).jjtAccept(this, data);
        --indent;

        return data;
    }

    public Object visit(ASTCflowBelow node, Object data) {
        System.out.println(indentString() + node);
        ++indent;
        data = node.jjtGetChild(0).jjtAccept(this, data);
        --indent;

        return data;
    }

    public Object visit(ASTClassPattern node, Object data) {
        System.out.println(indentString() + node);
        ++indent;

        int nr = node.jjtGetNumChildren();

        for (int i = 0; i < nr; i++) {
            data = node.jjtGetChild(i).jjtAccept(this, data);
        }

        --indent;

        return data;
    }

    public Object visit(ASTMethodPattern node, Object data) {
        System.out.println(indentString() + node);
        ++indent;

        int nr = node.jjtGetNumChildren();

        for (int i = 0; i < nr; i++) {
            data = node.jjtGetChild(i).jjtAccept(this, data);
        }

        --indent;

        return data;
    }

    public Object visit(ASTConstructorPattern node, Object data) {
        System.out.println(indentString() + node);
        ++indent;

        int nr = node.jjtGetNumChildren();

        for (int i = 0; i < nr; i++) {
            data = node.jjtGetChild(i).jjtAccept(this, data);
        }

        --indent;

        return data;
    }

    public Object visit(ASTFieldPattern node, Object data) {
        System.out.println(indentString() + node);
        ++indent;

        int nr = node.jjtGetNumChildren();

        for (int i = 0; i < nr; i++) {
            data = node.jjtGetChild(i).jjtAccept(this, data);
        }

        --indent;

        return data;
    }

    public Object visit(ASTPointcutReference node, Object data) {
        System.out.println(indentString() + node);

        return data;
    }

    public Object visit(ASTParameter node, Object data) {
        System.out.println(indentString() + node);

        return data;
    }

    public Object visit(ASTAttribute node, Object data) {
        System.out.println(indentString() + node);

        return data;
    }

    public Object visit(ASTModifier node, Object data) {
        System.out.println(indentString() + node);

        return data;
    }

    private String indentString() {
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < indent; ++i) {
            sb.append(" ");
        }

        return sb.toString();
    }
}
