/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform.delegation;

import org.codehaus.aspectwerkz.transform.Context;
import org.codehaus.aspectwerkz.transform.Klass;

/**
 * Activator alters bytecode at runtime
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public interface Activator {
    /**
     * Transforms bytecode at runtime
     *
     * @param context
     * @param klass
     * @throws Exception
     */
    public abstract void activate(final Context context, final Klass klass) throws Exception;
}
