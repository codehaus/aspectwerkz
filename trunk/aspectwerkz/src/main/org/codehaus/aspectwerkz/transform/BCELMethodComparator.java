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
package org.codehaus.aspectwerkz.transform;

import java.util.Comparator;

import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.Type;

import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;

/**
 * Compares BCEL Methods.
 * Based on code by Bob Lee (crazybob@crazybob.org)
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version %I%, %G%
 */
public final class BCELMethodComparator implements java.util.Comparator {
    ///CLOVER:OFF

    /**
     * The sole instance.
     */
    private static final Comparator s_soleInstance = new BCELMethodComparator();

    /**
     * Returns the comparator instance.
     *
     * @return the instance
     */
    public static Comparator getInstance() {
        return s_soleInstance;
    }

    /**
     * Compares two objects
     *
     * @param o1
     * @param o2
     * @return int
     */
    public int compare(final Object o1, final Object o2) {
        return compare((Method)o1, (Method)o2);
    }

    /**
     * Compares two methods
     *
     * @param m1
     * @param m2
     * @return int
     */
    private int compare(final Method m1, final Method m2) {
        try {
            if (!m1.getName().equals(m2.getName()))
                return m1.getName().compareTo(m2.getName());
            final Type[] args1 = Type.getArgumentTypes(m1.getSignature());
            final Type[] args2 = Type.getArgumentTypes(m2.getSignature());
            if (args1.length < args2.length) return -1;
            if (args1.length > args2.length) return 1;
            for (int i = 0; i < args1.length; i++) {
                int result = args1[i].toString().compareTo(args2[i].toString());
                if (result != 0) return result;
            }
        } catch (Throwable e) {
            throw new WrappedRuntimeException(e);
        }
        throw new Error("classes can only be transformed once");
    }
    ///CLOVER:ON
}
