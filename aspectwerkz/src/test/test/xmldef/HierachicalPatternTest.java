package test;

import junit.framework.TestCase;

import org.codehaus.aspectwerkz.AspectWerkz;

/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
public class HierachicalPatternTest extends TestCase implements Loggable, DummyInterface1 {

    private String m_logString = "";

    public void test1() {
        m_logString = "";
        testMethod1();
        assertEquals("before1 invocation after1 ", m_logString);
    }

    public void test2() {
        m_logString = "";
        testMethod2();
        assertEquals("before1 invocation after1 ", m_logString);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(HierachicalPatternTest.class);
    }

    public HierachicalPatternTest() {}
    public HierachicalPatternTest(String name) {
        super(name);
        AspectWerkz.getSystem("tests").initialize();
    }

    // ==== methods to test ====

    public void log(final String wasHere) {
        m_logString += wasHere;
    }

    public void testMethod1() {
        log("invocation ");
    }
    public void testMethod2() {
        log("invocation ");
    }
}
