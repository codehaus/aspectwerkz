package org.codehaus.aspectwerkz.expression.ast;

import org.codehaus.aspectwerkz.expression.regexp.NamePattern;
import org.codehaus.aspectwerkz.expression.regexp.Pattern;
import org.codehaus.aspectwerkz.expression.regexp.TypePattern;

public class ASTFieldPattern extends SimpleNode {
    private TypePattern m_fieldTypePattern;
    private TypePattern m_declaringTypePattern;
    private NamePattern m_fieldNamePattern;

    public ASTFieldPattern(int id) {
        super(id);
    }

    public ASTFieldPattern(ExpressionParser p, int id) {
        super(p, id);
    }

    public Object jjtAccept(ExpressionParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }

    public void setFieldTypePattern(String pattern) {
        boolean hierarchical = false;

        if (pattern.endsWith("+")) {
            hierarchical = true;
            pattern = pattern.substring(0, pattern.length() - 1);
        }

        m_fieldTypePattern = Pattern.compileTypePattern(pattern, hierarchical);
    }

    public void setFullNamePattern(final String pattern) {
        int index = pattern.lastIndexOf('.');
        String classPattern = pattern.substring(0, index);
        boolean hierarchical = false;

        if (classPattern.endsWith("+")) {
            hierarchical = true;
            classPattern = classPattern.substring(0, classPattern.length() - 1);
        }

        m_declaringTypePattern = Pattern.compileTypePattern(classPattern, hierarchical);

        String namePattern = pattern.substring(index + 1, pattern.length());

        m_fieldNamePattern = Pattern.compileNamePattern(namePattern);
    }

    public TypePattern getFieldTypePattern() {
        return m_fieldTypePattern;
    }

    public TypePattern getDeclaringTypePattern() {
        return m_declaringTypePattern;
    }

    public NamePattern getFieldNamePattern() {
        return m_fieldNamePattern;
    }
}
