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
import org.codehaus.aspectwerkz.annotation.TypedAnnotationProxy;
import org.codehaus.aspectwerkz.annotation.UntypedAnnotationProxy;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class AnnotationParserTest extends TestCase {

    protected static final AnnotationParser s_parser = Helper.getAnnotationParser();

    public void testSimple() {
        try {
            Simple annotation = new Simple();
            AnnotationVisitor.parse(annotation, s_parser.parse("@Single(val=\"foo\")"));
            assertEquals("foo", annotation.getVal());
            AnnotationVisitor.parse(annotation, s_parser.parse("@Single(val=\"foo bar\")"));
            AnnotationVisitor.parse(annotation, s_parser.parse("@Single (val=\"foo bar\")"));
            AnnotationVisitor.parse(annotation, s_parser.parse("@Single(val=\"foo bar\"       )"));

            AnnotationVisitor.parse(annotation, s_parser.parse("@Single(s=\"foo\")"));
            assertEquals("foo", annotation.s());
            AnnotationVisitor.parse(annotation, s_parser.parse("@Single(s=\"foo bar\")"));
            AnnotationVisitor.parse(annotation, s_parser.parse("@Single (s=\"foo bar\")"));
            AnnotationVisitor.parse(annotation, s_parser.parse("@Single(s=\"foo bar\"       )"));

            VoidTyped annotation2 = new VoidTyped();
            AnnotationVisitor.parse(annotation2, s_parser.parse("@Void()"));
            AnnotationVisitor.parse(annotation2, s_parser.parse("@Void"));
        } catch (Throwable t) {
            fail(t.toString());
        }
    }

    public void testDefault() {
        try {
            DefaultString annotation = new DefaultString();
            AnnotationVisitor.parse(annotation, s_parser.parse("@DefaultString(\"foo\")"));
            assertEquals("foo", annotation.getValue());
        } catch (Throwable t) {
            fail(t.toString());
        }
    }

    // note that for correct long type handling, it is mandatory to have the l or L suffix
    public void testComplex() {
        try {
            Complex annotation = new Complex();
            AnnotationVisitor.parse(annotation, s_parser.parse("@Complex(i=3  ls={1l,2l,6L}  klass=java.lang.String.class)"));
            assertEquals(String.class, annotation.getKlass());
            AnnotationVisitor.parse(annotation, s_parser.parse("@Complex(i=3, ls={1l,2l,6L},  klass=java.lang.String.class)"));
            assertEquals(String.class, annotation.getKlass());
        } catch (Throwable t) {
            fail(t.toString());
        }
    }

    public static class Helper extends TypedAnnotationProxy {
        public static AnnotationParser getAnnotationParser() {
            return s_parser;
        }
    }

    public static class VoidTyped extends TypedAnnotationProxy {
    }

    public static class Simple extends TypedAnnotationProxy {
        String s;
        public void setVal(String s) {this.s = s;}
        public String getVal() {return this.s;}
        public void sets(String s) {this.s = s;}
        public String s() {return this.s;}
    }

    public static class DefaultString extends TypedAnnotationProxy {
        String s;
        public void setValue(String s) {this.s = s;}
        public String getValue() {return this.s;}
    }

    public static class Complex extends TypedAnnotationProxy {
        int i;
        long[] ls;
        Class klass;
        public void setI(int i) {this.i = i;}
        public int getI() {return this.i;}
        public void setLs(long[] ls) {this.ls = ls;}
        public long[] getLs() {return this.ls;}
        public void setKlass(Class k) {this.klass = k;}
        public Class getKlass() {return this.klass;}
    }

    public static class Untyped extends UntypedAnnotationProxy {
    }
}
