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

import org.objectweb.asm.Type;

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
            m_type = AsmClassInfo.createClassInfoFromStream(m_typeName, (ClassLoader) m_loaderRef.get());

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
        FieldInfo fieldInfo = (FieldInfo) o;
        if (!m_declaringTypeName.equals(fieldInfo.getDeclaringType().getName().toString())) {
            return false;
        }
        if (!m_member.name.equals(fieldInfo.getName().toString())) {
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
        result = (29 * result) + m_declaringTypeName.hashCode();
        result = (29 * result) + m_member.name.toString().hashCode();
        result = (29 * result) + getType().getName().toString().hashCode();
        return result;
    }
}