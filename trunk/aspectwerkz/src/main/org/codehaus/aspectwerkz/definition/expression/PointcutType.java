/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition.expression;

/**
 * Type-safe enum for the pointcut types.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class PointcutType {

    public static final PointcutType EXECUTION = new PointcutType("EXECUTION");
    public static final PointcutType CALL = new PointcutType("CALL");
    public static final PointcutType SET = new PointcutType("SET");
    public static final PointcutType GET = new PointcutType("GET");
    public static final PointcutType CFLOW = new PointcutType("CFLOW");
    public static final PointcutType THROWS = new PointcutType("THROWS");
    public static final PointcutType CLASS = new PointcutType("CLASS");
    public static final PointcutType ATTRIBUTE = new PointcutType("ATTRIBUTE");

    private final String m_name;

    private PointcutType(String name) {
        m_name = name;
    }

    public String toString() {
        return m_name;
    }
}
