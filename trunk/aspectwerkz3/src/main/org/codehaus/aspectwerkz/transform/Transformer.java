/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform;

import org.codehaus.aspectwerkz.transform.delegation.Klass;

/**
 * Component for class transformation At load time / post compilation time: transform At runtime to
 * activate prepared declarations: activate
 * 
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur </a>
 */
public interface Transformer {
    /**
     * Transforms bytecode at load time
     * 
     * @param context
     * @param klass
     * @throws Exception
     */
    public abstract void transform(final Context context, final Klass klass) throws Exception;
}