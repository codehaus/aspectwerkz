/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.annotation;

import junit.framework.TestCase;
import org.codehaus.aspectwerkz.reflect.MethodInfo;
import org.codehaus.aspectwerkz.reflect.impl.java.JavaMethodInfo;
import org.codehaus.aspectwerkz.annotation.Annotations;
import org.codehaus.aspectwerkz.annotation.Annotation;
import org.codehaus.aspectwerkz.annotation.AnnotationInfo;

import java.lang.reflect.Method;

import test.expression.ExpressionTest;

/**
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class DefaultValueTest extends TestCase {

    @DefaultedAnnotation
    void annotatedMethod() {
    }

    public MethodInfo getAnnotatedMethod() {
        try {
            Method m = DefaultValueTest.class.getDeclaredMethod("annotatedMethod", new Class[0]);
            MethodInfo mi = JavaMethodInfo.getMethodInfo(m);
            if (mi.getAnnotations().size() <= 0) {
                throw new Error("test corrupted");
            }
            return mi;
        } catch (Throwable t) {
            throw new Error("test corrupted");
        }
    }

    public void testDefaultedAnnotation() {
        AnnotationInfo annI = (AnnotationInfo) getAnnotatedMethod().getAnnotations().get(0);
        DefaultedAnnotation ann = (DefaultedAnnotation) annI.getAnnotation();

        // string
        assertEquals("default", ann.s());

        // primitive array
        int[] is = ann.is();
        int[] defaultIs = {1, 2};
        for (int i = 0; i < is.length; i++) {
            assertEquals(defaultIs[i], is[i]);
        }

        // class
        assertEquals(ReferencedClass.class, ann.klass());

        // nested annotation which has a default itself but whose assigned default differs
        DefaultedAnnotation.NestedDefaultedAnnotation nested = ann.nested();
        assertEquals("default_const", nested.s());

        // nested annotation which is using the default itself
        DefaultedAnnotation.NestedDefaultedAnnotation nested2 = ann.nested2();
        assertEquals("default_nested", nested2.s());
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(DefaultValueTest.class);
    }



}
