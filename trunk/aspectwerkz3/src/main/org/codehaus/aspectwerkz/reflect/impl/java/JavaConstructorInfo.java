/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.reflect.impl.java;

import gnu.trove.TIntObjectHashMap;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.ConstructorInfo;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.util.List;

/**
 * Implementation of the ConstructorInfo interface for java.lang.reflect.*.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class JavaConstructorInfo extends JavaMemberInfo implements ConstructorInfo {
    /**
     * Caches the constructor infos.
     */
    private static final TIntObjectHashMap s_cache = new TIntObjectHashMap();

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
    JavaConstructorInfo(final Constructor constructor, final JavaClassInfo declaringType) {
        super(constructor, declaringType);
        JavaConstructorInfo.addConstructorInfo(constructor, this);
    }

    /**
     * Returns the constructor info for the constructor specified.
     *
     * @param constructor the constructor
     * @return the constructor info
     */
    public static JavaConstructorInfo getConstructorInfo(final Constructor constructor) {
        int hash = constructor.hashCode();
        WeakReference constructorInfoRef = (WeakReference)s_cache.get(hash);
        JavaConstructorInfo constructorInfo = ((constructorInfoRef == null) ? null
                                                                            : (JavaConstructorInfo)constructorInfoRef
                                                                              .get());
        if ((constructorInfoRef == null) || (constructorInfo == null)) {
            new JavaClassInfo(constructor.getDeclaringClass());
            constructorInfo = (JavaConstructorInfo)((WeakReference)s_cache.get(hash)).get();
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
        s_cache.put(constructor.hashCode(), new WeakReference(methodInfo));
    }

    /**
     * Returns the attributes.
     *
     * @return the attributes
     */
    public List getAnnotations() {
        if (m_annotations == null) {
            // TODO: fix constructor annotations
            //            m_annotations = Annotations.getAnnotationInfos((Constructor)m_member);
        }
        return m_annotations;
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
}
