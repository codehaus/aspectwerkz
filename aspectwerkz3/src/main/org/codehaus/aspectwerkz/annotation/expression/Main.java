/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.annotation.expression;

import org.codehaus.aspectwerkz.annotation.TypedAnnotationProxy;
import org.codehaus.aspectwerkz.annotation.expression.ast.AnnotationParser;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class Main {
    public static final int CONSTANT = 98;

    public static void main(String[] args) throws Throwable {
        AnnotationParser parser = new AnnotationParser(System.in); // can be only one
        Annotation annotation = new Annotation();

        //        AnnotationVisitor.parse(annotation, parser.parse("@Annotation(hex=0xCAFEBABE)"));
        //        AnnotationVisitor.parse(annotation, parser.parse("@Annotation(cArr={java.lang.String.class, int.class, double[].class})"));
        AnnotationVisitor.parse(annotation, parser.parse("@Annotation(lng=3)"));
        AnnotationVisitor.parse(annotation, parser.parse("@Annotation(lng=33598476398762L)"));
        AnnotationVisitor.parse(annotation, parser.parse("@Annotation(lng=-33598476398762L)"));
        AnnotationVisitor.parse(annotation, parser.parse("@Annotation(flt=33.345F)"));
        AnnotationVisitor.parse(annotation, parser.parse("@Annotation(flt=-33.345f)"));
        AnnotationVisitor.parse(annotation, parser.parse("@Annotation(dbl=33.345d)"));
        AnnotationVisitor.parse(annotation, parser.parse("@Annotation(dbl=-33.345d)"));
        AnnotationVisitor.parse(annotation, parser.parse("@Annotation(\"value\")"));
        AnnotationVisitor.parse(annotation, parser.parse("@Annotation(key=true)"));
        AnnotationVisitor.parse(
                annotation,
                parser.parse(
                        "@Annotation(constant=org.codehaus.aspectwerkz.annotation.expression.Main.CONSTANT lng=54543 flt=76.7F key=FALSE)"
                )
        );
        AnnotationVisitor.parse(annotation, parser.parse("@Annotation(sArr={\"hej\", \"hey\"})"));
        AnnotationVisitor.parse(annotation, parser.parse("@Annotation(bArr={TRUE, false})"));
        AnnotationVisitor.parse(annotation, parser.parse("@Annotation(klass=java.lang.String.class)"));
        AnnotationVisitor.parse(
                annotation,
                parser.parse("@Annotation(cArr={java.lang.String.class, int.class, double.class})")
        );

        //        DumpVisitor.dumpAST(parser.parse("@Annotation(level=3)"));
        //        DumpVisitor.dumpAST(parser.parse("@Annotation(level=0xCAFEBABE)"));
        //        DumpVisitor.dumpAST(parser.parse("@Annotation(level=33598476398762L)"));
        //        DumpVisitor.dumpAST(parser.parse("@Annotation(level=-33598476398762L)"));
        //        DumpVisitor.dumpAST(parser.parse("@Annotation(level=33.345F)"));
        //        DumpVisitor.dumpAST(parser.parse("@Annotation(level=-33.345F)"));
        //        DumpVisitor.dumpAST(parser.parse("@Annotation(level=33.345d)"));
        //        DumpVisitor.dumpAST(parser.parse("@Annotation(level=-33.345d)"));
        //        DumpVisitor.dumpAST(parser.parse("@Annotation(value)"));
        //        DumpVisitor.dumpAST(parser.parse("@Annotation(key=value)"));
        //        DumpVisitor.dumpAST(parser.parse("@Annotation(key=true)"));
        //        DumpVisitor.dumpAST(parser.parse("@Annotation(constant=org.foo.Bar.CONSTANT int=54543 float=76.7F bool=FALSE)"));
        //        DumpVisitor.dumpAST(parser.parse("@Annotation(key={\"hej\", \"hey\"})"));
        //        DumpVisitor.dumpAST(parser.parse("@Annotation(key={TRUE, false})"));
    }

    private static class Annotation extends TypedAnnotationProxy {
        long lng;
        int hex;
        float flt;
        double dbl;
        String value;
        boolean key;
        int constant;
        String[] sArr;
        boolean[] bArr;
        Class klass;
        Class[] cArr;

        public long lng() {
            return lng;
        }

        public void setlng(long lng) {
            System.out.println("lng = " + lng);
            this.lng = lng;
        }

        public int hex() {
            return hex;
        }

        public void sethex(int hex) {
            System.out.println("hex = " + hex);
            this.hex = hex;
        }

        public float flt() {
            return flt;
        }

        public void setflt(float flt) {
            System.out.println("flt = " + flt);
            this.flt = flt;
        }

        public double dbl() {
            return dbl;
        }

        public void setdbl(double dbl) {
            System.out.println("dbl = " + dbl);
            this.dbl = dbl;
        }

        public String value() {
            return value;
        }

        public void setvalue(String value) {
            System.out.println("value = " + value);
            this.value = value;
        }

        public boolean key() {
            return key;
        }

        public void setkey(boolean key) {
            System.out.println("key = " + key);
            this.key = key;
        }

        public int constant() {
            return constant;
        }

        public void setconstant(int constant) {
            System.out.println("constant = " + constant);
            this.constant = constant;
        }

        public String[] sArr() {
            return sArr;
        }

        public void setsArr(String[] sArr) {
            for (int i = 0; i < sArr.length; i++) {
                System.out.println("sArr = " + sArr[i]);
            }
            this.sArr = sArr;
        }

        public boolean[] bArr() {
            return bArr;
        }

        public void setbArr(boolean[] bArr) {
            for (int i = 0; i < bArr.length; i++) {
                System.out.println("bArr = " + bArr[i]);
            }
            this.bArr = bArr;
        }

        public Class klass() {
            return klass;
        }

        public void setklass(Class klass) {
            System.out.println("klass = " + klass);
            this.klass = klass;
        }

        public Class[] cArr() {
            return cArr;
        }

        public void setcArr(Class[] cArr) {
            for (int i = 0; i < cArr.length; i++) {
                System.out.println("cArr = " + cArr[i]);
            }
            this.cArr = cArr;
        }
    }
}
