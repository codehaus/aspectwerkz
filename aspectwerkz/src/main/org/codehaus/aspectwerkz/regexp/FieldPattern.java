/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.regexp;

import java.io.ObjectInputStream;

import org.codehaus.aspectwerkz.metadata.FieldMetaData;
import org.codehaus.aspectwerkz.exception.DefinitionException;
import org.codehaus.aspectwerkz.util.Strings;

/**
 * Implements the regular expression pattern matcher for fields in AspectWerkz.
 * <p/>
 * Example of supported patterns:
 * <pre>
 *      int m_field
 *      * m_field
 *      int m_*
 *      int m_*d
 *      * *
 *      java.lang.String m_field
 *      String m_field
 * </pre>
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class FieldPattern extends Pattern {

    /**
     * The field name pattern.
     */
    protected transient com.karneim.util.collection.regex.Pattern m_fieldNamePattern;

    /**
     * The field type pattern.
     */
    protected transient com.karneim.util.collection.regex.Pattern m_fieldTypePattern;

    /**
     * The full pattern as a string.
     */
    protected String m_pattern;

    /**
     * Matches a field.
     *
     * @param field the field
     * @return true if we have a matches
     */
    public boolean matches(final FieldMetaData field) {
        if (!matchFieldName(field.getName())) {
            return false;
        }
        if (!matchFieldType(field.getType())) {
            return false;
        }
        return true;
    }

    /**
     * Matches a field name.
     *
     * @param fieldName the name of the field
     * @return true if we have a matches
     */
    public boolean matchFieldName(final String fieldName) {
        if (fieldName == null) throw new IllegalArgumentException("field name can not be null");
        if (fieldName.equals("")) return false;
        return m_fieldNamePattern.contains(fieldName);
    }

    /**
     * Matches a field type.
     *
     * @param fieldType the type of the field
     * @return true if we have a matches
     */
    public boolean matchFieldType(final String fieldType) {
        if (fieldType == null) throw new IllegalArgumentException("field type can not be null");
        if (fieldType.equals("")) return false;
        return m_fieldTypePattern.contains(fieldType);
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
     * Parses the field pattern.
     *
     * @param pattern the field pattern
     */
    protected void parse(final String pattern) {
        try {
            parseFieldTypePattern(pattern);
            parseFieldNamePattern(pattern);
        }
        catch (Throwable e) {
            throw new DefinitionException("field pattern is not well formed: " + pattern);
        }
    }

    /**
     * Parses the field name pattern.
     *
     * @param pattern the pattern
     */
    protected void parseFieldNamePattern(final String pattern) {
        final int startIndexFieldName = pattern.indexOf(' ') + 1;
        String fieldName = pattern.substring(startIndexFieldName, pattern.length());
        if (fieldName.equals(SINGLE_WILDCARD)) {
            fieldName = "[a-zA-Z0-9_$]*";
        }
        else {
            fieldName = Strings.replaceSubString(fieldName, "*", "[a-zA-Z0-9_$]*");
        }
        m_fieldNamePattern = new com.karneim.util.collection.regex.Pattern(fieldName);
    }

    /**
     * Parses the field type pattern.
     *
     * @param pattern the pattern
     */
    protected void parseFieldTypePattern(final String pattern) {
        final int endIndexFieldType = pattern.indexOf(' ');
        String fieldType = pattern.substring(0, endIndexFieldType);
        if (m_abbreviations.containsKey(fieldType)) {
            fieldType = (String)m_abbreviations.get(fieldType);
        }
        if (fieldType.equals(SINGLE_WILDCARD)) {
            fieldType = "[a-zA-Z0-9_$.]+";
        }
        else {
            fieldType = Strings.replaceSubString(fieldType, ".", "\\.");
            fieldType = Strings.replaceSubString(fieldType, "[", "\\[");
            fieldType = Strings.replaceSubString(fieldType, "]", "\\]");
            fieldType = Strings.replaceSubString(fieldType, "*", "[a-zA-Z0-9_$]*");
        }
        m_fieldTypePattern = new com.karneim.util.collection.regex.Pattern(fieldType);
    }

    /**
     * Private constructor.
     *
     * @param pattern the pattern
     */
    FieldPattern(final String pattern) {
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
        result = 37 * result + hashCodeOrZeroIfNull(m_fieldNamePattern);
        result = 37 * result + hashCodeOrZeroIfNull(m_fieldTypePattern);
        result = 37 * result + hashCodeOrZeroIfNull(m_abbreviations);
        return result;
    }

    protected static int hashCodeOrZeroIfNull(final Object o) {
        if (null == o) return 19;
        return o.hashCode();
    }

    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof MethodPattern)) return false;
        final FieldPattern obj = (FieldPattern)o;
        return areEqualsOrBothNull(obj.m_pattern, this.m_pattern)
                && areEqualsOrBothNull(obj.m_fieldNamePattern, this.m_fieldNamePattern)
                && areEqualsOrBothNull(obj.m_fieldTypePattern, this.m_fieldTypePattern)
                && areEqualsOrBothNull(obj.m_abbreviations, this.m_abbreviations);
    }

    protected static boolean areEqualsOrBothNull(final Object o1, final Object o2) {
        if (null == o1) return (null == o2);
        return o1.equals(o2);
    }
}
