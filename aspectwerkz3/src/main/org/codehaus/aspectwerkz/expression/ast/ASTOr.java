/* Generated By:JJTree: Do not edit this line. ASTOr.java */
package org.codehaus.aspectwerkz.expression.ast;

public class ASTOr extends SimpleNode {
    public ASTOr(int id) {
        super(id);
    }

    public ASTOr(ExpressionParser p, int id) {
        super(p, id);
    }

    public Object jjtAccept(ExpressionParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}