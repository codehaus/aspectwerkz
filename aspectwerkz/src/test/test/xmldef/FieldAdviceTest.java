/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test;

import junit.framework.TestCase;

import org.codehaus.aspectwerkz.xmldef.AspectWerkz;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class FieldAdviceTest extends TestCase {//implements Loggable {

    private static String s_logString = "";
    private int m_setFieldPreAdviced = 0;
    private int m_setFieldPostAdviced = 0;
    private int m_setFieldPrePostAdviced = 0;
    private int m_getFieldPreAdviced = 0;
    private int m_getFieldPostAdviced = 0;
    private int m_getFieldPrePostAdviced = 0;

    private static int s_setStaticFieldPreAdviced = 0;
    private static int s_setStaticFieldPostAdviced = 0;
    private static int s_setStaticFieldPrePostAdviced = 0;
    private static int s_getStaticFieldPreAdviced = 0;
    private static int s_getStaticFieldPostAdviced = 0;
    private static int s_getStaticFieldPrePostAdviced = 0;

    public void testSetFieldPreAdviced() {
        s_logString = "";
        try {
            setFieldPreAdviced();
            assertEquals("pre1 pre2 ", s_logString);
        }
        catch (Exception e) {
            fail();
        }
    }

    public void testSetFieldPostAdviced() {
        s_logString = "";
        try {
            setFieldPostAdviced();
            assertEquals("post2 post1 ", s_logString);
        }
        catch (Exception e) {
            fail();
        }
    }

    public void testSetFieldPrePostAdviced() {
        s_logString = "";
        try {
            setFieldPrePostAdviced();
            assertEquals("pre1 pre2 post2 post1 ", s_logString);
        }
        catch (Exception e) {
            fail();
        }
    }

    public void testGetFieldPreAdviced() {
        s_logString = "";
        try {
            getFieldPreAdviced();
            assertEquals("pre2 pre1 ", s_logString);
        }
        catch (Exception e) {
            fail();
        }
    }

    public void testGetFieldPostAdviced() {
        s_logString = "";
        try {
            getFieldPostAdviced();
            assertEquals("post2 post1 ", s_logString);
        }
        catch (Exception e) {
            fail();
        }
    }

    public void testGetFieldPrePostAdviced() {
        s_logString = "";
        try {
            getFieldPrePostAdviced();
            assertEquals("pre1 pre2 post2 post1 ", s_logString);
        }
        catch (Exception e) {
            fail();
        }
    }

    public void testSetStaticFieldPreAdviced() {
        s_logString = "";
        try {
            setStaticFieldPreAdviced();
            assertEquals("pre2 pre1 ", s_logString);
        }
        catch (Exception e) {
            fail();
        }
    }

    public void testSetStaticFieldPostAdviced() {
        s_logString = "";
        try {
            setStaticFieldPostAdviced();
            assertEquals("post2 post1 ", s_logString);
        }
        catch (Exception e) {
            fail();
        }
    }

    public void testSetStaticFieldPrePostAdviced() {
        s_logString = "";
        try {
            setStaticFieldPrePostAdviced();
            assertEquals("pre1 pre2 post2 post1 ", s_logString);
        }
        catch (Exception e) {
            fail();
        }
    }

    public void testGetStaticFieldPreAdviced() {
        s_logString = "";
        try {
            getStaticFieldPreAdviced();
            assertEquals("pre2 pre1 ", s_logString);
        }
        catch (Exception e) {
            fail();
        }
    }

    public void testGetStaticFieldPostAdviced() {
        s_logString = "";
        try {
            getStaticFieldPostAdviced();
            assertEquals("post2 post1 ", s_logString);
        }
        catch (Exception e) {
            fail();
        }
    }

    public void testStaticGetFieldPrePostAdviced() {
        s_logString = "";
        try {
            getStaticFieldPrePostAdviced();
            assertEquals("pre1 pre2 post2 post1 ", s_logString);
        }
        catch (Exception e) {
            fail();
        }
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(FieldAdviceTest.class);
    }

    public FieldAdviceTest() {}

    public FieldAdviceTest(String name) {
        super(name);
        AspectWerkz.getSystem("tests").initialize();
    }

    // ==== methods to test ====

    public static void log(final String wasHere) {
        s_logString += wasHere;
    }

    public void setFieldPreAdviced() {
        m_setFieldPreAdviced = 3 + 23 * 8;
    }

    public void setFieldPostAdviced() {
        m_setFieldPostAdviced = 3;
    }

    public void setFieldPrePostAdviced() {
        m_setFieldPrePostAdviced = 3;
    }

    public int getFieldPreAdviced() {
        return m_getFieldPreAdviced;
    }

    public int getFieldPostAdviced() {
        return m_getFieldPostAdviced;
    }

    public int getFieldPrePostAdviced() {
        return m_getFieldPrePostAdviced;
    }

    public static void setStaticFieldPreAdviced() {
        s_setStaticFieldPreAdviced = 3;
    }

    public static void setStaticFieldPostAdviced() {
        s_setStaticFieldPostAdviced = 3;
    }

    public static void setStaticFieldPrePostAdviced() {
        s_setStaticFieldPrePostAdviced = 3;
    }

    public static int getStaticFieldPreAdviced() {
        return s_getStaticFieldPreAdviced;
    }

    public static int getStaticFieldPostAdviced() {
        return s_getStaticFieldPostAdviced;
    }

    public static int getStaticFieldPrePostAdviced() {
        return s_getStaticFieldPrePostAdviced;
    }
}
