/*
 * AspectWerkz - a dynamic, lightweight and high-performant AOP/AOSD framework for Java.
 * Copyright (C) 2002-2003  Jonas Bonér. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.codehaus.aspectwerkz.definition;

import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Handles the advice weaving rule definition.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: AdviceWeavingRule.java,v 1.2 2003-06-17 16:07:54 jboner Exp $
 */
public class AdviceWeavingRule implements WeavingRule {

    private String m_expression;
    private final List m_adviceRefs = new ArrayList();
    private final List m_adviceStackRefs = new ArrayList();
    private List m_pointcutRefs = null;

    /**
     * Returns the expression.
     *
     * @return the expression
     */
    public String getExpression() {
        return m_expression;
    }

    /**
     * Sets the expression.
     *
     * @param expression the expression
     */
    public void setExpression(final String expression) {
        String tmp = expression.replaceAll("AND", "&&");
        tmp = tmp.replaceAll("and", "&&");
        tmp = tmp.replaceAll("OR", "||");
        tmp = tmp.replaceAll("or", "||");
        m_expression = tmp;
    }

    public List getPointcutRefs() {
        if (m_pointcutRefs != null) {
            return m_pointcutRefs;
        }
        String expression = m_expression.replaceAll("&&", "");
        expression = expression.replaceAll("\\|\\|", "");
        expression = expression.replaceAll("!", "");
        expression = expression.replaceAll("\\(", "");
        expression = expression.replaceAll("\\)", "");

        m_pointcutRefs = new ArrayList();
        StringTokenizer tokenizer = new StringTokenizer(expression, " ");
        while (tokenizer.hasMoreTokens()) {
            String pointcutRef = tokenizer.nextToken();
            m_pointcutRefs.add(pointcutRef);
        }
        return m_pointcutRefs;
    }

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
}
