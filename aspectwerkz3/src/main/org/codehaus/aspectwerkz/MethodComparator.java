/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz;

import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.MethodInfo;
import org.codehaus.aspectwerkz.reflect.TypeConverter;
import org.codehaus.aspectwerkz.transform.TransformationUtil;
import org.codehaus.aspectwerkz.transform.TransformationConstants;
import org.codehaus.aspectwerkz.transform.TransformationConstants;
import org.codehaus.aspectwerkz.util.Strings;

import java.lang.reflect.Method;
import java.util.Comparator;

/**
 * Compares Methods. To be used when sorting methods.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r </a>
 */
public final class MethodComparator implements java.util.Comparator {
    /**
     * Compares normal method names.
     */
    public static final int NORMAL_METHOD = 0;

    /**
     * Compares prefixed method names.
     */
    public static final int PREFIXED_METHOD = 1;

    /**
     * Compares method meta-data.
     */
    public static final int METHOD_META_DATA = 2;

    /**
     * Defines the type of comparator.
     */
    private final int m_type;

    /**
     * Sets the type.
     *
     * @param type the type
     */
    private MethodComparator(final int type) {
        m_type = type;
    }

    /**
     * Returns the comparator instance.
     *
     * @param type the type of the method comparison
     * @return the instance
     */
    public static Comparator getInstance(final int type) {
        return new MethodComparator(type);
    }

    /**
     * Compares two objects.
     *
     * @param o1
     * @param o2
     * @return int
     */
    public int compare(final Object o1, final Object o2) {
        switch (m_type) {
            case NORMAL_METHOD:
                return compareNormal((Method) o1, (Method) o2);
            case PREFIXED_METHOD:
                return comparePrefixed((Method) o1, (Method) o2);
            case METHOD_META_DATA:
                return compareMethodInfo((MethodInfo) o1, (MethodInfo) o2);
            default:
                throw new RuntimeException("invalid method comparison type");
        }
    }

    /**
     * Compares two methods.
     *
     * @param m1
     * @param m2
     * @return int
     */
    private int compareNormal(final Method m1, final Method m2) {
        try {
            if (m1.equals(m2)) {
                return 0;
            }
            final String m1Name = m1.getName();
            final String m2Name = m2.getName();
            if (!m1Name.equals(m2Name)) {
                return m1Name.compareTo(m2Name);
            }
            final Class[] args1 = m1.getParameterTypes();
            final Class[] args2 = m2.getParameterTypes();
            if (args1.length < args2.length) {
                return -1;
            }
            if (args1.length > args2.length) {
                return 1;
            }
            if (args1.length == 0) {
                return 0;
            }
            for (int i = 0; i < args1.length; i++) {
                //handles array types - AW-104
                int result = TypeConverter.convertTypeToJava(args1[i]).compareTo(
                        TypeConverter.convertTypeToJava(args2[i])
                );
                if (result != 0) {
                    return result;
                }
            }
        } catch (Throwable e) {
            throw new WrappedRuntimeException(e);
        }
        java.lang.System.err.println(m1.getName());
        java.lang.System.err.println(m2.getName());
        throw new Error("should be unreachable");
    }

    /**
     * Compares two prefixed methods. Assumes the the prefixed methods looks like this: "somePrefix SEP methodName SEP"
     *
     * @param m1
     * @param m2
     * @return int
     */
    private int comparePrefixed(final Method m1, final Method m2) {
        try {
            if (m1.equals(m2)) {
                return 0;
            }

            // compare only the original method names, i.e. remove the prefix and suffix
            final String[] m1Tokens = Strings.splitString(m1.getName(), TransformationConstants.DELIMITER);
            final String[] m2Tokens = Strings.splitString(m2.getName(), TransformationConstants.DELIMITER);
            final String m1Name = m1Tokens[1];
            final String m2Name = m2Tokens[1];
            if (!m1Name.equals(m2Name)) {
                return m1Name.compareTo(m2Name);
            }
            final Class[] args1 = m1.getParameterTypes();
            final Class[] args2 = m2.getParameterTypes();
            if (args1.length < args2.length) {
                return -1;
            }
            if (args1.length > args2.length) {
                return 1;
            }
            if (args1.length == 0) {
                return 0;
            }
            for (int i = 0; i < args1.length; i++) {
                //handles array types - AW-104
                int result = TypeConverter.convertTypeToJava(args1[i]).compareTo(
                        TypeConverter.convertTypeToJava(args2[i])
                );
                if (result != 0) {
                    return result;
                }
            }
        } catch (Throwable e) {
            throw new WrappedRuntimeException(e);
        }
        java.lang.System.err.println(m1.getName());
        java.lang.System.err.println(m2.getName());
        throw new Error("should be unreachable");
    }

    /**
     * Compares two methods meta-data.
     *
     * @param m1
     * @param m2
     * @return int
     */
    private int compareMethodInfo(final MethodInfo m1, final MethodInfo m2) {
        try {
            if (m1.equals(m2)) {
                return 0;
            }
            final String m1Name = m1.getName();
            final String m2Name = m2.getName();
            if (!m1Name.equals(m2Name)) {
                return m1Name.compareTo(m2Name);
            }
            final ClassInfo[] args1 = m1.getParameterTypes();
            final ClassInfo[] args2 = m2.getParameterTypes();
            if (args1.length < args2.length) {
                return -1;
            }
            if (args1.length > args2.length) {
                return 1;
            }
            if (args1.length == 0) {
                return 0;
            }
            for (int i = 0; i < args1.length; i++) {
                if (args1[i].getName().equals(args2[i].getName())) {
                    return 0;
                }
            }
        } catch (Throwable e) {
            throw new WrappedRuntimeException(e);
        }
        java.lang.System.err.println(m1.getName());
        java.lang.System.err.println(m2.getName());
        throw new Error("should be unreachable");
    }
}