/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.xmldef.clapp;

import junit.framework.TestCase;

import java.net.URL;
import java.lang.reflect.Method;

import org.codehaus.aspectwerkz.compiler.VerifierClassLoader;

public class CustomClassLoaderTest extends TestCase {

    private static String targetPath = CustomClassLoaderTest.class.getClassLoader().getResource("test/xmldef/clapp/Target.class").toString();
    static {
        targetPath = targetPath.substring(0, targetPath.indexOf("test/xmldef/clapp/Target.class"));
    }

    public void testCustomClassLoaderWeaving() {
        try {
            VerifierClassLoader cl = new VerifierClassLoader(
                    new URL[]{new URL(targetPath)},
                    ClassLoader.getSystemClassLoader());

            Class target = cl.loadClass("test.xmldef.clapp.Target");
            assertEquals(target.getClassLoader().hashCode(), cl.hashCode());
            Method m = target.getMethod("callme", new Class[]{});
            String res = (String) m.invoke(target.newInstance(), new Object[]{});
            assertEquals("before call after", res);
        } catch (Throwable t) {
            t.printStackTrace();
            fail(t.getMessage());
        }
    }

    public static void main(String a[]) {
        CustomClassLoaderTest me = new CustomClassLoaderTest();
        me.testCustomClassLoaderWeaving();
    }

}
