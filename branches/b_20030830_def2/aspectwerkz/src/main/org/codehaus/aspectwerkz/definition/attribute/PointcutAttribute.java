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
 * Attribute for the Pointcut construct.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class PointcutAttribute implements Attribute {

    /**
     * The expression for the pointcut.
     */
    private String m_expression;

    /**
     * Create an Pointcut attribute.
     *
     * @param expression the expression for the pointcut
     */
    public PointcutAttribute(final String expression) {
        if (expression == null) throw new IllegalArgumentException("pointcut expression is not valid");
        m_expression = expression;
    }

    /**
     * Return the expression for the pointcut.
     *
     * @return the expression for the pointcut
     */
    public String getExpression() {
        return m_expression;
    }
}
