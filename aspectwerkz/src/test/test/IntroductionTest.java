package test;

import junit.framework.TestCase;
import org.codehaus.aspectwerkz.AspectWerkz;
import org.codehaus.aspectwerkz.Identifiable;

public class IntroductionTest extends TestCase implements Identifiable {

    private ToBeIntroduced m_toBeIntroduced;

    public void testReplaceImplementation() {
        assertEquals("test.IntroductionsImpl", AspectWerkz.getIntroduction("introductionReplacement").getImplementation());
        AspectWerkz.getIntroduction("introductionReplacement").swapImplementation("test.IntroductionsImplReplacement");
        assertEquals("test.IntroductionsImplReplacement", AspectWerkz.getIntroduction("introductionReplacement").getImplementation());
    }

    public void testGetInterface() {
        assertEquals("test.PerJVM", AspectWerkz.getIntroduction("introductionPerJVM").getInterface());
    }

    public void testGetImplementation() {
        assertEquals("test.PerJVMImpl", AspectWerkz.getIntroduction("introductionPerJVM").getImplementation());
    }

    public void testGetMethod() {
        assertEquals("runPerJVM", AspectWerkz.getIntroduction("introductionPerJVM").getMethod(0).getName());
    }

    public void testGetMethods() {
        assertEquals(1, AspectWerkz.getIntroduction("introductionPerJVM").getMethods().length);
    }

    public void testInvokePerJVM() {
        try {
            AspectWerkz.getIntroduction("introductionPerJVM").invoke(0, this);
        }
        catch (Exception e) {
            fail();
        }
    }

    public void testInvokePerClass() {
        try {
            AspectWerkz.getIntroduction("introductionPerClass").invoke(0, this);
        }
        catch (Exception e) {
            fail();
        }
    }

    public void testInvokePerInstance() {
        try {
            AspectWerkz.getIntroduction("introductionPerInstance").invoke(0, this);
        }
        catch (Exception e) {
            System.out.println("e = " + e);
            fail();
        }
    }

    public void testInvokePerThread() {
        try {
            AspectWerkz.getIntroduction("introductionPerThread").invoke(0, this);
        }
        catch (Exception e) {
            fail();
        }
    }

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

    public void testVariousArguments1() {
        assertEquals("dummy".hashCode() + 1 + (int)2.3F, this.hashCode() + (int)34L,
                ((Introductions)m_toBeIntroduced).variousArguments1("dummy", 1, 2.3F, this, 34L));
    }

    public void testVariousArguments2() {
        assertEquals((int)2.3F + 1 + "dummy".hashCode() + this.hashCode() + (int)34L + "test".hashCode(),
                ((Introductions)m_toBeIntroduced).variousArguments2(2.3F, 1, "dummy", this, 34L, "test"));
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(IntroductionTest.class);
    }

    public IntroductionTest(String name) {
        super(name);
        m_toBeIntroduced = new ToBeIntroduced();
        AspectWerkz.initialize();
    }

    public String getUuid() {
        return "ZZZZZZZZZZZZZZZZZZZZZZZZZZ";
    }
}
