/*
 * AspectWerkz AOP Framework.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package test;

import junit.framework.TestCase;
import org.codehaus.aspectwerkz.definition.regexp.Pattern;
import org.codehaus.aspectwerkz.definition.regexp.ClassPattern;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: ClassPatternTest.java,v 1.4 2003-06-09 07:04:13 jboner Exp $
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
