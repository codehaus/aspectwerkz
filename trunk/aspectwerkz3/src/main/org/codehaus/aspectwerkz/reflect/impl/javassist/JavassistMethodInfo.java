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
import org.codehaus.aspectwerkz.reflect.MethodInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

/**
 * Implementation of the MethodInfo interface for Javassist.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class JavassistMethodInfo extends JavassistCodeInfo implements MethodInfo {
    /**
     * Caches the method infos.
     */
    private static final Map s_cache = new WeakHashMap();

    /**
     * The return type.
     */
    private ClassInfo m_returnType = null;

    /**
     * Creates a new method meta data instance.
     *
     * @param method
     * @param declaringType
     * @param loader
     * @param attributeExtractor
     */
    JavassistMethodInfo(final CtMethod method, final JavassistClassInfo declaringType, final ClassLoader loader,
                        final AttributeExtractor attributeExtractor) {
        super(method, declaringType, loader, attributeExtractor);
        JavassistMethodInfo.addMethodInfo(method, this);
    }

    /**
     * Returns the method info for the method specified.
     *
     * @param method      the method
     * @param classLoader the class loader
     * @return the method info
     */
    public static JavassistMethodInfo getMethodInfo(final CtMethod method, final ClassLoader classLoader) {
        JavassistMethodInfo methodInfo = (JavassistMethodInfo)s_cache.get(method);
        if (methodInfo == null) {
            new JavassistClassInfo(method.getDeclaringClass(), classLoader);
            methodInfo = (JavassistMethodInfo)s_cache.get(method);
        }
        return methodInfo;
    }

    /**
     * Adds the method info to the cache.
     *
     * @param method     the method
     * @param methodInfo the method info
     */
    public static void addMethodInfo(final CtMethod method, final JavassistMethodInfo methodInfo) {
        s_cache.put(method, methodInfo);
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

    /**
     * Returns the return type.
     *
     * @return the return type
     */
    public ClassInfo getReturnType() {
        if (m_returnType == null) {
            try {
                CtClass returnTypeClass = ((CtMethod)m_member).getReturnType();
                if (m_classInfoRepository.hasClassInfo(returnTypeClass.getName())) {
                    m_returnType = m_classInfoRepository.getClassInfo(returnTypeClass.getName());
                } else {
                    m_returnType = new JavassistClassInfo(returnTypeClass, m_loader);
                    m_classInfoRepository.addClassInfo(m_returnType);
                }
            } catch (NotFoundException e) {
                e.printStackTrace();
            }
        }
        return m_returnType;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MethodInfo)) {
            return false;
        }
        MethodInfo methodInfo = (MethodInfo)o;
        if (!m_declaringType.getName().toString().equals(methodInfo.getDeclaringType().getName().toString())) {
            return false;
        }
        if (!m_member.getName().toString().equals(methodInfo.getName().toString())) {
            return false;
        }
        ClassInfo[] parameterTypes = methodInfo.getParameterTypes();
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
        m_annotations = new ArrayList();
        String[] parameterNames = new String[m_parameterTypes.length];
        for (int i = 0; i < m_parameterTypes.length; i++) {
            parameterNames[i] = m_parameterTypes[i].getName();
        }
        Object[] attributes = m_attributeExtractor.getMethodAttributes(getName(), parameterNames);
        m_annotations = new ArrayList();
        for (int i = 0; i < attributes.length; i++) {
            Object attribute = attributes[i];
            if (attribute instanceof AnnotationInfo) {
                m_annotations.add(attribute);
            }
        }
    }
}
