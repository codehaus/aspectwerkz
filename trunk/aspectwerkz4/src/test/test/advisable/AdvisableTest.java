/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.advisable;

import junit.framework.TestCase;
import org.codehaus.aspectwerkz.delegation.Advisable;
import org.codehaus.aspectwerkz.delegation.AroundAdviceDelegator;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public class AdvisableTest extends TestCase {
    private static String LOG = "";

    public static void log(String msg) {
        LOG += msg;
    }

    public void testIsAdvisable() {
        assertTrue(this instanceof Advisable);
    }

    public void testAddAround() {
        LOG = "";
        adviseWithAround();
        assertEquals("adviseWithAround ", LOG);

        ((Advisable) this).aw$addAdviceDelegator(
                new AroundAdviceDelegator(new MyAspect(), "around1") {
                    MyAspect m_aspect;

                    public Object delegate(JoinPoint jp) throws Throwable {
                        return m_aspect.around1(jp);
                    }
                }
        );

        LOG = "";
        adviseWithAround();
        assertEquals("around1_pre adviseWithAround around1_post ", LOG);
    }

    public void testAddAroundStack() {
        LOG = "";
        adviseWithAroundStack();
        assertEquals("adviseWithAroundStack ", LOG);

        ((Advisable) this).aw$addAdviceDelegator(
                new AroundAdviceDelegator(new MyAspect(), "around2") {
                    MyAspect m_aspect;

                    public Object delegate(JoinPoint jp) throws Throwable {
                        return m_aspect.around2(jp);
                    }
                }
        );

        LOG = "";
        adviseWithAroundStack();
        assertEquals("around2_pre adviseWithAroundStack around2_post ", LOG);

        ((Advisable) this).aw$addAdviceDelegator(
                new AroundAdviceDelegator(new MyAspect(), "around3") {
                    MyAspect m_aspect;

                    public Object delegate(JoinPoint jp) throws Throwable {
                        return m_aspect.around3(jp);
                    }
                }
        );

        LOG = "";
        adviseWithAroundStack();
        assertEquals("around2_pre around3_pre adviseWithAroundStack around3_pre around2_post ", LOG);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(AdvisableTest.class);
    }

    public void adviseWithAround() {
        log("adviseWithAround ");
    }

    public void adviseWithAroundStack() {
        log("adviseWithAroundStack ");
    }
}
