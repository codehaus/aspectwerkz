/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.hook.impl;

import org.codehaus.aspectwerkz.hook.ClassPreProcessor;

import java.security.ProtectionDomain;
import java.nio.ByteBuffer;

/**
 * Helper class called by the modified java.lang.ClassLoader. <p/>This class is called at different points by the
 * modified java.lang.ClassLoader of the org.codehaus.aspectwerkz.hook.impl.ClassLoaderPreProcessorImpl implemention.
 * <br/>This class must reside in the -Xbootclasspath when AspectWerkz layer 1 is used, but the effective implementation
 * of the class preprocessor (AspectWerkz layer 2) can be in standard system classpath (-cp).
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur </a>
 */
public class ClassPreProcessorHelper {
    /**
     * ClassPreProcessor used if aspectwerkz.classloader.preprocessor property is defined to full qualified class name
     */
    private static ClassPreProcessor preProcessor;

    /**
     * true if preProcesor already initalized
     */
    private static boolean preProcessorInitialized;

    /**
     * option used to defined the class preprocessor
     */
    private static String PRE_PROCESSOR_CLASSNAME_PROPERTY = "aspectwerkz.classloader.preprocessor";

    /**
     * default class preprocessor
     */
    private static String PRE_PROCESSOR_CLASSNAME_DEFAULT = "org.codehaus.aspectwerkz.transform.AspectWerkzPreProcessor";

    static {
        initializePreProcessor();
    }

    /**
     * Returns the configured class preprocessor Should be called after initialization only
     *
     * @return the preprocessor or null if not initialized
     */
    public static ClassPreProcessor getClassPreProcessor() {
        return preProcessor;
    }

    /**
     * Initialization of the ClassPreProcessor The ClassPreProcessor implementation is lazy loaded. This allow to put it
     * in the regular classpath whereas the instrumentation layer (layer 1) is in the bootclasspath
     */
    public static synchronized void initializePreProcessor() {
        if (preProcessorInitialized) {
            return;
        }
        preProcessorInitialized = true;
        Class klass = null;
        String s = System.getProperty(PRE_PROCESSOR_CLASSNAME_PROPERTY, PRE_PROCESSOR_CLASSNAME_DEFAULT);
        try {
            // force loading thru System class loader to allow
            // preprocessor implementation to be in standard classpath
            klass = Class.forName(s, true, ClassLoader.getSystemClassLoader());
        } catch (ClassNotFoundException _ex) {
            System.err.println("AspectWerkz - WARN - Pre-processor class '" + s + "' not found");
        }
        if (klass != null) {
            try {
                preProcessor = (ClassPreProcessor) klass.newInstance();
                preProcessor.initialize();
                System.out.println("AspectWerkz - INFO - Pre-processor " + s + " loaded and initialized");
            } catch (Throwable throwable) {
                System.err.println("AspectWerkz - WARN - Error initializing pre-processor class " + s + ':');
                throwable.printStackTrace();
            }                       
        }
    }

    /**
     * byte code instrumentation of class loaded
     */
    public static byte[] defineClass0Pre(ClassLoader caller,
                                         String name,
                                         byte[] b,
                                         int off,
                                         int len,
                                         ProtectionDomain pd) {
        if (preProcessor == null) {
            // we need to check this due to reentrancy when ClassPreProcessorHelper is beeing
            // initialized
            // since it tries to load a ClassPreProcessor implementation
            byte[] obyte = new byte[len];
            System.arraycopy(b, off, obyte, 0, len);
            return obyte;
        } else {
            try {
                byte[] ibyte = new byte[len];
                System.arraycopy(b, off, ibyte, 0, len);
                return preProcessor.preProcess(name, ibyte, caller);
            } catch (Throwable throwable) {
                System.err.println(
                        "AspectWerkz - WARN - Error pre-processing class "
                        + name
                        + " in "
                        + Thread.currentThread()
                );
                throwable.printStackTrace();
                // fallback to unweaved bytecode
                byte[] obyte = new byte[len];
                System.arraycopy(b, off, obyte, 0, len);
                return obyte;
            }
        }
    }

    // FIXME needed for 1.5 Plug but implies Java 1.4 NIO usage
    public static ByteBuffer defineClass0Pre(ClassLoader caller,
                                         String name,
                                         ByteBuffer byteBuffer,
                                         int off,
                                         int len,
                                         ProtectionDomain pd) {
        byte[] bytes = new byte[len];
        byteBuffer.get(bytes, off, len);
        byte[] newbytes = defineClass0Pre(caller, name, bytes, 0, bytes.length, pd);
        ByteBuffer newBuffer = ByteBuffer.wrap(newbytes);
        return newBuffer;
    }

}