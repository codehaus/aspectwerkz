/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

/**
 * Holds the advice stack definition.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class AdviceStackDefinition implements Serializable {

    private String m_name;
    private final List m_advices = new ArrayList();

    /**
     * Returns the name of the advice stack.
     *
     * @return the name
     */
    public String getName() {
        return m_name;
    }

    /**
     * Sets the name of the advice stack.
     *
     * @param name the name
     */
    public void setName(final String name) {
        m_name = name.trim();
    }

    /**
     * Returns the name of the advices as list.
     *
     * @return the name of the advices
     */
    public List getAdviceRefs() {
        return m_advices;
    }

    /**
     * Adds the name of an advice.
     *
     * @param advice the names of the advice
     */
    public void addAdvice(final String advice) {
        m_advices.add(advice.trim());
    }
}
