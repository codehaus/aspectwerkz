/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package examples.xmldef.logging;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public class Target {

    public Target() {
        this(1);
    }

    public Target(int i) {
    }

    public Target(String i) {
    }

    /**
     * @aspectwerkz.joinpoint.controller examples.xmldef.logging.DummyJoinPointController
     * @aspectwerkz.advice.method log
     * @aspectwerkz.advice.method log
     */
    public static void toLog1() {
        new Target().toLog2("parameter");
    }

    /**
     * @aspectwerkz.advice.method log
     */
    private void toLog2(java.lang.String arg) {
        new Target().toLog3();
    }

    /**
     * @aspectwerkz.advice.method log
     */
    private String toLog3() {
        return "result";
    }

    /**
     * @aspectwerkz.advice.method log
     */
    public static void main(String[] args) {
        Target.toLog1();
    }
}
