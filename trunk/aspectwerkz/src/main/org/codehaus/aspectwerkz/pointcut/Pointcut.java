/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.pointcut;

/**
 * Interface for the pointcut concept.<br/>
 * I.e.an abstraction of a well defined point of execution in the program.<br/>
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public interface Pointcut {

    /**
     * Returns the name of the pointcut.
     *
     * @return the name
     */
    String getExpression();
}
