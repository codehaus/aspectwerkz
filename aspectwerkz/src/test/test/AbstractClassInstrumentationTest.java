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

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: AbstractClassInstrumentationTest.java,v 1.1.2.1 2003-07-17 21:00:01 avasseur Exp $
 */
public class AbstractClassInstrumentationTest extends TestCase {

    public void testInstrumentedAbstractMemberMethodInvocation() {
        try {
            AbstractTarget target = new AbstractTargetImpl();
            assertEquals("method1", target.method1());
        }
        catch (Exception e) {
            fail();
        }
    }

    public void testInstrumentedAbstractStaticMethodInvocation() {
        try {
            AbstractTarget target = new AbstractTargetImpl();
            assertEquals("method2", target.method2());
        }
        catch (Exception e) {
            fail();
        }
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(AbstractClassInstrumentationTest.class);
    }

    public AbstractClassInstrumentationTest(String name) {
        super(name);
    }
}
