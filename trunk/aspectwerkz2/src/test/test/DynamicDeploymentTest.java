/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test;

import java.util.List;

import junit.framework.TestCase;
import org.codehaus.aspectwerkz.metadata.MethodMetaData;
import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.metadata.ReflectionMetaDataMaker;
import org.codehaus.aspectwerkz.NameIndexTuple;
import org.codehaus.aspectwerkz.SystemLoader;
import org.codehaus.aspectwerkz.DeploymentModel;
import org.codehaus.aspectwerkz.aspect.management.Pointcut;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @TODO: this test is deprecated - need a better way of handling dynamic stuff
 */
public class DynamicDeploymentTest extends TestCase implements Loggable {

    private static final String ASPECT_NAME = "test.aspect.DynamicDeploymentTestAspect";
    private static final String NEW_ASPECT_NAME = "test.aspect.DynamicallyCreatedAspect";

    private String m_logString = "";

    private ClassMetaData m_classMetaData = ReflectionMetaDataMaker.createClassMetaData(DynamicDeploymentTest.class);

    public void testReorderAdvicesAtRuntime1() {
        m_logString = "";
        reorderAdvicesTestMethod();
        assertEquals("before1 before2 invocation after2 after1 ", m_logString);

        // get the pointcut by name (can also be retrieved by method meta-data)
        Pointcut pointcut = SystemLoader.getSystem("tests").
                getAspectManager().
                getPointcutManager(ASPECT_NAME).
                getPointcut("pc1 || pc2 || pc3");

        // get the advices
        List advices = pointcut.getAroundAdviceIndexTuples();
        NameIndexTuple tuple1 = (NameIndexTuple)advices.get(0);
        NameIndexTuple tuple2 = (NameIndexTuple)advices.get(1);

        // reorder the advices
        advices.set(0, tuple2);
        advices.set(1, tuple1);

        // set the reordered advices
        pointcut.setAroundAdviceIndexTuples(advices);
    }

    public void testAddAdviceAtRuntime() {
        m_logString = "";
        addAdviceTestMethod();
        assertEquals("before1 invocation after1 ", m_logString);

        MethodMetaData methodMetaData = new MethodMetaData();
        methodMetaData.setName("addAdviceTestMethod");
        methodMetaData.setParameterTypes(new String[]{});
        methodMetaData.setReturnType("void");
        methodMetaData.setExceptionTypes(new String[]{});

        Pointcut methodPointcut = (Pointcut)SystemLoader.getSystem("tests").
                getAspectManager().
                getPointcutManager(ASPECT_NAME).
                getExecutionPointcuts(m_classMetaData, methodMetaData).get(0);

        methodPointcut.addAroundAdvice("test.aspect.DynamicDeploymentTestAspect.advice2");

        m_logString = "";
        addAdviceTestMethod();
        assertEquals("before1 before2 invocation after2 after1 ", m_logString);

        // remove it for other tests
        methodPointcut.removeAroundAdvice("test.aspect.DynamicDeploymentTestAspect.advice2");
    }

    public void testRemoveAdviceAtRuntime() {
        m_logString = "";
        removeAdviceTestMethod();
        assertEquals("before1 before2 invocation after2 after1 ", m_logString);

        MethodMetaData methodMetaData = new MethodMetaData();
        methodMetaData.setName("removeAdviceTestMethod");
        methodMetaData.setParameterTypes(new String[]{});
        methodMetaData.setReturnType("void");
        methodMetaData.setExceptionTypes(new String[]{});

        Pointcut methodPointcut = (Pointcut)SystemLoader.getSystem("tests").
                getAspectManager().
                getPointcutManager(ASPECT_NAME).
                getExecutionPointcuts(m_classMetaData, methodMetaData).get(0);

        List advices = methodPointcut.getAroundAdviceIndexTuples();

        NameIndexTuple adviceTuple = (NameIndexTuple)advices.remove(0);
        methodPointcut.setAroundAdviceIndexTuples(advices);

        m_logString = "";
        removeAdviceTestMethod();
        assertEquals("before2 invocation after2 ", m_logString);

        // restore it for other tests
        advices.add(0, adviceTuple);
        methodPointcut.setAroundAdviceIndexTuples(advices);
    }

    public void testCreateAspectAtRuntime() {
        try {
            // check that we have a pointcut at the createAspectTestMethod method
            m_logString = "";
            createAspectTestMethod();
            assertEquals("before2 invocation after2 ", m_logString);

            // create a new advice
            SystemLoader.getSystem("tests").getAspectManager().createAspect(
                    NEW_ASPECT_NAME,
                    NEW_ASPECT_NAME,
                    DeploymentModel.PER_INSTANCE,
                    null
            );

            // test the some stuff for the aspect
            assertNotNull(
                    SystemLoader.getSystem("tests").
                    getAspectManager().getPointcutManager(NEW_ASPECT_NAME)
            );

            assertEquals(
                    DeploymentModel.PER_INSTANCE,
                    SystemLoader.getSystem("tests").
                    getAspectManager().
                    getPointcutManager(NEW_ASPECT_NAME).
                    getDeploymentModel()
            );

            assertEquals(
                    NEW_ASPECT_NAME,
                    SystemLoader.getSystem("tests").
                    getAspectManager().
                    getPointcutManager(NEW_ASPECT_NAME).
                    getName()
            );

            // test an advice from the aspect in action
            MethodMetaData methodMetaData = new MethodMetaData();
            methodMetaData.setName("createAspectTestMethod");
            methodMetaData.setParameterTypes(new String[]{});
            methodMetaData.setReturnType("void");
            methodMetaData.setExceptionTypes(new String[]{});

            // get an existing pointcut
            Pointcut methodPointcut = (Pointcut)SystemLoader.getSystem("tests").
                    getAspectManager().
                    getPointcutManager(ASPECT_NAME).
                    getExecutionPointcuts(m_classMetaData, methodMetaData).
                    get(0);

            // add the new advice to the pointcut
            methodPointcut.addAroundAdvice("test.aspects.DynamicallyCreatedAspect.advice1");

            // check that it is executed
            m_logString = "";
            createAspectTestMethod();
            assertEquals("before2 beforeNew invocation afterNew after2 ", m_logString);

            //remove it for other tests
            methodPointcut.removeAroundAdvice("test.aspects.DynamicallyCreatedAspect.advice1");
        }
        catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(DynamicDeploymentTest.class);
    }

    public DynamicDeploymentTest(String name) {
        super(name);
        SystemLoader.getSystem("tests").initialize();
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

    public void createAspectTestMethod() {
        log("invocation ");
    }
}
