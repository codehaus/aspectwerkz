/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.hook;

import com.sun.jdi.VirtualMachine;
import com.sun.jdi.Bootstrap;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.Connector;

import java.io.*;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.net.ConnectException;

/**
 * Utility methods to manipulate class redefinition of java.lang.ClassLoader in xxxStarter
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class ClassLoaderPatcher {

    /**
     * Converts an input stream to a byte[]
     */
    public static byte[] inputStreamToByteArray(InputStream is) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        for (int b = is.read(); b != -1; b = is.read()) {
            os.write(b);
        }
        return os.toByteArray();
    }

    /**
     * Gets the bytecode of the modified java.lang.ClassLoader using given ClassLoaderPreProcessor class name
     */
    static byte[] getPatchedClassLoader(String preProcessorName) {
        byte[] abyte = null;
        try {
            InputStream is = ClassLoader.getSystemClassLoader().getParent().
                    getResourceAsStream("java/lang/ClassLoader.class");
            abyte = inputStreamToByteArray(is);
            is.close();
        } catch (IOException e) {
            throw new Error("failed to read java.lang.ClassLoader: " + e.toString());
        }

        if (preProcessorName != null) {
            try {
                ClassLoaderPreProcessor clpi = (ClassLoaderPreProcessor) Class.forName(preProcessorName).newInstance();
                abyte = clpi.preProcess(abyte);
            } catch (Exception e) {
                System.err.println("failed to instrument java.lang.ClassLoader: preprocessor not found");
                e.printStackTrace();
            }
        }

        return abyte;
    }

    /**
     * Dump bytecode bytes in dir/className.class directory, created if needed
     */
    private static void writeClass(String className, byte[] bytes, String dir) {
        String filename = dir + File.separatorChar + className.replace('.', File.separatorChar) + ".class";
        int pos = filename.lastIndexOf(File.separatorChar);
        if (pos > 0) {
            String finalDir = filename.substring(0, pos);
            (new File(finalDir)).mkdirs();
        }
        try {
            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(filename)));
            out.write(bytes);
            out.close();
        } catch (IOException e) {
            System.err.println("failed to write " + className + " in " + dir);
            e.printStackTrace();
        }
    }

    /**
     * HotSwap className in target VM
     */
    private static void redefineClass(VirtualMachine vm, String className, byte bytes[]) {
        // determine if VM support class HotSwap with introspection
        try {
            Method canM = VirtualMachine.class.getMethod("canRedefineClasses", new Class[]{});
            if (((Boolean) canM.invoke(vm, new Object[]{})).equals(Boolean.FALSE)) {
                throw new Error("target JVM cannot redefine classes, please force the use of -Xbootclasspath");
            }
            List classList = vm.classesByName(className);
            if (classList.size() == 0)
                throw new Error("Fatal error: Can't find class " + className);
            ReferenceType rt = (ReferenceType) classList.get(0);
            Map map = new HashMap();
            map.put(rt, bytes);
            Method doM = VirtualMachine.class.getMethod("redefineClasses", new Class[]{Map.class});
            doM.invoke(vm, new Object[]{map});
        } catch (NoSuchMethodException e) {
            // java 1.3 or not HotSwap compatible JVM
            throw new Error("target JVM cannot redefine classes, please force the use of -Xbootclasspath");
        } catch (InvocationTargetException e) {
            // java 1.4+ failure
            System.err.println("failed to HotSwap " + className + ":");
            e.getTargetException().printStackTrace();
            throw new Error("try to force force the use of -Xbootclasspath");
        } catch (IllegalAccessException e) {
            // java 1.4+ failure
            System.err.println("failed to HotSwap " + className + ":");
            e.printStackTrace();
            throw new Error("try to force force the use of -Xbootclasspath");
        }
    }

    /**
     * Patch java.lang.ClassLoader with preProcessorName instance and dump class bytecode in dir
     */
    public static void patchClassLoader(String preProcessorName, String dir) {
        byte[] cl = getPatchedClassLoader(preProcessorName);
        writeClass("java.lang.ClassLoader", cl, dir);
    }

    /**
     * Patch java.lang.ClassLoader with preProcessorName instance and hotswap in target VM using a JDWP attaching connector
     * Don't wait before connecting
     */
    public static VirtualMachine hotswapClassLoader(String preProcessorName, String transport, String address) {
        return hotswapClassLoader(preProcessorName, transport, address, 0);
    }

    /**
     * Patch java.lang.ClassLoader with preProcessorName instance and hotswap in target VM using a JDWP attaching connector
     */
    public static VirtualMachine hotswapClassLoader(String preProcessorName, String transport, String address, int secondsToWait) {
        String name = null;
        if ("dt_socket".equals(transport))
            name = "com.sun.jdi.SocketAttach";
        else if ("dt_shmem".equals(transport))
            name = "com.sun.jdi.SharedMemoryAttach";

        AttachingConnector connector = null;
        for (Iterator i = Bootstrap.virtualMachineManager().attachingConnectors().iterator(); i.hasNext();) {
            AttachingConnector aConnector = (AttachingConnector) i.next();
            if (aConnector.name().equals(name)) {
                connector = aConnector;
                break;
            }
        }
        if (connector == null)
            throw new Error("no AttachingConnector for transport: " + transport);

        Map args = connector.defaultArguments();
        if ("dt_socket".equals(transport)) {
            ((Connector.Argument) args.get("port")).setValue(address);
        } else if ("dt_shmem".equals(transport)) {
            ((Connector.Argument) args.get("name")).setValue(address);
        }

        try {
            if (secondsToWait > 0) {
                try {
                    Thread.sleep(1000 * secondsToWait);
                } catch (Exception e) {
                    ;
                }
            }
            // loop 10 times, during 5 sec max. It appears some VM under Linux take time to accept connections
            // this avoid to specifically set -Daspectwerkz.classloader.wait
            VirtualMachine vm = null;
            ConnectException vmConnectionRefused = new ConnectException("should not appear as is");
            for (int retry = 0; retry < 10; retry++) {
                try {
                    vm = connector.attach(args);
                    break;
                } catch (ConnectException ce) {
                    vmConnectionRefused = ce;
                    try {
                        Thread.sleep(500);
                    } catch (Throwable t) {
                        ;
                    }
                }
            }
            if (vm == null) {
                throw vmConnectionRefused;
            }
            redefineClass(vm, "java.lang.ClassLoader", getPatchedClassLoader(preProcessorName));
            return vm;
        } catch (IllegalConnectorArgumentsException e) {
            System.err.println("failed to attach to VM (" + transport + ", " + address + "):");
            e.printStackTrace();
            for (Iterator i = e.argumentNames().iterator(); i.hasNext();) {
                System.err.println("wrong or missing argument - " + i.next());
            }
            return null;
        } catch (IOException e) {
            System.err.println("failed to attach to VM (" + transport + ", " + address + "):");
            e.printStackTrace();
            return null;
        }
    }
}
