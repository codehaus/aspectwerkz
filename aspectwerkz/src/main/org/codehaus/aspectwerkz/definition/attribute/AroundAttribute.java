/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition.attribute;

import java.io.Serializable;

/**
 * Attribute for the Around Advice construct.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class AroundAttribute implements Serializable {

    /**
     * @TODO: calculate serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * The expression for the advice.
     */
    private String m_expression;

    /**
     * Create an AroundAdvice attribute.
     *
     * @param pointcut the pointcut for the advice
     */
    public AroundAttribute(final String expression) {
        if (expression == null) throw new IllegalArgumentException("expression is not valid for around advice");
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
