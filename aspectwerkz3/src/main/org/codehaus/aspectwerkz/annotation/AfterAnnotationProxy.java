/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.annotation;

import java.io.Serializable;

/**
 * The 'After' annotation proxy.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public class AfterAnnotationProxy extends AdviceAnnotationProxyBase {

    public static final String RETURNING_PREFIX = "returning(";
    public static final String THROWING_PREFIX = "throwing(";
    public static final String FINALLY_PREFIX = "finally ";

    private AfterAnnotationType m_type = AfterAnnotationType.AFTER;
    private String m_argument;

    public void setValue(final String value) {
        if (value.startsWith(RETURNING_PREFIX)) {
            m_type = AfterAnnotationType.AFTER_RETURNING;
            int start = value.indexOf('(');
            int end = value.indexOf(')');
            m_argument = value.substring(start + 1, end).trim();
            m_pointcut = value.substring(end + 1, value.length()).trim();
            System.out.println("m_argument = " + m_argument);
            System.out.println("m_pointcut = " + m_pointcut);
        } else if (value.startsWith(THROWING_PREFIX)) {
            m_type = AfterAnnotationType.AFTER_THROWING;
            int start = value.indexOf('(');
            int end = value.indexOf(')');
            m_argument = value.substring(start + 1, end).trim();
            m_pointcut = value.substring(end + 1, value.length()).trim();
        } else if (value.startsWith(FINALLY_PREFIX)) {
            m_type = AfterAnnotationType.AFTER_FINALLY;
            m_pointcut = value.substring(value.indexOf(' ') + 1, value.length()).trim();
        } else {
            m_pointcut = value;
        }
    }

    /**
     * Returns the argument expression, f.e. "Throwable cause".
     *
     * @return
     */
    public String getArgument() {
        return m_argument;
    }

    /**
     * Returns the after annotation type.
     *
     * @return
     */
    public AfterAnnotationType getType() {
        return m_type;
    }


    /**
     * Type-safe enum for the after annotation types.
     *
     * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
     */
    public static class AfterAnnotationType implements Serializable {
        public static final AfterAnnotationType AFTER = new AfterAnnotationType("AFTER");
        public static final AfterAnnotationType AFTER_FINALLY = new AfterAnnotationType("AFTER_FINALLY");
        public static final AfterAnnotationType AFTER_RETURNING = new AfterAnnotationType("AFTER_RETURNING");
        public static final AfterAnnotationType AFTER_THROWING = new AfterAnnotationType("AFTER_THROWING");

        private final String m_name;

        private AfterAnnotationType(String name) {
            m_name = name;
        }

        public String toString() {
            return m_name;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof AfterAnnotationType)) {
                return false;
            }
            final AfterAnnotationType afterAnnotationType = (AfterAnnotationType) o;
            if ((m_name != null) ? (!m_name.equals(afterAnnotationType.m_name)) : (afterAnnotationType.m_name != null)) {
                return false;
            }
            return true;
        }

        public int hashCode() {
            return ((m_name != null) ? m_name.hashCode() : 0);
        }
    }
}