/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition;

import org.codehaus.aspectwerkz.definition.expression.Expression;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds the meta-data for the interface introductions. <p/>This definition holds only pure interface introduction.
 * <p/>It is extended in IntroductionDefinition for interface+implementation introductions
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class InterfaceIntroductionDefinition
{
    /**
     * The name of the interface introduction.
     */
    protected final String m_name;

    /**
     * The introduction expressions.
     */
    protected Expression[] m_expressions;

    /**
     * The attribute for the introduction.
     */
    protected String m_attribute = "";

    /**
     * The interface classes name.
     */
    protected List m_interfaceClassNames = new ArrayList();

    /**
     * Creates a new introduction meta-data instance.
     *
     * @param name               the name of the expression
     * @param expression         the expression
     * @param interfaceClassName the class name of the interface
     */
    public InterfaceIntroductionDefinition(final String name,
        final Expression expression, final String interfaceClassName)
    {
        if (name == null)
        {
            throw new IllegalArgumentException("name can not be null");
        }

        if (expression == null)
        {
            throw new IllegalArgumentException("expression can not be null");
        }

        if (interfaceClassName == null)
        {
            throw new IllegalArgumentException(
                "interface class name can not be null");
        }

        m_name = name;
        m_interfaceClassNames.add(interfaceClassName);
        m_expressions = new Expression[1];
        m_expressions[0] = expression;
    }

    /**
     * Returns the name of the introduction.
     *
     * @return the name
     */
    public String getName()
    {
        return m_name;
    }

    /**
     * Returns the expressions.
     *
     * @return the expressions array
     */
    public Expression[] getExpressions()
    {
        return m_expressions;
    }

    /**
     * Returns the class name of the interface.
     *
     * @return the class name of the interface
     */
    public String getInterfaceClassName()
    {
        return (String) m_interfaceClassNames.get(0);
    }

    /**
     * Returns the class name of the interface.
     *
     * @return the class name of the interface
     */
    public List getInterfaceClassNames()
    {
        return m_interfaceClassNames;
    }

    /**
     * Returns the attribute.
     *
     * @return the attribute
     */
    public String getAttribute()
    {
        return m_attribute;
    }

    /**
     * Sets the attribute.
     *
     * @param attribute the attribute
     */
    public void setAttribute(final String attribute)
    {
        m_attribute = attribute;
    }

    public void addExpression(Expression expression)
    {
        final Expression[] tmpExpressions = new Expression[m_expressions.length
            + 1];

        java.lang.System.arraycopy(m_expressions, 0, tmpExpressions, 0,
            m_expressions.length);
        tmpExpressions[m_expressions.length] = expression;
        m_expressions = new Expression[m_expressions.length + 1];
        java.lang.System.arraycopy(tmpExpressions, 0, m_expressions, 0,
            tmpExpressions.length);
    }

    public void addExpressions(Expression[] expressions)
    {
        final Expression[] tmpExpressions = new Expression[m_expressions.length
            + expressions.length];

        java.lang.System.arraycopy(m_expressions, 0, tmpExpressions, 0,
            m_expressions.length);
        java.lang.System.arraycopy(expressions, 0, tmpExpressions,
            m_expressions.length, expressions.length);
        m_expressions = new Expression[m_expressions.length
            + expressions.length];
        java.lang.System.arraycopy(tmpExpressions, 0, m_expressions, 0,
            tmpExpressions.length);
    }
}
