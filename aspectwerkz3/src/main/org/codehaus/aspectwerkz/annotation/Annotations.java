/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.annotation;

import org.codehaus.aspectwerkz.definition.attribute.CustomAttribute;
import org.codehaus.aspectwerkz.definition.attribute.Attributes;

import java.lang.reflect.Method;
import java.lang.reflect.Field;

/**
 * Utility class for annotation retrieval.
 * <p/>
 * PLEASE NOTE: this class will change in the future, the CustomAttribute return types will be user defined
 * annotation wrappers to have a single interface to both JavaDoc and JSR-175 annotations.
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
    public static CustomAttribute getAnnotation(final String annotationName, final Class klass) {
        Object[] attributes = Attributes.getAttributes(klass);
        for (int i = 0; i < attributes.length; i++) {
            Object attribute = attributes[i];
            if (attribute instanceof CustomAttribute) {
                CustomAttribute attr = (CustomAttribute)attribute;
                if (attr.getName().equalsIgnoreCase(annotationName)) {
                    return attr;
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
    public static CustomAttribute getAnnotation(final String annotationName, final Method method) {
        Object[] attributes = Attributes.getAttributes(method);
        for (int i = 0; i < attributes.length; i++) {
            Object attribute = attributes[i];
            if (attribute instanceof CustomAttribute) {
                CustomAttribute attr = (CustomAttribute)attribute;
                if (attr.getName().equalsIgnoreCase(annotationName)) {
                    return attr;
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
    public static CustomAttribute getAnnotation(final String annotationName, final Field field) {
        Object[] attributes = Attributes.getAttributes(field);
        for (int i = 0; i < attributes.length; i++) {
            Object attribute = attributes[i];
            if (attribute instanceof CustomAttribute) {
                CustomAttribute attr = (CustomAttribute)attribute;
                if (attr.getName().equalsIgnoreCase(annotationName)) {
                    return attr;
                }
            }
        }
        return null;
    }
}
                                                                                          f