/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.reflect.impl.javassist;

import org.codehaus.aspectwerkz.annotation.AnnotationInfo;
import org.codehaus.aspectwerkz.annotation.instrumentation.AttributeExtractor;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.ConstructorInfo;
import org.codehaus.aspectwerkz.reflect.MethodInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javassist.CtConstructor;

/**
 * Implementation of the ConstructorInfo interface for Javassist.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class JavassistConstructorInfo extends JavassistCodeInfo implements ConstructorInfo {
    /**
     * Caches the constructor infos.
     */
    private static final Map s_cache = new WeakHashMap();

    /**
     * Creates a new method meta data instance.
     *
     * @param constructor
     * @param declaringType
     * @param loader
     * @param attributeExtractor
     */
    JavassistConstructorInfo(final CtConstructor constructor, final JavassistClassInfo declaringType,
                             final ClassLoader loader, final AttributeExtractor attributeExtractor) {
        super(constructor, declaringType, loader, attributeExtractor);
        JavassistConstructorInfo.addConstructorInfo(constructor, this);
    }

    /**
     * Returns the constructor info for the constructor specified.
     *
     * @param constructor the constructor
     * @return the constructor info
     */
    public static JavassistConstructorInfo getConstructorInfo(final CtConstructor constructor, final ClassLoader loader) {
        JavassistConstructorInfo constructorInfo = (JavassistConstructorInfo)s_cache.get(constructor);
        if (constructorInfo == null) {
            new JavassistClassInfo(constructor.getDeclaringClass(), loader);
            constructorInfo = (JavassistConstructorInfo)s_cache.get(constructor);
        }
        return constructorInfo;
    }

    /**
     * Adds the constructor info to the cache.
     *
     * @param constructor the constructor
     * @param methodInfo  the constructor info
     */
    public static void addConstructorInfo(final CtConstructor constructor, final JavassistConstructorInfo methodInfo) {
        s_cache.put(constructor, methodInfo);
    }

    /**
     * Returns the attributes.
     *
     * @return the attributes
     */
    public List getAnnotations() {
        if (m_annotations == null) {
            addAnnotations();
        }
        return m_annotations;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MethodInfo)) {
            return false;
        }
        MethodInfo constructorInfo = (MethodInfo)o;
        if (!m_declaringType.getName().toString().equals(constructorInfo.getDeclaringType().getName().toString())) {
            return false;
        }
        if (!m_member.getName().toString().equals(constructorInfo.getName().toString())) {
            return false;
        }
        ClassInfo[] parameterTypes = constructorInfo.getParameterTypes();
        if (m_parameterTypes.length != parameterTypes.length) {
            return false;
        }
        for (int i = 0; i < m_parameterTypes.length; i++) {
            if (!m_parameterTypes[i].getName().toString().equals(parameterTypes[i].getName().toString())) {
                return false;
            }
        }
        return true;
    }

    public int hashCode() {
        int result = 29;
        result = (29 * result) + m_declaringType.getName().toString().hashCode();
        result = (29 * result) + m_member.getName().toString().hashCode();
        if (m_parameterTypes == null) {
            getParameterTypes();
        }
        for (int i = 0; i < m_parameterTypes.length; i++) {
            result = (29 * result) + m_parameterTypes[i].getName().toString().hashCode();
        }
        return result;
    }

    /**
     * Adds annotations to the method info.
     */
    private void addAnnotations() {
        if (m_attributeExtractor == null) {
            return;
        }
        if (m_parameterTypes == null) {
            getParameterTypes();
        }
        String[] parameterNames = new String[m_parameterTypes.length];
        for (int i = 0; i < m_parameterTypes.length; i++) {
            parameterNames[i] = m_parameterTypes[i].getName();
        }

        Object[] attributes = m_attributeExtractor.getMethodAttributes(m_member.getName(), parameterNames);
        m_annotations = new ArrayList();
        for (int i = 0; i < attributes.length; i++) {
            Object attribute = attributes[i];
            if (attribute instanceof AnnotationInfo) {
                m_annotations.add(attribute);
            }
        }
    }
}
