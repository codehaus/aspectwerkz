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
import java.util.StringTokenizer;

import org.codehaus.aspectwerkz.util.Strings;

/**
 * Holds the meta-data for the interface introductions.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public class InterfaceIntroductionDefinition {

    /**
     * The name of the introduction.
     */
    protected String m_name;

    /**
     * The pointcut for the introduction.
     */
    private final String m_expression;

    /**
     * The introduction weaving rule.
     */
    private IntroductionWeavingRule m_weavingRule;

    /**
     * The attribute for the introduction.
     */
    private String m_attribute = "";

    /**
     * The pointcut definition references.
     */
    private List m_pointcutRefs = null;

    /**
     * The interface class name.
     */
    private String m_interfaceClassName;

    /**
     * Creates a new introduction meta-data instance.
     *
     * @param name the name of the expression
     * @param expression the expression
     * @param interfaceClassName the class name of the interface
     */
    public InterfaceIntroductionDefinition(final String name,
                                           final String expression,
                                           final String interfaceClassName) {
        if (name == null) throw new IllegalArgumentException("name can not be null");
        if (interfaceClassName == null) throw new IllegalArgumentException("interface class name can not be null");
        if (expression == null) throw new IllegalArgumentException("expression can not be null");

        m_name = name;
        m_interfaceClassName = interfaceClassName;
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
     * Returns the pointcut.
     *
     * @retur1n the pointcut
     */
    public String getExpression() {
        return m_expression;
    }

    /**
     * Returns the class name of the interface.
     *
     * @return the class name of the interface
     */
    public String getInterfaceClassName() {
        return m_interfaceClassName;
    }

    /**
     * Returns the weaving rule.
     *
     * @return the weaving rule
     */
    public IntroductionWeavingRule getWeavingRule() {
        return m_weavingRule;
    }

    /**
     * Sets the weaving rule.
     *
     * @param weavingRule the weaving rule
     */
    public void setWeavingRule(final IntroductionWeavingRule weavingRule) {
        m_weavingRule = weavingRule;
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

    /**
     * Returns a list with the pointcut references.
     *
     * @return the pointcut references
     */
    public List getPointcutRefs() {
        if (m_pointcutRefs != null) {
            return m_pointcutRefs;
        }
        String expression = Strings.replaceSubString(m_expression, "&&", "");
        expression = Strings.replaceSubString(expression, "||", "");
        expression = Strings.replaceSubString(expression, "!", "");
        expression = Strings.replaceSubString(expression, "(", "");
        expression = Strings.replaceSubString(expression, ")", "");

        m_pointcutRefs = new ArrayList();
        StringTokenizer tokenizer = new StringTokenizer(expression, " ");
        while (tokenizer.hasMoreTokens()) {
            String pointcutRef = tokenizer.nextToken();
            m_pointcutRefs.add(pointcutRef);
        }
        return m_pointcutRefs;
    }
}
