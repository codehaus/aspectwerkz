/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.xmldef;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.codehaus.aspectwerkz.xmldef.AspectWerkz;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class AllTests extends TestCase {

    public static Test suite() {
        TestSuite suite = new TestSuite("All tests");

        suite.addTestSuite(test.xmldef.ClassPatternTest.class);
        suite.addTestSuite(test.xmldef.FieldPatternTest.class);
        suite.addTestSuite(test.xmldef.MethodPatternTest.class);
        suite.addTestSuite(test.xmldef.AdviceWeavingRuleTest.class);
        suite.addTestSuite(test.xmldef.IntroductionTest.class);
        suite.addTestSuite(test.xmldef.MemberMethodAdviceTest.class);
        suite.addTestSuite(test.xmldef.StaticMethodAdviceTest.class);
        suite.addTestSuite(test.xmldef.FieldAdviceTest.class);
        suite.addTestSuite(test.xmldef.CallerSideAdviceTest.class);
        suite.addTestSuite(test.xmldef.ThrowsAdviceTest.class);
        suite.addTestSuite(test.xmldef.DynamicDeploymentTest.class);
        suite.addTestSuite(test.xmldef.ExceptionTest.class);
        suite.addTestSuite(test.xmldef.MethodComparatorTest.class);
        suite.addTestSuite(test.xmldef.XmlDefinitionParserTest.class);
        suite.addTestSuite(test.xmldef.JexlTest.class);
        suite.addTestSuite(test.xmldef.AspectWerkzTest.class);
        suite.addTestSuite(test.xmldef.CFlowTest.class);
        suite.addTestSuite(test.xmldef.PointcutExpressionTest.class);
        suite.addTestSuite(test.xmldef.AbstractClassInstrumentationTest.class);
        suite.addTestSuite(test.xmldef.StringsTest.class);
        suite.addTestSuite(test.xmldef.HierachicalPatternTest.class);
        suite.addTestSuite(test.xmldef.reflection.ReflectionTest.class);
        suite.addTestSuite(test.xmldef.bindingsyntax.AdviceBindingTest.class);
        suite.addTestSuite(test.xmldef.clapp.CustomClassLoaderTest.class);
        suite.addTestSuite(test.xmldef.superclassconstructorbug.SuperClassConstructorTest.class);
        suite.addTestSuite(test.xmldef.clapp.CustomClassLoaderTest.class);
//        suite.addTestSuite(test.xmldef.reentrant.ReentrantTest.class);

        suite.addTestSuite(test.xmldef.PerformanceTest.class);
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
