/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD-style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.extension.hotswap;

/**
 * In process HotSwap - Java level API
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class HotSwapClient {

    static {
        System.loadLibrary("aspectwerkz");
    }

    //public static AspectWerkzPreProcessor pp;

    public static void hotswap(Class klazz) throws Throwable {
        //System.out.println(ClassPreProcessorHelper.preProcessor);
        //AspectWerkzPreProcessor.preProcessActivateS(klazz);
//        Class aw = ClassLoader.getSystemClassLoader().loadClass(AspectWerkzPreProcessor.class.getName());
//        Method m = aw.getMethod("preProcessActivateS", new Class[]{Class.class});
//        byte[] newB = (byte[])m.invoke(null, new Object[]{klazz});

//        System.out.println(WeavingClassLoader.pp);
//        byte[] newB = WeavingClassLoader.pp.preProcessActivate(klazz);
//        hotswap(klazz.getName(), klazz, newB, newB.length);
    }

    /**
     * Native method to calls the JVM C level API
     *
     * @param className
     * @param orginalClass
     * @param newBytes
     * @param newLength
     * @return
     */
    private static native int hotswap(String className, Class orginalClass, byte[] newBytes, int newLength);

    public static void hotswap(Class orginalClass, byte[] newBytes) {
        int code = hotswap(orginalClass.getName(), orginalClass, newBytes, newBytes.length);
        System.out.println("hotswapped " + orginalClass.getName() + " = code " + code);
    }

}
