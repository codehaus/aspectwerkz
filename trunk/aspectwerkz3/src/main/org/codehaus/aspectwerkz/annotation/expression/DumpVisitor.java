/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.annotation.expression;

import org.codehaus.aspectwerkz.annotation.expression.ast.*;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
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
        int nr = node.jjtGetNumChildren();
        for (int i = 0; i < nr; i++) {
            data = node.jjtGetChild(i).jjtAccept(this, data);
        }
        --indent;
        return data;
    }

    public Object visit(ASTRoot node, Object data) {
        System.out.println(indentString() + node);
        ++indent;
        int nr = node.jjtGetNumChildren();
        for (int i = 0; i < nr; i++) {
            data = node.jjtGetChild(i).jjtAccept(this, data);
        }
        --indent;
        return data;
    }

    public Object visit(ASTAnnotation node, Object data) {
        System.out.println(indentString() + node);
        ++indent;
        int nr = node.jjtGetNumChildren();
        for (int i = 0; i < nr; i++) {
            data = node.jjtGetChild(i).jjtAccept(this, data);
        }
        --indent;
        return data;
    }

    public Object visit(ASTKeyValuePair node, Object data) {
        System.out.println(indentString() + node);
        ++indent;
        int nr = node.jjtGetNumChildren();
        for (int i = 0; i < nr; i++) {
            data = node.jjtGetChild(i).jjtAccept(this, data);
        }
        --indent;
        return data;
    }

    public Object visit(ASTArray node, Object data) {
        System.out.println(indentString() + node);
        ++indent;
        int nr = node.jjtGetNumChildren();
        for (int i = 0; i < nr; i++) {
            data = node.jjtGetChild(i).jjtAccept(this, data);
        }
        --indent;
        return data;
    }

    public Object visit(ASTIdentifier node, Object data) {
        System.out.println(indentString() + node);
        return data;
    }

    public Object visit(ASTBoolean node, Object data) {
        System.out.println(indentString() + node);
        return data;
    }

    public Object visit(ASTChar node, Object data) {
        System.out.println(indentString() + node);
        return data;
    }

    public Object visit(ASTString node, Object data) {
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
