/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.xmldef;

import junit.framework.TestCase;

import org.codehaus.aspectwerkz.xmldef.XmlDefSystem;
import org.codehaus.aspectwerkz.SystemLoader;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
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
            e.printStackTrace();
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
                    "protected void test.xmldef.ThrowsAdviceTest.___AW_original_method$_AW_$getJoinPointMetaData$_AW_$1$_AW_$test_xmldef_ThrowsAdviceTest() throws java.lang.Throwable" +
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
        SystemLoader.getSystem("tests").initialize();
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
