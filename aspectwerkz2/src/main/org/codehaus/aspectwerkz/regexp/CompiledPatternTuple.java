/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.regexp;

import java.io.Serializable;

/**
 * Holds a pre-compiled tuple that consists of the class pattern and the pattern for a specific pointcut.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class CompiledPatternTuple implements Serializable {

    /**
     * The class pattern.
     */
    private final ClassPattern m_classPattern;

    /**
     * The method/field/callerside/throws pattern.
     */
    private final Pattern m_pattern;

    /**
     * Hierachical flag.
     */
    private boolean m_hierarchical = false;

    /**
     * Creates a new pointcut pattern.
     *
     * @param classPattern the class pattern
     * @param pattern      the pattern
     * @param hierarchical the hierarchical flag
     */
    public CompiledPatternTuple(final ClassPattern classPattern,
                                final Pattern pattern,
                                final boolean hierarchical) {
        m_classPattern = classPattern;
        m_pattern = pattern;
        m_hierarchical = hierarchical;
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

    /**
     * Checks it the pointcut is hierarchical.
     *
     * @return the flag
     */
    public boolean isHierarchical() {
        return m_hierarchical;
    }

    public String toString() {
        return "["
                + super.toString()
                + ": "
                + "," + m_pattern.toString()
                + "," + m_classPattern.m_pattern
                + "," + m_hierarchical
                + "]";
    }

    public int hashCode() {
        int result = 17;
        result = 37 * result + hashCodeOrZeroIfNull(m_classPattern);
        result = 37 * result + hashCodeOrZeroIfNull(m_pattern);
        return result;
    }

    protected static int hashCodeOrZeroIfNull(final Object o) {
        if (null == o) return 19;
        return o.hashCode();
    }

    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof CompiledPatternTuple)) return false;
        final CompiledPatternTuple obj = (CompiledPatternTuple)o;
        return areEqualsOrBothNull(obj.m_classPattern, this.m_classPattern)
                && areEqualsOrBothNull(obj.m_pattern, this.m_pattern);
    }

    protected static boolean areEqualsOrBothNull(final Object o1, final Object o2) {
        if (null == o1) return (null == o2);
        return o1.equals(o2);
    }
}
