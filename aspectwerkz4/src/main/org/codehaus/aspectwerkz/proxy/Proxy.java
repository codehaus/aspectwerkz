/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.proxy;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.WeakHashMap;
import java.util.Map;

import org.codehaus.aspectwerkz.hook.impl.ClassPreProcessorHelper;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.util.ContextClassLoader;

/**
 * Creates proxy classes from target classes and weaves in all matching aspects deployed in the class loader
 * and defined by the <code>META-INF/aop.xml</code> file.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public class Proxy {
    public static final String CLASS_LOADER_REFLECT_CLASS_NAME = "java.lang.ClassLoader";
    public static final String DEFINE_CLASS_METHOD_NAME = "defineClass";
    public static final String PROXY_SUFFIX_START = "$$ProxiedByAW$$";
//    private static final RuntimePermission DEFINE_AW_PROXY_CLASS_IN_JAVA_PACKAGE_PERMISSION =
//            new RuntimePermission("defineAWProxyClassInJavaPackage");

    private static final Map PROXY_CLASS_CACHE = new WeakHashMap();

    /**
     * Creates a new proxy instance based for the class specified and instantiates it using its default no-argument
     * constructor.
     *
     * @param clazz    the target class to make a proxy for
     * @param useCache true if a cached instance of the proxy classed should be used
     * @return the proxy instance
     */
    public static Object newInstance(final Class clazz, final boolean useCache) {
        try {
            Class proxyClass = getProxyClassFor(clazz, useCache);
            return proxyClass.newInstance();
        } catch (Throwable e) {
            e.printStackTrace();
            throw new Error(e.toString());
        }
    }

    /**
     * Creates a new proxy instance for the class specified and instantiates it using the constructor matching
     * the argument type array specified.
     *
     * @param clazz          the target class to make a proxy for
     * @param argumentTypes  the argument types matching the signature of the constructor to use when instantiating the proxy
     * @param argumentValues the argument values to use when instantiating the proxy
     * @param useCache       true if a cached instance of the proxy classed should be used
     * @return the proxy instance
     */
    public static Object newInstance(final Class clazz,
                                     final Class[] argumentTypes,
                                     final Object[] argumentValues,
                                     final boolean useCache) {
        try {
            Class proxyClass = getProxyClassFor(clazz, useCache);
            return proxyClass.getDeclaredConstructor(argumentTypes).newInstance(argumentValues);
        } catch (Throwable e) {
            e.printStackTrace();
            throw new Error(e.toString());
        }
    }

    /**
     * Compiles and returns a proxy class for the class specified.
     *
     * @param clazz    the target class to make a proxy for
     * @param useCache true if a cached instance of the proxy classed should be used
     * @return the proxy class
     */
    public static Class getProxyClassFor(final Class clazz, final boolean useCache) {
        if (!useCache) {
            return getProxyClassFor(clazz);
        } else {
            synchronized (PROXY_CLASS_CACHE) {
                Object cachedProxyClass = PROXY_CLASS_CACHE.get(clazz);
                if (cachedProxyClass != null) {
                    return (Class) cachedProxyClass;
                }
                Class proxyClass = getProxyClassFor(clazz);
                PROXY_CLASS_CACHE.put(clazz, proxyClass);
                return proxyClass;
            }
        }
    }

    /**
     * Compiles and returns a proxy class for the class specified.
     * No cache is used, but compiles a new one each invocation.
     *
     * @param clazz
     * @return the proxy class
     */
    private static Class getProxyClassFor(final Class clazz) {
        ClassLoader loader = clazz.getClassLoader();
        String proxyClassName = getUniqueClassNameForProxy(clazz);
        byte[] bytes = ProxyCompiler.compileProxyFor(clazz, proxyClassName);
        byte[] transformedBytes = ClassPreProcessorHelper.defineClass0Pre(
                loader, proxyClassName, bytes, 0, bytes.length, null
        );
        Class proxyClass = defineClass(loader, transformedBytes, proxyClassName);
        return proxyClass;
    }

    /**
     * Returns a unique name for the proxy class.
     *
     * @param clazz target class
     * @return the proxy class name
     */
    private static String getUniqueClassNameForProxy(final Class clazz) {
        return clazz.getName().replace('.', '/') + PROXY_SUFFIX_START + new Long(Uuid.newUuid()).toString();
    }

    /**
     * Adds a class to a class loader and loads it.
     *
     * @param loader the class loader
     * @param bytes  the bytes for the class
     * @param name   the name of the class
     * @return the class
     */
    private static Class defineClass(ClassLoader loader, final byte[] bytes, final String name) {
        String className = name.replace('/', '.');
        try {
            if (loader == null) {
                loader = ContextClassLoader.getLoader();
            }

//            SecurityManager sm = System.getSecurityManager();
//            if (className != null && className.startsWith("java.") && sm != null) {
//                sm.checkPermission(DEFINE_AW_PROXY_CLASS_IN_JAVA_PACKAGE_PERMISSION);
//            }

            Class klass = loader.loadClass(CLASS_LOADER_REFLECT_CLASS_NAME);
            Method method = klass.getDeclaredMethod(
                    DEFINE_CLASS_METHOD_NAME, new Class[]{
                        String.class, byte[].class, int.class, int.class
                    }
            );

            method.setAccessible(true);
            Object[] args = new Object[]{
                className, bytes, new Integer(0), new Integer(bytes.length)
            };
            Class clazz = (Class) method.invoke(loader, args);

            method.setAccessible(false);
            return clazz;

        } catch (InvocationTargetException e) {
            if (e.getTargetException() instanceof LinkageError) {
                Class failoverProxyClass = loadClass(loader, className);
                if (failoverProxyClass != null) {
                    return failoverProxyClass;
                }
            }
            throw new WrappedRuntimeException(e.getTargetException());
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
    private static Class loadClass(ClassLoader loader, final String name) {
        String className = name.replace('/', '.');
        try {
            if (loader == null) {
                loader = ContextClassLoader.getLoader();
            }
            // Use Class.forName since loader.loadClass fails on JBoss UCL
            return Class.forName(className, false, loader);
        } catch (Exception e) {
            return null;
        }
    }
}
