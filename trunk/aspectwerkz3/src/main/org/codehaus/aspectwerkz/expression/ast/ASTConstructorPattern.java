package org.codehaus.aspectwerkz.expression.ast;

import org.codehaus.aspectwerkz.expression.regexp.Pattern;
import org.codehaus.aspectwerkz.expression.regexp.TypePattern;

public class ASTConstructorPattern extends SimpleNode {
    private TypePattern m_declaringTypePattern;

    public ASTConstructorPattern(int id) {
        super(id);
    }

    public ASTConstructorPattern(ExpressionParser p, int id) {
        super(p, id);
    }

    public Object jjtAccept(ExpressionParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }

    public void setFullNamePattern(String pattern) {
        int index = pattern.lastIndexOf('.');
        pattern = pattern.substring(0, index);
        boolean hierarchical = false;
        if (pattern.endsWith("+")) {
            hierarchical = true;
            pattern = pattern.substring(0, pattern.length() - 1);
        }
        m_declaringTypePattern = Pattern.compileTypePattern(pattern, hierarchical);
    }

    public TypePattern getDeclaringTypePattern() {
        return m_declaringTypePattern;
    }
}
