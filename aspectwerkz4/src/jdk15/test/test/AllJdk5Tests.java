/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import test.annotation.DefaultValueTest;
import test.deployment.HotDeployedTest;

/**
 * JDK 5 specific tests.
 *
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class AllJdk5Tests extends TestCase {

    public static Test suite() {
        TestSuite suite = new TestSuite("All JDK 5 tests");

        suite.addTestSuite(DefaultValueTest.class);
        suite.addTestSuite(HotDeployedTest.class);
        suite.addTestSuite(CflowBelowTest.class);

        // bug fix tests
        suite.addTestSuite(FieldGetOutOfWeaver.class);
        suite.addTestSuite(InterfaceDefinedMethodTestCase.class);
        suite.addTestSuite(CtorExecution.class);
        suite.addTestSuite(MixinTest.class);
        suite.addTestSuite(CustomProceedChangeTargetTest.class);
        suite.addTestSuite(PerInstanceSerializationTest.class);
        suite.addTestSuite(QNameTest.class);

        return suite;
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

}
