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
import org.codehaus.aspectwerkz.annotation.AnnotationInfo;

import java.lang.reflect.Method;

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

        // nested annotation which has a default itself but whose assigned default differs
        DefaultedAnnotation.NestedDefaultedAnnotation nested = ann.nested();
        assertEquals("default_const", nested.s());

        // nested annotation which is using the default itself
        DefaultedAnnotation.NestedDefaultedAnnotation nested2 = ann.nested2();
        assertEquals("default_nested", nested2.s());

        //-- so far we should not have triggered any ReferencedClass loading
        System.out.println("----");
        // class
        assertEquals(ReferencedClass.class, ann.klass());

        // class
        assertEquals(ReferencedClass.class, ann.klass2()[0]);
        assertEquals(ReferencedClass.class, ann.klass2()[1]);
    }

    public void testToString() throws Throwable {
        AnnotationInfo annI = (AnnotationInfo) getAnnotatedMethod().getAnnotations().get(0);
        DefaultedAnnotation ann = (DefaultedAnnotation) annI.getAnnotation();

        System.out.println("");
        System.out.println(ann.toString());
        System.out.println(ann.annotationType());


        Method m = DefaultValueTest.class.getDeclaredMethod("annotatedMethod", new Class[0]);
        System.out.println(m);
        java.lang.annotation.Annotation[] anns = m.getAnnotations();
        for (int i = 0; i < anns.length; i++) {
            java.lang.annotation.Annotation annotation = anns[i];
            System.out.println(annotation);
            System.out.println(annotation.annotationType());
        }


    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(DefaultValueTest.class);
    }



}
