/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.proxy;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Constructor;
import java.io.IOException;

import org.codehaus.aspectwerkz.transform.TransformationConstants;
import org.codehaus.aspectwerkz.transform.inlining.AsmHelper;
import org.codehaus.aspectwerkz.reflect.ReflectHelper;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.CodeVisitor;
import org.objectweb.asm.Type;

/**
 * Compiler for the AspectWerkz proxies.
 * <p/>
 * Creates a subclass of the target class and adds delegate methods to all the non-private and non-final
 * methods/constructors which delegates to the super class.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas BonŽr</a>
 */
public class ProxyCompiler implements TransformationConstants {
    private final static String[] EMPTY_STRING_ARRAY = new String[0];

    /**
     * Compiles a new proxy for the class specified.
     *
     * @param clazz
     * @param proxyClassName
     * @return the byte code
     */
    public static byte[] compileProxyFor(final Class clazz, final String proxyClassName) {

        final String targetClassName = clazz.getName().replace('.', '/');
        final ClassWriter writer = AsmHelper.newClassWriter(true);

        writer.visit(
                AsmHelper.JAVA_VERSION,
                ACC_PUBLIC + ACC_SUPER + ACC_SYNTHETIC,
                proxyClassName,
                targetClassName,
                EMPTY_STRING_ARRAY,
                null
        );

        createConstructorDelegators(writer, clazz, targetClassName);
        createMethodDelegators(writer, clazz, targetClassName);

        return writer.toByteArray();
    }

    /**
     * Creates constructors that delgates to the matching base class constructors.
     * Skips all private constructors.
     *
     * @param writer
     * @param clazz
     * @param targetClassName
     */
    private static void createConstructorDelegators(final ClassWriter writer,
                                                    final Class clazz,
                                                    final String targetClassName) {
        CodeVisitor cv;
        Constructor[] constructors = clazz.getDeclaredConstructors();
        for (int i = 0; i < constructors.length; i++) {
            Constructor constructor = constructors[i];
            int mods = constructor.getModifiers();
            if (!Modifier.isPrivate(mods) && !Modifier.isFinal(mods)) {
                Class[] exceptionClasses = constructor.getExceptionTypes();
                String[] exceptionTypeNames = new String[constructor.getExceptionTypes().length];
                for (int j = 0; j < exceptionTypeNames.length; j++) {
                    exceptionTypeNames[j] = exceptionClasses[j].getName().replace('.', '/');
                }
                final String desc = ReflectHelper.getConstructorSignature(constructor);
                cv = writer.visitMethod(
                        mods + ACC_SYNTHETIC,
                        INIT_METHOD_NAME,
                        desc,
                        exceptionTypeNames,
                        null
                );

                cv.visitVarInsn(ALOAD, 0);
                AsmHelper.loadArgumentTypes(cv, Type.getArgumentTypes(desc), false);

                cv.visitMethodInsn(INVOKESPECIAL, targetClassName, INIT_METHOD_NAME, desc);

                cv.visitInsn(RETURN);
                cv.visitMaxs(0, 0);
            }
        }
    }

    /**
     * Creates method methods that delgates to the base class method.
     * Skips all private and final methods.
     *
     * @param writer
     * @param clazz
     * @param targetClassName
     */
    private static void createMethodDelegators(final ClassWriter writer,
                                               final Class clazz,
                                               final String targetClassName) {
        CodeVisitor cv;
        Method[] methods = clazz.getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            int mods = method.getModifiers();
            if (!Modifier.isPrivate(mods) && !Modifier.isFinal(mods)) {

                Class[] exceptionClasses = method.getExceptionTypes();
                String[] exceptionTypeNames = new String[method.getExceptionTypes().length];
                for (int j = 0; j < exceptionTypeNames.length; j++) {
                    exceptionTypeNames[j] = exceptionClasses[j].getName().replace('.', '/');
                }
                final String methodName = method.getName();
                final String desc = Type.getMethodDescriptor(method);

                cv = writer.visitMethod(
                        mods + ACC_SYNTHETIC,
                        methodName,
                        desc,
                        exceptionTypeNames,
                        null
                );

                if (Modifier.isStatic(mods)) {
                    AsmHelper.loadArgumentTypes(cv, Type.getArgumentTypes(desc), true);
                    cv.visitMethodInsn(INVOKESTATIC, targetClassName, methodName, desc);
                } else {
                    cv.visitVarInsn(ALOAD, 0);
                    AsmHelper.loadArgumentTypes(cv, Type.getArgumentTypes(desc), false);
                    cv.visitMethodInsn(INVOKESPECIAL, targetClassName, methodName, desc);
                }

                AsmHelper.addReturnStatement(cv, Type.getReturnType(method));
                cv.visitMaxs(0, 0);
            }
        }
    }
}
