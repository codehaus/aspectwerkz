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
package org.codehaus.aspectwerkz.definition;

import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Iterator;
import java.io.Serializable;

import org.codehaus.aspectwerkz.definition.regexp.Pattern;

/**
 * Holds the pointcut definition.
 *
 * @author <a href="mailto:jboner@acm.org">Jonas Bonér</a>
 * @version $Id: PointcutDefinition.java,v 1.1.1.1 2003-05-11 15:13:56 jboner Exp $
 */
public class PointcutDefinition implements Serializable {

    public static final String METHOD = "method";
    public static final String GET_FIELD = "getfield";
    public static final String SET_FIELD = "setfield";
    public static final String THROWS = "throws";
    public static final String CALLER_SIDE = "callerside";

    /**
     * The type for the pointcut.
     */
    private String m_type;

    /**
     * The pattern the pointcut should match.
     */
    private List m_patterns = new ArrayList();

    /**
     * If we have a caller side pointcut; the caller side pattern.
     */
    private String m_callerSidePattern;

    /**
     * The advices for this pointcut.
     */
    private final List m_advices = new ArrayList();

    /**
     * The advice stacks for this pointcut.
     */
    private final List m_adviceStacks = new ArrayList();

    /**
     * Marks the pointcut as thread-safe.
     */
    private String m_threadSafe;

    /**
     *
     * A a list with pre-compiled regexp patterns for this pointcut.
     */
    private List m_regexps = null;

    /**
     * Returns the type of the pointcut.
     *
     * @return the type
     */
    public String getType() {
        return m_type;
    }

    /**
     * Sets the type of the pointcut.
     *
     * @param type the type
     */
    public void setType(final String type) {
        m_type = type.trim();
    }

    /**
     * Returns the pattern for the pointcut.
     *
     * @return the pattern
     */
    public List getPatterns() {
        return m_patterns;
    }

    /**
     * Adds a pattern for the pointcut.
     *
     * @param pattern the pattern
     */
    public void addPattern(final String pattern) {
        m_patterns.add(pattern.trim());
    }

    /**
     * Returns the caller side pattern for the pointcut.
     *
     * @return the pattern
     */
    public String getCallerSidePattern() {
        return m_callerSidePattern;
    }

    /**
     * Sets the caller side pattern for the pointcut.
     *
     * @param pattern the pattern
     */
    public void setCallerSidePattern(final String pattern) {
        m_callerSidePattern = pattern.trim();
    }

    /**
     * Returns the name of the advices as list.
     *
     * @return the name of the advices
     */
    public List getAdvices() {
        return m_advices;
    }

    /**
     * Adds the name of an advice.
     *
     * @param advice the names of the advice
     */
    public void addAdvice(final String advice) {
        m_advices.add(advice.trim());
    }

    /**
     * Returns the advice stacks.
     *
     * @return the advice stacks
     */
    public List getAdviceStacks() {
        return m_adviceStacks;
    }

    /**
     * Adds an advice stack.
     *
     * @param adviceStack the advice stack
     */
    public void addAdviceStack(final String adviceStack) {
        m_adviceStacks.add(adviceStack.trim());
    }

    /**
     * Returns the threadSafe attribute.
     *
     * @return the threadSafe parameter
     */
    public String getIsThreadSafe() {
        return m_threadSafe;
    }

    /**
     * Sets the threadSafe attribute.
     *
     * @param threadSafe marks the pointcut thread-safe or not
     */
    public void setThreadSafe(final String threadSafe) {
        m_threadSafe = threadSafe.trim();
    }

    /**
     * Checks if the pointcut is thread-safe.
     *
     * @return true if the pointcut is thread-safe
     */
    public boolean isThreadSafe() {
        if (m_threadSafe != null &&
                (m_threadSafe.equalsIgnoreCase("no") ||
                m_threadSafe.equalsIgnoreCase("false"))) {
            return false;
        }
        else {
            return true;
        }
    }

    /**
     * Returns list of pre-compiled Pattern instances.
     *
     * @return a list of pre-compiled Pattern instances
     */
    public List getRegexpPatterns() {
        if (m_regexps == null) {
            m_regexps = new ArrayList(m_patterns.size());

            if (m_type.equalsIgnoreCase(METHOD)) {
                for (Iterator it = m_patterns.iterator(); it.hasNext();) {
                    m_regexps.add(Pattern.compileMethodPattern((String)it.next()));
                }
            }
            else if (m_type.equalsIgnoreCase(GET_FIELD)) {
                for (Iterator it = m_patterns.iterator(); it.hasNext();) {
                    m_regexps.add(Pattern.compileFieldPattern((String)it.next()));
                }
            }
            else if (m_type.equalsIgnoreCase(SET_FIELD)) {
                for (Iterator it = m_patterns.iterator(); it.hasNext();) {
                    m_regexps.add(Pattern.compileFieldPattern((String)it.next()));
                }
            }
            else if (m_type.equalsIgnoreCase(THROWS)) {
                for (Iterator it = m_patterns.iterator(); it.hasNext();) {
                    final StringTokenizer tokenizer = new StringTokenizer(
                            (String)it.next(),
                            AspectWerkzDefinition.THROWS_DELIMITER);
                    final String method = tokenizer.nextToken();
                    final String exception = tokenizer.nextToken();
                    m_regexps.add(Pattern.compileMethodPattern(method));
                }
            }
            else if (m_type.equalsIgnoreCase(CALLER_SIDE)) {
                for (Iterator it = m_patterns.iterator(); it.hasNext();) {
                    final StringTokenizer tokenizer = new StringTokenizer(
                            (String)it.next(),
                            AspectWerkzDefinition.CALLER_SIDE_DELIMITER);
                    String classNamePattern = tokenizer.nextToken();
                    String methodNamePattern = tokenizer.nextToken();
                    m_regexps.add(Pattern.compileMethodPattern(methodNamePattern));
                }
            }
            else {
                throw new IllegalStateException("pointcut has an undefined type: " + m_type);
            }
        }
        return m_regexps;
    }
}
