/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.mixindeployment;

import junit.framework.TestCase;
import org.codehaus.aspectwerkz.WeavedTestCase;

/**
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class IntroductionDeploymentTest extends WeavedTestCase {

    public class TargetA {
    };

    public class TargetB {
    };

    public class TargetC {
    };

    public void testPerInstanceMixin() {
        TargetA a1 = new TargetA();
        TargetA a2 = new TargetA();
        TargetB b = new TargetB();

        Marker m1 = (Marker)a1;
        Object o1 = m1.getTargetInstance();

        assertEquals(a1, ((Marker)a1).getTargetInstance());

        assertNotSame(((Marker)a1).getTargetInstance(), ((Marker)a2).getTargetInstance());
        assertEquals(((Marker)a1).getTargetClass(), ((Marker)a2).getTargetClass());

        assertEquals(b, ((Marker)b).getTargetInstance());
        assertEquals(b.getClass(), ((Marker)b).getTargetClass());
    }

    public void testPerClassMixin() {
        TargetC c1 = new TargetC();
        TargetC c2 = new TargetC();

        assertNull(((Marker)c1).getTargetInstance());
        assertEquals(((Marker)c1).getTargetClass(), ((Marker)c2).getTargetClass());
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(IntroductionDeploymentTest.class);
    }

}
