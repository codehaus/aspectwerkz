/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD-style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.extension.hotswap;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

/**
 * A simple class to test the in process HotSwap
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class Foo {

    public void sayHello() {
        System.out.println("\tHello - I am " + this + " class " + this.getClass().hashCode());
    }

    public static void main(String a[]) throws Throwable {
        System.out.println("start");
        HotSwapClient client = new HotSwapClient();
        System.out.println("created hotswap client");

        Foo aFoo = new Foo();
        aFoo.sayHello();

        ClassPool cp = ClassPool.getDefault();
        CtClass newFoo = cp.get("org.codehaus.aspectwerkz.extension.hotswap.Foo");
        CtMethod m = newFoo.getDeclaredMethod("sayHello");
        m.insertBefore("{System.out.println(\"\thotswapped talks:\");}");
        byte[] newFooB = cp.write("org.codehaus.aspectwerkz.extension.hotswap.Foo");

        client.hotswap(Foo.class, newFooB);

        // same instance is hotswapped
        aFoo.sayHello();

        // other instance is hotswapped
        Foo bFoo = new Foo();
        bFoo.sayHello();

    }
}
