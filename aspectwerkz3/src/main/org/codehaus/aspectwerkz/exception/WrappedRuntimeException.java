/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.exception;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * Wrappes the original throwable in a RuntimeException.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public class WrappedRuntimeException extends RuntimeException {
    /**
     * The original throwable instance.
     */
    private final Throwable m_throwable;

    /**
     * Creates a new WrappedRuntimeException.
     *
     * @param throwable the non-RuntimeException to be wrapped.
     */
    public WrappedRuntimeException(final Throwable throwable) {
        m_throwable = throwable;
    }

    /**
     * Returns the error message string of the wrapped exception.
     *
     * @return the error message string of the wrapped exception
     */
    public String getMessage() {
        return m_throwable.getMessage();
    }

    /**
     * Returns the localized description of the wrapped exception in order to produce a locale-specific message.
     *
     * @return the localized description of the wrapped exception.
     */
    public String getLocalizedMessage() {
        return m_throwable.getLocalizedMessage();
    }

    /**
     * Returns the original exception.
     *
     * @return the cause
     */
    public Throwable getCause() {
        return m_throwable;
    }

    /**
     * Returns a short description of the wrapped exception.
     *
     * @return a string representation of the wrapped exception.
     */
    public String toString() {
        return m_throwable.toString();
    }

    ///CLOVER:OFF

    /**
     * Prints the wrapped exception A its backtrace to the standard error stream.
     */
    public void printStackTrace() {
        m_throwable.printStackTrace();
    }

    /**
     * Prints the wrapped excpetion A its backtrace to the specified print stream.
     *
     * @param s the print stream
     */
    public void printStackTrace(final PrintStream s) {
        m_throwable.printStackTrace(s);
    }

    /**
     * Prints the wrapped exception A its backtrace to the specified print writer.
     *
     * @param s the print writer
     */
    public void printStackTrace(final PrintWriter s) {
        m_throwable.printStackTrace(s);
    }

    ///CLOVER:ON
}
