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
package org.codehaus.aspectwerkz.regexp;

import org.codehaus.aspectwerkz.regexp.Pattern;
import org.codehaus.aspectwerkz.regexp.ClassPattern;

/**
 * Holds a pre-compiled tuple that consists of the class pattern and the
 * pattern for a specific pointcut.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: PointcutPatternTuple.java,v 1.1 2003-06-30 16:04:36 jboner Exp $
 */
public class PointcutPatternTuple {

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
     * @param pattern the pattern
     */
    public PointcutPatternTuple(final ClassPattern classPattern,
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

    public String toString() {
        return "["
                + super.toString()
                + ": "
                + "," + m_pattern.toString()
                + "," + m_classPattern.m_pattern
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
        if (!(o instanceof PointcutPatternTuple)) return false;
        final PointcutPatternTuple obj = (PointcutPatternTuple)o;
        return areEqualsOrBothNull(obj.m_classPattern, this.m_classPattern)
                && areEqualsOrBothNull(obj.m_pattern, this.m_pattern);
    }

    protected static boolean areEqualsOrBothNull(final Object o1, final Object o2) {
        if (null == o1) return (null == o2);
        return o1.equals(o2);
    }
}
