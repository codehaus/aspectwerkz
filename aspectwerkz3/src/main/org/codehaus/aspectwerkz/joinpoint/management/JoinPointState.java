/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.joinpoint.management;

/**
 * Enumeration for all join point states.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public final class JoinPointState {
    /**
     * The join point is not advised.
     */
    public static final int NOT_ADVISED = 0;

    /**
     * The join point is advised (this does not mean that it has advices attached to it).
     */
    public static final int ADVISED = 1;

    /**
     * The join point has advices.
     */
    public static final int HAS_ADVICES = 2;

    /**
     * The join point has beed redefined, needs redeployment.
     */
    public static final int REDEFINED = 3;
}
