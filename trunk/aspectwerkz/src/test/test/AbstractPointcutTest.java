package test;

import java.util.List;
import java.util.ArrayList;

import junit.framework.TestCase;

import org.codehaus.aspectwerkz.AspectWerkz;
import org.codehaus.aspectwerkz.Aspect;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.advice.PreAdvice;
import org.codehaus.aspectwerkz.advice.AdviceIndexTuple;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: AbstractPointcutTest.java,v 1.2 2003-06-09 07:04:13 jboner Exp $
 */
public class AbstractPointcutTest extends TestCase {

    public void testGetPointcutName() {
        ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.AbstractPointcutTest").get(0)).createMethodPointcut("* testGetPointcutName()");
        assertEquals("* testGetPointcutName()", ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.AbstractPointcutTest").get(0)).getMethodPointcut("* testGetPointcutName()").getName());
        assertEquals("* testGetPointcutName()", ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.AbstractPointcutTest").get(0)).getMethodPointcut("* testGetPointcutName()").getName());
    }

    public void testGetAdviceNames() {
        ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.AbstractPointcutTest").get(0)).createMethodPointcut("* testGetAdviceNames()").addAdvice("methodAdvice1");
        ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.AbstractPointcutTest").get(0)).createMethodPointcut("* testGetAdviceNames()").addAdvice("methodAdvice2");
        ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.AbstractPointcutTest").get(0)).createMethodPointcut("* testGetAdviceNames()").addAdvice("methodAdvice3");
        String[] names = ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.AbstractPointcutTest").get(0)).getMethodPointcut("* testGetAdviceNames()").getAdviceNames();
        assertEquals(3, names.length);
        assertEquals("methodAdvice1", names[0]);
        assertEquals("methodAdvice2", names[1]);
        assertEquals("methodAdvice3", names[2]);
    }

    public void testGetAdviceIndexes() {
        ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.AbstractPointcutTest").get(0)).createMethodPointcut("* testGetAdviceIndexes()").addAdvice("methodAdvice1");
        ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.AbstractPointcutTest").get(0)).createMethodPointcut("* testGetAdviceIndexes()").addAdvice("methodAdvice2");
        ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.AbstractPointcutTest").get(0)).createMethodPointcut("* testGetAdviceIndexes()").addAdvice("methodAdvice3");
        int[] indexes = ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.AbstractPointcutTest").get(0)).getMethodPointcut("* testGetAdviceIndexes()").getAdviceIndexes();
        assertEquals(3, indexes.length);
        assertEquals(2, indexes[0]);
        assertEquals(3, indexes[1]);
        assertEquals(4, indexes[2]);
    }

    public void testAddSingleAdvices() {
        ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.AbstractPointcutTest").get(0)).createMethodPointcut("* testAddSingleAdvices()").addAdvice("methodAdvice1");
        ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.AbstractPointcutTest").get(0)).createMethodPointcut("* testAddSingleAdvices()").addAdvice("methodAdvice2");
        ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.AbstractPointcutTest").get(0)).createMethodPointcut("* testAddSingleAdvices()").addAdvice("methodAdvice3");

        String[] names1 = ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.AbstractPointcutTest").get(0)).getMethodPointcut("* testAddSingleAdvices()").getAdviceNames();
        assertEquals(3, names1.length);
        assertEquals("methodAdvice1", names1[0]);
        assertEquals("methodAdvice2", names1[1]);
        assertEquals("methodAdvice3", names1[2]);
        int[] indexes1 = ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.AbstractPointcutTest").get(0)).getMethodPointcut("* testAddSingleAdvices()").getAdviceIndexes();
        assertEquals(3, indexes1.length);
        assertEquals(2, indexes1[0]);
        assertEquals(3, indexes1[1]);
        assertEquals(4, indexes1[2]);
    }

    public void testAddMultipleAdvices() {
        ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.AbstractPointcutTest").get(0)).createMethodPointcut("* testAddMultipleAdvices()").addAdvices(new String[]{"methodAdvice1", "methodAdvice2", "methodAdvice3"});
        String[] names1 = ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.AbstractPointcutTest").get(0)).getMethodPointcut("* testAddMultipleAdvices()").getAdviceNames();
        assertEquals(3, names1.length);
        assertEquals("methodAdvice1", names1[0]);
        assertEquals("methodAdvice2", names1[1]);
        assertEquals("methodAdvice3", names1[2]);
        int[] indexes1 = ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.AbstractPointcutTest").get(0)).getMethodPointcut("* testAddMultipleAdvices()").getAdviceIndexes();
        assertEquals(3, indexes1.length);
        assertEquals(2, indexes1[0]);
        assertEquals(3, indexes1[1]);
        assertEquals(4, indexes1[2]);
    }

    public void testRemoveEndAdvice() {
        ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.AbstractPointcutTest").get(0)).createMethodPointcut("* testRemoveEndAdvice()").addAdvices(new String[]{"methodAdvice1", "methodAdvice2", "methodAdvice3"});
        ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.AbstractPointcutTest").get(0)).getMethodPointcut("* testRemoveEndAdvice()").removeAdvice("methodAdvice3");
        String[] names1 = ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.AbstractPointcutTest").get(0)).getMethodPointcut("* testRemoveEndAdvice()").getAdviceNames();
        int[] indexes1 = ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.AbstractPointcutTest").get(0)).getMethodPointcut("* testRemoveEndAdvice()").getAdviceIndexes();
        assertEquals(2, names1.length);
        assertEquals("methodAdvice1", names1[0]);
        assertEquals("methodAdvice2", names1[1]);
        assertEquals(2, indexes1.length);
        assertEquals(2, indexes1[0]);
        assertEquals(3, indexes1[1]);
    }

    public void testRemoveStartAdvice() {
        ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.AbstractPointcutTest").get(0)).createMethodPointcut("* testRemoveStartAdvice()").addAdvices(new String[]{"methodAdvice1", "methodAdvice2", "methodAdvice3"});
        ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.AbstractPointcutTest").get(0)).getMethodPointcut("* testRemoveStartAdvice()").removeAdvice("methodAdvice1");
        String[] names1 = ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.AbstractPointcutTest").get(0)).getMethodPointcut("* testRemoveStartAdvice()").getAdviceNames();
        int[] indexes1 = ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.AbstractPointcutTest").get(0)).getMethodPointcut("* testRemoveStartAdvice()").getAdviceIndexes();
        assertEquals(2, names1.length);
        assertEquals("methodAdvice2", names1[0]);
        assertEquals("methodAdvice3", names1[1]);
        assertEquals(2, indexes1.length);
        assertEquals(3, indexes1[0]);
        assertEquals(4, indexes1[1]);
    }

    public void testRemoveMiddleAdvice() {
        ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.AbstractPointcutTest").get(0)).createMethodPointcut("* testRemoveMiddleAdvice()").addAdvices(new String[]{"methodAdvice1", "methodAdvice2", "methodAdvice3"});
        ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.AbstractPointcutTest").get(0)).getMethodPointcut("* testRemoveMiddleAdvice()").removeAdvice("methodAdvice2");
        String[] names1 = ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.AbstractPointcutTest").get(0)).getMethodPointcut("* testRemoveMiddleAdvice()").getAdviceNames();
        int[] indexes1 = ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.AbstractPointcutTest").get(0)).getMethodPointcut("* testRemoveMiddleAdvice()").getAdviceIndexes();
        assertEquals(2, names1.length);
        assertEquals("methodAdvice1", names1[0]);
        assertEquals("methodAdvice3", names1[1]);
        assertEquals(2, indexes1.length);
        assertEquals(2, indexes1[0]);
        assertEquals(4, indexes1[1]);
    }

    public void testGetAdvicesIndexTuples() {
        ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.AbstractPointcutTest").get(0)).createMethodPointcut("* testGetAdvicesIndexTuples()").addAdvices(new String[]{"methodAdvice1", "methodAdvice2", "methodAdvice3"});
        String[] adviceNames = ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.AbstractPointcutTest").get(0)).getMethodPointcut("* testGetAdvicesIndexTuples()").getAdviceNames();
        int[] indexes = ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.AbstractPointcutTest").get(0)).getMethodPointcut("* testGetAdvicesIndexTuples()").getAdviceIndexes();
        List advices = ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.AbstractPointcutTest").get(0)).getMethodPointcut("* testGetAdvicesIndexTuples()").getAdviceIndexTuples();
        assertEquals(((AdviceIndexTuple)advices.get(0)).getName(), adviceNames[0]);
        assertEquals(((AdviceIndexTuple)advices.get(1)).getName(), adviceNames[1]);
        assertEquals(((AdviceIndexTuple)advices.get(2)).getName(), adviceNames[2]);
        assertEquals(((AdviceIndexTuple)advices.get(0)).getIndex(), indexes[0]);
        assertEquals(((AdviceIndexTuple)advices.get(1)).getIndex(), indexes[1]);
        assertEquals(((AdviceIndexTuple)advices.get(2)).getIndex(), indexes[2]);
    }

    public void testSetAdvicesIndexTuples() {
        ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.AbstractPointcutTest").get(0)).createMethodPointcut("* testSetAdvicesIndexTuples()");
        List advices = new ArrayList(3);
        advices.add(new AdviceIndexTuple("advice1", 1001));
        advices.add(new AdviceIndexTuple("advice2", 1002));
        advices.add(new AdviceIndexTuple("advice3", 1003));
        ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.AbstractPointcutTest").get(0)).getMethodPointcut("* testSetAdvicesIndexTuples()").setAdviceIndexTuples(advices);
        String[] adviceNames = ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.AbstractPointcutTest").get(0)).getMethodPointcut("* testSetAdvicesIndexTuples()").getAdviceNames();
        int[] indexes = ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.AbstractPointcutTest").get(0)).getMethodPointcut("* testSetAdvicesIndexTuples()").getAdviceIndexes();
        assertEquals(((AdviceIndexTuple)advices.get(0)).getName(), adviceNames[0]);
        assertEquals(((AdviceIndexTuple)advices.get(1)).getName(), adviceNames[1]);
        assertEquals(((AdviceIndexTuple)advices.get(2)).getName(), adviceNames[2]);
        assertEquals(((AdviceIndexTuple)advices.get(0)).getIndex(), indexes[0]);
        assertEquals(((AdviceIndexTuple)advices.get(1)).getIndex(), indexes[1]);
        assertEquals(((AdviceIndexTuple)advices.get(2)).getIndex(), indexes[2]);
    }

    public void testRearrangeAdvices() {
        ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.AbstractPointcutTest").get(0)).createMethodPointcut("* testRearrangeAdvices()").addAdvices(new String[]{"methodAdvice1", "methodAdvice2", "methodAdvice3"});
        List advices = ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.AbstractPointcutTest").get(0)).getMethodPointcut("* testRearrangeAdvices()").getAdviceIndexTuples();
        AdviceIndexTuple tuple1 = ((AdviceIndexTuple)advices.get(0));
        AdviceIndexTuple tuple2 = ((AdviceIndexTuple)advices.get(1));
        AdviceIndexTuple tuple3 = ((AdviceIndexTuple)advices.get(2));
        advices.set(0, tuple3);
        advices.set(1, tuple2);
        advices.set(2, tuple1);
        ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.AbstractPointcutTest").get(0)).getMethodPointcut("* testRearrangeAdvices()").setAdviceIndexTuples(advices);
        String[] adviceNames = ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.AbstractPointcutTest").get(0)).getMethodPointcut("* testRearrangeAdvices()").getAdviceNames();
        assertEquals(tuple3.getName(), adviceNames[0]);
        assertEquals(tuple2.getName(), adviceNames[1]);
        assertEquals(tuple1.getName(), adviceNames[2]);
        int[] indexes = ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.AbstractPointcutTest").get(0)).getMethodPointcut("* testRearrangeAdvices()").getAdviceIndexes();
        assertEquals(tuple3.getIndex(), indexes[0]);
        assertEquals(tuple2.getIndex(), indexes[1]);
        assertEquals(tuple1.getIndex(), indexes[2]);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(AbstractPointcutTest.class);
    }

    public AbstractPointcutTest(String name) {
        super(name);
        AspectWerkz.getSystem("tests").initialize();
    }

    public static class MyPreAdvice extends PreAdvice {
        public MyPreAdvice() {
            super();
        }

        public void execute(final JoinPoint joinpoint) {
        }
    }
}
