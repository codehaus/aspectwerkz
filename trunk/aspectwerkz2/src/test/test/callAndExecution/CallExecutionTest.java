/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.callAndExecution;

import org.codehaus.aspectwerkz.SystemLoader;
import org.codehaus.aspectwerkz.WeavedTestCase;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class CallExecutionTest extends WeavedTestCase {

    private static String s_logString = "";

    public void testPrivateMethod() {
        s_logString = "";
        privateMethod();
        assertEquals("call1 execution1 invocation execution2 call2 ", s_logString);
    }

    public void testPublicMethod() {
        s_logString = "";
        publicMethod();
        assertEquals("call1 execution1 invocation execution2 call2 ", s_logString);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(CallExecutionTest.class);
    }

    public CallExecutionTest() {
    }

    public CallExecutionTest(String name) {
        super(name);
    }

    // ==== methods to test ====

    public static void log(final String wasHere) {
        s_logString += wasHere;
    }

    private void privateMethod() {
        log("invocation ");
    }

    public void publicMethod() {
        log("invocation ");
    }
}
