/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition.attribute;

import attrib4j.Attribute;

/**
 * Attribute for the Pre Advice construct.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public class PreAdviceAttribute implements Attribute {

    /**
     * The expression for the advice.
     */
    private String m_expression;

    /**
     * Create an PreAdvice attribute.
     *
     * @param expression the expression for the advice
     */
    public PreAdviceAttribute(final String expression) {
        if (expression == null) throw new IllegalArgumentException("expression is not valid for pre advice");
        m_expression = expression;
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
