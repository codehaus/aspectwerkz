package test;

import junit.framework.TestCase;

import org.codehaus.aspectwerkz.AspectWerkz;

public class CallerSideAdviceTest extends TestCase {

    private static String s_logString = "";

    public void testPassingParameterToAdvice() {
        s_logString = "";
        CallerSideTestHelper helper = new CallerSideTestHelper();
        helper.passingParameterToAdviceMethod();
        assertEquals("test_value", s_logString);
    }

    public void testPreAdvicedMemberMethod() {
        s_logString = "";
        try {
            CallerSideTestHelper helper = new CallerSideTestHelper();
            helper.invokeMemberMethodPre();
            assertEquals("pre1 pre2 ", s_logString);
        }
        catch (Exception e) {
            fail();
        }
    }

    public void testPostAdvicedMemberMethod() {
        s_logString = "";
        try {
            CallerSideTestHelper helper = new CallerSideTestHelper();
            helper.invokeMemberMethodPost();
            assertEquals("post1 post2 ", s_logString);
        }
        catch (Exception e) {
            fail();
        }
    }

    public void testPrePostAdvicedMemberMethod() {
        s_logString = "";
        try {
            CallerSideTestHelper helper = new CallerSideTestHelper();
            helper.invokeMemberMethodPrePost();
            assertEquals("pre1 pre2 post1 post2 ", s_logString);
        }
        catch (Exception e) {
            fail();
        }
    }

    public void testPreAdvicedStaticMethod() {
        s_logString = "";
        try {
            CallerSideTestHelper.invokeStaticMethodPre();
            assertEquals("pre1 pre2 ", s_logString);
        }
        catch (Exception e) {
            fail();
        }
    }

    public void testPostAdvicedStaticMethod() {
        s_logString = "";
        try {
            CallerSideTestHelper.invokeStaticMethodPost();
            assertEquals("post1 post2 ", s_logString);
        }
        catch (Exception e) {
            fail();
        }
    }

    public void testPrePostAdvicedStaticMethod() {
        s_logString = "";
        try {
            CallerSideTestHelper.invokeStaticMethodPrePost();
            assertEquals("pre1 pre2 post1 post2 ", s_logString);
        }
        catch (Exception e) {
            fail();
        }
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(CallerSideAdviceTest.class);
    }

    public CallerSideAdviceTest(String name) {
        super(name);
        AspectWerkz.initialize();
    }

    // ==== methods to test ====

    public static void log(final String wasHere) {
        s_logString += wasHere;
    }

    public void setFieldPreAdviced() {
    }
}
