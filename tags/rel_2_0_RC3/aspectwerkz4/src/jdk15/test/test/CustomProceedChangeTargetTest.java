/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test;

import junit.framework.TestCase;
import org.codehaus.aspectwerkz.annotation.Around;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

/**
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class CustomProceedChangeTargetTest extends TestCase {

    static int s_instance = 0;
    int m_me;

    public CustomProceedChangeTargetTest() {
        m_me = ++s_instance;
    }

    public void testPassOtherTarget() {
        s_instance = 0;
        CustomProceedChangeTargetTest one = new CustomProceedChangeTargetTest();//1

        // as an around
        int meOfOne = one.getMe(1);//advised, new instance[2] + 1 -> 3
        assertFalse(meOfOne==one.m_me);
        assertTrue(meOfOne==3);

        String meOfOneAsString = one.getMeAsString(1);//advised, new instance[3] + 1 -> 4
        assertFalse(meOfOneAsString.equals(""+(one.m_me+1)));
        assertTrue("4".equals(meOfOneAsString));
    }

    public int getMe(int i) {
        return m_me + i;
    }

    public String getMeAsString(int i) {
        return "" + (m_me + i);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(CustomProceedChangeTargetTest.class);
    }

    public static class Aspect {

        public static interface CustomJp extends JoinPoint {
            int proceed(CustomProceedChangeTargetTest callee, int arg);
        }

        @Around("execution(int test.CustomProceedChangeTargetTest.getMe(int)) && args(arg) && target(t)")
        public Object around(CustomJp jp, CustomProceedChangeTargetTest t, int arg) throws Throwable {
            int meOfOther = jp.proceed(new CustomProceedChangeTargetTest(), arg);
            return new Integer(meOfOther);
        }

        public static interface CustomJp2 extends JoinPoint {
            String proceed(CustomProceedChangeTargetTest callee, int arg);
        }

        @Around("execution(String test.CustomProceedChangeTargetTest.getMeAsString(int)) && args(arg) && target(t)")
        public Object around(CustomJp2 jp, CustomProceedChangeTargetTest t, int arg) throws Throwable {
            String meOfOther = jp.proceed(new CustomProceedChangeTargetTest(), arg);
            return meOfOther;
        }
    }
}
