/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test;

import junit.framework.TestCase;
import org.codehaus.aspectwerkz.AspectContext;
import org.codehaus.aspectwerkz.aspect.management.Aspects;
import org.codehaus.aspectwerkz.annotation.Before;

/**
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class QNameTest extends TestCase {

    static StringBuffer s_log = new StringBuffer();
    static void log(String s) {
        s_log.append(s).append(" ");
    }

    void doStuff() {
        log("doStuff");
    }

    public void testQNames() {
        doStuff();
        // note: aspect instantiation happens first due to perJVM and JP clinit
        assertEquals("1 jdk5test/Aspect_1 2 jdk5test/Aspect_2 before-1 before-2 doStuff ", s_log.toString());

        Aspect a = (Aspect)Aspects.aspectOf("jdk5test/Aspect_1");
        assertEquals("1", a.p);

        Aspect b = (Aspect)Aspects.aspectOf("jdk5test/Aspect_2");
        assertEquals("2", b.p);

        // in that case there is several aspects for Aspect.class
        // so fails
        try {
            Aspect c = (Aspect)Aspects.aspectOf(Aspect.class);
            fail("should fail");
        } catch (Error e) {
            ;
        }
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(QNameTest.class);
    }

    public static class Aspect {

        String p;

        public Aspect(AspectContext ctx) {
            p = ctx.getParameter("p");
            log(p);
            log(ctx.getAspectDefinition().getQualifiedName());
        }

        @Before("execution(* test.QNameTest.doStuff())")
        public void before() {
            log("before-"+p);
        }

    }

}
