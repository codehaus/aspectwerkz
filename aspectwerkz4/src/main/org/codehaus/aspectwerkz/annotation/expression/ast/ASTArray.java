/* Generated By:JJTree: Do not edit this line. ASTArray.java */
package org.codehaus.aspectwerkz.annotation.expression.ast;

public class ASTArray extends SimpleNode {
    public ASTArray(int id) {
        super(id);
    }

    public ASTArray(AnnotationParser p, int id) {
        super(p, id);
    }

    /**
     * Accept the visitor. *
     */
    public Object jjtAccept(AnnotationParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}