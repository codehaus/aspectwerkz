/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.hook;

import com.sun.jdi.VirtualMachine;

import java.lang.reflect.Method;
import java.io.File;
import java.io.IOException;

/**
 * ProcessStarter uses JPDA JDI api to start a VM with a runtime modified java.lang.ClassLoader, or transparently use a Xbootclasspath style (java 1.3 detected or forced)
 *
 * <p><h2>Important note</h2>
 * Due to a JPDA issue in LauchingConnector, this implementation is based on Process forking.
 * If Xbootclasspath is not used the target VM is started with JDWP options <i>transport=dt_socket,address=9300</i>
 * unless other specified.<br/>
 * It is possible after the short startup sequence to attach a debugger or any other JPDA attaching connector.
 * It has been validated against a WebLogic 7 startup and is the <i>must use</i> implementation.
 * </p>
 *
 * <p><h2>Implementation Note</h2>
 * See http://java.sun.com/products/jpda/<br/>
 * See http://java.sun.com/j2se/1.4.1/docs/guide/jpda/jdi/index.html<br/>
 * </p>
 *
 * <p>
 * For java 1.3, it launch the target VM using a modified java.lang.ClassLoader by
 * generating it and putting it in the bootstrap classpath of the target VM. The java 1.3 version should only be run for experimentation since
 * it breaks the Java 2 Runtime Environment binary code license by overriding a class of rt.jar
 * </p>
 *
 * <p>
 * For java 1.4, it hotswaps java.lang.ClassLoader with a runtime patched version, wich is compatible
 * with the Java 2 Runtime Environment binary code license. For JVM not supporting the class hotswapping,
 * the same mechanism as for java 1.3 is used.
 * </p>
 *
 * <p><h2>Usage</h2>
 * Use it as a replacement of "java" :<br/>
 * <code>java [target jvm option] [target classpath] targetMainClass [targetMainClass args]</code><br/>
 * should be called like:<br/>
 * <code>java [jvm option] [classpath] org.codehaus.aspectwerkz.hook.ProcessStarter [target jvm option] [target classpath] targetMainClass [targetMainClass args]</code><br/>
 * <b>[classpath] must contain %JAVA_HOME%/tools.jar for HotSwap support</b><br/>
 * [target jvm option] can contain JDWP options, transport and address are preserved if specified.
 * </p>
 *
 * <p><h2>Options</h2>
 * [classpath] must contain %JAVA_HOME%/tools.jar and the jar you want for bytecode modification (bcel, javassist...)<br/>
 * The java.lang.ClassLoader is patched using the <code>-Daspectwerkz.classloader.clpreprocessor=...</code>
 * in [jvm option]. Specify the FQN of your implementation of hook.ClassLoaderPreProcessor.
 * See {@link org.codehaus.aspectwerkz.hook.ClassLoaderPreProcessor}
 * If not given, the default AspectWerkz layer 1 BCEL implementation hook.impl.* is used, which is equivalent to
 * <code>-Daspectwerkz.classloader.clpreprocessor=org.codehaus.aspectwerkz.hook.impl.ClassLoaderPreProcessorImpl</code>
 * </p>
 *
 * <p><h2>Disabling HotSwap</h2>
 * You disable HotSwap and thus force the use of -Xbootclasspath (like in java 1.3 mode)
 * and specify the directory where the modified class loader bytecode will be stored using
 * in [jvm option] <code>-Daspectwerkz.classloader.clbootclasspath=...</code>. Specify the directory where you
 * want the patched java.lang.ClassLoader to be stored. Default is "./boot".
 * The directory is created if needed (with the subdirectories corresponding to package names).<br/>
 * The directory is <b>automatically</b> incorporated in the -Xbootclasspath option of [target jvm option].<br/>
 * You shoud use this option mainly for debuging purpose, or if you need to start different jvm with different
 * classloader preprocessor implementations.
 * </p>
 *
 * <p><h2>Option for AspectWerkz layer 1 BCEL implementation</h2>
 * When using the default AspectWerkz layer 1 BCEL implementation <code>org.codehaus.aspectwerkz.hook.impl.ClassLoaderPreProcessorImpl</code>
 * , java.lang.ClassLoader is modified to call a class preprocessor at each class load
 * (except for class loaded by the bootstrap classloader).<br/>
 * The effective class preprocessor is defined with <code>-Daspectwerkz.classloader.preprocessor=...</code>
 * in [target jvm option]. Specify the FQN of your implementation of org.codehaus.aspectwerkz.hook.ClassPreProcessor interface.<br/>
 * If this parameter is not given, no pre processing of loaded classed will occurs.<br/>
 * </p>
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 * @version $Id: ProcessStarter.java,v 1.2 2003-07-23 14:20:32 avasseur Exp $
 */
public class ProcessStarter {

    /** option for classloader preprocessor target */
    private final static String CL_PRE_PROCESSOR_CLASSNAME_PROPERTY = "aspectwerkz.classloader.clpreprocessor";
    /** default dir when -Xbootclasspath is forced or used (java 1.3) */
    private final static String CL_BOOTCLASSPATH_FORCE_DEFAULT = "."+File.separatorChar+"boot";
    /** option for target dir when -Xbootclasspath is forced or used (java 1.3) */
    private final static String CL_BOOTCLASSPATH_FORCE_PROPERTY = "aspectwerkz.classloader.clbootclasspath";

