/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.proxy;

import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.transform.TransformationConstants;
import org.codehaus.aspectwerkz.transform.inlining.AsmHelper;
import org.codehaus.aspectwerkz.transform.inlining.AsmNullAdapter;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.CodeVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.attrs.Attributes;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;

/**
 * Compiler for the AspectWerkz proxies.
 * <p/>
 * Creates a subclass of the target class and adds delegate methods to all the non-private and non-final
 * methods/constructors which delegates to the super class.
 * <p/>
 * The annotations are copied.
 *
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 * @author <a href="mailto:jboner@codehaus.org">Jonas BonŽr</a>
 */
public class ProxySubclassingCompiler implements TransformationConstants {

//    /**
//     * Returns an InputStream that would be the one of the AWproxy for the given proxy class name
//     * Used to read annotations from proxy f.e.
//     *
//     * @param loader
//     * @param proxyClassName
//     * @return or null if not found
//     */
//    public static InputStream getProxyResourceAsStream(final ClassLoader loader, final String proxyClassName) {
//        String className = Proxy.getUniqueClassNameFromProxy(proxyClassName);
//        if (className != null) {
//            byte[] proxy = compileProxyFor(loader, className, proxyClassName);
//            return new BufferedInputStream(new ByteArrayInputStream(proxy));
//        } else {
//            return null;
//        }
//    }

    /**
     * Compiles a new proxy for the class specified.
     *
     * @param clazz
     * @param proxyClassName
     * @return the byte code
     */
    public static byte[] compileProxyFor(final Class clazz, final String proxyClassName) {
        return compileProxyFor(clazz.getClassLoader(), clazz.getName(), proxyClassName);
    }

    /**
     * Compiles a new proxy for the class specified.
     *
     * @param loader
     * @param className
     * @param proxyClassName
     * @return the byte code
     */
    public static byte[] compileProxyFor(final ClassLoader loader, final String className, final String proxyClassName) {

        final String targetClassName = className.replace('.', '/');
        final ClassWriter proxyWriter = AsmHelper.newClassWriter(true);

        InputStream in = null;
        final ClassReader classReader;
        try {
            if (loader != null) {
                in = loader.getResourceAsStream(targetClassName + ".class");
            } else {
                in = ClassLoader.getSystemClassLoader().getResourceAsStream(targetClassName + ".class");
            }
            classReader = new ClassReader(in);
        } catch (IOException e) {
            throw new WrappedRuntimeException("Cannot compile proxy for " + className, e);
        } finally {
            try {
                in.close();
            } catch (Throwable t) {
                ;
            }
        }

        ClassVisitor createProxy = new ProxyCompilerClassVisitor(proxyWriter, proxyClassName.replace('.', '/'));
        classReader.accept(createProxy, Attributes.getDefaultAttributes(), true);// no need for debug info
        return proxyWriter.toByteArray();
    }

    /**
     * Visit the class and create the proxy that delegates to super.
     *
     * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
     */
    private static class ProxyCompilerClassVisitor extends AsmNullAdapter.NullClassAdapter {

        final private ClassVisitor m_proxyCv;
        final private String m_proxyClassName;
        private String m_className;

        public ProxyCompilerClassVisitor(final ClassVisitor proxyCv, final String proxyClassName) {
            m_proxyCv = proxyCv;
            m_proxyClassName = proxyClassName;
        }

        /**
         * Visits the class.
         *
         * @param access
         * @param name
         * @param superName
         * @param interfaces
         * @param sourceFile
         */
        public void visit(final int version,
                          final int access,
                          final String name,
                          final String superName,
                          final String[] interfaces,
                          final String sourceFile) {
            if (Modifier.isFinal(access)) {
                throw new RuntimeException("Cannot create a proxy from final class " + name);
            }
            m_className = name;
            m_proxyCv.visit(
                    version,
                    ACC_PUBLIC + ACC_SUPER + ACC_SYNTHETIC,
                    m_proxyClassName.replace('.', '/'),
                    name,
                    interfaces,
                    null
            );
        }

