package test;

import java.util.List;

import junit.framework.TestCase;
import org.codehaus.aspectwerkz.AspectWerkz;
import org.codehaus.aspectwerkz.Aspect;
import org.codehaus.aspectwerkz.DeploymentModel;
import org.codehaus.aspectwerkz.advice.AdviceIndexTuple;

public class DynamicDeploymentTest extends TestCase implements Loggable {

    private String m_logString = "";

    public void testCreateTransientAdvice() {
        try {
            // create the new advice
            AspectWerkz.createAdvice("createTransientAdviceTest", "test.DynamicallyCreatedTransientAdvice", "perInstance", null);

            // test the easy stuff
            assertNotNull(AspectWerkz.getAdvice("createTransientAdviceTest"));
            assertEquals(DeploymentModel.getDeploymentModelAsInt("perInstance"), AspectWerkz.getAdvice("createTransientAdviceTest").getDeploymentModel());
            assertEquals("createTransientAdviceTest", AspectWerkz.getAdvice("createTransientAdviceTest").getName());

            // test it in action
            ((Aspect)AspectWerkz.getAspects("test.DynamicDeploymentTest").get(0)).
                    getMethodPointcut("* createTransientAdviceTestMethod(..)").
                    addAdvice("createTransientAdviceTest");

            m_logString = "";
            createTransientAdviceTestMethod();
            assertEquals("before invocation after ", m_logString);
        }
        catch (Exception e) {
            fail();
        }
    }
/*
    public void testCreatePersistentAdvice() {
        try {
            // create the new advice
            AspectWerkz.createAdvice("createPersistentAdviceTest", "test.DynamicallyCreatedPersistentAdvice", "perInstance", true, null);

            // test the easy stuff
            assertNotNull(AspectWerkz.getAdvice("createPersistentAdviceTest"));
            assertEquals(DeploymentModel.getDeploymentModelAsInt("perInstance"), AspectWerkz.getAdvice("createPersistentAdviceTest").getDeploymentModel());
            assertEquals("createPersistentAdviceTest", AspectWerkz.getAdvice("createPersistentAdviceTest").getName());

            // test it in action
            ((Aspect)AspectWerkz.getAspects("test.DynamicDeploymentTest").get(0)).
                    getMethodPointcut("* createPersistentAdviceTestMethod(..)").
                    addAdvice("createPersistentAdviceTest");

            m_logString = "";
            createPersistentAdviceTestMethod();
            System.out.println("m_logString = " + m_logString);
        }
        catch (Exception e) {
            System.out.println("e = " + e);
            fail();
        }
    }
*/
    public void testRemoveAdviceDuringRuntime() {
        m_logString = "";
        removeAdviceTestMethod();
        assertEquals("before1 before2 before2 invocation after2 after2 after1 ", m_logString);
        ((Aspect)AspectWerkz.getAspects("test.DynamicDeploymentTest").get(0)).
                getMethodPointcut("* removeAdviceTestMethod(..)").removeAdvice("methodAdvice2");

        m_logString = "";
        removeAdviceTestMethod();
        assertEquals("before2 before2 invocation after2 after2 ", m_logString);
    }

    public void testAddAdviceDuringRuntime() {
        m_logString = "";
        addAdviceTestMethod();
        assertEquals("before1 invocation after1 ", m_logString);

        ((Aspect)AspectWerkz.getAspects("test.DynamicDeploymentTest").get(0)).
                getMethodPointcut("* addAdviceTestMethod(..)").addAdvice("methodAdvice3");

        m_logString = "";
        addAdviceTestMethod();
        assertEquals("before1 before2 invocation after2 after1 ", m_logString);
    }

    public void testReorderAdvicesDuringRuntime() {
        m_logString = "";
        reorderAdvicesTestMethod();
        assertEquals("before1 before2 before2 invocation after2 after2 after1 ", m_logString);

        List advices = ((Aspect)AspectWerkz.getAspects("test.DynamicDeploymentTest").get(0)).
                getMethodPointcut("* reorderAdvicesTestMethod(..)").getAdviceIndexTuples();
        AdviceIndexTuple tuple1 = (AdviceIndexTuple)advices.get(0);
        AdviceIndexTuple tuple2 = (AdviceIndexTuple)advices.get(1);
        advices.set(0, tuple2);
        advices.set(1, tuple1);
        ((Aspect)AspectWerkz.getAspects("test.DynamicDeploymentTest").get(0)).
                getMethodPointcut("* reorderAdvicesTestMethod(..)").setAdviceIndexTuples(advices);

        m_logString = "";
        reorderAdvicesTestMethod();
        assertEquals("before2 before1 before2 invocation after2 after1 after2 ", m_logString);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(DynamicDeploymentTest.class);
    }

    public DynamicDeploymentTest(String name) {
        super(name);
        AspectWerkz.initialize();
    }

    public void log(final String wasHere) {
        m_logString += wasHere;
    }

    public void reorderAdvicesTestMethod() {
        log("invocation ");
    }

    public void removeAdviceTestMethod() {
        log("invocation ");
    }

    public void addAdviceTestMethod() {
        log("invocation ");
    }

    public void createTransientAdviceTestMethod() {
        log("invocation ");
    }

    public void createPersistentAdviceTestMethod() {
    }
}
