/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.extension.objectfactory;

/**
 * ObjectFactoryException.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public class ObjectFactoryException extends Exception {

    /**
     * Construct a new <code>ObjectFactoryException</code> instance.
     *
     * @param message The detail message for this exception.
     */
    public ObjectFactoryException(final String message) {
        super(message);
    }
}
