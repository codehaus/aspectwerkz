/* Generated By:JJTree: Do not edit this line. InNode.java */
package org.codehaus.aspectwerkz.definition.expression.ast;

public class InNode extends SimpleNode
{
    public InNode(int id)
    {
        super(id);
    }

    public InNode(ExpressionParser p, int id)
    {
        super(p, id);
    }

    /**
     * Accept the visitor. *
     */
    public Object jjtAccept(ExpressionParserVisitor visitor, Object data)
    {
        return visitor.visit(this, data);
    }
}
