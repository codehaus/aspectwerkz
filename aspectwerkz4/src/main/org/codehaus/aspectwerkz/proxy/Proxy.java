/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.proxy;

import java.util.WeakHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;

import org.codehaus.aspectwerkz.hook.impl.ClassPreProcessorHelper;
import org.codehaus.aspectwerkz.transform.inlining.AsmHelper;
import org.codehaus.aspectwerkz.definition.DefinitionParserHelper;
import org.codehaus.aspectwerkz.definition.SystemDefinition;
import org.codehaus.aspectwerkz.definition.SystemDefinitionContainer;
import org.codehaus.aspectwerkz.intercept.AdvisableImpl;
import org.codehaus.aspectwerkz.DeploymentModel;

/**
 * Compiles proxy classes from target classes and weaves in all matching aspects deployed in the class loader
 * and defined by the <code>META-INF/aop.xml</code> file.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public class Proxy {

    /**
     * The suffix for the compiled proxy classes.
     */
    public static final String PROXY_SUFFIX_START = "$$ProxiedByAW$$";

    /**
     * Cache for the compiled proxy classes. Target class is key.
     */
    private static final Map PROXY_CLASS_CACHE = new WeakHashMap();

    /**
     * Creates a new proxy instance based for the class specified and instantiates it using its default no-argument
     * constructor.
     * <p/>
     * The proxy will be cached and non-advisable.
     *
     * @param clazz the target class to make a proxy for
     * @return the proxy instance
     */
    public static Object newInstance(final Class clazz) {
        try {
            Class proxyClass = getProxyClassFor(clazz, true, false);
            return proxyClass.newInstance();
        } catch (Throwable e) {
            e.printStackTrace();
            throw new Error(e.toString());
        }
    }

    /**
     * Creates a new proxy instance for the class specified and instantiates it using the constructor matching
     * the argument type array specified.
     * <p/>
     * The proxy will be cached and non-advisable.
     *
     * @param clazz          the target class to make a proxy for
     * @param argumentTypes  the argument types matching the signature of the constructor to use when instantiating the proxy
     * @param argumentValues the argument values to use when instantiating the proxy
     * @return the proxy instance
     */
    public static Object newInstance(final Class clazz, final Class[] argumentTypes, final Object[] argumentValues) {
        try {
            Class proxyClass = getProxyClassFor(clazz, true, false);
            return proxyClass.getDeclaredConstructor(argumentTypes).newInstance(argumentValues);
        } catch (Throwable e) {
            e.printStackTrace();
            throw new Error(e.toString());
        }
    }

    /**
     * Creates a new proxy instance based for the class specified and instantiates it using its default no-argument
     * constructor.
     *
     * @param clazz         the target class to make a proxy for
     * @param useCache      true if a cached instance of the proxy classed should be used
     * @param makeAdvisable true if the proxy class should implement the <code>Advisable</code> interface,
     *                      e.g. be prepared for programmatic, runtime, per instance hot deployement of advice
     * @return the proxy instance
     */
    public static Object newInstance(final Class clazz, final boolean useCache, final boolean makeAdvisable) {
        try {
            Class proxyClass = getProxyClassFor(clazz, useCache, makeAdvisable);
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
     * @param makeAdvisable  true if the proxy class should implement the <code>Advisable</code> interface,
     *                       e.g. be prepared for programmatic, runtime, per instance hot deployement of advice
     * @return the proxy instance
     */
    public static Object newInstance(final Class clazz,
                                     final Class[] argumentTypes,
                                     final Object[] argumentValues,
                                     final boolean useCache,
                                     final boolean makeAdvisable) {
        try {
            Class proxyClass = getProxyClassFor(clazz, useCache, makeAdvisable);
            return proxyClass.getDeclaredConstructor(argumentTypes).newInstance(argumentValues);
        } catch (Throwable e) {
            e.printStackTrace();
            throw new Error(e.toString());
        }
    }

    /**
     * Compiles and returns a proxy class for the class specified.
     *
     * @param clazz         the target class to make a proxy for
     * @param useCache      true if a cached instance of the proxy classed should be used
     * @param makeAdvisable true if the proxy class should implement the <code>Advisable</code> interface,
     *                      e.g. be prepared for programmatic, runtime, per instance hot deployement of advice
     * @return the proxy class
     */
    public static Class getProxyClassFor(final Class clazz, final boolean useCache, final boolean makeAdvisable) {

        // FIXME - add support for proxying java.* classes
        if (clazz.getName().startsWith("java.")) {
            throw new RuntimeException("can not create proxies from system classes (java.*)");
        }
        if (!useCache) {
            return getNewProxyClassFor(clazz, makeAdvisable);
        } else {
            synchronized (PROXY_CLASS_CACHE) {
                Object cachedProxyClass = PROXY_CLASS_CACHE.get(clazz);
                if (cachedProxyClass != null) {
                    return (Class) cachedProxyClass;
                }
                Class proxyClass = getNewProxyClassFor(clazz, makeAdvisable);
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
     * @param makeAdvisable true if the proxy class should implement the <code>Advisable</code> interface,
     *                      e.g. be prepared for programmatic, runtime, per instance hot deployement of advice
     * @return the proxy class
     */
    private static Class getNewProxyClassFor(final Class clazz, final boolean makeAdvisable) {
        ClassLoader loader = clazz.getClassLoader();
        String proxyClassName = getUniqueClassNameForProxy(clazz);

        if (makeAdvisable) {
            makeProxyAdvisable(clazz);
        }

        byte[] bytes = ProxyCompiler.compileProxyFor(clazz, proxyClassName);
        byte[] transformedBytes = ClassPreProcessorHelper.defineClass0Pre(
                loader, proxyClassName, bytes, 0, bytes.length, null
        );

        return AsmHelper.loadClass(loader, transformedBytes, proxyClassName);
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
     * Enhances the proxy class with the Advisable mixin, to allow runtime per instance additions of
     * interceptors.
     *
     * @param clazz
     */
    private static void makeProxyAdvisable(final Class clazz) {
        // changes occurs in the virtual definition only
        SystemDefinition definition = SystemDefinitionContainer.getVirtualDefinitionAt(clazz.getClassLoader());
        addAdvisableDefToSystemDef(clazz, definition);
    }

    private static void addAdvisableDefToSystemDef(final Class clazz, final SystemDefinition definition) {
        String withinPointcut = "within(" + clazz.getName().replace('/', '.') + ')';
        definition.addMixinDefinition(
                DefinitionParserHelper.createAndAddMixinDefToSystemDef(
                        AdvisableImpl.CLASS_INFO,
                        withinPointcut,
                        DeploymentModel.PER_INSTANCE,
                        false,
                        definition
                )
        );
        DefinitionParserHelper.createAndAddAdvisableDef(
                "(execution(!static * *.*(..)) && " + withinPointcut + ')',
                definition
        );
    }
}
