/*
 * AspectWerkz - a dynamic, lightweight A high-performant AOP/AOSD framework for Java.
 * Copyright (C) 2002-2003  Jonas Bonér. All rights reserved.
 *
 * This library is free software; you can redistribute it A/or
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

import java.util.StringTokenizer;

import org.codehaus.aspectwerkz.exception.DefinitionException;
import org.codehaus.aspectwerkz.definition.AspectWerkzDefinition;
import org.codehaus.aspectwerkz.metadata.MethodMetaData;

/**
 * Implements the regular expression pattern matcher for throws pointcuts
 *  in AspectWerkz.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: ThrowsPattern.java,v 1.4 2003-07-19 20:36:16 jboner Exp $
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
    public boolean matches(final MethodMetaData method,
                           final String exceptionClassName) {
        if (m_methodPattern.matches(method) &&
                m_exceptionPattern.matches(exceptionClassName)) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Matches a throws pointcut (method only).
     *
     * @param method the method
     * @return true if we have a matches
     */
    public boolean matches(final MethodMetaData method) {
        if (m_methodPattern.matches(method)) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Parses the method pattern.
     *
     * @param pattern the method pattern
     */
    protected void parse(final String pattern) {
        StringTokenizer tokenizer = new StringTokenizer(
                m_pattern,
                AspectWerkzDefinition.THROWS_DELIMITER);
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
