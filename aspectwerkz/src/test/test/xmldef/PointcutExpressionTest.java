package test;

import junit.framework.TestCase;

import org.codehaus.aspectwerkz.AspectWerkz;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: PointcutExpressionTest.java,v 1.1 2003-07-03 13:12:03 jboner Exp $
 */
public class PointcutExpressionTest extends TestCase implements Loggable {

    private String m_logString = "";

    public void test_NEG() {
        m_logString = "";
        A();
        assertEquals("A ", m_logString);

        m_logString = "";
        B();
        assertEquals("before1 # B after1 ", m_logString);
    }


    public void test_OR() {
        m_logString = "";
        B();
        assertEquals("before1 # B after1 ", m_logString);

        m_logString = "";
        C();
        assertEquals("before1 # C after1 ", m_logString);
    }

    public void test_AND_NEG() {
        m_logString = "";
        D();
        assertEquals("# before1 D after1 ", m_logString);

        m_logString = "";
        E();
        assertEquals("# E ", m_logString);
    }

    public void test_OR_AND() {
        m_logString = "";
        F();
        assertEquals("# F ", m_logString);

        m_logString = "";
        G();
        assertEquals("# G ", m_logString);
    }

    public void test_OR_AND_GENERIC() {
        m_logString = "";
        I();
        assertEquals("before1 # I after1 ", m_logString);

        m_logString = "";
        J();
        assertEquals("before1 # J after1 ", m_logString);
    }

    public void test_COMPLEX() {
        m_logString = "";
        K();
        assertEquals("# K ", m_logString);

        m_logString = "";
        L();
        assertEquals("# L ", m_logString);

        m_logString = "";
        M();
        assertEquals("# M ", m_logString);

        m_logString = "";
        N();
        assertEquals("before1 # N after1 ", m_logString);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(PointcutExpressionTest.class);
    }

    public PointcutExpressionTest(String name) {
        super(name);
        AspectWerkz.getSystem("tests").initialize();
    }

    // ==== methods to test ====

    public void log(final String wasHere) {
        m_logString += wasHere;
    }

    public void A() {
        log("A ");
    }
    public void B() {
        log("B ");
    }
    public void C() {
        log("C ");
    }
    public void D() {
        log("D ");
    }
    public void E() {
        log("E ");
    }
    public void F() {
        log("F ");
    }
    public void G() {
        log("G ");
    }
    public void H() {
        log("H ");
    }
    public void I() {
        log("I ");
    }
    public void J() {
        log("J ");
    }
    public void K() {
        log("K ");
    }
    public void L() {
        log("L ");
    }
    public void M() {
        log("M ");
    }
    public void N() {
        log("N ");
    }
}
