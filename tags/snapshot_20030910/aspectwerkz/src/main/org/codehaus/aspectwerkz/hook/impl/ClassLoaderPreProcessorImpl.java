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
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;

/**
 * Instruments the java.lang.ClassLoader to plug in the Class PreProcessor
 * mechanism using BCEL.
 *
 * We are using a lazy initialization of the class preprocessor to allow all class
 * pre processor logic to be in system classpath and not in bootclasspath.
 *
 * This implementation should support IBM custom JRE
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
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
                    //System.out.println(ins.getName() + ins.getOpcode());
                    if (ins instanceof INVOKESPECIAL
                        || ins instanceof INVOKESTATIC
                        || ins instanceof INVOKEVIRTUAL) {

                        final InvokeInstruction invokeInst = (InvokeInstruction) ins;
                        final String callerSideMethodClassName = invokeInst.getClassName(cpg);
                        final String callerSideMethodName = invokeInst.getMethodName(cpg);

                        //System.out.println(callerSideMethodClassName + "." + callerSideMethodName);
                        if ("java.lang.ClassLoader".equals(callerSideMethodClassName)
                            && "defineClass0".equals(callerSideMethodName)) {

                            //assert compliant JRE
                            Type args[] = invokeInst.getArgumentTypes(cpg);
                            assertSupported(args);

                            // store former method args in local vars
                            InstructionHandle ihc = null;
                            if (args.length > 5) {
                                // IBM like JRE with extra args
                                ihc = il.append(ih.getPrev(), factory.createStore(args[args.length-1], 2100+args.length-1));
                                for (int index = args.length-2; index >= 5; index--) {
                                    ihc = il.append(ihc, factory.createStore(args[index], 2100+index));
                                }
                                ihc = il.append(ihc, factory.createStore(Type.OBJECT, 2016));//protection domain
                            } else {
                                // SUN regular JRE
                                ihc = il.append(ih.getPrev(), factory.createStore(Type.OBJECT, 2016));//protection domain
                            }

                            ihc = il.append(ihc, factory.createStore(Type.INT, 2015));//length
                            ihc = il.append(ihc, factory.createStore(Type.INT, 2014));//index
                            ihc = il.append(ihc, factory.createStore(Type.OBJECT, 2013));//bytes
                            ihc = il.append(ihc, factory.createStore(Type.OBJECT, 2012));//name

                            // prepare method call stack
                            ihc = il.append(ihc, factory.createLoad(Type.OBJECT, 0));//this
                            ihc = il.append(ihc, factory.createLoad(Type.OBJECT, 2012));
                            ihc = il.append(ihc, factory.createLoad(Type.OBJECT, 2013));
                            ihc = il.append(ihc, factory.createLoad(Type.INT, 2014));
                            ihc = il.append(ihc, factory.createLoad(Type.INT, 2015));
                            ihc = il.append(ihc, factory.createLoad(Type.OBJECT, 2016));

                            // call weaver helper
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
                            ihc = il.append(ihc, factory.createStore(Type.OBJECT, 3018));//result bytes

                            // rebuild former method call stack
                            ihc = il.append(ihc, factory.createLoad(Type.OBJECT, 2012));//name
                            ihc = il.append(ihc, factory.createLoad(Type.OBJECT, 3018));//bytes
                            ihc = il.append(ihc, new PUSH(cpg, 0));
                            ihc = il.append(ihc, factory.createLoad(Type.OBJECT, 3018));//bytes
                            ihc = il.append(ihc, InstructionConstants.ARRAYLENGTH);//.length
                            ihc = il.append(ihc, factory.createLoad(Type.OBJECT, 2016));//protection domain

                            // extra args for IBM like JRE
                            if (args.length > 5) {
                                for (int index = 5; index < args.length; index++) {
                                    ihc = il.append(ihc, factory.createLoad(args[index], 2100+index));
                                }
                            }

                            // call to define0 occurs here ...

                            // substitute result
                            ihc = il.append(ihc.getNext(), factory.createStore(Type.OBJECT, 3020));//result Class
                            ihc = il.append(ihc, factory.createLoad(Type.OBJECT, 3020));
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


















    public byte[] preProcessCOPY(byte[] b) {
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
                            //assert compliant JRE
                            Type args[] = invokeInst.getArgumentTypes(cpg);
                            assertSupported(args);

                            System.out.println(invokeInst.consumeStack(cpg));
                            System.out.println(invokeInst.produceStack(cpg));
                            System.out.println(invokeInst.getIndex());


                            InstructionHandle ihc = null;
                            /*if (args.length > 5) {
                                // IBM like JRE with extra args
                                ihc = il.insert(ih, factory.createStore(args[args.length-1], 160+args.length-1));
                                System.out.println(160+args.length-1);
                                for (int index = args.length-2; index >= 5; index--) {
                                    ihc = il.append(ihc, factory.createStore(args[index], 160+index));
                                    System.out.println(160+index);
                                }
                                ihc = il.append(ihc, factory.createStore(Type.OBJECT, 16));//bytes
                            } else {
                                // SUN regular JRE
                                ihc = il.insert(ih, factory.createStore(Type.OBJECT, 16));//bytes
                            }*/

                            //ihc = il.insert(ih, factory.createStore(Type.OBJECT, 19));//bytes
                            //ihc = il.append(ihc, factory.createStore(Type.OBJECT, 18));//bytes
                            //ihc = il.append(ihc, factory.createStore(Type.OBJECT, 16));//bytes


                            ihc = il.insert(ih, factory.createStore(Type.OBJECT, 16));//bytes

                            ihc = il.append(ihc, factory.createStore(Type.INT, 15));//length
                            ihc = il.append(ihc, factory.createStore(Type.INT, 14));//index
                            ihc = il.append(ihc, factory.createStore(Type.OBJECT, 13));//bytes
                            ihc = il.append(ihc, factory.createStore(Type.OBJECT, 12));//name
                            ihc = il.append(ihc, factory.createStore(Type.OBJECT, 11));//this
                            ihc = il.append(ihc, factory.createLoad(Type.OBJECT, 0));
                            //ihc = il.append(ihc, InstructionConstants.THIS);

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
                            ihc = il.append(ihc, factory.createStore(Type.OBJECT, 18+2/*18*/));//result bytes


                            ihc = il.append(ihc, factory.createLoad(Type.OBJECT, 11));//this
                            ihc = il.append(ihc, factory.createLoad(Type.OBJECT, 12));//name
                            ihc = il.append(ihc, factory.createLoad(Type.OBJECT, 18+2/*18*/));//bytes
                            ihc = il.append(ihc, new PUSH(cpg, 0));
                            ihc = il.append(ihc, factory.createLoad(Type.OBJECT, 18+2/*18*/));//bytes
                            ihc = il.append(ihc, InstructionConstants.ARRAYLENGTH);//.length
                            ihc = il.append(ihc, factory.createLoad(Type.OBJECT, 16));//protection domain

                            //ihc = il.append(ihc, factory.createLoad(Type.OBJECT, 18));//bytes
                            //ihc = il.append(ihc, factory.createLoad(Type.OBJECT, 19));//bytes

                            /*
                            // extra args for IBM like JRE
                            if (args.length > 5) {
                                for (int index = 5; index < args.length; index++) {
                                    ihc = il.append(ihc, factory.createLoad(Type.OBJECT, 160+index));
                                    System.out.println(" " + 160+index);
                                }
                            }
                            */

                            // call to define0 occurs here ...

                            ihc = il.append(ihc.getNext(), factory.createStore(Type.OBJECT, 19+2/*19*/));//result Class
                            ihc = il.append(ihc, factory.createLoad(Type.OBJECT, 19+2/*19*/));
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
     * Check the signature of defineClass0
     * @param args
     */
    private static void assertSupported(Type[] args) {
        if (args.length >= 5 &&
            (
            args[0].getSignature().equals("Ljava/lang/String;")
            && args[1].getSignature().equals("[B")
            && args[2].getSignature().equals("I")
            && args[3].getSignature().equals("I")
            && args[4].getSignature().equals("Ljava/security/ProtectionDomain;")
            ))
            ;
        else {
            StringBuffer sign = new StringBuffer("(");
            for (int i = 0; i < args.length; i++) {
                sign.append(args[i].toString());
                if (i < args.length -1)
                    sign.append(", ");
            }
            sign.append(")");
            throw new Error("non standard JDK, native call not supported "+sign.toString());
        }
    }

    /**
     * main test
     */
    public static void main(String args[]) throws Exception {
        ClassLoaderPreProcessor me = new ClassLoaderPreProcessorImpl();
        InputStream is = ClassLoader.getSystemClassLoader().getParent().getResourceAsStream("java/lang/ClassLoader.class");
        byte[] out = me.preProcess(ClassLoaderPatcher.inputStreamToByteArray(is));
        is.close();
        OutputStream os = new FileOutputStream("_boot/java/lang/ClassLoader.class");
        os.write(out);
        os.close();
    }

}
