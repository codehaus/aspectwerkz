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

import org.codehaus.aspectwerkz.regexp.ClassPattern;
import org.codehaus.aspectwerkz.regexp.Pattern;

/**
 * Handles the introduction weaving rule definition.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: IntroductionWeavingRule.java,v 1.1 2003-06-17 14:45:14 jboner Exp $
 */
public class IntroductionWeavingRule implements WeavingRule {

    private String m_classPattern;
    private ClassPattern m_regexpClassPattern;
    private final List m_introductionRefs = new ArrayList();

    /**
     * Returns the class pattern.
     *
     * @return the class pattern
     */
    public String getClassPattern() {
        return m_classPattern;
    }

    /**
     * Sets the class pattern
     *
     * @param classPattern the class pattern
     */
    public void setClassPattern(final String classPattern) {
        m_classPattern = classPattern;
        m_regexpClassPattern = Pattern.compileClassPattern(classPattern);
    }

    /**
     * Returns the class pattern as a pre-compiled pattern.
     *
     * @return the class pattern
     */
    public ClassPattern getRegexpClassPattern() {
        return m_regexpClassPattern;
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
}
