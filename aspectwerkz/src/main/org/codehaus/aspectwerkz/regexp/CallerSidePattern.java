/*
 * AspectWerkz - a dynamic, lightweight and high-performant AOP/AOSD framework for Java.
 * Copyright (C) 2002-2003  Jonas Bon�r. All rights reserved.
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

import java.util.StringTokenizer;

import org.codehaus.aspectwerkz.exception.DefinitionException;
import org.codehaus.aspectwerkz.definition.AspectWerkzDefinition;
import org.codehaus.aspectwerkz.metadata.MethodMetaData;

/**
 * Implements the regular expression pattern matcher for caller side methods
 *  in AspectWerkz.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 * @version $Id: CallerSidePattern.java,v 1.1 2003-06-17 14:56:41 jboner Exp $
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
     * @param method the method
     * @return true if we have a matches
     */
    public boolean matches(final String className,
                           final MethodMetaData method) {
        if (m_classPattern.matches(className) &&
                m_methodPattern.matches(method)) {
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
        m_pattern = pattern;
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
        parse(pattern);
    }
}
