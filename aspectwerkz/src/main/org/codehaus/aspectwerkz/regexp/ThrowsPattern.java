/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.regexp;

import java.util.StringTokenizer;

import org.codehaus.aspectwerkz.exception.DefinitionException;
import org.codehaus.aspectwerkz.metadata.MethodMetaData;
import org.codehaus.aspectwerkz.definition.AspectWerkzDefinition;

/**
 * Implements the regular expression pattern matcher for throws pointcuts in AspectWerkz.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class ThrowsPattern extends Pattern {

    /**
     * The full pattern as a string.
     */
    protected String m_pattern;

    /**
     * The method pattern part of the pattern.
     */
    protected MethodPattern m_methodPattern;

    /**
     * The exception pattern part of the pattern.
     */
    protected ClassPattern m_exceptionPattern;

    /**
     * Matches a throws pointcut.
     *
     * @param method the method
     * @param exceptionClassName the exception class name
     * @return true if we have a matches
     */
    public boolean matches(final MethodMetaData method, final String exceptionClassName) {
        return m_methodPattern.matches(method) && m_exceptionPattern.matches(exceptionClassName);
    }

    /**
     * Matches a throws pointcut (method only).
     *
     * @param method the method
     * @return true if we have a matches
     */
    public boolean matches(final MethodMetaData method) {
        return m_methodPattern.matches(method);
    }

    /**
     * Parses the method pattern.
     *
     * @param pattern the method pattern
     */
    protected void parse(final String pattern) {
        StringTokenizer tokenizer = new StringTokenizer(m_pattern, AspectWerkzDefinition.THROWS_DELIMITER);
        try {
            m_methodPattern = Pattern.compileMethodPattern(tokenizer.nextToken());
            m_exceptionPattern = Pattern.compileClassPattern(tokenizer.nextToken());
        }
        catch (Exception e) {
            throw new DefinitionException("method pattern is not well formed: " + pattern);
        }
    }

    /**
     * Creates a new throws pattern.
     *
     * @param pattern the pattern
     */
    ThrowsPattern(final String pattern) {
        m_pattern = pattern;
        parse(m_pattern);
    }
}
