/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition.attribute;

/**
 * Attribute for the Implements construct.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class ImplementsAttribute implements Attribute {

    private static final long serialVersionUID = 6733442201047160043L;

    /**
     * The pointcut expression.
     */
    private final String m_expression;

    /**
     * Create an Implements attribute.
     *
     * @param expression the pointcut expression
     */
    public ImplementsAttribute(final String expression) {
        if (expression == null || expression.equals("")) throw new IllegalArgumentException("expression is not valid for implements pointcut");
        m_expression = expression;
    }

    /**
     * Returns the expression.
     *
     * @return the expression
     */
    public String getExpression() {
        return m_expression;
    }
}
