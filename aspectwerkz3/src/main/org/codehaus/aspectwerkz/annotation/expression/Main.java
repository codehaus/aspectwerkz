/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.annotation.expression;

import org.codehaus.aspectwerkz.annotation.expression.ast.AnnotationParser;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class Main {
    public static void main(String[] args) throws Throwable {
        AnnotationParser parser = new AnnotationParser(System.in); // can be only one
        DumpVisitor.dumpAST(parser.parse("@Annotation(level=3)"));
        DumpVisitor.dumpAST(parser.parse("@Annotation(level=0xCAFEBABE)"));
        DumpVisitor.dumpAST(parser.parse("@Annotation(level=33598476398762L)"));
        DumpVisitor.dumpAST(parser.parse("@Annotation(level=-33598476398762L)"));
        DumpVisitor.dumpAST(parser.parse("@Annotation(level=33.345F)"));
        DumpVisitor.dumpAST(parser.parse("@Annotation(level=-33.345F)"));
        DumpVisitor.dumpAST(parser.parse("@Annotation(level=33.345d)"));
        DumpVisitor.dumpAST(parser.parse("@Annotation(level=-33.345d)"));
        DumpVisitor.dumpAST(parser.parse("@Annotation(value)"));
        DumpVisitor.dumpAST(parser.parse("@Annotation(key=value)"));
        DumpVisitor.dumpAST(parser.parse("@Annotation(key=true)"));
        DumpVisitor.dumpAST(parser.parse("@Annotation(constant=org.foo.Bar.CONSTANT int=54543 float=76.7F bool=FALSE)"));
        DumpVisitor.dumpAST(parser.parse("@Annotation(key={\"hej\", \"hey\"})"));
        DumpVisitor.dumpAST(parser.parse("@Annotation(key={TRUE, false})"));
    }
}
