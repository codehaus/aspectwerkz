/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.joinpoint.management;

/**
 * Enumeration for all join point types.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public final class JoinPointType {
    public static final int METHOD_EXECUTION = 1;

    public static final int METHOD_CALL = 2;

    public static final int CONSTRUCTOR_EXECUTION = 3;

    public static final int CONSTRUCTOR_CALL = 4;

    public static final int FIELD_SET = 5;

    public static final int FIELD_GET = 6;

    public static final int HANDLER = 7;

    public static final int STATIC_INITALIZATION = 8;
}