/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.regexp;

import java.io.Serializable;

/**
 * Holds a tuple that consists of the class pattern and the pattern for a specific pointcut.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class PatternTuple implements Serializable {

    /**
     * The caller class pattern.
     */
    private final String m_callerClassPattern;

    /**
     * The callee class pattern.
     */
    private final String m_calleeClassPattern;

    /**
     * The method/field/callerside/throws pattern.
     */
    private final String m_memberPattern;

    /**
     * Hierachical flag.
     */
    private boolean m_hierarchical = false;

    /**
     * Creates a new pointcut pattern.
     *
     * @param callerClassPattern the caller class pattern
     * @param calleeClassPattern the callee class pattern
     * @param pattern the pattern
     * @param hierarchical the hierarchical flag
     */
    public PatternTuple(final String callerClassPattern,
                        final String calleeClassPattern,
                        final String pattern,
                        final boolean hierarchical) {
        m_callerClassPattern = callerClassPattern;
        m_calleeClassPattern = calleeClassPattern;
        m_memberPattern = pattern;
        m_hierarchical = hierarchical;
    }

    /**
     * Returns the caller class pattern.
     *
     * @return the caller class pattern
     */
    public String getCallerClassPattern() {
        return m_callerClassPattern;
    }

    /**
     * Returns the callee class pattern.
     *
     * @return the callee class pattern
     */
    public String getCalleeClassPattern() {
        return m_calleeClassPattern;
    }

    /**
     * Returns the pattern.
     *
     * @return the pattern
     */
    public String getMemberPattern() {
        return m_memberPattern;
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
                + "," + m_memberPattern
                + "," + m_callerClassPattern
                + "," + m_hierarchical
                + "]";
    }

    public int hashCode() {
        int result = 17;
        result = 37 * result + hashCodeOrZeroIfNull(m_callerClassPattern);
        result = 37 * result + hashCodeOrZeroIfNull(m_memberPattern);
        return result;
    }

    protected static int hashCodeOrZeroIfNull(final Object o) {
        if (null == o) return 19;
        return o.hashCode();
    }

    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof PatternTuple)) return false;
        final PatternTuple obj = (PatternTuple)o;
        return areEqualsOrBothNull(obj.m_callerClassPattern, this.m_callerClassPattern)
                && areEqualsOrBothNull(obj.m_memberPattern, this.m_memberPattern);
    }

    protected static boolean areEqualsOrBothNull(final Object o1, final Object o2) {
        if (null == o1) return (null == o2);
        return o1.equals(o2);
    }
}
