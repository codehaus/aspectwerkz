/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD-style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package aspectwerkz.aosd.transaction;

/**
 * TransactionException.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public final class TransactionException extends RuntimeException {

    /**
     * Construct a new <code>TransactionException</code> instance.
     *
     * @param message The detail message for this exception.
     */
    public TransactionException(final String message) {
        super(message);
    }
}
