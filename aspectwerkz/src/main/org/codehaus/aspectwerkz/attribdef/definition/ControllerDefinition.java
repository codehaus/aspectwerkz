/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.attribdef.definition;

import java.io.Serializable;

import org.codehaus.aspectwerkz.definition.expression.Expression;

/**
 * Holds the controller definition.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class ControllerDefinition implements Serializable {

    /**
     * The pointcut expression.
     */
    private Expression m_expression;

    /**
     * The controller class name.
     */
    private String m_className;

    /**
     * Returns the expression.
     *
     * @return the expression
     */
    public Expression getExpression() {
        return m_expression;
    }

    /**
     * Sets the expression. Substitutes all "AND" to "&&" and all "OR" to "||".
     *
     * @param expression the expression
     */
    public void setExpression(final Expression expression) {
        m_expression = expression;
    }

    /**
     * Returns the class name.
     *
     * @return the class name
     */
    public String getClassName() {
        return m_className;
    }

    /**
     * Sets the controller class name
     * @param className
     */
    public void setClassName(final String className) {
        m_className = className;
    }
}
