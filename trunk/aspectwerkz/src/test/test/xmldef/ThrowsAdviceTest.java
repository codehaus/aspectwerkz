package test;

import junit.framework.TestCase;

import org.codehaus.aspectwerkz.AspectWerkz;

public class ThrowsAdviceTest extends TestCase {

    public void testExceptionThrown() {
        try {
            throwsException();
        }
        catch (TestException e) {
            assertEquals("test", e.getMessage());
            return;
        }
        catch (Throwable e) {
            fail("exception should have been catched by previous handler");
        }
        fail("exception expected");
    }

    public void testErrorThrown() {
        try {
            throwsError();
        }
        catch (TestException e) {
            assertEquals("test", e.getMessage());
            return;
        }
        catch (Throwable e) {
            fail("exception should have been catched by previous handler");
        }
        fail("exception expected");
    }

    public void testRuntimeExceptionThrown() {
        try {
            throwsRuntimeException();
        }
        catch (TestException e) {
            assertEquals("test", e.getMessage());
            return;
        }
        catch (Throwable e) {
            fail("exception should have been catched by previous handler");
        }
        fail("exception expected");
    }

    public void testJoinPointMetaData() {
        try {
            getJoinPointMetaData();
        }
        catch (TestException e) {
            Throwable error = new Error("test");
            assertEquals(
                    error +
                    error.getMessage() +
                    error.getClass() +
                    error.getClass().getName() +
                    error.getLocalizedMessage() +
                    "public void test.ThrowsAdviceTest.___originalMethod$getJoinPointMetaData$1() throws java.lang.Throwable" +
                    "getJoinPointMetaData" +
                    "void" +
                    this +
                    this.getClass().getName(), e.getMessage());
            return;
        }
        catch (Throwable e) {
            fail("exception should have been catched by previous handler");
        }
        fail("exception expected");
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(ThrowsAdviceTest.class);
    }

    public ThrowsAdviceTest(String name) {
        super(name);
        AspectWerkz.initialize();
    }

    // ==== methods to test ====

    public void throwsException() throws Throwable {
        throw new Exception("test");
    }

    public void throwsRuntimeException() throws Throwable {
        throw new RuntimeException("test");
    }

    public void throwsError() throws Throwable {
        throw new Error("test");
    }

    public void getJoinPointMetaData() throws Throwable {
        throw new Error("test");
    }
}
