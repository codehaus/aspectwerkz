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

import org.codehaus.aspectwerkz.transform.inlining.weaver.AddSerialVersionUidVisitor;
import org.codehaus.aspectwerkz.transform.inlining.weaver.AddSerialVersionUidVisitor;
import org.codehaus.aspectwerkz.reflect.impl.asm.AsmClassInfo;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.util.ContextClassLoader;
import org.codehaus.aspectwerkz.util.ContextClassLoader;

/**
 * Test for the SerialVerionUid computation.
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class SerialVerUidTest extends TestCase implements Serializable {
    static {
        System.gc();
    }

    public Object[] someMethod() {
        return null;
    }

    protected static final int someField = 32;

    public void testSerialVerUid() throws Throwable {
        ClassInfo classInfo = AsmClassInfo.getClassInfo("test.SerialVerUidTest", ContextClassLoader.getLoader());
        long UID = AddSerialVersionUidVisitor.calculateSerialVersionUID(classInfo);
        assertEquals(-6289975506796941698L, UID);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(SerialVerUidTest.class);
    }
}
