/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.xmldef;

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

        suite.addTestSuite(test.ClassPatternTest.class);
        suite.addTestSuite(test.FieldPatternTest.class);
        suite.addTestSuite(test.MethodPatternTest.class);
        suite.addTestSuite(test.JexlTest.class);
        suite.addTestSuite(test.ExceptionTest.class);
        suite.addTestSuite(test.MethodComparatorTest.class);

        suite.addTestSuite(test.xmldef.IntroductionTest.class);
        suite.addTestSuite(test.xmldef.MemberMethodAdviceTest.class);
        suite.addTestSuite(test.xmldef.StaticMethodAdviceTest.class);
        suite.addTestSuite(test.xmldef.FieldAdviceTest.class);
        suite.addTestSuite(test.xmldef.CallerSideAdviceTest.class);
        suite.addTestSuite(test.xmldef.ThrowsAdviceTest.class);
        suite.addTestSuite(test.xmldef.DynamicDeploymentTest.class);
//        suite.addTestSuite(test.xmldef.XmlDefinitionParserTest.class);
        suite.addTestSuite(test.xmldef.AspectWerkzTest.class);
        suite.addTestSuite(test.xmldef.CFlowTest.class);
        suite.addTestSuite(test.xmldef.PointcutExpressionTest.class);
        suite.addTestSuite(test.xmldef.AbstractClassInstrumentationTest.class);
        suite.addTestSuite(test.xmldef.HierachicalPatternTest.class);
        suite.addTestSuite(test.xmldef.reflection.ReflectionTest.class);
        suite.addTestSuite(test.xmldef.bindingsyntax.AdviceBindingTest.class);
        suite.addTestSuite(test.xmldef.superclassconstructorbug.SuperClassConstructorTest.class);
        suite.addTestSuite(test.xmldef.staticfield.StaticFieldAdviceTest.class);
        suite.addTestSuite(test.xmldef.staticfield.CollectionFieldTest.class);
        suite.addTestSuite(test.xmldef.PerformanceTest.class);

        // not used
        //suite.addTestSuite(test.xmldef.reentrant.ReentrantTest.class);
        //suite.addTestSuite(test.xmldef.clapp.CustomClassLoaderTest.class);//broken on JRockit

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
