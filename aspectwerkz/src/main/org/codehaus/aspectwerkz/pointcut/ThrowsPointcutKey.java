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
package org.codehaus.aspectwerkz.pointcut;

/**
 * Key class for the throws pointcut holds the method name A the exception name.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: ThrowsPointcutKey.java,v 1.3 2003-07-03 13:10:49 jboner Exp $
 */
public class ThrowsPointcutKey {

    /**
     * The name of the method for the throws pointcut.
     */
    private final String m_methodName;

    /**
     * The name of the exception class for the throws pointcut.
     */
    private final String m_exceptionName;

    /**
     * Sets the method name A the exception name.
     *
     * @param methodName
     * @param exceptionName
     */
    public ThrowsPointcutKey(final String methodName,
                             final String exceptionName) {
        if (methodName == null) throw new IllegalArgumentException("method name can not be null");
        if (exceptionName == null) throw new IllegalArgumentException("exception name can not be null");
        m_methodName = methodName;
        m_exceptionName = exceptionName;
    }

    /**
     * Returns the method name.
     *
     * @return the method name
     */
    public String getMethodName() {
        return m_methodName;
    }

    /**
     * Returns the excpetion name.
     *
     * @return the exception name
     */
    public String getExceptionName() {
        return m_exceptionName;
    }

    /**
     * Overrides toString
     *
     * @return
     */
    public String toString() {
        return m_methodName + ":" + m_exceptionName;
    }

    /**
     * Overrides equals.
     *
     * @param o
     * @return
     */
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof ThrowsPointcutKey)) return false;
        final ThrowsPointcutKey obj = (ThrowsPointcutKey)o;
        return areEqualsOrBothNull(obj.m_methodName, this.m_methodName)
                && areEqualsOrBothNull(obj.m_exceptionName, this.m_exceptionName);
    }

    /**
     * Helper method for equals
     *
     * @param o1
     * @param o2
     * @return
     */
    private static boolean areEqualsOrBothNull(final Object o1, final Object o2) {
        if (null == o1) return (null == o2);
        return o1.equals(o2);
    }

    /**
     * Overrides hashCode.
     *
     * @return
     */
    public int hashCode() {
        int result = 17;
        result = 37 * result + hashCodeOrZeroIfNull(m_methodName);
        result = 37 * result + hashCodeOrZeroIfNull(m_exceptionName);
        return result;
    }

    /**
     * Helper method for hashCode.
     *
     * @param o
     * @return
     */
    private static int hashCodeOrZeroIfNull(final Object o) {
        if (null == o) return 19;
        return o.hashCode();
    }
}
