/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package examples.logging;                                                   

/**
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur </a>
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public class ArgLoggingTarget {

    /**
     * @Annotation
     */
    public int toLog_1(int typeMatch, String s, int i) {
        System.out.println("toLog_1");
        toLog_2(0, "b", 2);
        return 0;
    }

    public java.lang.String[] toLog_2(int typeMatch, String s, int i) {
        System.out.println("toLog_2");
        int result = toLog_3(0, new String[]{"c"});
        return null;
    }

    private static int toLog_3(int typeMatch, String[] sarr) {
        System.out.println("toLog_2");
        return -1;
    }

    public static void main(String args[]) throws Throwable {
        System.out.println("main start");
        ArgLoggingTarget target = new ArgLoggingTarget();
        target.toLog_1(0, "a", 1);
//        new Runner().run();
        System.out.println("main end");
    }
}

class Runner {
    public void run() {
        ArgLoggingTarget target = new ArgLoggingTarget();
        target.toLog_1(0, "a", 1);
//        new ArgLoggingTarget().toLog_1(0, "a", 1);
    }
}