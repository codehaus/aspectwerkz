/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD-style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.extension.hotswap;

import org.codehaus.aspectwerkz.hook.impl.ClassPreProcessorHelper;
import org.codehaus.aspectwerkz.hook.RuntimeClassProcessor;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;

/**
 * In process HotSwap - Java level API
 *
 * When used, the hook* classes (AspectWerkz - core) MUST be in bootclasspath
 * to ensure correct behavior and lookup of the ClassPreProcessor singleton
 * and the cache
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class HotSwapClient {

    static {
        System.loadLibrary("aspectwerkz");
    }

    /**
     * Native method to calls the JVM C level API
     *
     * @param className
     * @param klazz
     * @param newBytes
     * @param newLength
     * @return
     */
    private static native int hotswap(String className, Class klazz, byte[] newBytes, int newLength);

    /**
     * In process hotswap
     *
     * @param klazz
     * @param newBytes
     */
    public static void hotswap(Class klazz, byte[] newBytes) {
        int code = hotswap(klazz.getName(), klazz, newBytes, newBytes.length);
        System.out.println("hotswapped " + klazz.getName() + " = code " + code);
    }

    /**
     * AspectWerkz HotSwap, uses the ClassPreProcessor if capable of Runtime weaving
     * The given Class is hotswap after transformation based on the current definitions
     *
     * @param klazz
     */
    public static void hotswap(Class klazz) {
        if (ClassPreProcessorHelper.class.getClassLoader() != null) {
            throw new RuntimeException("AspectWerkz core must be in bootclasspath for HotSwap cache to work: "
                + ClassPreProcessorHelper.class.getClassLoader());
        }
        try {
            RuntimeClassProcessor runtimeProcessor = (RuntimeClassProcessor) ClassPreProcessorHelper.getClassPreProcessor();
            hotswap(klazz, runtimeProcessor.preProcessActivate(klazz));
        } catch (Throwable t) {
            throw new WrappedRuntimeException(t);
        }
    }

}
