package org.codehaus.aspectwerkz.expression.ast;

import org.codehaus.aspectwerkz.expression.SubtypePatternType;
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
        String classPattern = null;
        //Aw-112 support for "new(..)"
        if (index > 0) {
            classPattern = pattern.substring(0, index);
        } else {
            // unspecified classPattern like "new(..)"
            classPattern = "*..*";
        }
        if (classPattern.endsWith("+")) {
            classPattern = classPattern.substring(0, classPattern.length() - 1);
            m_declaringTypePattern = Pattern.compileTypePattern(classPattern, SubtypePatternType.MATCH_ON_ALL_METHODS);
        } else if (classPattern.endsWith("#")) {
            classPattern = classPattern.substring(0, classPattern.length() - 1);
            m_declaringTypePattern = Pattern.compileTypePattern(
                classPattern,
                SubtypePatternType.MATCH_ON_BASE_TYPE_METHODS_ONLY);
        } else {
            m_declaringTypePattern = Pattern.compileTypePattern(classPattern, SubtypePatternType.NOT_HIERARCHICAL);
        }
    }

    public TypePattern getDeclaringTypePattern() {
        return m_declaringTypePattern;
    }
}