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

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

/**
 * Holds the advice stack definition.
 *
 * @author <a href="mailto:jboner@acm.org">Jonas Bonér</a>
 * @version $Id: AdviceStackDefinition.java,v 1.2 2003-05-12 09:41:29 jboner Exp $
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
    public List getAdvices() {
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
