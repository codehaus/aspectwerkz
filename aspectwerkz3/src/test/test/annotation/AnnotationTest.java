/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.annotation;

import org.codehaus.aspectwerkz.WeavedTestCase;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class AnnotationTest extends WeavedTestCase {
    private static String s_logString = "";

    /** @@privateField */
    private int privateField;

    /** @@protectedField */
    protected int protectedField;

    /** @@publicField */
    public int publicField;

    /** @@packagePrivateField */
    int packagePrivateField;

    public AnnotationTest() {
    }

    public AnnotationTest(String name) {
        super(name);
    }

    public void testPrivateMethod() {
        s_logString = "";
        privateMethod();
        assertEquals("call execution invocation execution call ", s_logString);
    }

    public void testProtectedMethod() {
        s_logString = "";
        protectedMethod();
        assertEquals("call execution invocation execution call ", s_logString);
    }

    public void testPackagePrivateMethod() {
        s_logString = "";
        packagePrivateMethod();
        assertEquals("call execution invocation execution call ", s_logString);
    }

    public void testPublicMethod() {
        s_logString = "";
        publicMethod();
        assertEquals("call execution invocation execution call ", s_logString);
    }

    public void testSetPublicField() {
        s_logString = "";
        publicField = 0;
        assertEquals("set set ", s_logString);
    }

    public void testSetPrivateField() {
        s_logString = "";
        privateField = 0;
        assertEquals("set set ", s_logString);
    }

    public void testSetProtectedField() {
        s_logString = "";
        protectedField = 0;
        assertEquals("set set ", s_logString);
    }

    public void testSetPackagePrivateField() {
        s_logString = "";
        packagePrivateField = 0;
        assertEquals("set set ", s_logString);
    }

    public void testGetPublicField() {
        s_logString = "";
        int i = publicField;
        assertEquals("get get ", s_logString);
    }

    public void testGetPrivateField() {
        s_logString = "";
        int i = privateField;
        assertEquals("get get ", s_logString);
    }

    public void testGetProtectedField() {
        s_logString = "";
        int i = protectedField;
        assertEquals("get get ", s_logString);
    }

    public void testGetPackagePrivateField() {
        s_logString = "";
        int i = packagePrivateField;
        assertEquals("get get ", s_logString);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(AnnotationTest.class);
    }

    // ==== methods to test ====
    public static void log(final String wasHere) {
        s_logString += wasHere;
    }

    /** @@privateMethod */
    private void privateMethod() {
        log("invocation ");
    }

    /** @@protectedMethod */
    protected void protectedMethod() {
        log("invocation ");
    }

    /** @@publicMethod */
    public void publicMethod() {
        log("invocation ");
    }

    /** @@packagePrivateMethod */
    void packagePrivateMethod() {
        log("invocation ");
    }
}
