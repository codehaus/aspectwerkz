/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.reflect.impl.javassist;

import org.codehaus.aspectwerkz.reflect.ConstructorInfo;
import java.util.Arrays;
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
     */
    public JavassistConstructorInfo(final CtConstructor constructor, final JavassistClassInfo declaringType,
                                    final ClassLoader loader) {
        super(constructor, declaringType, loader);
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

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof JavassistCodeInfo)) {
            return false;
        }
        final JavassistCodeInfo javassistCodeInfo = (JavassistCodeInfo)o;
        if ((m_attributes != null) ? (!m_attributes.equals(javassistCodeInfo.m_attributes))
                                   : (javassistCodeInfo.m_attributes != null)) {
            return false;
        }
        if ((m_member != null) ? (!m_member.equals(javassistCodeInfo.m_member)) : (javassistCodeInfo.m_member != null)) {
            return false;
        }
        if ((m_classInfoRepository != null) ? (!m_classInfoRepository.equals(javassistCodeInfo.m_classInfoRepository))
                                            : (javassistCodeInfo.m_classInfoRepository != null)) {
            return false;
        }
        if ((m_declaringType != null) ? (!m_declaringType.equals(javassistCodeInfo.m_declaringType))
                                      : (javassistCodeInfo.m_declaringType != null)) {
            return false;
        }
        if (!Arrays.equals(m_exceptionTypes, javassistCodeInfo.m_exceptionTypes)) {
            return false;
        }
        if ((m_loader != null) ? (!m_loader.equals(javassistCodeInfo.m_loader)) : (javassistCodeInfo.m_loader != null)) {
            return false;
        }
        if (!Arrays.equals(m_parameterTypes, javassistCodeInfo.m_parameterTypes)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        int result;
        result = ((m_member != null) ? m_member.hashCode() : 0);
        result = (29 * result) + ((m_declaringType != null) ? m_declaringType.hashCode() : 0);
        result = (29 * result) + ((m_attributes != null) ? m_attributes.hashCode() : 0);
        result = (29 * result) + ((m_classInfoRepository != null) ? m_classInfoRepository.hashCode() : 0);
        result = (29 * result) + ((m_loader != null) ? m_loader.hashCode() : 0);
        return result;
    }
}
