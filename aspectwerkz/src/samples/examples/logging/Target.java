/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package examples.logging;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class Target {

    public static void toLog1() {
        System.out.println("Target.toLog1");
        new Target().toLog2("parameter");
    }

    private void toLog2(java.lang.String arg) {
        System.out.println("Target.toLog2");
        new Target().toLog3();
    }

    private String toLog3() {
        System.out.println("Target.toLog3");
        return "result";
    }

    public static void main(String[] args) {
        Target.toLog1();
    }
}
