/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.constructor;

import junit.framework.TestCase;

import org.codehaus.aspectwerkz.SystemLoader;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public class ConstructorAdviceTest extends TestCase {

    private static String s_logString = "";

    public void testAroundAdvice() {
        s_logString = "";
        TestAroundAdvice test = new TestAroundAdvice(1L, new Object(), new String[]{});
        assertEquals("before init after ", s_logString);
        assertNotNull(test);
        assertTrue(test instanceof TestAroundAdvice);
    }

    public void testBeforeAdvice() {
        s_logString = "";
        TestBeforeAdvice test = new TestBeforeAdvice();
        assertEquals("pre init ", s_logString);
        assertNotNull(test);
        assertTrue(test instanceof TestBeforeAdvice);
    }

    public void testAfterAdvice() {
        s_logString = "";
        TestAfterAdvice test = new TestAfterAdvice("test");
        assertEquals("test post ", s_logString);
        assertNotNull(test);
        assertTrue(test instanceof TestAfterAdvice);
    }

    public void testBeforeAfterAdvice() {
        s_logString = "";
        TestBeforeAfterAdvice test = new TestBeforeAfterAdvice(new String[]{"test"});
        assertEquals("pre test post ", s_logString);
        assertNotNull(test);
        assertTrue(test instanceof TestBeforeAfterAdvice);
    }

    public void testReturnFalseType() {
        s_logString = "";
        TestReturnFalseType test = null;
        try {
            test = new TestReturnFalseType();
        }
        catch (ClassCastException e) {
            return;
        }
        fail("this point should not have been reached a class cast exception should have been thrown");
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(ConstructorAdviceTest.class);
    }

    public ConstructorAdviceTest() {
    }

    public ConstructorAdviceTest(String name) {
        super(name);
        SystemLoader.getSystem("tests").initialize();
    }

    public static void log(final String wasHere) {
        s_logString += wasHere;
    }

    public void noParams() throws RuntimeException {
    }

    public long longParam(long arg) {
        return arg;
    }

    public int intParam(int arg) {
        return arg;
    }

    public short shortParam(short arg) {
        return arg;
    }

    public double doubleParam(double arg) {
        return arg;
    }

    public float floatParam(float arg) {
        return arg;
    }

    public byte byteParam(byte arg) {
        return arg;
    }

    public boolean booleanParam(boolean arg) {
        return arg;
    }

    public char charParam(char arg) {
        return arg;
    }

    public Object objectParam(Object arg) {
        return arg;
    }

    public String[] arrayParam(String[] arg) {
        return arg;
    }

    public int variousParams1(String str, int i, float f, Object o, long l) throws RuntimeException {
        return str.hashCode() + i + (int)f + o.hashCode() + (int)l;
    }

    public int variousParams2(float f, int i, String str1, Object o, long l, String str2) throws RuntimeException {
        return (int)f + i + str1.hashCode() + o.hashCode() + (int)l + str2.hashCode();
    }

    public float variousParams3(String s, long y, String t, String r, String e, int w, String q) {
        return 2.5F;
    }

    public String[] takesArrayAsArgument(String[] arr) {
        return arr;
    }

    public void getVoid() throws RuntimeException {
    }

    public long getLong() throws RuntimeException {
        return 1L;
    }

    public int getInt() throws RuntimeException {
        return 1;
    }

    public short getShort() throws RuntimeException {
        return 1;
    }

    public double getDouble() throws RuntimeException {
        return 1.1D;
    }

    public float getFloat() throws RuntimeException {
        return 1.1F;
    }

    public byte getByte() throws RuntimeException {
        return Byte.parseByte("1");
    }

    public char getChar() throws RuntimeException {
        return 'A';
    }

    public boolean getBoolean() throws RuntimeException {
        return true;
    }

    public long getPrimitiveAndNullFromAdvice() throws RuntimeException {
        return 123456789L;
    }
}