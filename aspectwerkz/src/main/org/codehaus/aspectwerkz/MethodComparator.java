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
package org.codehaus.aspectwerkz;

import java.util.Comparator;
import java.util.StringTokenizer;
import java.lang.reflect.Method;

import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.transform.TransformationUtil;
import org.codehaus.aspectwerkz.metadata.MethodMetaData;

/**
 * Compares Methods. To be used when sorting methods.
 * Based on code by Bob Lee (crazybob@crazybob.org)
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: MethodComparator.java,v 1.6 2003-07-08 19:35:02 jboner Exp $
 */
public final class MethodComparator implements java.util.Comparator {

    /**
     * Defines the type of comparator.
     */
    private final int m_type;

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
                return compareNormal((Method)o1, (Method)o2);
            case PREFIXED_METHOD:
                return comparePrefixed((Method)o1, (Method)o2);
            case METHOD_META_DATA:
                return compareMethodMetaData((MethodMetaData)o1, (MethodMetaData)o2);
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
            if (m1.equals(m2)) return 0;

            final String m1Name = m1.getName();
            final String m2Name = m2.getName();

            if (!m1Name.equals(m2Name)) {
                return m1Name.compareTo(m2Name);
            }
            final Class[] args1 = m1.getParameterTypes();
            final Class[] args2 = m2.getParameterTypes();
            if (args1.length < args2.length) return -1;
            if (args1.length > args2.length) return 1;
            for (int i = 0; i < args1.length; i++) {
                int result = args1[i].getName().compareTo(args2[i].getName());
                if (result != 0) return result;
            }
        } catch (Throwable e) {
            throw new WrappedRuntimeException(e);
        }
        throw new Error("should be unreachable");
    }

    /**
     * Compares two prefixed methods.
     * Assumes the the prefixed methods looks like this: somePrefix$methodName
     *
     * @param m1
     * @param m2
     * @return int
     */
    private int comparePrefixed(final Method m1, final Method m2) {
        try {
            if (m1.equals(m2)) return 0;

            // compare only the original method names, i.e. remove the prefix and suffix
            final StringTokenizer m1Tokenizer = new StringTokenizer(
                    m1.getName(), TransformationUtil.DELIMITER);
            final StringTokenizer m2Tokenizer = new StringTokenizer(
                    m2.getName(), TransformationUtil.DELIMITER);
            m1Tokenizer.nextToken();
            m2Tokenizer.nextToken();

            final String m1Name = m1Tokenizer.nextToken();
            final String m2Name = m2Tokenizer.nextToken();

            if (!m1Name.equals(m2Name)) {
                return m1Name.compareTo(m2Name);
            }
            final Class[] args1 = m1.getParameterTypes();
            final Class[] args2 = m2.getParameterTypes();
            if (args1.length < args2.length) return -1;
            if (args1.length > args2.length) return 1;
            for (int i = 0; i < args1.length; i++) {
                int result = args1[i].getName().compareTo(args2[i].getName());
                if (result != 0) return result;
            }
        } catch (Throwable e) {
            throw new WrappedRuntimeException(e);
        }
        throw new Error("should be unreachable");
    }

    /**
     * Compares two methods meta-data.
     *
     * @param m1
     * @param m2
     * @return int
     */
    private int compareMethodMetaData(final MethodMetaData m1,
                                      final MethodMetaData m2) {
        try {
            if (m1.equals(m2)) return 0;

            final String m1Name = m1.getName();
            final String m2Name = m2.getName();

            if (!m1Name.equals(m2Name)) {
                return m1Name.compareTo(m2Name);
            }
            final String[] args1 = m1.getParameterTypes();
            final String[] args2 = m2.getParameterTypes();
            if (args1.length < args2.length) return -1;
            if (args1.length > args2.length) return 1;
            for (int i = 0; i < args1.length; i++) {
                int result = args1[i].compareTo(args2[i]);
                if (result != 0) return result;
            }
        } catch (Throwable e) {
            throw new WrappedRuntimeException(e);
        }
        throw new Error("should be unreachable");
    }

    /**
     * Sets the type.
     *
     * @param type the type
     */
    private MethodComparator(final int type) {
        m_type = type;
    }
}
