/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition;

import org.codehaus.aspectwerkz.definition.expression.PointcutType;

/**
 * Holds the meta-data for the pointcuts.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class PointcutDefinition
{
    /**
     * The name of the pointcut.
     */
    private String m_name;

    /**
     * The type for the pointcut.
     */
    private PointcutType m_type;

    /**
     * The expression.
     */
    private String m_expression;

    /**
     * Marks the pointcut as reentrant.
     */
    private String m_isNonReentrant = "false";

    /**
     * Returns the expression for the pointcut.
     *
     * @return the expression for the pointcut
     */
    public String getExpression()
    {
        return m_expression;
    }

    /**
     * Sets the expression.
     *
     * @param expression the expression
     */
    public void setExpression(final String expression)
    {
        m_expression = expression;
    }

    /**
     * Returns the name of the pointcut.
     *
     * @return the name
     */
    public String getName()
    {
        return m_name;
    }

    /**
     * Sets the name of the pointcut.
     */
    public void setName(final String name)
    {
        m_name = name;
    }

    /**
     * Returns the type of the pointcut.
     *
     * @return the type
     */
    public PointcutType getType()
    {
        return m_type;
    }

    /**
     * Sets the type of the pointcut.
     *
     * @param type the type
     */
    public void setType(final PointcutType type)
    {
        m_type = type;
    }

    /**
     * Sets the non-reentrancy flag.
     *
     * @param isNonReentrant
     */
    public void setNonReentrant(final String isNonReentrant)
    {
        m_isNonReentrant = isNonReentrant;
    }

    /**
     * Returns the string representation of the non-reentrancy flag.
     *
     * @return the non-reentrancy flag
     */
    public String getNonReentrant()
    {
        return m_isNonReentrant;
    }

    /**
     * Checks if the pointcut is non-reentrant or not.
     *
     * @return the non-reentrancy flag
     */
    public boolean isNonReentrant()
    {
        return "true".equalsIgnoreCase(m_isNonReentrant);
    }
}
