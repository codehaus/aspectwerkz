/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.xmldef.definition;

import java.util.List;
import java.util.ArrayList;
import java.io.ObjectInputStream;

import org.codehaus.aspectwerkz.definition.expression.Expression;

/**
 * Handles the bind-introduction rule definition.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class BindIntroductionRule implements BindRule {

    /**
     * The expression for the bind-introduction rule.
     */
    private Expression m_expression;

    /**
     * A list with the introduction references bound to this rule.
     */
    private List m_introductionRefs = new ArrayList();

    /**
     * Returns the expression.
     *
     * @return the expression
     */
    public Expression getExpression() {
        return m_expression;
    }

    /**
     * Sets the expression.
     *
     * @param expression the expression
     */
    public void setExpression(final Expression expression) {
        m_expression = expression;
    }

    /**
     * Returns a list with all the introduction references.
     *
     * @return the introduction references
     */
    public List getIntroductionRefs() {
        return m_introductionRefs;
    }

    /**
     * Adds a new introduction reference.
     *
     * @param introductionRef the introduction reference
     */
    public void addIntroductionRef(final String introductionRef) {
        m_introductionRefs.add(introductionRef);
    }

    /**
     * Provides custom deserialization.
     *
     * @param stream the object input stream containing the serialized object
     * @throws java.lang.Exception in case of failure
     */
    private void readObject(final ObjectInputStream stream) throws Exception {
        ObjectInputStream.GetField fields = stream.readFields();
        m_expression = (Expression)fields.get("m_expression", null);
        m_introductionRefs = (List)fields.get("m_introductionRefs", null);
    }
}
