/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test;

import junit.framework.TestCase;

import org.codehaus.aspectwerkz.definition.expression.Expression;
import org.codehaus.aspectwerkz.definition.expression.ExpressionNamespace;
import org.codehaus.aspectwerkz.definition.expression.PointcutType;
import org.codehaus.aspectwerkz.exception.ExpressionException;
import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.metadata.FieldMetaData;
import org.codehaus.aspectwerkz.metadata.MetaDataMaker;
import org.codehaus.aspectwerkz.metadata.MethodMetaData;
import org.codehaus.aspectwerkz.metadata.ReflectionMetaDataMaker;

/**
 * Test expression syntax
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class ExpressionTest extends TestCase
{
    private ReflectionMetaDataMaker m_metaDataMaker = MetaDataMaker
        .getReflectionMetaDataMaker(ExpressionTest.class.getClassLoader());
    private ExpressionNamespace space = ExpressionNamespace
        .getExpressionNamespace();
    private String m_name;
    private String m_type;
    private String m_dummy;

    public ExpressionTest(String name)
    {
        super(name);
    }

    public void testBuildSingleAnonymousExpression()
    {
        try
        {
            Expression root = ExpressionNamespace.getExpressionNamespace()
                                                 .createExpression("* test.ExpressionTest.set(..)",
                    PointcutType.EXECUTION);
        }
        catch (Exception e)
        {
            fail(e.toString());
        }
    }

    public void testBuildSingleAnonymousHierarchicalExpression()
    {
        try
        {
            Expression root = ExpressionNamespace.getExpressionNamespace()
                                                 .createExpression("* foo.bar.Baz+.set(..)",
                    PointcutType.EXECUTION);
        }
        catch (Exception e)
        {
            fail(e.toString());
        }
    }

    public void testBuildExpressionWithTheSamePointcutTypes()
    {
        try
        {
            ExpressionNamespace space = ExpressionNamespace
                .getExpressionNamespace();

            space.registerExpression(space.createExpression(
                    "* test.ExpressionTest.set(..)", "", "pc1",
                    PointcutType.EXECUTION));
            space.registerExpression(space.createExpression(
                    "* test.ExpressionTest.get(..)", "", "pc2",
                    PointcutType.EXECUTION));

            Expression root = ExpressionNamespace.getExpressionNamespace()
                                                 .createExpression("pc1&&pc2");
        }
        catch (Exception e)
        {
            fail(e.toString());
        }
    }

    public void testBuildExpressionWithDifferentPointcutTypes()
    {
        try
        {
            ExpressionNamespace space = ExpressionNamespace
                .getExpressionNamespace();

            space.registerExpression(space.createExpression(
                    "* test.ExpressionTest.set(..)", "", "pc1",
                    PointcutType.EXECUTION));
            space.registerExpression(space.createExpression(
                    "call(* test.ExpressionTest.get(..))", "", "pc2")); //implicit type

            Expression root = ExpressionNamespace.getExpressionNamespace()
                                                 .createExpression("pc1 && NOT pc2");

            fail("expected exception");
        }
        catch (Exception e)
        {
        }
    }

    public void testBuildExpressionWithDifferentPointcutTypesButOneIsCflow()
    {
        try
        {
            ExpressionNamespace space = ExpressionNamespace
                .getExpressionNamespace();

            space.registerExpression(space.createExpression(
                    "* test.ExpressionTest.set(..)", "", "pc1",
                    PointcutType.EXECUTION));
            space.registerExpression(space.createExpression(
                    "* test.ExpressionTest.get(..)", "", "cf1",
                    PointcutType.CFLOW));
            space.registerExpression(space.createExpression(
                    "* test.ExpressionTest.get2(..)", "", "cf2",
                    PointcutType.CFLOW));

            Expression root1 = ExpressionNamespace.getExpressionNamespace()
                                                  .createExpression("pc1 AND (cf1 OR cf2)");
            Expression root2 = ExpressionNamespace.getExpressionNamespace()
                                                  .createExpression("pc1 AND cf1");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    public void testMatchSingleAnonymousExpression()
    {
        try
        {
            ExpressionNamespace space = ExpressionNamespace
                .getExpressionNamespace();
            Expression root = space.createExpression("* test.ExpressionTest.set(..)",
                    PointcutType.EXECUTION);

            Expression rootAnonymous = space.createExpression(
                    "execution(* test.ExpressionTest.set(..))"); //type autodetection
            Expression rootAnonymous2 = space.createExpression(
                    "execution(* test.hierarchicalpattern.DummyInterface1+.testMethod1(..))"); //type autodetection

            ClassMetaData classMetaDataTrue = m_metaDataMaker
                .createClassMetaData(ExpressionTest.class);
            ClassMetaData classMetaDataFalse = m_metaDataMaker
                .createClassMetaData(ExpressionException.class);
            MethodMetaData methodMetaDataTrue = m_metaDataMaker
                .createMethodMetaData(ExpressionTest.class.getDeclaredMethod(
                        "set", new Class[] {  }));
            MethodMetaData methodMetaDataFalse = m_metaDataMaker
                .createMethodMetaData(ExpressionTest.class.getDeclaredMethod(
                        "get", new Class[] {  }));

            assertTrue(root.match(classMetaDataTrue));
            assertTrue(rootAnonymous.match(classMetaDataTrue));
            assertTrue(root.match(classMetaDataTrue, methodMetaDataTrue));
            assertTrue(rootAnonymous.match(classMetaDataTrue, methodMetaDataTrue));
            assertFalse(root.match(classMetaDataFalse));
            assertFalse(rootAnonymous.match(classMetaDataFalse));
            assertFalse(root.match(classMetaDataTrue, methodMetaDataFalse));
            assertFalse(rootAnonymous.match(classMetaDataTrue,
                    methodMetaDataFalse));
        }
        catch (Exception e)
        {
            fail(e.toString());
        }
    }

    public void testOneLevel_EXECUTION_OR()
    {
        try
        {
            ExpressionNamespace space = ExpressionNamespace
                .getExpressionNamespace();

            space.registerExpression("* test.ExpressionTest.set(..)", "",
                "pc1", PointcutType.EXECUTION);
            space.registerExpression("* test.ExpressionTest.get(..)", "",
                "pc2", PointcutType.EXECUTION);

            Expression root = space.createExpression("pc1 || pc2");
            Expression rootAnonymous = space.createExpression(
                    "pc1 || execution(* test.ExpressionTest.get(..))");
            Expression rootAnonymousAnonymous = space.createExpression(
                    "execution(* test.ExpressionTest.set(..)) || execution(* test.ExpressionTest.get(..))");

            ClassMetaData classMetaData1 = m_metaDataMaker.createClassMetaData(ExpressionTest.class);
            ClassMetaData classMetaData2 = m_metaDataMaker.createClassMetaData(ExpressionException.class);
            MethodMetaData methodMetaData1 = ReflectionMetaDataMaker
                .createMethodMetaData(ExpressionTest.class.getDeclaredMethod(
                        "set", new Class[] {  }));
            MethodMetaData methodMetaData2 = ReflectionMetaDataMaker
                .createMethodMetaData(ExpressionTest.class.getDeclaredMethod(
                        "get", new Class[] {  }));
            MethodMetaData methodMetaData3 = ReflectionMetaDataMaker
                .createMethodMetaData(ExpressionTest.class.getDeclaredMethod(
                        "suite", new Class[] {  }));

            assertTrue(root.match(classMetaData1, PointcutType.EXECUTION));
            assertTrue(rootAnonymous.match(classMetaData1,
                    PointcutType.EXECUTION));
            assertTrue(rootAnonymousAnonymous.match(classMetaData1,
                    PointcutType.EXECUTION));

            assertFalse(root.match(classMetaData2, PointcutType.EXECUTION));
            assertFalse(rootAnonymous.match(classMetaData2,
                    PointcutType.EXECUTION));
            assertFalse(rootAnonymousAnonymous.match(classMetaData2,
                    PointcutType.EXECUTION));

            assertTrue(root.match(classMetaData1, methodMetaData1,
                    PointcutType.EXECUTION));
            assertTrue(rootAnonymous.match(classMetaData1, methodMetaData1,
                    PointcutType.EXECUTION));
            assertTrue(rootAnonymousAnonymous.match(classMetaData1,
                    methodMetaData1, PointcutType.EXECUTION));

            assertTrue(root.match(classMetaData1, methodMetaData2,
                    PointcutType.EXECUTION));
            assertTrue(rootAnonymous.match(classMetaData1, methodMetaData2,
                    PointcutType.EXECUTION));
            assertTrue(rootAnonymousAnonymous.match(classMetaData1,
                    methodMetaData2, PointcutType.EXECUTION));

            //            long loop = 100000;
            //            long t1 = System.currentTimeMillis();
            //            for (int i = 0; i < loop; i++) {
            //                assertTrue(root.match(classMetaData1, methodMetaData2, PointcutType.EXECUTION));
            //            } System.out.println("\tNamed expression: " + (System.currentTimeMillis()-t1));
            //            t1 = System.currentTimeMillis();
            //            for (int i = 0; i < loop; i++) {
            //                assertTrue(rootAnonymous.match(classMetaData1, methodMetaData2, PointcutType.EXECUTION));
            //            } System.out.println("\tHalfNamed HalfAnonymous expression: " + (System.currentTimeMillis()-t1));
            //            t1 = System.currentTimeMillis();
            //            for (int i = 0; i < loop; i++) {
            //                assertTrue(rootAnonymousAnonymous.match(classMetaData1, methodMetaData2, PointcutType.EXECUTION));
            //            } System.out.println("\tAnonymous expression: " + (System.currentTimeMillis()-t1));
            assertFalse(root.match(classMetaData1, methodMetaData3,
                    PointcutType.EXECUTION));
            assertFalse(rootAnonymous.match(classMetaData1, methodMetaData3,
                    PointcutType.EXECUTION));
            assertFalse(rootAnonymousAnonymous.match(classMetaData1,
                    methodMetaData3, PointcutType.EXECUTION));
        }
        catch (Exception e)
        {
            fail(e.toString());
        }
    }

    //    public void testOneLevel_EXECUTION_OR_WITHATTRIBUTE() {
    //        try {
    //            ExpressionNamespace space = ExpressionNamespace.getExpressionNamespace();
    //            space.registerExpression("* test.ExpressionTest.set(..)", "", "pc1", PointcutType.EXECUTION);
    //            space.registerExpression("* test.ExpressionTest.get(..)", "", "pc2", PointcutType.EXECUTION);
    //            space.registerExpression("ATTR", "", "pc3", PointcutType.ATTRIBUTE);
    //
    //
    //            Expression root = space.createExpression("pc1 || (pc2 && pc3)");
    //            Expression rootAnonymous = space.createExpression("pc1 || (execution(* test.ExpressionTest.get(..)) && attribute(ATTR))");
    //
    //            ClassMetaData classMetaData1 = ReflectionMetaDataMaker.createClassMetaData(ExpressionTest.class);
    //            ClassMetaData classMetaData2 = ReflectionMetaDataMaker.createClassMetaData(ExpressionException.class);
    //            MethodMetaData methodMetaData1 = ReflectionMetaDataMaker.createMethodMetaData(
    //                    ExpressionTest.class.getDeclaredMethod("set", new Class[]{})
    //            );
    //            MethodMetaData methodMetaData2 = ReflectionMetaDataMaker.createMethodMetaData(
    //                    ExpressionTest.class.getDeclaredMethod("get", new Class[]{})
    //            );
    //            methodMetaData2.addAttribute(new CustomAttribute("ATTR", "", null));
    //            MethodMetaData methodMetaData3 = ReflectionMetaDataMaker.createMethodMetaData(
    //                    ExpressionTest.class.getDeclaredMethod("suite", new Class[]{})
    //            );
    //
    //            assertTrue(root.match(classMetaData1, PointcutType.EXECUTION));
    //            assertTrue(rootAnonymous.match(classMetaData1, PointcutType.EXECUTION));
    //
    //            assertFalse(root.match(classMetaData2, PointcutType.EXECUTION));
    //            assertFalse(rootAnonymous.match(classMetaData2, PointcutType.EXECUTION));
    //
    //            assertTrue(root.match(classMetaData1, methodMetaData1, PointcutType.EXECUTION));
    //            assertTrue(rootAnonymous.match(classMetaData1, methodMetaData1, PointcutType.EXECUTION));
    //
    //            assertTrue(root.match(classMetaData1, methodMetaData2, PointcutType.EXECUTION));
    //            assertTrue(rootAnonymous.match(classMetaData1, methodMetaData2, PointcutType.EXECUTION));
    //
    //            assertFalse(root.match(classMetaData1, methodMetaData3, PointcutType.EXECUTION));
    //            assertFalse(rootAnonymous.match(classMetaData1, methodMetaData3, PointcutType.EXECUTION));
    //        }
    //        catch (Exception e) {
    //            fail(e.toString());
    //        }
    //    }
    public void testOneLevel_CALL_OR()
    {
        try
        {
            ExpressionNamespace space = ExpressionNamespace
                .getExpressionNamespace();

            space.registerExpression("*->* test.ExpressionTest.set(..)", "",
                "pc1", PointcutType.CALL);
            space.registerExpression("*->* test.ExpressionTest.get(..)", "",
                "pc2", PointcutType.CALL);

            Expression root = space.createExpression("pc1 || pc2");
            Expression rootAnonymous = space.createExpression(
                    "pc1 || call(*->* test.ExpressionTest.get(..))");

            ClassMetaData classMetaData1 = m_metaDataMaker.createClassMetaData(ExpressionTest.class);
            MethodMetaData methodMetaData1 = ReflectionMetaDataMaker
                .createMethodMetaData(ExpressionTest.class.getDeclaredMethod(
                        "set", new Class[] {  }));
            MethodMetaData methodMetaData2 = ReflectionMetaDataMaker
                .createMethodMetaData(ExpressionTest.class.getDeclaredMethod(
                        "get", new Class[] {  }));
            MethodMetaData methodMetaData3 = ReflectionMetaDataMaker
                .createMethodMetaData(ExpressionTest.class.getDeclaredMethod(
                        "suite", new Class[] {  }));

            assertTrue(root.match(classMetaData1, PointcutType.CALL));
            assertTrue(rootAnonymous.match(classMetaData1, PointcutType.CALL));

            assertTrue(root.match(classMetaData1, methodMetaData1,
                    PointcutType.CALL));
            assertTrue(rootAnonymous.match(classMetaData1, methodMetaData1,
                    PointcutType.CALL));

            assertTrue(root.match(classMetaData1, methodMetaData2,
                    PointcutType.CALL));
            assertTrue(rootAnonymous.match(classMetaData1, methodMetaData2,
                    PointcutType.CALL));

            assertFalse(root.match(classMetaData1, methodMetaData3,
                    PointcutType.CALL));
            assertFalse(rootAnonymous.match(classMetaData1, methodMetaData3,
                    PointcutType.CALL));
        }
        catch (Exception e)
        {
            fail(e.toString());
        }
    }

    public void testOneLevel_SET_OR()
    {
        try
        {
            space.registerExpression("* test.ExpressionTest.m_name", "", "pc1",
                PointcutType.SET);
            space.registerExpression("* test.ExpressionTest.m_type", "", "pc2",
                PointcutType.SET);

            Expression root = space.createExpression("pc1 || pc2");
            Expression rootAnonymous = space.createExpression(
                    "set(* test.ExpressionTest.m_name) || pc2");

            ClassMetaData classMetaData1 = m_metaDataMaker.createClassMetaData(ExpressionTest.class);
            ClassMetaData classMetaData2 = m_metaDataMaker.createClassMetaData(ExpressionException.class);
            FieldMetaData fieldMetaData1 = ReflectionMetaDataMaker
                .createFieldMetaData(ExpressionTest.class.getDeclaredField(
                        "m_name"));
            FieldMetaData fieldMetaData2 = ReflectionMetaDataMaker
                .createFieldMetaData(ExpressionTest.class.getDeclaredField(
                        "m_type"));

            assertTrue(root.match(classMetaData1, PointcutType.SET));
            assertTrue(rootAnonymous.match(classMetaData1, PointcutType.SET));
            assertFalse(root.match(classMetaData2, PointcutType.SET));
            assertFalse(rootAnonymous.match(classMetaData2, PointcutType.SET));
            assertTrue(root.match(classMetaData1, fieldMetaData1,
                    PointcutType.SET));
            assertTrue(rootAnonymous.match(classMetaData1, fieldMetaData1,
                    PointcutType.SET));
            assertTrue(root.match(classMetaData1, fieldMetaData2,
                    PointcutType.SET));
            assertTrue(rootAnonymous.match(classMetaData1, fieldMetaData2,
                    PointcutType.SET));
        }
        catch (Exception e)
        {
            fail(e.toString());
        }
    }

    public void testOneLevel_GET_OR()
    {
        try
        {
            space.registerExpression("* test.ExpressionTest.m_name", "", "pc1",
                PointcutType.GET);
            space.registerExpression("* test.ExpressionTest.m_type", "", "pc2",
                PointcutType.GET);

            Expression root = space.createExpression("pc1 || pc2");
            Expression rootAnonymous = space.createExpression(
                    "pc1 || get(* test.ExpressionTest.m_type)");

            ClassMetaData classMetaData1 = m_metaDataMaker.createClassMetaData(ExpressionTest.class);
            ClassMetaData classMetaData2 = m_metaDataMaker.createClassMetaData(ExpressionException.class);
            FieldMetaData fieldMetaData1 = ReflectionMetaDataMaker
                .createFieldMetaData(ExpressionTest.class.getDeclaredField(
                        "m_name"));
            FieldMetaData fieldMetaData2 = ReflectionMetaDataMaker
                .createFieldMetaData(ExpressionTest.class.getDeclaredField(
                        "m_type"));

            assertTrue(root.match(classMetaData1, PointcutType.GET));
            assertTrue(rootAnonymous.match(classMetaData1, PointcutType.GET));
            assertFalse(root.match(classMetaData2, PointcutType.GET));
            assertFalse(rootAnonymous.match(classMetaData2, PointcutType.GET));
            assertTrue(root.match(classMetaData1, fieldMetaData1,
                    PointcutType.GET));
            assertTrue(rootAnonymous.match(classMetaData1, fieldMetaData1,
                    PointcutType.GET));
            assertTrue(root.match(classMetaData1, fieldMetaData2,
                    PointcutType.GET));
            assertTrue(rootAnonymous.match(classMetaData1, fieldMetaData2,
                    PointcutType.GET));
        }
        catch (Exception e)
        {
            fail(e.toString());
        }
    }

    public void testOneLevel_EXECUTION_AND()
    {
        try
        {
            space.registerExpression("* test.ExpressionTest.set(..)", "",
                "pc1", PointcutType.EXECUTION);
            space.registerExpression("* test.ExpressionTest.get(..)", "",
                "pc2", PointcutType.EXECUTION);

            Expression root = space.createExpression("!pc1 && pc2");

            ClassMetaData classMetaData1 = m_metaDataMaker.createClassMetaData(ExpressionTest.class);
            ClassMetaData classMetaData2 = m_metaDataMaker.createClassMetaData(ExpressionException.class);
            MethodMetaData methodMetaData1 = ReflectionMetaDataMaker
                .createMethodMetaData(ExpressionTest.class.getDeclaredMethod(
                        "set", new Class[] {  }));
            MethodMetaData methodMetaData2 = ReflectionMetaDataMaker
                .createMethodMetaData(ExpressionTest.class.getDeclaredMethod(
                        "get", new Class[] {  }));
            MethodMetaData methodMetaData3 = ReflectionMetaDataMaker
                .createMethodMetaData(ExpressionTest.class.getDeclaredMethod(
                        "suite", new Class[] {  }));

            assertTrue(root.match(classMetaData1, PointcutType.EXECUTION));
            assertFalse(root.match(classMetaData1, methodMetaData1,
                    PointcutType.EXECUTION));
            assertFalse(root.match(classMetaData2, PointcutType.EXECUTION));
            assertTrue(root.match(classMetaData1, methodMetaData2,
                    PointcutType.EXECUTION));
            assertFalse(root.match(classMetaData1, methodMetaData3,
                    PointcutType.EXECUTION));
        }
        catch (Exception e)
        {
            fail(e.toString());
        }
    }

    public void testOneLevel_CALL_AND()
    {
        try
        {
            space.registerExpression("* test.ExpressionTest.set(..)", "",
                "pc1", PointcutType.CALL);
            space.registerExpression("* test.ExpressionTest.get(..)", "",
                "pc2", PointcutType.CALL);

            Expression root = space.createExpression("!pc1 && pc2");

            ClassMetaData classMetaData1 = m_metaDataMaker.createClassMetaData(ExpressionTest.class);
            MethodMetaData methodMetaData1 = m_metaDataMaker
                .createMethodMetaData(ExpressionTest.class.getDeclaredMethod(
                        "set", new Class[] {  }));
            MethodMetaData methodMetaData2 = m_metaDataMaker
                .createMethodMetaData(ExpressionTest.class.getDeclaredMethod(
                        "get", new Class[] {  }));
            MethodMetaData methodMetaData3 = m_metaDataMaker
                .createMethodMetaData(ExpressionTest.class.getDeclaredMethod(
                        "suite", new Class[] {  }));

            assertTrue(root.match(classMetaData1, PointcutType.CALL));
            assertFalse(root.match(classMetaData1, methodMetaData1,
                    PointcutType.CALL));
            assertTrue(root.match(classMetaData1, methodMetaData2,
                    PointcutType.CALL));
            assertFalse(root.match(classMetaData1, methodMetaData3,
                    PointcutType.CALL));
        }
        catch (Exception e)
        {
            fail(e.toString());
        }
    }

    public void testOneLevel_SET_AND()
    {
        try
        {
            space.registerExpression("* test.ExpressionTest.m_name", "", "pc1",
                PointcutType.SET);
            space.registerExpression("* test.ExpressionTest.m_type", "", "pc2",
                PointcutType.SET);

            Expression root = space.createExpression("!pc1 && pc2");

            ClassMetaData classMetaData1 = m_metaDataMaker.createClassMetaData(ExpressionTest.class);
            ClassMetaData classMetaData2 = m_metaDataMaker.createClassMetaData(ExpressionException.class);
            FieldMetaData fieldMetaData1 = m_metaDataMaker.createFieldMetaData(ExpressionTest.class
                    .getDeclaredField("m_name"));
            FieldMetaData fieldMetaData2 = m_metaDataMaker.createFieldMetaData(ExpressionTest.class
                    .getDeclaredField("m_type"));

            assertTrue(root.match(classMetaData1, PointcutType.SET));
            assertFalse(root.match(classMetaData2, PointcutType.SET));
            assertFalse(root.match(classMetaData1, fieldMetaData1,
                    PointcutType.SET));
            assertTrue(root.match(classMetaData1, fieldMetaData2,
                    PointcutType.SET));
        }
        catch (Exception e)
        {
            fail(e.toString());
        }
    }

    public void testOneLevel_GET_AND()
    {
        try
        {
            space.registerExpression("* test.ExpressionTest.m_name", "", "pc1",
                PointcutType.GET);
            space.registerExpression("* test.ExpressionTest.m_type", "", "pc2",
                PointcutType.GET);

            Expression root = space.createExpression("!pc1 && pc2");

            ClassMetaData classMetaData1 = m_metaDataMaker.createClassMetaData(ExpressionTest.class);
            ClassMetaData classMetaData2 = m_metaDataMaker.createClassMetaData(ExpressionException.class);
            FieldMetaData fieldMetaData1 = m_metaDataMaker.createFieldMetaData(ExpressionTest.class
                    .getDeclaredField("m_name"));
            FieldMetaData fieldMetaData2 = m_metaDataMaker.createFieldMetaData(ExpressionTest.class
                    .getDeclaredField("m_type"));

            assertTrue(root.match(classMetaData1, PointcutType.GET));
            assertFalse(root.match(classMetaData2, PointcutType.GET));
            assertFalse(root.match(classMetaData1, fieldMetaData1));
            assertTrue(root.match(classMetaData1, fieldMetaData2));
        }
        catch (Exception e)
        {
            fail(e.toString());
        }
    }

    public void testOneLevel_CFLOW_AND_EXECUTION()
    {
        try
        {
            space.registerExpression("* test.ExpressionTest.get(..)", "",
                "pc1", PointcutType.EXECUTION);
            space.registerExpression("* test.ExpressionTest.set(..)", "",
                "cf1", PointcutType.CFLOW);

            Expression root = space.createExpression("pc1 AND cf1");
            Expression rootAnonymous = space.createExpression(
                    "pc1 AND cflow(* test.ExpressionTest.set(..))");

            ClassMetaData classMetaData1 = m_metaDataMaker.createClassMetaData(ExpressionTest.class);
            ClassMetaData classMetaData2 = m_metaDataMaker.createClassMetaData(ExpressionException.class);
            MethodMetaData methodMetaData1 = m_metaDataMaker
                .createMethodMetaData(ExpressionTest.class.getDeclaredMethod(
                        "set", new Class[] {  }));
            MethodMetaData methodMetaData2 = m_metaDataMaker
                .createMethodMetaData(ExpressionTest.class.getDeclaredMethod(
                        "get", new Class[] {  }));
            MethodMetaData methodMetaData3 = m_metaDataMaker
                .createMethodMetaData(ExpressionTest.class.getDeclaredMethod(
                        "suite", new Class[] {  }));

            assertTrue(root.match(classMetaData1));
            assertTrue(rootAnonymous.match(classMetaData1));
            assertFalse(root.match(classMetaData1, methodMetaData1));
            assertFalse(rootAnonymous.match(classMetaData1, methodMetaData1));
            assertFalse(root.match(classMetaData2));
            assertFalse(rootAnonymous.match(classMetaData2));
            assertTrue(root.match(classMetaData1, methodMetaData2));
            assertTrue(rootAnonymous.match(classMetaData1, methodMetaData2));
            assertFalse(root.match(classMetaData1, methodMetaData3));
            assertFalse(rootAnonymous.match(classMetaData1, methodMetaData3));
        }
        catch (Exception e)
        {
            fail(e.toString());
        }
    }

    public void testOneLevel_CFLOW_AND_CALL()
    {
        try
        {
            space.registerExpression("* test.ExpressionTest.get(..)", "",
                "pc1", PointcutType.CALL);
            space.registerExpression("* test.ExpressionTest.set(..)", "",
                "cf1", PointcutType.CFLOW);

            Expression root = space.createExpression("pc1 AND cf1");

            ClassMetaData classMetaData1 = m_metaDataMaker.createClassMetaData(ExpressionTest.class);
            MethodMetaData methodMetaData1 = m_metaDataMaker
                .createMethodMetaData(ExpressionTest.class.getDeclaredMethod(
                        "set", new Class[] {  }));
            MethodMetaData methodMetaData2 = m_metaDataMaker
                .createMethodMetaData(ExpressionTest.class.getDeclaredMethod(
                        "get", new Class[] {  }));
            MethodMetaData methodMetaData3 = m_metaDataMaker
                .createMethodMetaData(ExpressionTest.class.getDeclaredMethod(
                        "suite", new Class[] {  }));

            assertTrue(root.match(classMetaData1));
            assertFalse(root.match(classMetaData1, methodMetaData1));
            assertTrue(root.match(classMetaData1, methodMetaData2));
            assertFalse(root.match(classMetaData1, methodMetaData3));
        }
        catch (Exception e)
        {
            fail(e.toString());
        }
    }

    public void testTwoLevels_EXECUTION()
    {
        try
        {
            space.registerExpression("* test.ExpressionTest.set(..)", "",
                "pc1", PointcutType.EXECUTION);
            space.registerExpression("* test.ExpressionTest.get(..)", "",
                "pc2", PointcutType.EXECUTION);
            space.registerExpression(space.createExpression("pc1 || pc2", "pc3"));
            space.registerExpression("* test.ExpressionTest.suite(..)", "",
                "pc4", PointcutType.EXECUTION);

            Expression root = space.createExpression("pc3 && !pc4");

            ClassMetaData classMetaData1 = m_metaDataMaker.createClassMetaData(ExpressionTest.class);
            ClassMetaData classMetaData2 = m_metaDataMaker.createClassMetaData(ExpressionException.class);
            MethodMetaData methodMetaData1 = m_metaDataMaker
                .createMethodMetaData(ExpressionTest.class.getDeclaredMethod(
                        "set", new Class[] {  }));
            MethodMetaData methodMetaData2 = m_metaDataMaker
                .createMethodMetaData(ExpressionTest.class.getDeclaredMethod(
                        "get", new Class[] {  }));
            MethodMetaData methodMetaData3 = m_metaDataMaker
                .createMethodMetaData(ExpressionTest.class.getDeclaredMethod(
                        "suite", new Class[] {  }));

            assertTrue(root.match(classMetaData1));
            assertTrue(root.match(classMetaData1, methodMetaData1));
            assertFalse(root.match(classMetaData2));
            assertTrue(root.match(classMetaData1, methodMetaData2));
            assertFalse(root.match(classMetaData1, methodMetaData3));
        }
        catch (Exception e)
        {
            fail(e.toString());
        }
    }

    public void testTwoLevels_CALL()
    {
        try
        {
            space.registerExpression("* test.ExpressionTest.set(..)", "",
                "pc1", PointcutType.CALL);
            space.registerExpression("* test.ExpressionTest.get(..)", "",
                "pc2", PointcutType.CALL);
            space.registerExpression(space.createExpression("pc1 || pc2", "pc3"));
            space.registerExpression("* test.ExpressionTest.suite(..)", "",
                "pc4", PointcutType.CALL);

            Expression root = space.createExpression("pc3 && !pc4");

            ClassMetaData classMetaData1 = m_metaDataMaker.createClassMetaData(ExpressionTest.class);
            MethodMetaData methodMetaData1 = m_metaDataMaker
                .createMethodMetaData(ExpressionTest.class.getDeclaredMethod(
                        "set", new Class[] {  }));
            MethodMetaData methodMetaData2 = m_metaDataMaker
                .createMethodMetaData(ExpressionTest.class.getDeclaredMethod(
                        "get", new Class[] {  }));
            MethodMetaData methodMetaData3 = m_metaDataMaker
                .createMethodMetaData(ExpressionTest.class.getDeclaredMethod(
                        "suite", new Class[] {  }));

            assertTrue(root.match(classMetaData1));
            assertTrue(root.match(classMetaData1, methodMetaData1));
            assertTrue(root.match(classMetaData1, methodMetaData2));
            assertFalse(root.match(classMetaData1, methodMetaData3));
        }
        catch (Exception e)
        {
            fail(e.toString());
        }
    }

    public void testTwoLevels_SET()
    {
        try
        {
            space.registerExpression("* test.ExpressionTest.m_name", "", "pc1",
                PointcutType.SET);
            space.registerExpression("* test.ExpressionTest.m_type", "", "pc2",
                PointcutType.SET);
            space.registerExpression(space.createExpression("pc1 || pc2", "pc3"));
            space.registerExpression("* test.ExpressionTest.m_dummy", "",
                "pc4", PointcutType.SET);

            Expression root = space.createExpression("pc3 && !pc4");

            ClassMetaData classMetaData1 = m_metaDataMaker.createClassMetaData(ExpressionTest.class);
            FieldMetaData fieldMetaData1 = m_metaDataMaker.createFieldMetaData(ExpressionTest.class
                    .getDeclaredField("m_name"));
            FieldMetaData fieldMetaData2 = m_metaDataMaker.createFieldMetaData(ExpressionTest.class
                    .getDeclaredField("m_type"));
            FieldMetaData fieldMetaData3 = m_metaDataMaker.createFieldMetaData(ExpressionTest.class
                    .getDeclaredField("m_dummy"));

            assertTrue(root.match(classMetaData1));
            assertTrue(root.match(classMetaData1, fieldMetaData1));
            assertTrue(root.match(classMetaData1, fieldMetaData2));
            assertFalse(root.match(classMetaData1, fieldMetaData3));
        }
        catch (Exception e)
        {
            fail(e.toString());
        }
    }

    public void testTwoLevels_GET()
    {
        try
        {
            space.registerExpression("* test.ExpressionTest.m_name", "", "pc1",
                PointcutType.GET);
            space.registerExpression("* test.ExpressionTest.m_type", "", "pc2",
                PointcutType.GET);
            space.registerExpression(space.createExpression("pc1 || pc2", "pc3"));
            space.registerExpression("* test.ExpressionTest.m_dummy", "",
                "pc4", PointcutType.GET);

            Expression root = space.createExpression("pc3 && !pc4");

            ClassMetaData classMetaData1 = m_metaDataMaker.createClassMetaData(ExpressionTest.class);
            FieldMetaData fieldMetaData1 = m_metaDataMaker.createFieldMetaData(ExpressionTest.class
                    .getDeclaredField("m_name"));
            FieldMetaData fieldMetaData2 = m_metaDataMaker.createFieldMetaData(ExpressionTest.class
                    .getDeclaredField("m_type"));
            FieldMetaData fieldMetaData3 = m_metaDataMaker.createFieldMetaData(ExpressionTest.class
                    .getDeclaredField("m_dummy"));

            assertTrue(root.match(classMetaData1));
            assertTrue(root.match(classMetaData1, fieldMetaData1));
            assertTrue(root.match(classMetaData1, fieldMetaData2));
            assertFalse(root.match(classMetaData1, fieldMetaData3));
        }
        catch (Exception e)
        {
            fail(e.toString());
        }
    }

    public void testComplexPattern_EXECUTION_1()
    {
        try
        {
            space.registerExpression("* test.ExpressionTest.set(..)", "",
                "pc1", PointcutType.EXECUTION);
            space.registerExpression("* test.ExpressionTest.get(..)", "",
                "pc2", PointcutType.EXECUTION);
            space.registerExpression("* test.ExpressionTest.suite(..)", "",
                "pc3", PointcutType.EXECUTION);

            Expression root = space.createExpression("!pc3 && (pc1 || pc2)");

            ClassMetaData classMetaData1 = m_metaDataMaker.createClassMetaData(ExpressionTest.class);
            MethodMetaData methodMetaData1 = m_metaDataMaker
                .createMethodMetaData(ExpressionTest.class.getDeclaredMethod(
                        "set", new Class[] {  }));
            MethodMetaData methodMetaData2 = m_metaDataMaker
                .createMethodMetaData(ExpressionTest.class.getDeclaredMethod(
                        "get", new Class[] {  }));
            MethodMetaData methodMetaData3 = m_metaDataMaker
                .createMethodMetaData(ExpressionTest.class.getDeclaredMethod(
                        "suite", new Class[] {  }));

            assertTrue(root.match(classMetaData1));
            assertTrue(root.match(classMetaData1, methodMetaData1));
            assertTrue(root.match(classMetaData1, methodMetaData2));
            assertFalse(root.match(classMetaData1, methodMetaData3));
        }
        catch (Exception e)
        {
            fail(e.toString());
        }
    }

    public void testComplexPattern_EXECUTION_2()
    {
        try
        {
            space.registerExpression("* test.ExpressionTest.set(..)", "",
                "pc1", PointcutType.EXECUTION);
            space.registerExpression("* test.ExpressionTest.get(..)", "",
                "pc2", PointcutType.EXECUTION);
            space.registerExpression("* test.ExpressionTest.suite(..)", "",
                "pc3", PointcutType.EXECUTION);

            Expression root = space.createExpression("pc1 && !(pc2 || pc3)");

            ClassMetaData classMetaData1 = m_metaDataMaker.createClassMetaData(ExpressionTest.class);
            MethodMetaData methodMetaData1 = m_metaDataMaker
                .createMethodMetaData(ExpressionTest.class.getDeclaredMethod(
                        "set", new Class[] {  }));
            MethodMetaData methodMetaData2 = m_metaDataMaker
                .createMethodMetaData(ExpressionTest.class.getDeclaredMethod(
                        "get", new Class[] {  }));
            MethodMetaData methodMetaData3 = m_metaDataMaker
                .createMethodMetaData(ExpressionTest.class.getDeclaredMethod(
                        "suite", new Class[] {  }));

            assertTrue(root.match(classMetaData1));
            assertTrue(root.match(classMetaData1, methodMetaData1));
            assertFalse(root.match(classMetaData1, methodMetaData2));
            assertFalse(root.match(classMetaData1, methodMetaData3));
        }
        catch (Exception e)
        {
            fail(e.toString());
        }
    }

    public void testComplexPattern_CALL_1()
    {
        try
        {
            space.registerExpression("* test.ExpressionTest.set(..)", "",
                "pc1", PointcutType.CALL);
            space.registerExpression("* test.ExpressionTest.get(..)", "",
                "pc2", PointcutType.CALL);
            space.registerExpression("* test.ExpressionTest.suite(..)", "",
                "pc3", PointcutType.CALL);

            Expression root = space.createExpression("!pc3 && (pc1 || pc2)");

            ClassMetaData classMetaData1 = m_metaDataMaker.createClassMetaData(ExpressionTest.class);
            MethodMetaData methodMetaData1 = m_metaDataMaker
                .createMethodMetaData(ExpressionTest.class.getDeclaredMethod(
                        "set", new Class[] {  }));
            MethodMetaData methodMetaData2 = m_metaDataMaker
                .createMethodMetaData(ExpressionTest.class.getDeclaredMethod(
                        "get", new Class[] {  }));
            MethodMetaData methodMetaData3 = m_metaDataMaker
                .createMethodMetaData(ExpressionTest.class.getDeclaredMethod(
                        "suite", new Class[] {  }));

            assertTrue(root.match(classMetaData1));
            assertTrue(root.match(classMetaData1, methodMetaData1));
            assertTrue(root.match(classMetaData1, methodMetaData2));
            assertFalse(root.match(classMetaData1, methodMetaData3));
        }
        catch (Exception e)
        {
            fail(e.toString());
        }
    }

    public void testComplexPattern_CALL_2()
    {
        try
        {
            space.registerExpression("* test.ExpressionTest.set(..)", "",
                "pc1", PointcutType.CALL);
            space.registerExpression("* test.ExpressionTest.get(..)", "",
                "pc2", PointcutType.CALL);
            space.registerExpression("* test.ExpressionTest.suite(..)", "",
                "pc3", PointcutType.CALL);

            Expression root = space.createExpression("pc1 && !(pc2 || pc3)");

            ClassMetaData classMetaData1 = m_metaDataMaker.createClassMetaData(ExpressionTest.class);
            MethodMetaData methodMetaData1 = m_metaDataMaker
                .createMethodMetaData(ExpressionTest.class.getDeclaredMethod(
                        "set", new Class[] {  }));
            MethodMetaData methodMetaData2 = m_metaDataMaker
                .createMethodMetaData(ExpressionTest.class.getDeclaredMethod(
                        "get", new Class[] {  }));
            MethodMetaData methodMetaData3 = m_metaDataMaker
                .createMethodMetaData(ExpressionTest.class.getDeclaredMethod(
                        "suite", new Class[] {  }));

            assertTrue(root.match(classMetaData1));
            assertTrue(root.match(classMetaData1, methodMetaData1));
            assertFalse(root.match(classMetaData1, methodMetaData2));
            assertFalse(root.match(classMetaData1, methodMetaData3));
        }
        catch (Exception e)
        {
            fail(e.toString());
        }
    }

    public void testMatchHierachicalExpression_EXECUTION()
    {
        try
        {
            Expression root = space.createExpression("* *..TestCase+.set(..)",
                    PointcutType.EXECUTION);
            ClassMetaData classMetaData1 = m_metaDataMaker.createClassMetaData(ExpressionTest.class);
            ClassMetaData classMetaData2 = m_metaDataMaker.createClassMetaData(ExpressionException.class);
            MethodMetaData methodMetaData1 = m_metaDataMaker
                .createMethodMetaData(ExpressionTest.class.getDeclaredMethod(
                        "set", new Class[] {  }));
            MethodMetaData methodMetaData2 = m_metaDataMaker
                .createMethodMetaData(ExpressionTest.class.getDeclaredMethod(
                        "get", new Class[] {  }));

            assertTrue(root.match(classMetaData1));
            assertTrue(root.match(classMetaData1, methodMetaData1));
            assertFalse(root.match(classMetaData2));
            assertFalse(root.match(classMetaData1, methodMetaData2));
        }
        catch (Exception e)
        {
            fail(e.toString());
        }
    }

    public void testMatchCallerHierachicalExpression_CALL()
    {
        try
        {
            Expression root = space.createExpression("java.lang.Object+->* *..ExpressionTest.suite(..)",
                    PointcutType.CALL);
            ClassMetaData classMetaData1 = m_metaDataMaker.createClassMetaData(ExpressionTest.class);
            MethodMetaData methodMetaData1 = m_metaDataMaker
                .createMethodMetaData(ExpressionTest.class.getDeclaredMethod(
                        "set", new Class[] {  }));
            MethodMetaData methodMetaData2 = m_metaDataMaker
                .createMethodMetaData(ExpressionTest.class.getDeclaredMethod(
                        "get", new Class[] {  }));
            MethodMetaData methodMetaData3 = m_metaDataMaker
                .createMethodMetaData(ExpressionTest.class.getDeclaredMethod(
                        "suite", new Class[] {  }));

            assertTrue(root.match(classMetaData1));
            assertFalse(root.match(classMetaData1, methodMetaData1));
            assertFalse(root.match(classMetaData1, methodMetaData2));
            assertTrue(root.match(classMetaData1, methodMetaData3));
        }
        catch (Exception e)
        {
            fail(e.toString());
        }
    }

    public void testMatchCalleeHierachicalExpression_CALL()
    {
        try
        {
            Expression root = space.createExpression("*->* *..TestCase+.suite(..)",
                    PointcutType.CALL);
            ClassMetaData classMetaData1 = m_metaDataMaker.createClassMetaData(ExpressionTest.class);
            MethodMetaData methodMetaData1 = m_metaDataMaker
                .createMethodMetaData(ExpressionTest.class.getDeclaredMethod(
                        "set", new Class[] {  }));
            MethodMetaData methodMetaData2 = m_metaDataMaker
                .createMethodMetaData(ExpressionTest.class.getDeclaredMethod(
                        "get", new Class[] {  }));
            MethodMetaData methodMetaData3 = m_metaDataMaker
                .createMethodMetaData(ExpressionTest.class.getDeclaredMethod(
                        "suite", new Class[] {  }));

            assertTrue(root.match(classMetaData1));
            assertFalse(root.match(classMetaData1, methodMetaData1));
            assertFalse(root.match(classMetaData1, methodMetaData2));
            assertTrue(root.match(classMetaData1, methodMetaData3));
        }
        catch (Exception e)
        {
            fail(e.toString());
        }
    }

    public void testMatchDoubleHierachicalExpression_CALL()
    {
        try
        {
            Expression root = space.createExpression("java.lang.Object+->* *..TestCase+.suite(..)",
                    PointcutType.CALL);
            ClassMetaData classMetaData1 = m_metaDataMaker.createClassMetaData(ExpressionTest.class);
            MethodMetaData methodMetaData1 = m_metaDataMaker
                .createMethodMetaData(ExpressionTest.class.getDeclaredMethod(
                        "set", new Class[] {  }));
            MethodMetaData methodMetaData2 = m_metaDataMaker
                .createMethodMetaData(ExpressionTest.class.getDeclaredMethod(
                        "get", new Class[] {  }));
            MethodMetaData methodMetaData3 = m_metaDataMaker
                .createMethodMetaData(ExpressionTest.class.getDeclaredMethod(
                        "suite", new Class[] {  }));

            assertTrue(root.match(classMetaData1));
            assertFalse(root.match(classMetaData1, methodMetaData1));
            assertFalse(root.match(classMetaData1, methodMetaData2));
            assertTrue(root.match(classMetaData1, methodMetaData3));
        }
        catch (Exception e)
        {
            fail(e.toString());
        }
    }

    public void testMatchHierachicalExpression_SET()
    {
        try
        {
            Expression root = space.createExpression("* *..TestCase+.m_dummy",
                    PointcutType.SET);
            ClassMetaData classMetaData1 = m_metaDataMaker.createClassMetaData(ExpressionTest.class);
            FieldMetaData fieldMetaData1 = m_metaDataMaker.createFieldMetaData(ExpressionTest.class
                    .getDeclaredField("m_name"));
            FieldMetaData fieldMetaData2 = m_metaDataMaker.createFieldMetaData(ExpressionTest.class
                    .getDeclaredField("m_type"));
            FieldMetaData fieldMetaData3 = m_metaDataMaker.createFieldMetaData(ExpressionTest.class
                    .getDeclaredField("m_dummy"));

            assertTrue(root.match(classMetaData1));
            assertFalse(root.match(classMetaData1, fieldMetaData1));
            assertFalse(root.match(classMetaData1, fieldMetaData2));
            assertTrue(root.match(classMetaData1, fieldMetaData3));
        }
        catch (Exception e)
        {
            fail(e.toString());
        }
    }

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite()
    {
        return new junit.framework.TestSuite(ExpressionTest.class);
    }

    // === methods below are needed for the test even though they are empty and not "used" anywhere ===
    public void set()
    {
    }

    public void get()
    {
    }

    public void throws1() throws Exception
    {
    }

    public void throws2() throws Exception
    {
    }

    public void throwsDummy()
        throws Exception
    {
    }

    public void throwsError()
        throws Error
    {
    }
}
