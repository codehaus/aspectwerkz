package org.codehaus.aspectwerkz.expression.ast;

import org.codehaus.aspectwerkz.expression.regexp.Pattern;
import org.codehaus.aspectwerkz.expression.regexp.TypePattern;

public class ASTParameter extends SimpleNode {
    private TypePattern m_declaringClassPattern;

    public ASTParameter(int id) {
        super(id);
    }

    public ASTParameter(ExpressionParser p, int id) {
        super(p, id);
    }

    public Object jjtAccept(ExpressionParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }

    public void setTypePattern(String pattern) {
        boolean hierarchical = false;
        if (pattern.endsWith("+")) {
            hierarchical = true;
            pattern = pattern.substring(0, pattern.length() - 1);
        }
        m_declaringClassPattern = Pattern.compileTypePattern(pattern, hierarchical);
    }

    public TypePattern getDeclaringClassPattern() {
        return m_declaringClassPattern;
    }
}
