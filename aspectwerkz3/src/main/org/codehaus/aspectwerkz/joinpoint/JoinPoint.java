/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.joinpoint;

/**
 * Implements the join point concept, e.g. defines a well defined point in the program flow.
 * <p/>
 * Provides access to runtime type information (RTTI), is therefore significantly <b>slower</b>
 * than the usage of the {@link org.codehaus.aspectwerkz.joinpoint.StaticJoinPoint} interface.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public interface JoinPoint extends StaticJoinPoint {

    /**
     * Returns the RTTI for the join point.
     * 
     * @return the RTTI
     */
    Rtti getRtti();

    /**
     * Resets the join point. <p/>Will restart the execution chain of advice.
     *
     * @TODO remove for 2.0
     * @deprecated 
     */
    void reset();
}