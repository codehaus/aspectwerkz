/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
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
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r </a>
 */
public interface JoinPoint extends StaticJoinPoint {

    /**
     * Returns the callee instance.
     *
     * @return the callee instance
     */
    Object getCallee();

    /**
     * Returns the caller instance.
     *
     * @return the caller instance
     */
    Object getCaller();

    /**
     * Returns the 'this' instance (the one currently executing).
     *
     * @return 'this'
     */
    Object getThis();

    /**
     * Returns the target instance. If the join point is executing in a static context it returns null.
     *
     * @return the target instance
     */
    Object getTarget();    

    /**
     * Returns the RTTI for the join point.
     *
     * @return the RTTI
     */
    Rtti getRtti();
}