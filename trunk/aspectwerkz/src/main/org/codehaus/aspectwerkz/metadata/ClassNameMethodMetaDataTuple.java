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
package org.codehaus.aspectwerkz.metadata;

import org.codehaus.aspectwerkz.metadata.MethodMetaData;

/**
 * Holds a tuple that consists of the class name A the meta-data for a specific method.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: ClassNameMethodMetaDataTuple.java,v 1.2 2003-07-03 13:10:49 jboner Exp $
 */
public class ClassNameMethodMetaDataTuple {

    /**
     * The class name.
     */
    private final String m_className;

    /**
     * The method meta-data.
     */
    private final MethodMetaData m_methodMetaData;

    /**
     * Creates a new ClassNameMethodMetaDataTuple.
     *
     * @param className the class metaData
     * @param metaData the method meta-data
     */
    public ClassNameMethodMetaDataTuple(final String className,
                                        final MethodMetaData metaData) {
        m_className = className;
        m_methodMetaData = metaData;
    }

    /**
     * Returns the class name.
     *
     * @return the class name
     */
    public String getClassName() {
        return m_className;
    }

    /**
     * Returns the method meta-data.
     *
     * @return the method meta-data
     */
    public MethodMetaData getMethodMetaData() {
        return m_methodMetaData;
    }

    // --- over-ridden methods ---

    public String toString() {
        return "["
                + super.toString()
                + ": "
                + "," + m_className
                + "," + m_methodMetaData
                + "]";
    }

    public int hashCode() {
        int result = 17;
        result = 37 * result + hashCodeOrZeroIfNull(m_className);
        result = 37 * result + hashCodeOrZeroIfNull(m_methodMetaData);
        return result;
    }

    protected static int hashCodeOrZeroIfNull(final Object o) {
        if (null == o) return 19;
        return o.hashCode();
    }

    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof ClassNameMethodMetaDataTuple)) return false;
        final ClassNameMethodMetaDataTuple obj = (ClassNameMethodMetaDataTuple)o;
        return areEqualsOrBothNull(obj.m_className, this.m_className)
                && areEqualsOrBothNull(obj.m_methodMetaData, this.m_methodMetaData);
    }

    protected static boolean areEqualsOrBothNull(final Object o1, final Object o2) {
        if (null == o1) return (null == o2);
        return o1.equals(o2);
    }
}
