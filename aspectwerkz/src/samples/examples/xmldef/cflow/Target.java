/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package examples.xmldef.cflow;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public class Target {

    /**
     * @aspectwerkz.cflow cflowtest
     */
    public void step1() {
        System.out.println("  --> invoking step1");
        step2();
    }

    /**
     * @aspectwerkz.advice.method cflow cflow=cflowtest
     */
    public void step2() {
        System.out.println("  --> invoking step2");
    }

    public static void main(String[] args) {
        Target target = new Target();

        System.out.println("\n--------------------------");
        System.out.println("step2 is called in the cflow of step1 => should trigger the advice");
        target.step1();

        System.out.println("\n--------------------------");
        System.out.println("step2 is called directly (not in cflow of step1) => should NOT trigger the advice");
        target.step2();
    }
}
