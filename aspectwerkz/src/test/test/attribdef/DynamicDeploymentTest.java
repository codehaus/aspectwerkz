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
import test.attribdef.Loggable;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class DynamicDeploymentTest extends TestCase implements Loggable {

    private String m_logString = "";
    private ClassMetaData m_classMetaData = ReflectionMetaDataMaker.createClassMetaData(
            DynamicDeploymentTest.class
    );

    public void testReorderAdvicesAtRuntime() {
        m_logString = "";
        reorderAdvicesTestMethod();
        assertEquals("before1 before2 invocation after2 after1 ", m_logString);

        MethodMetaData methodMetaData = new MethodMetaData();
        methodMetaData.setName("reorderAdvicesTestMethod");
        methodMetaData.setParameterTypes(new String[]{});
        methodMetaData.setReturnType("void");
        methodMetaData.setExceptionTypes(new String[]{});

        List advices = ((MethodPointcut)SystemLoader.getSystem("tests").
                getAspectMetaData("test.attribdef.aspect.DynamicDeploymentTestAspect").
                getMethodPointcuts(m_classMetaData, methodMetaData).get(0)).
                getAdviceIndexTuples();

        NameIndexTuple tuple1 = (NameIndexTuple)advices.get(0);
        NameIndexTuple tuple2 = (NameIndexTuple)advices.get(1);

        advices.set(0, tuple2);
        advices.set(1, tuple1);

        ((MethodPointcut)SystemLoader.getSystem("tests").
                getAspectMetaData("test.attribdef.aspect.DynamicDeploymentTestAspect").
                getMethodPointcuts(m_classMetaData, methodMetaData).get(0)).
                setAdviceIndexTuples(advices);

        m_logString = "";
        reorderAdvicesTestMethod();
        assertEquals("before2 before1 invocation after1 after2 ", m_logString);

        //reorder for other tests
        advices.set(0, tuple1);
        advices.set(1, tuple2);
        ((MethodPointcut)SystemLoader.getSystem("tests").
                getAspectMetaData("test.attribdef.aspect.DynamicDeploymentTestAspect").
                getMethodPointcuts(m_classMetaData, methodMetaData).get(0)).
                setAdviceIndexTuples(advices);
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

        ((MethodPointcut)SystemLoader.getSystem("tests").
                getAspectMetaData("test.attribdef.aspect.DynamicDeploymentTestAspect").
                getMethodPointcuts(m_classMetaData, methodMetaData).get(0)).
                addAdvice("methodAdvice3");

        m_logString = "";
        addAdviceTestMethod();
        assertEquals("before1 before2 invocation after2 after1 ", m_logString);

        // remove it for other tests
        ((MethodPointcut)SystemLoader.getSystem("tests").
                getAspectMetaData("test.attribdef.aspect.DynamicDeploymentTestAspect").
                getMethodPointcuts(m_classMetaData, methodMetaData).get(0)).
                removeAdvice("methodAdvice3");
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

        List advices = ((MethodPointcut)SystemLoader.getSystem("tests").
                getAspectMetaData("test.attribdef.aspect.DynamicDeploymentTestAspect").
                getMethodPointcuts(m_classMetaData, methodMetaData).get(0)).
                getAdviceIndexTuples();
        NameIndexTuple adviceTuple0 = (NameIndexTuple)advices.remove(0);
        ((MethodPointcut)SystemLoader.getSystem("tests").
                getAspectMetaData("test.attribdef.aspect.DynamicDeploymentTestAspect").
                getMethodPointcuts(m_classMetaData, methodMetaData).get(0)).
                setAdviceIndexTuples(advices);

        m_logString = "";
        removeAdviceTestMethod();
        assertEquals("before2 invocation after2 ", m_logString);

        // restore it for other tests
        // the methodAdvice2 was first
        advices.add(0, adviceTuple0);
        ((MethodPointcut)SystemLoader.getSystem("tests").
                getAspectMetaData("test.attribdef.aspect.DynamicDeploymentTestAspect").
                getMethodPointcuts(m_classMetaData, methodMetaData).get(0)).
                setAdviceIndexTuples(advices);
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
