/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test;

import junit.framework.TestCase;

import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.metadata.ReflectionMetaDataMaker;
import org.codehaus.aspectwerkz.metadata.MethodMetaData;
import org.codehaus.aspectwerkz.metadata.FieldMetaData;
import org.codehaus.aspectwerkz.definition.expression.Expression;
import org.codehaus.aspectwerkz.definition.expression.PointcutType;
import org.codehaus.aspectwerkz.exception.ExpressionException;

/**
 * @todo document
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class ExpressionTest extends TestCase {

    public void testBuildSingleAnonymousExpression() {
        try {
            Expression root = Expression.createRootExpression("namespace", "* test.ExpressionTest.set(..)", PointcutType.EXECUTION);
        }
        catch (Exception e) {
            fail(e.toString());
        }
    }

    public void testBuildSingleAnonymousHierarchicalExpression() {
        try {
            Expression root = Expression.createRootExpression("namespace", "* foo.bar.Baz+.set(..)", PointcutType.EXECUTION);
        }
        catch (Exception e) {
            fail(e.toString());
        }
    }

    public void testBuildExpressionWithTheSamePointcutTypes() {
        try {
            Expression.registerExpressionTemplate(Expression.createExpressionTemplate("namespace", "* test.ExpressionTest.set(..)", "", "pc1", PointcutType.EXECUTION));
            Expression.registerExpressionTemplate(Expression.createExpressionTemplate("namespace", "* test.ExpressionTest.get(..)", "", "pc2", PointcutType.EXECUTION));
            Expression root = Expression.createRootExpression("namespace", "pc1&&pc2");
        }
        catch (Exception e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    public void testBuildExpressionWithDifferentPointcutTypes() {
        try {
            Expression.registerExpressionTemplate(Expression.createExpressionTemplate("namespace", "* test.ExpressionTest.set(..)", "", "pc1", PointcutType.EXECUTION));
            Expression.registerExpressionTemplate(Expression.createExpressionTemplate("namespace", "* test.ExpressionTest.get(..)", "", "pc2", PointcutType.CALL));
            Expression root = Expression.createRootExpression("namespace", "pc1 && pc2");
            fail("expected exception");
        }
        catch (Exception e) {
        }
    }

    public void testBuildExpressionWithDifferentPointcutTypesButOneIsCflow() {
        try {
            Expression.registerExpressionTemplate(Expression.createExpressionTemplate("namespace", "* test.ExpressionTest.set(..)", "", "pc1", PointcutType.EXECUTION));
            Expression.registerExpressionTemplate(Expression.createExpressionTemplate("namespace", "* test.ExpressionTest.get(..)", "", "pc2", PointcutType.CFLOW));
            Expression root = Expression.createRootExpression("namespace", "pc1 && pc2");
        }
        catch (Exception e) {
            fail(e.toString());
        }
    }

    public void testBuildExpressionWithWrongPointcutTypeInContext() {
        try {
            Expression.registerExpressionTemplate(Expression.createExpressionTemplate("namespace", "* test.ExpressionTest.set(..)", "", "pc1", PointcutType.EXECUTION));
            Expression.registerExpressionTemplate(Expression.createExpressionTemplate("namespace", "* test.ExpressionTest.get(..)", "", "pc2", PointcutType.CALL));
            Expression root = Expression.createRootExpression("namespace", "pc1 && pc2");
            fail("expected exception");
        }
        catch (Exception e) {
        }
    }

    public void testMatchSingleAnonymousExpression() {
        try {
            Expression root = Expression.createRootExpression("namespace", "* test.ExpressionTest.set(..)", PointcutType.EXECUTION);

            ClassMetaData classMetaDataTrue = ReflectionMetaDataMaker.createClassMetaData(ExpressionTest.class);
            ClassMetaData classMetaDataFalse = ReflectionMetaDataMaker.createClassMetaData(ExpressionException.class);
            MethodMetaData methodMetaDataTrue = ReflectionMetaDataMaker.createMethodMetaData(ExpressionTest.class.getDeclaredMethod("set", new Class[]{}));
            MethodMetaData methodMetaDataFalse = ReflectionMetaDataMaker.createMethodMetaData(ExpressionTest.class.getDeclaredMethod("get", new Class[]{}));

            assertTrue(root.match(classMetaDataTrue));
            assertTrue(root.match(classMetaDataTrue, methodMetaDataTrue));
            assertFalse(root.match(classMetaDataFalse));
            assertFalse(root.match(classMetaDataTrue, methodMetaDataFalse));
        }
        catch (Exception e) {
            fail(e.toString());
        }
    }

    public void testOneLevel_EXECUTION_OR() {
        try {
            Expression.registerExpressionTemplate(Expression.createExpressionTemplate("namespace", "* test.ExpressionTest.set(..)", "", "pc1", PointcutType.EXECUTION));
            Expression.registerExpressionTemplate(Expression.createExpressionTemplate("namespace", "* test.ExpressionTest.get(..)", "", "pc2", PointcutType.EXECUTION));
            Expression root = Expression.createRootExpression("namespace", "pc1 || pc2");

            ClassMetaData classMetaData1 = ReflectionMetaDataMaker.createClassMetaData(ExpressionTest.class);
            ClassMetaData classMetaData2 = ReflectionMetaDataMaker.createClassMetaData(ExpressionException.class);
            MethodMetaData methodMetaData1 = ReflectionMetaDataMaker.createMethodMetaData(ExpressionTest.class.getDeclaredMethod("set", new Class[]{}));
            MethodMetaData methodMetaData2 = ReflectionMetaDataMaker.createMethodMetaData(ExpressionTest.class.getDeclaredMethod("get", new Class[]{}));
            MethodMetaData methodMetaData3 = ReflectionMetaDataMaker.createMethodMetaData(ExpressionTest.class.getDeclaredMethod("suite", new Class[]{}));

            assertTrue(root.match(classMetaData1));
            assertFalse(root.match(classMetaData2));
            assertTrue(root.match(classMetaData1, methodMetaData1));
            assertTrue(root.match(classMetaData1, methodMetaData2));
            assertFalse(root.match(classMetaData1, methodMetaData3));
        }
        catch (Exception e) {
            fail(e.toString());
        }
    }

    public void testOneLevel_CALL_OR() {
        try {
            Expression.registerExpressionTemplate(Expression.createExpressionTemplate("namespace", "*->* test.ExpressionTest.set(..)", "", "pc1", PointcutType.CALL));
            Expression.registerExpressionTemplate(Expression.createExpressionTemplate("namespace", "*->* test.ExpressionTest.get(..)", "", "pc2", PointcutType.CALL));
            Expression root = Expression.createRootExpression("namespace", "pc1 || pc2");

            ClassMetaData classMetaData1 = ReflectionMetaDataMaker.createClassMetaData(ExpressionTest.class);
            MethodMetaData methodMetaData1 = ReflectionMetaDataMaker.createMethodMetaData(ExpressionTest.class.getDeclaredMethod("set", new Class[]{}));
            MethodMetaData methodMetaData2 = ReflectionMetaDataMaker.createMethodMetaData(ExpressionTest.class.getDeclaredMethod("get", new Class[]{}));
            MethodMetaData methodMetaData3 = ReflectionMetaDataMaker.createMethodMetaData(ExpressionTest.class.getDeclaredMethod("suite", new Class[]{}));

            assertTrue(root.match(classMetaData1));
            assertTrue(root.match(classMetaData1, methodMetaData1));
            assertTrue(root.match(classMetaData1, methodMetaData2));
            assertFalse(root.match(classMetaData1, methodMetaData3));
        }
        catch (Exception e) {
            fail(e.toString());
        }
    }

    public void testOneLevel_SET_OR() {
        try {
            Expression.registerExpressionTemplate(Expression.createExpressionTemplate("namespace", "* test.ExpressionTest.m_name", "", "pc1", PointcutType.SET));
            Expression.registerExpressionTemplate(Expression.createExpressionTemplate("namespace", "* test.ExpressionTest.m_type", "", "pc2", PointcutType.SET));
            Expression root = Expression.createRootExpression("namespace", "pc1 || pc2");

            ClassMetaData classMetaData1 = ReflectionMetaDataMaker.createClassMetaData(ExpressionTest.class);
            ClassMetaData classMetaData2 = ReflectionMetaDataMaker.createClassMetaData(ExpressionException.class);
            FieldMetaData fieldMetaData1 = ReflectionMetaDataMaker.createFieldMetaData(ExpressionTest.class.getDeclaredField("m_name"));
            FieldMetaData fieldMetaData2 = ReflectionMetaDataMaker.createFieldMetaData(ExpressionTest.class.getDeclaredField("m_type"));

            assertTrue(root.match(classMetaData1));
            assertFalse(root.match(classMetaData2));
            assertTrue(root.match(classMetaData1, fieldMetaData1));
            assertTrue(root.match(classMetaData1, fieldMetaData2));
        }
        catch (Exception e) {
            fail(e.toString());
        }
    }

    public void testOneLevel_GET_OR() {
        try {
            Expression.registerExpressionTemplate(Expression.createExpressionTemplate("namespace", "* test.ExpressionTest.m_name", "", "pc1", PointcutType.GET));
            Expression.registerExpressionTemplate(Expression.createExpressionTemplate("namespace", "* test.ExpressionTest.m_type", "", "pc2", PointcutType.GET));
            Expression root = Expression.createRootExpression("namespace", "pc1 || pc2");

            ClassMetaData classMetaData1 = ReflectionMetaDataMaker.createClassMetaData(ExpressionTest.class);
            ClassMetaData classMetaData2 = ReflectionMetaDataMaker.createClassMetaData(ExpressionException.class);
            FieldMetaData fieldMetaData1 = ReflectionMetaDataMaker.createFieldMetaData(ExpressionTest.class.getDeclaredField("m_name"));
            FieldMetaData fieldMetaData2 = ReflectionMetaDataMaker.createFieldMetaData(ExpressionTest.class.getDeclaredField("m_type"));

            assertTrue(root.match(classMetaData1));
            assertFalse(root.match(classMetaData2));
            assertTrue(root.match(classMetaData1, fieldMetaData1));
            assertTrue(root.match(classMetaData1, fieldMetaData2));
        }
        catch (Exception e) {
            fail(e.toString());
        }
    }

    public void testOneLevel_THROWS_OR() {
        try {
            Expression.registerExpressionTemplate(Expression.createExpressionTemplate("namespace", "* test.ExpressionTest.throws1(..)#java.lang.Exception", "", "pc1", PointcutType.THROWS));
            Expression.registerExpressionTemplate(Expression.createExpressionTemplate("namespace", "* test.ExpressionTest.throws2(..)#java.lang.Exception", "", "pc2", PointcutType.THROWS));
            Expression root = Expression.createRootExpression("namespace", "pc1 || pc2");

            ClassMetaData classMetaData1 = ReflectionMetaDataMaker.createClassMetaData(ExpressionTest.class);
            ClassMetaData classMetaData2 = ReflectionMetaDataMaker.createClassMetaData(ExpressionException.class);
            MethodMetaData methodMetaData1 = ReflectionMetaDataMaker.createMethodMetaData(ExpressionTest.class.getDeclaredMethod("throws1", new Class[]{}));
            MethodMetaData methodMetaData2 = ReflectionMetaDataMaker.createMethodMetaData(ExpressionTest.class.getDeclaredMethod("throws2", new Class[]{}));

            assertTrue(root.match(classMetaData1));
            assertFalse(root.match(classMetaData2));
            assertTrue(root.match(classMetaData1, methodMetaData1));
            assertTrue(root.match(classMetaData1, methodMetaData2));
        }
        catch (Exception e) {
            fail(e.toString());
        }
    }

    public void testOneLevel_THROWS_OR_matchDifferentException() {
        try {
            Expression.registerExpressionTemplate(Expression.createExpressionTemplate("namespace", "* test.ExpressionTest.throws1(..)#java.lang.Exception", "", "pc1", PointcutType.THROWS));
            Expression.registerExpressionTemplate(Expression.createExpressionTemplate("namespace", "* test.ExpressionTest.throwsError(..)#java.lang.Error", "", "pc2", PointcutType.THROWS));
            Expression root = Expression.createRootExpression("namespace", "pc1 || pc2");

            ClassMetaData classMetaData1 = ReflectionMetaDataMaker.createClassMetaData(ExpressionTest.class);
            MethodMetaData methodMetaData1 = ReflectionMetaDataMaker.createMethodMetaData(ExpressionTest.class.getDeclaredMethod("throws1", new Class[]{}));
            MethodMetaData methodMetaData2 = ReflectionMetaDataMaker.createMethodMetaData(ExpressionTest.class.getDeclaredMethod("throwsError", new Class[]{}));

            assertTrue(root.match(classMetaData1));
            assertTrue(root.match(classMetaData1, methodMetaData1, "java.lang.Exception"));
            assertFalse(root.match(classMetaData1, methodMetaData1, "java.lang.Error"));
            assertTrue(root.match(classMetaData1, methodMetaData2, "java.lang.Error"));
            assertFalse(root.match(classMetaData1, methodMetaData2, "java.lang.Exception"));
        }
        catch (Exception e) {
            fail(e.toString());
        }
    }

    public void testOneLevel_EXECUTION_AND() {
        try {
            Expression.registerExpressionTemplate(Expression.createExpressionTemplate("namespace", "* test.ExpressionTest.set(..)", "", "pc1", PointcutType.EXECUTION));
            Expression.registerExpressionTemplate(Expression.createExpressionTemplate("namespace", "* test.ExpressionTest.get(..)", "", "pc2", PointcutType.EXECUTION));
            Expression root = Expression.createRootExpression("namespace", "!pc1 && pc2");

            ClassMetaData classMetaData1 = ReflectionMetaDataMaker.createClassMetaData(ExpressionTest.class);
            ClassMetaData classMetaData2 = ReflectionMetaDataMaker.createClassMetaData(ExpressionException.class);
            MethodMetaData methodMetaData1 = ReflectionMetaDataMaker.createMethodMetaData(ExpressionTest.class.getDeclaredMethod("set", new Class[]{}));
            MethodMetaData methodMetaData2 = ReflectionMetaDataMaker.createMethodMetaData(ExpressionTest.class.getDeclaredMethod("get", new Class[]{}));
            MethodMetaData methodMetaData3 = ReflectionMetaDataMaker.createMethodMetaData(ExpressionTest.class.getDeclaredMethod("suite", new Class[]{}));

            assertTrue(root.match(classMetaData1));
            assertFalse(root.match(classMetaData1, methodMetaData1));
            assertFalse(root.match(classMetaData2));
            assertTrue(root.match(classMetaData1, methodMetaData2));
            assertFalse(root.match(classMetaData1, methodMetaData3));
        }
        catch (Exception e) {
            fail(e.toString());
        }
    }

    public void testOneLevel_CALL_AND() {
        try {
            Expression.registerExpressionTemplate(Expression.createExpressionTemplate("namespace", "* test.ExpressionTest.set(..)", "", "pc1", PointcutType.CALL));
            Expression.registerExpressionTemplate(Expression.createExpressionTemplate("namespace", "* test.ExpressionTest.get(..)", "", "pc2", PointcutType.CALL));
            Expression root = Expression.createRootExpression("namespace", "!pc1 && pc2");

            ClassMetaData classMetaData1 = ReflectionMetaDataMaker.createClassMetaData(ExpressionTest.class);
            MethodMetaData methodMetaData1 = ReflectionMetaDataMaker.createMethodMetaData(ExpressionTest.class.getDeclaredMethod("set", new Class[]{}));
            MethodMetaData methodMetaData2 = ReflectionMetaDataMaker.createMethodMetaData(ExpressionTest.class.getDeclaredMethod("get", new Class[]{}));
            MethodMetaData methodMetaData3 = ReflectionMetaDataMaker.createMethodMetaData(ExpressionTest.class.getDeclaredMethod("suite", new Class[]{}));

            assertTrue(root.match(classMetaData1));
            assertFalse(root.match(classMetaData1, methodMetaData1));
            assertTrue(root.match(classMetaData1, methodMetaData2));
            assertFalse(root.match(classMetaData1, methodMetaData3));
        }
        catch (Exception e) {
            fail(e.toString());
        }
    }

    public void testOneLevel_SET_AND() {
        try {
            Expression.registerExpressionTemplate(Expression.createExpressionTemplate("namespace", "* test.ExpressionTest.m_name", "", "pc1", PointcutType.SET));
            Expression.registerExpressionTemplate(Expression.createExpressionTemplate("namespace", "* test.ExpressionTest.m_type", "", "pc2", PointcutType.SET));
            Expression root = Expression.createRootExpression("namespace", "!pc1 && pc2");

            ClassMetaData classMetaData1 = ReflectionMetaDataMaker.createClassMetaData(ExpressionTest.class);
            ClassMetaData classMetaData2 = ReflectionMetaDataMaker.createClassMetaData(ExpressionException.class);
            FieldMetaData fieldMetaData1 = ReflectionMetaDataMaker.createFieldMetaData(ExpressionTest.class.getDeclaredField("m_name"));
            FieldMetaData fieldMetaData2 = ReflectionMetaDataMaker.createFieldMetaData(ExpressionTest.class.getDeclaredField("m_type"));

            assertTrue(root.match(classMetaData1));
            assertFalse(root.match(classMetaData2));
            assertFalse(root.match(classMetaData1, fieldMetaData1));
            assertTrue(root.match(classMetaData1, fieldMetaData2));
        }
        catch (Exception e) {
            fail(e.toString());
        }
    }

    public void testOneLevel_GET_AND() {
        try {
            Expression.registerExpressionTemplate(Expression.createExpressionTemplate("namespace", "* test.ExpressionTest.m_name", "", "pc1", PointcutType.GET));
            Expression.registerExpressionTemplate(Expression.createExpressionTemplate("namespace", "* test.ExpressionTest.m_type", "", "pc2", PointcutType.GET));
            Expression root = Expression.createRootExpression("namespace", "!pc1 && pc2");

            ClassMetaData classMetaData1 = ReflectionMetaDataMaker.createClassMetaData(ExpressionTest.class);
            ClassMetaData classMetaData2 = ReflectionMetaDataMaker.createClassMetaData(ExpressionException.class);
            FieldMetaData fieldMetaData1 = ReflectionMetaDataMaker.createFieldMetaData(ExpressionTest.class.getDeclaredField("m_name"));
            FieldMetaData fieldMetaData2 = ReflectionMetaDataMaker.createFieldMetaData(ExpressionTest.class.getDeclaredField("m_type"));

            assertTrue(root.match(classMetaData1));
            assertFalse(root.match(classMetaData2));
            assertFalse(root.match(classMetaData1, fieldMetaData1));
            assertTrue(root.match(classMetaData1, fieldMetaData2));
        }
        catch (Exception e) {
            fail(e.toString());
        }
    }

    public void testOneLevel_THROWS_AND() {
        try {
            Expression.registerExpressionTemplate(Expression.createExpressionTemplate("namespace", "* test.ExpressionTest.throws1(..)#java.lang.Exception", "", "pc1", PointcutType.THROWS));
            Expression.registerExpressionTemplate(Expression.createExpressionTemplate("namespace", "* test.ExpressionTest.throws2(..)#java.lang.Exception", "", "pc2", PointcutType.THROWS));
            Expression root = Expression.createRootExpression("namespace", "!pc1 && pc2");

            ClassMetaData classMetaData1 = ReflectionMetaDataMaker.createClassMetaData(ExpressionTest.class);
            ClassMetaData classMetaData2 = ReflectionMetaDataMaker.createClassMetaData(ExpressionException.class);
            MethodMetaData methodMetaData1 = ReflectionMetaDataMaker.createMethodMetaData(ExpressionTest.class.getDeclaredMethod("throws1", new Class[]{}));
            MethodMetaData methodMetaData2 = ReflectionMetaDataMaker.createMethodMetaData(ExpressionTest.class.getDeclaredMethod("throws2", new Class[]{}));

            assertTrue(root.match(classMetaData1));
            assertFalse(root.match(classMetaData2));
            assertFalse(root.match(classMetaData1, methodMetaData1));
            assertTrue(root.match(classMetaData1, methodMetaData2));
        }
        catch (Exception e) {
            fail(e.toString());
        }
    }

    public void testOneLevel_CFLOW_AND_EXECUTION() {
        try {
            Expression.registerExpressionTemplate(Expression.createExpressionTemplate("namespace", "* test.ExpressionTest.set(..)", "", "pc1", PointcutType.CFLOW));
            Expression.registerExpressionTemplate(Expression.createExpressionTemplate("namespace", "* test.ExpressionTest.get(..)", "", "pc2", PointcutType.EXECUTION));
            Expression root = Expression.createRootExpression("namespace", "pc1 && pc2");

            ClassMetaData classMetaData1 = ReflectionMetaDataMaker.createClassMetaData(ExpressionTest.class);
            ClassMetaData classMetaData2 = ReflectionMetaDataMaker.createClassMetaData(ExpressionException.class);
            MethodMetaData methodMetaData1 = ReflectionMetaDataMaker.createMethodMetaData(ExpressionTest.class.getDeclaredMethod("set", new Class[]{}));
            MethodMetaData methodMetaData2 = ReflectionMetaDataMaker.createMethodMetaData(ExpressionTest.class.getDeclaredMethod("get", new Class[]{}));
            MethodMetaData methodMetaData3 = ReflectionMetaDataMaker.createMethodMetaData(ExpressionTest.class.getDeclaredMethod("suite", new Class[]{}));

            assertTrue(root.match(classMetaData1));
            assertFalse(root.match(classMetaData1, methodMetaData1));
            assertFalse(root.match(classMetaData2));
            assertTrue(root.match(classMetaData1, methodMetaData2));
            assertFalse(root.match(classMetaData1, methodMetaData3));
        }
        catch (Exception e) {
            fail(e.toString());
        }
    }

    public void testOneLevel_CFLOW_AND_CALL() {
        try {
            Expression.registerExpressionTemplate(Expression.createExpressionTemplate("namespace", "* test.ExpressionTest.set(..)", "", "pc1", PointcutType.CFLOW));
            Expression.registerExpressionTemplate(Expression.createExpressionTemplate("namespace", "* test.ExpressionTest.get(..)", "", "pc2", PointcutType.CALL));
            Expression root = Expression.createRootExpression("namespace", "pc1 && pc2");

            ClassMetaData classMetaData1 = ReflectionMetaDataMaker.createClassMetaData(ExpressionTest.class);
            MethodMetaData methodMetaData1 = ReflectionMetaDataMaker.createMethodMetaData(ExpressionTest.class.getDeclaredMethod("set", new Class[]{}));
            MethodMetaData methodMetaData2 = ReflectionMetaDataMaker.createMethodMetaData(ExpressionTest.class.getDeclaredMethod("get", new Class[]{}));
            MethodMetaData methodMetaData3 = ReflectionMetaDataMaker.createMethodMetaData(ExpressionTest.class.getDeclaredMethod("suite", new Class[]{}));

            assertTrue(root.match(classMetaData1));
            assertFalse(root.match(classMetaData1, methodMetaData1));
            assertTrue(root.match(classMetaData1, methodMetaData2));
            assertFalse(root.match(classMetaData1, methodMetaData3));
        }
        catch (Exception e) {
            fail(e.toString());
        }
    }

    public void testTwoLevels_EXECUTION() {
        try {
            Expression.registerExpressionTemplate(Expression.createExpressionTemplate("namespace", "* test.ExpressionTest.set(..)", "", "pc1", PointcutType.EXECUTION));
            Expression.registerExpressionTemplate(Expression.createExpressionTemplate("namespace", "* test.ExpressionTest.get(..)", "", "pc2", PointcutType.EXECUTION));
            Expression.registerExpressionTemplate(Expression.createExpressionTemplate("namespace", "pc1 || pc2", "", "pc3", PointcutType.EXECUTION));
            Expression.registerExpressionTemplate(Expression.createExpressionTemplate("namespace", "* test.ExpressionTest.suite(..)", "", "pc4", PointcutType.EXECUTION));
            Expression root = Expression.createRootExpression("namespace", "pc3 && !pc4");

            ClassMetaData classMetaData1 = ReflectionMetaDataMaker.createClassMetaData(ExpressionTest.class);
            ClassMetaData classMetaData2 = ReflectionMetaDataMaker.createClassMetaData(ExpressionException.class);
            MethodMetaData methodMetaData1 = ReflectionMetaDataMaker.createMethodMetaData(ExpressionTest.class.getDeclaredMethod("set", new Class[]{}));
            MethodMetaData methodMetaData2 = ReflectionMetaDataMaker.createMethodMetaData(ExpressionTest.class.getDeclaredMethod("get", new Class[]{}));
            MethodMetaData methodMetaData3 = ReflectionMetaDataMaker.createMethodMetaData(ExpressionTest.class.getDeclaredMethod("suite", new Class[]{}));

            assertTrue(root.match(classMetaData1));
            assertTrue(root.match(classMetaData1, methodMetaData1));
            assertFalse(root.match(classMetaData2));
            assertTrue(root.match(classMetaData1, methodMetaData2));
            assertFalse(root.match(classMetaData1, methodMetaData3));
        }
        catch (Exception e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    public void testTwoLevels_CALL() {
        try {
            Expression.registerExpressionTemplate(Expression.createExpressionTemplate("namespace", "* test.ExpressionTest.set(..)", "", "pc1", PointcutType.CALL));
            Expression.registerExpressionTemplate(Expression.createExpressionTemplate("namespace", "* test.ExpressionTest.get(..)", "", "pc2", PointcutType.CALL));
            Expression.registerExpressionTemplate(Expression.createExpressionTemplate("namespace", "pc1 || pc2", "", "pc3", PointcutType.CALL));
            Expression.registerExpressionTemplate(Expression.createExpressionTemplate("namespace", "* test.ExpressionTest.suite(..)", "", "pc4", PointcutType.CALL));
            Expression root = Expression.createRootExpression("namespace", "pc3 && !pc4");

            ClassMetaData classMetaData1 = ReflectionMetaDataMaker.createClassMetaData(ExpressionTest.class);
            MethodMetaData methodMetaData1 = ReflectionMetaDataMaker.createMethodMetaData(ExpressionTest.class.getDeclaredMethod("set", new Class[]{}));
            MethodMetaData methodMetaData2 = ReflectionMetaDataMaker.createMethodMetaData(ExpressionTest.class.getDeclaredMethod("get", new Class[]{}));
            MethodMetaData methodMetaData3 = ReflectionMetaDataMaker.createMethodMetaData(ExpressionTest.class.getDeclaredMethod("suite", new Class[]{}));

            assertTrue(root.match(classMetaData1));
            assertTrue(root.match(classMetaData1, methodMetaData1));
            assertTrue(root.match(classMetaData1, methodMetaData2));
            assertFalse(root.match(classMetaData1, methodMetaData3));
        }
        catch (Exception e) {
            fail(e.toString());
        }
    }

    public void testTwoLevels_SET() {
        try {
            Expression.registerExpressionTemplate(Expression.createExpressionTemplate("namespace", "* test.ExpressionTest.m_name", "", "pc1", PointcutType.SET));
            Expression.registerExpressionTemplate(Expression.createExpressionTemplate("namespace", "* test.ExpressionTest.m_type", "", "pc2", PointcutType.SET));
            Expression.registerExpressionTemplate(Expression.createExpressionTemplate("namespace", "pc1 || pc2", "", "pc3", PointcutType.SET));
            Expression.registerExpressionTemplate(Expression.createExpressionTemplate("namespace", "* test.ExpressionTest.m_dummy", "", "pc4", PointcutType.SET));
            Expression root = Expression.createRootExpression("namespace", "pc3 && !pc4");

            ClassMetaData classMetaData1 = ReflectionMetaDataMaker.createClassMetaData(ExpressionTest.class);
            FieldMetaData fieldMetaData1 = ReflectionMetaDataMaker.createFieldMetaData(ExpressionTest.class.getDeclaredField("m_name"));
            FieldMetaData fieldMetaData2 = ReflectionMetaDataMaker.createFieldMetaData(ExpressionTest.class.getDeclaredField("m_type"));
            FieldMetaData fieldMetaData3 = ReflectionMetaDataMaker.createFieldMetaData(ExpressionTest.class.getDeclaredField("m_dummy"));

            assertTrue(root.match(classMetaData1));
            assertTrue(root.match(classMetaData1, fieldMetaData1));
            assertTrue(root.match(classMetaData1, fieldMetaData2));
            assertFalse(root.match(classMetaData1, fieldMetaData3));
        }
        catch (Exception e) {
            fail(e.toString());
        }
    }

    public void testTwoLevels_GET() {
        try {
            Expression.registerExpressionTemplate(Expression.createExpressionTemplate("namespace", "* test.ExpressionTest.m_name", "", "pc1", PointcutType.GET));
            Expression.registerExpressionTemplate(Expression.createExpressionTemplate("namespace", "* test.ExpressionTest.m_type", "", "pc2", PointcutType.GET));
            Expression.registerExpressionTemplate(Expression.createExpressionTemplate("namespace", "pc1 || pc2", "", "pc3", PointcutType.GET));
            Expression.registerExpressionTemplate(Expression.createExpressionTemplate("namespace", "* test.ExpressionTest.m_dummy", "", "pc4", PointcutType.GET));
            Expression root = Expression.createRootExpression("namespace", "pc3 && !pc4");

            ClassMetaData classMetaData1 = ReflectionMetaDataMaker.createClassMetaData(ExpressionTest.class);
            FieldMetaData fieldMetaData1 = ReflectionMetaDataMaker.createFieldMetaData(ExpressionTest.class.getDeclaredField("m_name"));
            FieldMetaData fieldMetaData2 = ReflectionMetaDataMaker.createFieldMetaData(ExpressionTest.class.getDeclaredField("m_type"));
            FieldMetaData fieldMetaData3 = ReflectionMetaDataMaker.createFieldMetaData(ExpressionTest.class.getDeclaredField("m_dummy"));

            assertTrue(root.match(classMetaData1));
            assertTrue(root.match(classMetaData1, fieldMetaData1));
            assertTrue(root.match(classMetaData1, fieldMetaData2));
            assertFalse(root.match(classMetaData1, fieldMetaData3));
        }
        catch (Exception e) {
            fail(e.toString());
        }
    }

    public void testTwoLevels_THROWS() {
        try {
            Expression.registerExpressionTemplate(Expression.createExpressionTemplate("namespace", "* test.ExpressionTest.throws1(..)#java.lang.Exception", "", "pc1", PointcutType.THROWS));
            Expression.registerExpressionTemplate(Expression.createExpressionTemplate("namespace", "* test.ExpressionTest.throws2(..)#java.lang.Exception", "", "pc2", PointcutType.THROWS));
            Expression.registerExpressionTemplate(Expression.createExpressionTemplate("namespace", "pc1 || pc2", "", "pc3", PointcutType.THROWS));
            Expression.registerExpressionTemplate(Expression.createExpressionTemplate("namespace", "* test.ExpressionTest.throwsDummy(..)#java.lang.Exception", "", "pc4", PointcutType.THROWS));
            Expression root = Expression.createRootExpression("namespace", "pc3 && !pc4");

            ClassMetaData classMetaData1 = ReflectionMetaDataMaker.createClassMetaData(ExpressionTest.class);
            MethodMetaData methodMetaData1 = ReflectionMetaDataMaker.createMethodMetaData(ExpressionTest.class.getDeclaredMethod("throws1", new Class[]{}));
            MethodMetaData methodMetaData2 = ReflectionMetaDataMaker.createMethodMetaData(ExpressionTest.class.getDeclaredMethod("throws2", new Class[]{}));
            MethodMetaData methodMetaData3 = ReflectionMetaDataMaker.createMethodMetaData(ExpressionTest.class.getDeclaredMethod("throwsDummy", new Class[]{}));

            assertTrue(root.match(classMetaData1));
            assertTrue(root.match(classMetaData1, methodMetaData1));
            assertTrue(root.match(classMetaData1, methodMetaData2));
            assertFalse(root.match(classMetaData1, methodMetaData3));
        }
        catch (Exception e) {
            fail(e.toString());
        }
    }

    public void testComplexPattern_EXECUTION_1() {
        try {
            Expression.registerExpressionTemplate(Expression.createExpressionTemplate("namespace", "* test.ExpressionTest.set(..)", "", "pc1", PointcutType.EXECUTION));
            Expression.registerExpressionTemplate(Expression.createExpressionTemplate("namespace", "* test.ExpressionTest.get(..)", "", "pc2", PointcutType.EXECUTION));
            Expression.registerExpressionTemplate(Expression.createExpressionTemplate("namespace", "* test.ExpressionTest.suite(..)", "", "pc3", PointcutType.EXECUTION));
            Expression root = Expression.createRootExpression("namespace", "!pc3 && (pc1 || pc2)");

            ClassMetaData classMetaData1 = ReflectionMetaDataMaker.createClassMetaData(ExpressionTest.class);
            MethodMetaData methodMetaData1 = ReflectionMetaDataMaker.createMethodMetaData(ExpressionTest.class.getDeclaredMethod("set", new Class[]{}));
            MethodMetaData methodMetaData2 = ReflectionMetaDataMaker.createMethodMetaData(ExpressionTest.class.getDeclaredMethod("get", new Class[]{}));
            MethodMetaData methodMetaData3 = ReflectionMetaDataMaker.createMethodMetaData(ExpressionTest.class.getDeclaredMethod("suite", new Class[]{}));

            assertTrue(root.match(classMetaData1));
            assertTrue(root.match(classMetaData1, methodMetaData1));
            assertTrue(root.match(classMetaData1, methodMetaData2));
            assertFalse(root.match(classMetaData1, methodMetaData3));
        }
        catch (Exception e) {
            fail(e.toString());
        }
    }

    public void testComplexPattern_EXECUTION_2() {
        try {
            Expression.registerExpressionTemplate(Expression.createExpressionTemplate("namespace", "* test.ExpressionTest.set(..)", "", "pc1", PointcutType.EXECUTION));
            Expression.registerExpressionTemplate(Expression.createExpressionTemplate("namespace", "* test.ExpressionTest.get(..)", "", "pc2", PointcutType.EXECUTION));
            Expression.registerExpressionTemplate(Expression.createExpressionTemplate("namespace", "* test.ExpressionTest.suite(..)", "", "pc3", PointcutType.EXECUTION));
            Expression root = Expression.createRootExpression("namespace", "pc1 && !(pc2 || pc3)");

            ClassMetaData classMetaData1 = ReflectionMetaDataMaker.createClassMetaData(ExpressionTest.class);
            MethodMetaData methodMetaData1 = ReflectionMetaDataMaker.createMethodMetaData(ExpressionTest.class.getDeclaredMethod("set", new Class[]{}));
            MethodMetaData methodMetaData2 = ReflectionMetaDataMaker.createMethodMetaData(ExpressionTest.class.getDeclaredMethod("get", new Class[]{}));
            MethodMetaData methodMetaData3 = ReflectionMetaDataMaker.createMethodMetaData(ExpressionTest.class.getDeclaredMethod("suite", new Class[]{}));

            assertTrue(root.match(classMetaData1));
            assertTrue(root.match(classMetaData1, methodMetaData1));
            assertFalse(root.match(classMetaData1, methodMetaData2));
            assertFalse(root.match(classMetaData1, methodMetaData3));
        }
        catch (Exception e) {
            fail(e.toString());
        }
    }

    public void testComplexPattern_CALL_1() {
        try {
            Expression.registerExpressionTemplate(Expression.createExpressionTemplate("namespace", "* test.ExpressionTest.set(..)", "", "pc1", PointcutType.CALL));
            Expression.registerExpressionTemplate(Expression.createExpressionTemplate("namespace", "* test.ExpressionTest.get(..)", "", "pc2", PointcutType.CALL));
            Expression.registerExpressionTemplate(Expression.createExpressionTemplate("namespace", "* test.ExpressionTest.suite(..)", "", "pc3", PointcutType.CALL));
            Expression root = Expression.createRootExpression("namespace", "!pc3 && (pc1 || pc2)");

            ClassMetaData classMetaData1 = ReflectionMetaDataMaker.createClassMetaData(ExpressionTest.class);
            MethodMetaData methodMetaData1 = ReflectionMetaDataMaker.createMethodMetaData(ExpressionTest.class.getDeclaredMethod("set", new Class[]{}));
            MethodMetaData methodMetaData2 = ReflectionMetaDataMaker.createMethodMetaData(ExpressionTest.class.getDeclaredMethod("get", new Class[]{}));
            MethodMetaData methodMetaData3 = ReflectionMetaDataMaker.createMethodMetaData(ExpressionTest.class.getDeclaredMethod("suite", new Class[]{}));

            assertTrue(root.match(classMetaData1));
            assertTrue(root.match(classMetaData1, methodMetaData1));
            assertTrue(root.match(classMetaData1, methodMetaData2));
            assertFalse(root.match(classMetaData1, methodMetaData3));
        }
        catch (Exception e) {
            fail(e.toString());
        }
    }

    public void testComplexPattern_CALL_2() {
        try {
            Expression.registerExpressionTemplate(Expression.createExpressionTemplate("namespace", "* test.ExpressionTest.set(..)", "", "pc1", PointcutType.CALL));
            Expression.registerExpressionTemplate(Expression.createExpressionTemplate("namespace", "* test.ExpressionTest.get(..)", "", "pc2", PointcutType.CALL));
            Expression.registerExpressionTemplate(Expression.createExpressionTemplate("namespace", "* test.ExpressionTest.suite(..)", "", "pc3", PointcutType.CALL));
            Expression root = Expression.createRootExpression("namespace", "pc1 && !(pc2 || pc3)");

            ClassMetaData classMetaData1 = ReflectionMetaDataMaker.createClassMetaData(ExpressionTest.class);
            MethodMetaData methodMetaData1 = ReflectionMetaDataMaker.createMethodMetaData(ExpressionTest.class.getDeclaredMethod("set", new Class[]{}));
            MethodMetaData methodMetaData2 = ReflectionMetaDataMaker.createMethodMetaData(ExpressionTest.class.getDeclaredMethod("get", new Class[]{}));
            MethodMetaData methodMetaData3 = ReflectionMetaDataMaker.createMethodMetaData(ExpressionTest.class.getDeclaredMethod("suite", new Class[]{}));

            assertTrue(root.match(classMetaData1));
            assertTrue(root.match(classMetaData1, methodMetaData1));
            assertFalse(root.match(classMetaData1, methodMetaData2));
            assertFalse(root.match(classMetaData1, methodMetaData3));
        }
        catch (Exception e) {
            fail(e.toString());
        }
    }

    public void testMatchHierachicalExpression_EXECUTION() {
        try {
            Expression root = Expression.createRootExpression("namespace", "* *..TestCase+.set(..)", PointcutType.EXECUTION);
            ClassMetaData classMetaData1 = ReflectionMetaDataMaker.createClassMetaData(ExpressionTest.class);
            ClassMetaData classMetaData2 = ReflectionMetaDataMaker.createClassMetaData(ExpressionException.class);
            MethodMetaData methodMetaData1 = ReflectionMetaDataMaker.createMethodMetaData(ExpressionTest.class.getDeclaredMethod("set", new Class[]{}));
            MethodMetaData methodMetaData2 = ReflectionMetaDataMaker.createMethodMetaData(ExpressionTest.class.getDeclaredMethod("get", new Class[]{}));
            assertTrue(root.match(classMetaData1));
            assertTrue(root.match(classMetaData1, methodMetaData1));
            assertFalse(root.match(classMetaData2));
            assertFalse(root.match(classMetaData1, methodMetaData2));
        }
        catch (Exception e) {
            fail(e.toString());
        }
    }

    public void testMatchHierachicalExpression_CALL() {
        try {
            Expression root = Expression.createRootExpression("namespace", "*->* *..TestCase+.suite(..)", PointcutType.CALL);
            ClassMetaData classMetaData1 = ReflectionMetaDataMaker.createClassMetaData(ExpressionTest.class);
            MethodMetaData methodMetaData1 = ReflectionMetaDataMaker.createMethodMetaData(ExpressionTest.class.getDeclaredMethod("set", new Class[]{}));
            MethodMetaData methodMetaData2 = ReflectionMetaDataMaker.createMethodMetaData(ExpressionTest.class.getDeclaredMethod("get", new Class[]{}));
            MethodMetaData methodMetaData3 = ReflectionMetaDataMaker.createMethodMetaData(ExpressionTest.class.getDeclaredMethod("suite", new Class[]{}));
            assertTrue(root.match(classMetaData1));
            assertFalse(root.match(classMetaData1, methodMetaData1));
            assertFalse(root.match(classMetaData1, methodMetaData2));
            assertTrue(root.match(classMetaData1, methodMetaData3));
        }
        catch (Exception e) {
            fail(e.toString());
        }
    }

    public void testMatchHierachicalExpression_SET() {
        try {
            Expression root = Expression.createRootExpression("namespace", "* *..TestCase+.m_dummy", PointcutType.SET);
            ClassMetaData classMetaData1 = ReflectionMetaDataMaker.createClassMetaData(ExpressionTest.class);
            FieldMetaData fieldMetaData1 = ReflectionMetaDataMaker.createFieldMetaData(ExpressionTest.class.getDeclaredField("m_name"));
            FieldMetaData fieldMetaData2 = ReflectionMetaDataMaker.createFieldMetaData(ExpressionTest.class.getDeclaredField("m_type"));
            FieldMetaData fieldMetaData3 = ReflectionMetaDataMaker.createFieldMetaData(ExpressionTest.class.getDeclaredField("m_dummy"));
            assertTrue(root.match(classMetaData1));
            assertFalse(root.match(classMetaData1, fieldMetaData1));
            assertFalse(root.match(classMetaData1, fieldMetaData2));
            assertTrue(root.match(classMetaData1, fieldMetaData3));
        }
        catch (Exception e) {
            fail(e.toString());
        }
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(ExpressionTest.class);
    }

    public ExpressionTest(String name) {
        super(name);
    }

    // === methods below are needed for the test even though they are empty and not "used" anywhere ===

    public void set() {
    }

    public void get() {
    }

    public void throws1() throws Exception {
    }

    public void throws2() throws Exception {
    }

    public void throwsDummy() throws Exception {
    }

    public void throwsError() throws Error {
    }

    private String m_name;
    private String m_type;
    private String m_dummy;
}
