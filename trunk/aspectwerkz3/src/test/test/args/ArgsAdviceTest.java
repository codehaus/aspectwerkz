/**************************************************************************************
 * Copyright (c) Jonas Bon?r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.args;

import test.Loggable;
import junit.framework.TestCase;

/**
 * Test for args() syntax and pointcut / advice with signatures.
 * Some tests to cover XML syntax.
 * TODO: test for CALL pc and ctor exe/call jp
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class ArgsAdviceTest extends TestCase implements Loggable {

    private String m_logString = "";

    //args(String, String, long)
    public void testMatchAll() {
        m_logString = "";
        matchAll("a0", "a1", 2);
        assertEquals("before before1 invocation after1 after ", m_logString);
        m_logString = "";
        matchAllXML("a0", "a1", 2);
        assertEquals("before before1 invocation after1 after ", m_logString);
    }

    //args(..)
    public void testMatchAllWithWildcard() {
        m_logString = "";
        matchAllWithWildcard("a0", "a1", 2);
        assertEquals("before before1 invocation after1 after ", m_logString);
    }

    //args(s, ..)
    public void testGetFirst() {
        m_logString = "";
        getFirst("a0", "a1", 2);
        assertEquals("before a0 before1 a0 invocation after1 a0 after a0 ", m_logString);
        m_logString = "";
        getFirstXML("a0", "a1", 2);
        assertEquals("before a0 before1 a0 invocation after1 a0 after a0 ", m_logString);

    }

    //args(s, ..) as anonymous pointcut
    public void testGetFirstAnonymous() {
        m_logString = "";
        getFirstAnonymous("a0", "a1", 2);
        assertEquals("before a0 before1 a0 invocation after1 a0 after a0 ", m_logString);
        //TODO (low prio): anonymous pc with args() is not supported in XML - see notes in test-attribdef.xml
//        m_logString = "";
//        getFirstAnonymousXML("a0", "a1", 2);
//        assertEquals("before a0 before1 a0 invocation after1 a0 after a0 ", m_logString);
    }

    //args(String, s, long) and increment it
    public void testChangeArg() {
        m_logString = "";
        changeArg("a0", new StringBuffer("a1"), 2);
        // beware: using String won't work as for regular Java behavior
        assertEquals("before a1x before1 a1xx invocation after1 a1xxx after a1xxxx ", m_logString);
    }

    // args(s0, s1, long), with Pc signature reversed
    public void testOrderChangedInPointcutSignature() {
        m_logString = "";
        orderChangedInPointcutSignature("a0", "a1", 2);
        assertEquals("before a1 a0 before1 a1 a0 invocation after1 a1 a0 after a1 a0 ", m_logString);
    }

    // args(s0, s1, long), with Advice signature reversed
    public void testOrderChangedInAdviceSignature() {
        m_logString = "";
        orderChangedInAdviceSignature("a0", "a1", 2);
        assertEquals("before a1 a0 before1 a1 a0 invocation after1 a1 a0 after a1 a0 ", m_logString);
    }

    // args(s0, s1, long), with Pointcut and Advice signature reversed
    public void testOrderChangedInPointcutAndAdviceSignature() {
        m_logString = "";
        orderChangedInPointcutAndAdviceSignature("a0", "a1", 2);
        assertEquals("before a0 a1 before1 a0 a1 invocation after1 a0 a1 after a0 a1 ", m_logString);
        m_logString = "";
        orderChangedInPointcutAndAdviceSignatureXML("a0", "a1", null);
        assertEquals("before a0 a1 before1 a0 a1 invocation after1 a0 a1 after a0 a1 ", m_logString);
    }


    //-- Implementation methods
    public void log(String s) {
        m_logString += s;
    }
    public void matchAll(String a0, String a1, long a2) {
        log("invocation ");
    }
    public void matchAllXML(String a0, String a1, long a2) {
        log("invocation ");
    }
    public void matchAllWithWildcard(String a0, String a1, long a2) {
        log("invocation ");
    }
    public void getFirst(String a0, String a1, long a2) {
        log("invocation ");
    }
    public void getFirstXML(String a0, String a1, long a2) {
        log("invocation ");
    }
    public void getFirstAnonymous(String a0, String a1, long a2) {
        log("invocation ");
    }
    public void getFirstAnonymousXML(String a0, String a1, long a2) {
        log("invocation ");
    }
    public void changeArg(String a0, StringBuffer a1, long a2) {
        log("invocation ");
    }
    public void orderChangedInPointcutSignature(String a0, String a1, long a2) {
        log("invocation ");
    }
    public void orderChangedInAdviceSignature(String a0, String a1, long a2) {
        log("invocation ");
    }
    public void orderChangedInPointcutAndAdviceSignature(String a0, String a1, long a2) {
        log("invocation ");
    }
    public void orderChangedInPointcutAndAdviceSignatureXML(String a0, String a1, Object[] a2) {
        log("invocation ");
    }





    //-- JUnit
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(ArgsAdviceTest.class);
    }

}
