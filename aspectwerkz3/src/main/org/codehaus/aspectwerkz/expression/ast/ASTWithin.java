/* Generated By:JJTree: Do not edit this line. ASTWithin.java */

package org.codehaus.aspectwerkz.expression.ast;

public class ASTWithin extends SimpleNode {
    public ASTWithin(int id) {
        super(id);
    }

    public ASTWithin(ExpressionParser p, int id) {
        super(p, id);
    }

    /** Accept the visitor. * */
    public Object jjtAccept(ExpressionParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}