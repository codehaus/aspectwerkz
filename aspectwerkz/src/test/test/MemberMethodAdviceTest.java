package test;

import junit.framework.TestCase;

import org.codehaus.aspectwerkz.AspectWerkz;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public class MemberMethodAdviceTest extends TestCase implements Loggable {

    private String m_logString = "";

    public void testPassingParameterToAdvice() {
        m_logString = "";
        passingParameterToAdviceMethod();
        assertEquals("test_value", m_logString);
    }

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

    public void testMultipleMethodAdvices() {
        m_logString = "";
        multipleMethodAdvicedMethod();
        assertEquals("before1 before2 before2 invocation after2 after2 after1 ", m_logString);
    }

    public void testMultipleChainedMethodAdvices() {
        m_logString = "";
        multipleChainedMethodAdvicedMethod();
        assertEquals("before1 before2 invocation after2 after1 ", m_logString);
    }

    public void testMultiplePointcuts() {
        m_logString = "";
        multiplePointcutsMethod();
        assertEquals("before2 before2 before1 before1 invocation after1 after1 after2 after2 ", m_logString);
    }

    public void testGetJoinPointMetaData() {
        String param = "parameter";
        assertEquals(
                getClass().getName() +
                "___AW_originalMethod$joinPointMetaData$1" +
                hashCode() +
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
            assertTrue(e instanceof UnsupportedOperationException);
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

    public void testArrayArg() {
        String[] array = new String[]{"one", "two", "three"};
        assertTrue(arrayParam(array)[0].equals(array[0]));
        assertTrue(arrayParam(array)[1].equals(array[1]));
        assertTrue(arrayParam(array)[2].equals(array[2]));
    }

    public void testVariousArguments1() {
        assertEquals("dummy".hashCode() + 1 + (int)2.3F, this.hashCode() + (int)34L,
                variousParams1("dummy", 1, 2.3F, this, 34L));
    }

    public void testVariousArguments2() {
        assertEquals((int)2.3F + 1 + "dummy".hashCode() + this.hashCode() + (int)34L + "test".hashCode(),
                variousParams2(2.3F, 1, "dummy", this, 34L, "test"));
    }

    public void testVariousArguments4() {
        assertEquals("dummy", takesArrayAsArgument(new String[]{"dummy", "test"})[0]);
        assertEquals("test", takesArrayAsArgument(new String[]{"dummy", "test"})[1]);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(MemberMethodAdviceTest.class);
    }

    public MemberMethodAdviceTest() {}
    public MemberMethodAdviceTest(String name) {
        super(name);
        AspectWerkz.getSystem("tests").initialize();
    }

    // ==== methods to test ====

    public void log(final String wasHere) {
        m_logString += wasHere;
    }

    private void passingParameterToAdviceMethod() {
    }

    public void nonAdvisedMethod() {
    }

    public void methodAdvicedMethod() {
        log("invocation ");
    }

    public void methodAdvicedMethodNewThread() {
        log("invocation ");
    }

    public void multipleMethodAdvicedMethod() {
        log("invocation ");
    }

    public void multipleChainedMethodAdvicedMethod() {
        log("invocation ");
    }

    public void multiplePointcutsMethod() {
        log("invocation ");
    }

    public void multipleMethodAndPrePostAdvicedMethod() {
        log("invocation ");
    }

    public void methodAdvicedWithPreAndPost() {
        log("invocation ");
    }

    public void multipleMethodAdvicedWithPreAndPost() {
        log("invocation ");
    }

    public void methodAdviceWithMultiplePreAndPostAdviced() {
        log("invocation ");
    }

    public void exceptionThrower() throws Throwable {
        throw new UnsupportedOperationException("this is a test");
    }

    public String joinPointMetaData(String param) {
        return "result";
    }

    public void hasPointcutButNoAdvice() {
    }

    public String postAdviced() {
        return "test";
    }

    public void anonymousAdviced() {
    }

    public void throwsException() throws Exception {
        throw new Exception("test");
    }

    public void throwsRuntimeException() {
        throw new RuntimeException("test");
    }

    public void throwsError() {
        throw new Error("test");
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

    public static String[] takesArrayAsArgument(String[] arr) {
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
}
