package test;

import junit.framework.TestCase;

import org.codehaus.aspectwerkz.AspectWerkz;
import org.codehaus.aspectwerkz.Aspect;
import org.codehaus.aspectwerkz.DeploymentModel;
import org.codehaus.aspectwerkz.introduction.Introduction;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.advice.Advice;
import org.codehaus.aspectwerkz.advice.PreAdvice;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: AspectWerkzTest.java,v 1.2 2003-06-09 07:04:13 jboner Exp $
 */
public class AspectWerkzTest extends TestCase {

    public void testSetDeploymentModelForAdvice() {
        assertEquals(DeploymentModel.PER_JVM, AspectWerkz.getSystem("tests").getAdvice("methodAdvice1").getDeploymentModel());
        AspectWerkz.getSystem("tests").getAdvice("methodAdvice1").setDeploymentModel(DeploymentModel.PER_CLASS);
        assertEquals(DeploymentModel.PER_CLASS, AspectWerkz.getSystem("tests").getAdvice("methodAdvice1").getDeploymentModel());
    }

    public void testRegisterAspect() {
        AspectWerkz.getSystem("tests").register(new Aspect(getClass().getName()));
        assertEquals(getClass().getName(), ((Aspect)AspectWerkz.getSystem("tests").getAspects(getClass().getName()).get(0)).getPattern());
    }

    public void testRegisterAdvice() {
        Advice advice = new PreAdvice() {
            public void execute(final JoinPoint joinPoint) {
            }
        };
        AspectWerkz.getSystem("tests").register("testRegisterAdvice", advice);
        assertNotNull(AspectWerkz.getSystem("tests").getAdvice("testRegisterAdvice"));
    }

    public void testRegisterIntroduction() {
        AspectWerkz.getSystem("tests").register("testRegisterIntroduction", new Introduction("testRegisterIntroduction", "java.io.Serializable"));
        assertNotNull(AspectWerkz.getSystem("tests").getIntroduction("testRegisterIntroduction"));
    }

    public void testFindAdviceByIndex() {
        Advice advice = new PreAdvice() {
            public void execute(final JoinPoint joinPoint) {
            }
        };
        AspectWerkz.getSystem("tests").register("testFindAdviceByIndex", advice);
        int index = AspectWerkz.getSystem("tests").getAdviceIndexFor("testFindAdviceByIndex");
        assertEquals(AspectWerkz.getSystem("tests").getAdvice("testFindAdviceByIndex"), AspectWerkz.getSystem("tests").getAdvice(index));
    }

    public void testFindIntroductionByIndex() {
        AspectWerkz.getSystem("tests").register("testFindIntroductionByIndex", new Introduction("testFindIntroductionByIndex", "java.io.Serializable"));
        int index = AspectWerkz.getSystem("tests").getIntroductionIndexFor("testFindIntroductionByIndex");
        assertEquals(9, index);
        assertEquals(AspectWerkz.getSystem("tests").getIntroduction("testFindIntroductionByIndex"), AspectWerkz.getSystem("tests").getIntroduction(index));
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(AspectWerkzTest.class);
    }

    public AspectWerkzTest(String name) {
        super(name);
        AspectWerkz.getSystem("tests").initialize();
    }
}
