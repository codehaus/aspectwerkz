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
 * Abstract advice attribute class.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public abstract class AbstractAdviceAttribute implements Serializable {

    /**
     * @TODO: calculate serialVersionUID
     */
    private static final long serialVersionUID = 1L;

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
     * @param name the name of the advice
     * @param pointcut the pointcut for the advice
     */
    public AbstractAdviceAttribute(final String name, final String expression) {
        if (expression == null) throw new IllegalArgumentException("expression is not valid for around advice");
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
