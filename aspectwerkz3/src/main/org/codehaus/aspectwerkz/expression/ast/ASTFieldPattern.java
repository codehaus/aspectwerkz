package org.codehaus.aspectwerkz.expression.ast;

import org.codehaus.aspectwerkz.expression.SubtypePatternType;
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
        if (pattern.endsWith("+")) {
            pattern = pattern.substring(0, pattern.length() - 1);
            m_fieldTypePattern = Pattern.compileTypePattern(
                pattern,
                SubtypePatternType.MATCH_ON_ALL_METHODS);
        } else if (pattern.endsWith("#")) {
            pattern = pattern.substring(0, pattern.length() - 1);
            m_fieldTypePattern = Pattern.compileTypePattern(
                pattern,
                SubtypePatternType.MATCH_ON_BASE_TYPE_METHODS_ONLY);
        } else {
            m_fieldTypePattern = Pattern.compileTypePattern(
                pattern,
                SubtypePatternType.NOT_HIERARCHICAL);
        }
    }

    public void setFullNamePattern(final String pattern) {
        int index = pattern.lastIndexOf('.');
        String classPattern = pattern.substring(0, index);
        if (classPattern.endsWith("+")) {
            classPattern = classPattern.substring(0, classPattern.length() - 1);
            m_declaringTypePattern = Pattern.compileTypePattern(
                classPattern,
                SubtypePatternType.MATCH_ON_ALL_METHODS);
        } else if (classPattern.endsWith("#")) {
            classPattern = classPattern.substring(0, classPattern.length() - 1);
            m_declaringTypePattern = Pattern.compileTypePattern(
                classPattern,
                SubtypePatternType.MATCH_ON_BASE_TYPE_METHODS_ONLY);
        } else {
            m_declaringTypePattern = Pattern.compileTypePattern(
                classPattern,
                SubtypePatternType.NOT_HIERARCHICAL);
        }
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