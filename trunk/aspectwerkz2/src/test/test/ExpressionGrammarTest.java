/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test;

import junit.framework.TestCase;
import org.codehaus.aspectwerkz.definition.expression.visitor.EvaluateVisitor;
import org.codehaus.aspectwerkz.definition.expression.ast.*;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;

import java.io.StringReader;

/**
 * Test the pointcut expression boolean grammar
 * Ignores cflow operator
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class ExpressionGrammarTest extends TestCase {

    private final static ExpressionParserVisitor MOCK_VISITOR = new MockEvaluateVisitor();

    public void testBooleanConstant() {
        assertTrue(evaluate("true"));
        assertFalse(evaluate("false"));
        assertTrue(evaluate("!false"));
        assertTrue(evaluate("! false"));
        assertTrue(evaluate("not false"));
        assertTrue(evaluate("NOT FALSE"));
        assertTrue(evaluate("NOT FalsE"));
        assertFalse(evaluate("! true"));
    }

    public void testSimpleExpression() {
        assertTrue(evaluate("true_ && true__"));
        assertTrue(evaluate("true_ AND true__"));
        assertTrue(evaluate("true_ || true__"));
        assertTrue(evaluate("true_ OR true__"));
        assertFalse(evaluate("true_ && false__"));
        assertFalse(evaluate("false_ || false"));
        assertTrue(evaluate("false OR true_"));
    }

    public void testPrecedence() {
        assertTrue(evaluate("false OR true AND true_"));
        assertTrue(evaluate("false OR (true AND true_)"));

        assertFalse(evaluate("false AND true AND true_"));
        assertFalse(evaluate("false AND (true AND true_)"));

        assertTrue(evaluate("false OR true AND ! false"));
        assertFalse(evaluate("false OR ! true AND ! false"));
    }

    private boolean evaluate(String expression) {
        try {
            SimpleNode root = (new ExpressionParser(new StringReader(expression))).ExpressionScript();
            return ((Boolean)root.jjtAccept(MOCK_VISITOR, null)).booleanValue();
        } catch (ParseException e) {
            throw new WrappedRuntimeException(e);
        }
    }

    public static class MockEvaluateVisitor extends EvaluateVisitor {

        public Object visit(Identifier node, Object data) {
            String leafName = node.name;
            if (leafName.startsWith("true_")) {
                return Boolean.TRUE;
            } else {
                return Boolean.FALSE;
            }
        }

    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(ExpressionGrammarTest.class);
    }

    public ExpressionGrammarTest(String name) {
        super(name);
    }
}
