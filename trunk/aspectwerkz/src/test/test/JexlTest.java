/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test;

import junit.framework.TestCase;
import org.apache.commons.jexl.JexlContext;
import org.apache.commons.jexl.JexlHelper;
import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.ExpressionFactory;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class JexlTest extends TestCase {

    public void test_NEG1() throws Exception {
        JexlContext jc = JexlHelper.createContext();
        jc.getVars().put("A", Boolean.TRUE);
        Expression e = ExpressionFactory.createExpression("!A");
        Object actual = e.evaluate(jc);
        assertEquals(Boolean.FALSE, actual);
    }

    public void test_NEG2() throws Exception {
        JexlContext jc = JexlHelper.createContext();
        jc.getVars().put("A", Boolean.TRUE);
        Expression e = ExpressionFactory.createExpression("!(A)");
        Object actual = e.evaluate(jc);
        assertEquals(Boolean.FALSE, actual);
    }

    public void test_COMPLEX() throws Exception {
        JexlContext jc = JexlHelper.createContext();

        jc.getVars().put("A", Boolean.TRUE);
        jc.getVars().put("B", Boolean.FALSE);
        jc.getVars().put("C", Boolean.FALSE);

        Expression e = ExpressionFactory.createExpression("(A || B) && !C");
        Object actual = e.evaluate(jc);
        assertEquals(Boolean.TRUE, actual);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(JexlTest.class);
    }

    public JexlTest(String name) {
        super(name);
    }

}
