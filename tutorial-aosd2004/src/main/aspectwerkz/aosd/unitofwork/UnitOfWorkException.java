/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD-style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package aspectwerkz.aosd.unitofwork;

/**
 * UnitOfWorkException.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public final class UnitOfWorkException extends RuntimeException {

    /**
     * Construct a new <code>UnitOfWorkException</code> instance.
     *
     * @param message The detail message for this exception.
     */
    public UnitOfWorkException(final String message) {
        super(message);
    }
}
