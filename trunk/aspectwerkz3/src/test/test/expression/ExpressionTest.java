/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.expression;

import junit.framework.TestCase;
import org.codehaus.aspectwerkz.expression.ExpressionContext;
import org.codehaus.aspectwerkz.expression.ExpressionInfo;
import org.codehaus.aspectwerkz.expression.ExpressionNamespace;
import org.codehaus.aspectwerkz.expression.ExpressionVisitor;
import org.codehaus.aspectwerkz.expression.PointcutType;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.FieldInfo;
import org.codehaus.aspectwerkz.reflect.MethodInfo;
import org.codehaus.aspectwerkz.reflect.impl.java.JavaClassInfo;
import org.codehaus.aspectwerkz.reflect.impl.java.JavaFieldInfo;
import org.codehaus.aspectwerkz.reflect.impl.java.JavaMethodInfo;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class ExpressionTest extends TestCase {
    private static final String NAMESPACE = "TESTING";
    private static ExpressionNamespace s_namespace = ExpressionNamespace.getNamespace(NAMESPACE);
    private static ClassInfo s_declaringType = JavaClassInfo.getClassInfo(Target.class);
    private static MethodInfo modifiers1;
    private static MethodInfo modifiers2;
    private static MethodInfo modifiers3;
    private static MethodInfo modifiers4;
    private static MethodInfo parameters1;
    private static MethodInfo parameters2;
    private static MethodInfo parameters3;
    private static MethodInfo parameters4;
    private static MethodInfo parameters5;
    private static MethodInfo returnType1;
    private static MethodInfo returnType2;
    private static MethodInfo returnType3;
    private static MethodInfo returnType4;
    private static MethodInfo returnType5;
    private static MethodInfo _method$Name1;
    private static MethodInfo attributes1;
    private static FieldInfo modifier1;
    private static FieldInfo modifier2;
    private static FieldInfo modifier3;
    private static FieldInfo type1;
    private static FieldInfo type2;
    private static FieldInfo type3;
    private static FieldInfo type4;
    private static FieldInfo _field$Name1;
    private static FieldInfo attribute1;

    // ============ setup =============
    static {
        MethodInfo[] methods = s_declaringType.getMethods();
        for (int i = 0; i < methods.length; i++) {
            MethodInfo method = methods[i];
            if (method.getName().equals("modifiers1")) {
                modifiers1 = method;
            }
            if (method.getName().equals("modifiers2")) {
                modifiers2 = method;
            }
            if (method.getName().equals("modifiers3")) {
                modifiers3 = method;
            }
            if (method.getName().equals("modifiers4")) {
                modifiers4 = method;
            }
            if (method.getName().equals("parameters1")) {
                parameters1 = method;
            }
            if (method.getName().equals("parameters2")) {
                parameters2 = method;
            }
            if (method.getName().equals("parameters3")) {
                parameters3 = method;
            }
            if (method.getName().equals("parameters4")) {
                parameters4 = method;
            }
            if (method.getName().equals("parameters5")) {
                parameters5 = method;
            }
            if (method.getName().equals("returnType1")) {
                returnType1 = method;
            }
            if (method.getName().equals("returnType1")) {
                returnType1 = method;
            }
            if (method.getName().equals("returnType2")) {
                returnType2 = method;
            }
            if (method.getName().equals("returnType3")) {
                returnType3 = method;
            }
            if (method.getName().equals("returnType4")) {
                returnType4 = method;
            }
            if (method.getName().equals("returnType5")) {
                returnType5 = method;
            }
            if (method.getName().equals("__method$Name1")) {
                _method$Name1 = method;
            }
            if (method.getName().equals("attributes1")) {
                attributes1 = method;
            }
        }
        FieldInfo[] fields = s_declaringType.getFields();
        for (int f = 0; f < fields.length; f++) {
            FieldInfo field = fields[f];
            if (field.getName().equals("modifier1")) {
                modifier1 = field;
            }
            if (field.getName().equals("modifier2")) {
                modifier2 = field;
            }
            if (field.getName().equals("modifier3")) {
                modifier3 = field;
            }
            if (field.getName().equals("type1")) {
                type1 = field;
            }
            if (field.getName().equals("type2")) {
                type2 = field;
            }
            if (field.getName().equals("type3")) {
                type3 = field;
            }
            if (field.getName().equals("type4")) {
                type4 = field;
            }
            if (field.getName().equals("__field$Name1")) {
                _field$Name1 = field;
            }
            if (field.getName().equals("attribute1")) {
                attribute1 = field;
            }
        }
    }

    public ExpressionTest(String name) {
        super(name);
    }

    // ============ method modifiers test =============
    public void testMethodModifiers1() throws Exception {
        assertFalse(
                new ExpressionInfo("call(public void test.expression.Target.modifiers1())", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                modifiers1,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("call(void test.expression.Target.modifiers1())", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                modifiers1,
                                null
                        )
                )
        );
        assertFalse(
                new ExpressionInfo("call(static final void test.expression.Target.modifiers1())", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                modifiers1,
                                null
                        )
                )
        );
    }

    public void testMethodModifiers2() throws Exception {
        assertTrue(
                new ExpressionInfo("call(public void test.expression.Target.modifiers2())", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                modifiers2,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("call(void test.expression.Target.modifiers2())", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                modifiers2,
                                null
                        )
                )
        );
        assertFalse(
                new ExpressionInfo("call(static final void test.expression.Target.modifiers2())", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                modifiers2,
                                null
                        )
                )
        );
        assertFalse(
                new ExpressionInfo("call(public static final void test.expression.Target.modifiers2())", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                modifiers2,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("call(public static void test.expression.Target.modifiers2())", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                modifiers2,
                                null
                        )
                )
        );
    }

    public void testMethodModifiers3() throws Exception {
        assertFalse(
                new ExpressionInfo("call(public void test.expression.Target.modifiers3())", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                modifiers3,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("call(void test.expression.Target.modifiers3())", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                modifiers3,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("call(static final void test.expression.Target.modifiers3())", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                modifiers3,
                                null
                        )
                )
        );
        assertFalse(
                new ExpressionInfo("call(public static native void test.expression.Target.modifiers3())", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                modifiers3,
                                null
                        )
                )
        );
        assertFalse(
                new ExpressionInfo("call(public static void test.expression.Target.modifiers3())", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                modifiers3,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("call(synchronized void test.expression.Target.modifiers3())", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                modifiers3,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo(
                        "call(protected native synchronized void test.expression.Target.modifiers3())",
                        NAMESPACE
                ).getExpression().match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                modifiers3, null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("call(native protected void test.expression.Target.modifiers3())", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                modifiers3,
                                null
                        )
                )
        );
    }

    public void testMethodModifiers4() throws Exception {
        assertFalse(
                new ExpressionInfo("call(public * test.expression.*.*(..)) && within(test.expression.*)", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                modifiers4,
                                s_declaringType
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("call(private * test.expression.*.*(..)) && within(test.expression.*)", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                modifiers4,
                                s_declaringType
                        )
                )
        );
        assertFalse(
                new ExpressionInfo(
                        "call(protected * test.expression.*.*(..)) && within(test.expression.*)",
                        NAMESPACE
                ).getExpression().match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                modifiers4,
                                s_declaringType
                        )
                )
        );
    }

    public void testMethodModifiers5() throws Exception {
        assertFalse(
                new ExpressionInfo("call(!public void test.expression.Target.modifiers2())", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                modifiers2,
                                null
                        )
                )
        );
        assertFalse(
                new ExpressionInfo("call(NOT public void test.expression.Target.modifiers2())", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                modifiers2,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("call(!private void test.expression.Target.modifiers2())", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                modifiers2,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("call(!private static void test.expression.Target.modifiers2())", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                modifiers2,
                                null
                        )
                )
        );
        assertFalse(
                new ExpressionInfo("call(public !static void test.expression.Target.modifiers2())", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                modifiers2,
                                null
                        )
                )
        );
        assertFalse(
                new ExpressionInfo("call(public NOT static void test.expression.Target.modifiers2())", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                modifiers2,
                                null
                        )
                )
        );
    }

    // ============ method parameters test =============
    public void testMethodParameters1() throws Exception {
        assertTrue(
                new ExpressionInfo("call(void test.expression.Target.parameters1())", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                parameters1,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("call(void test.expression.Target.parameters1(..))", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                parameters1,
                                null
                        )
                )
        );
        assertFalse(
                new ExpressionInfo("call(void test.expression.Target.parameters1(*))", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                parameters1,
                                null
                        )
                )
        );
        assertFalse(
                new ExpressionInfo("call(void test.expression.Target.parameters1(int))", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                parameters1,
                                null
                        )
                )
        );
    }

    public void testMethodParameters2() throws Exception {
        assertFalse(
                new ExpressionInfo("call(void test.expression.Target.parameters2())", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                parameters2,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("call(void test.expression.Target.parameters2(..))", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                parameters2,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("call(void test.expression.Target.parameters2(int, ..))", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                parameters2,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("call(void test.expression.Target.parameters2(int, float, byte))", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                parameters2,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("call(void test.expression.Target.parameters2(int, *, *))", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                parameters2,
                                null
                        )
                )
        );
        assertFalse(
                new ExpressionInfo("call(void test.expression.Target.parameters2(int, int, byte))", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                parameters2,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("call(void test.expression.Target.parameters2(*, *, byte))", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                parameters2,
                                null
                        )
                )
        );
    }

    public void testMethodParameters3() throws Exception {
        assertFalse(
                new ExpressionInfo("call(void test.expression.Target.parameters3())", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                parameters3,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("call(void test.expression.Target.parameters3(..))", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                parameters3,
                                null
                        )
                )
        );
        assertFalse(
                new ExpressionInfo("call(void test.expression.Target.parameters3(int, ..))", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                parameters3,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("call(void test.expression.Target.parameters3(String, ..))", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                parameters3,
                                null
                        )
                )
        );
        assertFalse(
                new ExpressionInfo(
                        "call(void test.expression.Target.parameters3(String, String, String))",
                        NAMESPACE
                ).getExpression().match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                parameters3, null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo(
                        "call(void test.expression.Target.parameters3(String, StringBuffer, String))",
                        NAMESPACE
                ).getExpression().match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                parameters3, null
                        )
                )
        );
        assertFalse(
                new ExpressionInfo(
                        "call(void test.expression.Target.parameters3(String, StringBuffer, String, *))",
                        NAMESPACE
                ).getExpression().match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                parameters3, null
                        )
                )
        );
    }

    public void testMethodParameters4() throws Exception {
        assertFalse(
                new ExpressionInfo("call(void test.expression.Target.parameters4())", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                parameters4,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("call(void test.expression.Target.parameters4(..))", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                parameters4,
                                null
                        )
                )
        );
        assertFalse(
                new ExpressionInfo("call(void test.expression.Target.parameters4(Object))", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                parameters4,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("call(void test.expression.Target.parameters4(Object[]))", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                parameters4,
                                null
                        )
                )
        );
        assertFalse(
                new ExpressionInfo("call(void test.expression.Target.parameters4(Object[][]))", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                parameters4,
                                null
                        )
                )
        );
    }

    public void testMethodParameters5() throws Exception {
        assertFalse(
                new ExpressionInfo("call(void test.expression.Target.parameters5())", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                parameters5,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("call(void test.expression.Target.parameters5(..))", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                parameters5,
                                null
                        )
                )
        );
        assertFalse(
                new ExpressionInfo("call(void test.expression.Target.parameters5(int))", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                parameters5,
                                null
                        )
                )
        );
        assertFalse(
                new ExpressionInfo("call(void test.expression.Target.parameters5(int[]))", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                parameters5,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("call(void test.expression.Target.parameters5(int[][]))", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                parameters5,
                                null
                        )
                )
        );
        assertFalse(
                new ExpressionInfo("call(void test.expression.Target.parameters5(int[][][]))", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                parameters5,
                                null
                        )
                )
        );
    }

    // ============ method return type test =============
    public void testMethodReturnType1() throws Exception {
        assertTrue(
                new ExpressionInfo("call(void test.expression.Target.returnType1())", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                returnType1,
                                null
                        )
                )
        );
        assertFalse(
                new ExpressionInfo("call(String test.expression.Target.returnType1())", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                returnType1,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("call(* test.expression.Target.returnType1())", NAMESPACE).getExpression().match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                returnType1,
                                null
                        )
                )
        );
    }

    public void testMethodReturnType2() throws Exception {
        assertFalse(
                new ExpressionInfo("call(void test.expression.Target.returnType2())", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                returnType2,
                                null
                        )
                )
        );
        assertFalse(
                new ExpressionInfo("call(String test.expression.Target.returnType2())", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                returnType2,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("call(* test.expression.Target.returnType2())", NAMESPACE).getExpression().match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                returnType2,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("call(int test.expression.Target.returnType2())", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                returnType2,
                                null
                        )
                )
        );
        assertFalse(
                new ExpressionInfo("call(int[] test.expression.Target.returnType2())", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                returnType2,
                                null
                        )
                )
        );
    }

    public void testMethodReturnType3() throws Exception {
        assertFalse(
                new ExpressionInfo("call(void test.expression.Target.returnType3())", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                returnType3,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("call(String test.expression.Target.returnType3())", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                returnType3,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("call(* test.expression.Target.returnType3())", NAMESPACE).getExpression().match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                returnType3,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("call(java.lang.String* test.expression.Target.returnType3())", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                returnType3,
                                null
                        )
                )
        );
        assertFalse(
                new ExpressionInfo("call(java.lang.StringBuffer test.expression.Target.returnType3())", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                returnType3,
                                null
                        )
                )
        );
    }

    public void testMethodReturnType4() throws Exception {
        assertFalse(
                new ExpressionInfo("call(void test.expression.Target.returnType4())", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                returnType4,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("call(Process test.expression.Target.returnType4())", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                returnType4,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("call(* test.expression.Target.returnType4())", NAMESPACE).getExpression().match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                returnType4,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("call(java.lang.Process test.expression.Target.returnType4())", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                returnType4,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("call(java.lang.* test.expression.Target.returnType4())", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                returnType4,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("call(java..* test.expression.Target.returnType4())", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                returnType4,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("call(java.*.Process test.expression.Target.returnType4())", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                returnType4,
                                null
                        )
                )
        );
    }

    public void testMethodReturnType5() throws Exception {
        assertFalse(
                new ExpressionInfo("call(void test.expression.Target.returnType5())", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                returnType5,
                                null
                        )
                )
        );
        assertFalse(
                new ExpressionInfo("call(float test.expression.Target.returnType5())", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                returnType5,
                                null
                        )
                )
        );
        assertFalse(
                new ExpressionInfo("call(float[] test.expression.Target.returnType5())", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                returnType5,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("call(float[][] test.expression.Target.returnType5())", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                returnType5,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("call(* test.expression.Target.returnType5())", NAMESPACE).getExpression().match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                returnType5,
                                null
                        )
                )
        );
    }

    // ============ method name test =============
    public void testMethodName() throws Exception {
        assertTrue(
                new ExpressionInfo("call(void test.expression.Target.__method$Name1())", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                _method$Name1,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("call(void test.expression.Target.__method$*())", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                _method$Name1,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("call(void test.expression.Target.*Name1())", NAMESPACE).getExpression().match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                _method$Name1,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("call(void test.expression.Target.*$*())", NAMESPACE).getExpression().match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                _method$Name1,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("call(void test.expression.*.__method$Name1())", NAMESPACE).getExpression().match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                _method$Name1,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("call(void test..*.__method$Name1())", NAMESPACE).getExpression().match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                _method$Name1,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("call(void test..*.*())", NAMESPACE).getExpression().match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                _method$Name1,
                                null
                        )
                )
        );
        assertFalse(
                new ExpressionInfo("call(void test.expression.Target.__Method$Name1())", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                _method$Name1,
                                null
                        )
                )
        );
        assertFalse(
                new ExpressionInfo("call(void test.expression.Target.__method$Name())", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                _method$Name1,
                                null
                        )
                )
        );
        assertFalse(
                new ExpressionInfo("call(void test.expression.Target._methodName1())", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                _method$Name1,
                                null
                        )
                )
        );
    }

    // ============ method attribute test =============
    public void testMethodAttributes1() throws Exception {
        assertTrue(
                new ExpressionInfo("call(void test.expression.Target.attributes1())", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                attributes1,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("call(@Requires void test.expression.Target.attributes1())", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                attributes1,
                                null
                        )
                )
        );
        assertFalse(
                new ExpressionInfo("call(@RequiresNew void test.expression.Target.attributes1())", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                attributes1,
                                null
                        )
                )
        );
        assertFalse(
                new ExpressionInfo(
                        "call(@Requires @RequiresNew void test.expression.Target.attributes1())",
                        NAMESPACE
                ).getExpression().match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                attributes1, null
                        )
                )
        );
    }

    // ============ field modifier test =============
    public void testFieldModifiers1() throws Exception {
        assertTrue(
                new ExpressionInfo("set(int test.expression.Target.modifier1)", NAMESPACE).getExpression().match(
                        new ExpressionContext(
                                PointcutType.SET,
                                modifier1,
                                null
                        )
                )
        );
        assertFalse(
                new ExpressionInfo("set(public int test.expression.Target.modifier1)", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.SET,
                                modifier1,
                                null
                        )
                )
        );
        assertFalse(
                new ExpressionInfo("set(public int test.expression.Target.modifier1)", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.SET,
                                modifier1,
                                null
                        )
                )
        );
    }

    public void testFieldModifiers2() throws Exception {
        assertTrue(
                new ExpressionInfo("set(int test.expression.Target.modifier2)", NAMESPACE).getExpression().match(
                        new ExpressionContext(
                                PointcutType.SET,
                                modifier2,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("set(public int test.expression.Target.modifier2)", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.SET,
                                modifier2,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("set(static public int test.expression.Target.modifier2)", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.SET,
                                modifier2,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("set(static int test.expression.Target.modifier2)", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.SET,
                                modifier2,
                                null
                        )
                )
        );
        assertFalse(
                new ExpressionInfo("set(protected int test.expression.Target.modifier2)", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.SET,
                                modifier2,
                                null
                        )
                )
        );
    }

    public void testFieldModifiers3() throws Exception {
        assertTrue(
                new ExpressionInfo("set(int test.expression.Target.modifier3)", NAMESPACE).getExpression().match(
                        new ExpressionContext(
                                PointcutType.SET,
                                modifier3,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("set(protected int test.expression.Target.modifier3)", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.SET,
                                modifier3,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("set(static protected int test.expression.Target.modifier3)", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.SET,
                                modifier3,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("set(static int test.expression.Target.modifier3)", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.SET,
                                modifier3,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("set(transient int test.expression.Target.modifier3)", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.SET,
                                modifier3,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo(
                        "set(static transient protected final int test.expression.Target.modifier3)",
                        NAMESPACE
                ).getExpression().match(
                        new ExpressionContext(
                                PointcutType.SET,
                                modifier3, null
                        )
                )
        );
        assertFalse(
                new ExpressionInfo("set(public int test.expression.Target.modifier3)", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.SET,
                                modifier3,
                                null
                        )
                )
        );
    }

    //    public void testFieldModifiers4() throws Exception {
    //        assertTrue(new ExpressionInfo("set(!private int test.expression.Target.modifier2)", NAMESPACE).getExpression().parse(new ExpressionContext(PointcutType.SET,
    //                                                                                                                                          modifier2,
    //                                                                                                                                          null)));
    //        assertFalse(new ExpressionInfo("set(!public int test.expression.Target.modifier2)", NAMESPACE).getExpression()
    //                                                                                                     .parse(new ExpressionContext(PointcutType.SET,
    //                                                                                                                                  modifier2,
    //                                                                                                                                  null)));
    //        assertFalse(new ExpressionInfo("set(public !static int test.expression.Target.modifier2)", NAMESPACE).getExpression()
    //                                                                                                     .parse(new ExpressionContext(PointcutType.SET,
    //                                                                                                                                  modifier2,
    //                                                                                                                                  null)));
    //    }
    // ============ field type test =============
    public void testFieldType1() throws Exception {
        assertTrue(
                new ExpressionInfo("set(int test.expression.Target.type1)", NAMESPACE).getExpression().match(
                        new ExpressionContext(
                                PointcutType.SET,
                                type1,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("set(* test.expression.Target.type1)", NAMESPACE).getExpression().match(
                        new ExpressionContext(
                                PointcutType.SET,
                                type1,
                                null
                        )
                )
        );
        assertFalse(
                new ExpressionInfo("set(Integer test.expression.Target.type1)", NAMESPACE).getExpression().match(
                        new ExpressionContext(
                                PointcutType.SET,
                                type1,
                                null
                        )
                )
        );
        assertFalse(
                new ExpressionInfo("set(int[] test.expression.Target.type1)", NAMESPACE).getExpression().match(
                        new ExpressionContext(
                                PointcutType.SET,
                                type1,
                                null
                        )
                )
        );
    }

    public void testFieldType2() throws Exception {
        assertFalse(
                new ExpressionInfo("set(int test.expression.Target.type2)", NAMESPACE).getExpression().match(
                        new ExpressionContext(
                                PointcutType.SET,
                                type2,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("set(* test.expression.Target.type2)", NAMESPACE).getExpression().match(
                        new ExpressionContext(
                                PointcutType.SET,
                                type2,
                                null
                        )
                )
        );
        assertFalse(
                new ExpressionInfo("set(Integer test.expression.Target.type2)", NAMESPACE).getExpression().match(
                        new ExpressionContext(
                                PointcutType.SET,
                                type2,
                                null
                        )
                )
        );
        assertFalse(
                new ExpressionInfo("set(int[] test.expression.Target.type2)", NAMESPACE).getExpression().match(
                        new ExpressionContext(
                                PointcutType.SET,
                                type2,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("set(int[][] test.expression.Target.type2)", NAMESPACE).getExpression().match(
                        new ExpressionContext(
                                PointcutType.SET,
                                type2,
                                null
                        )
                )
        );
    }

    public void testFieldType3() throws Exception {
        assertTrue(
                new ExpressionInfo("set(String test.expression.Target.type3)", NAMESPACE).getExpression().match(
                        new ExpressionContext(
                                PointcutType.SET,
                                type3,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("set(java.lang.String test.expression.Target.type3)", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.SET,
                                type3,
                                null
                        )
                )
        );
        assertFalse(
                new ExpressionInfo("set(java.lang.string test.expression.Target.type3)", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.SET,
                                type3,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("set(java..* test.expression.Target.type3)", NAMESPACE).getExpression().match(
                        new ExpressionContext(
                                PointcutType.SET,
                                type3,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("set(java.*.String test.expression.Target.type3)", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.SET,
                                type3,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("set(java.lang.String* test.expression.Target.type3)", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.SET,
                                type3,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("set(* test.expression.Target.type3)", NAMESPACE).getExpression().match(
                        new ExpressionContext(
                                PointcutType.SET,
                                type3,
                                null
                        )
                )
        );
        assertFalse(
                new ExpressionInfo("set(StringBuffer test.expression.Target.type3)", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.SET,
                                type3,
                                null
                        )
                )
        );
        assertFalse(
                new ExpressionInfo("set(java.lang.StringBuffer test.expression.Target.type3)", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.SET,
                                type3,
                                null
                        )
                )
        );
        assertFalse(
                new ExpressionInfo("set(String[] test.expression.Target.type3)", NAMESPACE).getExpression().match(
                        new ExpressionContext(
                                PointcutType.SET,
                                type3,
                                null
                        )
                )
        );
        assertFalse(
                new ExpressionInfo("set(java.lang.String[] test.expression.Target.type3)", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.SET,
                                type3,
                                null
                        )
                )
        );
    }

    public void testFieldType4() throws Exception {
        assertFalse(
                new ExpressionInfo("set(String test.expression.Target.type4)", NAMESPACE).getExpression().match(
                        new ExpressionContext(
                                PointcutType.SET,
                                type4,
                                null
                        )
                )
        );
        assertFalse(
                new ExpressionInfo("set(java.lang.String test.expression.Target.type4)", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.SET,
                                type4,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("set(String[] test.expression.Target.type4)", NAMESPACE).getExpression().match(
                        new ExpressionContext(
                                PointcutType.SET,
                                type4,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("set(java.lang.String[] test.expression.Target.type4)", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.SET,
                                type4,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("set(* test.expression.Target.type4)", NAMESPACE).getExpression().match(
                        new ExpressionContext(
                                PointcutType.SET,
                                type4,
                                null
                        )
                )
        );
        assertFalse(
                new ExpressionInfo("set(String[][] test.expression.Target.type4)", NAMESPACE).getExpression().match(
                        new ExpressionContext(
                                PointcutType.SET,
                                type4,
                                null
                        )
                )
        );
        assertFalse(
                new ExpressionInfo("set(int[] test.expression.Target.type4)", NAMESPACE).getExpression().match(
                        new ExpressionContext(
                                PointcutType.SET,
                                type4,
                                null
                        )
                )
        );
    }

    // ============ field name test =============
    public void testFieldName() throws Exception {
        assertTrue(
                new ExpressionInfo("set(int test.expression.Target.__field$Name1)", NAMESPACE).getExpression().match(
                        new ExpressionContext(
                                PointcutType.SET,
                                _field$Name1,
                                null
                        )
                )
        );
        assertFalse(
                new ExpressionInfo("set(int test.expression.Target.field$Name1)", NAMESPACE).getExpression().match(
                        new ExpressionContext(
                                PointcutType.SET,
                                _field$Name1,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("set(int test.expression.Target.__*$Name*)", NAMESPACE).getExpression().match(
                        new ExpressionContext(
                                PointcutType.SET,
                                _field$Name1,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("set(int test.expression.Target.__field*)", NAMESPACE).getExpression().match(
                        new ExpressionContext(
                                PointcutType.SET,
                                _field$Name1,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("set(int test.expression.*.__field$Name1)", NAMESPACE).getExpression().match(
                        new ExpressionContext(
                                PointcutType.SET,
                                _field$Name1,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("set(int test..Target.__field$Name1)", NAMESPACE).getExpression().match(
                        new ExpressionContext(
                                PointcutType.SET,
                                _field$Name1,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("set(int test..*.__field$Name1)", NAMESPACE).getExpression().match(
                        new ExpressionContext(
                                PointcutType.SET,
                                _field$Name1,
                                null
                        )
                )
        );
        assertFalse(
                new ExpressionInfo("set(int test..*.__fieldName1)", NAMESPACE).getExpression().match(
                        new ExpressionContext(
                                PointcutType.SET,
                                _field$Name1,
                                null
                        )
                )
        );
    }

    // ============ field attribute test =============
    public void testFieldAttributes1() throws Exception {
        assertTrue(
                new ExpressionInfo("set(int test.expression.Target.attribute1)", NAMESPACE).getExpression().match(
                        new ExpressionContext(
                                PointcutType.SET,
                                attribute1,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("set(@ReadOnly int test.expression.Target.attribute1)", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.SET,
                                attribute1,
                                null
                        )
                )
        );
        assertFalse(
                new ExpressionInfo("set(@Read int test.expression.Target.attribute1)", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.SET,
                                attribute1,
                                null
                        )
                )
        );
        assertFalse(
                new ExpressionInfo("set(@ReadOnly @ReadWrite int test.expression.Target.attribute1)", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.SET,
                                attribute1,
                                null
                        )
                )
        );
    }

    // ============ class modifier test =============
    public void testClassModifier() throws Exception {
        assertTrue(
                new ExpressionInfo("within(test.expression.Target)", NAMESPACE).getExpression().match(
                        new ExpressionContext(
                                PointcutType.HANDLER,
                                s_declaringType,
                                s_declaringType
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("within(public test.expression.Target)", NAMESPACE).getExpression().match(
                        new ExpressionContext(
                                PointcutType.HANDLER,
                                s_declaringType,
                                s_declaringType
                        )
                )
        );
        assertFalse(
                new ExpressionInfo("within(protected test.expression.Target)", NAMESPACE).getExpression().match(
                        new ExpressionContext(
                                PointcutType.HANDLER,
                                s_declaringType,
                                s_declaringType
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("within(final public test.expression.Target)", NAMESPACE).getExpression().match(
                        new ExpressionContext(
                                PointcutType.HANDLER,
                                s_declaringType,
                                s_declaringType
                        )
                )
        );

        //        assertFalse(
        //                new ExpressionInfo( "within(abstract test.expression.Target)", NAMESPACE).
        //                getExpression().parse(new ExpressionContext( PointcutType.HANDLER, klass, klass))
        //
        //        );
    }

    // ============ class type test =============
    public void testClassType() throws Exception {
        assertTrue(
                new ExpressionInfo("within(test.expression.Target)", NAMESPACE).getExpression().match(
                        new ExpressionContext(
                                PointcutType.ANY,
                                s_declaringType,
                                s_declaringType
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("within(*)", NAMESPACE).getExpression().match(
                        new ExpressionContext(
                                PointcutType.HANDLER,
                                s_declaringType,
                                s_declaringType
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("within(..)", NAMESPACE).getExpression().match(
                        new ExpressionContext(
                                PointcutType.HANDLER,
                                s_declaringType,
                                s_declaringType
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("within(public *)", NAMESPACE).getExpression().match(
                        new ExpressionContext(
                                PointcutType.HANDLER,
                                s_declaringType,
                                s_declaringType
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("within(test.*.Target)", NAMESPACE).getExpression().match(
                        new ExpressionContext(
                                PointcutType.HANDLER,
                                s_declaringType,
                                s_declaringType
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("within(test.expression.*)", NAMESPACE).getExpression().match(
                        new ExpressionContext(
                                PointcutType.HANDLER,
                                s_declaringType,
                                s_declaringType
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("within(test.expression.Tar*)", NAMESPACE).getExpression().match(
                        new ExpressionContext(
                                PointcutType.HANDLER,
                                s_declaringType,
                                s_declaringType
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("within(test.expression.T*et)", NAMESPACE).getExpression().match(
                        new ExpressionContext(
                                PointcutType.HANDLER,
                                s_declaringType,
                                s_declaringType
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("within(test..*)", NAMESPACE).getExpression().match(
                        new ExpressionContext(
                                PointcutType.HANDLER,
                                s_declaringType,
                                s_declaringType
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("within(*.expression.*)", NAMESPACE).getExpression().match(
                        new ExpressionContext(
                                PointcutType.HANDLER,
                                s_declaringType,
                                s_declaringType
                        )
                )
        );
        assertFalse(
                new ExpressionInfo("within(test.expression.target)", NAMESPACE).getExpression().match(
                        new ExpressionContext(
                                PointcutType.HANDLER,
                                s_declaringType,
                                s_declaringType
                        )
                )
        );
        assertFalse(
                new ExpressionInfo("within(test.expression.Targett)", NAMESPACE).getExpression().match(
                        new ExpressionContext(
                                PointcutType.HANDLER,
                                s_declaringType,
                                s_declaringType
                        )
                )
        );
        assertFalse(
                new ExpressionInfo("within(test.*.*.Target)", NAMESPACE).getExpression().match(
                        new ExpressionContext(
                                PointcutType.HANDLER,
                                s_declaringType,
                                s_declaringType
                        )
                )
        );
    }

    // ============ class attribute test =============
    public void testClassAttribute() throws Exception {
        assertTrue(
                new ExpressionInfo("within(test.expression.Target)", NAMESPACE).getExpression().match(
                        new ExpressionContext(
                                PointcutType.HANDLER,
                                s_declaringType,
                                s_declaringType
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("within(@Serializable test.expression.Target)", NAMESPACE).getExpression().match(
                        new ExpressionContext(
                                PointcutType.HANDLER,
                                s_declaringType,
                                s_declaringType
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("within(@Serializable public final test.expression.Target)", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.HANDLER,
                                s_declaringType,
                                s_declaringType
                        )
                )
        );
        assertFalse(
                new ExpressionInfo("within(@Serializable @Dummy test.expression.Target)", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.HANDLER,
                                s_declaringType,
                                s_declaringType
                        )
                )
        );
    }

    // ============ pointcut type tests =============
    public void testPointcutTypes() throws Exception {
        MethodInfo method = JavaMethodInfo.getMethodInfo(
                Target.class.getDeclaredMethod("modifiers1", new Class[]{})
        );
        FieldInfo field = JavaFieldInfo.getFieldInfo(Target.class.getDeclaredField("modifier1"));
        assertTrue(
                new ExpressionInfo("execution(void test.expression.Target.modifiers1())", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.EXECUTION,
                                method,
                                null
                        )
                )
        );
        assertFalse(
                new ExpressionInfo("execution(void test.expression.Target.modifiers1())", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                method,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("set(int test.expression.Target.modifier1)", NAMESPACE).getExpression().match(
                        new ExpressionContext(
                                PointcutType.SET,
                                field,
                                null
                        )
                )
        );
        assertFalse(
                new ExpressionInfo("get(int test.expression.Target.modifier1)", NAMESPACE).getExpression().match(
                        new ExpressionContext(
                                PointcutType.SET,
                                field,
                                null
                        )
                )
        );
        try {
            assertFalse(
                    new ExpressionInfo("set(int test.expression.Target.modifier1())", NAMESPACE).getExpression()
                    .match(
                            new ExpressionContext(
                                    PointcutType.SET,
                                    method,
                                    null
                            )
                    )
            );
        } catch (Throwable e) {
            return;
        }
        fail("expected exception");
    }

    // ============ advised class tests =============
    public void testAdvisedClassExpression() throws Exception {
        ClassInfo otherType = JavaClassInfo.getClassInfo(String.class);
        assertFalse(
                new ExpressionInfo("execution(void test.expression.Target.modifiers1())", NAMESPACE).getAdvisedClassFilterExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.ANY,
                                otherType,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("execution(void test.expression.Target.modifiers1())", NAMESPACE).getAdvisedClassFilterExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.ANY,
                                s_declaringType,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("call(void test.expression.Target.modifiers1())", NAMESPACE).getAdvisedClassFilterExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.ANY,
                                s_declaringType,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("set(int test.expression.Target.modifier1)", NAMESPACE).getAdvisedClassFilterExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.ANY,
                                s_declaringType,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("get(int test.expression.Target.modifier1)", NAMESPACE).getAdvisedClassFilterExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.ANY,
                                s_declaringType,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("handler(java.lang.Exception) && within(test.expression.Target)", NAMESPACE).getAdvisedClassFilterExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.ANY,
                                s_declaringType,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("handler(java.lang.Exception) && within(test.expression.Target)", NAMESPACE).getAdvisedClassFilterExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.ANY,
                                s_declaringType,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo(
                        "handler(java.lang.Exception) && withincode(void test.expression.Target.modifiers1())",
                        NAMESPACE
                ).getAdvisedClassFilterExpression().match(
                        new ExpressionContext(
                                PointcutType.ANY,
                                s_declaringType,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo(
                        "handler(java.lang.Exception) && NOT withincode(void test.expression.Target.modifiers1())",
                        NAMESPACE
                ).getAdvisedClassFilterExpression().match(
                        new ExpressionContext(
                                PointcutType.ANY,
                                otherType,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo(
                        "call(void test.expression.Dummy.modifiers1()) && within(test.expression.Target)",
                        NAMESPACE
                ).getAdvisedClassFilterExpression().match(
                        new ExpressionContext(
                                PointcutType.ANY,
                                s_declaringType,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo(
                        "call(void test.expression.Dummy.modifiers1()) && withincode(void test.expression.Target.modifiers1())",
                        NAMESPACE
                ).getAdvisedClassFilterExpression().match(
                        new ExpressionContext(
                                PointcutType.ANY,
                                s_declaringType,
                                null
                        )
                )
        );
    }

    public void testAdvisedCflowClassExpression() throws Exception {
        ClassInfo otherType = JavaClassInfo.getClassInfo(String.class);
        s_namespace.addExpressionInfo(
                "string", new ExpressionInfo("execution(void java.lang.String.*(..))", NAMESPACE)
        );
        s_namespace.addExpressionInfo(
                "target",
                new ExpressionInfo("execution(* test.expression.Target.*(..))", NAMESPACE)
        );
        s_namespace.addExpressionInfo(
                "cflowString",
                new ExpressionInfo("cflow(execution(void java.lang.String.*(..)))", NAMESPACE)
        );
        s_namespace.addExpressionInfo(
                "cflowTarget",
                new ExpressionInfo(
                        "cflow(call(void test.expression.Target.modifiers3()) && withincode(void test.expression.Target.*(..)))",
                        NAMESPACE
                )
        );
        assertFalse(
                new ExpressionInfo("string && cflowString", NAMESPACE).getAdvisedCflowClassFilterExpression().match(
                        new ExpressionContext(
                                PointcutType.ANY,
                                s_declaringType,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("string && cflowString", NAMESPACE).getAdvisedCflowClassFilterExpression().match(
                        new ExpressionContext(
                                PointcutType.ANY,
                                otherType,
                                null
                        )
                )
        );
        assertFalse(
                new ExpressionInfo("target && cflowString", NAMESPACE).getAdvisedCflowClassFilterExpression().match(
                        new ExpressionContext(
                                PointcutType.ANY,
                                s_declaringType,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("target && cflowString", NAMESPACE).getAdvisedCflowClassFilterExpression().match(
                        new ExpressionContext(
                                PointcutType.ANY,
                                otherType,
                                null
                        )
                )
        );
        assertFalse(
                new ExpressionInfo("string && cflowTarget", NAMESPACE).getAdvisedCflowClassFilterExpression().match(
                        new ExpressionContext(
                                PointcutType.ANY,
                                otherType,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("string && cflowTarget", NAMESPACE).getAdvisedCflowClassFilterExpression().match(
                        new ExpressionContext(
                                PointcutType.ANY,
                                s_declaringType,
                                null
                        )
                )
        );
        assertFalse(
                new ExpressionInfo("target && cflowTarget", NAMESPACE).getAdvisedCflowClassFilterExpression().match(
                        new ExpressionContext(
                                PointcutType.ANY,
                                otherType,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("target && cflowTarget", NAMESPACE).getAdvisedCflowClassFilterExpression().match(
                        new ExpressionContext(
                                PointcutType.ANY,
                                s_declaringType,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo(
                        "execution(void test.expression.Dummy.modifiers1()) && cflow(execution(void test.expression.Target.modifiers1()))",
                        NAMESPACE
                ).getAdvisedCflowClassFilterExpression().match(
                        new ExpressionContext(
                                PointcutType.ANY,
                                s_declaringType,
                                null
                        )
                )
        );
        assertFalse(
                new ExpressionInfo(
                        "execution(void test.expression.Dummy.modifiers1()) && cflow(execution(void test.expression.Target.modifiers1()))",
                        NAMESPACE
                ).getAdvisedCflowClassFilterExpression().match(
                        new ExpressionContext(
                                PointcutType.ANY,
                                otherType,
                                null
                        )
                )
        );
        assertFalse(
                new ExpressionInfo("cflow(execution(void test.expression.Target.modifiers1()))", NAMESPACE).getAdvisedCflowClassFilterExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.ANY,
                                otherType,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("cflow(call(void test.expression.Target.modifiers1()))", NAMESPACE).getAdvisedCflowClassFilterExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.ANY,
                                s_declaringType,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo(
                        "cflow(execution(void test.expression.Target.modifiers1())) && within(test.expression.Target)",
                        NAMESPACE
                ).getAdvisedCflowClassFilterExpression().match(
                        new ExpressionContext(
                                PointcutType.ANY,
                                s_declaringType,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo(
                        "within(test.expression.Target) && cflow(call(void test.expression.T.modifiers1()))",
                        NAMESPACE
                ).getAdvisedCflowClassFilterExpression().match(
                        new ExpressionContext(
                                PointcutType.ANY,
                                s_declaringType,
                                null
                        )
                )
        );
        assertFalse(
                new ExpressionInfo(
                        "cflow(within(test.expression.T) && call(void test.expression.T.modifiers1()))",
                        NAMESPACE
                ).getAdvisedCflowClassFilterExpression().match(
                        new ExpressionContext(
                                PointcutType.ANY,
                                s_declaringType,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo(
                        "cflow(!within(test.expression.T) && call(void test.expression.Target.modifiers1()))",
                        NAMESPACE
                ).getAdvisedCflowClassFilterExpression().match(
                        new ExpressionContext(
                                PointcutType.ANY,
                                s_declaringType,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo(
                        "cflow(call(void test.expression.Target.modifiers1()) && NOT withincode(void test.expression.Target.modifiers1()))",
                        NAMESPACE
                ).getAdvisedCflowClassFilterExpression().match(
                        new ExpressionContext(
                                PointcutType.ANY,
                                otherType,
                                null
                        )
                )
        );
    }

    // ============ cflow type tests =============
    public void testFindCflowPointcut() throws Exception {
        MethodInfo method1 = JavaMethodInfo.getMethodInfo(
                Target.class.getDeclaredMethod("modifiers1", new Class[]{})
        );
        MethodInfo method2 = JavaMethodInfo.getMethodInfo(
                Target.class.getDeclaredMethod("modifiers2", new Class[]{})
        );
        MethodInfo method3 = JavaMethodInfo.getMethodInfo(
                Target.class.getDeclaredMethod("modifiers3", new Class[]{})
        );
        s_namespace.addExpressionInfo(
                "pc1",
                new ExpressionInfo(
                        "execution(void test.expression.Target.modifiers2())",
                        NAMESPACE
                )
        );
        s_namespace.addExpressionInfo(
                "pc2",
                new ExpressionInfo(
                        "execution(void test.expression.Target.modifiers3())",
                        NAMESPACE
                )
        );
        s_namespace.addExpressionInfo(
                "cflowPC",
                new ExpressionInfo(
                        "cflow(call(void test.expression.Target.modifiers3()) AND within(test.expression.*))",
                        NAMESPACE
                )
        );
        assertTrue(
                new ExpressionInfo("cflow(execution(void test.expression.Target.modifiers1()))", NAMESPACE).getCflowExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.EXECUTION,
                                method1,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo(
                        "execution(void test.expression.Target.modifiers2()) && cflow(execution(void test.expression.Target.modifiers1()))",
                        NAMESPACE
                ).hasCflowPointcut()
        );
        assertTrue(
                new ExpressionInfo(
                        "execution(void test.expression.Target.modifiers2()) && cflow(execution(void test.expression.Target.modifiers1()))",
                        NAMESPACE
                ).hasCflowPointcut()
        );
        assertTrue(
                new ExpressionInfo(
                        "cflow(execution(void test.expression.Target.modifiers1())) && execution(void test.expression.Target.modifiers2())",
                        NAMESPACE
                ).hasCflowPointcut()
        );
        assertTrue(new ExpressionInfo("cflowPC && pc1", NAMESPACE).hasCflowPointcut());
        assertTrue(new ExpressionInfo("pc1 && cflowPC", NAMESPACE).hasCflowPointcut());
        assertTrue(new ExpressionInfo("cflow(pc2) && pc1", NAMESPACE).hasCflowPointcut());
        assertTrue(new ExpressionInfo("pc1 && cflow(pc2)", NAMESPACE).hasCflowPointcut());
        assertTrue(
                new ExpressionInfo(
                        "pc2 && cflow(pc1 || pc2 || call(void test.expression.Target.modifiers1()))",
                        NAMESPACE
                ).hasCflowPointcut()
        );
        assertTrue(
                new ExpressionInfo(
                        "cflow(pc1 || pc2 || call(void test.expression.Target.modifiers1())) AND pc1",
                        NAMESPACE
                ).hasCflowPointcut()
        );
        assertTrue(
                new ExpressionInfo(
                        "cflow(pc1 || call(void test.expression.Target.modifiers1())) && (execution(void test.expression.Target.modifiers3()) || pc1)",
                        NAMESPACE
                ).hasCflowPointcut()
        );
        assertTrue(
                new ExpressionInfo(
                        "cflow(execution(void test.expression.Target.modifiers1())) && execution(void test.expression.Target.modifiers2())",
                        NAMESPACE
                ).hasCflowPointcut()
        );
        assertTrue(
                new ExpressionInfo("cflow(pc1) && execution(void test.expression.Target.modifiers3())", NAMESPACE)
                .hasCflowPointcut()
        );
        assertTrue(
                new ExpressionInfo(
                        "cflow(call(void test.expression.Target.modifiers1())) && execution(void test.expression.Target.modifiers1())",
                        NAMESPACE
                ).hasCflowPointcut()
        );
        assertTrue(
                new ExpressionInfo(
                        "cflow(call(void test.expression.Target.modifiers1())) || execution(void test.expression.Target.modifiers1())",
                        NAMESPACE
                ).hasCflowPointcut()
        );
        assertFalse(
                new ExpressionInfo(
                        "call(void test.expression.Target.modifiers1()) || execution(void test.expression.Target.modifiers1())",
                        NAMESPACE
                ).hasCflowPointcut()
        );
        assertFalse(
                new ExpressionInfo(
                        "call(void test.expression.Target.modifiers1()) && execution(void test.expression.Target.modifiers1())",
                        NAMESPACE
                ).hasCflowPointcut()
        );
    }

    public void testCflowTypes() throws Exception {
        MethodInfo method1 = JavaMethodInfo.getMethodInfo(
                Target.class.getDeclaredMethod("modifiers1", new Class[]{})
        );
        MethodInfo method2 = JavaMethodInfo.getMethodInfo(
                Target.class.getDeclaredMethod("modifiers2", new Class[]{})
        );
        MethodInfo method3 = JavaMethodInfo.getMethodInfo(
                Target.class.getDeclaredMethod("modifiers3", new Class[]{})
        );
        s_namespace.addExpressionInfo(
                "pc1",
                new ExpressionInfo(
                        "execution(void test.expression.Target.modifiers2())",
                        NAMESPACE
                )
        );
        s_namespace.addExpressionInfo(
                "pc2",
                new ExpressionInfo(
                        "execution(void test.expression.Target.modifiers3())",
                        NAMESPACE
                )
        );
        s_namespace.addExpressionInfo(
                "cflowPC",
                new ExpressionInfo(
                        "cflow(call(void test.expression.Target.modifiers3()) AND within(test.expression.*))",
                        NAMESPACE
                )
        );
        assertTrue(
                new ExpressionInfo("cflow(execution(void test.expression.Target.modifiers1()))", NAMESPACE).getCflowExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.EXECUTION,
                                method1,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo(
                        "execution(void test.expression.Target.modifiers2()) && cflow(execution(void test.expression.Target.modifiers1()))",
                        NAMESPACE
                ).getCflowExpression().match(
                        new ExpressionContext(
                                PointcutType.EXECUTION,
                                method1, null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo(
                        "execution(void test.expression.Target.modifiers2()) && cflow(execution(void test.expression.Target.modifiers1()))",
                        NAMESPACE
                ).getCflowExpression().match(
                        new ExpressionContext(
                                PointcutType.EXECUTION,
                                method1, null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo(
                        "cflow(execution(void test.expression.Target.modifiers1())) && execution(void test.expression.Target.modifiers2())",
                        NAMESPACE
                ).getCflowExpression().match(
                        new ExpressionContext(
                                PointcutType.EXECUTION,
                                method1, null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("cflowPC && pc1", NAMESPACE).getCflowExpression().match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                method3,
                                s_declaringType
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("pc1 && cflowPC", NAMESPACE).getCflowExpression().match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                method3,
                                s_declaringType
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("cflow(pc2) && pc1", NAMESPACE).getCflowExpression().match(
                        new ExpressionContext(
                                PointcutType.EXECUTION,
                                method3,
                                s_declaringType
                        )
                )
        );
        assertFalse(
                new ExpressionInfo("pc1 && cflow(pc2)", NAMESPACE).getCflowExpression().match(
                        new ExpressionContext(
                                PointcutType.EXECUTION,
                                method2,
                                s_declaringType
                        )
                )
        );
        assertTrue(
                new ExpressionInfo(
                        "pc2 && cflow(pc1 || pc2 || call(void test.expression.Target.modifiers1()))",
                        NAMESPACE
                ).getCflowExpression().match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                method1, null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo(
                        "cflow(pc1 || pc2 || call(void test.expression.Target.modifiers1())) AND pc1",
                        NAMESPACE
                ).getCflowExpression().match(
                        new ExpressionContext(
                                PointcutType.EXECUTION,
                                method3, null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo(
                        "cflow(pc1 || call(void test.expression.Target.modifiers1())) && (execution(void test.expression.Target.modifiers3()) || pc1)",
                        NAMESPACE
                ).getCflowExpression().match(
                        new ExpressionContext(
                                PointcutType.EXECUTION,
                                method2, null
                        )
                )
        );
        assertFalse(
                new ExpressionInfo(
                        "cflow(execution(void test.expression.Target.modifiers1())) && execution(void test.expression.Target.modifiers2())",
                        NAMESPACE
                ).getCflowExpression().match(
                        new ExpressionContext(
                                PointcutType.EXECUTION,
                                method2, null
                        )
                )
        );
        assertFalse(
                new ExpressionInfo("cflow(pc1) && execution(void test.expression.Target.modifiers3())", NAMESPACE).getCflowExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.EXECUTION,
                                method3,
                                null
                        )
                )
        );
        assertFalse(
                new ExpressionInfo(
                        "cflow(call(void test.expression.Target.modifiers1())) && execution(void test.expression.Target.modifiers1())",
                        NAMESPACE
                ).getCflowExpression().match(
                        new ExpressionContext(
                                PointcutType.EXECUTION,
                                method1, null
                        )
                )
        );
        assertFalse(
                new ExpressionInfo(
                        "cflow(call(void test.expression.Target.modifiers1())) || execution(void test.expression.Target.modifiers1())",
                        NAMESPACE
                ).getCflowExpression().match(
                        new ExpressionContext(
                                PointcutType.EXECUTION,
                                method1, null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo(
                        "execution(void test.expression.Target.modifiers2()) AND NOT cflow(call(void test.expression.Target.modifiers1()))",
                        NAMESPACE
                ).getExpression().match(
                        new ExpressionContext(
                                PointcutType.EXECUTION,
                                method2, null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo(
                        "execution(void test.expression.Target.modifiers2()) AND NOT cflow(call(void test.expression.Target.modifiers1()))",
                        NAMESPACE
                ).getCflowExpression().match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                method1, null
                        )
                )
        );
    }

    public void testCflowBelowTypes() throws Exception {
        MethodInfo method1 = JavaMethodInfo.getMethodInfo(
                Target.class.getDeclaredMethod("modifiers1", new Class[]{})
        );
        MethodInfo method2 = JavaMethodInfo.getMethodInfo(
                Target.class.getDeclaredMethod("modifiers2", new Class[]{})
        );
        MethodInfo method3 = JavaMethodInfo.getMethodInfo(
                Target.class.getDeclaredMethod("modifiers3", new Class[]{})
        );
        s_namespace.addExpressionInfo(
                "pc1",
                new ExpressionInfo(
                        "execution(void test.expression.Target.modifiers2())",
                        NAMESPACE
                )
        );
        s_namespace.addExpressionInfo(
                "pc2",
                new ExpressionInfo(
                        "execution(void test.expression.Target.modifiers3())",
                        NAMESPACE
                )
        );
        assertTrue(
                new ExpressionInfo("cflowbelow(execution(void test.expression.Target.modifiers1()))", NAMESPACE).getCflowExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.EXECUTION,
                                method1,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo(
                        "cflowbelow(pc1 || pc2 || call(void test.expression.Target.modifiers1()))",
                        NAMESPACE
                ).getCflowExpression().match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                method1, null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo(
                        "cflowbelow(pc1 || pc2 || call(void test.expression.Target.modifiers1()))",
                        NAMESPACE
                ).getCflowExpression().match(
                        new ExpressionContext(
                                PointcutType.EXECUTION,
                                method2, null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo(
                        "cflowbelow(pc1 || pc2 || call(void test.expression.Target.modifiers1()))",
                        NAMESPACE
                ).getCflowExpression().match(
                        new ExpressionContext(
                                PointcutType.EXECUTION,
                                method3, null
                        )
                )
        );
    }

    // ============ within type tests =============
    public void testWithinType() throws Exception {
        ClassInfo klass = JavaClassInfo.getClassInfo(Target.class);
        MethodInfo method = JavaMethodInfo.getMethodInfo(
                Target.class.getDeclaredMethod("modifiers1", new Class[]{})
        );
        assertTrue(
                new ExpressionInfo(
                        "execution(void test.expression.Target.modifiers1()) AND within(test.expression.Target)",
                        NAMESPACE
                ).getExpression().match(
                        new ExpressionContext(
                                PointcutType.EXECUTION,
                                method, s_declaringType
                        )
                )
        );
        assertTrue(
                new ExpressionInfo(
                        "call(void test.expression.Target.modifiers1()) AND within(test.expression.Target)",
                        NAMESPACE
                ).getExpression().match(
                        new ExpressionContext(
                                PointcutType.CALL, method,
                                s_declaringType
                        )
                )
        );
        assertFalse(
                new ExpressionInfo(
                        "call(void test.expression.Target.modifiers1()) AND NOT within(test.expression.Target)",
                        NAMESPACE
                ).getExpression().match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                method, s_declaringType
                        )
                )
        );
    }

    public void testWithinCodeType() throws Exception {
        MethodInfo method = JavaMethodInfo.getMethodInfo(
                Target.class.getDeclaredMethod("modifiers1", new Class[]{})
        );
        assertTrue(
                new ExpressionInfo(
                        "execution(void test.expression.Target.modifiers1()) AND withincode(void test.expression.Target.modifiers1())",
                        NAMESPACE
                ).getExpression().match(
                        new ExpressionContext(
                                PointcutType.EXECUTION,
                                method, method
                        )
                )
        );
        assertTrue(
                new ExpressionInfo(
                        "call(void test.expression.Target.modifiers1()) AND withincode(void test.expression.Target.modifiers1())",
                        NAMESPACE
                ).getExpression().match(
                        new ExpressionContext(
                                PointcutType.CALL, method,
                                method
                        )
                )
        );
        assertFalse(
                new ExpressionInfo(
                        "call(void test.expression.Target.modifiers1()) AND NOT withincode(void test.expression.Target.modifiers1())",
                        NAMESPACE
                ).getExpression().match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                method, method
                        )
                )
        );
    }

    // ============ pointcut ref tests =============
    public void testPointcutReference() throws Exception {
        s_namespace.addExpressionInfo(
                "pc1",
                new ExpressionInfo(
                        "execution(void test.expression.Target.modifiers2())",
                        NAMESPACE
                )
        );
        s_namespace.addExpressionInfo(
                "pc2",
                new ExpressionInfo(
                        "execution(void test.expression.Target.modifiers3())",
                        NAMESPACE
                )
        );
        assertTrue(
                new ExpressionInfo("execution(void test.expression.Target.modifiers1()) || pc1", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.EXECUTION,
                                modifiers1,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("execution(void test.expression.Target.modifiers1()) || pc1", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.EXECUTION,
                                modifiers2,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("execution(void test.expression.Target.modifiers1()) || pc2", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.EXECUTION,
                                modifiers3,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("execution(void test.expression.Target.modifiers1()) || pc1 || pc2", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.EXECUTION,
                                modifiers1,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("execution(void test.expression.Target.modifiers1()) || pc1 || pc2", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.EXECUTION,
                                modifiers2,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("execution(void test.expression.Target.modifiers1()) || pc1 || pc2 ", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.EXECUTION,
                                modifiers3,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("execution(void test.expression.Target.modifiers1()) || pc2 || pc1", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.EXECUTION,
                                modifiers1,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("execution(void test.expression.Target.modifiers1()) || pc2 || pc1", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.EXECUTION,
                                modifiers2,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("execution(void test.expression.Target.modifiers1()) || pc2 || pc1 ", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.EXECUTION,
                                modifiers3,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("pc1 || pc2 || execution(void test.expression.Target.modifiers1())", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.EXECUTION,
                                modifiers1,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("pc1 || pc2 || execution(void test.expression.Target.modifiers1())", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.EXECUTION,
                                modifiers2,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("pc1 || pc2 || execution(void test.expression.Target.modifiers1())", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.EXECUTION,
                                modifiers3,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo(
                        "(execution(void test.expression.Target.modifiers1()) || pc1 || pc2) AND within(test.expression.Target)",
                        NAMESPACE
                ).getExpression().match(
                        new ExpressionContext(
                                PointcutType.EXECUTION,
                                modifiers1, s_declaringType
                        )
                )
        );
        assertTrue(
                new ExpressionInfo(
                        "(execution(void test.expression.Target.modifiers1()) || pc1 || pc2) AND within(test.expression.Target)",
                        NAMESPACE
                ).getExpression().match(
                        new ExpressionContext(
                                PointcutType.EXECUTION,
                                modifiers2, s_declaringType
                        )
                )
        );
        assertTrue(
                new ExpressionInfo(
                        "(execution(void test.expression.Target.modifiers1()) || pc1 || pc2) AND within(test.expression.Target)",
                        NAMESPACE
                ).getExpression().match(
                        new ExpressionContext(
                                PointcutType.EXECUTION,
                                modifiers3, s_declaringType
                        )
                )
        );
        assertTrue(
                new ExpressionInfo(
                        "within(test.expression.Target) && (pc1 || pc2 || execution(void test.expression.Target.modifiers1()))",
                        NAMESPACE
                ).getExpression().match(
                        new ExpressionContext(
                                PointcutType.EXECUTION,
                                modifiers1, s_declaringType
                        )
                )
        );
        assertTrue(
                new ExpressionInfo(
                        "within(test.expression.Target) && (pc2 || pc1 || execution(void test.expression.Target.modifiers1()))",
                        NAMESPACE
                ).getExpression().match(
                        new ExpressionContext(
                                PointcutType.EXECUTION,
                                modifiers2, s_declaringType
                        )
                )
        );
        assertTrue(
                new ExpressionInfo(
                        "within(test.expression.Target) && (pc1 || execution(void test.expression.Target.modifiers1()) || pc2)",
                        NAMESPACE
                ).getExpression().match(
                        new ExpressionContext(
                                PointcutType.EXECUTION,
                                modifiers3, s_declaringType
                        )
                )
        );
    }

    // ============ pointcut ref tests =============
    public void testPointcutReferenceOutsideScope() throws Exception {
        String namespace1 = "Lib";
        String namespace2 = "org.moneymaker.Util";
        ExpressionNamespace.getNamespace(namespace1).addExpressionInfo(
                "pc1",
                new ExpressionInfo(
                        "execution(void test.expression.Target.modifiers1())",
                        namespace1
                )
        );
        ExpressionNamespace.getNamespace(namespace2).addExpressionInfo(
                "pc2",
                new ExpressionInfo(
                        "execution(void test.expression.Target.modifiers2())",
                        namespace2
                )
        );
        assertTrue(
                new ExpressionInfo("execution(* foo.bar.*()) || Lib.pc1", NAMESPACE).getExpression().match(
                        new ExpressionContext(
                                PointcutType.EXECUTION,
                                modifiers1,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("Lib.pc1 || execution(* foo.bar.*())", NAMESPACE).getExpression().match(
                        new ExpressionContext(
                                PointcutType.EXECUTION,
                                modifiers1,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("org.moneymaker.Util.pc2 || Lib.pc1", NAMESPACE).getExpression().match(
                        new ExpressionContext(
                                PointcutType.EXECUTION,
                                modifiers2,
                                null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo("Lib.pc1 || org.moneymaker.Util.pc2", NAMESPACE).getExpression().match(
                        new ExpressionContext(
                                PointcutType.EXECUTION,
                                modifiers2,
                                null
                        )
                )
        );
        assertFalse(
                new ExpressionInfo("execution(void test.expression.Target.modifiers1()) || Lib.pc1", NAMESPACE).getExpression()
                .match(
                        new ExpressionContext(
                                PointcutType.EXECUTION,
                                modifiers2,
                                null
                        )
                )
        );
    }

    // ============ and tests =============
    public void testAnd() throws Exception {
        assertTrue(
                new ExpressionInfo(
                        "execution(void test.expression.Target.modifiers1()) AND within(test.expression.Target)",
                        NAMESPACE
                ).getExpression().match(
                        new ExpressionContext(
                                PointcutType.EXECUTION,
                                modifiers1, s_declaringType
                        )
                )
        );
        assertFalse(
                new ExpressionInfo(
                        "execution(void test.expression.Target.modifiers1()) AND call(void test.expression.Target.modifiers1())",
                        NAMESPACE
                ).getExpression().match(
                        new ExpressionContext(
                                PointcutType.EXECUTION,
                                modifiers1,
                                s_declaringType
                        )
                )
        );
        assertFalse(
                new ExpressionInfo(
                        "call(void test.expression.Target.modifiers1()) AND set(int test.expression.Target.modifier1)",
                        NAMESPACE
                ).getExpression().match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                modifier1, null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo(
                        "execution(void test.expression.Target.modifiers1()) && within(test.expression.Target)",
                        NAMESPACE
                ).getExpression().match(
                        new ExpressionContext(
                                PointcutType.EXECUTION,
                                modifiers1, s_declaringType
                        )
                )
        );
        assertFalse(
                new ExpressionInfo(
                        "execution(void test.expression.Target.modifiers1()) && call(void test.expression.Target.modifiers1())",
                        NAMESPACE
                ).getExpression().match(
                        new ExpressionContext(
                                PointcutType.EXECUTION,
                                modifiers1,
                                s_declaringType
                        )
                )
        );
        assertFalse(
                new ExpressionInfo(
                        "call(void test.expression.Target.modifiers1()) && set(int test.expression.Target.modifier1)",
                        NAMESPACE
                ).getExpression().match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                modifier1, null
                        )
                )
        );
    }

    // ============ or tests =============
    public void testOr() throws Exception {
        assertTrue(
                new ExpressionInfo(
                        "execution(void test.expression.Target.modifiers1()) OR call(void test.expression.Target.modifiers1())",
                        NAMESPACE
                ).getExpression().match(
                        new ExpressionContext(
                                PointcutType.EXECUTION,
                                modifiers1, null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo(
                        "execution(void test.expression.Target.modifiers1()) OR call(void test.expression.Target.modifiers1())",
                        NAMESPACE
                ).getExpression().match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                modifiers1, null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo(
                        "call(void test.expression.Target.modifiers1()) OR set(int test.expression.Target.modifiers)",
                        NAMESPACE
                ).getExpression().match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                modifiers1, null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo(
                        "set(int test.expression.Target.modifier1) OR call(void test.expression.Target.modifiers1())",
                        NAMESPACE
                ).getExpression().match(
                        new ExpressionContext(
                                PointcutType.SET,
                                modifier1, null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo(
                        "call(void test.expression.Target.modifiers1()) OR set(int test.expression.Target.modifier1)",
                        NAMESPACE
                ).getExpression().match(
                        new ExpressionContext(
                                PointcutType.SET,
                                modifier1, null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo(
                        "execution(void test.expression.Target.modifiers1()) || call(void test.expression.Target.modifiers1())",
                        NAMESPACE
                ).getExpression().match(
                        new ExpressionContext(
                                PointcutType.EXECUTION,
                                modifiers1, null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo(
                        "execution(void test.expression.Target.modifiers1()) || call(void test.expression.Target.modifiers1())",
                        NAMESPACE
                ).getExpression().match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                modifiers1, null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo(
                        "call(void test.expression.Target.modifiers1()) || set(int test.expression.Target.modifier1)",
                        NAMESPACE
                ).getExpression().match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                modifiers1, null
                        )
                )
        );
        assertTrue(
                new ExpressionInfo(
                        "call(void test.expression.Target.modifiers1()) || set(int test.expression.Target.modifier1)",
                        NAMESPACE
                ).getExpression().match(
                        new ExpressionContext(
                                PointcutType.SET,
                                modifier1, null
                        )
                )
        );
    }

    // ============ not tests =============
    public void testNot() throws Exception {
        assertFalse(
                new ExpressionInfo(
                        "execution(void test.expression.Target.modifiers1()) AND NOT within(test.expression.Target)",
                        NAMESPACE
                ).getExpression().match(
                        new ExpressionContext(
                                PointcutType.EXECUTION,
                                modifiers1,
                                s_declaringType
                        )
                )
        );
        assertTrue(
                new ExpressionInfo(
                        "execution(void test.expression.Target.modifiers1()) AND NOT call(void test.expression.Target.modifiers1())",
                        NAMESPACE
                ).getExpression().match(
                        new ExpressionContext(
                                PointcutType.EXECUTION,
                                modifiers1, s_declaringType
                        )
                )
        );
        assertFalse(
                new ExpressionInfo(
                        "call(void test.expression.Target.modifiers1()) AND NOT set(int test.expression.Target.modifier1)",
                        NAMESPACE
                ).getExpression().match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                modifier1, null
                        )
                )
        );
        assertFalse(
                new ExpressionInfo(
                        "execution(void test.expression.Target.modifiers1()) && !within(test.expression.Target)",
                        NAMESPACE
                ).getExpression().match(
                        new ExpressionContext(
                                PointcutType.EXECUTION,
                                modifiers1,
                                s_declaringType
                        )
                )
        );
        assertTrue(
                new ExpressionInfo(
                        "execution(void test.expression.Target.modifiers1()) && !call(void test.expression.Target.modifiers1())",
                        NAMESPACE
                ).getExpression().match(
                        new ExpressionContext(
                                PointcutType.EXECUTION,
                                modifiers1, s_declaringType
                        )
                )
        );
        ExpressionVisitor expression = new ExpressionInfo(
                "execution(void test.expression.Target.modifiers1()) && !call(void test.expression.Target.modifiers3()) && !(call(void test.expression.Target.modifiers1()) || call(void test.expression.Target.modifiers2()))",
                NAMESPACE
        ).getExpression();
        assertTrue(expression.match(new ExpressionContext(PointcutType.EXECUTION, modifiers1, s_declaringType)));
        assertFalse(expression.match(new ExpressionContext(PointcutType.CALL, modifiers1, s_declaringType)));
        assertFalse(expression.match(new ExpressionContext(PointcutType.SET, modifier1, s_declaringType)));
        assertFalse(
                new ExpressionInfo(
                        "call(void test.expression.Target.modifiers1()) && !set(int test.expression.Target.modifier1)",
                        NAMESPACE
                ).getExpression().match(
                        new ExpressionContext(
                                PointcutType.CALL,
                                modifier1, null
                        )
                )
        );
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(ExpressionTest.class);
    }
}
