/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.abstractclass;


/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public abstract class AbstractTarget {
    public String method1() {
        return "method1";
    }

    public static String method2() {
        return "method2";
    }
}
