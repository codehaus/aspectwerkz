/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.util;

import java.io.Serializable;

/**
 * Extends the java.lang.SerializableThreadLocal to make it serializable.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class SerializableThreadLocal extends java.lang.ThreadLocal implements Serializable {

    /**
     * Constructor. Simply calls the base class constructor.
     */
    public SerializableThreadLocal() {
        super();
    }
}
