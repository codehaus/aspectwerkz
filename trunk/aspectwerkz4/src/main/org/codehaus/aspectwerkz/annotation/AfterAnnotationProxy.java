/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.annotation;

import org.codehaus.aspectwerkz.exception.DefinitionException;
import org.codehaus.aspectwerkz.aspect.AdviceType;

/**
 * The 'After' annotation proxy.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public class AfterAnnotationProxy extends AdviceAnnotationProxyBase {

    public static final String RETURNING_PREFIX = "returning(";
    public static final String THROWING_PREFIX = "throwing(";
    public static final String FINALLY_PREFIX = "finally ";

    private String m_specialArgumentType;

    public AfterAnnotationProxy() {
        m_type = AdviceType.AFTER;
    }

    public void setValue(final String value) {
        if (value.startsWith(RETURNING_PREFIX)) {
            m_type = AdviceType.AFTER_RETURNING;
            int start = value.indexOf('(');
            int end = value.indexOf(')');
            m_specialArgumentType = value.substring(start + 1, end).trim();
            m_pointcut = value.substring(end + 1, value.length()).trim();
        } else if (value.startsWith(THROWING_PREFIX)) {
            m_type = AdviceType.AFTER_THROWING;
            int start = value.indexOf('(');
            int end = value.indexOf(')');
            m_specialArgumentType = value.substring(start + 1, end).trim();
            m_pointcut = value.substring(end + 1, value.length()).trim();
        } else if (value.startsWith(FINALLY_PREFIX)) {
            m_type = AdviceType.AFTER_FINALLY;
            m_pointcut = value.substring(value.indexOf(' ') + 1, value.length()).trim();
        } else {
            m_pointcut = value;
        }
        if (m_specialArgumentType != null && m_specialArgumentType.indexOf(' ') > 0) {
            throw new DefinitionException(
                    "argument to after (returning/throwing) can only be a type (parameter name binding should be done using args(..))"
            );
        }
    }

    /**
     * Returns the special argument type.
     *
     * @return
     */
    public String getSpecialArgumentType() {
        return m_specialArgumentType;
    }
}