/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.xmldef.xmldef;

import junit.framework.TestCase;

import org.codehaus.aspectwerkz.xmldef.AspectWerkz;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class CFlowTest extends TestCase implements Loggable {

    private String m_logString = "";

    public void testCallWithinCFlow() {
        m_logString = "";
        step1();
        assertEquals("step1 advice-before step2 advice-after ", m_logString);
    }

    public void testCallOutsideCFlow() {
        m_logString = "";
        step2();
        assertEquals("step2 ", m_logString);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(CFlowTest.class);
    }

    public CFlowTest(String name) {
        super(name);
        AspectWerkz.getSystem("tests").initialize();
    }

    // ==== methods to test ====

    public void log(final String wasHere) {
        m_logString += wasHere;
    }

    public void step1() {
        log("step1 ");
        step2();
    }

    public void step2() {
        log("step2 ");
    }
}
