/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.reflect.impl.asm;

import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.FieldInfo;
import org.codehaus.aspectwerkz.transform.inlining.AsmHelper;
import org.codehaus.aspectwerkz.annotation.instrumentation.asm.AsmAnnotationHelper;

import org.objectweb.asm.Type;
import org.objectweb.asm.ClassReader;

import java.util.List;
import java.util.ArrayList;
import java.io.IOException;

/**
 * ASM implementation of the FieldInfo interface.
 * 
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public class AsmFieldInfo extends AsmMemberInfo implements FieldInfo {

    /**
     * The field type name.
     */
    private String m_typeName;

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
     */
    AsmFieldInfo(final FieldStruct field, final String declaringType, final ClassLoader loader) {
        super(field, declaringType, loader);
        m_typeName = Type.getType(field.desc).getClassName();
    }

    /**
     * Returns the field info for the field specified.
     * 
     * @param fieldName
     * @param fieldDesc
     * @param bytecode
     * @param loader
     * @return the field info
     */
    public static FieldInfo getFieldInfo(
        final String fieldName,
        final String fieldDesc,
        final byte[] bytecode,
        final ClassLoader loader) {
        String className = AsmClassInfo.retrieveClassNameFromBytecode(bytecode);
        AsmClassInfoRepository repository = AsmClassInfoRepository.getRepository(loader);
        ClassInfo classInfo = repository.getClassInfo(className);
        if (classInfo == null) {
            classInfo = AsmClassInfo.getClassInfo(bytecode, loader);
        }
        return classInfo.getField(AsmHelper.calculateFieldHash(fieldName, fieldDesc));
    }

    /**
     * Returns the type.
     * 
     * @return the type
     */
    public ClassInfo getType() {
        if (m_type == null) {
            m_type = AsmClassInfo.getClassInfo(m_typeName, (ClassLoader) m_loaderRef.get());
        }
        return m_type;
    }

    /**
     * Returns the annotations.
     *
     * @return the annotations
     */
    public List getAnnotations() {
        if (m_annotations == null) {
            try {
                ClassReader cr = new ClassReader(((ClassLoader)m_loaderRef.get()).getResourceAsStream(m_declaringTypeName.replace('.','/')+".class"));
                List annotations = new ArrayList();
                cr.accept(
                        new AsmAnnotationHelper.FieldAnnotationExtractor(annotations, m_member.name, (ClassLoader)m_loaderRef.get()),
                        AsmAnnotationHelper.ANNOTATIONS_ATTRIBUTES,
                        true
                );
                m_annotations = annotations;
            } catch (IOException e) {
                // unlikely to occur since ClassInfo relies on getResourceAsStream
                System.err.println("WARN - could not load " + m_declaringTypeName + " as a resource to retrieve annotations");
                m_annotations = AsmClassInfo.EMPTY_LIST;
            }
        }
        return m_annotations;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FieldInfo)) {
            return false;
        }
        FieldInfo fieldInfo = (FieldInfo) o;
        if (!m_declaringTypeName.equals(fieldInfo.getDeclaringType().getName())) {
            return false;
        }
        if (!m_member.name.equals(fieldInfo.getName())) {
            return false;
        }
        if (!m_typeName.equals(fieldInfo.getType().getName())) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        int result = 29;
        result = (29 * result) + m_declaringTypeName.hashCode();
        result = (29 * result) + m_member.name.hashCode();
        result = (29 * result) + m_typeName.hashCode();
        return result;
    }
}