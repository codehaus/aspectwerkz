package test;

import junit.framework.TestCase;

import org.codehaus.aspectwerkz.AspectWerkz;
import org.codehaus.aspectwerkz.Aspect;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.advice.PreAdvice;
import org.codehaus.aspectwerkz.advice.PostAdvice;
import org.codehaus.aspectwerkz.advice.AdviceIndexTuple;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: FieldPointcutTest.java,v 1.2 2003-06-09 07:04:13 jboner Exp $
 */
public class FieldPointcutTest extends TestCase {

    public void testGetPointcutName() {
        ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).createGetFieldPointcut("* testGetAdviceNames");
        assertEquals("* testGetAdviceNames", ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).getGetFieldPointcut("* testGetAdviceNames").getName());
    }

    public void testGetPreAdviceNames() {
        ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).createGetFieldPointcut("* testGetAdviceNames").addPreAdvice("preAdvice1");
        ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).createGetFieldPointcut("* testGetAdviceNames").addPreAdvice("preAdvice2");
        ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).createGetFieldPointcut("* testGetAdviceNames").addPreAdvice("preAdvice3");
        String[] names = ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).getGetFieldPointcut("* testGetAdviceNames").getPreAdviceNames();
        assertEquals(3, names.length);
        assertEquals("preAdvice1", names[0]);
        assertEquals("preAdvice2", names[1]);
        assertEquals("preAdvice3", names[2]);
    }

    public void testGetPreAdviceIndexes() {
        ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).createGetFieldPointcut("* testGetAdviceIndexes").addPreAdvice("preAdvice1");
        ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).createGetFieldPointcut("* testGetAdviceIndexes").addPreAdvice("preAdvice2");
        ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).createGetFieldPointcut("* testGetAdviceIndexes").addPreAdvice("preAdvice3");
        int[] indexes = ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).getGetFieldPointcut("* testGetAdviceIndexes").getPreAdviceIndexes();
        assertEquals(3, indexes.length);
        assertEquals(22, indexes[0]);
        assertEquals(23, indexes[1]);
        assertEquals(24, indexes[2]);
    }

    public void testAddSinglePreAdvices() {
        ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).createGetFieldPointcut("* addSingleAdvices").addPreAdvice("preAdvice1");
        ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).createGetFieldPointcut("* addSingleAdvices").addPreAdvice("preAdvice2");
        ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).createGetFieldPointcut("* addSingleAdvices").addPreAdvice("preAdvice3");

        String[] names1 = ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).getGetFieldPointcut("* addSingleAdvices").getPreAdviceNames();
        assertEquals(3, names1.length);
        assertEquals("preAdvice1", names1[0]);
        assertEquals("preAdvice2", names1[1]);
        assertEquals("preAdvice3", names1[2]);
        int[] indexes1 = ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).getGetFieldPointcut("* addSingleAdvices").getPreAdviceIndexes();
        assertEquals(3, indexes1.length);
        assertEquals(22, indexes1[0]);
        assertEquals(23, indexes1[1]);
        assertEquals(24, indexes1[2]);
    }

    public void testAddMultiplePreAdvices() {
        ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).createGetFieldPointcut("* addMultipleAdvices").addPreAdvices(new String[]{"preAdvice1", "preAdvice2", "preAdvice3"});
        String[] names1 = ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).getGetFieldPointcut("* addMultipleAdvices").getPreAdviceNames();
        assertEquals(3, names1.length);
        assertEquals("preAdvice1", names1[0]);
        assertEquals("preAdvice2", names1[1]);
        assertEquals("preAdvice3", names1[2]);
        int[] indexes1 = ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).getGetFieldPointcut("* addMultipleAdvices").getPreAdviceIndexes();
        assertEquals(3, indexes1.length);
        assertEquals(22, indexes1[0]);
        assertEquals(23, indexes1[1]);
        assertEquals(24, indexes1[2]);
    }

    public void testRemoveEndPreAdvice() {
        ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).createGetFieldPointcut("* removeEndAdvice").addPreAdvices(new String[]{"preAdvice1", "preAdvice2", "preAdvice3"});
        ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).getGetFieldPointcut("* removeEndAdvice").removePreAdvice("preAdvice3");
        String[] names1 = ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).getGetFieldPointcut("* removeEndAdvice").getPreAdviceNames();
        int[] indexes1 = ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).getGetFieldPointcut("* removeEndAdvice").getPreAdviceIndexes();
        assertEquals(2, names1.length);
        assertEquals("preAdvice1", names1[0]);
        assertEquals("preAdvice2", names1[1]);
        assertEquals(2, indexes1.length);
        assertEquals(22, indexes1[0]);
        assertEquals(23, indexes1[1]);
    }

    public void testRemoveStartPreAdvice() {
        ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).createGetFieldPointcut("* removeStartAdvice").addPreAdvices(new String[]{"preAdvice1", "preAdvice2", "preAdvice3"});
        ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).getGetFieldPointcut("* removeStartAdvice").removePreAdvice("preAdvice1");
        String[] names1 = ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).getGetFieldPointcut("* removeStartAdvice").getPreAdviceNames();
        int[] indexes1 = ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).getGetFieldPointcut("* removeStartAdvice").getPreAdviceIndexes();
        assertEquals(2, names1.length);
        assertEquals("preAdvice2", names1[0]);
        assertEquals("preAdvice3", names1[1]);
        assertEquals(2, indexes1.length);
        assertEquals(23, indexes1[0]);
        assertEquals(24, indexes1[1]);
    }

    public void testRemoveMiddlePreAdvice() {
        ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).createGetFieldPointcut("* removeMiddleAdvice").addPreAdvices(new String[]{"preAdvice1", "preAdvice2", "preAdvice3"});
        ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).getGetFieldPointcut("* removeMiddleAdvice").removePreAdvice("preAdvice2");
        String[] names1 = ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).getGetFieldPointcut("* removeMiddleAdvice").getPreAdviceNames();
        int[] indexes1 = ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).getGetFieldPointcut("* removeMiddleAdvice").getPreAdviceIndexes();
        assertEquals(2, names1.length);
        assertEquals("preAdvice1", names1[0]);
        assertEquals("preAdvice3", names1[1]);
        assertEquals(2, indexes1.length);
        assertEquals(22, indexes1[0]);
        assertEquals(24, indexes1[1]);
    }

    public void testGetPostAdviceNames() {
        ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).createGetFieldPointcut("* testGetAdviceNames").addPostAdvice("preAdvice1");
        ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).createGetFieldPointcut("* testGetAdviceNames").addPostAdvice("preAdvice2");
        ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).createGetFieldPointcut("* testGetAdviceNames").addPostAdvice("preAdvice3");
        String[] names = ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).getGetFieldPointcut("* testGetAdviceNames").getPostAdviceNames();
        assertEquals(3, names.length);
        assertEquals("preAdvice1", names[0]);
        assertEquals("preAdvice2", names[1]);
        assertEquals("preAdvice3", names[2]);
    }

    public void testGetPostAdviceIndexes() {
        ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).createGetFieldPointcut("* testGetAdviceIndexes").addPostAdvice("preAdvice1");
        ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).createGetFieldPointcut("* testGetAdviceIndexes").addPostAdvice("preAdvice2");
        ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).createGetFieldPointcut("* testGetAdviceIndexes").addPostAdvice("preAdvice3");
        int[] indexes = ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).getGetFieldPointcut("* testGetAdviceIndexes").getPostAdviceIndexes();
        assertEquals(3, indexes.length);
        assertEquals(22, indexes[0]);
        assertEquals(23, indexes[1]);
        assertEquals(24, indexes[2]);
    }

    public void testAddSinglePostAdvices() {
        ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).createGetFieldPointcut("* addSingleAdvices").addPostAdvice("preAdvice1");
        ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).createGetFieldPointcut("* addSingleAdvices").addPostAdvice("preAdvice2");
        ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).createGetFieldPointcut("* addSingleAdvices").addPostAdvice("preAdvice3");

        String[] names1 = ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).getGetFieldPointcut("* addSingleAdvices").getPostAdviceNames();
        assertEquals(3, names1.length);
        assertEquals("preAdvice1", names1[0]);
        assertEquals("preAdvice2", names1[1]);
        assertEquals("preAdvice3", names1[2]);
        int[] indexes1 = ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).getGetFieldPointcut("* addSingleAdvices").getPostAdviceIndexes();
        assertEquals(3, indexes1.length);
        assertEquals(22, indexes1[0]);
        assertEquals(23, indexes1[1]);
        assertEquals(24, indexes1[2]);
    }

    public void testAddMultiplePostAdvices() {
        ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).createGetFieldPointcut("* addMultipleAdvices").addPostAdvices(new String[]{"preAdvice1", "preAdvice2", "preAdvice3"});
        String[] names1 = ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).getGetFieldPointcut("* addMultipleAdvices").getPostAdviceNames();
        assertEquals(3, names1.length);
        assertEquals("preAdvice1", names1[0]);
        assertEquals("preAdvice2", names1[1]);
        assertEquals("preAdvice3", names1[2]);
        int[] indexes1 = ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).getGetFieldPointcut("* addMultipleAdvices").getPostAdviceIndexes();
        assertEquals(3, indexes1.length);
        assertEquals(22, indexes1[0]);
        assertEquals(23, indexes1[1]);
        assertEquals(24, indexes1[2]);
    }

    public void testRemoveEndPostAdvice() {
        ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).createGetFieldPointcut("* removeEndAdvice").addPostAdvices(new String[]{"preAdvice1", "preAdvice2", "preAdvice3"});
        ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).getGetFieldPointcut("* removeEndAdvice").removePostAdvice("preAdvice3");
        String[] names1 = ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).getGetFieldPointcut("* removeEndAdvice").getPostAdviceNames();
        int[] indexes1 = ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).getGetFieldPointcut("* removeEndAdvice").getPostAdviceIndexes();
        assertEquals(2, names1.length);
        assertEquals("preAdvice1", names1[0]);
        assertEquals("preAdvice2", names1[1]);
        assertEquals(2, indexes1.length);
        assertEquals(22, indexes1[0]);
        assertEquals(23, indexes1[1]);
    }

    public void testRemoveStartPostAdvice() {
        ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).createGetFieldPointcut("* removeStartAdvice").addPostAdvices(new String[]{"preAdvice1", "preAdvice2", "preAdvice3"});
        ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).getGetFieldPointcut("* removeStartAdvice").removePostAdvice("preAdvice1");
        String[] names1 = ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).getGetFieldPointcut("* removeStartAdvice").getPostAdviceNames();
        int[] indexes1 = ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).getGetFieldPointcut("* removeStartAdvice").getPostAdviceIndexes();
        assertEquals(2, names1.length);
        assertEquals("preAdvice2", names1[0]);
        assertEquals("preAdvice3", names1[1]);
        assertEquals(2, indexes1.length);
        assertEquals(23, indexes1[0]);
        assertEquals(24, indexes1[1]);
    }

    public void testRemoveMiddlePostAdvice() {
        ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).createGetFieldPointcut("* removeMiddleAdvice").addPostAdvices(new String[]{"preAdvice1", "preAdvice2", "preAdvice3"});
        ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).getGetFieldPointcut("* removeMiddleAdvice").removePostAdvice("preAdvice2");
        String[] names1 = ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).getGetFieldPointcut("* removeMiddleAdvice").getPostAdviceNames();
        int[] indexes1 = ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).getGetFieldPointcut("* removeMiddleAdvice").getPostAdviceIndexes();
        assertEquals(2, names1.length);
        assertEquals("preAdvice1", names1[0]);
        assertEquals("preAdvice3", names1[1]);
        assertEquals(2, indexes1.length);
        assertEquals(22, indexes1[0]);
        assertEquals(24, indexes1[1]);
    }

    public void testGetPreAdvicesIndexTuples() {
        ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).createGetFieldPointcut("* testGetPreAdvicesIndexTuples").addPreAdvices(new String[]{"preAdvice1", "preAdvice2", "preAdvice3"});
        String[] adviceNames = ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).getGetFieldPointcut("* testGetPreAdvicesIndexTuples").getPreAdviceNames();
        int[] indexes = ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).getGetFieldPointcut("* testGetPreAdvicesIndexTuples").getPreAdviceIndexes();
        AdviceIndexTuple[] advices = ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).getGetFieldPointcut("* testGetPreAdvicesIndexTuples").getPreAdviceIndexTuples();
        assertEquals(3, advices.length);
        assertEquals(3, adviceNames.length);
        assertEquals(3, indexes.length);
        assertEquals(advices[0].getName(), adviceNames[0]);
        assertEquals(advices[1].getName(), adviceNames[1]);
        assertEquals(advices[2].getName(), adviceNames[2]);
        assertEquals(advices[0].getIndex(), indexes[0]);
        assertEquals(advices[1].getIndex(), indexes[1]);
        assertEquals(advices[2].getIndex(), indexes[2]);
    }

    public void testSetPreAdvicesIndexTuples() {
        ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).createGetFieldPointcut("* testSetPreAdvicesIndexTuples");
        AdviceIndexTuple[] advices = new AdviceIndexTuple[3];
        advices[0] = new AdviceIndexTuple("advice1", 1001);
        advices[1] = new AdviceIndexTuple("advice2", 1002);
        advices[2] = new AdviceIndexTuple("advice3", 1003);
        ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).getGetFieldPointcut("* testSetPreAdvicesIndexTuples").setPreAdviceIndexTuples(advices);
        String[] adviceNames = ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).getGetFieldPointcut("* testSetPreAdvicesIndexTuples").getPreAdviceNames();
        int[] indexes = ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).getGetFieldPointcut("* testSetPreAdvicesIndexTuples").getPreAdviceIndexes();
        assertEquals(3, advices.length);
        assertEquals(3, adviceNames.length);
        assertEquals(3, indexes.length);
        assertEquals(advices[0].getName(), adviceNames[0]);
        assertEquals(advices[1].getName(), adviceNames[1]);
        assertEquals(advices[2].getName(), adviceNames[2]);
        assertEquals(advices[0].getIndex(), indexes[0]);
        assertEquals(advices[1].getIndex(), indexes[1]);
        assertEquals(advices[2].getIndex(), indexes[2]);
    }

    public void testRearrangePreAdvices() {
        ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).createGetFieldPointcut("* testRearrangePreAdvices").addPreAdvices(new String[]{"preAdvice1", "preAdvice2", "preAdvice3"});
        AdviceIndexTuple[] advices = ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).getGetFieldPointcut("* testRearrangePreAdvices").getPreAdviceIndexTuples();
        assertEquals(3, advices.length);
        AdviceIndexTuple tuple1 = advices[0];
        AdviceIndexTuple tuple2 = advices[1];
        AdviceIndexTuple tuple3 = advices[2];
        advices[0] = tuple3;
        advices[1] = tuple2;
        advices[2] = tuple1;
        ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).getGetFieldPointcut("* testRearrangePreAdvices").setPreAdviceIndexTuples(advices);
        String[] adviceNames = ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).getGetFieldPointcut("* testRearrangePreAdvices").getPreAdviceNames();
        assertEquals(3, adviceNames.length);
        assertEquals(tuple3.getName(), adviceNames[0]);
        assertEquals(tuple2.getName(), adviceNames[1]);
        assertEquals(tuple1.getName(), adviceNames[2]);
        int[] indexes = ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).getGetFieldPointcut("* testRearrangePreAdvices").getPreAdviceIndexes();
        assertEquals(3, indexes.length);
        assertEquals(tuple3.getIndex(), indexes[0]);
        assertEquals(tuple2.getIndex(), indexes[1]);
        assertEquals(tuple1.getIndex(), indexes[2]);
    }

    public void testGetPostAdvicesIndexTuples() {
        ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).createSetFieldPointcut("* testGetPostAdvicesIndexTuples").addPostAdvices(new String[]{"postAdvice1", "postAdvice2", "postAdvice3"});

        String[] adviceNames = ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).getSetFieldPointcut("* testGetPostAdvicesIndexTuples").getPostAdviceNames();
        assertEquals(3, adviceNames.length);

        int[] indexes = ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).getSetFieldPointcut("* testGetPostAdvicesIndexTuples").getPostAdviceIndexes();
        assertEquals(3, indexes.length);

        AdviceIndexTuple[] advices = ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).getSetFieldPointcut("* testGetPostAdvicesIndexTuples").getPostAdviceIndexTuples();
        assertEquals(3, advices.length);
        assertEquals(advices[0].getName(), adviceNames[0]);
        assertEquals(advices[1].getName(), adviceNames[1]);
        assertEquals(advices[2].getName(), adviceNames[2]);
        assertEquals(advices[0].getIndex(), indexes[0]);
        assertEquals(advices[1].getIndex(), indexes[1]);
        assertEquals(advices[2].getIndex(), indexes[2]);
    }

    public void testSetPostAdvicesIndexTuples() {
        ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).createSetFieldPointcut("* testSetPostAdvicesIndexTuples");
        AdviceIndexTuple[] advices = new AdviceIndexTuple[3];
        advices[0] = new AdviceIndexTuple("advice1", 1001);
        advices[1] = new AdviceIndexTuple("advice2", 1002);
        advices[2] = new AdviceIndexTuple("advice3", 1003);
        ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).getSetFieldPointcut("* testSetPostAdvicesIndexTuples").setPostAdviceIndexTuples(advices);

        String[] adviceNames = ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).getSetFieldPointcut("* testSetPostAdvicesIndexTuples").getPostAdviceNames();
        assertEquals(3, adviceNames.length);

        int[] indexes = ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).getSetFieldPointcut("* testSetPostAdvicesIndexTuples").getPostAdviceIndexes();
        assertEquals(3, indexes.length);

        assertEquals(advices[0].getName(), adviceNames[0]);
        assertEquals(advices[1].getName(), adviceNames[1]);
        assertEquals(advices[2].getName(), adviceNames[2]);
        assertEquals(advices[0].getIndex(), indexes[0]);
        assertEquals(advices[1].getIndex(), indexes[1]);
        assertEquals(advices[2].getIndex(), indexes[2]);
    }

    public void testRearrangePostAdvices() {
        ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).createSetFieldPointcut("* testRearrangePostAdvices").addPostAdvices(new String[]{"postAdvice1", "postAdvice2", "postAdvice3"});
        AdviceIndexTuple[] advices = ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).getSetFieldPointcut("* testRearrangePostAdvices").getPostAdviceIndexTuples();
        assertEquals(3, advices.length);

        AdviceIndexTuple tuple1 = advices[0];
        AdviceIndexTuple tuple2 = advices[1];
        AdviceIndexTuple tuple3 = advices[2];
        advices[0] = tuple3;
        advices[1] = tuple2;
        advices[2] = tuple1;
        ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).getSetFieldPointcut("* testRearrangePostAdvices").setPostAdviceIndexTuples(advices);
        String[] adviceNames = ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).getSetFieldPointcut("* testRearrangePostAdvices").getPostAdviceNames();
        assertEquals(3, adviceNames.length);

        assertEquals(tuple3.getName(), adviceNames[0]);
        assertEquals(tuple2.getName(), adviceNames[1]);
        assertEquals(tuple1.getName(), adviceNames[2]);

        int[] indexes = ((Aspect)AspectWerkz.getSystem("tests").getAspects("test.FieldPointcutTest").get(0)).getSetFieldPointcut("* testRearrangePostAdvices").getPostAdviceIndexes();
        assertEquals(3, indexes.length);
        assertEquals(tuple3.getIndex(), indexes[0]);
        assertEquals(tuple2.getIndex(), indexes[1]);
        assertEquals(tuple1.getIndex(), indexes[2]);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(FieldPointcutTest.class);
    }

    public FieldPointcutTest(String name) {
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

    public static class MyPostAdvice extends PostAdvice {
        public MyPostAdvice() {
            super();
        }
        public void execute(final JoinPoint joinpoint) {
        }
    }
}
