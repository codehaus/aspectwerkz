/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.reflect.impl.java;

import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.FieldInfo;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Implementation of the FieldInfo interface for java.lang.reflect.*.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class JavaFieldInfo extends JavaMemberInfo implements FieldInfo {
    /**
     * Caches the field infos.
     */
    private static final Map s_cache = new WeakHashMap();

    /**
     * The field type.
     */
    private ClassInfo m_type = null;

    /**
     * Creates a new field java instance.
     *
     * @param field
     * @param declaringType
     */
    public JavaFieldInfo(final Field field, final JavaClassInfo declaringType) {
        super(field, declaringType);
        JavaFieldInfo.addFieldInfo(field, this);
    }

    /**
     * Returns the field info for the field specified.
     *
     * @param field the field
     * @return the field info
     */
    public static JavaFieldInfo getFieldInfo(final Field field) {
        JavaFieldInfo fieldInfo = (JavaFieldInfo)s_cache.get(field);
        if (fieldInfo == null) { //  declaring class is not loaded yet; load it and retry
            new JavaClassInfo(field.getDeclaringClass());
            fieldInfo = (JavaFieldInfo)s_cache.get(field);
        }
        return fieldInfo;
    }

    /**
     * Adds the field info to the cache.
     *
     * @param field     the field
     * @param fieldInfo the field info
     */
    public static void addFieldInfo(final Field field, final JavaFieldInfo fieldInfo) {
        s_cache.put(field, fieldInfo);
    }

    /**
     * Returns the type.
     *
     * @return the type
     */
    public ClassInfo getType() {
        if (m_type == null) {
            Class type = ((Field)m_member).getType();
            if (m_classInfoRepository.hasClassInfo(type.getName())) {
                m_type = m_classInfoRepository.getClassInfo(type.getName());
            } else {
                m_type = new JavaClassInfo(type);
                m_classInfoRepository.addClassInfo(m_type);
            }
        }
        return m_type;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof JavaFieldInfo)) {
            return false;
        }
        final JavaFieldInfo javaFieldInfo = (JavaFieldInfo)o;
        if ((m_attributes != null) ? (!m_attributes.equals(javaFieldInfo.m_attributes))
                                   : (javaFieldInfo.m_attributes != null)) {
            return false;
        }
        if ((m_classInfoRepository != null) ? (!m_classInfoRepository.equals(javaFieldInfo.m_classInfoRepository))
                                            : (javaFieldInfo.m_classInfoRepository != null)) {
            return false;
        }
        if ((m_declaringType != null) ? (!m_declaringType.equals(javaFieldInfo.m_declaringType))
                                      : (javaFieldInfo.m_declaringType != null)) {
            return false;
        }
        if ((m_member != null) ? (!m_member.equals(javaFieldInfo.m_member)) : (javaFieldInfo.m_member != null)) {
            return false;
        }
        if ((m_type != null) ? (!m_type.equals(javaFieldInfo.m_type)) : (javaFieldInfo.m_type != null)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        int result;

        result = ((m_member != null) ? m_member.hashCode() : 0);
        result = (29 * result) + ((m_type != null) ? m_type.hashCode() : 0);
        result = (29 * result) + ((m_declaringType != null) ? m_declaringType.hashCode() : 0);
        result = (29 * result) + ((m_attributes != null) ? m_attributes.hashCode() : 0);
        result = (29 * result) + ((m_classInfoRepository != null) ? m_classInfoRepository.hashCode() : 0);

        return result;
    }
}
