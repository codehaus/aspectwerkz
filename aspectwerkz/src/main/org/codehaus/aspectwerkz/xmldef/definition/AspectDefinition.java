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
import java.io.Serializable;

import org.codehaus.aspectwerkz.definition.regexp.ClassPattern;
import org.codehaus.aspectwerkz.definition.regexp.Pattern;

/**
 * Holds the aspect definition.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: AspectDefinition.java,v 1.3 2003-06-09 07:04:13 jboner Exp $
 */
public class AspectDefinition implements Serializable {

    /**
     * The class pattern for this aspect.
     */
    private String m_pattern;

    /**
     * The introductions for this aspect.
     */
    private final List m_introductions = new ArrayList();

    /**
     * The pointcuts for this aspect.
     */
    private final List m_pointcuts = new ArrayList();

    /**
     * A pre-compiled regexp pattern for this aspect.
     */
    private ClassPattern m_regexp;

    /**
     * Returns the pattern for the aspect
     * @return the pattern
     */
    public String getPattern() {
        return m_pattern;
    }

    /**
     * Sets the pattern for the aspect.
     *
     * @param pattern the pattern
     */
    public void setPattern(final String pattern) {
        m_pattern = pattern.trim();
    }

    /**
     * Returns the introduction names as a list.
     *
     * @return the introduction names
     */
    public List getIntroductions() {
        return m_introductions;
    }

    /**
     * Adds a new introduction.
     *
     * @param introduction the introduction to add
     */
    public void addIntroduction(final String introduction) {
        m_introductions.add(introduction.trim());
    }

    /**
     * Returns a list with the pointcuts.
     *
     * @return the pointcuts
     */
    public List getPointcuts() {
        return m_pointcuts;
    }

    /**
     * Adds a new pointcut.
     *
     * @param pointcut a pointcut
     */
    public void addPointcut(final PointcutDefinition pointcut) {
        if (pointcut.getType().equalsIgnoreCase(
                PointcutDefinition.CALLER_SIDE)) {
            pointcut.setCallerSidePattern(m_pattern);
        }
        m_pointcuts.add(pointcut);
    }

    /**
     * Returns a pre-compiled ClassPattern instance.
     *
     * @return a pre-compiled ClassPattern instance
     */
    public ClassPattern getRegexpPattern() {
        if (m_regexp == null) {
            m_regexp = Pattern.compileClassPattern(m_pattern);
        }
        return m_regexp;
    }
}
