/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.regexp;

import java.util.StringTokenizer;

import org.codehaus.aspectwerkz.exception.DefinitionException;
import org.codehaus.aspectwerkz.definition.AspectWerkzDefinition;
import org.codehaus.aspectwerkz.metadata.MethodMetaData;

/**
 * Implements the regular expression pattern matcher for caller side methods
 *  in AspectWerkz.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class CallerSidePattern extends Pattern {

    /**
     * The full pattern as a string.
     */
    protected String m_pattern;

    /**
     * The class pattern part of the pattern.
     */
    protected ClassPattern m_classPattern;

    /**
     * The method pattern part of the pattern.
     */
    protected MethodPattern m_methodPattern;

    /**
     * Matches a caller side pointcut.
     *
     * @param className the class name
     * @return true if we have a matches
     */
    public boolean matches(final String className) {
        if (m_classPattern.matches(className)) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Matches a caller side pointcut.
     *
     * @param className the class name
     * @param methodMetaData the method meta-data
     * @return true if we have a matches
     */
    public boolean matches(final String className, final MethodMetaData methodMetaData) {
        if (m_classPattern.matches(className) && m_methodPattern.matches(methodMetaData)) {
            return true;
        }
        else {
            return false;
        }
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
     * Parses the method pattern.
     *
     * @param pattern the method pattern
     */
    protected void parse(final String pattern) {
        StringTokenizer tokenizer = new StringTokenizer(
                m_pattern,
                AspectWerkzDefinition.CALLER_SIDE_DELIMITER);
        try {
            m_classPattern = Pattern.compileClassPattern(tokenizer.nextToken());
            m_methodPattern = Pattern.compileMethodPattern(tokenizer.nextToken());
        }
        catch (Exception e) {
            throw new DefinitionException("method pattern is not well formed: " + pattern);
        }
    }

    /**
     * Private constructor.
     *
     * @param pattern the pattern
     */
    CallerSidePattern(final String pattern) {
        m_pattern = pattern;
        parse(m_pattern);
    }
}
