/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.xmldef.staticfield;

import junit.framework.TestCase;

/**
 * Test case for AW-92
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class StaticFieldAdviceTest extends TestCase {

    public static int s_fieldA = 0;

    public static int s_fieldB = 0;

    public void testStaticFieldAccessedOutsideStaticCtx() {
        try {
            assertEquals(0, accessFieldA());
        } catch (Throwable t) {
            fail(t.getMessage());
        }
    }

    public void testStaticFieldAccessedInsideStaticCtx() {
        assertEquals(0, StaticFieldAdviceTest.accessFieldB());
    }

    // -- methods --

    private int accessFieldA() {
        //static field access in member method
        int value = s_fieldA;
        return value;
    }

    private static int accessFieldB() {
        //static field access in static method
        int value = s_fieldB;
        return value;
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(StaticFieldAdviceTest.class);
    }

}
