/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test;

import junit.framework.TestCase;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Set;

import org.codehaus.aspectwerkz.annotation.Before;
import org.codehaus.aspectwerkz.annotation.Around;
import org.codehaus.aspectwerkz.joinpoint.StaticJoinPoint;

/**
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class InterfaceDefinedMethodTestCase extends TestCase {

    public static String s_log = "";

    public InterfaceDefinedMethodTestCase(String s) {
        super(s);
    }

    public InterfaceDefinedMethodTestCase() {
        SortedSet ss = new TreeSet();
        ss.add("foo"); // Warning, add is in super interface
        ss.first(); // Ok, first is in SortedSet

        try {
            Set s = ss;
            s.add("bar"); // Ok, add is in Set
            throw new NullPointerException("fake");
        } catch (NullPointerException npe) {
            ;
        }
    }

    /**
     * When visiting the bytecode of this method, the classInfo must lookup in the class + intf
     * hierarchy
     */
    public void testInterfaceDefinedMethod() {
        s_log = "";
        SortedSet ss = new TreeSet();
        ss.add("foo"); // Warning, add is in super interface
        ss.first(); // Ok, first is in SortedSet

        try {
            Set s = ss;
            s.add("bar"); // Ok, add is in Set
            throw new NullPointerException("fake");
        } catch (NullPointerException npe) {
            ;
        }
        assertEquals("advice advice advice advice advice advice advice ", s_log);
    }

    public void testWithinCtor() {
        s_log = "";
        InterfaceDefinedMethodTestCase me = new InterfaceDefinedMethodTestCase();
        assertEquals("around around around around around around around ", s_log);
    }

    public void testWithinNot() {
        s_log = "";
        withinNot();
        assertEquals("", s_log);
    }

    private void withinNot() {
        InterfaceDefinedMethodTestCase me = new InterfaceDefinedMethodTestCase("ignore");
    }


    public static class Aspect {

        @Before("withincode(* test.InterfaceDefinedMethodTestCase.testInterfaceDefinedMethod(..))")
        public void before(StaticJoinPoint sjp) {
            s_log += "advice ";
        }

        @Around("withincode(test.InterfaceDefinedMethodTestCase.new())")
        public Object around(StaticJoinPoint sjp) throws Throwable {
            s_log += "around ";
            return sjp.proceed();
        }

        @Before("cflow(call(* test.InterfaceDefinedMethodTestCase.withinNot()))" +
                "&& !withincode(* test.InterfaceDefinedMethodTestCase.withinNot())" +
                "&& within(test.InterfaceDefinedMethodTestCase)")
        public void neverCalled() {
            s_log += "no way";
        }
    }


    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(InterfaceDefinedMethodTestCase.class);
    }
}
