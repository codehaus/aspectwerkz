/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.pointcut;

import org.codehaus.aspectwerkz.regexp.ClassPattern;
import org.codehaus.aspectwerkz.regexp.Pattern;

/**
 * Holds a pre-compiled tuple that consists of the class pattern and the pattern for a specific pointcut.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class PointcutPattern {

    /**
     * The class pattern.
     */
    private final ClassPattern m_classPattern;

    /**
     * The method/field/callerside/throws pattern.
     */
    private final Pattern m_pattern;

    /**
     * Creates a new pointcut pattern.
     *
     * @param classPattern the class pattern
     * @param pattern      the pattern
     */
    public PointcutPattern(final ClassPattern classPattern,
                           final Pattern pattern) {
        m_classPattern = classPattern;
        m_pattern = pattern;
    }

    /**
     * Returns the class pattern.
     *
     * @return the class pattern
     */
    public ClassPattern getClassPattern() {
        return m_classPattern;
    }

    /**
     * Returns the pattern.
     *
     * @return the pattern
     */
    public Pattern getPattern() {
        return m_pattern;
    }
}
