/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition.expression;

import java.util.Set;
import java.util.Iterator;

/**
 * Type-safe enum for the pointcut types.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class PointcutType {

    public static final PointcutType ANY = new PointcutType("any");

    public static final PointcutType EXECUTION = new PointcutType("execution");
    public static final PointcutType CALL = new PointcutType("call");
    public static final PointcutType SET = new PointcutType("set");
    public static final PointcutType GET = new PointcutType("get");
    public static final PointcutType HANDLER = new PointcutType("handler");
    public static final PointcutType CFLOW = new PointcutType("cflow");
    public static final PointcutType CLASS = new PointcutType("class");
    public static final PointcutType ATTRIBUTE = new PointcutType("attribute");

    private final String m_name;

    private PointcutType(String name) {
        m_name = name;
    }

    public String toString() {
        return m_name;
    }

    public static boolean isCflowTypeOnly(Set typeSet) {
        boolean hasCflow = false;
        boolean hasOther = false;
        for (Iterator types = typeSet.iterator(); types.hasNext();) {
            PointcutType type = (PointcutType)types.next();
            if (type.equals(PointcutType.CFLOW)) {
                hasCflow = true;
            } else {
                //TODO add support for WITHIN
                //??ANY??
                hasOther = true;
            }
        }
        return (hasCflow && ! hasOther);
    }

}
