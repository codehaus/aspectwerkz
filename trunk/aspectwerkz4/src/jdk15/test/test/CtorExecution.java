/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test;

import junit.framework.TestCase;
import org.codehaus.aspectwerkz.annotation.Before;
import org.codehaus.aspectwerkz.annotation.Around;
import org.codehaus.aspectwerkz.joinpoint.StaticJoinPoint;

/**
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class CtorExecution extends TestCase {

    static int s_count = 0;

    public CtorExecution m_ref;

    public CtorExecution(CtorExecution ref) {
        m_ref = ref;
    }

    public CtorExecution() {
        // tricky INVOKESPECIAL indexing
        this(new CtorExecution((CtorExecution)null));
    }

    public CtorExecution(String s) {
        // tricky INVOKESPECIAL indexing
        super((new CtorExecution()).string(s));
    }

    public String string(String s) {
        return s;
    }

    public void testSome() {
        s_count = 0;
        CtorExecution me = new CtorExecution();
        me = new CtorExecution(me);
        me = new CtorExecution("foo");
        assertEquals(16, s_count);
    }

    public static class Aspect {
        @Before("execution(test.CtorExecution.new(..))")
        void before(StaticJoinPoint sjp) {
            s_count++;
            //System.out.println(sjp.getSignature());
        }
        @Around("execution(test.CtorExecution.new(..))")
        Object around(StaticJoinPoint sjp) throws Throwable {
            s_count++;
            //System.out.println(sjp.getSignature());
            return sjp.proceed();
        }

    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(CtorExecution.class);
    }

}
