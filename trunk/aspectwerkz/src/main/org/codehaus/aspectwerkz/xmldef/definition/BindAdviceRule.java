/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.xmldef.definition;

import java.util.List;
import java.util.ArrayList;
import java.io.ObjectInputStream;

import org.codehaus.aspectwerkz.definition.expression.Expression;

/**
 * Handles the bind-advice rule definition.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class BindAdviceRule implements BindRule {

    /**
     * The pointcut expression.
     */
    private Expression m_expression;

//    /**
//     * The cflow pointcut expression - if any
//     */
//    private Expression m_cflow;

    /**
     * The advices references.
     */
    private List m_adviceRefs = new ArrayList();

    /**
     * The advice stack references.
     */
    private List m_adviceStackRefs = new ArrayList();

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

//    /**
//     * Returns the cflow expression - if any
//     *
//     * @return the cflow expression or null
//     */
//    public Expression getCflowExpression() {
//        return null;//m_cflow;
//    }

//    /**
//     * Sets the cflow expression - if any
//     *
//     * @param expression the expression
//     */
//    public void setCflowExpression(Expression expression) {
//        this.m_cflow = expression;
//    }

    /**
     * Returns a list with all the advice references.
     *
     * @return the advice references
     */
    public List getAdviceRefs() {
        return m_adviceRefs;
    }

    /**
     * Adds a new advice reference.
     *
     * @param adviceRef the advice reference
     */
    public void addAdviceRef(final String adviceRef) {
        m_adviceRefs.add(adviceRef);
    }

    /**
     * Returns a list with all the advice stack references.
     *
     * @return the advice stack references
     */
    public List getAdviceStackRefs() {
        return m_adviceStackRefs;
    }

    /**
     * Adds a new advice stack reference.
     *
     * @param adviceStackRef the advice stack reference
     */
    public void addAdviceStackRef(final String adviceStackRef) {
        m_adviceStackRefs.add(adviceStackRef);
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
        m_adviceRefs = (List)fields.get("m_adviceRefs", null);
        m_adviceStackRefs = (List)fields.get("m_adviceStackRefs", null);
    }
}

