/**************************************************************************************
 * Copyright (c) Jonas Bon?r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test;

import junit.framework.TestCase;

import java.io.Serializable;

/**
 * Test the Javassist based SerialVerUid computation.
 * See AW-244 for synthetic members bug.
 *
 * FIXME impl with ASM
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class SerialVerUidTest extends TestCase implements Serializable {

    public Object[] someMethod() {
        return null;
    }

    public SerialVerUidTest() {
        super();
    }

    public SerialVerUidTest(Object[] foo) {
        ;
    }

    protected static final int someField = 32;

    public void testSerialVerUid() throws Throwable {
        //FIXME assertFalse("implement test", true);
    }

    public void testSerialVerUidSynthetic() throws Throwable {
        //FIXME assertFalse("implement test", true);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(SerialVerUidTest.class);
    }
}
