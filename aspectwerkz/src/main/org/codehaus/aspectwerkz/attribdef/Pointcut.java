/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.attribdef;

/**
 * Represents the pointcut construct.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class Pointcut {
    public static final String EXECUTION = "execution";
    public static final String CALL = "call";
    public static final String SET = "set";
    public static final String GET = "get";
    public static final String THROWS = "throws";
    public static final String CFLOW = "cflow";
    public static final String CLASS = "class";
}
