/**************************************************************************************
 * Copyright (c) Jonas Bon?r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/       
package test.annotation;

import org.codehaus.aspectwerkz.annotation.expression.ast.AnnotationParser;
import org.codehaus.aspectwerkz.annotation.expression.AnnotationVisitor;
import org.codehaus.aspectwerkz.annotation.Java5AnnotationInvocationHandler;
import junit.framework.TestCase;

import java.util.Map;
import java.util.HashMap;

/**
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class AnnotationParserTest extends TestCase {

    protected static final AnnotationParser s_parser = Helper.getAnnotationParser();

    private Object getElementValue(Object o) {
        Java5AnnotationInvocationHandler.AnnotationElement element = (Java5AnnotationInvocationHandler.AnnotationElement) o;
        return element.resolveValueHolderFrom(AnnotationParserTest.class.getClassLoader());

    }

    private void check(Map elements, String key, Object expected) {
        Object o = elements.get(key);
        if (o == null) {
            fail("No such element - " + key);
        } else {
            assertEquals(expected, getElementValue(o));
        }
    }

    public void testSimple() {
        try {
            Map elements = new HashMap();
            AnnotationVisitor.parse(elements, "@Simple(val=\"foo\")", Simple.class);
            check(elements, "val", "foo");
            AnnotationVisitor.parse(elements, "@Simple(val=\"foo bar\")", Simple.class);
            AnnotationVisitor.parse(elements, "@Simple (val=\"foo bar\")", Simple.class);
            AnnotationVisitor.parse(elements, "@Simple(val=\"foo bar\"       )", Simple.class);

            elements = new HashMap();
            AnnotationVisitor.parse(elements, "@Simple(s=\"foo\")", Simple.class);
            check(elements, "s", "foo");

            AnnotationVisitor.parse(elements, "@Simple(s=\"foo bar\")", Simple.class);
            AnnotationVisitor.parse(elements, "@Simple (s=\"foo bar\")", Simple.class);
            AnnotationVisitor.parse(elements, "@Simple(s=\"foo bar\"       )", Simple.class);

            elements = new HashMap();
            AnnotationVisitor.parse(elements, "@Simple(s=\"foo\", val=\"bar\")", Simple.class);
            check(elements, "s", "foo");
            check(elements, "val", "bar");

            elements = new HashMap();
            AnnotationVisitor.parse(elements, "@Void()", VoidTyped.class);
            assertEquals(0, elements.size());
            AnnotationVisitor.parse(elements, "@Void", VoidTyped.class);
            assertEquals(0, elements.size());
        } catch (Throwable t) {
            fail(t.toString());
        }
    }

    public void testDefault() {
        try {
            Map elements = new HashMap();
            AnnotationVisitor.parse(elements, "@DefaultString(\"foo\")", DefaultString.class);
            check(elements, "value", "foo");

            elements = new HashMap();
            AnnotationVisitor.parse(elements, "@DefaultInt(3)", DefaultInt.class);
            check(elements, "value", new Integer(3));
        } catch (Throwable t) {
            fail(t.toString());
        }
    }

    // note that for correct long type handling, it is mandatory to have the l or L suffix
    public void testComplex() {
        try {
            Map elements = new HashMap();
            AnnotationVisitor.parse(
                    elements, "@Complex(i=3  ls={1l,2l,6L}  klass=java.lang.String.class)", Complex.class
            );
            check(elements, "i", new Integer(3));
            long[] ls = new long[]{1L, 2L, 6L};
            long[] lsGet = (long[]) getElementValue(elements.get("ls"));
            for (int i = 0; i < ls.length; i++) {
                assertEquals(ls[i], lsGet[i]);
            }
            // TODO change when support for LazyClass is there
            check(elements, "klass", String.class);
        } catch (Throwable t) {
            fail(t.toString());
        }
    }

    public void testStringArray() {
        try {
            Map elements = new HashMap();
            AnnotationVisitor.parse(elements, "@StringArray(i=3  ss={\"hello\", \"foo\"})", StringArray.class);
            check(elements, "i", new Integer(3));
            String[] ss = new String[]{"hello", "foo"};
            String[] ssGet = (String[]) getElementValue(elements.get("ss"));
            for (int i = 0; i < ss.length; i++) {
                assertEquals(ss[i], ssGet[i]);

            }
        } catch (Throwable t) {
            fail(t.toString());
        }
    }

    public static class Helper extends AnnotationVisitor {
        public Helper(final Map annotationValues, final Class annotationClass) {
            super(annotationValues, annotationClass);
        }
        public static AnnotationParser getAnnotationParser() {
            return PARSER;
        }
    }

    public static interface VoidTyped {
    }

    public static interface Simple {

        public String val();

        public String s();
    }

    public static interface DefaultString {

        public String value();
    }

    public static interface DefaultInt {

        public int value();
    }


    public static interface Complex {

        public int i();

        public long[] ls();

        public Class klass();

    }

    public static interface StringArray {
        public int i();
        public String[] ss();
    }

    public static interface Untyped {
        public String value();
    }
}
