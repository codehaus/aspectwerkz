/**************************************************************************************
 * Copyright (c) Jonas Bon?r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.aopc;

import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.LoaderClassPath;
import javassist.Modifier;

/**
 * Test helper for AspectContainer, emulates a ClassLoader hierarchy sys/sub/ sys/sub/a sys/sub/b
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class ClassCreator {
    /**
    * ClassLoader.defineClass(name, bytes, from, to)
    */
    private static Method CLASSLOADER_DEFINECLASS_METHOD;

    static {
        try {
            Object b = Array.newInstance(byte.class, 1);
            CLASSLOADER_DEFINECLASS_METHOD = ClassLoader.class.getDeclaredMethod("defineClass",
                                                                                 new Class[] {
                                                                                     String.class, b.getClass(),
                                                                                     int.class, int.class
                                                                                 });
            CLASSLOADER_DEFINECLASS_METHOD.setAccessible(true);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static Object createInstance(String name, ClassLoader loader) {
        try {
            return createClass(name, loader).newInstance();
        } catch (Throwable t) {
            throw new WrappedRuntimeException(t);
        }
    }

    public static Class createClass(String name, ClassLoader loader) {
        try {
            ClassPool cp = new ClassPool(null);
            cp.appendClassPath(new LoaderClassPath(loader));
            CtClass prototype = cp.get(CallablePrototype.class.getName());
            prototype.setName(name);
            if (true) {
                return define(prototype.toBytecode(), name, loader);
            }
            CtClass klass = cp.makeClass(name);
            klass.setSuperclass(cp.get(BaseCallable.class.getName()));

            // public void methodAround();
            CtMethod m = new CtMethod(CtClass.voidType, "methodAround", new CtClass[] {  }, klass);
            m.setModifiers(m.getModifiers() | Modifier.PUBLIC);

            //m.setBody("{java.lang.System.out.println(\"... "+name+ " @ \" + this.getClass().getClassLoader());}");
            m.setBody("{ m_logString = \"methodAround \"; }");
            klass.addMethod(m);

            // public void methodPre();
            m = new CtMethod(CtClass.voidType, "methodPre", new CtClass[] {  }, klass);
            m.setModifiers(m.getModifiers() | Modifier.PUBLIC);

            //m.setBody("{java.lang.System.out.println(\"... "+name+ " @ \" + this.getClass().getClassLoader());}");
            m.setBody("{ m_logString = \"methodPre \"; }");
            klass.addMethod(m);

            // public void methodPost();
            m = new CtMethod(CtClass.voidType, "methodPost", new CtClass[] {  }, klass);
            m.setModifiers(m.getModifiers() | Modifier.PUBLIC);

            //m.setBody("{java.lang.System.out.println(\"... "+name+ " @ \" + this.getClass().getClassLoader());}");
            m.setBody("{ m_logString = \"methodPost \"; }");
            klass.addMethod(m);
            klass.addInterface(cp.get(Callable.class.getName()));
            return define(klass.toBytecode(), name, loader);
        } catch (Throwable throwable) {
            throw new WrappedRuntimeException(throwable);
        }
    }

    public static void main(String[] a) throws Throwable {
        ClassLoader myCL = new URLClassLoader(new URL[] { getPathFor(Callable.class.getResource("META-INF/aop.xml")) },
                                              ClassLoader.getSystemClassLoader());
        ClassLoader mySubCLA = new URLClassLoader(new URL[] { getPathFor(Callable.class.getResource("a/META-INF/aop.xml")) },
                                                  myCL);
        Callable ca = (Callable)(createClass("test.aopc.a.Callee", mySubCLA)).newInstance();
        ca.methodAround();
        ca.debug();
        ClassLoader mySubCLB = new URLClassLoader(new URL[] {  }, myCL);
        Callable cb = (Callable)(createClass("test.aopc.b.Callee", mySubCLB)).newInstance();
        cb.methodAround();
        cb.debug();
    }

    public static URL getPathFor(URL definition) {
        try {
            File f = new File(definition.getFile());
            if (!f.exists()) {
                System.err.println("<WARN> could not find " + f);
            }
            String path = new File(f.getParent()).getParent();
            File testExists = new File(path);
            if (!testExists.isDirectory()) {
                System.err.println("<WARN> could not find " + path);
            }
            return new File(path).toURL();
        } catch (MalformedURLException e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
    * Helper to define a Class within a specific ClassLoader
    *
    * @param b
    * @param name
    * @param loader
    * @return
    * @throws Throwable
    */
    public static Class define(byte[] b, String name, ClassLoader loader) throws Throwable {
        Object k = CLASSLOADER_DEFINECLASS_METHOD.invoke(loader,
                                                         new Object[] { name, b, new Integer(0), new Integer(b.length) });
        return (Class)k;
    }
}
