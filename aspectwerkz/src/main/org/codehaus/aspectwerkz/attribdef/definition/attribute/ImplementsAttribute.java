/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.attribdef.definition.attribute;

import java.io.Serializable;

/**
 * Attribute for the Implements construct.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public class ImplementsAttribute implements Serializable {

    /**
     * @TODO: calculate serialVersionUID
     */
    private static final long serialVersionUID = 1L;

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