/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.attribdef.definition;

import java.util.List;
import java.util.ArrayList;

import org.codehaus.aspectwerkz.xmldef.definition.BindIntroductionRule;
import org.codehaus.aspectwerkz.definition.expression.Expression;

/**
 * Holds the meta-data for the interface introductions.
 * This definition holds only pure interface introduction.
 *
 * It is extended in IntroductionDefinition for interface+implementation introductions
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class InterfaceIntroductionDefinition {

    protected final String m_name;

    /**
     * The introduction expression.
     */
    protected final Expression m_expression;

    /**
     * The bind-introduction rule.
     */
    protected BindIntroductionRule m_bindIntroductionRule;

    /**
     * The attribute for the introduction.
     */
    protected String m_attribute = "";

    /**
     * The interface classes name.
     */
    protected List m_interfaceClassNames = new ArrayList();

    /**
     * Creates a new introduction meta-data instance.
     *
     * @param name the name of the expression
     * @param expression the expression
     * @param interfaceClassName the class name of the interface
     */
    public InterfaceIntroductionDefinition(final String name,
                                           final Expression expression,
                                           final String interfaceClassName) {
        if (name == null) throw new IllegalArgumentException("name can not be null");
        if (interfaceClassName == null) throw new IllegalArgumentException("interface class name can not be null");
        if (expression == null) throw new IllegalArgumentException("expression can not be null");

        m_name = name;
        m_interfaceClassNames.add(interfaceClassName);
        m_expression = expression;
    }

    /**
     * Returns the name of the introduction.
     *
     * @return the name
     */
    public String getName() {
        return m_name;
    }

    /**
     * Returns the expression.
     *
     * @return the expression
     */
    public Expression getExpression() {
        return m_expression;
    }

    /**
     * Returns the class name of the interface.
     *
     * @return the class name of the interface
     */
    public String getInterfaceClassName() {
        return (String)m_interfaceClassNames.get(0);
    }

    /**
     * Returns the class name of the interface.
     *
     * @return the class name of the interface
     */
    public List getInterfaceClassNames() {
        return m_interfaceClassNames;
    }

    /**
     * Returns the bind-introduction rule.
     *
     * @return the bind-introduction  rule
     */
    public BindIntroductionRule getBindIntroductionRule() {
        return m_bindIntroductionRule;
    }

    /**
     * Sets the bind-introduction rule.
     *
     * @param bindIntroductionRule the bind-introduction rule
     */
    public void setBindIntroductionRule(final BindIntroductionRule bindIntroductionRule) {
        m_bindIntroductionRule = bindIntroductionRule;
    }

    /**
     * Returns the attribute.
     *
     * @return the attribute
     */
    public String getAttribute() {
        return m_attribute;
    }

    /**
     * Sets the attribute.
     *
     * @param attribute the attribute
     */
    public void setAttribute(final String attribute) {
        m_attribute = attribute;
    }
}
