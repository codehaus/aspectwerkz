package test;

import junit.framework.TestCase;

import org.codehaus.aspectwerkz.AspectWerkz;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: StaticMethodAdviceTest.java,v 1.2 2003-06-09 07:04:13 jboner Exp $
 */
public class StaticMethodAdviceTest extends TestCase {

    private static String m_logString = "";

    public void testMethodAdvice() {
        m_logString = "";
        methodAdvicedMethod();
        assertEquals("before1 invocation after1 ", m_logString);
    }

    public void testMethodAdviceNewThread() {
        m_logString = "";
        methodAdvicedMethodNewThread();
        assertEquals("before before invocation after after ", m_logString);
    }

    public void testMultipleChainedMethodAdvices() {
        m_logString = "";
        multipleChainedMethodAdvicedMethod();
        assertEquals("before1 before2 invocation after2 after1 ", m_logString);
    }

    public void testGetJoinPointMetaData() {
        String param = "parameter";
        assertEquals(
                getClass().getName() +
                "___originalMethod$joinPointMetaData$1" +
                param +
                param.getClass().getName() +
                "java.lang.String" +
                "result",
                joinPointMetaData(param));
    }

    public void testHasPointcutButNoAdvice() {
        try {
            hasPointcutButNoAdvice();
        }
        catch (Exception e) {
            fail();
        }
    }

    public void testAnonymousAdviced() {
        try {
            anonymousAdviced();
        }
        catch (Exception e) {
            fail();
        }
    }

    public void testThrowException() {
        try {
            exceptionThrower();
        }
        catch (Throwable e) {
            assertTrue(e instanceof test.TestException);
            return;
        }
        fail("this point should never be reached");
    }

    public void testReturnVoid() {
        getVoid();
    }

    public void testReturnLong() {
        assertEquals(1L, getLong());
    }

    public void testReturnInt() {
        assertEquals(1, getInt());
    }

    public void testReturnShort() {
        assertEquals(1, getShort());
    }

    public void testReturnDouble() {
        assertEquals(new Double(1.1D), new Double(getDouble()));
    }

    public void testReturnFloat() {
        assertEquals(new Float(1.1F), new Float(getFloat()));
    }

    public void testReturnByte() {
        assertEquals(Byte.parseByte("1"), getByte());
    }

    public void testReturnChar() {
        assertEquals('A', getChar());
    }

    public void testReturnBoolean() {
        assertEquals(true, getBoolean());
    }

    public void testNoArgs() {
        noParams();
    }

    public void testIntArg() {
        assertEquals(12, intParam(12));
    }

    public void testLongArg() {
        assertEquals(12L, longParam(12L));
    }

    public void testShortArg() {
        assertEquals(3, shortParam((short)3));
    }

    public void testDoubleArg() {
        assertEquals(new Double(2.3D), new Double(doubleParam(2.3D)));
    }

    public void testFloatArg() {
        assertEquals(new Float(2.3F), new Float(floatParam(2.3F)));
    }

    public void testByteArg() {
        assertEquals(Byte.parseByte("1"), byteParam(Byte.parseByte("1")));
    }

    public void testCharArg() {
        assertEquals('B', charParam('B'));
    }

    public void testBooleanArg() {
        assertEquals(false, booleanParam(false));
    }

    public void testObjectArg() {
        assertEquals(this, objectParam(this));
    }

    public void testVariousArguments1() {
        assertEquals("dummy".hashCode() + 1 + (int)2.3F, this.hashCode() + (int)34L,
                variousParams1("dummy", 1, 2.3F, this, 34L));
    }

    public void testVariousArguments2() {
        assertEquals((int)2.3F + 1 + "dummy".hashCode() + this.hashCode() + (int)34L + "test".hashCode(),
                variousParams2(2.3F, 1, "dummy", this, 34L, "test"));
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(StaticMethodAdviceTest.class);
    }

    public StaticMethodAdviceTest(String name) {
        super(name);
        AspectWerkz.getSystem("tests").initialize();
    }

    // ==== methods to test ====

    public static void log(final String wasHere) {
        m_logString += wasHere;
    }

    public static void nonAdvisedMethod() {
    }

    public static void methodAdvicedMethod() {
        log("invocation ");
    }

    public static void methodAdvicedMethodNewThread() {
        log("invocation ");
    }

    public static void multipleMethodAdvicedMethod() {
        log("invocation ");
    }

    public static void multipleChainedMethodAdvicedMethod() {
        log("invocation ");
    }

    public static void multipleMethodAndPrePostAdvicedMethod() {
        log("invocation ");
    }

    public static void methodAdvicedWithPreAndPost() {
        log("invocation ");
    }

    public static void multipleMethodAdvicedWithPreAndPost() {
        log("invocation ");
    }

    public static void methodAdviceWithMultiplePreAndPostAdviced() {
        log("invocation ");
    }

    public static void exceptionThrower() throws Throwable {
        throw new UnsupportedOperationException("this is a test");
    }

    public static String joinPointMetaData(String param) {
        return "result";
    }

    public static void hasPointcutButNoAdvice() {
    }

    public static String postAdviced() {
        return "test";
    }

    public static void anonymousAdviced() {
    }

    public static void throwsException() throws Exception {
        throw new Exception("test");
    }

    public static void throwsRuntimeException() {
        throw new RuntimeException("test");
    }

    public static void throwsError() {
        throw new Error("test");
    }

    public static void noParams() throws RuntimeException {
    }

    public static long longParam(long arg) {
        return arg;
    }

    public static int intParam(int arg) {
        return arg;
    }

    public static short shortParam(short arg) {
        return arg;
    }

    public static double doubleParam(double arg) {
        return arg;
    }

    public static float floatParam(float arg) {
        return arg;
    }

    public static byte byteParam(byte arg) {
        return arg;
    }

    public static boolean booleanParam(boolean arg) {
        return arg;
    }

    public static char charParam(char arg) {
        return arg;
    }

    public static Object objectParam(Object arg) {
        return arg;
    }

    public static int variousParams1(String str, int i, float f, Object o, long l) throws RuntimeException {
        return str.hashCode() + i + (int)f + o.hashCode() + (int)l;
    }

    public static int variousParams2(float f, int i, String str1, Object o, long l, String str2) throws RuntimeException {
        return (int)f + i + str1.hashCode() + o.hashCode() + (int)l + str2.hashCode();
    }

    public static float variousParams3(String s, long y, String t, String r, String e, int w, String q) {
        return 2.5F;
    }

    public static void getVoid() throws RuntimeException {
    }

    public static long getLong() throws RuntimeException {
        return 1L;
    }

    public static int getInt() throws RuntimeException {
        return 1;
    }

    public static short getShort() throws RuntimeException {
        return 1;
    }

    public static double getDouble() throws RuntimeException {
        return 1.1D;
    }

    public static float getFloat() throws RuntimeException {
        return 1.1F;
    }

    public static byte getByte() throws RuntimeException {
        return Byte.parseByte("1");
    }

    public static char getChar() throws RuntimeException {
        return 'A';
    }

    public static boolean getBoolean() throws RuntimeException {
        return true;
    }
}
