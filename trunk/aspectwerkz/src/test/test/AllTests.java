/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * This tests can be run without online / offline or other post compilation mode.
 * Used to test standalone component of AspectWerkz.
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class AllTests extends TestCase {

    public static Test suite() {
        TestSuite suite = new TestSuite("All tests");

        suite.addTestSuite(test.ClassPatternTest.class);
        suite.addTestSuite(test.MethodPatternTest.class);
        suite.addTestSuite(test.ExceptionTest.class);
        suite.addTestSuite(test.MethodComparatorTest.class);
        suite.addTestSuite(test.JexlTest.class);
        suite.addTestSuite(test.StringsTest.class);

        return suite;
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }

}
