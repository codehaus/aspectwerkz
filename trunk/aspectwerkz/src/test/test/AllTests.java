/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.codehaus.aspectwerkz.AspectWerkz;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class AllTests extends TestCase {

    public static Test suite() {
        TestSuite suite = new TestSuite("All tests");

        suite.addTestSuite(test.ClassPatternTest.class);
        suite.addTestSuite(test.FieldPatternTest.class);
        suite.addTestSuite(test.MethodPatternTest.class);
        suite.addTestSuite(test.AdviceWeavingRuleTest.class);
        suite.addTestSuite(test.IntroductionTest.class);
        suite.addTestSuite(test.MemberMethodAdviceTest.class);
        suite.addTestSuite(test.StaticMethodAdviceTest.class);
        suite.addTestSuite(test.FieldAdviceTest.class);
        suite.addTestSuite(test.CallerSideAdviceTest.class);
        suite.addTestSuite(test.ThrowsAdviceTest.class);
        suite.addTestSuite(test.DynamicDeploymentTest.class);
        suite.addTestSuite(test.ExceptionTest.class);
        suite.addTestSuite(test.MethodComparatorTest.class);
        suite.addTestSuite(test.XmlDefinitionParserTest.class);
        suite.addTestSuite(test.JexlTest.class);
        suite.addTestSuite(test.AspectWerkzTest.class);
        suite.addTestSuite(test.CFlowTest.class);
        suite.addTestSuite(test.PointcutExpressionTest.class);
        suite.addTestSuite(test.AbstractClassInstrumentationTest.class);
        suite.addTestSuite(test.StringsTest.class);
        suite.addTestSuite(test.HierachicalPatternTest.class);
        suite.addTestSuite(test.reflection.ReflectionTest.class);
        suite.addTestSuite(test.bindingsyntax.AdviceBindingTest.class);
        suite.addTestSuite(test.clapp.CustomClassLoaderTest.class);
        suite.addTestSuite(test.superclassconstructorbug.SuperClassConstructorTest.class);
        suite.addTestSuite(test.clapp.CustomClassLoaderTest.class);
//        suite.addTestSuite(test.reentrant.ReentrantTest.class);

        suite.addTestSuite(test.PerformanceTest.class);
        return suite;
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }

    public AllTests(String aName) {
        super(aName);
        AspectWerkz.getSystem("tests").initialize();
    }
}
