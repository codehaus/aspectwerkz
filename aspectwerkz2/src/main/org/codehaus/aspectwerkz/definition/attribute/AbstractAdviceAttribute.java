/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition.attribute;

/**
 * Abstract advice attribute class.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public abstract class AbstractAdviceAttribute implements Attribute {

    private final static long serialVersionUID = -4932063216445134332L;

    /**
     * The expression for the advice.
     */
    protected String m_expression;

    /**
     * The name of the advice.
     */
    protected String m_name;

    /**
     * Create an AbstractAdviceAttribute attribute.
     *
     * @param name       the name of the advice
     * @param expression the pointcut for the advice
     */
    public AbstractAdviceAttribute(final String name, final String expression) {
        if (expression == null || expression.equals("")) {
            throw new IllegalArgumentException("expression is not valid for around advice");
        }
        m_name = name;
        m_expression = expression;
    }

    /**
     * Returns the name of the advice.
     *
     * @return the name of the advice
     */
    public String getName() {
        return m_name;
    }

    /**
     * Return the expression.
     *
     * @return the expression
     */
    public String getExpression() {
        return m_expression;
    }
}
