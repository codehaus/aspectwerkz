/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.delegation;

import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r </a>
 */
public abstract class AfterFinallyAdviceDelegator extends AbstractAdviceDelegator {

    /**
     * @param aspect
     * @param adviceName
     */
    public AfterFinallyAdviceDelegator(final Object aspect, final String adviceName) {
        super(aspect, adviceName);
    }

    /**
     * @param jp
     * @throws Throwable
     */
    public abstract void delegate(final JoinPoint jp) throws Throwable;
}