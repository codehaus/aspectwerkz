/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.clapp;

import junit.framework.TestCase;
import org.codehaus.aspectwerkz.compiler.VerifierClassLoader;
import java.lang.reflect.Method;
import java.net.URL;

public class CustomClassLoaderTest extends TestCase {
    private static String targetPath = CustomClassLoaderTest.class.getClassLoader()
                                                                  .getResource("test/clapp/Target.class").toString();

    static {
        targetPath = targetPath.substring(0, targetPath.indexOf("test/clapp/Target.class"));
    }

    /**
     * Note: this test cannot be runned thru the WeavingClassLoader for debugging since it uses custom class loader
     * hierarchy. See testWeavingClassLoader() commented method
     */
    public void testCustomClassLoaderWeaving() {
        try {
            VerifierClassLoader cl = new VerifierClassLoader(new URL[] { new URL(targetPath) },
                                                             ClassLoader.getSystemClassLoader());

            Class target = cl.loadClass("test.clapp.Target");

            assertEquals(target.getClassLoader().hashCode(), cl.hashCode());

            Method m = target.getMethod("callme", new Class[] {  });
            String res = (String)m.invoke(target.newInstance(), new Object[] {  });

            assertEquals("before call after", res);
        } catch (Throwable t) {
            t.printStackTrace();
            fail(t.getMessage());
        }
    }

    /*
    // uncomment this to test outside of online mode
    // hack clinit to fix taregtPath = "foo";
    // put Target.class in C:\temp and remove it from the IDE compiled classes
    public void testWeavingClassLoader() {
        try {
            targetPath = (new java.io.File("C:\\temp\\")).toURL().toString();
            WeavingClassLoader wcl = new WeavingClassLoader(
                    new URL[]{new URL(targetPath)},
                    ClassLoader.getSystemClassLoader());
            Class target = wcl.loadClass("test.xmldef.clapp.Target");
            assertEquals(target.getClassLoader().hashCode(), wcl.hashCode());
            Method m = target.getAdvice("callme", new Class[]{});
            String res = (String) m.invoke(target.newInstance(), new Object[]{});
            assertEquals("before call after", res);
        } catch (Throwable t) {
            t.printStackTrace();
            fail(t.getMessage());
        }
    }*/
    public static void main(String[] a) {
        CustomClassLoaderTest me = new CustomClassLoaderTest();

        me.testCustomClassLoaderWeaving();

        // uncomment this to run test outside of online mode
        //me.testWeavingClassLoader();

        /*
        // uncomment this to run test outside of junitperf
        Thread t1 = new Thread(new Runnable() {
            public void run() {
                CustomClassLoaderTest me = new CustomClassLoaderTest();
                me.testWeavingClassLoader();
            }
        });
        Thread t2 = new Thread(new Runnable() {
            public void run() {
                CustomClassLoaderTest me = new CustomClassLoaderTest();
                me.testWeavingClassLoader();
            }
        });
        t1.start();
        t2.start();
        */
    }
}