        /**
         * Visits the methods.
         *
         * @param access
         * @param name
         * @param desc
         * @param exceptions
         * @param attrs
         * @return
         */
        public CodeVisitor visitMethod(final int access,
                                       final String name,
                                       final String desc,
                                       final String[] exceptions,
                                       final Attribute attrs) {
            if (Modifier.isFinal(access) || Modifier.isPrivate(access) || Modifier.isNative(access)) {
                // skip final or private or native methods
                // TODO we could proxy native methods but that would lead to difference with regular weaving
                ;
            } else if (CLINIT_METHOD_NAME.equals(name)) {
                // skip clinit
                ;
            } else if (INIT_METHOD_NAME.equals(name)) {
                // constructors
                CodeVisitor proxyCode = m_proxyCv.visitMethod(
                        access + ACC_SYNTHETIC,
                        INIT_METHOD_NAME,
                        desc,
                        exceptions,
                        attrs
                );

                proxyCode.visitVarInsn(ALOAD, 0);
                AsmHelper.loadArgumentTypes(proxyCode, Type.getArgumentTypes(desc), false);
                proxyCode.visitMethodInsn(INVOKESPECIAL, m_className, INIT_METHOD_NAME, desc);
                proxyCode.visitInsn(RETURN);
                proxyCode.visitMaxs(0, 0);
            } else {
                // method that can be proxied
                CodeVisitor proxyCode = m_proxyCv.visitMethod(
                        access + ACC_SYNTHETIC,
                        name,
                        desc,
                        exceptions,
                        attrs
                );

                if (Modifier.isStatic(access)) {
                    AsmHelper.loadArgumentTypes(proxyCode, Type.getArgumentTypes(desc), true);
                    proxyCode.visitMethodInsn(INVOKESTATIC, m_className, name, desc);
                    AsmHelper.addReturnStatement(proxyCode, Type.getReturnType(desc));
                    proxyCode.visitMaxs(0, 0);
                } else {
                    proxyCode.visitVarInsn(ALOAD, 0);
                    AsmHelper.loadArgumentTypes(proxyCode, Type.getArgumentTypes(desc), false);
                    proxyCode.visitMethodInsn(INVOKESPECIAL, m_className, name, desc);
                    AsmHelper.addReturnStatement(proxyCode, Type.getReturnType(desc));
                    proxyCode.visitMaxs(0, 0);
                }
            }

            return AsmNullAdapter.NullCodeAdapter.NULL_CODE_ADAPTER;
        }
    }

//    /**
//     * Creates constructors that delgates to the matching base class constructors.
//     * Skips all private constructors.
//     *
//     * @param writer
//     * @param clazz
//     * @param targetClassName
//     */
//    private static void createConstructorDelegators(final ClassWriter writer,
//                                                    final Class clazz,
//                                                    final String targetClassName) {
//        CodeVisitor cv;
//        Constructor[] constructors = clazz.getDeclaredConstructors();
//        for (int i = 0; i < constructors.length; i++) {
//            Constructor constructor = constructors[i];
//            int mods = constructor.getModifiers();
//            if (!Modifier.isPrivate(mods) && !Modifier.isFinal(mods)) {
//                Class[] exceptionClasses = constructor.getExceptionTypes();
//                String[] exceptionTypeNames = new String[constructor.getExceptionTypes().length];
//                for (int j = 0; j < exceptionTypeNames.length; j++) {
//                    exceptionTypeNames[j] = exceptionClasses[j].getName().replace('.', '/');
//                }
//                final String desc = ReflectHelper.getConstructorSignature(constructor);
//                cv = writer.visitMethod(
//                        mods + ACC_SYNTHETIC,
//                        INIT_METHOD_NAME,
//                        desc,
//                        exceptionTypeNames,
//                        null
//                );
//
//                cv.visitVarInsn(ALOAD, 0);
//                AsmHelper.loadArgumentTypes(cv, Type.getArgumentTypes(desc), false);
//
//                cv.visitMethodInsn(INVOKESPECIAL, targetClassName, INIT_METHOD_NAME, desc);
//
//                cv.visitInsn(RETURN);
//                cv.visitMaxs(0, 0);
//            }
//        }
//    }
//
//    /**
//     * Creates method methods that delgates to the base class method.
//     * Skips all private and final methods.
//     *
//     * @param writer
//     * @param clazz
//     * @param targetClassName
//     */
//    private static void createMethodDelegators(final ClassWriter writer,
//                                               final Class clazz,
//                                               final String targetClassName) {
//        CodeVisitor cv;
//        Method[] methods = clazz.getDeclaredMethods();
//        for (int i = 0; i < methods.length; i++) {
//            Method method = methods[i];
//            int mods = method.getModifiers();
//            if (!Modifier.isPrivate(mods) && !Modifier.isFinal(mods)) {
//
//                Class[] exceptionClasses = method.getExceptionTypes();
//                String[] exceptionTypeNames = new String[method.getExceptionTypes().length];
//                for (int j = 0; j < exceptionTypeNames.length; j++) {
//                    exceptionTypeNames[j] = exceptionClasses[j].getName().replace('.', '/');
//                }
//                final String methodName = method.getName();
//                final String desc = Type.getMethodDescriptor(method);
//
//                cv = writer.visitMethod(
//                        mods + ACC_SYNTHETIC,
//                        methodName,
//                        desc,
//                        exceptionTypeNames,
//                        null
//                );
//
//                if (Modifier.isStatic(mods)) {
//                    AsmHelper.loadArgumentTypes(cv, Type.getArgumentTypes(desc), true);
//                    cv.visitMethodInsn(INVOKESTATIC, targetClassName, methodName, desc);
//                } else {
//                    cv.visitVarInsn(ALOAD, 0);
//                    AsmHelper.loadArgumentTypes(cv, Type.getArgumentTypes(desc), false);
//                    cv.visitMethodInsn(INVOKESPECIAL, targetClassName, methodName, desc);
//                }
//
//                AsmHelper.addReturnStatement(cv, Type.getReturnType(method));
//                cv.visitMaxs(0, 0);
//            }
//        }
//    }
}
