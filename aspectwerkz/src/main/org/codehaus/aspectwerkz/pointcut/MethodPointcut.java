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
package org.codehaus.aspectwerkz.pointcut;

import org.codehaus.aspectwerkz.definition.regexp.MethodPattern;
import org.codehaus.aspectwerkz.definition.regexp.Pattern;

/**
 * Implements the pointcut concept for method access.
 * Is an abstraction of a well defined point of execution in the program.<br/>
 * Could match one or many points as long as they are well defined.<br/>
 * Stores the advices for the specific pointcut.
 *
 * @author <a href="mailto:jboner@acm.org">Jonas Bonér</a>
 * @version $Id: MethodPointcut.java,v 1.1.1.1 2003-05-11 15:14:54 jboner Exp $
 */
public class MethodPointcut extends AbstractPointcut {

    /**
     * The pattern for this pointcut.
     */
    private final MethodPattern m_pattern;

    /**
     * Creates a new pointcut.
     * @todo is it better to pass the compiled pattern as a parameter from the definition?
     *
     * @param pattern the pattern for the pointcut
     */
    public MethodPointcut(final String pattern) {
        super(pattern);
        m_pattern = Pattern.compileMethodPattern(pattern);
    }

    /**
     * Creates a new pointcut.
     *
     * @param pattern the pattern of the pointcut
     * @param isThreadSafe the thread safe type
     */
    public MethodPointcut(final String pattern, final boolean isThreadSafe) {
        super(pattern, isThreadSafe);
        m_pattern = Pattern.compileMethodPattern(pattern);
    }

    /**
     * Returns a pre-compiled pattern for this pointcut.
     *
     * @return the pattern
     */
    public MethodPattern getPattern() {
        return m_pattern;
    }
}