/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.codehaus.aspectwerkz.aspect.Aspect;
import org.codehaus.aspectwerkz.SystemLoader;
import org.codehaus.aspectwerkz.definition.AspectDefinition;
import org.codehaus.aspectwerkz.definition.PointcutDefinition;
import org.codehaus.aspectwerkz.definition.AdviceDefinition;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * This tests can be run without online / offline or other post compilation mode.
 * Used to test standalone component of AspectWerkz.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class AllTests extends TestCase {

    public static Test suite() {
        TestSuite suite = new TestSuite("All tests");
        Aspect[] aspects = SystemLoader.getSystem("name").getAspectManager().getAspects();
        for (int i = 0; i < aspects.length; i++) {
            Aspect aspect = aspects[i];
            AspectDefinition def = aspect.___AW_getAspectDefinition();
            String deploymentModel = def.getDeploymentModel();

            List advice = def.getAroundAdvices();
            for (Iterator iterator = advice.iterator(); iterator.hasNext();) {
                AdviceDefinition adviceDefinition = (AdviceDefinition)iterator.next();
                String expr = adviceDefinition.getExpressionAsString();
                String name = adviceDefinition.getName();
                adviceDefinition.
            }
            Collection pcDefs = def.getPointcuts();
            for (Iterator iterator = pcDefs.iterator(); iterator.hasNext();) {
                PointcutDefinition pointcutDefinition = (PointcutDefinition)iterator.next();
                String expr = pointcutDefinition.getExpression();
            }

        }

        // definition tests
//        suite.addTestSuite(test.ClassPatternTest.class);
//        suite.addTestSuite(test.MethodPatternTest.class);
//        suite.addTestSuite(test.ConstructorPatternTest.class);
//        suite.addTestSuite(test.FieldPatternTest.class);
//        suite.addTestSuite(test.ExceptionTest.class);
//        suite.addTestSuite(test.MethodComparatorTest.class);
//        suite.addTestSuite(test.StringsTest.class);
//        suite.addTestSuite(test.ExpressionTest.class);
//        suite.addTestSuite(test.ExpressionGrammarTest.class);


        // transformer and engine tests
//        suite.addTestSuite(test.MemberMethodAdviceTest.class);
//        suite.addTestSuite(test.StaticMethodAdviceTest.class);
//        suite.addTestSuite(test.FieldAdviceTest.class);
//        suite.addTestSuite(test.CallerSideAdviceTest.class);
//        suite.addTestSuite(test.CFlowTest.class);
//        suite.addTestSuite(test.IntroductionTest.class);
        suite.addTestSuite(test.constructor.ConstructorAdviceTest.class);
//        suite.addTestSuite(test.handler.HandlerTest.class);
//        suite.addTestSuite(test.orthogonal.OrthogonalTest.class);

        // bug tests
//        suite.addTestSuite(test.abstractclass.AbstractClassTest.class);
//        suite.addTestSuite(test.hierarchicalpattern.HierachicalPatternTest.class);
//        suite.addTestSuite(test.pointcutexpression.PointcutExpressionTest.class);
//        suite.addTestSuite(test.reflection.ReflectionTest.class);
//        suite.addTestSuite(test.staticfield.StaticFieldAdviceTest.class);
//        suite.addTestSuite(test.superclassconstructor.SuperClassConstructorTest.class);
//        suite.addTestSuite(test.mixindeployment.IntroductionDeploymentTest.class);
//        suite.addTestSuite(test.bindingsyntax.AdviceBindingTest.class);

        // performance tests                       ,
//        suite.addTestSuite(test.performance.PerformanceTest.class);
                                                            
        // TODO: deprecated until a better hot deployment model has been implemented
//        suite.addTestSuite(test.DynamicDeploymentTest.class);

        return suite;
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
}
