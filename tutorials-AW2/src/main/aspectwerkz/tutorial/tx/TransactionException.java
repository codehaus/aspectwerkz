/**************************************************************************************
 * Copyright (c) Jonas BonŽr, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package aspectwerkz.tutorial.tx;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * Transaction exception.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public class TransactionException extends RuntimeException {

    /**
     * Original exception which caused this exception.
     */
    private Throwable m_originalException;

    /**
     * Sets the message for the exception.
     *
     * @param message the message
     */
    public TransactionException(final String message) {
        super(message);
    }

    /**
     * Sets the message for the exception and the original exception being wrapped.
     *
     * @param throwable the original exception
     */
    public TransactionException(final Throwable throwable) {
        super(throwable.getMessage());
        m_originalException = throwable;
    }

    /**
     * Sets the message for the exception and the original exception being wrapped.
     *
     * @param message   the detail of the error message
     * @param throwable the original exception
     */
    public TransactionException(final String message, final Throwable throwable) {
        super(message);
        m_originalException = throwable;
    }

    /**
     * Print the full stack trace, including the original exception.
     */
    public void printStackTrace() {
        printStackTrace(System.err);
    }

    /**
     * Print the full stack trace, including the original exception.
     *
     * @param ps the byte stream in which to print the stack trace
     */
    public void printStackTrace(final PrintStream ps) {
        super.printStackTrace(ps);
        if (m_originalException != null) {
            m_originalException.printStackTrace(ps);
        }
    }

    /**
     * Print the full stack trace, including the original exception.
     *
     * @param pw the character stream in which to print the stack trace
     */
    public void printStackTrace(final PrintWriter pw) {
        super.printStackTrace(pw);
        if (m_originalException != null) {
            m_originalException.printStackTrace(pw);
        }
    }
}
