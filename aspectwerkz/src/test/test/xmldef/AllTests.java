package test;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.codehaus.aspectwerkz.AspectWerkz;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 * @version $Id: AllTests.java,v 1.3 2003-06-17 15:19:42 jboner Exp $
 */
public class AllTests extends TestCase {

    public static Test suite() {
        TestSuite suite = new TestSuite("All tests");

//        suite.addTestSuite(test.ClassPatternTest.class);
//        suite.addTestSuite(test.FieldPatternTest.class);
//        suite.addTestSuite(test.MethodPatternTest.class);
//        suite.addTestSuite(test.AdviceWeavingRuleTest.class);
//        suite.addTestSuite(test.IntroductionTest.class);
//        suite.addTestSuite(test.MemberMethodAdviceTest.class);
//        suite.addTestSuite(test.StaticMethodAdviceTest.class);
//        suite.addTestSuite(test.FieldAdviceTest.class);
//        suite.addTestSuite(test.CallerSideAdviceTest.class);
//        suite.addTestSuite(test.ThrowsAdviceTest.class);
//        suite.addTestSuite(test.DynamicDeploymentTest.class);
//        suite.addTestSuite(test.ExceptionTest.class);
//        suite.addTestSuite(test.MethodComparatorTest.class);
//        suite.addTestSuite(test.XmlDefinitionParserTest.class);
//        suite.addTestSuite(test.JexlTest.class);
//        suite.addTestSuite(test.AspectWerkzTest.class);

//        suite.addTestSuite(test.AspectWerkzDefinitionTest.class);
//        suite.addTestSuite(test.AspectTest.class);
//        suite.addTestSuite(test.AbstractPointcutTest.class);
//        suite.addTestSuite(test.FieldPointcutTest.class);

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
