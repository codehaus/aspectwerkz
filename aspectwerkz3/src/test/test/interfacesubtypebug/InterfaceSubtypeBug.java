/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.interfacesubtypebug;

import junit.framework.TestCase;

public class InterfaceSubtypeBug extends TestCase {
    public static String LOG = "";

    public InterfaceSubtypeBug() {
    }

    public InterfaceSubtypeBug(String name) {
        super(name);
    }

    public void testInterfaceMethod() {
        LOG = "";
        Target target = new Target();
        target.interfaceMethod();
        assertEquals("interface interface ", LOG);
    }

    public void testNonInterfaceMethod() {
        LOG = "";
        Target target = new Target();
        target.interfaceMethod();
        assertEquals("", LOG);
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(InterfaceSubtypeBug.class);
    }
}
