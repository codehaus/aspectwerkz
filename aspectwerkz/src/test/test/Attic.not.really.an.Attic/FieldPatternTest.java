/*
 * AspectWerkz AOP Framework.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package test;

import junit.framework.TestCase;
import org.codehaus.aspectwerkz.regexp.Pattern;
import org.codehaus.aspectwerkz.regexp.FieldPattern;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 * @version $Id: FieldPatternTest.java,v 1.4 2003-06-17 15:19:42 jboner Exp $
 */
public class FieldPatternTest extends TestCase {

    public void testMatchFieldName1() {
        FieldPattern fieldPattern = Pattern.compileFieldPattern("int m_field");

        assertFalse(fieldPattern.matchFieldName(""));
        assertTrue(fieldPattern.matchFieldName("m_field"));
        assertFalse(fieldPattern.matchFieldName("m_"));
    }

    public void testMatchFieldName2() {
        FieldPattern fieldPattern = Pattern.compileFieldPattern("int m_*");

        assertFalse(fieldPattern.matchFieldName(""));
        assertTrue(fieldPattern.matchFieldName("m_field"));
        assertTrue(fieldPattern.matchFieldName("m_"));
    }

    public void testMatchFieldName3() {
        FieldPattern fieldPattern = Pattern.compileFieldPattern("int *");

        assertFalse(fieldPattern.matchFieldName(""));
        assertTrue(fieldPattern.matchFieldName("m_field"));
        assertTrue(fieldPattern.matchFieldName("m_"));
    }

    public void testMatchFieldName4() {
        FieldPattern fieldPattern = Pattern.compileFieldPattern("int *ield");

        assertFalse(fieldPattern.matchFieldName(""));
        assertTrue(fieldPattern.matchFieldName("m_field"));
        assertFalse(fieldPattern.matchFieldName("m_"));
    }

    public void testMatchFieldName5() {
        FieldPattern fieldPattern = Pattern.compileFieldPattern("int m_*ld");

        assertFalse(fieldPattern.matchFieldName(""));
        assertTrue(fieldPattern.matchFieldName("m_field"));
        assertFalse(fieldPattern.matchFieldName("m_"));
    }

    public void testMatchFieldType1() {
        FieldPattern fieldPattern = Pattern.compileFieldPattern("int m_field");

        assertFalse(fieldPattern.matchFieldType(""));
        assertTrue(fieldPattern.matchFieldType("int"));
        assertFalse(fieldPattern.matchFieldType("java.lang.String"));
    }

    public void testMatchFieldType2() {
        FieldPattern fieldPattern = Pattern.compileFieldPattern("* m_field");

        assertFalse(fieldPattern.matchFieldType(""));
        assertTrue(fieldPattern.matchFieldType("int"));
        assertTrue(fieldPattern.matchFieldType("java.lang.String"));
    }

    public void testMatchFieldType3() {
        FieldPattern fieldPattern = Pattern.compileFieldPattern("java.lang.String m_field");

        assertFalse(fieldPattern.matchFieldType(""));
        assertFalse(fieldPattern.matchFieldType("java.lang.StringBuffer"));
        assertTrue(fieldPattern.matchFieldType("java.lang.String"));
    }

    public void testMatchFieldType4() {
        FieldPattern fieldPattern = Pattern.compileFieldPattern("String m_field");

        assertFalse(fieldPattern.matchFieldType(""));
        assertFalse(fieldPattern.matchFieldType("java.lang.StringBuffer"));
        assertTrue(fieldPattern.matchFieldType("java.lang.String"));
    }

    public void testMatchFieldType5() {
        FieldPattern fieldPattern = Pattern.compileFieldPattern("java.lang.* m_field");

        assertFalse(fieldPattern.matchFieldType(""));
        assertFalse(fieldPattern.matchFieldType("java.util.List"));
        assertTrue(fieldPattern.matchFieldType("java.lang.StringBuffer"));
        assertTrue(fieldPattern.matchFieldType("java.lang.String"));
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(FieldPatternTest.class);
    }
}
