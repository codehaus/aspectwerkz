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
public class AnnotationVisitor implements ExpressionParserVisitor {
    protected ASTRoot m_root;
    protected String m_annotation;
    protected String m_namespace;

    /**
     * Creates a new visitor.
     *
     * @param annotation the annotation as a string
     * @param namespace  the namespace
     * @param root       the AST root
     */
    public AnnotationVisitor(final String annotation, final String namespace, final ASTRoot root) {
        m_root = root;
        m_annotation = annotation;
        m_namespace = namespace;
    }

    //    public boolean match(final ExpressionContext context) {
    //        return ((Boolean)visit(m_root, context)).booleanValue();
    //    }
    public Object visit(SimpleNode node, Object data) {
        return node.jjtGetChild(0).jjtAccept(this, data);
    }

    public Object visit(ASTRoot node, Object data) {
        return node.jjtGetChild(0).jjtAccept(this, data);
    }

    public Object visit(ASTAnnotation node, Object data) {
        return node.jjtGetChild(0).jjtAccept(this, data);
    }

    public Object visit(ASTKeyValuePair node, Object data) {
        return node.jjtGetChild(0).jjtAccept(this, data);
    }

    public Object visit(ASTArray node, Object data) {
        return node.jjtGetChild(0).jjtAccept(this, data);
    }

    public Object visit(ASTIdentifier node, Object data) {
        return node.jjtGetChild(0).jjtAccept(this, data);
    }

    public Object visit(ASTBoolean node, Object data) {
        return node.jjtGetChild(0).jjtAccept(this, data);
    }

    public Object visit(ASTChar node, Object data) {
        return node.jjtGetChild(0).jjtAccept(this, data);
    }

    public Object visit(ASTString node, Object data) {
        return node.jjtGetChild(0).jjtAccept(this, data);
    }
}
