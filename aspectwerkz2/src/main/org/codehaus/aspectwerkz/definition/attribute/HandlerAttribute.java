/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition.attribute;


/**
 * Attribute for the handler pointcut.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class HandlerAttribute implements Attribute
{
    /**
     * @TODO: recalculate
     */
    private static final long serialVersionUID = 727314474096204037L;

    /**
     * The expression for the pointcut.
     */
    private final String m_expression;

    /**
     * Create a handler attribute.
     *
     * @param expression the expression
     */
    public HandlerAttribute(final String expression)
    {
        if ((expression == null) || expression.equals(""))
        {
            throw new IllegalArgumentException(
                "expression is not valid for handler pointcut");
        }

        m_expression = expression;
    }

    /**
     * Return the expression for the pointcut.
     *
     * @return the expression for the pointcut
     */
    public String getExpression()
    {
        return m_expression;
    }
}
