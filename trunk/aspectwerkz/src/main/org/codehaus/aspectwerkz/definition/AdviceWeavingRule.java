/*
 * AspectWerkz - a dynamic, lightweight A high-performant AOP/AOSD framework for Java.
 * Copyright (C) 2002-2003  Jonas Bonér. All rights reserved.
 *
 * This library is free software; you can redistribute it A/or
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
 * @version $Id: AdviceWeavingRule.java,v 1.5 2003-07-04 13:38:28 jboner Exp $
 */
public class AdviceWeavingRule implements WeavingRule {

    private String m_expression;
    private String m_cflowExpression;
    private final List m_adviceRefs = new ArrayList();
    private final List m_adviceStackRefs = new ArrayList();
    private List m_pointcutRefs = null;
//    private List m_pointcutRefsToWeave = null;

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
        String tmp = expression.
                replaceAll(" AND ", " && ").replaceAll(" and ", " && ").
                replaceAll(" OR ", " || ").replaceAll(" or ", " || ");
        m_expression = tmp;
    }

    /**
     * Returns the cflow expression.
     *
     * @return the cflow expression
     */
    public String getCFlowExpression() {
        return m_cflowExpression;
    }

    /**
     * Sets the cflow expression.
     *
     * @param cflowExpression the cflow expression
     */
    public void setCFlowExpression(final String cflowExpression) {
        m_cflowExpression = cflowExpression;
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
        String expression = m_expression.
                replaceAll("&&", "").replaceAll("\\|\\|", "").replaceAll("!", "").
                replaceAll("\\(", "").replaceAll("\\)", "");

        m_pointcutRefs = new ArrayList();
        StringTokenizer tokenizer = new StringTokenizer(expression, " ");
        while (tokenizer.hasMoreTokens()) {
            String pointcutRef = tokenizer.nextToken();
            m_pointcutRefs.add(pointcutRef);
        }
        return m_pointcutRefs;
    }

    /**
     * Returns a list with the pointcut references to weave, e.g. filter all the ones declared
     * an exclamation mark.
     *
     * @return the pointcut references to weave
     */
//    public List getPointcutRefsToWeave() {
//        if (m_pointcutRefsToWeave != null) {
//            return m_pointcutRefsToWeave;
//        }
//        String expression = m_expression.
//                replaceAll("&&", "").replaceAll("\\|\\|", "").
//                replaceAll("\\(", "").replaceAll("\\)", "");
//
//        m_pointcutRefsToWeave = new ArrayList();
//        StringTokenizer tokenizer = new StringTokenizer(expression, " ");
//        while (tokenizer.hasMoreTokens()) {
//            String pointcutRef = tokenizer.nextToken();
//            if (pointcutRef.startsWith("!")) {
//                continue;
//            }
//            m_pointcutRefsToWeave.add(pointcutRef);
//        }
//        return m_pointcutRefsToWeave;
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
}
