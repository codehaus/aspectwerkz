/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.hook.impl;

import org.codehaus.aspectwerkz.hook.ClassLoaderPreProcessor;
import org.codehaus.aspectwerkz.hook.ClassLoaderPatcher;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.Constants;
import org.apache.bcel.generic.*;

import java.io.InputStream;
import java.io.ByteArrayInputStream;

/**
 * Instruments the java.lang.ClassLoader to plug in the Class PreProcessor
 * mechanism using BCEL.
 *
 * We are using a lazy initialization of the class preprocessor to allow all class
 * pre processor logic to be in system classpath and not in bootclasspath
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 * @version $Id: ClassLoaderPreProcessorImpl.java,v 1.2 2003-07-23 14:20:32 avasseur Exp $
 */
public class ClassLoaderPreProcessorImpl implements ClassLoaderPreProcessor {

    public ClassLoaderPreProcessorImpl() {
    }

    public byte[] preProcess(byte[] b) {
        try {
            // Implementation note: add modified ClassLoader in bcel/lib/ and add wished class in bootclasspath
            // then turn on BCELifier to learn more about BCEL
            //org.apache.bcel.util.BCELifier bc = new org.apache.bcel.util.BCELifier(repository.loadClass("java.lang.ClassLoader"), System.out);
            //bc.start();

            final ClassGen cg = new ClassGen(
                    (new ClassParser(new ByteArrayInputStream(b), "<generated>")).parse());
            final String className = cg.getClassName();
            final Method[] methods = cg.getMethods();
            final ConstantPoolGen cpg = cg.getConstantPool();
            final InstructionFactory factory = new InstructionFactory(cg);

            // for all methods, look for caller side "this.define0" calls
            for (int i = 0; i < methods.length; i++) {
                final MethodGen mg = new MethodGen(methods[i], className, cpg);
                final InstructionList il = mg.getInstructionList();
                if (il == null) continue;

                InstructionHandle ih = il.getStart();
                while (ih != null) {
                    final Instruction ins = ih.getInstruction();
                    if (ins instanceof INVOKESPECIAL
                        || ins instanceof INVOKESTATIC
                        || ins instanceof INVOKEVIRTUAL) {

                        final InvokeInstruction invokeInst = (InvokeInstruction) ins;
                        final String callerSideMethodClassName = invokeInst.getClassName(cpg);
                        final String callerSideMethodName = invokeInst.getMethodName(cpg);

                        //System.out.println(callerSideMethodClassName + "." + callerSideMethodName);
                        if ("java.lang.ClassLoader".equals(callerSideMethodClassName)
                            && "defineClass0".equals(callerSideMethodName)) {
                            InstructionHandle ihc = il.insert(ih, factory.createStore(Type.OBJECT, 16));
                            ihc = il.append(ihc, factory.createStore(Type.INT, 15));
                            ihc = il.append(ihc, factory.createStore(Type.INT, 14));
                            ihc = il.append(ihc, factory.createStore(Type.OBJECT, 13));
                            ihc = il.append(ihc, factory.createStore(Type.OBJECT, 12));
                            ihc = il.append(ihc, factory.createStore(Type.OBJECT, 11));
                            ihc = il.append(ihc, factory.createLoad(Type.OBJECT, 11));
                            ihc = il.append(ihc, factory.createLoad(Type.OBJECT, 12));
                            ihc = il.append(ihc, factory.createLoad(Type.OBJECT, 13));
                            ihc = il.append(ihc, factory.createLoad(Type.INT, 14));
                            ihc = il.append(ihc, factory.createLoad(Type.INT, 15));
                            ihc = il.append(ihc, factory.createLoad(Type.OBJECT, 16));
                            ihc = il.append(ihc, factory.createInvoke(
                                "org.codehaus.aspectwerkz.hook.impl.ClassPreProcessorHelper",
                                "defineClass0Pre",
                                new ArrayType(Type.BYTE, 1),
                                new Type[] {
                                    new ObjectType("java.lang.ClassLoader"),
                                    Type.STRING,
                                    new ArrayType(Type.BYTE, 1),
                                    Type.INT,
                                    Type.INT,
                                    new ObjectType("java.security.ProtectionDomain") },
                                Constants.INVOKESTATIC));
                            ihc = il.append(ihc, factory.createStore(Type.OBJECT, 18));
                            ihc = il.append(ihc, factory.createLoad(Type.OBJECT, 11));
                            ihc = il.append(ihc, factory.createLoad(Type.OBJECT, 12));
                            ihc = il.append(ihc, factory.createLoad(Type.OBJECT, 18));
                            ihc = il.append(ihc, new PUSH(cpg, 0));
                            ihc = il.append(ihc, factory.createLoad(Type.OBJECT, 18));
                            ihc = il.append(ihc, InstructionConstants.ARRAYLENGTH);
                            ihc = il.append(ihc, factory.createLoad(Type.OBJECT, 16));
                            // call to define0 occurs here
                            ihc = il.append(ihc.getNext(), factory.createStore(Type.OBJECT, 17));
                            ihc = il.append(ihc, factory.createLoad(Type.OBJECT, 17));
                        }
                    }
                    ih = ih.getNext();
                }
                mg.setInstructionList(il);
                mg.setMaxLocals();
                mg.setMaxStack();
                methods[i] = mg.getMethod();
            }
            cg.setMethods(methods);

            // dump generated BCEL stuff thru BCELifier
            //org.apache.bcel.util.BCELifier bc = new org.apache.bcel.util.BCELifier(cg.getJavaClass(), System.out);
            //bc.start();

            // dump class file
            //cg.getJavaClass().dump("ClassLoader.class");

            return cg.getJavaClass().getBytes();
        } catch (Exception e) {
            System.err.println("failed to patch ClassLoader:");
            e.printStackTrace();
            return b;
        }
    }

    /**
     * main test
     */
    public static void main(String args[]) throws Exception {
        ClassLoaderPreProcessor me = new ClassLoaderPreProcessorImpl();
        InputStream is = ClassLoader.getSystemClassLoader().getParent().getResourceAsStream("java/lang/ClassLoader.class");
        me.preProcess(ClassLoaderPatcher.inputStreamToByteArray(is));
        is.close();
    }

}
