/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.xmldef;

import junit.framework.TestCase;
import org.codehaus.aspectwerkz.regexp.Pattern;
import org.codehaus.aspectwerkz.regexp.ClassPattern;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public class ClassPatternTest extends TestCase {

    public void testMatchMethodName1() {
        ClassPattern classPattern = Pattern.compileClassPattern("foo.bar.SomeClass");
        assertFalse(classPattern.matches("SomeClass"));
        assertTrue(classPattern.matches("foo.bar.SomeClass"));
        assertFalse(classPattern.matches("Class"));
        assertFalse(classPattern.matches(""));
    }

    public void testMatchMethodName2() {
        ClassPattern classPattern = Pattern.compileClassPattern("foo.bar.*");
        assertTrue(classPattern.matches("foo.bar.SomeClass"));
        assertTrue(classPattern.matches("foo.bar.SomeOtherClass"));
        assertFalse(classPattern.matches("SomeClass"));
        assertFalse(classPattern.matches(""));
    }

    public void testMatchMethodName3() {
        ClassPattern classPattern = Pattern.compileClassPattern("foo.*.bar.SomeClass");
        assertTrue(classPattern.matches("foo.hey.bar.SomeClass"));
        assertTrue(classPattern.matches("foo.there.bar.SomeClass"));
        assertFalse(classPattern.matches("SomeClass"));
        assertFalse(classPattern.matches(""));
    }

    public void testMatchMethodName4() {
        ClassPattern classPattern = Pattern.compileClassPattern("foo.ba*.*");
        assertTrue(classPattern.matches("foo.bag.SomeClass"));
        assertTrue(classPattern.matches("foo.bar.SomeClass"));
        assertTrue(classPattern.matches("foo.ba.SomeClass"));
        assertFalse(classPattern.matches("foo.bear.SomeClass"));
        assertFalse(classPattern.matches("foo"));
    }

    public void testMatchMethodName5() {
        ClassPattern classPattern = Pattern.compileClassPattern("foo..");
        assertTrue(classPattern.matches("foo.hey.bar.SomeClass"));
        assertTrue(classPattern.matches("foo.SomeClass"));
        assertTrue(classPattern.matches("foo.bar.SomeClass"));
        assertFalse(classPattern.matches("foo"));
    }

    public void testMatchMethodName6() {
        ClassPattern classPattern = Pattern.compileClassPattern("*");
        assertTrue(classPattern.matches("foo.hey.bar.SomeClass"));
        assertTrue(classPattern.matches("foo.SomeClass"));
        assertTrue(classPattern.matches("foo.bar.SomeClass"));
        assertTrue(classPattern.matches("foo"));
    }

    public void testMatchMethodName7() {
        ClassPattern classPattern = Pattern.compileClassPattern("..");
        assertTrue(classPattern.matches("foo.hey.bar.SomeClass"));
        assertTrue(classPattern.matches("foo.SomeClass"));
        assertTrue(classPattern.matches("foo.bar.SomeClass"));
        assertTrue(classPattern.matches("foo"));
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(ClassPatternTest.class);
    }
}
