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

/**
 * Copied from AllTest, kept only relevant test (using the weaver), and commented the test that fail for now.
 * <p/>
 * This tests can be run without online / offline or other post compilation mode. Used to test
 * standalone component of AspectWerkz.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur </a>
 */
public class AllTestsInlined extends TestCase {
    public static Test suite() {
        TestSuite suite = new TestSuite("All tests");

        suite.addTestSuite(MemberMethodAdviceTest.class);
        suite.addTestSuite(StaticMethodAdviceTest.class);
        suite.addTestSuite(CallerSideAdviceTest.class);
        suite.addTestSuite(test.callAndExecution.CallExecutionTest.class);
        suite.addTestSuite(test.hierarchicalpattern.HierachicalPatternTest.class);
        suite.addTestSuite(test.abstractclass.AbstractClassTest.class);
        suite.addTestSuite(test.reflection.ReflectionTest.class);
        suite.addTestSuite(test.pointcutexpression.PointcutExpressionTest.class);
        suite.addTestSuite(test.args.ArgsAdviceTest.class);
        suite.addTestSuite(test.afterxxx.Test.class);
        suite.addTestSuite(FieldAdviceTest.class);
        suite.addTestSuite(ClassPatternTest.class);
        suite.addTestSuite(ExceptionTest.class);
        suite.addTestSuite(MethodComparatorTest.class);
        suite.addTestSuite(StringsTest.class);
        suite.addTestSuite(test.SerialVerUidTest.class);
        suite.addTestSuite(test.expression.ExpressionTest.class);
        suite.addTestSuite(test.rtti.RttiTest.class);
        suite.addTestSuite(test.proceedinnewthread.ProceedTest.class);
        suite.addTestSuite(test.constructor.ConstructorAdviceTest.class);
        suite.addTestSuite(test.bindingsyntax.AdviceBindingTest.class);
        suite.addTestSuite(test.orthogonal.OrthogonalTest.class);
        suite.addTestSuite(test.annotation.AnnotationTest.class);
        suite.addTestSuite(test.modifier.ModifierTest.class);
        suite.addTestSuite(test.superclassconstructor.SuperClassConstructorTest.class);
        suite.addTestSuite(test.staticfield.StaticFieldAdviceTest.class);
        suite.addTestSuite(test.SerialVerUidTest.class);

        // NOT YET COMPLETED
//        suite.addTestSuite(test.handler.HandlerTest.class);
        //suite.addTestSuite(test.aopc.AspectSystemTest.class);//FIXME - test broken since Attr.Extractor removal
//        suite.addTestSuite(test.advisetostringbug.Test.class);//mixin
//        suite.addTestSuite(CFlowTest.class);
//        suite.addTestSuite(ComplexCFlowTest.class);
//        suite.addTestSuite(test.mixindeployment.IntroductionDeploymentTest.class);
//        suite.addTestSuite(IntroductionTest.class);
//        suite.addTestSuite(test.implementsbug.ImplementsTest.class);
//        suite.addTestSuite(test.inheritedmixinbug.Target.class);
//        suite.addTestSuite(test.interfacesubtypebug.InterfaceSubtypeBug.class);
//        suite.addTestSuite(test.adviseonintroducedinterface.Test.class);
//        suite.addTestSuite(test.aspectutilmethodbug.Test.class);//is using perThread
//        suite.addTestSuite(test.performance.PerformanceTest.class);

        // TODO: deprecated until a better hot deployment model has been implemented
        //        suite.addTestSuite(test.DynamicDeploymentTest.class);
        return suite;
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}