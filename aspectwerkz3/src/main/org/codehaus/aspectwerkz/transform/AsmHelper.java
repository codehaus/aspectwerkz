/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.transform;

import org.codehaus.aspectwerkz.ContextClassLoader;
import org.codehaus.aspectwerkz.transform.TransformationConstants;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.CodeVisitor;
import org.objectweb.asm.Constants;
import org.objectweb.asm.Type;
import org.objectweb.asm.Label;
import org.objectweb.asm.ClassReader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 * Helper class with utility methods for the ASM library.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas BonŽr </a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur </a>
 */
public class AsmHelper implements Constants, TransformationConstants {

    /**
     * A boolean to check if we have a J2SE 5 support
     */
    public final static boolean IS_JAVA_5;
    public static int JAVA_VERSION = V1_3;

    static {
        Class annotation = null;
        try {
            annotation = Class.forName("java.lang.annotation.Annotation");
            ClassReader cr = new ClassReader("java.lang.annotation.Annotation");
            JAVA_VERSION = V1_5;
        } catch (Throwable e) {
            annotation = null;
        }
        if (annotation == null) {
            IS_JAVA_5 = false;
        } else {
            IS_JAVA_5 = true;
        }
    }

    /**
     * Factory method for ASM ClassWriter and J2SE 5 support
     * See http://www.objectweb.org/wws/arc/asm/2004-08/msg00005.html
     *
     * @param computeMax
     * @return
     */
    public static ClassWriter newClassWriter(boolean computeMax) {
        return new ClassWriter(computeMax, true);
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
                    } else if (d == Void.TYPE) {
                        car = 'V';
                    } else if (d == Boolean.TYPE) {
                        car = 'Z';
                    } else if (d == Byte.TYPE) {
                        car = 'B';
                    } else if (d == Character.TYPE) {
                        car = 'C';
                    } else if (d == Short.TYPE) {
                        car = 'S';
                    } else if (d == Double.TYPE) {
                        car = 'D';
                    } else if (d == Float.TYPE) {
                        car = 'F';
                    } else /* if (d == Long.TYPE) */ {
                        car = 'J';
                    }
                    buf.append(car);
                    break;
                } else if (d.isArray()) {
                    buf.append('[');
                    d = d.getComponentType();
                } else {
                    buf.append('L');
                    String name = d.getName();
                    int len = name.length();
                    for (int i1 = 0; i1 < len; ++i1) {
                        char car = name.charAt(i1);
                        buf.append((car == '.') ? '/' : car);
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
     * Creates a method descriptor.
     * <p/>
     * Parts of code in this method is taken from the ASM codebase.
     *
     * @param method
     * @return the descriptor
     */
    public static String getMethodDescriptor(final Method method) {
        Class[] parameters = method.getParameterTypes();
        StringBuffer buf = new StringBuffer();
        buf.append('(');
        for (int i = 0; i < parameters.length; ++i) {
            Class d = parameters[i];
            while (true) {
                if (d.isPrimitive()) {
                    char car;
                    if (d == Integer.TYPE) {
                        car = 'I';
                    } else if (d == Void.TYPE) {
                        car = 'V';
                    } else if (d == Boolean.TYPE) {
                        car = 'Z';
                    } else if (d == Byte.TYPE) {
                        car = 'B';
                    } else if (d == Character.TYPE) {
                        car = 'C';
                    } else if (d == Short.TYPE) {
                        car = 'S';
                    } else if (d == Double.TYPE) {
                        car = 'D';
                    } else if (d == Float.TYPE) {
                        car = 'F';
                    } else /* if (d == Long.TYPE) */ {
                        car = 'J';
                    }
                    buf.append(car);
                    break;
                } else if (d.isArray()) {
                    buf.append('[');
                    d = d.getComponentType();
                } else {
                    buf.append('L');
                    String name = d.getName();
                    int len = name.length();
                    for (int i1 = 0; i1 < len; ++i1) {
                        char car = name.charAt(i1);
                        buf.append((car == '.') ? '/' : car);
                    }
                    buf.append(';');
                    break;
                }
            }
        }
        buf.append(")");
        //FIXME handles return type
        return buf.toString();
    }

    /**
     * Gets the argument types for a constructor. <p/>Parts of code in this method is taken from the ASM codebase.
     *
     * @param constructor
     * @return the ASM argument types for the constructor
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
     * Adds a class to a class loader and loads it.
     *
     * @param loader the class loader (if null the context class loader will be used)
     * @param bytes  the bytes for the class
     * @param name   the name of the class
     * @return the class
     */
    public static Class loadClass(ClassLoader loader, final byte[] bytes, final String name) {
        String className = name.replace('/', '.');
        try {
            if (loader == null) {
                loader = ContextClassLoader.getLoader();
            }
            Class klass = loader.loadClass(CLASS_LOADER_REFLECT_CLASS_NAME);
            Method method = klass.getDeclaredMethod(
                    DEFINE_CLASS_METHOD_NAME, new Class[]{
                        String.class, byte[].class, int.class, int.class
                    }
            );

            // TODO: what if we don't have rights to set this method to
            // accessible on this specific CL? Load it in System CL?
            method.setAccessible(true);
            Object[] args = new Object[]{
                className, bytes, new Integer(0), new Integer(bytes.length)
            };
            System.out.println("-->AsmHelper.loadClass  " + className + " in "  + loader);
            Class clazz = (Class) method.invoke(loader, args);

            method.setAccessible(false);
            return clazz;

        } catch (InvocationTargetException e) {
            // JIT failovering for Thread concurrency
            // AW-222 (Tomcat and WLS were reported for AW-222)
            if (e.getTargetException() instanceof LinkageError) {
                Class failoverJoinpointClass = loadClass(loader, className);
                if (failoverJoinpointClass != null) {
                    return failoverJoinpointClass;
                }
            }
            throw new WrappedRuntimeException(e);
        } catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Tries to load a class if unsuccessful returns null.
     *
     * @param loader the class loader
     * @param name   the name of the class
     * @return the class
     */
    public static Class loadClass(ClassLoader loader, final String name) {
        String className = name.replace('/', '.');
        try {
            if (loader == null) {
                loader = ContextClassLoader.getLoader();
            }
            // use Class.forName since loader.loadClass leads to error on JBoss UCL
            return Class.forName(className, false, loader);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Calculates the method hash. The computation MUST be the same as in ReflectHelper, thus we switch back the names
     * to Java style. Note that for array type, Java.reflect is using "[Lpack.foo;" style unless primitive.
     *
     * @param name
     * @param desc
     * @return
     */
    public static int calculateMethodHash(final String name, final String desc) {
        int hash = 17;
        hash = (37 * hash) + name.replace('/', '.').hashCode();
        Type[] argumentTypes = Type.getArgumentTypes(desc);
        for (int i = 0; i < argumentTypes.length; i++) {
            hash = (37 * hash)
                   + AsmHelper.convertTypeDescToReflectDesc(argumentTypes[i].getDescriptor()).hashCode();
        }
        return hash;
    }

    /**
     * Calculates the constructor hash.
     *
     * @param desc
     * @return
     */
    public static int calculateConstructorHash(final String desc) {
        return AsmHelper.calculateMethodHash(INIT_METHOD_NAME, desc);
    }

    /**
     * Calculates the field hash.
     *
     * @param name
     * @param desc
     * @return
     */
    public static int calculateFieldHash(final String name, final String desc) {
        int hash = 17;
        hash = (37 * hash) + name.hashCode();
        Type type = Type.getType(desc);
        hash = (37 * hash) + AsmHelper.convertTypeDescToReflectDesc(type.getDescriptor()).hashCode();
        return hash;
    }

    /**
     * Calculates the class hash.
     *
     * @param declaringType
     * @return
     */
    public static int calculateClassHash(final String declaringType) {
        return AsmHelper.convertTypeDescToReflectDesc(declaringType).hashCode();
    }

    /**
     * Converts an ASM type descriptor" (I, [I, [Ljava/lang/String;, Ljava/lang/String;) to a Java.reflect one (int, [I,
     * [Ljava.lang.String;, java.lang.String)
     *
     * @param typeDesc
     * @return the Java.reflect string representation
     */
    public static String convertTypeDescToReflectDesc(final String typeDesc) {
        if (typeDesc == null) {
            return null;
        }
        String result = null;
        // change needed for array types only
        if (typeDesc.startsWith("[")) {
            result = typeDesc;
        } else {
            // support for single dimension type
            if (typeDesc.startsWith("L") && typeDesc.endsWith(";")) {
                result = typeDesc.substring(1, typeDesc.length() - 1);
            } else {
                // primitive type, single dimension
                if (typeDesc.equals("I")) {
                    result = "int";
                } else if (typeDesc.equals("J")) {
                    result = "long";
                } else if (typeDesc.equals("S")) {
                    result = "short";
                } else if (typeDesc.equals("F")) {
                    result = "float";
                } else if (typeDesc.equals("D")) {
                    result = "double";
                } else if (typeDesc.equals("Z")) {
                    result = "boolean";
                } else if (typeDesc.equals("C")) {
                    result = "char";
                } else if (typeDesc.equals("B")) {
                    result = "byte";
                } else {
                    throw new RuntimeException("unknown primitive type " + typeDesc);
                }
            }
        }
        return result.replace('/', '.');
    }

    /**
     * Converts a java reflect type desc to ASM type desc.
     *
     * @param desc
     * @return
     */
    public static String convertReflectDescToTypeDesc(final String desc) {
        if (desc == null) {
            return null;
        }
        String typeDesc = desc;
        int dimension = 0;
        char[] arr = desc.toCharArray();
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == ']') {
                dimension++;
            }
        }
        typeDesc = desc.substring(0, desc.length() - dimension * 2);
        if (typeDesc.equals("int")) {
            typeDesc = "I";
        } else if (typeDesc.equals("short")) {
            typeDesc = "S";
        } else if (typeDesc.equals("long")) {
            typeDesc = "J";
        } else if (typeDesc.equals("float")) {
            typeDesc = "F";
        } else if (typeDesc.equals("double")) {
            typeDesc = "D";
        } else if (typeDesc.equals("byte")) {
            typeDesc = "B";
        } else if (typeDesc.equals("char")) {
            typeDesc = "C";
        } else if (typeDesc.equals("boolean")) {
            typeDesc = "Z";
        } else {
            typeDesc = 'L' + typeDesc + ';';
        }
        for (int i = 0; i < dimension; i++) {
            typeDesc = '[' + typeDesc;
        }
        return typeDesc.replace('.', '/');
    }

    /**
     * Creates and adds the correct parameter index for integer types.
     *
     * @param cv
     * @param index
     */
    public static void loadIntegerConstant(final CodeVisitor cv, final int index) {
        switch (index) {
            case 0:
                cv.visitInsn(ICONST_0);
                break;
            case 1:
                cv.visitInsn(ICONST_1);
                break;
            case 2:
                cv.visitInsn(ICONST_2);
                break;
            case 3:
                cv.visitInsn(ICONST_3);
                break;
            case 4:
                cv.visitInsn(ICONST_4);
                break;
            case 5:
                cv.visitInsn(ICONST_5);
                break;
            default:
                cv.visitIntInsn(BIPUSH, index);
                break;
        }
    }

}