/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.attribdef.definition.attribute;

/**
 * Attribute for the inner class Introduction construct.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class IntroduceAttribute implements Attribute {

    private static final long serialVersionUID = -146743510655018866L;

    /**
     * The expression for the introduction.
     */
    private final String m_expression;

    /**
     * The FQN of the inner class for default introduction impl.
     */
    private final String m_innerClassName;

    /**
     * The FQN of interface implemented by the inner class.
     */
    private final String[] m_introducedInterfaceNames;

    /**
     * Create an Introduction attribute.
     *
     * @param expression the expression for the introduction
     */
    public IntroduceAttribute(final String expression, final String innerClassName, final String[] interfaceNames) {
        if (expression == null) throw new IllegalArgumentException("expression is not valid for introduction");
        m_expression = expression;
        m_innerClassName = innerClassName;
        m_introducedInterfaceNames = interfaceNames;
    }

    /**
     * Return the expression.
     *
     * @return the expression
     */
    public String getExpression() {
        return m_expression;
    }

    public String getInnerClassName() {
        return m_innerClassName;
    }

    public String[] getIntroducedInterfaceNames() {
        return m_introducedInterfaceNames;
    }

}
