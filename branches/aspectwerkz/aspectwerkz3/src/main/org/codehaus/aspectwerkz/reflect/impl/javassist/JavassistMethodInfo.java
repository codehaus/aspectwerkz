/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.reflect.impl.javassist;

import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.MethodInfo;

import java.util.Arrays;
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
public class JavassistMethodInfo extends JavassistCodeInfo implements MethodInfo
{
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
     */
    public JavassistMethodInfo(final CtMethod method,
        final JavassistClassInfo declaringType, final ClassLoader loader)
    {
        super(method, declaringType, loader);
        JavassistMethodInfo.addMethodInfo(method, this);
    }

    /**
     * Returns the method info for the method specified.
     *
     * @param method      the method
     * @param classLoader the class loader
     * @return the method info
     */
    public static JavassistMethodInfo getMethodInfo(final CtMethod method,
        final ClassLoader classLoader)
    {
        JavassistMethodInfo methodInfo = (JavassistMethodInfo) s_cache.get(method);

        if (methodInfo == null)
        {
            new JavassistClassInfo(method.getDeclaringClass(), classLoader);
            methodInfo = (JavassistMethodInfo) s_cache.get(method);
        }

        return methodInfo;
    }

    /**
     * Adds the method info to the cache.
     *
     * @param method     the method
     * @param methodInfo the method info
     */
    public static void addMethodInfo(final CtMethod method,
        final JavassistMethodInfo methodInfo)
    {
        s_cache.put(method, methodInfo);
    }

    /**
     * Returns the return type.
     *
     * @return the return type
     */
    public ClassInfo getReturnType()
    {
        if (m_returnType == null)
        {
            try
            {
                CtClass returnTypeClass = ((CtMethod) m_member).getReturnType();

                if (m_classInfoRepository.hasClassInfo(
                        returnTypeClass.getName()))
                {
                    m_returnType = m_classInfoRepository.getClassInfo(returnTypeClass
                            .getName());
                }
                else
                {
                    m_returnType = new JavassistClassInfo(returnTypeClass,
                            m_loader);
                    m_classInfoRepository.addClassInfo(m_returnType);
                }
            }
            catch (NotFoundException e)
            {
                e.printStackTrace();
            }
        }

        return m_returnType;
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (!(o instanceof JavassistMethodInfo))
        {
            return false;
        }

        final JavassistMethodInfo javassistMethodInfo = (JavassistMethodInfo) o;

        if ((m_attributes != null)
            ? (!m_attributes.equals(javassistMethodInfo.m_attributes))
            : (javassistMethodInfo.m_attributes != null))
        {
            return false;
        }

        if ((m_classInfoRepository != null)
            ? (!m_classInfoRepository.equals(
                javassistMethodInfo.m_classInfoRepository))
            : (javassistMethodInfo.m_classInfoRepository != null))
        {
            return false;
        }

        if ((m_declaringType != null)
            ? (!m_declaringType.equals(javassistMethodInfo.m_declaringType))
            : (javassistMethodInfo.m_declaringType != null))
        {
            return false;
        }

        if (!Arrays.equals(m_exceptionTypes,
                javassistMethodInfo.m_exceptionTypes))
        {
            return false;
        }

        if ((m_loader != null) ? (!m_loader.equals(javassistMethodInfo.m_loader))
                               : (javassistMethodInfo.m_loader != null))
        {
            return false;
        }

        if ((m_member != null) ? (!m_member.equals(javassistMethodInfo.m_member))
                               : (javassistMethodInfo.m_member != null))
        {
            return false;
        }

        if (!Arrays.equals(m_parameterTypes,
                javassistMethodInfo.m_parameterTypes))
        {
            return false;
        }

        if ((m_returnType != null)
            ? (!m_returnType.equals(javassistMethodInfo.m_returnType))
            : (javassistMethodInfo.m_returnType != null))
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
            + ((m_returnType != null) ? m_returnType.hashCode() : 0);
        result = (29 * result)
            + ((m_declaringType != null) ? m_declaringType.hashCode() : 0);
        result = (29 * result)
            + ((m_attributes != null) ? m_attributes.hashCode() : 0);
        result = (29 * result)
            + ((m_classInfoRepository != null)
            ? m_classInfoRepository.hashCode() : 0);
        result = (29 * result) + ((m_loader != null) ? m_loader.hashCode() : 0);

        return result;
    }
}
