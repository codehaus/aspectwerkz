/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform;

import java.util.Comparator;

import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import javassist.CtMethod;
import javassist.CtClass;

/**
 * Compares Javassist Methods.
 * <p/>
 * Based on code by Bob Lee (crazybob@crazybob.org)
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public final class JavassistMethodComparator implements Comparator {

    /**
     * The sole instance.
     */
    private static final Comparator s_soleInstance = new JavassistMethodComparator();

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
        return compare((CtMethod)o1, (CtMethod)o2);
    }

    /**
     * Compares two methods
     *
     * @param m1
     * @param m2
     * @return int
     */
    private int compare(final CtMethod m1, final CtMethod m2) {
        try {
            if (!m1.getName().equals(m2.getName())) {
                return m1.getName().compareTo(m2.getName());
            }
            final CtClass[] args1 = m1.getParameterTypes();
            final CtClass[] args2 = m2.getParameterTypes();
            if (args1.length < args2.length) return -1;
            if (args1.length > args2.length) return 1;
            for (int i = 0; i < args1.length; i++) {
                int result = args1[i].getName().compareTo(args2[i].getName());
                if (result != 0) return result;
            }
        }
        catch (Throwable e) {
            throw new WrappedRuntimeException(e);
        }
        throw new Error("classes can only be transformed once");
    }
}
