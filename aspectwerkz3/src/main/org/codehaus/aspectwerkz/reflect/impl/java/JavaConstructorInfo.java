/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.reflect.impl.java;

import org.codehaus.aspectwerkz.annotation.Annotations;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.ConstructorInfo;
import org.codehaus.aspectwerkz.transform.ReflectHelper;

import java.lang.reflect.Constructor;
import java.util.List;

/**
 * Implementation of the ConstructorInfo interface for java.lang.reflect.*.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r </a>
 */
public class JavaConstructorInfo extends JavaMemberInfo implements ConstructorInfo {
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
    }

    /**
     * Returns the constructor info for the constructor specified.
     *
     * @param constructor the constructor
     * @return the constructor info
     */
    public static ConstructorInfo getConstructorInfo(final Constructor constructor) {
        Class declaringClass = constructor.getDeclaringClass();
        JavaClassInfoRepository repository = JavaClassInfoRepository.getRepository(declaringClass.getClassLoader());
        ClassInfo classInfo = repository.getClassInfo(declaringClass.getName());
        if (classInfo == null) {
            classInfo = JavaClassInfo.getClassInfo(declaringClass);
        }
        return classInfo.getConstructor(ReflectHelper.calculateHash(constructor));
    }

    /**
     * Returns the attributes.
     *
     * @return the attributes
     * @TODO: fix constructor annotations
     */
    public List getAnnotations() {
        if (m_annotations == null) {
            m_annotations = Annotations.getAnnotationInfos((Constructor) m_member);
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
            Class[] parameterTypes = ((Constructor) m_member).getParameterTypes();
            m_parameterTypes = new ClassInfo[parameterTypes.length];
            for (int i = 0; i < parameterTypes.length; i++) {
                Class parameterType = parameterTypes[i];
                ClassInfo metaData;
                if (m_classInfoRepository.hasClassInfo(parameterType.getName())) {
                    metaData = m_classInfoRepository.getClassInfo(parameterType.getName());
                } else {
                    metaData = JavaClassInfo.getClassInfo(parameterType);
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
            Class[] exceptionTypes = ((Constructor) m_member).getExceptionTypes();
            m_exceptionTypes = new ClassInfo[exceptionTypes.length];
            for (int i = 0; i < exceptionTypes.length; i++) {
                Class exceptionType = exceptionTypes[i];
                ClassInfo metaData;
                if (m_classInfoRepository.hasClassInfo(exceptionType.getName())) {
                    metaData = m_classInfoRepository.getClassInfo(exceptionType.getName());
                } else {
                    metaData = JavaClassInfo.getClassInfo(exceptionType);
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
        ConstructorInfo constructorInfo = (ConstructorInfo) o;
        if (!m_declaringType.getName().equals(constructorInfo.getDeclaringType().getName())) {
            return false;
        }
        if (!m_member.getName().equals(constructorInfo.getName())) {
            return false;
        }
        Class[] parameterTypes1 = ((Constructor) m_member).getParameterTypes();
        ClassInfo[] parameterTypes2 = constructorInfo.getParameterTypes();
        if (parameterTypes1.length != parameterTypes2.length) {
            return false;
        }
        for (int i = 0; i < parameterTypes1.length; i++) {
            if (!parameterTypes1[i].getName().equals(parameterTypes2[i].getName())) {
                return false;
            }
        }
        return true;
    }

    public int hashCode() {
        int result = 29;
        result = (29 * result) + m_declaringType.getName().hashCode();
        result = (29 * result) + m_member.getName().hashCode();
        Class[] parameterTypes = ((Constructor) m_member).getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            result = (29 * result) + parameterTypes[i].getName().hashCode();
        }
        return result;
    }
}