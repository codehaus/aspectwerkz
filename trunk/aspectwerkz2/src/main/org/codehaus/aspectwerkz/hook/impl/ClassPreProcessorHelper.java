/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.hook.impl;

import java.security.ProtectionDomain;

import org.codehaus.aspectwerkz.hook.ClassPreProcessor;

/**
 * Helper class called by the modified java.lang.ClassLoader.
 * <p/>
 * This class is called at different points by the modified java.lang.ClassLoader of the
 * org.codehaus.aspectwerkz.hook.impl.ClassLoaderPreProcessorImpl implemention.<br/> This class must reside in the
 * -Xbootclasspath when AspectWerkz layer 1 is used, but the effective implementation of the class preprocessor
 * (AspectWerkz layer 2) can be in standard system classpath (-cp).
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
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
     * Initialization of the ClassPreProcessor The ClassPreProcessor implementation is lazy loaded. This allow to put it
     * in the regular classpath whereas the instrumentation layer (layer 1) is in the bootclasspath
     */
    public static synchronized void initializePreProcessor() {
        //@todo review log statement according to log layer
        if (preProcessorInitialized)
            return;
        preProcessorInitialized = true;
        Class klass = null;
        String s = System.getProperty(PRE_PROCESSOR_CLASSNAME_PROPERTY, PRE_PROCESSOR_CLASSNAME_DEFAULT);

        try {
            // force loading thru System class loader to allow
            // preprocessor implementation to be in standard classpath
            klass = Class.forName(s, true, ClassLoader.getSystemClassLoader());
        }
        catch (ClassNotFoundException _ex) {
            System.err.println("AspectWerkz - WARN - Pre-processor class '" + s + "' not found");
        }
        if (klass != null)
            try {
                preProcessor = (ClassPreProcessor)klass.newInstance();
                preProcessor.initialize(null);
                System.out.println("AspectWerkz - INFO - Pre-processor " + s + " loaded and initialized");
            }
            catch (Throwable throwable) {
                System.err.println("AspectWerkz - WARN - Error initializing pre-processor class " + s + ':');
                throwable.printStackTrace();
            }
    }

    /**
     * byte code instrumentation of class loaded
     */
    public static byte[] defineClass0Pre(ClassLoader caller, String name, byte[] b, int off, int len, ProtectionDomain pd) {
        if (!preProcessorInitialized) {
            initializePreProcessor();
        }

        if (preProcessor == null) {
            // we need to check this due to reentrancy when ClassPreProcessorHelper is beeing initialized
            // since it tries to load a ClassPreProcessor implementation
            byte[] obyte = new byte[len];
            System.arraycopy(b, off, obyte, 0, len);
            return obyte;
        }
        else {
            try {
                byte[] ibyte = new byte[len];
                byte[] obyte = new byte[]{};
                System.arraycopy(b, off, ibyte, 0, len);
                obyte = preProcessor.preProcess(name, ibyte, caller);
                return obyte;
            }
            catch (Throwable throwable) {
                System.err.println("AspectWerkz - WARN - Error pre-processing class " + name + " in " + Thread.currentThread());
                throwable.printStackTrace();
                byte[] obyte = new byte[len];
                System.arraycopy(b, off, obyte, 0, len);
                //abyte = preProcessor.preProcess(name, abyte, caller);
                return obyte;
            }
        }
    }

}
