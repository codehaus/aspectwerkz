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
package org.codehaus.aspectwerkz.pointcut;

import java.util.StringTokenizer;

import org.codehaus.aspectwerkz.definition.regexp.MethodPattern;
import org.codehaus.aspectwerkz.definition.regexp.ClassPattern;
import org.codehaus.aspectwerkz.definition.regexp.Pattern;
import org.codehaus.aspectwerkz.definition.AspectWerkzDefinition;
import org.codehaus.aspectwerkz.AspectWerkz;

/**
 * Implements the pointcut concept for exception handling.
 * Is an abstraction of a well defined point of execution in the program.<br/>
 * Could match one or many points as long as they are well defined.<br/>
 * Stores the advices for this specific pointcut.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 * @version $Id: ThrowsPointcut.java,v 1.2 2003-06-09 07:04:13 jboner Exp $
 */
public class ThrowsPointcut extends AbstractPointcut {

    /**
     * The pattern for the method for this pointcut.
     */
    private final MethodPattern m_methodPattern;

    /**
     * The pattern for the exception for this pointcut.
     */
    private final ClassPattern m_classPattern;

    /**
     * Creates a new pointcut.
     *
     * @param throwsPattern the throws pattern
     */
    public ThrowsPointcut(final String throwsPattern) {
        this(AspectWerkz.DEFAULT_SYSTEM, throwsPattern, false);
    }

    /**
     * Creates a new pointcut.
     *
     * @param throwsPattern the throws pattern
     * @param isThreadSafe the thread safe type
     */
    public ThrowsPointcut(final String throwsPattern,
                          final boolean isThreadSafe) {
        this(AspectWerkz.DEFAULT_SYSTEM, throwsPattern, isThreadSafe);
   }

    /**
     * Creates a new pointcut.
     *
     * @param uuid the UUID for the AspectWerkz system
     * @param throwsPattern the throws pattern
     */
    public ThrowsPointcut(final String uuid, final String throwsPattern) {
        this(uuid, throwsPattern, false);
    }

    /**
     * Creates a new pointcut.
     *
     * @param uuid the UUID for the AspectWerkz system
     * @param throwsPattern the throws pattern
     * @param isThreadSafe the thread safe type
     */
    public ThrowsPointcut(final String uuid,
                          final String throwsPattern,
                          final boolean isThreadSafe) {
        super(uuid, throwsPattern, isThreadSafe);

        final StringTokenizer tokenizer = new StringTokenizer(
                throwsPattern,
                AspectWerkzDefinition.THROWS_DELIMITER);
        final String methodName = tokenizer.nextToken();
        final String exceptionName = tokenizer.nextToken();

        m_methodPattern = Pattern.compileMethodPattern(methodName);
        m_classPattern = Pattern.compileClassPattern(exceptionName);
   }

    /**
     * Returns a pre-compiled pattern for the method for this pointcut.
     *
     * @return the pattern
     */
    public MethodPattern getMethodPattern() {
        return m_methodPattern;
    }

    /**
     * Returns a pre-compiled pattern for the exception for this pointcut.
     *
     * @return the pattern
     */
    public ClassPattern getExceptionPattern() {
        return m_classPattern;
    }
}