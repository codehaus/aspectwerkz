/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.annotation.instrumentation;

import org.codehaus.aspectwerkz.annotation.instrumentation.bcel.BcelAttributeExtractor;
import org.codehaus.aspectwerkz.annotation.instrumentation.javassist.JavassistAttributeExtractor;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.WeakHashMap;

import javassist.CtClass;

/**
 * Retrieves attributes on class, method and field level
 * 
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public class Attributes {
    /**
     * Hold a cache of AttributeExtractors so we don't have to load the class loaded repeatedly when accessing custom
     * attributes.
     */
    private static final Map s_extractorCache = new WeakHashMap();

    /**
     * Return the list (possibly empty) of custom attributes associated with the class "klass".
     * 
     * @param klass The java.lang.Class object to find the attributes on.
     * @return The possibly 0-length array of attributes
     */
    public static Object[] getAttributes(final Class klass) {
        return getAttributeExtractor(klass).getClassAttributes();
    }

    /**
     * Return all the attributes associated with the given method.
     * 
     * @param method The java.lang.reflect.Method describing the method.
     * @return Attribute[] all attributes associated with the method. Returns a 0 length array in case no attributes
     *         were found.
     */
    public static Object[] getAttributes(final Method method) {
        Class klass = method.getDeclaringClass();
        ArrayList attribList = new ArrayList();

        // search for superclass
        while (true) {
            Object[] returnAttribs = searchForMethodAttributes(klass, method);
            if (returnAttribs.length > 0) {
                // already in the list and the attribute is allowed to be specified mulitple times.
                attribList.addAll(Arrays.asList(returnAttribs));
            }
            Class superClass = klass.getSuperclass();
            if (superClass == null) {
                break;
            } else if (superClass.getName().startsWith("java.")) {
                break;
            } else {
                klass = superClass;
            }
        }

        // search for interfaces.
        while (true) {
            Class[] interfaceClasses = klass.getInterfaces();
            for (int i = 0; i < interfaceClasses.length; i++) {
                Object[] intAttribs = searchForMethodAttributes(interfaceClasses[i], method);
                if (intAttribs.length > 0) {
                    attribList.addAll(Arrays.asList(intAttribs));
                }
            }
            Class superClass = klass.getSuperclass();
            if (superClass == null) {
                break;
            } else if (superClass.getName().startsWith("java.")) {
                break;
            } else {
                klass = superClass;
            }
        }
        return attribList.toArray(new Object[attribList.size()]);
    }

    /**
     * Return all the attributes associated with the given method.
     * 
     * @param method The java.lang.reflect.Method describing the method.
     * @return Attribute[] all attributes associated with the method. Returns a 0 length array in case no attributes
     *         were found.
     */
    public static Object[] getAttributes(final Constructor constructor) {
        Class klass = constructor.getDeclaringClass();
        ArrayList attribList = new ArrayList();

        // search for superclass
        while (true) {
            Object[] returnAttribs = searchForConstructorAttributes(klass, constructor);
            if (returnAttribs.length > 0) {
                // already in the list and the attribute is allowed to be specified mulitple times.
                attribList.addAll(Arrays.asList(returnAttribs));
            }
            Class superClass = klass.getSuperclass();
            if (superClass == null) {
                break;
            } else if (superClass.getName().startsWith("java.")) {
                break;
            } else {
                klass = superClass;
            }
        }

        // search for interfaces.
        while (true) {
            Class[] interfaceClasses = klass.getInterfaces();
            for (int i = 0; i < interfaceClasses.length; i++) {
                Object[] intAttribs = searchForConstructorAttributes(interfaceClasses[i], constructor);
                if (intAttribs.length > 0) {
                    attribList.addAll(Arrays.asList(intAttribs));
                }
            }
            Class superClass = klass.getSuperclass();
            if (superClass == null) {
                break;
            } else if (superClass.getName().startsWith("java.")) {
                break;
            } else {
                klass = superClass;
            }
        }
        return attribList.toArray(new Object[attribList.size()]);
    }

    /**
     * Return the list (possibly empty) of custom attributes associated with the field.
     * 
     * @param field The java.lang.reflect.Field object to find the attributes on.
     * @return The possibly 0-length array of attributes
     */
    public static Object[] getAttributes(final Field field) {
        return getAttributeExtractor(field.getDeclaringClass()).getFieldAttributes(field.getName());
    }

    /**
     * Searches for method attributes
     * 
     * @param klass
     * @param method
     * @return Attribute[]
     */
    private static Object[] searchForMethodAttributes(final Class klass, final Method method) {
        AttributeExtractor extractor = getAttributeExtractor(klass);
        if (extractor != null) {
            String[] paramTypes = new String[method.getParameterTypes().length];
            for (int i = 0; i < paramTypes.length; i++) {
                String paramType = method.getParameterTypes()[i].getName();

                // TODO: is this fix generic? are there other cases not handled?
                // handle array types
                if (paramType.startsWith("[L")) {
                    paramType = paramType.substring(2, paramType.length() - 1) + "[]";
                }
                paramTypes[i] = paramType;
            }
            return extractor.getMethodAttributes(method.getName(), paramTypes);
        } else {
            return new Object[0];
        }
    }

    /**
     * Searches for constructor attributes
     * 
     * @param klass
     * @param constructor
     * @return Attribute[]
     */
    private static Object[] searchForConstructorAttributes(final Class klass, final Constructor constructor) {
        AttributeExtractor extractor = getAttributeExtractor(klass);
        if (extractor != null) {
            String[] paramTypes = new String[constructor.getParameterTypes().length];
            for (int i = 0; i < paramTypes.length; i++) {
                String paramType = constructor.getParameterTypes()[i].getName();

                // TODO: is this fix generic? are there other cases not handled?
                // handle array types
                if (paramType.startsWith("[L")) {
                    paramType = paramType.substring(2, paramType.length() - 1) + "[]";
                }
                paramTypes[i] = paramType;
            }
            return extractor.getConstructorAttributes(paramTypes);
        } else {
            return new Object[0];
        }
    }

    /**
     * Return the list (possibly empty) of custom attributes associated with the class.
     * 
     * @param klass the Class object to find the attributes on.
     * @return the possibly 0-length array of attributes
     */
    public static synchronized AttributeExtractor getAttributeExtractor(final Class klass) {
        if (klass.isPrimitive() || klass.isArray() || klass.getName().startsWith("java.")) {
            return null;
        }
        BcelAttributeExtractor extractor;
        if ((extractor = (BcelAttributeExtractor) s_extractorCache.get(klass)) == null) {
            String className = klass.getName();
            try {
                ClassLoader loader = klass.getClassLoader();
                if (loader != null) {
                    extractor = new BcelAttributeExtractor();

                    // extractor = new AsmAttributeExtractor();
                    extractor.initialize(className, klass.getClassLoader());
                    s_extractorCache.put(klass, extractor);
                } else {
                    // bootstrap classloader
                    extractor = new BcelAttributeExtractor();

                    // extractor = new AsmAttributeExtractor();
                    extractor.initialize(className, ClassLoader.getSystemClassLoader());
                    s_extractorCache.put(klass, extractor);
                }
            } catch (Exception e) {
                throw new WrappedRuntimeException(e);
            }
        }
        return extractor;
    }

    /**
     * Return the list (possibly empty) of custom attributes associated with the class.
     * 
     * @param ctClass the class name
     * @param loader the class loader
     * @return the possibly 0-length array of attributes
     */
    public static synchronized AttributeExtractor getAttributeExtractor(final CtClass ctClass, final ClassLoader loader) {
        if (ctClass.isPrimitive() || ctClass.isArray() || ctClass.getName().startsWith("java.")) {
            return null;
        }
        JavassistAttributeExtractor extractor;
        Integer hash = new Integer((29 * ctClass.hashCode()) + loader.hashCode());
        if ((extractor = (JavassistAttributeExtractor) s_extractorCache.get(hash)) == null) {
            try {
                extractor = new JavassistAttributeExtractor();
                extractor.initialize(ctClass);
                s_extractorCache.put(hash, extractor);
            } catch (Exception e) {
                throw new WrappedRuntimeException(e);
            }
        }
        return extractor;
    }
}