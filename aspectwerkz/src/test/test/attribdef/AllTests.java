/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.attribdef;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.codehaus.aspectwerkz.SystemLoader;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class AllTests extends TestCase {

    public static Test suite() {
        TestSuite suite = new TestSuite("All tests");

        suite.addTestSuite(test.attribdef.MemberMethodAdviceTest.class);
        suite.addTestSuite(test.attribdef.StaticMethodAdviceTest.class);
        suite.addTestSuite(test.attribdef.FieldAdviceTest.class);
        suite.addTestSuite(test.attribdef.CallerSideAdviceTest.class);
//        suite.addTestSuite(test.attribdef.CFlowTest.class);
        suite.addTestSuite(test.attribdef.IntroductionTest.class);
        suite.addTestSuite(test.attribdef.DynamicDeploymentTest.class);
//        suite.addTestSuite(test.attribdef.PerformanceTest.class);

        return suite;
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }

    public AllTests(String aName) {
        super(aName);
        SystemLoader.getSystem("tests").initialize();
    }
}
