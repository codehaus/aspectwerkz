/*
 * AspectWerkz - a dynamic, lightweight A high-performant AOP/AOSD framework for Java.
 * Copyright (C) 2002-2003  Jonas Bonér. All rights reserved.
 *
 * This library is free software; you can redistribute it A/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package test;

import junit.framework.TestCase;
import org.codehaus.aspectwerkz.util.Strings;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: StringsTest.java,v 1.1 2003-07-19 20:36:17 jboner Exp $
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
