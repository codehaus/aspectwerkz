/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class AbstractClassInstrumentationTest extends TestCase {

    public void testInstrumentedAbstractMemberMethodInvocation() {
        try {
            AbstractTarget target = new AbstractTargetImpl();
            //@todo uncomment this when fixed
            //assertEquals("method1", target.method1());
        }
        catch (Exception e) {
            fail();
        }
    }

    public void testInstrumentedAbstractStaticMethodInvocation() {
        try {
            AbstractTarget target = new AbstractTargetImpl();
            assertEquals("method2", target.method2());
        }
        catch (Exception e) {
            fail();
        }
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(AbstractClassInstrumentationTest.class);
    }

    public AbstractClassInstrumentationTest(String name) {
        super(name);
    }
}
