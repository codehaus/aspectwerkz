/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.reflect.impl.java;

import org.codehaus.aspectwerkz.annotation.Annotations;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.MethodInfo;
import java.lang.reflect.Method;
import java.lang.ref.WeakReference;
import java.util.List;

import gnu.trove.TIntObjectHashMap;

/**
 * Implementation of the MethodInfo interface for java.lang.reflect.*.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class JavaMethodInfo extends JavaMemberInfo implements MethodInfo {
    /**
     * Caches the method infos.
     */
    private static final TIntObjectHashMap s_cache = new TIntObjectHashMap();

    /**
     * The return type.
     */
    private ClassInfo m_returnType = null;

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
     * @param method
     * @param declaringType
     */
    JavaMethodInfo(final Method method, final JavaClassInfo declaringType) {
        super(method, declaringType);
        JavaMethodInfo.addMethodInfo(method, this);
    }

    /**
     * Returns the method info for the method specified.
     *
     * @param method the method
     * @return the method info
     */
    public static JavaMethodInfo getMethodInfo(final Method method) {
        int hash = method.hashCode();
        JavaMethodInfo methodInfo = (JavaMethodInfo)((WeakReference)s_cache.get(hash)).get();
        if (methodInfo == null) { //  declaring class is not loaded yet; load it and retry
            new JavaClassInfo(method.getDeclaringClass());
            methodInfo = (JavaMethodInfo)((WeakReference)s_cache.get(hash)).get();
        }
        return methodInfo;
    }

    /**
     * Adds the method info to the cache.
     *
     * @param method     the method
     * @param methodInfo the method info
     */
    public static void addMethodInfo(final Method method, final JavaMethodInfo methodInfo) {
        s_cache.put(method.hashCode(), new WeakReference(methodInfo));
    }

    /**
     * Returns the attributes.
     *
     * @return the attributes
     */
    public List getAnnotations() {
        if (m_annotations == null) {
            m_annotations = Annotations.getAnnotationInfos((Method)m_member);
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
            Class returnTypeClass = ((Method)m_member).getReturnType();
            if (m_classInfoRepository.hasClassInfo(returnTypeClass.getName())) {
                m_returnType = m_classInfoRepository.getClassInfo(returnTypeClass.getName());
            } else {
                m_returnType = new JavaClassInfo(returnTypeClass);
                m_classInfoRepository.addClassInfo(m_returnType);
            }
        }
        return m_returnType;
    }

    /**
     * Returns the parameter types.
     *
     * @return the parameter types
     */
    public ClassInfo[] getParameterTypes() {
        if (m_parameterTypes == null) {
            Class[] parameterTypes = ((Method)m_member).getParameterTypes();
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
            Class[] exceptionTypes = ((Method)m_member).getExceptionTypes();
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
}
