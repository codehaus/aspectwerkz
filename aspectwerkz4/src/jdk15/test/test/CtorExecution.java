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
import org.codehaus.aspectwerkz.annotation.Around;
import org.codehaus.aspectwerkz.joinpoint.StaticJoinPoint;
import org.codehaus.aspectwerkz.transform.inlining.weaver.AddSerialVersionUidVisitor;
import org.codehaus.aspectwerkz.reflect.impl.java.JavaClassInfo;

import java.io.Serializable;
import java.lang.reflect.Field;

/**
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class CtorExecution extends TestCase implements Serializable {

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
        // and tricky new CtorExecution() call before instance initialization
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
        assertEquals(22, s_count);//2x11=22 and not 2x16 since ctor call before object initialization are skept
    }

    public void testSerialVer() throws Throwable {
        Class x = CtorExecution.class;
        long l = AddSerialVersionUidVisitor.calculateSerialVersionUID(JavaClassInfo.getClassInfo(x));
        // uncomment me and turn off weaver to compute the expected serialVerUID
        //System.out.println(l);

        Field f = x.getDeclaredField("serialVersionUID");
        long uid = ((Long)f.get(null)).longValue();
        //System.out.println(uid);
        assertEquals(-4944916826301933718L, uid);
    }

    public static class Aspect {
        @Before("execution(test.CtorExecution.new(..))" +
                " || (call(test.CtorExecution.new(..)) && within(test.CtorExecution))")
        void before(StaticJoinPoint sjp) {
            s_count++;
            //System.out.println(sjp.getSignature());
        }

        @Around("execution(test.CtorExecution.new(..))" +
                " || (call(test.CtorExecution.new(..)) && within(test.CtorExecution))")
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
