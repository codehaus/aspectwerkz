/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.annotation;

/**
 * Annotation for pointcut
 *
 * TODO: rename to Pointcut and remove pointcut as field ??
 *
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public interface Expression {
    /**
     * The pointcut expression
     */
    String value();
}
