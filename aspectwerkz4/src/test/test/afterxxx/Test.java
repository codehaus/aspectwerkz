/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.afterxxx;

import junit.framework.TestCase;

public class Test extends TestCase {
    private static String s_log;

    public static void log(String msg) {
        s_log += msg;
    }

    public void testall() {
        s_log = "";
        all();
        assertEquals("logAround ", s_log);
    }

    public void testaroundFinally() {
        s_log = "";
        aroundFinally();
        assertEquals("logAround logAfterFinally ", s_log);
    }

    public void testaroundFinallyReturning() {
        s_log = "";
        aroundFinallyReturning();
        assertEquals("logAround logAfterFinally ", s_log);
    }

    public void testaroundReturning() {
        s_log = "";
        aroundReturning();
        assertEquals("logAround logAfterReturning ", s_log);
    }

    public void testaroundFinallyReturningThrowing() {
        s_log = "";
        try {
            aroundFinallyReturningThrowing();
        } catch (UnsupportedOperationException e) {
        }
        assertEquals("logAround logAfterThrowing logAfterFinally ", s_log);
    }

    public void testaroundReturningThrowing() {
        s_log = "";
        try {
            aroundReturningThrowing();
        } catch (UnsupportedOperationException e) {
        }
        assertEquals("logAround logAfterThrowing ", s_log);
    }

    public void test_finally() {
        s_log = "";
        _finally();
        assertEquals("logAfterFinally ", s_log);
    }

    public void testfinallyReturning() {
        s_log = "";
        finallyReturning();
        assertEquals("logAfterReturning logAfterFinally ", s_log);
    }

    public void testfinallyReturningThrowing() {
        s_log = "";
        try {
            finallyReturningThrowing();
        } catch (UnsupportedOperationException e) {
        }
        assertEquals("logAfterThrowing logAfterFinally ", s_log);
    }

    public void testreturning() {
        s_log = "";
        returning();
        assertEquals("logAfterReturning ", s_log);
    }

    public void testreturningThrowing() {
        s_log = "";
        try {
            returningThrowing();
        } catch (Exception e) {
        }
        assertEquals("", s_log);
    }

    public Test(String name) {
        super(name);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(Test.class);
    }

    void all() {
    }

    void aroundFinally() {
    }

    static Object aroundFinallyReturning() {
        return null;
    }

    Object aroundReturning() {
        return "aroundReturning";
    }

    static Object aroundFinallyReturningThrowing() {
        throw new UnsupportedOperationException();
    }

    Object aroundReturningThrowing() {
        throw new UnsupportedOperationException();
    }

    void _finally() {
    }

    static Object finallyReturning() {
        return "finallyReturning";
    }

    static Object finallyReturningThrowing() {
        throw new UnsupportedOperationException();
    }

    Object returningThrowing() throws Exception {
        throw new Exception();
    }

    Object returning() {
        return "returning";
    }
}