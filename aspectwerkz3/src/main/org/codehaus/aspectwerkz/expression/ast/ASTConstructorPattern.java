package org.codehaus.aspectwerkz.expression.ast;

import org.codehaus.aspectwerkz.expression.regexp.Pattern;
import org.codehaus.aspectwerkz.expression.regexp.TypePattern;
import org.codehaus.aspectwerkz.expression.SubtypePatternType;

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
        if (pattern.endsWith("+")) {
            pattern = pattern.substring(0, pattern.length() - 1);
            m_declaringTypePattern = Pattern.compileTypePattern(pattern, SubtypePatternType.MATCH_ON_ALL_METHODS);
        } else if (pattern.endsWith("#")) {
            pattern = pattern.substring(0, pattern.length() - 1);
            m_declaringTypePattern = Pattern.compileTypePattern(pattern, SubtypePatternType.MATCH_ON_BASE_TYPE_METHODS);
        } else {
            m_declaringTypePattern = Pattern.compileTypePattern(pattern, SubtypePatternType.NOT_HIERARCHICAL);
        }
    }

    public TypePattern getDeclaringTypePattern() {
        return m_declaringTypePattern;
    }
}
