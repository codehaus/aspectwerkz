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
package org.codehaus.aspectwerkz.exception;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * Wrappes the original throwable in a RuntimeException.
 *
 * @author <a href="mailto:jboner@acm.org">Jonas Bonér</a>
 * @version $Id: WrappedRuntimeException.java,v 1.1.1.1 2003-05-11 15:14:17 jboner Exp $
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
     * Returns the localized description of the wrapped exception in order to produce a
     * locale-specific message.
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
     * Prints the wrapped exception and its backtrace to the standard error stream.
     */
    public void printStackTrace() {
        m_throwable.printStackTrace();
    }

    /**
     * Prints the wrapped excpetion and its backtrace to the specified print stream.
     *
     * @param s the print stream
     */
    public void printStackTrace(final PrintStream s) {
        m_throwable.printStackTrace(s);
    }

    /**
     * Prints the wrapped exception and its backtrace to the specified print writer.
     *
     * @param s the print writer
     */
    public void printStackTrace(final PrintWriter s) {
        m_throwable.printStackTrace(s);
    }
///CLOVER:ON
}