/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.attribdef.definition.attribute;

/**
 * Attribute for the class pointcut.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public class ClassAttribute implements Attribute {

    private static final long serialVersionUID = 3779513437663080413L;

    /**
     * The expression for the pointcut.
     */
    private final String m_expression;

    /**
     * Create an execution attribute.
     *
     * @param expression the expression
     */
    public ClassAttribute(final String expression) {
        if (expression == null) throw new IllegalArgumentException("expression is not valid for class pointcut");
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
