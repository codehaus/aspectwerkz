/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.inlining.proxymethod;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.ClassVisitor;
import org.codehaus.aspectwerkz.joinpoint.management.JoinPointRegistry;
import org.codehaus.aspectwerkz.transform.Klass;
import org.codehaus.aspectwerkz.transform.inlining.ProxyMethodClassAdapter;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.impl.javassist.JavassistClassInfo;

import java.io.InputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Method;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class ProxyMethodAdapterTest extends ClassLoader {

    protected synchronized Class loadClass(final String name,
                                           final boolean resolve) throws ClassNotFoundException {
        if (name.startsWith("java.")) {
            System.err.println("Adapt: loading class '" + name + "' without on the fly adaptation");
            return super.loadClass(name, resolve);
        } else {
            System.err.println("Adapt: loading class '" + name + "' with on the fly adaptation");
        }

        String resource = name.replace('.', '/') + ".class";
        InputStream is = getResourceAsStream(resource);
        byte[] b;

        try {
            ClassReader cr = new ClassReader(is);
            ClassWriter cw = new ClassWriter(true);
            b = cw.toByteArray();

            ClassLoader loader = this;
//            ClassLoader loader = this.getParent();
            System.out.println("loader = " + loader);
            // faking the class info by using javassist
            ClassInfo classInfo = JavassistClassInfo.getClassInfo(new Klass(name, b, loader).getCtClass(), loader);


            cr.accept(new ProxyMethodClassAdapter(cw, loader, classInfo), false);
        } catch (Exception e) {
            throw new ClassNotFoundException(name, e);
        }

        try {
            String filename = resource;
            System.out.println("filename = " + filename);
            FileOutputStream fos = new FileOutputStream("target/classes/" + filename);
            fos.write(b);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return defineClass(name, b, 0, b.length);
    }

    public static void main(final String args[]) throws Exception {
        ClassLoader loader = new ProxyMethodAdapterTest();
        Class c = loader.loadClass(args[0]);
        Method m = c.getMethod("main", new Class[]{String[].class});
        String[] applicationArgs = new String[args.length - 1];
        System.arraycopy(args, 1, applicationArgs, 0, applicationArgs.length);
        m.invoke(null, new Object[]{applicationArgs});
    }

    public static class Test {
        public String toString() {
            return "called toString";
        }

        public void memberMethod() {
        }

        public static void staticMethod() {
        }

        public static void main(String[] args) {
            System.out.println("new Test().toString() = " + new Test().toString());
        }
    }
}
