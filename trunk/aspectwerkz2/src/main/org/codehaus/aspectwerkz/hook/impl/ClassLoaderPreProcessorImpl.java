/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.hook.impl;

import org.codehaus.aspectwerkz.hook.ClassLoaderPreProcessor;
import org.codehaus.aspectwerkz.hook.ClassLoaderPatcher;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

import java.io.InputStream;

/**
 * Instruments the java.lang.ClassLoader to plug in the Class PreProcessor
 * mechanism using Javassist.
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
            ClassPool pool = ClassPool.getDefault();
            CtClass klass = pool.get("java.lang.ClassLoader");

            // patch caller side of defineClass0
            // pre-call
            // byte[] besee = com.gnilux.besee.hook.impl.ClassPreProcessorHelper.defineClass0Pre(this, $$);
            // <call> c = defineClass0(name, besee, 0, besee.length, protectionDomain);
            ExprEditor defineClass0Pre = new ExprEditor() {
                    public void edit(MethodCall m) throws CannotCompileException {
                        if ("defineClass0".equals(m.getMethodName())) {
                            //TODO check for IBM: THIS $1.. $5
                            //TODO enhance this with a fake method preparation
                            m.replace(
                            "{"
                            +"  byte[] newBytes = org.codehaus.aspectwerkz.hook.impl.ClassPreProcessorHelper.defineClass0Pre($0, $$);"
                            +"  $_ = $proceed($1, newBytes, 0, newBytes.length, $5);"
                            +"}");
                        }
                    }
            };
            klass.instrument(defineClass0Pre);

            return pool.write("java.lang.ClassLoader");
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
