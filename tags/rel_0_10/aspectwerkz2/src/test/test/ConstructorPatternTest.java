/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test;

import junit.framework.TestCase;
import org.codehaus.aspectwerkz.regexp.Pattern;
import org.codehaus.aspectwerkz.regexp.ConstructorPattern;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class ConstructorPatternTest extends TestCase {

    public void testMatch1() {
        ConstructorPattern constructorPattern = Pattern.compileConstructorPattern("new(String, int)");
        assertTrue(constructorPattern.matchConstructorName("new"));
        assertFalse(constructorPattern.matchConstructorName("methods"));
        assertFalse(constructorPattern.matchConstructorName("meth"));
        assertFalse(constructorPattern.matchConstructorName(""));
    }

    public void testMatch2() {
        ConstructorPattern constructorPattern = Pattern.compileConstructorPattern("new(..)");
        assertTrue(constructorPattern.matchConstructorName("new"));
        assertFalse(constructorPattern.matchConstructorName("news"));
        assertFalse(constructorPattern.matchConstructorName(""));
    }

    public void testMatchParameterTypes1() {
        ConstructorPattern constructorPattern = Pattern.compileConstructorPattern("new(java.lang.String,..)");
        assertTrue(
                constructorPattern.matchParameterTypes(new String[]{"java.lang.String", "java.lang.String", "int"})
        );
        assertFalse(constructorPattern.matchParameterTypes(new String[]{"java.lang.String"}));
        assertFalse(constructorPattern.matchParameterTypes(new String[]{}));
    }

    public void testMatchParameterTypes2() {
        ConstructorPattern constructorPattern = Pattern.compileConstructorPattern("new(*)");
        assertFalse(
                constructorPattern.matchParameterTypes(new String[]{"java.lang.String", "java.lang.String", "int"})
        );
        assertTrue(constructorPattern.matchParameterTypes(new String[]{"java.lang.String"}));
        assertFalse(constructorPattern.matchParameterTypes(new String[]{}));
    }

    public void testMatchParameterTypes3() {
        ConstructorPattern constructorPattern = Pattern.compileConstructorPattern("new(..)");
        assertTrue(
                constructorPattern.matchParameterTypes(new String[]{"java.lang.String", "java.lang.String", "int"})
        );
        assertTrue(constructorPattern.matchParameterTypes(new String[]{"java.lang.String"}));
        assertTrue(constructorPattern.matchParameterTypes(new String[]{}));
    }

    public void testMatchParameterTypes4() {
        ConstructorPattern constructorPattern = Pattern.compileConstructorPattern("new(java.lang.*)");
        assertFalse(
                constructorPattern.matchParameterTypes(new String[]{"java.lang.String", "java.lang.StringBuffer"})
        );
        assertTrue(constructorPattern.matchParameterTypes(new String[]{"java.lang.String"}));
        assertTrue(constructorPattern.matchParameterTypes(new String[]{"java.lang.StringBuffer"}));
        assertFalse(constructorPattern.matchParameterTypes(new String[]{}));
    }

    public void testMatchParameterTypes5() {
        ConstructorPattern constructorPattern = Pattern.compileConstructorPattern("new(*,String)");
        assertTrue(constructorPattern.matchParameterTypes(new String[]{"java.lang.String", "java.lang.String"}));
        assertFalse(constructorPattern.matchParameterTypes(new String[]{"java.lang.String"}));
        assertFalse(constructorPattern.matchParameterTypes(new String[]{}));
    }

    public void testMatchParameterTypes6() {
        ConstructorPattern constructorPattern = Pattern.compileConstructorPattern("new()");
        assertFalse(constructorPattern.matchParameterTypes(new String[]{"java.lang.String", "java.lang.String"}));
        assertFalse(constructorPattern.matchParameterTypes(new String[]{"java.lang.String"}));
        assertTrue(constructorPattern.matchParameterTypes(new String[]{}));
    }

    public void testMatchParameterTypes7() {
        ConstructorPattern constructorPattern = Pattern.compileConstructorPattern("new(String, List)");
        assertTrue(constructorPattern.matchParameterTypes(new String[]{"java.lang.String", "java.util.List"}));
        assertFalse(constructorPattern.matchParameterTypes(new String[]{"java.lang.String"}));
    }

    public void testMatchParameterTypes8() {
        ConstructorPattern constructorPattern = Pattern.compileConstructorPattern("new(String, ..)");
        assertTrue(constructorPattern.matchParameterTypes(new String[]{"java.lang.String", "java.util.List"}));
        assertTrue(constructorPattern.matchParameterTypes(new String[]{"java.lang.String", "java.util.List", "int"}));
        assertFalse(constructorPattern.matchParameterTypes(new String[]{"java.lang.String"}));
    }

    public void testMatchParameterTypes9() {
        ConstructorPattern constructorPattern = Pattern.compileConstructorPattern("new(java.lang.String[])");
        assertTrue(constructorPattern.matchParameterTypes(new String[]{"java.lang.String[]"}));
        assertFalse(constructorPattern.matchParameterTypes(new String[]{"java.lang.String"}));
        assertFalse(constructorPattern.matchParameterTypes(new String[]{"java.lang.String[][]"}));
    }

    public void testMatchParameterTypes10() {
        ConstructorPattern constructorPattern = Pattern.compileConstructorPattern("new(java.lang.String[][])");
        assertTrue(constructorPattern.matchParameterTypes(new String[]{"java.lang.String[][]"}));
        assertFalse(constructorPattern.matchParameterTypes(new String[]{"java.lang.String[]"}));
        assertFalse(constructorPattern.matchParameterTypes(new String[]{"java.lang.String[][][]"}));
    }

    public void testMatchParameterTypes11() {
        ConstructorPattern constructorPattern = Pattern.compileConstructorPattern("new(java.*.*[])");
        assertTrue(constructorPattern.matchParameterTypes(new String[]{"java.lang.String[]"}));
        assertTrue(constructorPattern.matchParameterTypes(new String[]{"java.util.List[]"}));
        assertFalse(constructorPattern.matchParameterTypes(new String[]{"java.util.List[][]"}));
    }

    public void testMatchParameterTypes12() {
        ConstructorPattern constructorPattern = Pattern.compileConstructorPattern("new(java.lang.String*)");
        assertTrue(constructorPattern.matchParameterTypes(new String[]{"java.lang.String"}));
        assertTrue(constructorPattern.matchParameterTypes(new String[]{"java.lang.StringBuffer"}));
        assertFalse(constructorPattern.matchParameterTypes(new String[]{"java.lang.String", "int"}));
    }

    public void testMatchParameterTypes13() {
        //AW-91
        ConstructorPattern constructorPattern = Pattern.compileConstructorPattern("new(..)");
        assertTrue(constructorPattern.matchParameterTypes(new String[]{}));
        assertTrue(constructorPattern.matchParameterTypes(new String[]{"java.lang.Integer[]"}));
        assertTrue(constructorPattern.matchParameterTypes(new String[]{"int[]"}));
        assertTrue(constructorPattern.matchParameterTypes(new String[]{"java.lang.Integer[][]"}));
        assertTrue(constructorPattern.matchParameterTypes(new String[]{"int[][]"}));
    }

    public void testMatchParameterType14() {
        //AW-91
        ConstructorPattern constructorPattern = Pattern.compileConstructorPattern("new(java.lang.Integer*)");
        assertTrue(constructorPattern.matchParameterTypes(new String[]{"java.lang.Integer"}));
        assertFalse(constructorPattern.matchParameterTypes(new String[]{"java.lang.Integer[]"}));
    }

    public void testMatchParameterType15() {
        //AW-91
        ConstructorPattern constructorPattern = Pattern.compileConstructorPattern("new(Integer[])");
        assertTrue(constructorPattern.matchParameterTypes(new String[]{"java.lang.Integer[]"}));
        assertFalse(constructorPattern.matchParameterTypes(new String[]{"java.lang.Integer"}));

        constructorPattern = Pattern.compileConstructorPattern("new(Integer[][])");
        assertTrue(constructorPattern.matchParameterTypes(new String[]{"java.lang.Integer[][]"}));
        assertFalse(constructorPattern.matchParameterTypes(new String[]{"java.lang.Integer"}));
        assertFalse(constructorPattern.matchParameterTypes(new String[]{"java.lang.Integer[]"}));
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(ConstructorPatternTest.class);
    }
}
