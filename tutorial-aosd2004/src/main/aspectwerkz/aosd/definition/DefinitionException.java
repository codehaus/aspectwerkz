/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD-style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package aspectwerkz.aosd.definition;

/**
 * DefinitionException.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public final class DefinitionException extends RuntimeException {

    /**
     * Construct a new <code>DefinitionException</code> instance.
     *
     * @param message the message for this exception.
     */
    public DefinitionException(final String message) {
        super(message);
    }
}
