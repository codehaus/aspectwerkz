/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.File;

import org.objectweb.asm.CodeVisitor;
import org.objectweb.asm.Constants;
import org.objectweb.asm.Type;
import org.objectweb.asm.ClassWriter;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.ContextClassLoader;

/**
 * Utility methods for the ASM library.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class AsmHelper {

    private static final String CLASS_LOADER_CLASS_NAME = "java.lang.ClassLoader";
    private static final String DEFINE_CLASS_METHOD_NAME = "defineClass";

    /**
     * Creates and adds the correct parameter index.
     *
     * @param cv
     * @param index
     */
    public static void setICONST_X(final CodeVisitor cv, final int index) {
        switch (index) {
            case 0:
                cv.visitInsn(Constants.ICONST_0);
                break;
            case 1:
                cv.visitInsn(Constants.ICONST_1);
                break;
            case 2:
                cv.visitInsn(Constants.ICONST_2);
                break;
            case 3:
                cv.visitInsn(Constants.ICONST_3);
                break;
            case 4:
                cv.visitInsn(Constants.ICONST_4);
                break;
            case 5:
                cv.visitInsn(Constants.ICONST_5);
                break;
            default:
                cv.visitIntInsn(Constants.LDC, index);
                break;
        }
    }

    /**
     * Creates a constructor descriptor.
     * <p/>
     * Parts of code in this method is taken from the ASM codebase.
     *
     * @param constructor
     * @return the descriptor
     */
    public static String getConstructorDescriptor(final Constructor constructor) {
        Class[] parameters = constructor.getParameterTypes();
        StringBuffer buf = new StringBuffer();
        buf.append('(');
        for (int i = 0; i < parameters.length; ++i) {
            Class d = parameters[i];
            while (true) {
                if (d.isPrimitive()) {
                    char car;
                    if (d == Integer.TYPE) {
                        car = 'I';
                    }
                    else if (d == Void.TYPE) {
                        car = 'V';
                    }
                    else if (d == Boolean.TYPE) {
                        car = 'Z';
                    }
                    else if (d == Byte.TYPE) {
                        car = 'B';
                    }
                    else if (d == Character.TYPE) {
                        car = 'C';
                    }
                    else if (d == Short.TYPE) {
                        car = 'S';
                    }
                    else if (d == Double.TYPE) {
                        car = 'D';
                    }
                    else if (d == Float.TYPE) {
                        car = 'F';
                    }
                    else /*if (d == Long.TYPE)*/ {
                        car = 'J';
                    }
                    buf.append(car);
                    break;
                }
                else if (d.isArray()) {
                    buf.append('[');
                    d = d.getComponentType();
                }
                else {
                    buf.append('L');
                    String name = d.getName();
                    int len = name.length();
                    for (int i1 = 0; i1 < len; ++i1) {
                        char car = name.charAt(i1);
                        buf.append(car == '.' ? '/' : car);
                    }
                    buf.append(';');
                    break;
                }
            }
        }
        buf.append(")V");
        return buf.toString();
    }

    /**
     * Gets the argument types for a constructor.
     * <p/>
     * Parts of code in this method is taken from the ASM codebase.
     *
     * @param constructor
     * @return the argument types for the constructor
     */
    public static Type[] getArgumentTypes(final Constructor constructor) {
        Class[] classes = constructor.getParameterTypes();
        Type[] types = new Type[classes.length];
        for (int i = classes.length - 1; i >= 0; --i) {
            types[i] = Type.getType(classes[i]);
        }
        return types;
    }

    /**
     * Dumps an ASM class to disk.
     *
     * @param dumpDir
     * @param className
     * @param cw
     * @throws java.io.IOException
     */
    public static void dumpClass(final String dumpDir, final String className, final ClassWriter cw) throws IOException {
        System.out.println("Dumping class: " + className + " to: " + dumpDir);
        FileOutputStream os = new FileOutputStream(dumpDir + File.separator + className.replace('/', '_') + ".class");
        os.write(cw.toByteArray());
        os.close();
    }

    /**
     * Adds a class to the context class loader and loads it.
     *
     * @param bytes the bytes for the class
     * @param name  the name of the class
     * @return the class
     */
    public static Class loadClass(final byte[] bytes, final String name) {
        try {
//            ClassLoader loader = ClassLoader.getSystemClassLoader();
            ClassLoader loader = Thread.currentThread().getContextClassLoader();

            Class cls = Class.forName(CLASS_LOADER_CLASS_NAME);
            Method method =
                    cls.getDeclaredMethod(
                            DEFINE_CLASS_METHOD_NAME, new Class[]{String.class, byte[].class, int.class, int.class}
                    );

            // TODO: what if we don't have rights to set this method to accessible on this specific CL? Load it in System CL?
            method.setAccessible(true);
            Object[] args = new Object[]{name, bytes, new Integer(0), new Integer(bytes.length)};

            Class clazz = (Class)method.invoke(loader, args);
            method.setAccessible(false);

            return clazz;
        }
        catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Tries to load a class if unsuccessful returns null.
     *
     * @param name  the name of the class
     * @return the class
     */
    public static Class loadClass(final String name) {
        try {
            return ContextClassLoader.loadClass(name);
        }
        catch (Exception e) {
            return null;
        }
    }
}
