/*
 * AspectWerkz AOP Framework.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package test;

import junit.framework.TestCase;
import org.codehaus.aspectwerkz.definition.regexp.MethodPattern;
import org.codehaus.aspectwerkz.definition.regexp.Pattern;

/**
 *
 * @author <a href="mailto:jboner@acm.org">Jonas Bonér</a>
 * @version $Id: MethodPatternTest.java,v 1.2 2003-05-12 09:20:46 jboner Exp $
 */
public class MethodPatternTest extends TestCase {

    public void testMatchMethodName1() {
        MethodPattern methodPattern = Pattern.compileMethodPattern("* method()");
        assertTrue(methodPattern.matchMethodName("method"));
        assertFalse(methodPattern.matchMethodName("methods"));
        assertFalse(methodPattern.matchMethodName("meth"));
        assertFalse(methodPattern.matchMethodName(""));
    }

    public void testMatchMethodName2() {
        MethodPattern methodPattern = Pattern.compileMethodPattern("* meth*()");
        assertTrue(methodPattern.matchMethodName("method"));
        assertTrue(methodPattern.matchMethodName("methods"));
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

    public void testMatchParameterTypes1() {
        MethodPattern methodPattern = Pattern.compileMethodPattern("* method(java.lang.String,..)");
        assertTrue(methodPattern.matchParameterTypes(
                new String[]{"java.lang.String","java.lang.String", "int"}));
        assertTrue(methodPattern.matchParameterTypes(
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
        assertTrue (methodPattern.matchParameterTypes(
                new String[]{"java.lang.String"}));
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

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(MethodPatternTest.class);
    }
}
