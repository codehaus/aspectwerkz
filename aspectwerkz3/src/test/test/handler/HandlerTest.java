/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.handler;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class HandlerTest extends TestCase {
    private static String s_log = "";

    public HandlerTest() {
    }

    public HandlerTest(String name) {
        super(name);
    }

    public void testBeforeAdvice() {
        s_log = "";
        try {
            throw new HandlerTestBeforeException();
        } catch (HandlerTestBeforeException e) {
            log("before ");
        }
        assertEquals("pre before ", s_log);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(HandlerTest.class);
    }

    public static void log(final String wasHere) {
        s_log += wasHere;
    }
}
