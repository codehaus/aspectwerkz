/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
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
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * Compile a proxy class for the delegation strategy.
 *
 * All interfaces methods are taken in the given interface order and implemented using delegation.
 * A single constructor is compiled wich accept each interface as argument
 *
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class ProxyDelegationCompiler {

    /**
     * Compile the proxy
     *
     * @param loader
     * @param interfaces
     * @param proxyClassName
     * @return
     */
    public static byte[] compileProxyFor(final ClassLoader loader, final Class[] interfaces, final String proxyClassName) {
        final ClassWriter proxyWriter = AsmHelper.newClassWriter(true);

        final Set methodSignatures = new HashSet();
        final String[] interfaceClassNames = new String[interfaces.length];
        final String[] interfaceSignatures = new String[interfaces.length];
        for (int i = 0; i < interfaces.length; i++) {
            interfaceClassNames[i] = interfaces[i].getName().replace('.', '/');
            interfaceSignatures[i] = 'L' + interfaceClassNames[i] + ';';
        }

        //FIXME copy interfaces class level annotations, and make sure we ignore doublons if any
        ProxyCompilerClassVisitor createProxy = new ProxyDelegationCompiler.ProxyCompilerClassVisitor(
                proxyWriter,
                proxyClassName.replace('.', '/'),
                methodSignatures,
                interfaceClassNames,
                interfaceSignatures
        );

        // visit each interface
        for (int i = 0; i < interfaces.length; i++) {
            Class anInterface = interfaces[i];
            final String interfaceClassName = anInterface.getName().replace('.', '/');

            InputStream in = null;
            final ClassReader classReader;
            try {
                if (loader != null) {
                    in = loader.getResourceAsStream(interfaceClassName + ".class");
                } else {
                    in = ClassLoader.getSystemClassLoader().getResourceAsStream(interfaceClassName + ".class");
                }
                classReader = new ClassReader(in);
            } catch (IOException e) {
                throw new WrappedRuntimeException("Cannot compile proxy for " + anInterface, e);
            } finally {
                try {
                    in.close();
                } catch (Throwable t) {
                    ;
                }
            }
            classReader.accept(createProxy, true);// no need for debug info
        }
        return proxyWriter.toByteArray();
    }

    /**
     * Proxy compiler. Ones can call accept as many times as needed.
     * visitEnd allow to track the index of the visited interface
     */
    public static class ProxyCompilerClassVisitor extends AsmNullAdapter.NullClassAdapter implements Opcodes, TransformationConstants {
        final ClassVisitor m_proxyCv;
        final String m_proxyClassName;
        final Set m_signatures;
        private int currentInterfaceIndex = 0;
        final String[] m_interfaceClassNames;
        final String[] m_interfaceSignatures;

        /**
         * Create the class, a field per interface, and the single constructor
         *
         * @param proxyCv
         * @param proxyClassName
         * @param signatures
         * @param interfaceClassNames
         */
        public ProxyCompilerClassVisitor(final ClassVisitor proxyCv, final String proxyClassName, final Set signatures, final String[] interfaceClassNames, final String[] interfaceSignatures) {
            //super(proxyCv);
            m_proxyCv = proxyCv;
            m_proxyClassName = proxyClassName;
            m_signatures = signatures;
            m_interfaceClassNames = interfaceClassNames;
            m_interfaceSignatures = interfaceSignatures;

            m_proxyCv.visit(
                    AsmHelper.JAVA_VERSION,
                    ACC_PUBLIC + ACC_SYNTHETIC + ACC_SUPER,
                    m_proxyClassName,
                    null,//FIXME generic is that correct ?
                    OBJECT_CLASS_NAME,
                    interfaceClassNames
            );

            // create one field per implemented interface
            for (int i = 0; i < interfaceClassNames.length; i++) {
                m_interfaceSignatures[i] = 'L' + interfaceClassNames[i] + ';';
                m_proxyCv.visitField(
                        ACC_PRIVATE + ACC_SYNTHETIC + ACC_FINAL,
                        "DELEGATE_" + i,
                        m_interfaceSignatures[i],
                        null,
                        null
                );
            }

            // create ctor
            StringBuffer ctorDesc = new StringBuffer("(");
            for (int i = 0; i < interfaceClassNames.length; i++) {
                ctorDesc.append(m_interfaceSignatures[i]);
            }
            ctorDesc.append(")V");
            MethodVisitor init = m_proxyCv.visitMethod(
                    ACC_PUBLIC + ACC_SYNTHETIC,
                    INIT_METHOD_NAME,
                    ctorDesc.toString(),
                    null,
                    null
            );
            init.visitVarInsn(ALOAD, 0);
            init.visitMethodInsn(INVOKESPECIAL, OBJECT_CLASS_NAME, INIT_METHOD_NAME, NO_PARAM_RETURN_VOID_SIGNATURE);
            for (int i = 0; i < interfaceClassNames.length; i++) {
                init.visitVarInsn(ALOAD, 0);
                init.visitVarInsn(ALOAD, 1 + i);
                init.visitFieldInsn(PUTFIELD, m_proxyClassName, "DELEGATE_" + i, m_interfaceSignatures[i]);
            }
            init.visitInsn(RETURN);
            init.visitMaxs(0, 0);
        }

        /**
         * Implement the interface method by delegating to the corresponding field
         *
         * @param access
         * @param name
         * @param desc
         * @param signature
         * @param exceptions
         * @return
         */
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            if (m_signatures.contains(name + desc)) {
                return super.visitMethod(access, name, desc, signature, exceptions);
            }
            m_signatures.add(name + desc);

            MethodVisitor cv = m_proxyCv.visitMethod(
                    access & ~ACC_ABSTRACT,
                    name,
                    desc,
                    signature,
                    exceptions
            );

            cv.visitVarInsn(ALOAD, 0);
            cv.visitFieldInsn(
                    GETFIELD,
                    m_proxyClassName,
                    "DELEGATE_" + currentInterfaceIndex,
                    m_interfaceSignatures[currentInterfaceIndex]
            );
            AsmHelper.loadArgumentTypes(cv, Type.getArgumentTypes(desc), false);
            cv.visitMethodInsn(
                    INVOKEINTERFACE,
                    m_interfaceClassNames[currentInterfaceIndex],
                    name,
                    desc
            );
            AsmHelper.addReturnStatement(cv, Type.getReturnType(desc));
            cv.visitMaxs(0, 0);

            // as we return cv we will copy the interface[currentInterfaceIndex] current method annotations
            // which is what we want
            return cv;
        }

        /**
         * Update the interface index for the next accept()
         */
        public void visitEnd() {
            currentInterfaceIndex++;
            super.visitEnd();
        }
    }

}
