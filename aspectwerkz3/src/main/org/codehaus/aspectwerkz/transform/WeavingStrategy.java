/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
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
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r </a>
 */
public interface WeavingStrategy {
    
    /**
     * Defines the inlining weaving strategy.
     */
    public static final int INLINING = 0;

    /**
     * Defines the delegation weaving strategy.
     */
    public static final int DELEGATION = 1;

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
    public abstract void transform(final String className, final Context context);

    /**
     * Creates a new transformation context.
     * 
     * @param name
     * @param bytecode
     * @param loader
     * @return
     */
    public abstract Context newContext(
        final String name,
        final byte[] bytecode,
        final ClassLoader loader);
}