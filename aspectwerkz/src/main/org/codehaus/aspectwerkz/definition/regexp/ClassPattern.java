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
package org.codehaus.aspectwerkz.definition.regexp;

import org.codehaus.aspectwerkz.exception.DefinitionException;

/**
 * Implements the regular expression pattern matcher for classes in AspectWerkz.
 * <p/>
 * Example of supported patterns:
 * <pre>
 *      foo.bar.SomeClass
 *      foo.bar.*
 *      foo.*.SomeClass
 *      foo..
 * </pre>
 *
 * @author <a href="mailto:jboner@acm.org">Jonas Bonér</a>
 * @version $Id: ClassPattern.java,v 1.2 2003-05-14 17:17:39 jboner Exp $
 */
public class ClassPattern extends Pattern {

    /**
     * The fully qualified class name.
     */
    protected java.util.regex.Pattern m_classNamePattern;

    /**
     * The pattern as a string.
     */
    protected String m_pattern;

    /**
     * Matches a class name.
     *
     * @param className the name of the class
     * @return true if we have a match
     */
    public boolean matches(final String className) {
        if (className == null) throw new IllegalArgumentException("class name can not be null");
        if (className.equals("")) return false;
        return m_classNamePattern.matcher(className).matches();
    }

    /**
     * Returns the pattern as a string.
     *
     * @return the pattern
     */
    public String getPattern() {
        return m_pattern;
    }

    /**
     * Parses the class pattern.
     *
     * @param pattern the method pattern
     */
    protected void parse(final String pattern) {
        m_pattern = pattern;
        String className = pattern;

        if (className.equals(SINGLE_WILDCARD) ||
                className.equals(MULTIPLE_WILDCARD)) {
            className = "\\b.*\\b";
        }
        else {
            className = className.replaceAll("\\.\\.", "\\.*");
            className = className.replaceAll("\\.", "\\\\.");
            className = className.replaceAll("\\*", "\\.*");
        }
        m_classNamePattern = java.util.regex.Pattern.compile(className);
    }

    /**
     * Private constructor.
     *
     * @param pattern the pattern
     */
    ClassPattern(final String pattern) {
        try {
            parse(pattern);
        }
        catch (Exception e) {
            throw new DefinitionException("class pattern is not well formed: " + pattern);
        }
    }

    public int hashCode() {
        int result = 17;
        result = 37 * result + hashCodeOrZeroIfNull(m_pattern);
        result = 37 * result + hashCodeOrZeroIfNull(m_classNamePattern);
        result = 37 * result + hashCodeOrZeroIfNull(m_abbreviations);
        return result;
    }

    protected static int hashCodeOrZeroIfNull(final Object o) {
        if (null == o) return 19;
        return o.hashCode();
    }

    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof ClassPattern)) return false;
        final ClassPattern obj = (ClassPattern)o;
        return areEqualsOrBothNull(obj.m_pattern, this.m_pattern)
                && areEqualsOrBothNull(obj.m_classNamePattern, this.m_classNamePattern)
                && areEqualsOrBothNull(obj.m_abbreviations, this.m_abbreviations);
    }

    protected static boolean areEqualsOrBothNull(final Object o1, final Object o2) {
        if (null == o1) return (null == o2);
        return o1.equals(o2);
    }
}
