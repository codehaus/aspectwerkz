/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.util;

import org.codehaus.aspectwerkz.metadata.MetaData;

/**
 * Small utility methods used in the AspectWerkz system.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public final class Util {

    /**
     * Calculates the hash for the class name and the meta-data.
     *
     * @param className the class name
     * @param metaData the meta-data
     * @return the hash
     */
    public static Integer calculateHash(final String className, final MetaData metaData) {
        if (className == null) throw new IllegalArgumentException("class name can not be null");
        if (metaData == null) throw new IllegalArgumentException("meta-data can not be null");
        int hash = 17;
        hash = 37 * hash + className.hashCode();
        hash = 37 * hash + metaData.hashCode();
        Integer hashKey = new Integer(hash);
        return hashKey;
    }

    /**
     * Removes the AspectWerkz specific elements from the stack trace.
     *
     * @param exception the Throwable to modify the stack trace on
     * @param className the name of the fake origin class of the exception
     */
    public static void fakeStackTrace(final Throwable exception, final String className) {
        if (exception == null) throw new IllegalArgumentException("exception can not be null");
        if (className == null) throw new IllegalArgumentException("class name can not be null");

// TODO: how to mess w/ the stacktrace in JDK 1.3.x?

//        final List newStackTraceList = new ArrayList();
//        final StackTraceElement[] stackTrace = exception.getStackTrace();
//        int i;
//        for (i = 1; i < stackTrace.length; i++) {
//            if (stackTrace[i].getClassName().equals(className)) break;
//        }
//        for (int j = i; j < stackTrace.length; j++) {
//            newStackTraceList.add(stackTrace[j]);
//        }
//
//        final StackTraceElement[] newStackTrace =
//                new StackTraceElement[newStackTraceList.size()];
//        int k = 0;
//        for (Iterator it = newStackTraceList.iterator(); it.hasNext(); k++) {
//            final StackTraceElement element = (StackTraceElement)it.next();
//            newStackTrace[k] = element;
//        }
//        exception.setStackTrace(newStackTrace);
    }
}
