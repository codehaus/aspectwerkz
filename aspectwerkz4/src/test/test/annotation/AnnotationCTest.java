/**************************************************************************************
 * Copyright (c) Jonas Bon?r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.annotation;

import junit.framework.TestCase;
import org.codehaus.aspectwerkz.annotation.Annotations;
import org.codehaus.aspectwerkz.annotation.UntypedAnnotationProxy;

import java.util.List;
import java.lang.reflect.Method;

/**
 * Note: when using untyped annotation, then the first space character(s) in the value part will be
 * resumed to only one space (untyped     type -> untyped type), due to QDox doclet handling.
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 * @BeforeAction some untype that starts with Before
 * @BeforeAction(other   untyped)
 * @BeforeAction("yet another untyped")
 * @packaged.BeforeAction
 * @Void
 * @Void()
 * @Simple()
 * @Simple(val="foo", s="foo")
 * @DefaultString("hello")
 * @packaged.DefaultString("hello")
 * @Complex(i=3, ls={1l,2l,6L},  klass=java.lang.String.class)
 * @Untyped
 * @Untyped hello
 * @Untyped "hello"
 * @Untyped ("hello") - see the space here !
 * @Untyped (hello) - see the space here !
 * @Untyped("preserved hello")
 */
public class AnnotationCTest extends TestCase {

    public void testClassAnnotation() {
        Class me = AnnotationCTest.class;

        List voids = Annotations.getAnnotations("Void", me);
        assertEquals(2, voids.size());

        List simples = Annotations.getAnnotations("Simple", me);
        assertEquals(2, simples.size());
        StringBuffer all = new StringBuffer();
        for (int i = 0; i < simples.size(); i++) {
            all.append("[").append(((AnnotationParserTest.Simple) simples.get(i)).s()).append("]");
        }
        String[] lookFor = new String[]{
            "[null]",
            "[foo]"
        };
        for (int i = 0; i < lookFor.length; i++) {
            String s = lookFor[i];
            if (all.indexOf(s) < 0) {
                fail("could not find " + lookFor[i] + " in " + all.toString());
            }
        }

        List beforeActions = Annotations.getAnnotations("BeforeAction", me);
        assertEquals(3, beforeActions.size());
        all = new StringBuffer();
        for (int i = 0; i < beforeActions.size(); i++) {
            all.append("[").append(((UntypedAnnotationProxy)beforeActions.get(i)).getValue()).append("]");
        }
        lookFor = new String[]{
            "[some untype that starts with Before]",
            "[other untyped]",// some space chars have been lost 
            "[yet another untyped]",

        };
        for (int i = 0; i < lookFor.length; i++) {
            String s = lookFor[i];
            if (all.indexOf(s) < 0) {
                fail("could not find " + lookFor[i] + " in " + all.toString());
            }
        }

        assertEquals(
                "hello",
                ((AnnotationParserTest.DefaultString) Annotations.getAnnotation("DefaultString", me)).value()
        );

        assertEquals(
                String.class, ((AnnotationParserTest.Complex) Annotations.getAnnotation("Complex", me)).klass()
        );

        List untypeds = Annotations.getAnnotations("Untyped", me);
        assertEquals(6, untypeds.size());
        all = new StringBuffer();
        for (int i = 0; i < untypeds.size(); i++) {
            all.append("[").append(((AnnotationParserTest.Untyped) untypeds.get(i)).value()).append("]");
        }
        lookFor = new String[]{
            "[]",
            "hello",
            "\"hello\"",
            "(\"hello\") - see the space here !",
            "(hello)",
            "preserved hello"
        };
        for (int i = 0; i < lookFor.length; i++) {
            String s = lookFor[i];
            if (all.indexOf(s) < 0) {
                fail("could not find " + lookFor[i] + " in " + all.toString());
            }
        }
    }

    /**
     * @Void
     * @Void()
     * @Simple()
     * @Simple(val="foo", s="foo")
     * @DefaultString("hello")
     * @Complex(i=3, ls={1l,2l,6L},  klass=java.lang.String.class)
     * @Untyped
     * @Untyped hello
     * @Untyped "hello"
     * @Untyped ("hello") - see the space here !
     * @Untyped (hello) - see the space here !
     */
    public void testMethodAnnotation() throws Throwable {
        Class me = test.annotation.AnnotationCTest.class;
        Method m = me.getDeclaredMethod("testMethodAnnotation", new Class[0]);

        List voids = Annotations.getAnnotations("Void", me);
        assertEquals(2, voids.size());

        List simples = Annotations.getAnnotations("Simple", me);
        assertEquals(2, simples.size());
        StringBuffer all = new StringBuffer();
        for (int i = 0; i < simples.size(); i++) {
            all.append("[").append(((AnnotationParserTest.Simple) simples.get(i)).s()).append("]");
        }
        String[] lookFor = new String[]{
            "[null]",
            "[foo]"
        };
        for (int i = 0; i < lookFor.length; i++) {
            String s = lookFor[i];
            if (all.indexOf(s) < 0) {
                fail("could not find " + lookFor[i] + " in " + all.toString());
            }
        }

        assertEquals(
                "hello",
                ((AnnotationParserTest.DefaultString) Annotations.getAnnotation("DefaultString", me)).value()
        );

        assertEquals(
                String.class, ((AnnotationParserTest.Complex) Annotations.getAnnotation("Complex", me)).klass()
        );

        List untypeds = Annotations.getAnnotations("Untyped", me);
        assertEquals(6, untypeds.size());
        all = new StringBuffer();
        for (int i = 0; i < untypeds.size(); i++) {
            all.append("[").append(((AnnotationParserTest.Untyped) untypeds.get(i)).value()).append("]");
        }
        lookFor = new String[]{
            "[]",
            "hello",
            "\"hello\"",
            "(\"hello\") - see the space here !",
            "(hello)"
        };
        for (int i = 0; i < lookFor.length; i++) {
            String s = lookFor[i];
            if (all.indexOf(s) < 0) {
                fail("could not find " + lookFor[i] + " in " + all.toString());
            }
        }
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(AnnotationCTest.class);
    }
}
