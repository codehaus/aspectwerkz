/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test;

import junit.framework.TestCase;
import org.codehaus.aspectwerkz.MethodComparator;

import java.lang.reflect.Array;
import java.lang.reflect.Method;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public class MethodComparatorTest extends TestCase {
    public void testCompare() {
        Method method1 = null;
        Method method11 = null;
        Method method2 = null;
        Method method3 = null;
        Method method4 = null;
        Method method5 = null;
        Method method6 = null;
        try {
            method1 = this.getClass().getMethod("__generated$_AW_$method1", new Class[] {});
            method11 = this.getClass().getMethod("__generated$_AW_$method1$x", new Class[] {});
            method2 = this.getClass().getMethod("__generated$_AW_$method1", new Class[] {
                int.class
            });
            method3 = this.getClass().getMethod("__generated$_AW_$method2", new Class[] {});
            method4 = this.getClass().getMethod("__generated$_AW_$method2", new Class[] {
                int.class
            });
            method5 = this.getClass().getMethod("__generated$_AW_$method2", new Class[] {
                String.class
            });
            method6 = this.getClass().getMethod("__generated$_AW_$method2", new Class[] {
                Array.newInstance(String.class, 1).getClass()
            });
        } catch (Exception e) {
            throw new RuntimeException("exception unexpected: " + e);
        }
        assertTrue(0 == MethodComparator.getInstance(MethodComparator.PREFIXED_METHOD).compare(
            method1,
            method1));
        assertTrue(0 == MethodComparator.getInstance(MethodComparator.PREFIXED_METHOD).compare(
            method2,
            method2));
        assertTrue(0 > MethodComparator.getInstance(MethodComparator.PREFIXED_METHOD).compare(
            method1,
            method2));
        assertTrue(0 < MethodComparator.getInstance(MethodComparator.PREFIXED_METHOD).compare(
            method2,
            method1));
        assertTrue(0 > MethodComparator.getInstance(MethodComparator.PREFIXED_METHOD).compare(
            method1,
            method11));
        assertTrue(0 > MethodComparator.getInstance(MethodComparator.PREFIXED_METHOD).compare(
            method3,
            method4));
        assertTrue(0 < MethodComparator.getInstance(MethodComparator.PREFIXED_METHOD).compare(
            method4,
            method3));
        assertTrue(0 > MethodComparator.getInstance(MethodComparator.PREFIXED_METHOD).compare(
            method1,
            method4));
        assertTrue(0 < MethodComparator.getInstance(MethodComparator.PREFIXED_METHOD).compare(
            method4,
            method1));
        assertTrue(0 < MethodComparator.getInstance(MethodComparator.PREFIXED_METHOD).compare(
            method3,
            method2));
        assertTrue(0 > MethodComparator.getInstance(MethodComparator.PREFIXED_METHOD).compare(
            method2,
            method3));
        assertTrue(0 > MethodComparator.getInstance(MethodComparator.PREFIXED_METHOD).compare(
            method4,
            method5));

        // AW-104 test
        assertTrue(0 > MethodComparator.getInstance(MethodComparator.PREFIXED_METHOD).compare(
            method5,
            method6));
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(MethodComparatorTest.class);
    }

    public void __generated$_AW_$method1() {
    }

    public void __generated$_AW_$method1$x() {
    }

    public void __generated$_AW_$method1(int i) {
    }

    public void __generated$_AW_$method2() {
    }

    public void __generated$_AW_$method2(int i) {
    }

    public void __generated$_AW_$method2(String i) {
    }

    public void __generated$_AW_$method2(String[] i) {
    }
}