/* Generated By:JJTree: Do not edit this line. ASTClassPattern.java */
package org.codehaus.aspectwerkz.expression.ast;

import org.codehaus.aspectwerkz.expression.regexp.Pattern;
import org.codehaus.aspectwerkz.expression.regexp.TypePattern;

public class ASTClassPattern extends SimpleNode
{
    private TypePattern m_declaringClassPattern;
    private boolean m_hierarchical = false;

    public ASTClassPattern(int id)
    {
        super(id);
    }

    public ASTClassPattern(ExpressionParser p, int id)
    {
        super(p, id);
    }

    public Object jjtAccept(ExpressionParserVisitor visitor, Object data)
    {
        return visitor.visit(this, data);
    }

    public void setTypePattern(String pattern)
    {
        m_declaringClassPattern = Pattern.compileTypePattern(pattern, false);
    }

    public void setHierarchical(boolean isHierarchical)
    {
        m_hierarchical = isHierarchical;
        m_declaringClassPattern.setHierarchical(true);
    }

    public TypePattern getDeclaringClassPattern()
    {
        return m_declaringClassPattern;
    }

    boolean isHierarchical()
    {
        return m_hierarchical;
    }
}
