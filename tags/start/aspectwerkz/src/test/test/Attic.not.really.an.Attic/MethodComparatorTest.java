package test;

import java.lang.reflect.Method;

import org.codehaus.aspectwerkz.AspectWerkz;
import org.codehaus.aspectwerkz.MethodComparator;
import junit.framework.TestCase;

public class MethodComparatorTest extends TestCase {

    public void testCompare() {
        Method method1 = null;
        Method method2 = null;
        Method method3 = null;
        Method method4 = null;
        Method method5 = null;
        try {
            method1 = this.getClass().getMethod("__generated$method1", new Class[]{});
            method2 = this.getClass().getMethod("__generated$method1", new Class[]{int.class});
            method3 = this.getClass().getMethod("__generated$method2", new Class[]{});
            method4 = this.getClass().getMethod("__generated$method2", new Class[]{int.class});
            method5 = this.getClass().getMethod("__generated$method2", new Class[]{String.class});
        }
        catch (Exception e) {
            throw new RuntimeException("exception unexpected: " + e);
        }
        assertTrue(0 == MethodComparator.getInstance(MethodComparator.PREFIXED_METHOD).compare(method1, method1));
        assertTrue(0 == MethodComparator.getInstance(MethodComparator.PREFIXED_METHOD).compare(method2, method2));
        assertTrue(0 > MethodComparator.getInstance(MethodComparator.PREFIXED_METHOD).compare(method1, method2));
        assertTrue(0 < MethodComparator.getInstance(MethodComparator.PREFIXED_METHOD).compare(method2, method1));
        assertTrue(0 > MethodComparator.getInstance(MethodComparator.PREFIXED_METHOD).compare(method3, method4));
        assertTrue(0 < MethodComparator.getInstance(MethodComparator.PREFIXED_METHOD).compare(method4, method3));
        assertTrue(0 > MethodComparator.getInstance(MethodComparator.PREFIXED_METHOD).compare(method1, method4));
        assertTrue(0 < MethodComparator.getInstance(MethodComparator.PREFIXED_METHOD).compare(method4, method1));
        assertTrue(0 < MethodComparator.getInstance(MethodComparator.PREFIXED_METHOD).compare(method3, method2));
        assertTrue(0 > MethodComparator.getInstance(MethodComparator.PREFIXED_METHOD).compare(method2, method3));
        assertTrue(0 > MethodComparator.getInstance(MethodComparator.PREFIXED_METHOD).compare(method4, method5));
        assertTrue(0 < MethodComparator.getInstance(MethodComparator.PREFIXED_METHOD).compare(method5, method4));
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(MethodComparatorTest.class);
    }

    public MethodComparatorTest(String name) {
        super(name);
        AspectWerkz.initialize();
    }

    public void __generated$method1() {
    }

    public void __generated$method1(int i) {
    }

    public void __generated$method2() {
    }

    public void __generated$method2(int i) {
    }

    public void __generated$method2(String i) {
    }
}
