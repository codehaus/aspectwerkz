/**************************************************************************************
 * Copyright (c) Jonas Bon?r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.thistarget;

import junit.framework.TestCase;

import java.util.Stack;

/**
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class TargetTest extends TestCase {

    static String s_log = "";

//    public void testMethodExecutionTarget() {
//        // interface
//        ITarget iTarget = new TargetI();
//        s_log = "";
//        iTarget.target();
//        // this is both an interface, and an instance of TargetI
//        assertEquals("before_ITarget before_TargetI pre_ITarget pre_TargetI TargetI post_TargetI post_ITarget after_TargetI after_ITarget ", s_log);
//
//        // implementation of interface
//        TargetI targetI = new TargetI();
//        s_log = "";
//        targetI.target();
//        // this is both an interface, and an instance of TargetI
//        assertEquals("before_ITarget before_TargetI pre_ITarget pre_TargetI TargetI post_TargetI post_ITarget after_TargetI after_ITarget ", s_log);
//
//        // super class
//        SuperTarget superTarget = new TargetSuper();
//        s_log = "";
//        superTarget.target();
//        assertEquals("before_SuperTarget pre_SuperTarget SuperTarget post_SuperTarget after_SuperTarget ", s_log);
//
//        // super class abstract method
//        s_log = "";
//        superTarget.targetAbstract();
//        assertEquals("before_SuperTargetA pre_SuperTargetA TargetSuperA post_SuperTargetA after_SuperTargetA ", s_log);
//
//        // none
//        Target target = new Target();
//        s_log = "";
//        target.target();
//        assertEquals("Target ", s_log);
//    }

    public void testConstructorCallTarget() {
        // interface
        s_log = "";
        ITarget iTarget = new TargetI();
        // this is both an interface, and an instance of TargetI
        assertEquals("before_ITarget before_TargetI pre_ITarget pre_TargetI TargetI post_TargetI post_ITarget after_TargetI after_ITarget ", s_log);

        // implementation of interface
        s_log = "";
        TargetI targetI = new TargetI();
        // this is both an interface, and an instance of TargetI
        assertEquals("before_ITarget before_TargetI pre_ITarget pre_TargetI TargetI post_TargetI post_ITarget after_TargetI after_ITarget ", s_log);

        // super class
        s_log = "";
        SuperTarget superTarget = new TargetSuper();
        assertEquals("before_SuperTarget pre_SuperTarget SuperTarget TargetSuper post_SuperTarget after_SuperTarget ", s_log);

        // none
        s_log = "";
        Target target = new Target();
        assertEquals("Target ", s_log);
    }





    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(TargetTest.class);
    }

    static void log(String s) {
        s_log += s + " ";
    }

}
