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

import org.codehaus.aspectwerkz.definition.metadata.FieldMetaData;
import org.codehaus.aspectwerkz.exception.DefinitionException;

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
 * @version $Id: FieldPattern.java,v 1.2 2003-06-09 07:04:13 jboner Exp $
 */
public class FieldPattern extends Pattern {

    /**
     * The field name pattern.
     */
    protected java.util.regex.Pattern m_fieldNamePattern;

    /**
     * The field type pattern.
     */
    protected java.util.regex.Pattern m_fieldTypePattern;

    /**
     * The full pattern as a string.
     */
    protected String m_pattern;

    /**
     * Matches a field.
     *
     * @param field the field
     * @return true if we have a match
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
     * @return true if we have a match
     */
    public boolean matchFieldName(final String fieldName) {
        if (fieldName == null) throw new IllegalArgumentException("field name can not be null");
        if (fieldName.equals("")) return false;
        return m_fieldNamePattern.matcher(fieldName).matches();
    }

    /**
     * Matches a field type.
     *
     * @param fieldType the type of the field
     * @return true if we have a match
     */
    public boolean matchFieldType(final String fieldType) {
        if (fieldType == null) throw new IllegalArgumentException("field type can not be null");
        if (fieldType.equals("")) return false;
        return m_fieldTypePattern.matcher(fieldType).matches();
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
        m_pattern = pattern;
        try {
            parseFieldTypePattern(pattern);
            parseFieldNamePattern(pattern);
        }
        catch (Exception e) {
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
            fieldName = "\\b.*\\b";
        }
        else {
            fieldName = fieldName.replaceAll("\\*", "\\.*");
        }
        m_fieldNamePattern = java.util.regex.Pattern.compile(fieldName);
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
            fieldType = "\\b.*\\b";
        }
        else {
            fieldType = fieldType.replaceAll("\\.", "\\\\.");
            fieldType = fieldType.replaceAll("\\*", "\\.*");
        }
        m_fieldTypePattern = java.util.regex.Pattern.compile(fieldType);
    }

    /**
     * Private constructor.
     *
     * @param pattern the pattern
     */
    FieldPattern(final String pattern) {
        parse(pattern);
    }
}
