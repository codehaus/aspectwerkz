/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.annotation;

import org.codehaus.aspectwerkz.reflect.MethodInfo;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.ConstructorInfo;
import org.codehaus.aspectwerkz.reflect.FieldInfo;
import org.codehaus.aspectwerkz.reflect.impl.asm.AsmClassInfo;
import org.codehaus.aspectwerkz.reflect.impl.asm.AsmClassInfoRepository;
import org.codehaus.aspectwerkz.transform.ReflectHelper;
import org.codehaus.aspectwerkz.exception.DefinitionException;
import org.codehaus.aspectwerkz.annotation.instrumentation.asm.AsmAnnotations;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

/**
 * Utility class for annotation retrieval.
 * <br/>Note: Annotations are extracted out of ASMClassInfo
 * <br/>Note: caution when changing that to use reflection, since it might lead to load target class during aspect
 * system startup.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public final class Annotations {
    /**
     * Return the annotation with a specific name for a specific class.
     *
     * @param annotationName the annotation name
     * @param klass          the java.lang.Class object to find the annotation on.
     * @return the annotation or null
     */
    public static Annotation getAnnotation(final String annotationName, final Class klass) {
        ClassInfo classInfo = AsmClassInfo.getClassInfo(klass.getName(), klass.getClassLoader());
        return AsmAnnotations.getAnnotation(annotationName, classInfo);
    }

    /**
     * Return the annotation with a specific name for a specific method.
     *
     * @param annotationName the annotation name
     * @param method         the java.lang.refect.Method object to find the annotation on.
     * @return the annotation or null
     */
    public static Annotation getAnnotation(final String annotationName, final Method method) {
        ClassLoader loader = method.getDeclaringClass().getClassLoader();
        ClassInfo classInfo = AsmClassInfo.getClassInfo(method.getDeclaringClass().getName(), loader);
        MethodInfo methodInfo = classInfo.getMethod(ReflectHelper.calculateHash(method));
        return AsmAnnotations.getAnnotation(annotationName, methodInfo);
    }

    /**
     * Return the annotation with a specific name for a specific constructor.
     *
     * @param annotationName the annotation name
     * @param constructor    the java.lang.refect.Constructor object to find the annotation on.
     * @return the annotation or null
     */
    public static Annotation getAnnotation(final String annotationName, final Constructor constructor) {
        ClassLoader loader = constructor.getDeclaringClass().getClassLoader();
        ClassInfo classInfo = AsmClassInfo.getClassInfo(constructor.getDeclaringClass().getName(), loader);
        ConstructorInfo constructorInfo = classInfo.getConstructor(ReflectHelper.calculateHash(constructor));
        return AsmAnnotations.getAnnotation(annotationName, constructorInfo);
    }

    /**
     * Return the annotation with a specific name for a specific field.
     *
     * @param annotationName the annotation name
     * @param field          the java.lang.reflect.Field object to find the annotation on.
     * @return the annotation or null
     */
    public static Annotation getAnnotation(final String annotationName, final Field field) {
        ClassLoader loader = field.getDeclaringClass().getClassLoader();
        ClassInfo classInfo = AsmClassInfo.getClassInfo(field.getDeclaringClass().getName(), loader);
        FieldInfo fieldInfo = classInfo.getField(ReflectHelper.calculateHash(field));
        return AsmAnnotations.getAnnotation(annotationName, fieldInfo);
    }

    /**
     * Return a list with the annotations with a specific name for a specific class.
     *
     * @param annotationName the annotation name
     * @param klass          the java.lang.Class object to find the annotation on.
     * @return the annotations in a list (can be empty)
     */
    public static List getAnnotations(final String annotationName, final Class klass) {
        ClassInfo classInfo = AsmClassInfo.getClassInfo(klass.getName(), klass.getClassLoader());
        return AsmAnnotations.getAnnotations(annotationName, classInfo);
    }

    /**
     * Return a list with the annotations with a specific name for a specific method.
     *
     * @param annotationName the annotation name
     * @param method         the java.lang.refect.Method object to find the annotation on.
     * @return the annotations in a list (can be empty)
     */
    public static List getAnnotations(final String annotationName, final Method method) {
        ClassLoader loader = method.getDeclaringClass().getClassLoader();
        ClassInfo classInfo = AsmClassInfo.getClassInfo(method.getDeclaringClass().getName(), loader);
        MethodInfo methodInfo = classInfo.getMethod(ReflectHelper.calculateHash(method));
        return AsmAnnotations.getAnnotations(annotationName, methodInfo);
    }

    /**
     * Return a list with the annotations with a specific name for a specific constructor.
     *
     * @param annotationName the annotation name
     * @param constructor    the java.lang.refect.Constructor object to find the annotation on.
     * @return the annotations in a list (can be empty)
     */
    public static List getAnnotations(final String annotationName, final Constructor constructor) {
        ClassLoader loader = constructor.getDeclaringClass().getClassLoader();
        ClassInfo classInfo = AsmClassInfo.getClassInfo(constructor.getDeclaringClass().getName(), loader);
        ConstructorInfo constructorInfo = classInfo.getConstructor(ReflectHelper.calculateHash(constructor));
        return AsmAnnotations.getAnnotations(annotationName, constructorInfo);
    }

    /**
     * Return a list with the annotations with a specific name for a specific field.
     *
     * @param annotationName the annotation name
     * @param field          the java.lang.reflect.Field object to find the annotation on.
     * @return the annotations in a list (can be empty)
     */
    public static List getAnnotations(final String annotationName, final Field field) {
        ClassLoader loader = field.getDeclaringClass().getClassLoader();
        ClassInfo classInfo = AsmClassInfo.getClassInfo(field.getDeclaringClass().getName(), loader);
        FieldInfo fieldInfo = classInfo.getField(ReflectHelper.calculateHash(field));
        return AsmAnnotations.getAnnotations(annotationName, fieldInfo);
    }

    /**
     * Return a list with the annotations for a specific class. <p/>Each annotation is wrapped in {@link
     * org.codehaus.aspectwerkz.annotation.AnnotationInfo}instance.
     *
     * @param klass the java.lang.Class object to find the annotation on.
     * @return a list with the annotations
     */
    public static List getAnnotationInfos(final Class klass) {
        return AsmClassInfo.getClassInfo(klass.getName(), klass.getClassLoader()).getAnnotations();
    }

    /**
     * Return the annotations for a specific method. <p/>Each annotation is wrapped in {@link
     * org.codehaus.aspectwerkz.annotation.AnnotationInfo}instance.
     *
     * @param method the java.lang.refect.Method object to find the annotation on.
     * @return a list with the annotations
     */
    public static List getAnnotationInfos(final Method method) {
        ClassLoader loader = method.getDeclaringClass().getClassLoader();
        ClassInfo classInfo = AsmClassInfo.getClassInfo(method.getDeclaringClass().getName(), loader);
        return classInfo.getMethod(ReflectHelper.calculateHash(method)).getAnnotations();
    }

    /**
     * Return the annotations for a specific constructor. <p/>Each annotation is wrapped in {@link
     * org.codehaus.aspectwerkz.annotation.AnnotationInfo}instance.
     *
     * @param constructor the java.lang.reflect.Constructor object to find the annotation on.
     * @return a list with the annotations
     */
    public static List getAnnotationInfos(final Constructor constructor) {
        ClassLoader loader = constructor.getDeclaringClass().getClassLoader();
        ClassInfo classInfo = AsmClassInfo.getClassInfo(constructor.getDeclaringClass().getName(), loader);
        return classInfo.getConstructor(ReflectHelper.calculateHash(constructor)).getAnnotations();
    }

    /**
     * Return the annotations for a specific field. <p/>Each annotation is wrapped in {@link
     * org.codehaus.aspectwerkz.annotation.AnnotationInfo}instance.
     *
     * @param field the java.lang.reflect.Field object to find the annotation on.
     * @return a list with the annotations
     */
    public static List getAnnotationInfos(final Field field) {
        ClassLoader loader = field.getDeclaringClass().getClassLoader();
        ClassInfo classInfo = AsmClassInfo.getClassInfo(field.getDeclaringClass().getName(), loader);
        return classInfo.getField(ReflectHelper.calculateHash(field)).getAnnotations();
    }

    /**
     * Returns the annotation proxy class for a specific annotation loaded in a specific loader.
     *
     * @param annotationName
     * @param loader
     * @return
     */
    public static Class getProxyClass(final String annotationName, final ClassLoader loader) {
        Class proxyClass;
        AsmClassInfoRepository classInfoRepository = AsmClassInfoRepository.getRepository(loader);
        String proxyClassName = (String) classInfoRepository.getAnnotationProperties().get(annotationName);
        if (proxyClassName == null) {
            return null;
        }
        if (proxyClassName.equals("")) {
            throw new DefinitionException("untyped annotations can not be used with Java5 annotations");
        } else {
            try {
                proxyClass = loader.loadClass(proxyClassName);
            } catch (ClassNotFoundException e) {
                String message = proxyClassName
                                 +
                                 " could not be found on system classpath or class path provided as argument to the compiler";
                throw new DefinitionException(message);
            }
        }
        return proxyClass;
    }
}