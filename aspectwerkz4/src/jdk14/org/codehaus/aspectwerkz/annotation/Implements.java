/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.annotation;

/**
 * Implements annotation for interface introduction
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public interface Implements {
    /**
     * The type pointcut (composition of within / hasField / hasMethod) the annotated field type
     * will be introduced to
     */
    public String value();
}
