/**************************************************************************************
 * Copyright (c) Jonas Bon?r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.aopc;

import junit.framework.TestCase;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Note: does not work behing WeavingCL. Use a real online mode
 * <p/>
 * java -Xrunaspectwerkz -Xdebug -Xbootclasspath/a:lib\aspectwerkz-core-1.0-beta1.jar;lib\javassist-3.0beta.jar ...
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class AspectSystemTest extends TestCase {
    public void testDoubleHierarchyMethodExecution() {
        ClassLoader myCL = new URLClassLoader(new URL[] {
                                                  ClassCreator.getPathFor(Callable.class.getResource("META-INF/aop.xml"))
                                              }, ClassLoader.getSystemClassLoader());

        //TODO if CLA is runned, CLB fails. Might be related to metadata/TF/jpindex (see TF verbose)
        ClassLoader mySubCLA = new URLClassLoader(new URL[] {
                                                      ClassCreator.getPathFor(Callable.class.getResource("a/META-INF/aop.xml"))
                                                  }, myCL);
        Callable ca = (Callable)ClassCreator.createInstance("test.aopc.a.Callee", mySubCLA);
        ca.methodAround();
        ca.debug();
        assertEquals("beforeAround beforeAround methodAround afterAround afterAround ", ca.getLogString());
        ClassLoader mySubCLB = new URLClassLoader(new URL[] {  }, myCL);
        Callable cb = (Callable)ClassCreator.createInstance("test.aopc.b.Callee", mySubCLB);
        cb.methodAround();
        cb.debug();
        assertEquals("beforeAround methodAround afterAround ", cb.getLogString());
    }

    // ------------------------------------------------
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(AspectSystemTest.class);
    }
}
