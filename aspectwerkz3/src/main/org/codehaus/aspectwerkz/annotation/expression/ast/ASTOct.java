/* Generated By:JJTree: Do not edit this line. ASTOct.java */
package org.codehaus.aspectwerkz.annotation.expression.ast;

public class ASTOct extends SimpleNode {
    private String m_value;

    public ASTOct(int id) {
        super(id);
    }

    public ASTOct(AnnotationParser p, int id) {
        super(p, id);
    }

    public Object jjtAccept(AnnotationParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }

    public String getValue() {
        return m_value;
    }

    public void setValue(String value) {
        m_value = value;
    }
}
