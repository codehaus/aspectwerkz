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

import java.util.List;

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
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
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
            all.append("[").append(((AnnotationParserTest.Simple)simples.get(i)).s()).append("]");
        }
        String[] lookFor = new String[]{
            "[null]",
            "[foo]"
        };
        for (int i = 0; i < lookFor.length; i++) {
            String s = lookFor[i];
            if (all.indexOf(s)<0) {
                fail("could not find " + lookFor[i] + " in " + all.toString());
            }
        }

        assertEquals("hello", ((AnnotationParserTest.DefaultString)Annotations.getAnnotation("DefaultString", me)).getValue());

        assertEquals(String.class, ((AnnotationParserTest.Complex)Annotations.getAnnotation("Complex", me)).getKlass());

        List untypeds = Annotations.getAnnotations("Untyped", me);
        assertEquals(5, untypeds.size());
        all = new StringBuffer();
        for (int i = 0; i < untypeds.size(); i++) {
            all.append("[").append(((AnnotationParserTest.Untyped)untypeds.get(i)).getValue()).append("]");
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
            if (all.indexOf(s)<0) {
                fail("could not find " + lookFor[i] + " in " + all.toString());
            }
        }
    }
}
