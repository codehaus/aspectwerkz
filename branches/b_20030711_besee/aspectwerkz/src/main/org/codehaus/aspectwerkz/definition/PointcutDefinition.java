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
package org.codehaus.aspectwerkz.definition;

import java.io.Serializable;

import org.codehaus.aspectwerkz.regexp.Pattern;
import org.codehaus.aspectwerkz.regexp.ClassPattern;
import org.codehaus.aspectwerkz.regexp.PointcutPatternTuple;
import org.codehaus.aspectwerkz.exception.DefinitionException;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;

/**
 * Holds the pointcut definition.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: PointcutDefinition.java,v 1.7.2.1 2003-07-20 10:38:36 avasseur Exp $
 */
public class PointcutDefinition implements Serializable {

    public static final String METHOD = "method";
    public static final String GET_FIELD = "getfield";
    public static final String SET_FIELD = "setfield";
    public static final String THROWS = "throws";
    public static final String CALLER_SIDE = "callerside";
    public static final String CFLOW = "cflow";

    /**
     * The name of the pointcut.
     */
    private String m_name = null;

    /**
     * The type for the pointcut.
     */
    private String m_type = null;

    /**
     * The class pattern the pointcut should matches.
     */
    private String m_classPattern = null;

    /**
     * The pattern the pointcut should matches.
     */
    private String m_pattern = null;

    /**
     * A pre-compiled regexp pattern for this pointcut.
     */
    private Pattern m_regexpPattern = null;

    /**
     * A pre-compiled regexp class pattern for this pointcut.
     */
    private ClassPattern m_regexpClassPattern = null;

    /**
     * Hierarchical flag.
     */
    private boolean m_hierarchical = false;;

    /**
     * Returns the name of the pointcut.
     *
     * @return the name of the pointcut
     */
    public String getName() {
        return m_name;
    }

    /**
     * Sets the name of the pointcut.
     */
    public void setName(final String name) {
        m_name = name;
    }

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
     * Returns the class pattern for the pointcut.
     *
     * @return the class pattern
     */
    public String getClassPattern() {
        return m_classPattern;
    }

    /**
     * Adds a class pattern for the pointcut.
     *
     * @param pattern the class pattern
     */
    public void setClassPattern(final String classPattern) {
        m_classPattern = classPattern.trim();
        m_regexpClassPattern = Pattern.compileClassPattern(m_classPattern);
    }

    /**
     * Returns the pattern for the pointcut.
     *
     * @return the pattern
     */
    public String getPattern() {
        return m_pattern;
    }

    /**
     * Adds a pattern for the pointcut.
     *
     * @param pattern the pattern
     */
    public void setPattern(final String pattern) {
        m_pattern = pattern.trim();
    }

    /**
     * Returns a pre-compiled Pattern for the class pattern.
     *
     * @return a pre-compiled Pattern for the class pattern
     */
    public ClassPattern getRegexpClassPattern() {
        return m_regexpClassPattern;
    }

    /**
     * Returns a pre-compiled Pattern for the pattern.
     *
     * @return a pre-compiled Pattern for the pattern
     */
    public Pattern getRegexpPattern() {
        if (m_regexpPattern == null) {
            try {
                if (m_type.equalsIgnoreCase(METHOD)) {
                    m_regexpPattern = Pattern.compileMethodPattern(m_pattern);
                }
                else if (m_type.equalsIgnoreCase(GET_FIELD)) {
                    m_regexpPattern = Pattern.compileFieldPattern(m_pattern);
                }
                else if (m_type.equalsIgnoreCase(SET_FIELD)) {
                    m_regexpPattern = Pattern.compileFieldPattern(m_pattern);
                }
                else if (m_type.equalsIgnoreCase(THROWS)) {
                    m_regexpPattern = Pattern.compileThrowsPattern(m_pattern);
                }
                else if (m_type.equalsIgnoreCase(CALLER_SIDE)) {
                    m_regexpPattern = Pattern.compileCallerSidePattern(m_pattern);
                }
                else if (m_type.equalsIgnoreCase(CFLOW)) {
                    m_regexpPattern = Pattern.compileMethodPattern(m_pattern);
                }
                else {
                    throw new IllegalStateException("pointcut has an undefined type: " + m_type);
                }
            }
            catch (DefinitionException e) {
                throw new DefinitionException("pattern in pointcut definition <" + m_name + "> is not valid: " + m_pattern);
            }
            catch (Exception e) {
                throw new WrappedRuntimeException(e);
            }
        }
        return m_regexpPattern;
    }

    /**
     * Returns the pointcut pattern tuple for the pre-compiled class A method pattern.
     *
     * @return the pointcut pattern tuple
     */
    public PointcutPatternTuple getPointcutPatternTuple() {
        return new PointcutPatternTuple(
                getRegexpClassPattern(),
                getRegexpPattern(),
                m_hierarchical);
    }

    /**
     * Marks the pointcut as hierarchical.
     */
    public void markAsHierarchical() {
        m_hierarchical = true;
    }

    /**
     * Checks if the pointcut is hierarchical.
     *
     * @return the flag
     */
    public boolean isHierarchical() {
        return m_hierarchical;
    }
}
