/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD-style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package aspectwerkz.aosd.security;

/**
 * SecurityException.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class SecurityException extends RuntimeException {

    /**
     * Construct a new <code>SecurityException</code> instance.
     *
     * @param message message for this exception.
     */
    public SecurityException(final String message) {
        super(message);
    }
}
