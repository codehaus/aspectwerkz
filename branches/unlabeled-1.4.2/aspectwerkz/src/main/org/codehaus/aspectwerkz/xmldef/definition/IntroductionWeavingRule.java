/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition;

import java.util.List;
import java.util.ArrayList;

import org.codehaus.aspectwerkz.regexp.ClassPattern;
import org.codehaus.aspectwerkz.regexp.Pattern;

/**
 * Handles the introduction weaving rule definition.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
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
