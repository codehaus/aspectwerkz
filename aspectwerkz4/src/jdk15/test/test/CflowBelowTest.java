/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test;

import junit.framework.TestCase;
import org.codehaus.aspectwerkz.annotation.Before;
import org.codehaus.aspectwerkz.annotation.Expression;

/**
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class CflowBelowTest extends TestCase {

    static int s_inAspectCount = 0;

    public void testCflow() {
        s_inAspectCount = 0;
        recursiveCflow(3);
        assertEquals(3, s_inAspectCount);
    }

    public void testCflowBelow() {
        s_inAspectCount = 0;
        recursiveCflowBelow(3);
        assertEquals(2, s_inAspectCount);// one less
    }

    /**
     * Recursive i times.
     *
     * @param i
     */
    void recursiveCflow(int i) {
        if (i <= 1) {
            return;
        } else {
            recursiveCflow(i-1);
        }
    }

    /**
     * Recursive i times.
     *
     * @param i
     */
    void recursiveCflowBelow(int i) {
        if (i <= 1) {
            return;
        } else {
            recursiveCflowBelow(i-1);
        }
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(CflowBelowTest.class);
    }



    /**
     * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
     */
    public static class Aspect {

        @Expression("execution(* test.CflowBelowTest.recursiveCflow(..))")
        void pcCflow(){};

        @Expression("execution(* test.CflowBelowTest.recursiveCflowBelow(..))")
        void pcCflowBelow(){};

        @Before("pcCflow() && cflow(pcCflow())")
        public void beforeCflow() {
            s_inAspectCount++;
        }

        @Before("pcCflowBelow() && cflowbelow(pcCflowBelow())")
        public void beforeCflowBelow() {
            s_inAspectCount++;
        }
    }
}
