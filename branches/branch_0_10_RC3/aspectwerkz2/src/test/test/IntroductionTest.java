/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test;

import org.codehaus.aspectwerkz.SystemLoader;
import org.codehaus.aspectwerkz.WeavedTestCase;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class IntroductionTest extends WeavedTestCase {

    private ToBeIntroduced m_toBeIntroduced;

    public void testInterfaceIntroduction() {
        assertTrue(m_toBeIntroduced instanceof java.io.Serializable);
        assertTrue(m_toBeIntroduced instanceof test.Introductions);
    }

    public void testReturnVoid() {
        try {
            ((Introductions)m_toBeIntroduced).getVoid();
        }
        catch (RuntimeException e) {
            fail(e.getMessage());
        }
    }

    public void testReturnLong() {
        assertEquals(1L, ((Introductions)m_toBeIntroduced).getLong());
    }

    public void testReturnInt() {
        assertEquals(1, ((Introductions)m_toBeIntroduced).getInt());
    }

    public void testReturnShort() {
        assertEquals(1, ((Introductions)m_toBeIntroduced).getShort());
    }

    public void testReturnDouble() {
        assertEquals(new Double(1.1D), new Double(((Introductions)m_toBeIntroduced).getDouble()));
    }

    public void testReturnFloat() {
        assertEquals(new Float(1.1F), new Float(((Introductions)m_toBeIntroduced).getFloat()));
    }

    public void testReturnByte() {
        assertEquals(Byte.parseByte("1"), ((Introductions)m_toBeIntroduced).getByte());
    }

    public void testReturnChar() {
        assertEquals('A', ((Introductions)m_toBeIntroduced).getChar());
    }

    public void testReturnBoolean() {
        assertEquals(true, ((Introductions)m_toBeIntroduced).getBoolean());
    }

    public void testNoArgs() {
        try {
            ((Introductions)m_toBeIntroduced).noArgs();
        }
        catch (Exception e) {
            fail();
        }
    }

    public void testIntArg() {
        assertEquals(12, ((Introductions)m_toBeIntroduced).intArg(12));
    }

    public void testLongArg() {
        long result = ((Introductions)m_toBeIntroduced).longArg(12L);
        assertEquals(12L, result);
    }

    public void testShortArg() {
        assertEquals((short)3, ((Introductions)m_toBeIntroduced).shortArg((short)3));
    }

    public void testDoubleArg() {
        assertEquals(new Double(2.3D), new Double(((Introductions)m_toBeIntroduced).doubleArg(2.3D)));
    }

    public void testFloatArg() {
        assertEquals(new Float(2.3F), new Float(((Introductions)m_toBeIntroduced).floatArg(2.3F)));
    }

    public void testByteArg() {
        assertEquals(Byte.parseByte("1"), ((Introductions)m_toBeIntroduced).byteArg(Byte.parseByte("1")));
    }

    public void testCharArg() {
        assertEquals('B', ((Introductions)m_toBeIntroduced).charArg('B'));
    }

    public void testBooleanArg() {
        assertTrue(!((Introductions)m_toBeIntroduced).booleanArg(false));
    }

    public void testObjectArg() {
        assertEquals("test", ((Introductions)m_toBeIntroduced).objectArg("test"));
    }

    public void testArrayArg() {
        String[] strings = new String[0];
        try {
            strings = ((Introductions)m_toBeIntroduced).arrayArg(new String[]{"test1", "test2"});
        }
        catch (Throwable e) {
            System.out.println("e = " + e);
        }
        assertEquals("test1", strings[0]);
        assertEquals("test2", strings[1]);
    }

    public void testVariousArguments1() {
        assertEquals(
                "dummy".hashCode() + 1 + (int)2.3F, this.hashCode() + (int)34L,
                ((Introductions)m_toBeIntroduced).variousArguments1("dummy", 1, 2.3F, this, 34L)
        );
    }

    public void testVariousArguments2() {
        assertEquals(
                (int)2.3F + 1 + "dummy".hashCode() + this.hashCode() + (int)34L + "test".hashCode(),
                ((Introductions)m_toBeIntroduced).variousArguments2(2.3F, 1, "dummy", this, 34L, "test")
        );
    }

    public void testReplaceImplementation() {
        assertEquals(
                "test.aspect.IntroductionTestAspect$MyImpl",
                SystemLoader.getSystem("tests").
                getAspectManager().
                getMixin("test.aspect.IntroductionTestAspect$MyImpl").
                getImplementationClassName()
        );

        assertEquals(1, ((Introductions)m_toBeIntroduced).intArg(1));

        // swap with an inner class
        SystemLoader.getSystem("tests").
                getAspectManager().
                getMixin("test.aspect.IntroductionTestAspect$MyImpl").
                swapImplementation("test.aspect.IntroductionTestAspect$MyOtherImpl");

        assertEquals(-1, ((Introductions)m_toBeIntroduced).intArg(1));

        assertEquals(
                "test.aspect.IntroductionTestAspect$MyOtherImpl",
                SystemLoader.getSystem("tests").
                getAspectManager().
                getMixin("test.aspect.IntroductionTestAspect$MyImpl").
                getImplementationClassName()
        );
    }

    public void testReplaceImplementationToAutonomousOne() {
        assertEquals(
                "test.aspect.IntroductionTestAspect$MyOtherImpl",
                SystemLoader.getSystem("tests").
                getAspectManager().
                getMixin("test.aspect.IntroductionTestAspect$MyImpl").
                getImplementationClassName()
        );

        assertEquals(-1, ((Introductions)m_toBeIntroduced).intArg(1));

        // swap with an outer class
        SystemLoader.getSystem("tests").
                getAspectManager().
                getMixin("test.aspect.IntroductionTestAspect$MyImpl").
                swapImplementation("test.aspect.IntroductionTestAspectMyImplReplacement");

        assertEquals(-2, ((Introductions)m_toBeIntroduced).intArg(1));

        assertEquals(
                "test.aspect.IntroductionTestAspectMyImplReplacement",
                SystemLoader.getSystem("tests").
                getAspectManager().
                getMixin("test.aspect.IntroductionTestAspect$MyImpl").
                getImplementationClassName()
        );
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        //TODO: on IBM JRE, test method order is changed, and thus mixin replacement is done first
        // leading to some test failure.
        return new junit.framework.TestSuite(IntroductionTest.class);
    }

    public IntroductionTest(String name) {
        super(name);
        m_toBeIntroduced = new ToBeIntroduced();
        SystemLoader.getSystem("tests").initialize();
    }

    public String ___AW_getUuid() {
        return "ZZZZZZZZZZZZZZZZZZZZZZZZZZ";
    }
}
