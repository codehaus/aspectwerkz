/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.reflect.impl.java;

import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.MethodInfo;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Implementation of the MethodInfo interface for java.lang.reflect.*.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class JavaMethodInfo extends JavaMemberInfo implements MethodInfo {
    /**
     * Caches the method infos.
     */
    private static final Map s_cache = new WeakHashMap();

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
    public JavaMethodInfo(final Method method, final JavaClassInfo declaringType) {
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
        JavaMethodInfo methodInfo = (JavaMethodInfo)s_cache.get(method);

        if (methodInfo == null) { //  declaring class is not loaded yet; load it and retry
            new JavaClassInfo(method.getDeclaringClass());
            methodInfo = (JavaMethodInfo)s_cache.get(method);
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
        s_cache.put(method, methodInfo);
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
        if (!(o instanceof JavaMethodInfo)) {
            return false;
        }
        final JavaMethodInfo javaMethodInfo = (JavaMethodInfo)o;
        if ((m_attributes != null) ? (!m_attributes.equals(javaMethodInfo.m_attributes))
                                   : (javaMethodInfo.m_attributes != null)) {
            return false;
        }
        if ((m_classInfoRepository != null) ? (!m_classInfoRepository.equals(javaMethodInfo.m_classInfoRepository))
                                            : (javaMethodInfo.m_classInfoRepository != null)) {
            return false;
        }
        if ((m_declaringType != null) ? (!m_declaringType.equals(javaMethodInfo.m_declaringType))
                                      : (javaMethodInfo.m_declaringType != null)) {
            return false;
        }
        if (!Arrays.equals(m_exceptionTypes, javaMethodInfo.m_exceptionTypes)) {
            return false;
        }
        if ((m_member != null) ? (!m_member.equals(javaMethodInfo.m_member)) : (javaMethodInfo.m_member != null)) {
            return false;
        }
        if (!Arrays.equals(m_parameterTypes, javaMethodInfo.m_parameterTypes)) {
            return false;
        }
        if ((m_returnType != null) ? (!m_returnType.equals(javaMethodInfo.m_returnType))
                                   : (javaMethodInfo.m_returnType != null)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        int result;

        result = ((m_member != null) ? m_member.hashCode() : 0);
        result = (29 * result) + ((m_returnType != null) ? m_returnType.hashCode() : 0);
        result = (29 * result) + ((m_declaringType != null) ? m_declaringType.hashCode() : 0);
        result = (29 * result) + ((m_attributes != null) ? m_attributes.hashCode() : 0);
        result = (29 * result) + ((m_classInfoRepository != null) ? m_classInfoRepository.hashCode() : 0);

        return result;
    }
}
