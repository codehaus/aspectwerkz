/**************************************************************************************
 * Copyright (c) Jonas Bon?r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.annotation.instrumentation.asm;

import org.codehaus.aspectwerkz.annotation.Annotation;
import org.codehaus.aspectwerkz.annotation.AnnotationInfo;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.MethodInfo;
import org.codehaus.aspectwerkz.reflect.ConstructorInfo;
import org.codehaus.aspectwerkz.reflect.FieldInfo;

import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

/**
 * Helper class to extract annotations by their name from a ClassInfo structure.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class AsmAnnotations {
    /**
     * Return the annotation with a specific name for a specific class.
     *
     * @param annotationName the annotation name
     * @param classInfo      the ClassInfo object to find the annotation on.
     * @return the annotation or null
     */
    public static Annotation getAnnotation(final String annotationName, final ClassInfo classInfo) {
        List annotations = classInfo.getAnnotations();
        for (Iterator it = annotations.iterator(); it.hasNext();) {
            AnnotationInfo annotationInfo = (AnnotationInfo) it.next();
            if (annotationInfo.getName().equals(annotationName)) {
                return annotationInfo.getAnnotation();
            }
        }
        return null;
    }

    /**
     * Return the annotation with a specific name for a specific method.
     *
     * @param annotationName the annotation name
     * @param methodInfo     the MethodInfo object to find the annotation on.
     * @return the annotation or null
     */
    public static Annotation getAnnotation(final String annotationName, final MethodInfo methodInfo) {
        List annotations = methodInfo.getAnnotations();
        for (Iterator it = annotations.iterator(); it.hasNext();) {
            AnnotationInfo annotationInfo = (AnnotationInfo) it.next();
            if (annotationInfo.getName().equals(annotationName)) {
                return annotationInfo.getAnnotation();
            }
        }
        return null;
    }

    /**
     * Return the annotation with a specific name for a specific constructor.
     *
     * @param annotationName  the annotation name
     * @param constructorInfo the ConstructorInfo object to find the annotation on.
     * @return the annotation or null
     */
    public static Annotation getAnnotation(final String annotationName, final ConstructorInfo constructorInfo) {
        List annotations = constructorInfo.getAnnotations();
        for (Iterator it = annotations.iterator(); it.hasNext();) {
            AnnotationInfo annotationInfo = (AnnotationInfo) it.next();
            if (annotationInfo.getName().equals(annotationName)) {
                return annotationInfo.getAnnotation();
            }
        }
        return null;
    }

    /**
     * Return the annotation with a specific name for a specific field.
     *
     * @param annotationName the annotation name
     * @param fieldInfo      the FieldInfo object to find the annotation on.
     * @return the annotation or null
     */
    public static Annotation getAnnotation(final String annotationName, final FieldInfo fieldInfo) {
        List annotations = fieldInfo.getAnnotations();
        for (Iterator it = annotations.iterator(); it.hasNext();) {
            AnnotationInfo annotationInfo = (AnnotationInfo) it.next();
            if (annotationInfo.getName().equals(annotationName)) {
                return annotationInfo.getAnnotation();
            }
        }
        return null;
    }

    /**
     * Return a list with the annotations with a specific name for a specific class.
     *
     * @param annotationName the annotation name
     * @param classInfo      ClassInfo object to find the annotation on.
     * @return the annotations in a list (can be empty)
     */
    public static List getAnnotations(final String annotationName, final ClassInfo classInfo) {
        List annotations = new ArrayList();
        for (Iterator it = classInfo.getAnnotations().iterator(); it.hasNext();) {
            AnnotationInfo annotationInfo = (AnnotationInfo) it.next();
            if (annotationInfo.getName().equals(annotationName)) {
                annotations.add(annotationInfo.getAnnotation());
            }
        }
        return annotations;
    }

    /**
     * Return a list with the annotations with a specific name for a specific method.
     *
     * @param annotationName the annotation name
     * @param methodInfo     the MethodInfo object to find the annotation on.
     * @return the annotations in a list (can be empty)
     */
    public static List getAnnotations(final String annotationName, final MethodInfo methodInfo) {
        List annotations = new ArrayList();
        for (Iterator it = methodInfo.getAnnotations().iterator(); it.hasNext();) {
            AnnotationInfo annotationInfo = (AnnotationInfo) it.next();
            if (annotationInfo.getName().equals(annotationName)) {
                annotations.add(annotationInfo.getAnnotation());
            }
        }
        return annotations;
    }

    /**
     * Return a list with the annotations with a specific name for a specific constructor.
     *
     * @param annotationName  the annotation name
     * @param constructorInfo the ConstructorInfo object to find the annotation on.
     * @return the annotations in a list (can be empty)
     */
    public static List getAnnotations(final String annotationName, final ConstructorInfo constructorInfo) {
        List annotations = new ArrayList();
        for (Iterator it = constructorInfo.getAnnotations().iterator(); it.hasNext();) {
            AnnotationInfo annotationInfo = (AnnotationInfo) it.next();
            if (annotationInfo.getName().equals(annotationName)) {
                annotations.add(annotationInfo.getAnnotation());
            }
        }
        return annotations;
    }

    /**
     * Return a list with the annotations with a specific name for a specific field.
     *
     * @param annotationName the annotation name
     * @param fieldInfo      the FieldInfo object to find the annotation on.
     * @return the annotations in a list (can be empty)
     */
    public static List getAnnotations(final String annotationName, final FieldInfo fieldInfo) {
        List annotations = new ArrayList();
        for (Iterator it = fieldInfo.getAnnotations().iterator(); it.hasNext();) {
            AnnotationInfo annotationInfo = (AnnotationInfo) it.next();
            if (annotationInfo.getName().equals(annotationName)) {
                annotations.add(annotationInfo.getAnnotation());
            }
        }
        return annotations;
    }

}
