/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.reflect.impl.java;

import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.ConstructorInfo;

import java.lang.reflect.Constructor;

import java.util.Arrays;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Implementation of the ConstructorInfo interface for java.lang.reflect.*.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class JavaConstructorInfo extends JavaMemberInfo
    implements ConstructorInfo
{
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
    public JavaConstructorInfo(final Constructor constructor,
        final JavaClassInfo declaringType)
    {
        super(constructor, declaringType);
        JavaConstructorInfo.addConstructorInfo(constructor, this);
    }

    /**
     * Returns the constructor info for the constructor specified.
     *
     * @param constructor the constructor
     * @return the constructor info
     */
    public static JavaConstructorInfo getConstructorInfo(
        final Constructor constructor)
    {
        JavaConstructorInfo constructorInfo = (JavaConstructorInfo) s_cache.get(constructor);

        if (constructorInfo == null)
        {
            new JavaClassInfo(constructor.getDeclaringClass());
            constructorInfo = (JavaConstructorInfo) s_cache.get(constructor);
        }

        return constructorInfo;
    }

    /**
     * Adds the constructor info to the cache.
     *
     * @param constructor the constructor
     * @param methodInfo  the constructor info
     */
    public static void addConstructorInfo(final Constructor constructor,
        final JavaConstructorInfo methodInfo)
    {
        s_cache.put(constructor, methodInfo);
    }

    /**
     * Returns the parameter types.
     *
     * @return the parameter types
     */
    public ClassInfo[] getParameterTypes()
    {
        if (m_parameterTypes == null)
        {
            Class[] parameterTypes = ((Constructor) m_member).getParameterTypes();

            m_parameterTypes = new ClassInfo[parameterTypes.length];

            for (int i = 0; i < parameterTypes.length; i++)
            {
                Class parameterType = parameterTypes[i];
                ClassInfo metaData;

                if (m_classInfoRepository.hasClassInfo(parameterType.getName()))
                {
                    metaData = m_classInfoRepository.getClassInfo(parameterType
                            .getName());
                }
                else
                {
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
    public ClassInfo[] getExceptionTypes()
    {
        if (m_exceptionTypes == null)
        {
            Class[] exceptionTypes = ((Constructor) m_member).getExceptionTypes();

            m_exceptionTypes = new ClassInfo[exceptionTypes.length];

            for (int i = 0; i < exceptionTypes.length; i++)
            {
                Class exceptionType = exceptionTypes[i];
                ClassInfo metaData;

                if (m_classInfoRepository.hasClassInfo(exceptionType.getName()))
                {
                    metaData = m_classInfoRepository.getClassInfo(exceptionType
                            .getName());
                }
                else
                {
                    metaData = new JavaClassInfo(exceptionType);
                    m_classInfoRepository.addClassInfo(metaData);
                }

                m_exceptionTypes[i] = metaData;
            }
        }

        return m_exceptionTypes;
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (!(o instanceof JavaConstructorInfo))
        {
            return false;
        }

        final JavaConstructorInfo javaConstructorInfo = (JavaConstructorInfo) o;

        if ((m_attributes != null)
            ? (!m_attributes.equals(javaConstructorInfo.m_attributes))
            : (javaConstructorInfo.m_attributes != null))
        {
            return false;
        }

        if ((m_classInfoRepository != null)
            ? (!m_classInfoRepository.equals(
                javaConstructorInfo.m_classInfoRepository))
            : (javaConstructorInfo.m_classInfoRepository != null))
        {
            return false;
        }

        if ((m_member != null) ? (!m_member.equals(javaConstructorInfo.m_member))
                               : (javaConstructorInfo.m_member != null))
        {
            return false;
        }

        if ((m_declaringType != null)
            ? (!m_declaringType.equals(javaConstructorInfo.m_declaringType))
            : (javaConstructorInfo.m_declaringType != null))
        {
            return false;
        }

        if (!Arrays.equals(m_exceptionTypes,
                javaConstructorInfo.m_exceptionTypes))
        {
            return false;
        }

        if (!Arrays.equals(m_parameterTypes,
                javaConstructorInfo.m_parameterTypes))
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;

        result = ((m_member != null) ? m_member.hashCode() : 0);
        result = (29 * result)
            + ((m_declaringType != null) ? m_declaringType.hashCode() : 0);
        result = (29 * result)
            + ((m_attributes != null) ? m_attributes.hashCode() : 0);
        result = (29 * result)
            + ((m_classInfoRepository != null)
            ? m_classInfoRepository.hashCode() : 0);

        return result;
    }
}
