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
import org.codehaus.aspectwerkz.metadata.ConstructorMetaData;
import org.codehaus.aspectwerkz.metadata.MemberMetaData;
import org.codehaus.aspectwerkz.definition.SystemDefinition;

/**
 * Implements the regular expression pattern matcher for caller side methods in AspectWerkz.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @TODO remove when we have the 'within' construct
 */
public class CallerSidePattern extends Pattern {

    /**
     * The full pattern as a string.
     */
    protected final String m_pattern;

    /**
     * The pattern type.
     */
    protected final int m_type;

    /**
     * The caller class pattern part of the pattern.
     */
    protected ClassPattern m_callerClassPattern;

    /**
     * The callee class pattern part of the pattern.
     */
    protected ClassPattern m_calleeClassPattern;

    /**
     * The member pattern part of the pattern.
     */
    protected Pattern m_memberPattern;

    /**
     * Matches a caller side pointcut.
     *
     * @param className the class name
     * @return true if we have a matches
     */
    public boolean matches(final String className) {
        return m_calleeClassPattern.matches(className);
    }

    /**
     * Matches a caller side pointcut.
     *
     * @param className      the class name
     * @param methodMetaData the method meta-data
     * @return true if we have a matches
     */
    public boolean matches(final MethodMetaData methodMetaData) {
        if (!(m_memberPattern instanceof MethodPattern)) {
            return false;
        }
        return ((MethodPattern) m_memberPattern).matches(methodMetaData);
    }

    /**
     * Matches a caller side pointcut.
     *
     * @param className           the class name
     * @param constructorMetaData the constructor meta-data
     * @return true if we have a matches
     */
    public boolean matches(final ConstructorMetaData constructorMetaData) {
        if (!(m_memberPattern instanceof ConstructorPattern)) {
            return false;
        }
        return ((ConstructorPattern) m_memberPattern).matches(constructorMetaData);
    }

    /**
     * Matches a caller side pointcut.
     *
     * @param className      the class name
     * @param memberMetaData the method meta-data
     * @return true if we have a matches
     */
    public boolean matches(final String className, final MemberMetaData memberMetaData) {
        if (memberMetaData instanceof MethodMetaData) {
            return m_calleeClassPattern.matches(className) && matches((MethodMetaData) memberMetaData);
        } else if (memberMetaData instanceof ConstructorMetaData) {
            return m_calleeClassPattern.matches(className) && matches((ConstructorMetaData) memberMetaData);
        } else {
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
        StringTokenizer tokenizer = new StringTokenizer(m_pattern, SystemDefinition.CALLER_SIDE_DELIMITER);
        try {
            String classPattern = tokenizer.nextToken();
            String memberPattern = tokenizer.nextToken();
            m_calleeClassPattern = Pattern.compileClassPattern(classPattern);

            switch (m_type) {
                case Pattern.METHOD:
                    m_memberPattern = Pattern.compileMethodPattern(memberPattern);
                    break;
                case Pattern.CONSTRUCTOR:
                    m_memberPattern = Pattern.compileConstructorPattern(memberPattern);
                    break;
                case Pattern.FIELD:
                    // will probably never be implemented
                    break;
            }
        } catch (Exception e) {
            throw new DefinitionException("member pattern is not well formed: " + pattern, e);
        }
    }

    /**
     * Private constructor.
     *
     * @param type    the pattern type
     * @param pattern the pattern
     */
    CallerSidePattern(final int type, final String pattern) {
        m_pattern = pattern;
        m_type = type;
        parse(m_pattern);
    }
}
