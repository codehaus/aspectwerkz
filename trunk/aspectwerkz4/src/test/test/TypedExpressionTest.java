/**************************************************************************************
 * Copyright (c) Jonas Bon?r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test;

import junit.framework.TestCase;
import test.expression.ExpressionTest;
import org.codehaus.aspectwerkz.expression.DumpVisitor;
import org.codehaus.aspectwerkz.expression.ast.ASTRoot;
import org.codehaus.aspectwerkz.expression.ast.ExpressionParser;

import java.io.StringReader;

/**
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class TypedExpressionTest extends TestCase {

    private static final ExpressionParser s_parser = new ExpressionParser(System.in);


    public TypedExpressionTest(String name) {
        super(name);
    }

    public void testGrammar() throws Throwable {
//        PARSER.parse("pc(String, a) && ! ( args(s) )").dump(" ");
        s_parser.parse(
                //"NOT(execution(void test.expression.Target.modifiers1()) OR NOT execution(* java.lang.String.*(..)))"
                "dummy(s) OR not(dummy2)"
        ).dump(" ");

    }


    //--- JUnit
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(TypedExpressionTest.class);
    }


}
