/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.reflect.impl.java;

import gnu.trove.TIntObjectHashMap;
import org.codehaus.aspectwerkz.annotation.Annotations;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.FieldInfo;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.List;

/**
 * Implementation of the FieldInfo interface for java.lang.reflect.*.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class JavaFieldInfo extends JavaMemberInfo implements FieldInfo {
    /**
     * Caches the field infos.
     */
    private static final TIntObjectHashMap s_cache = new TIntObjectHashMap();

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
    JavaFieldInfo(final Field field, final JavaClassInfo declaringType) {
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
        int hash = field.hashCode();
        WeakReference fieldInfoRef = (WeakReference)s_cache.get(hash);
        JavaFieldInfo fieldInfo = ((fieldInfoRef == null) ? null : (JavaFieldInfo)fieldInfoRef.get());
        if ((fieldInfoRef == null) || (fieldInfo == null)) { //  declaring class is not loaded yet; load it and retry
            new JavaClassInfo(field.getDeclaringClass());
            fieldInfo = (JavaFieldInfo)((WeakReference)s_cache.get(hash)).get();
        }
        return fieldInfo;
    }

    /**
     * Returns the annotations.
     *
     * @return the annotations
     */
    public List getAnnotations() {
        if (m_annotations == null) {
            m_annotations = Annotations.getAnnotationInfos((Field)m_member);
        }
        return m_annotations;
    }

    /**
     * Adds the field info to the cache.
     *
     * @param field     the field
     * @param fieldInfo the field info
     */
    public static void addFieldInfo(final Field field, final JavaFieldInfo fieldInfo) {
        s_cache.put(field.hashCode(), new WeakReference(fieldInfo));
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
        if (!(o instanceof FieldInfo)) {
            return false;
        }
        FieldInfo fieldInfo = (FieldInfo)o;
        if (!m_declaringType.getName().toString().equals(fieldInfo.getDeclaringType().getName().toString())) {
            return false;
        }
        if (!m_member.getName().toString().equals(fieldInfo.getName().toString())) {
            return false;
        }
        ClassInfo fieldType = fieldInfo.getType();
        if (!m_type.getName().toString().equals(fieldType.getName().toString())) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        int result = 29;
        if (m_type == null) {
            getType();
        }
        result = (29 * result) + m_declaringType.getName().toString().hashCode();
        result = (29 * result) + m_member.getName().toString().hashCode();
        result = (29 * result) + m_type.getName().toString().hashCode();
        return result;
    }
}
