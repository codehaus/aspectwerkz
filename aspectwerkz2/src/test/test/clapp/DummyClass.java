/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.clapp;

import test.clapp.CrazyClassLoaderApp;

import java.net.URLClassLoader;
import java.net.URL;

/**
 * fake class
 * <p/>
 * The clinit will load another class thru a custom classloader
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class DummyClass {

    static {
        try {
            // create a URLClassLoader with NO delegation
            ClassLoader tmp = new URLClassLoader(new URL[]{new java.io.File(CrazyClassLoaderApp.DUMMYCLASS_LOCATION).toURL()}, null);
            // load another class in this clinit DummyClass
            Class re = Class.forName("test.clapp.DummyReentrantClass", true, tmp);
            Object reI = re.newInstance();
            System.out.println("DummyReentrantClass.hashcode=" + re.hashCode());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {
        System.out.println("Hello DummyClass");
        System.exit(0);
    }

}
