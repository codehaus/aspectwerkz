/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test;

import junit.framework.TestCase;
import org.codehaus.aspectwerkz.util.Strings;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public class StringsTest extends TestCase {

    public void test1() throws Exception {
        assertEquals("__BCDE", Strings.replaceSubString("ABCDE", "A", "__"));
    }

    public void test2() throws Exception {
        assertEquals("A__CDE", Strings.replaceSubString("A__CDE", "B", "__"));
    }

    public void test3() throws Exception {
        assertEquals("A..*B..*C..*D", Strings.replaceSubString("A..B..C..D", "..", "..*"));
    }

    public void test4() throws Exception {
        assertEquals("A.*B.*C.*D", Strings.replaceSubString("A.B.C.D", ".", ".*"));
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(StringsTest.class);
    }

    public StringsTest(String name) {
        super(name);
    }

}
