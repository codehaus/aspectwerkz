/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test;

import junit.framework.TestCase;
import org.codehaus.aspectwerkz.cflow.CflowCompiler;
import org.codehaus.aspectwerkz.cflow.AbstractCflowSystemAspect;
import org.codehaus.aspectwerkz.transform.inlining.AsmHelper;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.MethodInfo;
import org.codehaus.aspectwerkz.reflect.impl.asm.AsmClassInfo;
import org.codehaus.aspectwerkz.exception.DefinitionException;

import java.lang.reflect.Method;

/**
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class CflowCompilerTest extends TestCase {

    public void testCompiler() {
        try {
            Class cflowAspect = CflowCompiler.compileCflowAspectAndAttachToClassLoader(
                    CflowCompilerTest.class.getClassLoader(),
                    4
            );

            assertEquals(cflowAspect.getName().replace('/', '.'), "org.codehaus.aspectwerkz.cflow.Cflow_4");
            assertTrue(cflowAspect.getSuperclass().equals(AbstractCflowSystemAspect.class));
            Method enter = cflowAspect.getDeclaredMethod("isInCflow", new Class[0]);
            Method exit = cflowAspect.getDeclaredMethod("isInCflowBelow", new Class[0]);
        } catch (Throwable t) {
            fail(t.toString());
        }
    }

    public void testCflow() throws Throwable {
        Class cflowAspect = CflowCompiler.compileCflowAspectAndAttachToClassLoader(
                CflowCompilerTest.class.getClassLoader(),
                4
        );

        final AbstractCflowSystemAspect cflow = (AbstractCflowSystemAspect)cflowAspect.newInstance();
        assertFalse(cflow.inCflow());
        assertFalse(cflow.inCflowBelow());
        Thread t = new Thread() {
            public void run() {
                System.out.println(Thread.currentThread());
                cflow.enter();
                assertTrue(cflow.inCflow());
                assertTrue(cflow.inCflowBelow());//TODO is cflowbelow that or the opposite ??
                cflow.enter();
                assertTrue(cflow.inCflow());
                assertFalse(cflow.inCflowBelow());
                cflow.exit();
                // leave the cflow in "inCflow" state is in this thread
            }
        };
        t.start();
        System.out.println(Thread.currentThread());

        assertFalse(cflow.inCflow());
        assertFalse(cflow.inCflowBelow());
    }



    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(CflowCompilerTest.class);
    }

}
