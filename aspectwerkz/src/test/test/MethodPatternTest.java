/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test;

import junit.framework.TestCase;
import org.codehaus.aspectwerkz.regexp.MethodPattern;
import org.codehaus.aspectwerkz.regexp.Pattern;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class MethodPatternTest extends TestCase {

    public void testMatchMethodName1() {
        MethodPattern methodPattern = Pattern.compileMethodPattern("* method(String, int)");
        assertTrue(methodPattern.matchMethodName("method"));
        assertFalse(methodPattern.matchMethodName("methods"));
        assertFalse(methodPattern.matchMethodName("meth"));
        assertFalse(methodPattern.matchMethodName(""));
    }

    public void testMatchMethodName2() {
        MethodPattern methodPattern = Pattern.compileMethodPattern("* meth*(..)");
        assertTrue(methodPattern.matchMethodName("method"));
        assertTrue(methodPattern.matchMethodName("methods"));
        assertTrue(methodPattern.matchMethodName("meth"));
        assertFalse(methodPattern.matchMethodName("m"));
        assertFalse(methodPattern.matchMethodName(""));
    }

    public void testMatchMethodName3() {
        MethodPattern methodPattern = Pattern.compileMethodPattern("* *()");
        assertTrue(methodPattern.matchMethodName("method"));
        assertTrue(methodPattern.matchMethodName("methods"));
        assertTrue(methodPattern.matchMethodName("alsdkfj"));
        assertFalse(methodPattern.matchMethodName(""));
    }

    public void testMatchMethodName4() {
        MethodPattern methodPattern = Pattern.compileMethodPattern("* *th*()");
        assertTrue(methodPattern.matchMethodName("method"));
        assertTrue(methodPattern.matchMethodName("methods"));
        assertFalse(methodPattern.matchMethodName("sadlfkj"));
        assertFalse(methodPattern.matchMethodName(""));
    }

    public void testMatchMethodName5() {
        MethodPattern methodPattern = Pattern.compileMethodPattern("* A()");
        assertTrue(methodPattern.matchMethodName("A"));
        assertFalse(methodPattern.matchMethodName("a"));
        assertFalse(methodPattern.matchMethodName("B"));
        assertFalse(methodPattern.matchMethodName(""));
    }

    public void testMatchMethodName6() {
        MethodPattern methodPattern = Pattern.compileMethodPattern("* m_method()");
        assertTrue(methodPattern.matchMethodName("m_method"));
        assertFalse(methodPattern.matchMethodName("m"));
        assertFalse(methodPattern.matchMethodName("m_methods"));
        assertFalse(methodPattern.matchMethodName(""));
    }

    public void testMatchParameterTypes1() {
        MethodPattern methodPattern = Pattern.compileMethodPattern("* method(java.lang.String,..)");
        assertTrue(methodPattern.matchParameterTypes(
                new String[]{"java.lang.String","java.lang.String", "int"}));
        assertFalse(methodPattern.matchParameterTypes(
                new String[]{"java.lang.String"}));
        assertFalse(methodPattern.matchParameterTypes(
                new String[]{}));
    }

    public void testMatchParameterTypes2() {
        MethodPattern methodPattern = Pattern.compileMethodPattern("* method(*)");
        assertFalse(methodPattern.matchParameterTypes(
                new String[]{"java.lang.String","java.lang.String", "int"}));
        assertTrue(methodPattern.matchParameterTypes(
                new String[]{"java.lang.String"}));
        assertFalse(methodPattern.matchParameterTypes(
                new String[]{}));
    }

    public void testMatchParameterTypes3() {
        MethodPattern methodPattern = Pattern.compileMethodPattern("* method(..)");
        assertTrue(methodPattern.matchParameterTypes(
                new String[]{"java.lang.String","java.lang.String", "int"}));
        assertTrue(methodPattern.matchParameterTypes(
                new String[]{"java.lang.String"}));
        assertTrue(methodPattern.matchParameterTypes(
                new String[]{}));
    }

    public void testMatchParameterTypes4() {
        MethodPattern methodPattern = Pattern.compileMethodPattern("* method(java.lang.*)");
        assertFalse(methodPattern.matchParameterTypes(
                new String[]{"java.lang.String","java.lang.StringBuffer"}));
        assertTrue(methodPattern.matchParameterTypes(
                new String[]{"java.lang.String"}));
        assertTrue(methodPattern.matchParameterTypes(
                new String[]{"java.lang.StringBuffer"}));
        assertFalse(methodPattern.matchParameterTypes(
                new String[]{}));
    }

    public void testMatchParameterTypes5() {
        MethodPattern methodPattern = Pattern.compileMethodPattern("* method(*,String)");
        assertTrue(methodPattern.matchParameterTypes(
                new String[]{"java.lang.String","java.lang.String"}));
        assertFalse (methodPattern.matchParameterTypes(
                new String[]{"java.lang.String"}));
        assertFalse(methodPattern.matchParameterTypes(
                new String[]{}));
    }

    public void testMatchParameterTypes6() {
        MethodPattern methodPattern = Pattern.compileMethodPattern("* method()");
        assertFalse(methodPattern.matchParameterTypes(
                new String[]{"java.lang.String","java.lang.String"}));
        assertFalse (methodPattern.matchParameterTypes(
                new String[]{"java.lang.String"}));
        assertTrue(methodPattern.matchParameterTypes(
                new String[]{}));
    }

    public void testMatchParameterTypes7() {
        MethodPattern methodPattern = Pattern.compileMethodPattern("* method(String, List)");
        assertTrue(methodPattern.matchParameterTypes(
                new String[]{"java.lang.String","java.util.List"}));
        assertFalse (methodPattern.matchParameterTypes(
                new String[]{"java.lang.String"}));
    }

    public void testMatchParameterTypes8() {
        MethodPattern methodPattern = Pattern.compileMethodPattern("* method(String, ..)");
        assertTrue(methodPattern.matchParameterTypes(
                new String[]{"java.lang.String","java.util.List"}));
        assertTrue(methodPattern.matchParameterTypes(
                new String[]{"java.lang.String","java.util.List","int"}));
        assertFalse(methodPattern.matchParameterTypes(
                new String[]{"java.lang.String"}));
    }

    public void testMatchParameterTypes9() {
        MethodPattern methodPattern = Pattern.compileMethodPattern("* method(java.lang.String[])");
        assertTrue(methodPattern.matchParameterTypes(
                new String[]{"java.lang.String[]"}));
        assertFalse(methodPattern.matchParameterTypes(
                new String[]{"java.lang.String"}));
        assertFalse(methodPattern.matchParameterTypes(
                new String[]{"java.lang.String[][]"}));
    }

    public void testMatchParameterTypes10() {
        MethodPattern methodPattern = Pattern.compileMethodPattern("* method(java.lang.String[][])");
        assertTrue(methodPattern.matchParameterTypes(
                new String[]{"java.lang.String[][]"}));
        assertFalse(methodPattern.matchParameterTypes(
                new String[]{"java.lang.String[]"}));
        assertFalse(methodPattern.matchParameterTypes(
                new String[]{"java.lang.String[][][]"}));
    }

    public void testMatchParameterTypes11() {
        MethodPattern methodPattern = Pattern.compileMethodPattern("* method(java.*.*[])");
        assertTrue(methodPattern.matchParameterTypes(
                new String[]{"java.lang.String[]"}));
        assertTrue(methodPattern.matchParameterTypes(
                new String[]{"java.util.List[]"}));
        assertFalse(methodPattern.matchParameterTypes(
                new String[]{"java.util.List[][]"}));
    }

    public void testMatchParameterTypes12() {
        MethodPattern methodPattern = Pattern.compileMethodPattern("* method(java.lang.String*)");
        assertTrue(methodPattern.matchParameterTypes(new String[]{"java.lang.String"}));
        assertTrue(methodPattern.matchParameterTypes(new String[]{"java.lang.StringBuffer"}));
        assertFalse(methodPattern.matchParameterTypes(new String[]{"java.lang.String", "int"}));
    }

    public void testMatchReturnType1() {
        MethodPattern methodPattern = Pattern.compileMethodPattern("* method()");
        assertTrue(methodPattern.matchReturnType("int"));
        assertTrue(methodPattern.matchReturnType("java.lang.String"));
        assertTrue(methodPattern.matchReturnType("String"));
        assertFalse(methodPattern.matchReturnType(""));
    }

    public void testMatchReturnType2() {
        MethodPattern methodPattern = Pattern.compileMethodPattern("String method()");
        assertFalse(methodPattern.matchReturnType("int"));
        assertTrue(methodPattern.matchReturnType("java.lang.String"));
        assertFalse(methodPattern.matchReturnType("String"));
        assertFalse(methodPattern.matchReturnType(""));
    }

    public void testMatchReturnType3() {
        MethodPattern methodPattern = Pattern.compileMethodPattern("java.lang.String method()");

        assertFalse(methodPattern.matchReturnType("int"));
        assertTrue(methodPattern.matchReturnType("java.lang.String"));
        assertFalse(methodPattern.matchReturnType("java.foo.String"));
        assertFalse(methodPattern.matchReturnType("String"));
        assertFalse(methodPattern.matchReturnType(""));
    }

    public void testMatchReturnType4() {
        MethodPattern methodPattern = Pattern.compileMethodPattern("java.lang.* method()");

        assertFalse(methodPattern.matchReturnType("int"));
        assertTrue(methodPattern.matchReturnType("java.lang.String"));
        assertTrue(methodPattern.matchReturnType("java.lang.StringBuffer"));
        assertTrue(methodPattern.matchReturnType("java.lang.Bar"));
        assertFalse(methodPattern.matchReturnType("java.foo.String"));
        assertFalse(methodPattern.matchReturnType("String"));
        assertFalse(methodPattern.matchReturnType(""));
    }

    public void testMatchReturnType5() {
        MethodPattern methodPattern = Pattern.compileMethodPattern("void method()");

        assertFalse(methodPattern.matchReturnType("int"));
        assertTrue(methodPattern.matchReturnType("void"));
        assertFalse(methodPattern.matchReturnType("java.foo.String"));
        assertFalse(methodPattern.matchReturnType("String"));
        assertFalse(methodPattern.matchReturnType(""));
    }

    public void testMatchReturnType6() {
        MethodPattern methodPattern = Pattern.compileMethodPattern("java.lang.String* method()");

        assertFalse(methodPattern.matchReturnType("int"));
        assertTrue(methodPattern.matchReturnType("java.lang.String"));
        assertTrue(methodPattern.matchReturnType("java.lang.StringBuffer"));
        assertFalse(methodPattern.matchReturnType("java.foo.String"));
        assertFalse(methodPattern.matchReturnType("String"));
        assertFalse(methodPattern.matchReturnType(""));
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(MethodPatternTest.class);
    }
}
