/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform;

import java.util.Hashtable;

/**
 * Interface that all the weaving strategy implementations must implement.
 * 
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public interface WeavingStrategy {
    /**
     * Initializes the transformer stack.
     * 
     * @param params
     */
    public abstract void initialize(final Hashtable params);

    /**
     * @param className
     * @param klass
     * @param context
     * @return
     */
    public abstract void transform(final String className, final Klass klass,
            final Context context);
}