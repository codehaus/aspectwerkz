/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test;

import junit.framework.TestCase;
import org.codehaus.aspectwerkz.regexp.Pattern;
import org.codehaus.aspectwerkz.regexp.ClassPattern;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
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

    public void testMatchClassName5() {
        ClassPattern classPattern = Pattern.compileClassPattern("foo..");
        assertTrue(classPattern.matches("foo.hey.bar.SomeClass"));
        assertTrue(classPattern.matches("foo.SomeClass"));
        assertTrue(classPattern.matches("foo.bar.SomeClass"));
        assertFalse(classPattern.matches("foo"));
    }

    public void testMatchClassName6() {
        ClassPattern classPattern = Pattern.compileClassPattern("*");
        assertTrue(classPattern.matches("foo.hey.bar.SomeClass"));
        assertTrue(classPattern.matches("foo.SomeClass"));
        assertTrue(classPattern.matches("foo.bar.SomeClass"));
        assertTrue(classPattern.matches("foo"));
    }

    public void testMatchClassName7() {
        ClassPattern classPattern = Pattern.compileClassPattern("..");
        assertTrue(classPattern.matches("foo.hey.bar.SomeClass"));
        assertTrue(classPattern.matches("foo.SomeClass"));
        assertTrue(classPattern.matches("foo.bar.SomeClass"));
        assertTrue(classPattern.matches("foo"));
    }

    public void testMatchClassName8() {
        ClassPattern classPattern = Pattern.compileClassPattern("foo.bar..*");
        assertTrue(classPattern.matches("foo.bar.SomeClass"));
        assertTrue(classPattern.matches("foo.bar.baz.SomeClass"));
        assertTrue(classPattern.matches("foo.bar.baz.buzz.SomeClass"));
    }

    public void testMatchClassName9() {
        ClassPattern classPattern = Pattern.compileClassPattern("foo.bar.Baz$Buzz");
        assertTrue(classPattern.matches("foo.bar.Baz$Buzz"));
        assertFalse(classPattern.matches("foo.bar.Baz"));
    }

    public void testMatchClassName10() {
        ClassPattern classPattern = Pattern.compileClassPattern("foo.bar..$Buzz");
        assertTrue(classPattern.matches("foo.bar.Baz$Buzz"));
        assertTrue(classPattern.matches("foo.bar.Baz.Buz$Buzz"));
        assertFalse(classPattern.matches("foo.bar.Baz.Buz$Buz"));
        assertFalse(classPattern.matches("foo.bar.Baz"));
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(ClassPatternTest.class);
    }
}
