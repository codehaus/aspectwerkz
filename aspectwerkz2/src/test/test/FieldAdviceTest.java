/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test;

import junit.framework.TestCase;

import org.codehaus.aspectwerkz.SystemLoader;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class FieldAdviceTest extends TestCase {

    private static String s_logString = "";
    private int m_setFieldAroundAdviced = 0;
    private int m_setFieldPreAdviced = 0;
    private int m_setFieldPostAdviced = 0;
    private int m_setFieldPrePostAdviced = 0;
    private int m_getFieldAroundAdviced = 0;
    private int m_getFieldPreAdviced = 0;
    private int m_getFieldPostAdviced = 0;
    private int m_getFieldPrePostAdviced = 0;

    private static int s_setStaticFieldAroundAdviced = 0;
    private static int s_setStaticFieldPreAdviced = 0;
    private static int s_setStaticFieldPostAdviced = 0;
    private static int s_setStaticFieldPrePostAdviced = 0;
    private static int s_getStaticFieldAroundAdviced = 0;
    private static int s_getStaticFieldPreAdviced = 0;
    private static int s_getStaticFieldPostAdviced = 0;
    private static int s_getStaticFieldPrePostAdviced = 0;

    public void testSetMemberFieldAroundAdviced() {
        s_logString = "";
        try {
            setFieldAroundAdviced();
            assertEquals("before after ", s_logString);
        }
        catch (Exception e) {
            fail();
        }
    }

    public void testGetMemberFieldAroundAdviced() {
        s_logString = "";
        try {
            getFieldAroundAdviced();
            assertEquals("before after ", s_logString);
        }
        catch (Exception e) {
            fail();
        }
    }

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
            assertEquals("pre1 pre2 ", s_logString);
        }
        catch (Exception e) {
            e.printStackTrace();
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

    public void testSetStaticFieldAroundAdviced() {
        s_logString = "";
        try {
            setStaticFieldAroundAdviced();
            assertEquals("before after ", s_logString);
        }
        catch (Exception e) {
            fail();
        }
    }

    public void testGetStaticFieldAroundAdviced() {
        s_logString = "";
        try {
            getStaticFieldAroundAdviced();
            assertEquals("before after ", s_logString);
        }
        catch (Exception e) {
            fail();
        }
    }

    public void testSetStaticFieldPreAdviced() {
        s_logString = "";
        try {
            setStaticFieldPreAdviced();
            assertEquals("pre1 pre2 ", s_logString);
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
            assertEquals("pre1 pre2 ", s_logString);
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
        SystemLoader.getSystem("tests").initialize();
    }

    // ==== methods to test ====

    public static void log(final String wasHere) {
        s_logString += wasHere;
    }

    public void setFieldAroundAdviced() {
        m_setFieldAroundAdviced = 3 + 23 * 8;
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

    public int getFieldAroundAdviced() {
        return m_getFieldAroundAdviced;
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

    public static void setStaticFieldAroundAdviced() {
        s_setStaticFieldAroundAdviced = 3;
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

    public static int getStaticFieldAroundAdviced() {
        return s_getStaticFieldAroundAdviced;
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
