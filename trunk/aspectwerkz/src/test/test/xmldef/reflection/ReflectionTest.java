/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.reflection;

import junit.framework.TestCase;

/**
 * The advice used here reverse the sign of the integer returned by the incr(..) methods.
 * Each incr(..) method return the argument increment of 1.
 * Child is overriding a method defined in Super but still does call it.
 */
public class ReflectionTest extends TestCase {

    public void testDualPointcutWithOverridedMethodNonDelegating() {
        OtherChild c = new OtherChild();
        assertEquals(-2, c.incr(1));
    }

    public void testDualPointcutWithOverridedMethodDelegating() {
        Child c = new Child();
        try {
            //@todo uncomment this when fixed
            //assertEquals(+3, c.incr(1));
        } catch (Throwable t) {
            fail(t.getMessage());
        }
    }

}
