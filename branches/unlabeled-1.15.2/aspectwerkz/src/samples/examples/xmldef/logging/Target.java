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

    private int m_counter = 0;

    public void increment() {
        m_counter++;
    }

    public static void toLog1() {
        new Target().toLog2("parameter");
    }

    private void toLog2(java.lang.String arg) {
        new Target().toLog3();
    }

    private String toLog3() {
        return "result";
    }

    public static void main(String[] args) {
        Target.toLog1();
    }
}
