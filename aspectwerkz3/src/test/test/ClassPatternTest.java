/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test;

import junit.framework.TestCase;

import org.codehaus.aspectwerkz.expression.regexp.Pattern;
import org.codehaus.aspectwerkz.expression.regexp.TypePattern;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public class ClassPatternTest extends TestCase
{
    public void testMatchMethodName1()
    {
        TypePattern classPattern = Pattern.compileTypePattern("foo.bar.SomeClass",
                false);

        assertFalse(classPattern.matches("SomeClass"));
        assertTrue(classPattern.matches("foo.bar.SomeClass"));
        assertFalse(classPattern.matches("Class"));
        assertFalse(classPattern.matches(""));
    }

    public void testMatchMethodName2()
    {
        TypePattern classPattern = Pattern.compileTypePattern("foo.bar.*", false);

        assertTrue(classPattern.matches("foo.bar.SomeClass"));
        assertTrue(classPattern.matches("foo.bar.SomeOtherClass"));
        assertFalse(classPattern.matches("SomeClass"));
        assertFalse(classPattern.matches(""));
    }

    public void testMatchMethodName3()
    {
        TypePattern classPattern = Pattern.compileTypePattern("foo.*.bar.SomeClass",
                false);

        assertTrue(classPattern.matches("foo.hey.bar.SomeClass"));
        assertTrue(classPattern.matches("foo.there.bar.SomeClass"));
        assertFalse(classPattern.matches("SomeClass"));
        assertFalse(classPattern.matches(""));
    }

    public void testMatchMethodName4()
    {
        TypePattern classPattern = Pattern.compileTypePattern("foo.ba*.*", false);

        assertTrue(classPattern.matches("foo.bag.SomeClass"));
        assertTrue(classPattern.matches("foo.bar.SomeClass"));
        assertTrue(classPattern.matches("foo.ba.SomeClass"));
        assertFalse(classPattern.matches("foo.bear.SomeClass"));
        assertFalse(classPattern.matches("foo"));
    }

    public void testMatchClassName5()
    {
        TypePattern classPattern = Pattern.compileTypePattern("foo..", false);

        assertTrue(classPattern.matches("foo.hey.bar.SomeClass"));
        assertTrue(classPattern.matches("foo.SomeClass"));
        assertTrue(classPattern.matches("foo.bar.SomeClass"));
        assertFalse(classPattern.matches("foo"));
    }

    public void testMatchClassName6()
    {
        TypePattern classPattern = Pattern.compileTypePattern("*", false);

        assertTrue(classPattern.matches("foo.hey.bar.SomeClass"));
        assertTrue(classPattern.matches("foo.SomeClass"));
        assertTrue(classPattern.matches("foo.bar.SomeClass"));
        assertTrue(classPattern.matches("foo"));
    }

    public void testMatchClassName7()
    {
        TypePattern classPattern = Pattern.compileTypePattern("..", false);

        assertTrue(classPattern.matches("foo.hey.bar.SomeClass"));
        assertTrue(classPattern.matches("foo.SomeClass"));
        assertTrue(classPattern.matches("foo.bar.SomeClass"));
        assertTrue(classPattern.matches("foo"));
    }

    public void testMatchClassName8()
    {
        TypePattern classPattern = Pattern.compileTypePattern("foo.bar..*",
                false);

        assertTrue(classPattern.matches("foo.bar.SomeClass"));
        assertTrue(classPattern.matches("foo.bar.baz.SomeClass"));
        assertTrue(classPattern.matches("foo.bar.baz.buzz.SomeClass"));
    }

    public void testMatchClassName9()
    {
        TypePattern classPattern = Pattern.compileTypePattern("foo.bar.Baz$Buzz",
                false);

        assertTrue(classPattern.matches("foo.bar.Baz$Buzz"));
        assertFalse(classPattern.matches("foo.bar.Baz"));
    }

    public void testMatchClassName10()
    {
        TypePattern classPattern = Pattern.compileTypePattern("foo.bar..$Buzz",
                false);

        assertTrue(classPattern.matches("foo.bar.Baz$Buzz"));
        assertTrue(classPattern.matches("foo.bar.Baz.Buz$Buzz"));
        assertFalse(classPattern.matches("foo.bar.Baz.Buz$Buz"));
        assertFalse(classPattern.matches("foo.bar.Baz"));
    }

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite()
    {
        return new junit.framework.TestSuite(ClassPatternTest.class);
    }
}