    /** target process */
    private Process process = null;
    /** used if target VM exits before launching VM */
    private boolean executeShutdownHook = true;

    /** thread to redirect streams of target VM in launching VM */
    private Thread inThread;
    /** thread to redirect streams of target VM in launching VM */
    private Thread outThread;
    /** thread to redirect streams of target VM in launching VM */
    private Thread errThread;


    /**
     * Test if current java installation supports HotSwap
     */
    private boolean hasCanRedefineClass() {
        try {
            Method canM = VirtualMachine.class.getMethod("canRedefineClasses", new Class[]{});
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    private int run(String args[]) {
        // retrieve options and main
        StringBuffer optionsArgB = new StringBuffer();
        StringBuffer cpOptionsArgB = new StringBuffer();
        StringBuffer mainArgB = new StringBuffer();
        String previous = null;
        boolean foundMain = false;
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("-") && !foundMain) {
                if (!("-cp".equals(args[i])) && !("-classpath").equals(args[i])) {
                    optionsArgB.append(args[i]).append(" ");
                }
            } else if (!foundMain && ("-cp".equals(previous) || "-classpath".equals(previous))) {
                if (cpOptionsArgB.length()>0)
                    cpOptionsArgB.append((System.getProperty("os.name","").toLowerCase().indexOf("windows")>=0)?";":":");
                cpOptionsArgB.append(args[i]);
            } else {
                foundMain=true;
                mainArgB.append(args[i]).append(" ");
            }
            previous = args[i];
        }
        String opt = optionsArgB.append(" -cp \"").append(cpOptionsArgB).append("\"").toString();
        String main = mainArgB.toString();
        String clp = System.getProperty(CL_PRE_PROCESSOR_CLASSNAME_PROPERTY, "org.codehaus.aspectwerkz.hook.impl.ClassLoaderPreProcessorImpl");

        // if java version does not support method "VirtualMachine.canRedefineClass"
        // or if bootclasspath is forced, transform optionsArg
        if (!hasCanRedefineClass() || System.getProperty(CL_BOOTCLASSPATH_FORCE_PROPERTY)!=null) {
            String bootDir = System.getProperty(CL_BOOTCLASSPATH_FORCE_PROPERTY, CL_BOOTCLASSPATH_FORCE_DEFAULT);
            if (System.getProperty(CL_BOOTCLASSPATH_FORCE_PROPERTY)!=null)
                System.out.println("HotSwap deactivated, using bootclasspath: " + bootDir);
            else
                System.out.println("HotSwap not supported by this java version, using bootclasspath: " + bootDir);

            ClassLoaderPatcher.patchClassLoader(clp, bootDir);

            BootClasspathStarter starter = new BootClasspathStarter(opt, main, bootDir);
            try {
                process = starter.launchVM();
            } catch (IOException e) {
                System.err.println("failed to launch process :" + starter.getCommandLine());
                e.printStackTrace();
                return -1;
            }
        } else {
            // lauch VM in suspend mode
            JDWPStarter starter = new JDWPStarter(opt, main, "dt_socket", "9300");
            try {
                process = starter.launchVM();
            } catch (IOException e) {
                System.err.println("failed to launch process :" + starter.getCommandLine());
                e.printStackTrace();
                return -1;
            }

            // override class loader in VM thru an attaching connector
            VirtualMachine vm = ClassLoaderPatcher.hotswapClassLoader(clp, starter.getTransport(), starter.getAddress());
            if (vm == null) {
                process.destroy();
            } else {
            	vm.resume();
            	vm.dispose();
			}
        }

        // attach VM streams to this streams
        redirectStreams();

        // add a shutdown hook to "this" to shutdown VM
        Thread shutdownHook = new Thread() {
            public void run() {
                shutdown();
            }
        };
        try {
            Runtime.getRuntime().addShutdownHook(shutdownHook);
            int exitCode = process.waitFor();
            executeShutdownHook = false;
            return exitCode;
        } catch (Exception e) {
            executeShutdownHook = false;
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * shutdown target VM (used by shutdown hook of lauching VM)
     */
    private void shutdown() {
        if (executeShutdownHook) {
			process.destroy();
        }
		try {
            outThread.join();
			errThread.join();
		} catch (InterruptedException e) {
			;
		}
    }

    /**
     * Set up stream redirection in target VM
     */
    private void redirectStreams() {
        /*System.in = process.getInputStream();
        System.out = new java.io.PrintStream(process.getOutputStream(), true);
        System.err = new java.io.PrintStream(process.getErrorStream(), true);
        */

        inThread = new StreamRedirectThread("in.redirect", System.in, process.getOutputStream());
        inThread.setDaemon(true);
        outThread = new StreamRedirectThread("out.redirect", process.getInputStream(), System.out);
        errThread = new StreamRedirectThread("err.redirect", process.getErrorStream(),  System.err);

        inThread.start();
        outThread.start();
        errThread.start();
    }

    public static void main(String args[]) {
        System.exit((new ProcessStarter()).run(args));
    }

}
