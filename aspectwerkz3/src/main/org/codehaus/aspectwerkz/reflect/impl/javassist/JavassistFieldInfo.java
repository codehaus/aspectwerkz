/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.reflect.impl.javassist;

import org.codehaus.aspectwerkz.annotation.AnnotationInfo;
import org.codehaus.aspectwerkz.annotation.instrumentation.AttributeExtractor;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.FieldInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.lang.ref.WeakReference;

import javassist.CtClass;
import javassist.CtField;
import javassist.NotFoundException;

/**
 * Implementation of the FieldInfo interface for Javassist.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class JavassistFieldInfo extends JavassistMemberInfo implements FieldInfo {
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
     * @param loader
     * @param attributeExtractor
     */
    JavassistFieldInfo(final CtField field, final JavassistClassInfo declaringType, final ClassLoader loader,
                       final AttributeExtractor attributeExtractor) {
        super(field, declaringType, loader, attributeExtractor);
        JavassistFieldInfo.addFieldInfo(field, this);
    }

    /**
     * Returns the field info for the field specified.
     *
     * @param field  the field
     * @param loader the class loader
     * @return the field info
     */
    public static JavassistFieldInfo getFieldInfo(final CtField field, final ClassLoader loader) {
        WeakReference fieldRef = new WeakReference(field);
        JavassistFieldInfo fieldInfo = (JavassistFieldInfo)s_cache.get(fieldRef);
        if (fieldInfo == null) { //  declaring class is not loaded yet; load it and retry
            new JavassistClassInfo(field.getDeclaringClass(), loader);
            fieldInfo = (JavassistFieldInfo)s_cache.get(fieldRef);
        }
        return fieldInfo;
    }

    /**
     * Adds the field info to the cache.
     *
     * @param field     the field
     * @param fieldInfo the field info
     */
    public static void addFieldInfo(final CtField field, final JavassistFieldInfo fieldInfo) {
        s_cache.put(new WeakReference(field), fieldInfo);
    }

    /**
     * Returns the attributes.
     *
     * @return the attributes
     */
    public List getAnnotations() {
        if (m_annotations == null) {
            addAnnotations();
        }
        return m_annotations;
    }

    /**
     * Returns the field type.
     *
     * @return the field type
     */
    public ClassInfo getType() {
        if (m_type == null) {
            try {
                CtClass type = ((CtField)m_member).getType();
                if (m_classInfoRepository.hasClassInfo(type.getName())) {
                    m_type = m_classInfoRepository.getClassInfo(type.getName());
                } else {
                    m_type = new JavassistClassInfo(type, m_loader);
                    m_classInfoRepository.addClassInfo(m_type);
                }
            } catch (NotFoundException e) {
                e.printStackTrace();
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
        if (!m_member.getName().equals(fieldInfo.getName())) {
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

    /**
     * Adds annotations to the field info.
     */
    private void addAnnotations() {
        if (m_attributeExtractor == null) {
            return;
        }
        m_annotations = new ArrayList();
        Object[] attributes = m_attributeExtractor.getFieldAttributes(m_member.getName());
        for (int i = 0; i < attributes.length; i++) {
            Object attribute = attributes[i];
            if (attribute instanceof AnnotationInfo) {
                m_annotations.add(attribute);
            }
        }
    }
}
