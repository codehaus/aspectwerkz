/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.expression.regexp;

import org.codehaus.aspectwerkz.expression.ExpressionException;
import org.codehaus.aspectwerkz.util.Strings;

import java.io.ObjectInputStream;

/**
 * Implements the regular expression pattern matcher for types.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class TypePattern extends Pattern
{
    /**
     * The fully qualified type name.
     */
    protected transient com.karneim.util.collection.regex.Pattern m_typeNamePattern;

    /**
     * The pattern as a string.
     */
    protected String m_pattern;

    /**
     * Hierarchical boolean flag
     */
    private boolean m_hierarchical = false;

    /**
     * Private constructor.
     *
     * @param pattern      the pattern
     * @param hierarchical boolean flag
     */
    TypePattern(final String pattern, final boolean hierarchical)
    {
        m_pattern = pattern;
        m_hierarchical = hierarchical;
        escape(m_pattern);
    }

    /**
     * Matches a type name.
     *
     * @param typeName the name of the type
     * @return true if we have a matche
     */
    public boolean matches(final String typeName)
    {
        if (typeName == null)
        {
            throw new IllegalArgumentException("type name can not be null");
        }

        if (typeName.equals(""))
        {
            return false;
        }

        return m_typeNamePattern.contains(typeName);
    }

    /**
     * Checks if the pattern is hierarchical.
     *
     * @return boolean
     */
    public boolean isHierarchical()
    {
        return m_hierarchical;
    }

    /**
     * Checks if the pattern matches all types.
     *
     * @return boolean
     */
    public boolean isEagerWildCard()
    {
        return m_pattern.equals(EAGER_WILDCARD);
    }

    /**
     * Returns the pattern as a string.
     *
     * @return the pattern
     */
    public String getPattern()
    {
        return m_pattern;
    }

    /**
     * Sets the pattern as hierarchical.
     *
     * @param hierarchical
     */
    public void setHierarchical(boolean hierarchical)
    {
        m_hierarchical = hierarchical;
    }

    /**
     * Escapes the type pattern.
     *
     * @param pattern the method pattern
     */
    protected void escape(final String pattern)
    {
        String typeName = pattern;

        if (ABBREVIATIONS.containsKey(pattern))
        {
            typeName = (String) ABBREVIATIONS.get(pattern);
        }

        try
        {
            if (typeName.equals(REGULAR_WILDCARD)
                || typeName.equals(EAGER_WILDCARD))
            {
                typeName = "[a-zA-Z0-9_$.\\[\\]]+";
            }
            else
            {
                // CAUTION: order matters
                typeName = Strings.replaceSubString(typeName, "[", "\\[");
                typeName = Strings.replaceSubString(typeName, "]", "\\]");
                typeName = Strings.replaceSubString(typeName, "..",
                        "[a-zA-Z0-9_$.]+");
                typeName = Strings.replaceSubString(typeName, ".", "\\.");
                typeName = Strings.replaceSubString(typeName, "*",
                        "[a-zA-Z0-9_$\\[\\]]*");
            }

            m_typeNamePattern = new com.karneim.util.collection.regex.Pattern(typeName);
        }
        catch (Throwable e)
        {
            throw new ExpressionException("type pattern is not well formed: "
                + pattern, e);
        }
    }

    /**
     * Provides custom deserialization.
     *
     * @param stream the object input stream containing the serialized object
     * @throws Exception in case of failure
     */
    private void readObject(final ObjectInputStream stream)
        throws Exception
    {
        ObjectInputStream.GetField fields = stream.readFields();

        m_pattern = (String) fields.get("m_pattern", null);
        escape(m_pattern);
    }

    public int hashCode()
    {
        int result = 17;

        result = (37 * result) + hashCodeOrZeroIfNull(m_pattern);
        result = (37 * result) + hashCodeOrZeroIfNull(m_typeNamePattern);

        return result;
    }

    protected static int hashCodeOrZeroIfNull(final Object o)
    {
        if (null == o)
        {
            return 19;
        }

        return o.hashCode();
    }

    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (!(o instanceof TypePattern))
        {
            return false;
        }

        final TypePattern obj = (TypePattern) o;

        return areEqualsOrBothNull(obj.m_pattern, this.m_pattern)
        && areEqualsOrBothNull(obj.m_typeNamePattern, this.m_typeNamePattern);
    }

    protected static boolean areEqualsOrBothNull(final Object o1,
        final Object o2)
    {
        if (null == o1)
        {
            return (null == o2);
        }

        return o1.equals(o2);
    }
}
