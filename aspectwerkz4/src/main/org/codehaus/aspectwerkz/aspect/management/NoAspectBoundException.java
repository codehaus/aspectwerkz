/*******************************************************************************
 * Copyright (c) 2005 BEA 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *    BEA - initial API and implementation
 *******************************************************************************/
package org.codehaus.aspectwerkz.aspect.management;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class NoAspectBoundException extends RuntimeException {

    private String m_message;
    private Throwable m_throwable;

    public NoAspectBoundException(String message, String aspectName) {
        m_message = message + " - " + aspectName;
    }

    public NoAspectBoundException(Throwable t, String aspectName) {
        m_throwable = t;
        m_message = t.getMessage();
    }

    public String getMessage() {
        StringBuffer sb = new StringBuffer("NoAspectBound: ");
        sb.append(m_message);
        return sb.toString();
    }

    /**
     * Returns the original exception.
     *
     * @return the cause
     */
    public Throwable getCause() {
        if (m_throwable != null) {
            return m_throwable;
        } else {
            return super.getCause();
        }
    }

    /**
     * Prints the wrapped exception A its backtrace to the standard error stream.
     */
    public void printStackTrace() {
        if (m_throwable != null) {
            m_throwable.printStackTrace();
        } else {
            super.printStackTrace();
        }
    }

    /**
     * Prints the wrapped excpetion A its backtrace to the specified print stream.
     *
     * @param s the print stream
     */
    public void printStackTrace(final PrintStream s) {
        if (m_throwable != null) {
            m_throwable.printStackTrace(s);
        } else {
            super.printStackTrace(s);
        }
    }

    /**
     * Prints the wrapped exception A its backtrace to the specified print writer.
     *
     * @param s the print writer
     */
    public void printStackTrace(final PrintWriter s) {
        if (m_throwable != null) {
            m_throwable.printStackTrace(s);
        } else {
            super.printStackTrace(s);
        }
    }

}
