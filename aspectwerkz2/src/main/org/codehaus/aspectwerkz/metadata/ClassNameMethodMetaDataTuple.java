/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.metadata;

/**
 * Holds a tuple that consists of the class meta-data A the meta-data for a specific method. TODO: class should be
 * renamed (since cflow 0.9 fix)
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class ClassNameMethodMetaDataTuple {

    /**
     * The class name.
     */
    private final String m_className;

    /**
     * The class meta-data.
     */
    private ClassMetaData m_classMetaData;

    /**
     * The method meta-data.
     */
    private final MethodMetaData m_methodMetaData;

    /**
     * Creates a new ClassNameMethodMetaDataTuple.
     *
     * @param className the class metaData
     * @param metaData  the method meta-data ALEX RM
     */
    public ClassNameMethodMetaDataTuple(
            final String className,
            final MethodMetaData metaData) {
        m_className = className;
        m_methodMetaData = metaData;
    }

    public ClassNameMethodMetaDataTuple(
            final ClassMetaData classMetaData,
            final MethodMetaData metaData) {
        m_className = classMetaData.getName();
        m_classMetaData = classMetaData;
        m_methodMetaData = metaData;
    }

    /**
     * Returns the class meta-data.
     *
     * @return the class meta-data
     */
    public ClassMetaData getClassMetaData() {
        return m_classMetaData;
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
        return '['
               + super.toString()
               + ": "
               + ',' + m_className
               + ',' + m_methodMetaData
               + ']';
    }

    public int hashCode() {
        int result = 17;
        result = 37 * result + hashCodeOrZeroIfNull(m_className);
        result = 37 * result + hashCodeOrZeroIfNull(m_methodMetaData);
        return result;
    }

    protected static int hashCodeOrZeroIfNull(final Object o) {
        if (null == o) {
            return 19;
        }
        return o.hashCode();
    }

    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ClassNameMethodMetaDataTuple)) {
            return false;
        }
        final ClassNameMethodMetaDataTuple obj = (ClassNameMethodMetaDataTuple)o;
        return areEqualsOrBothNull(obj.m_className, this.m_className)
               && areEqualsOrBothNull(obj.m_methodMetaData, this.m_methodMetaData);
    }

    protected static boolean areEqualsOrBothNull(final Object o1, final Object o2) {
        if (null == o1) {
            return (null == o2);
        }
        return o1.equals(o2);
    }
}
