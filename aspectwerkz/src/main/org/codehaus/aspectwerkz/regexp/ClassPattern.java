/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.regexp;

import java.io.ObjectInputStream;

import org.codehaus.aspectwerkz.exception.DefinitionException;
import org.codehaus.aspectwerkz.util.Strings;

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
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class ClassPattern extends Pattern {

    /**
     * The fully qualified class name.
     */
    protected transient com.karneim.util.collection.regex.Pattern m_classNamePattern;

    /**
     * The pattern as a string.
     */
    protected String m_pattern;

    /**
     * Matches a class name.
     *
     * @param className the name of the class
     * @return true if we have a matches
     */
    public boolean matches(final String className) {
        if (className == null) throw new IllegalArgumentException("class name can not be null");
        if (className.equals("")) return false;
        return m_classNamePattern.contains(className);
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
        String className = pattern;

        try {
            if (className.equals(SINGLE_WILDCARD) || className.equals(MULTIPLE_WILDCARD)) {
                className = "[a-zA-Z0-9_$.]+";
            }
            else {
                className = Strings.replaceSubString(className, "..", "[a-zA-Z0-9_$.]+");
                className = Strings.replaceSubString(className, ".", "\\.");
                className = Strings.replaceSubString(className, "*", "[a-zA-Z0-9_$]*");
            }
            m_classNamePattern = new com.karneim.util.collection.regex.Pattern(className);
        }
        catch (Throwable e) {
            throw new DefinitionException("class pattern is not well formed: " + pattern);
        }
    }

    /**
     * Private constructor.
     *
     * @param pattern the pattern
     */
    ClassPattern(final String pattern) {
        m_pattern = pattern;
        parse(m_pattern);
    }

    /**
     * Provides custom deserialization.
     *
     * @param stream the object input stream containing the serialized object
     * @throws java.lang.Exception in case of failure
     */
    private void readObject(final ObjectInputStream stream) throws Exception {
        ObjectInputStream.GetField fields = stream.readFields();
        m_pattern = (String)fields.get("m_pattern", null);
        parse(m_pattern);
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
