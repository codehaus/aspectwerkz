/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.attribdef;

import java.util.List;

import junit.framework.TestCase;
import org.codehaus.aspectwerkz.pointcut.MethodPointcut;
import org.codehaus.aspectwerkz.metadata.MethodMetaData;
import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.metadata.ReflectionMetaDataMaker;
import org.codehaus.aspectwerkz.NameIndexTuple;
import org.codehaus.aspectwerkz.SystemLoader;
import org.codehaus.aspectwerkz.DeploymentModel;
import org.codehaus.aspectwerkz.attribdef.AttribDefSystem;
import test.attribdef.Loggable;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class DynamicDeploymentTest extends TestCase implements Loggable {

    private String m_logString = "";
    private ClassMetaData m_classMetaData = ReflectionMetaDataMaker.createClassMetaData(
            DynamicDeploymentTest.class
    );

    public void testReorderAdvicesAtRuntime1() {
        m_logString = "";
        reorderAdvicesTestMethod();
        assertEquals("before1 before2 invocation after2 after1 ", m_logString);

        // get the pointcut by name (can also be retrieved by method meta-data)
        MethodPointcut pointcut = SystemLoader.getSystem("tests").
                getAspectMetaData("test.attribdef.aspect.DynamicDeploymentTestAspect").
                getMethodPointcut("pc1");

        // get the advices
        List advices = pointcut.getAdviceIndexTuples();
        NameIndexTuple tuple1 = (NameIndexTuple)advices.get(0);
        NameIndexTuple tuple2 = (NameIndexTuple)advices.get(1);

        // reorder the advices
        advices.set(0, tuple2);
        advices.set(1, tuple1);

        // set the reordered advices
        pointcut.setAdviceIndexTuples(advices);
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

        MethodPointcut methodPointcut = (MethodPointcut)SystemLoader.getSystem("tests").
                        getAspectMetaData("test.attribdef.aspect.DynamicDeploymentTestAspect").
                        getMethodPointcuts(m_classMetaData, methodMetaData).get(0);

        methodPointcut.addAdvice("test.attribdef.aspect.DynamicDeploymentTestAspect.advice2");

        m_logString = "";
        addAdviceTestMethod();
        assertEquals("before1 before2 invocation after2 after1 ", m_logString);

        // remove it for other tests
        methodPointcut.removeAdvice("test.attribdef.aspect.DynamicDeploymentTestAspect.advice2");
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

        MethodPointcut methodPointcut = (MethodPointcut)SystemLoader.getSystem("tests").
                        getAspectMetaData("test.attribdef.aspect.DynamicDeploymentTestAspect").
                        getMethodPointcuts(m_classMetaData, methodMetaData).get(0);

        List advices = methodPointcut.getAdviceIndexTuples();

        NameIndexTuple adviceTuple = (NameIndexTuple)advices.remove(0);
        methodPointcut.setAdviceIndexTuples(advices);

        m_logString = "";
        removeAdviceTestMethod();
        assertEquals("before2 invocation after2 ", m_logString);

        // restore it for other tests
        advices.add(0, adviceTuple);
        methodPointcut.setAdviceIndexTuples(advices);
    }

//    public void testCreateAdviceAtRuntime() {
//        try {
//            // create the new advice
//            ((AttribDefSystem)SystemLoader.getSystem("tests")).createAdvice(
//                    "createTransientAdviceTest",
//                    "test.attribdef.DynamicallyCreatedTransientAdvice",
//                    "perInstance",
//                    null
//            );
//
//            // test the easy stuff
//            assertNotNull(((AttribDefSystem)SystemLoader.getSystem("tests")).getAdvice("createTransientAdviceTest"));
//            assertEquals(DeploymentModel.getDeploymentModelAsInt("perInstance"),
//                    ((AttribDefSystem)SystemLoader.getSystem("tests")).getAdvice("createTransientAdviceTest").
//                    getDeploymentModel());
//            assertEquals("createTransientAdviceTest", ((AttribDefSystem)SystemLoader.getSystem("tests")).
//                    getAdvice("createTransientAdviceTest").getName());
//
//            // test it in action
//            MethodMetaData methodMetaData = new MethodMetaData();
//            methodMetaData.setName("createTransientAdviceTestMethod");
//            methodMetaData.setParameterTypes(new String[]{});
//            methodMetaData.setReturnType("void");
//            methodMetaData.setExceptionTypes(new String[]{});
//
//            ((MethodPointcut)SystemLoader.getSystem("tests").getAspectMetaData("test.attribdef.aspect.DynamicDeploymentTestAspect").
//                    getMethodPointcuts(m_classMetaData, methodMetaData).
//                    get(0)).addAdvice("createTransientAdviceTest");
//
//            m_logString = "";
//            createTransientAdviceTestMethod();
//            assertEquals("before invocation after ", m_logString);
//
//            //remove it for other tests
//            ((MethodPointcut)SystemLoader.getSystem("tests").getAspectMetaData("test.attribdef.aspect.DynamicDeploymentTestAspect").
//                    getMethodPointcuts(m_classMetaData, methodMetaData).
//                    get(0)).removeAdvice("createTransientAdviceTest");
//        }
//        catch (Exception e) {
//            fail();
//        }
//    }

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

    public void createTransientAdviceTestMethod() {
        log("invocation ");
    }

    public void createPersistentAdviceTestMethod() {
    }
}
