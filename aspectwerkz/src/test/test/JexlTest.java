/*
 * AspectWerkz - a dynamic, lightweight and high-performant AOP/AOSD framework for Java.
 * Copyright (C) 2002-2003  Jonas Bonér. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package test;

import junit.framework.TestCase;
import org.apache.commons.jexl.JexlContext;
import org.apache.commons.jexl.JexlHelper;
import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.ExpressionFactory;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: JexlTest.java,v 1.1 2003-06-17 15:19:42 jboner Exp $
 */
public class JexlTest extends TestCase {

    public void testX() throws Exception {
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
