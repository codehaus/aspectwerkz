/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test;

import junit.framework.TestCase;

import java.io.PrintStream;

import org.codehaus.aspectwerkz.annotation.Before;

/**
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class FieldGetOutOfWeaver extends TestCase {

    static String s_log = "";

    public void testSystemGet() {
        s_log = "";
        PrintStream out = System.out;
        assertEquals("advice ", s_log);
    }

    public void testSystemGetOutsideCode() {
        s_log = "";
        PrintStream out = System.out;
        assertEquals("", s_log);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(FieldGetOutOfWeaver.class);
    }

    public static class Aspect {

        @Before("get(* out) && withincode(* test.FieldGetOutOfWeaver.testSystemGet(..))")
        void before() {
            FieldGetOutOfWeaver.s_log += "advice ";
        }
    }
}