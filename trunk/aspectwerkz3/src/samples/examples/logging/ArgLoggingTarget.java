/**************************************************************************************
 * Copyright (c) Jonas Bon?r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package examples.logging;

/**
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class ArgLoggingTarget {

    public void toLog_1(int typeMatch, String s, int i) {
        System.out.println("== ArgLoggingTarget.toLog_1 " + typeMatch + ", "+ s + ", "+i);
    }

    public void toLog_2(int typeMatch, String s, int i) {
        System.out.println("== ArgLoggingTarget.toLog_2 " + typeMatch + ", "+ s + ", "+i);
    }

    public static void main(String args[]) throws Throwable {
        ArgLoggingTarget me = new ArgLoggingTarget();
        me.toLog_1(0, "a", 1);
        me.toLog_2(0, "b", 2);
    }

}
