/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.annotation;

import org.codehaus.aspectwerkz.annotation.instrumentation.Attributes;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for annotation retrieval.
 *
 * @TODO: support for constructor annotations
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public final class Annotations {
    /**
     * Return the annotation with a specific name for a specific class.
     *
     * @param annotationName the annotation name
     * @param klass          the java.lang.Class object to find the annotation on.
     * @return the annotation or null
     */
    public static AnnotationProxyBase getAnnotation(final String annotationName, final Class klass) {
        Object[] attributes = Attributes.getAttributes(klass);
        for (int i = 0; i < attributes.length; i++) {
            Object attribute = attributes[i];
            if (attribute instanceof AnnotationInfo) {
                AnnotationInfo annotationInfo = (AnnotationInfo)attribute;
                if (annotationInfo.getName().equals(annotationName)) {
                    return annotationInfo.getAnnotation();
                }
            }
        }
        return null;
    }

    /**
     * Return the annotation with a specific name for a specific method.
     *
     * @param annotationName the annotation name
     * @param method         the java.lang.refect.Method object to find the annotation on.
     * @return the annotation or null
     */
    public static AnnotationProxyBase getAnnotation(final String annotationName, final Method method) {
        Object[] attributes = Attributes.getAttributes(method);
        for (int i = 0; i < attributes.length; i++) {
            Object attribute = attributes[i];
            if (attribute instanceof AnnotationInfo) {
                AnnotationInfo annotationInfo = (AnnotationInfo)attribute;
                if (annotationInfo.getName().equals(annotationName)) {
                    return annotationInfo.getAnnotation();
                }
            }
        }
        return null;
    }

    /**
     * Return the annotation with a specific name for a specific field.
     *
     * @param annotationName the annotation name
     * @param field          the java.lang.reflect.Field object to find the annotation on.
     * @return the annotation or null
     */
    public static AnnotationProxyBase getAnnotation(final String annotationName, final Field field) {
        Object[] attributes = Attributes.getAttributes(field);
        for (int i = 0; i < attributes.length; i++) {
            Object attribute = attributes[i];
            if (attribute instanceof AnnotationInfo) {
                AnnotationInfo annotationInfo = (AnnotationInfo)attribute;
                if (annotationInfo.getName().equals(annotationName)) {
                    return annotationInfo.getAnnotation();
                }
            }
        }
        return null;
    }

    /**
     * Return a list with the annotations with a specific name for a specific class.
      *
     * @param annotationName the annotation name
     * @param klass          the java.lang.Class object to find the annotation on.
     * @return the annotations
     */
    public static List getAnnotations(final String annotationName, final Class klass) {
        List annotations = new ArrayList();
        Object[] attributes = Attributes.getAttributes(klass);
        for (int i = 0; i < attributes.length; i++) {
            Object attribute = attributes[i];
            if (attribute instanceof AnnotationInfo) {
                AnnotationInfo annotationInfo = (AnnotationInfo)attribute;
                if (annotationInfo.getName().equals(annotationName)) {
                    annotations.add(annotationInfo.getAnnotation());
                }
            }
        }
        return annotations;
    }

    /**
     * Return a list with the annotations with a specific name for a specific method.
     *
     * @param annotationName the annotation name
     * @param method         the java.lang.refect.Method object to find the annotation on.
     * @return the annotations
     */
    public static List getAnnotations(final String annotationName, final Method method) {
        List annotations = new ArrayList();
        Object[] attributes = Attributes.getAttributes(method);
        for (int i = 0; i < attributes.length; i++) {
            Object attribute = attributes[i];
            if (attribute instanceof AnnotationInfo) {
                AnnotationInfo annotationInfo = (AnnotationInfo)attribute;
                if (annotationInfo.getName().equals(annotationName)) {
                    annotations.add(annotationInfo.getAnnotation());
                }
            }
        }
        return annotations;
    }

    /**
     * Return a list with the annotations with a specific name for a specific field.
     *
     * @param annotationName the annotation name
     * @param field          the java.lang.reflect.Field object to find the annotation on.
     * @return the annotations
     */
    public static List getAnnotations(final String annotationName, final Field field) {
        List annotations = new ArrayList();
        Object[] attributes = Attributes.getAttributes(field);
        for (int i = 0; i < attributes.length; i++) {
            Object attribute = attributes[i];
            if (attribute instanceof AnnotationInfo) {
                AnnotationInfo annotationInfo = (AnnotationInfo)attribute;
                if (annotationInfo.getName().equals(annotationName)) {
                    annotations.add(annotationInfo.getAnnotation());
                }
            }
        }
        return annotations;
    }

    /**
     * Return a list with the annotations for a specific class.
     * <p/>
     * Each annotation is wrapped in {@link org.codehaus.aspectwerkz.annotation.AnnotationInfo} instance.
     *
     * @param klass the java.lang.Class object to find the annotation on.
     * @return a list with annotation
     */
    public static List getAnnotationInfos(final Class klass) {
        List annotations = new ArrayList();
        Object[] attributes = Attributes.getAttributes(klass);
        for (int i = 0; i < attributes.length; i++) {
            Object attribute = attributes[i];
            if (attribute instanceof AnnotationInfo) {
                annotations.add(attribute);
            }
        }
        return annotations;
    }

    /**
     * Return the annotations for a specific method.
     * <p/>
     * Each annotation is wrapped in {@link org.codehaus.aspectwerkz.annotation.AnnotationInfo} instance.
     *
     * @param method the java.lang.refect.Method object to find the annotation on.
     * @return a list with annotation
     */
    public static List getAnnotationInfos(final Method method) {
        List annotations = new ArrayList();
        Object[] attributes = Attributes.getAttributes(method);
        for (int i = 0; i < attributes.length; i++) {
            Object attribute = attributes[i];
            if (attribute instanceof AnnotationInfo) {
                annotations.add(attribute);
            }
        }
        return annotations;
    }

    /**
     * Return the annotations for a specific field.
     * <p/>
     * Each annotation is wrapped in {@link org.codehaus.aspectwerkz.annotation.AnnotationInfo} instance.
     *
     * @param field the java.lang.reflect.Field object to find the annotation on.
     * @return a list with annotation
     */
    public static List getAnnotationInfos(final Field field) {
        List annotations = new ArrayList();
        Object[] attributes = Attributes.getAttributes(field);
        for (int i = 0; i < attributes.length; i++) {
            Object attribute = attributes[i];
            if (attribute instanceof AnnotationInfo) {
                annotations.add(attribute);
            }
        }
        return annotations;
    }
}
