/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test;

import junit.framework.TestCase;

import org.codehaus.aspectwerkz.regexp.FieldPattern;
import org.codehaus.aspectwerkz.regexp.Pattern;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class FieldPatternTest extends TestCase
{
    public void testMatchFieldName1()
    {
        FieldPattern fieldPattern = Pattern.compileFieldPattern("int m_field");

        assertFalse(fieldPattern.matchFieldName(""));
        assertTrue(fieldPattern.matchFieldName("m_field"));
        assertFalse(fieldPattern.matchFieldName("m_"));
    }

    public void testMatchFieldName2()
    {
        FieldPattern fieldPattern = Pattern.compileFieldPattern("int m_*");

        assertFalse(fieldPattern.matchFieldName(""));
        assertTrue(fieldPattern.matchFieldName("m_field"));
        assertTrue(fieldPattern.matchFieldName("m_"));
    }

    public void testMatchFieldName3()
    {
        FieldPattern fieldPattern = Pattern.compileFieldPattern("int *");

        assertFalse(fieldPattern.matchFieldName(""));
        assertTrue(fieldPattern.matchFieldName("m_field"));
        assertTrue(fieldPattern.matchFieldName("m_"));
    }

    public void testMatchFieldName4()
    {
        FieldPattern fieldPattern = Pattern.compileFieldPattern("int *ield");

        assertFalse(fieldPattern.matchFieldName(""));
        assertTrue(fieldPattern.matchFieldName("m_field"));
        assertFalse(fieldPattern.matchFieldName("m_"));
    }

    public void testMatchFieldName5()
    {
        FieldPattern fieldPattern = Pattern.compileFieldPattern("int m_*ld");

        assertFalse(fieldPattern.matchFieldName(""));
        assertTrue(fieldPattern.matchFieldName("m_field"));
        assertTrue(fieldPattern.matchFieldName("m_ld"));
        assertFalse(fieldPattern.matchFieldName("m_"));
    }

    public void testMatchFieldName6()
    {
        FieldPattern fieldPattern = Pattern.compileFieldPattern("int MyMethod");

        assertFalse(fieldPattern.matchFieldName(""));
        assertTrue(fieldPattern.matchFieldName("MyMethod"));
        assertFalse(fieldPattern.matchFieldName("mymethod"));
    }

    public void testMatchFieldType1()
    {
        FieldPattern fieldPattern = Pattern.compileFieldPattern("int m_field");

        assertFalse(fieldPattern.matchFieldType(""));
        assertTrue(fieldPattern.matchFieldType("int"));
        assertFalse(fieldPattern.matchFieldType("java.lang.String"));
    }

    public void testMatchFieldType2()
    {
        FieldPattern fieldPattern = Pattern.compileFieldPattern("* m_field");

        assertFalse(fieldPattern.matchFieldType(""));
        assertTrue(fieldPattern.matchFieldType("int"));
        assertTrue(fieldPattern.matchFieldType("java.lang.String"));
    }

    public void testMatchFieldType3()
    {
        FieldPattern fieldPattern = Pattern.compileFieldPattern(
                "java.lang.String m_field");

        assertFalse(fieldPattern.matchFieldType(""));
        assertFalse(fieldPattern.matchFieldType("java.lang.StringBuffer"));
        assertTrue(fieldPattern.matchFieldType("java.lang.String"));
    }

    public void testMatchFieldType4()
    {
        FieldPattern fieldPattern = Pattern.compileFieldPattern(
                "String m_field");

        assertFalse(fieldPattern.matchFieldType(""));
        assertFalse(fieldPattern.matchFieldType("java.lang.StringBuffer"));
        assertTrue(fieldPattern.matchFieldType("java.lang.String"));
    }

    public void testMatchFieldType5()
    {
        FieldPattern fieldPattern = Pattern.compileFieldPattern(
                "java.lang.* m_field");

        assertFalse(fieldPattern.matchFieldType(""));
        assertFalse(fieldPattern.matchFieldType("java.util.List"));
        assertTrue(fieldPattern.matchFieldType("java.lang.StringBuffer"));
        assertTrue(fieldPattern.matchFieldType("java.lang.String"));
    }

    public void testMatchFieldType6()
    {
        FieldPattern fieldPattern = Pattern.compileFieldPattern(
                "java.lang.String[] m_field");

        assertFalse(fieldPattern.matchFieldType(""));
        assertFalse(fieldPattern.matchFieldType("java.util.List[]"));
        assertFalse(fieldPattern.matchFieldType("java.lang.StringBuffer[]"));
        assertTrue(fieldPattern.matchFieldType("java.lang.String[]"));
    }

    public void testMatchFieldType7()
    {
        FieldPattern fieldPattern = Pattern.compileFieldPattern(
                "java.lang.String m_field");

        assertFalse(fieldPattern.matchFieldType(""));
        assertTrue(fieldPattern.matchFieldType("java.lang.String"));
        assertFalse(fieldPattern.matchFieldType("java.lang.String[]"));
        assertFalse(fieldPattern.matchFieldType("java.lang.String[][]"));
    }

    public void testMatchFieldType8()
    {
        FieldPattern fieldPattern = Pattern.compileFieldPattern(
                "java.lang.String[][] m_field");

        assertFalse(fieldPattern.matchFieldType(""));
        assertFalse(fieldPattern.matchFieldType("java.lang.String[]"));
        assertTrue(fieldPattern.matchFieldType("java.lang.String[][]"));
        assertFalse(fieldPattern.matchFieldType("java.lang.String[][][]"));
    }

    public void testMatchFieldType9()
    {
        FieldPattern fieldPattern = Pattern.compileFieldPattern(
                "String* m_field");

        assertFalse(fieldPattern.matchFieldType(""));

        // note: abbreviation not compatible with pattern matching
        assertFalse(fieldPattern.matchFieldType("java.lang.StringBuffer"));
        assertFalse(fieldPattern.matchFieldType("java.lang.String"));
        assertTrue(fieldPattern.matchFieldType("String"));
        assertTrue(fieldPattern.matchFieldType("StringBuffer"));
    }

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite()
    {
        return new junit.framework.TestSuite(FieldPatternTest.class);
    }
}
