/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.reflect.impl.java;

import org.codehaus.aspectwerkz.definition.attribute.AttributeExtractor;
import org.codehaus.aspectwerkz.definition.attribute.CustomAttribute;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.ConstructorInfo;
import org.codehaus.aspectwerkz.transform.TransformationUtil;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Implementation of the ConstructorInfo interface for java.lang.reflect.*.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public class JavaConstructorInfo extends JavaMemberInfo implements ConstructorInfo {
    /**
     * Caches the constructor infos.
     */
    private static final Map s_cache = new WeakHashMap();

    /**
     * A list with the parameter types.
     */
    private ClassInfo[] m_parameterTypes = null;

    /**
     * A list with the exception types.
     */
    private ClassInfo[] m_exceptionTypes = null;

    /**
     * Creates a new method meta data instance.
     *
     * @param constructor
     * @param declaringType
     */
    JavaConstructorInfo(final Constructor constructor, final JavaClassInfo declaringType,
                        final AttributeExtractor attributeExtractor) {
        super(constructor, declaringType, attributeExtractor);
        JavaConstructorInfo.addConstructorInfo(constructor, this);
    }

    /**
     * Returns the constructor info for the constructor specified.
     *
     * @param constructor the constructor
     * @return the constructor info
     */
    public static JavaConstructorInfo getConstructorInfo(final Constructor constructor) {
        JavaConstructorInfo constructorInfo = (JavaConstructorInfo)s_cache.get(constructor);
        if (constructorInfo == null) {
            new JavaClassInfo(constructor.getDeclaringClass());
            constructorInfo = (JavaConstructorInfo)s_cache.get(constructor);
        }
        return constructorInfo;
    }

    /**
     * Adds the constructor info to the cache.
     *
     * @param constructor the constructor
     * @param methodInfo  the constructor info
     */
    public static void addConstructorInfo(final Constructor constructor, final JavaConstructorInfo methodInfo) {
        s_cache.put(constructor, methodInfo);
    }

    /**
     * Returns the attributes.
     *
     * @return the attributes
     */
    public List getAnnotations() {
        if (m_annotations == null) {
            m_annotations = new ArrayList();
            addAnnotations();
        }
        return m_annotations;
    }

    /**
     * Adds an attribute.
     *
     * @param attribute the attribute
     */
    public void addAnnotation(final Object attribute) {
        m_annotations.add(attribute);
    }

    /**
     * Returns the parameter types.
     *
     * @return the parameter types
     */
    public ClassInfo[] getParameterTypes() {
        if (m_parameterTypes == null) {
            Class[] parameterTypes = ((Constructor)m_member).getParameterTypes();
            m_parameterTypes = new ClassInfo[parameterTypes.length];
            for (int i = 0; i < parameterTypes.length; i++) {
                Class parameterType = parameterTypes[i];
                ClassInfo metaData;
                if (m_classInfoRepository.hasClassInfo(parameterType.getName())) {
                    metaData = m_classInfoRepository.getClassInfo(parameterType.getName());
                } else {
                    metaData = new JavaClassInfo(parameterType);
                    m_classInfoRepository.addClassInfo(metaData);
                }
                m_parameterTypes[i] = metaData;
            }
        }
        return m_parameterTypes;
    }

    /**
     * Returns the exception types.
     *
     * @return the exception types
     */
    public ClassInfo[] getExceptionTypes() {
        if (m_exceptionTypes == null) {
            Class[] exceptionTypes = ((Constructor)m_member).getExceptionTypes();
            m_exceptionTypes = new ClassInfo[exceptionTypes.length];
            for (int i = 0; i < exceptionTypes.length; i++) {
                Class exceptionType = exceptionTypes[i];
                ClassInfo metaData;
                if (m_classInfoRepository.hasClassInfo(exceptionType.getName())) {
                    metaData = m_classInfoRepository.getClassInfo(exceptionType.getName());
                } else {
                    metaData = new JavaClassInfo(exceptionType);
                    m_classInfoRepository.addClassInfo(metaData);
                }
                m_exceptionTypes[i] = metaData;
            }
        }
        return m_exceptionTypes;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ConstructorInfo)) {
            return false;
        }
        ConstructorInfo constructorInfo = (ConstructorInfo)o;
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
        Object[] attributes = m_attributeExtractor.getMethodAttributes(getName(), parameterNames);
        for (int i = 0; i < attributes.length; i++) {
            Object attribute = attributes[i];
            if (attribute instanceof CustomAttribute) {
                CustomAttribute custom = (CustomAttribute)attribute;
                if (custom.getName().startsWith(TransformationUtil.ASPECTWERKZ_PREFIX)) {
                    // skip 'system' annotations
                    continue;
                }
                addAnnotation(custom);
            }
        }
    }
}
