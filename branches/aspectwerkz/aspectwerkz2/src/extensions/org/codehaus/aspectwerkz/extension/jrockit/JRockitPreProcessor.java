/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.extension.jrockit;

import org.codehaus.aspectwerkz.hook.ClassPreProcessor;
import org.codehaus.aspectwerkz.compiler.VerifierClassLoader;
import com.bea.jvm.JVMFactory;

import java.net.URL;
import java.io.File;

/**
 * JRockit (tested with 7SP4 and 8.1) preprocessor Adapter based on JMAPI
 *
 * JRockit has a low level API for hooking ClassPreProcessor, allowing the use of online
 * weaving at full speed. Moreover, JRockit does not allow java.lang.ClassLoader overriding thru
 * -Xbootclasspath/p option.
 *
 * The main difference with standard AspectWerkz online mode is that the ClassPreProcessor implementation
 * and all third party jars CAN reside in the standard classpath.
 *
 * The command line tool will look like:
 * <code>"%JAVA_COMMAND%" -Xmanagement:class=org.codehaus.aspectwerkz.extension.jrockit.JRockitPreProcessor -Xbootclasspath/p:"%ASPECTWERKZ_HOME%\target\extensions.jar;%ASPECTWERKZ_HOME%\lib\bcel-patch.jar;%ASPECTWERKZ_HOME%\lib\bcel.jar;%ASPECTWERKZ_HOME%\lib\aspectwerkz-core-%ASPECTWERKZ_VERSION%.jar;%ASPECTWERKZ_HOME%\lib\aspectwerkz-%ASPECTWERKZ_VERSION%.jar;%ASPECTWERKZ_LIBS%" -cp "%CP%;%ASPECTWERKZ_HOME%\lib\aspectwerkz-%ASPECTWERKZ_VERSION%.jar;%ASPECTWERKZ_LIBS%" -Daspectwerkz.home="%ASPECTWERKZ_HOME%" -Daspectwerkz.transform.verbose=yes %*</code>
 * Note: there can be some NoClassDefFoundError due to classpath limitation - as described in http://edocs.bea.com/wls/docs81/adminguide/winservice.html
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class JRockitPreProcessor implements com.bea.jvm.ClassPreProcessor {

    /** concrete preprocessor */
    private static ClassPreProcessor preProcessor;
    static {
        String clpp = System.getProperty("aspectwerkz.classloader.preprocessor", "org.codehaus.aspectwerkz.transform.AspectWerkzPreProcessor");
        try {
            // note: CLPP loaded by current thread classloader which is bootstrap classloader
            // caution: forcing loading thru Thread.setContextClassLoader() or ClassLoader.getSystemClassLoader()
            // does not work. We then do a filtering on the caller classloader - see preProcess(..)
            //preProcessor = (ClassPreProcessor) Class.forName(clpp).newInstance();
            preProcessor = (ClassPreProcessor) ClassLoader.getSystemClassLoader().loadClass(clpp).newInstance();
            preProcessor.initialize(null);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * The JMAPI ClassPreProcessor must be self registrating
     */
    public JRockitPreProcessor() {
        JVMFactory.getJVM().getClassLibrary().setClassPreProcessor(this);
    }

    /**
     * Weave a class
     * @param caller classloader
     * @param name of the class to weave
     * @param bytecode original
     * @return bytecode weaved
     */
    public byte[] preProcess(ClassLoader caller, String name, byte[] bytecode) {
        //System.out.println(name + " [" + caller + "]");
        if (caller == null || caller.getParent() == null) {
            return bytecode;
        } else {
            return preProcessor.preProcess(name, bytecode, caller);
        }
    }

    /**
     * Utility for testing
     */
    public static void main(String args[]) throws Throwable {
        // uncomment this lines to programmaticaly configure at runtime the CLPP
        JRockitPreProcessor pp = new JRockitPreProcessor();//self registration

        Class loadedCP = Class.forName("java.math.BigDecimal");

        System.out.println(org.codehaus.aspectwerkz.extension.jrockit.JRockitPreProcessor.class.getClassLoader());

        while (true) {
            System.out.print(".");
            ClassLoader nonDelegatingCL = new VerifierClassLoader(new URL[]{(new File(args[0])).toURL()}, ClassLoader.getSystemClassLoader());
            Class loaded = nonDelegatingCL.loadClass(org.codehaus.aspectwerkz.extension.jrockit.JRockitPreProcessor.class.getName());
            Thread.sleep(500);
        }
    }
}
