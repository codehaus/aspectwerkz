/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.orthogonal;

import junit.framework.TestCase;

import org.codehaus.aspectwerkz.SystemLoader;
import test.Loggable;
import test.MemberMethodAdviceTest;
import org.codehaus.aspectwerkz.WeavedTestCase;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class OrthogonalTest extends WeavedTestCase implements Loggable {

    private String m_logString = "";

    private int m_setFieldAroundAdviced = 0;
    private int m_getFieldAroundAdviced = 0;

    public void testMethodAdvice() {
        m_logString = "";
        methodAdvicedMethod();
        assertEquals("before invocation after ", m_logString);
    }

    public void testSetField() {
        m_logString = "";
        setField();
        assertEquals("before after ", m_logString);
    }

    public void testGetField() {
        m_logString = "";
        getField();
        assertEquals("before after ", m_logString);
    }




    // call

    // ctor




    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(OrthogonalTest.class);
    }

    public OrthogonalTest() {
    }

    public OrthogonalTest(String name) {
        super(name);
    }

    // ==== methods to test ====

    public void log(final String wasHere) {
        m_logString += wasHere;
    }

    public void methodAdvicedMethod() {
        log("invocation ");
    }

    public void getField() {
        int local = m_getFieldAroundAdviced;
    }

    public void setField() {
        int local = 1;
        m_setFieldAroundAdviced = 1;
    }

}
