package test;

import junit.framework.TestCase;

import org.codehaus.aspectwerkz.AspectWerkz;
import org.codehaus.aspectwerkz.Aspect;
import org.codehaus.aspectwerkz.DeploymentModel;
import org.codehaus.aspectwerkz.introduction.Introduction;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.advice.Advice;
import org.codehaus.aspectwerkz.advice.PreAdvice;

public class AspectWerkzTest extends TestCase {

    public void testSetDeploymentModelForAdvice() {
        assertEquals(DeploymentModel.PER_JVM, AspectWerkz.getAdvice("methodAdvice1").getDeploymentModel());
        AspectWerkz.getAdvice("methodAdvice1").setDeploymentModel(DeploymentModel.PER_CLASS);
        assertEquals(DeploymentModel.PER_CLASS, AspectWerkz.getAdvice("methodAdvice1").getDeploymentModel());
    }

    public void testRegisterAspect() {
        AspectWerkz.register(new Aspect(getClass().getName()));
        assertEquals(getClass().getName(), ((Aspect)AspectWerkz.getAspects(getClass().getName()).get(0)).getPattern());
    }

    public void testRegisterAdvice() {
        Advice advice = new PreAdvice() {
            public void execute(final JoinPoint joinPoint) {
            }
        };
        AspectWerkz.register("testRegisterAdvice", advice);
        assertNotNull(AspectWerkz.getAdvice("testRegisterAdvice"));
    }

    public void testRegisterIntroduction() {
        AspectWerkz.register("testRegisterIntroduction", new Introduction("testRegisterIntroduction", "java.io.Serializable"));
        assertNotNull(AspectWerkz.getIntroduction("testRegisterIntroduction"));
    }

    public void testFindAdviceByIndex() {
        Advice advice = new PreAdvice() {
            public void execute(final JoinPoint joinPoint) {
            }
        };
        AspectWerkz.register("testFindAdviceByIndex", advice);
        int index = AspectWerkz.getAdviceIndexFor("testFindAdviceByIndex");
        assertEquals(AspectWerkz.getAdvice("testFindAdviceByIndex"), AspectWerkz.getAdvice(index));
    }

    public void testFindIntroductionByIndex() {
        AspectWerkz.register("testFindIntroductionByIndex", new Introduction("testFindIntroductionByIndex", "java.io.Serializable"));
        int index = AspectWerkz.getIntroductionIndexFor("testFindIntroductionByIndex");
        assertEquals(9, index);
        assertEquals(AspectWerkz.getIntroduction("testFindIntroductionByIndex"), AspectWerkz.getIntroduction(index));
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(AspectWerkzTest.class);
    }

    public AspectWerkzTest(String name) {
        super(name);
        AspectWerkz.initialize();
    }
}
