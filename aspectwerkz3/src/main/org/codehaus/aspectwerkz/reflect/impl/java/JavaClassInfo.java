/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.reflect.impl.java;

import org.codehaus.aspectwerkz.definition.attribute.AttributeExtractor;
import org.codehaus.aspectwerkz.definition.attribute.Attributes;
import org.codehaus.aspectwerkz.definition.attribute.CustomAttribute;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.ClassInfoRepository;
import org.codehaus.aspectwerkz.reflect.ConstructorInfo;
import org.codehaus.aspectwerkz.reflect.FieldInfo;
import org.codehaus.aspectwerkz.reflect.MethodInfo;
import org.codehaus.aspectwerkz.transform.TransformationUtil;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the ClassInfo interface for java.lang.reflect.*.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public class JavaClassInfo implements ClassInfo {
    /**
     * The class.
     */
    private final Class m_class;

    /**
     * The name of the class.
     */
    private String m_name;

    /**
     * Is the class an interface.
     */
    private boolean m_isInterface = false;

    /**
     * Is the class a primitive type.
     */
    private boolean m_isPrimitive = false;

    /**
     * Is the class of type array.
     */
    private boolean m_isArray = false;

    /**
     * A list with the <code>ConstructorMetaData</code> instances.
     */
    private ConstructorInfo[] m_constructors = null;

    /**
     * A list with the <code>MethodInfo</code> instances.
     */
    private MethodInfo[] m_methods = null;

    /**
     * A list with the <code>FieldMetaData</code> instances.
     */
    private FieldInfo[] m_fields = null;

    /**
     * A list with the interfaces.
     */
    private ClassInfo[] m_interfaces = null;

    /**
     * The super class.
     */
    private ClassInfo m_superClass = null;

    /**
     * The attributes.
     */
    private List m_annotations = null;

    /**
     * The component type if array type.
     */
    private ClassInfo m_componentType = null;

    /**
     * The class info repository.
     */
    private final ClassInfoRepository m_classInfoRepository;

    /**
     * The annotation extractor.
     */
    private AttributeExtractor m_attributeExtractor = null;

    /**
     * Creates a new class meta data instance.
     *
     * @param klass
     */
    public JavaClassInfo(final Class klass) {
        if (klass == null) {
            throw new IllegalArgumentException("class can not be null");
        }
        m_class = klass;
        m_attributeExtractor = Attributes.getAttributeExtractor(m_class);
        m_classInfoRepository = ClassInfoRepository.getRepository(klass.getClassLoader());
        m_isInterface = klass.isInterface();
        if (klass.isPrimitive()) {
            m_name = klass.getName();
            m_isPrimitive = true;
        } else if (klass.getComponentType() != null) {
            m_name = convertArrayTypeName(klass.getName());
            m_isArray = true;
            m_methods = new MethodInfo[0];
            m_constructors = new ConstructorInfo[0];
            m_fields = new FieldInfo[0];
            m_interfaces = new ClassInfo[0];
        } else {
            m_name = klass.getName();
            Method[] methods = m_class.getDeclaredMethods();
            m_methods = new MethodInfo[methods.length];
            for (int i = 0; i < methods.length; i++) {
                m_methods[i] = new JavaMethodInfo(methods[i], this, m_attributeExtractor);
            }
            Constructor[] constructors = m_class.getDeclaredConstructors();
            m_constructors = new ConstructorInfo[constructors.length];
            for (int i = 0; i < constructors.length; i++) {
                m_constructors[i] = new JavaConstructorInfo(constructors[i], this, m_attributeExtractor);
            }
            Field[] fields = m_class.getDeclaredFields();
            m_fields = new FieldInfo[fields.length];
            for (int i = 0; i < fields.length; i++) {
                if (fields[i].getName().startsWith(TransformationUtil.ASPECTWERKZ_PREFIX)) {
                    continue;
                }
                m_fields[i] = new JavaFieldInfo(fields[i], this, m_attributeExtractor);
            }
        }
        m_classInfoRepository.addClassInfo(this);
    }

    /**
     * Returns the attributes.
     *
     * @return the attributes
     */
    public List getAnnotations() {
        if (m_annotations == null) {
            m_annotations = new ArrayList();
            addAnnotations();
        }
        return m_annotations;
    }

    /**
     * Adds an attribute.
     *
     * @param attribute the attribute
     */
    public void addAnnotation(final Object attribute) {
        m_annotations.add(attribute);
    }

    /**
     * Returns the name of the class.
     *
     * @return the name of the class
     */
    public String getName() {
        return m_name;
    }

    /**
     * Returns the class modifiers.
     *
     * @return the class modifiers
     */
    public int getModifiers() {
        return m_class.getModifiers();
    }

    /**
     * Returns a list with all the constructors info.
     *
     * @return the constructors info
     */
    public ConstructorInfo[] getConstructors() {
        return m_constructors;
    }

    /**
     * Returns a list with all the methods info.
     *
     * @return the methods info
     */
    public MethodInfo[] getMethods() {
        return m_methods;
    }

    /**
     * Returns a list with all the field info.
     *
     * @return the field info
     */
    public FieldInfo[] getFields() {
        return m_fields;
    }

    /**
     * Returns the interfaces.
     *
     * @return the interfaces
     */
    public ClassInfo[] getInterfaces() {
        if (m_interfaces == null) {
            Class[] interfaces = m_class.getInterfaces();
            m_interfaces = new ClassInfo[interfaces.length];
            for (int i = 0; i < interfaces.length; i++) {
                Class anInterface = interfaces[i];
                ClassInfo classInfo = new JavaClassInfo(anInterface);
                m_interfaces[i] = classInfo;
                if (!m_classInfoRepository.hasClassInfo(anInterface.getName())) {
                    m_classInfoRepository.addClassInfo(classInfo);
                }
            }
        }
        return m_interfaces;
    }

    /**
     * Returns the super class.
     *
     * @return the super class
     */
    public ClassInfo getSuperClass() {
        if (m_superClass == null) {
            Class superclass = m_class.getSuperclass();
            if (superclass != null) {
                if (m_classInfoRepository.hasClassInfo(superclass.getName())) {
                    m_superClass = m_classInfoRepository.getClassInfo(superclass.getName());
                } else {
                    m_superClass = new JavaClassInfo(superclass);
                    m_classInfoRepository.addClassInfo(m_superClass);
                }
            }
        }
        return m_superClass;
    }

    /**
     * Returns the component type if array type else null.
     *
     * @return the component type
     */
    public ClassInfo getComponentType() {
        if (isArray() && (m_componentType == null)) {
            Class componentType = m_class.getComponentType();
            if (m_classInfoRepository.hasClassInfo(componentType.getName())) {
                m_componentType = m_classInfoRepository.getClassInfo(componentType.getName());
            } else {
                m_componentType = new JavaClassInfo(componentType);
                m_classInfoRepository.addClassInfo(m_componentType);
            }
        }
        return m_componentType;
    }

    /**
     * Is the class an interface.
     *
     * @return
     */
    public boolean isInterface() {
        return m_isInterface;
    }

    /**
     * Is the class a primitive type.
     *
     * @return
     */
    public boolean isPrimitive() {
        return m_isPrimitive;
    }

    /**
     * Is the class an array type.
     *
     * @return
     */
    public boolean isArray() {
        return m_isArray;
    }

    /**
     * Converts an internal Java array type name ([Lblabla) to the a the format used by the expression matcher
     * (blabla[])
     *
     * @param typeName is type name
     * @return
     */
    private static String convertArrayTypeName(final String typeName) {
        int index = typeName.lastIndexOf('[');
        if (index != -1) {
            StringBuffer arrayType = new StringBuffer();
            if (typeName.endsWith("I")) {
                arrayType.append("int");
            } else if (typeName.endsWith("J")) {
                arrayType.append("long");
            } else if (typeName.endsWith("S")) {
                arrayType.append("short");
            } else if (typeName.endsWith("F")) {
                arrayType.append("float");
            } else if (typeName.endsWith("D")) {
                arrayType.append("double");
            } else if (typeName.endsWith("Z")) {
                arrayType.append("boolean");
            } else if (typeName.endsWith("C")) {
                arrayType.append("char");
            } else if (typeName.endsWith("B")) {
                arrayType.append("byte");
            } else {
                arrayType.append(typeName.substring(index + 2, typeName.length() - 1));
            }
            for (int i = 0; i < (index + 1); i++) {
                arrayType.append("[]");
            }
            return arrayType.toString();
        } else {
            return typeName;
        }
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ClassInfo)) {
            return false;
        }
        ClassInfo classInfo = (ClassInfo)o;
        return m_class.getName().toString().equals(classInfo.getName().toString());
    }

    public int hashCode() {
        return m_class.getName().toString().hashCode();
    }

    /**
     * Adds annotations to the class info.
     */
    private void addAnnotations() {
        if (m_attributeExtractor == null) {
            return;
        }
        Object[] attributes = m_attributeExtractor.getClassAttributes();
        for (int i = 0; i < attributes.length; i++) {
            Object attribute = attributes[i];
            if (attribute instanceof CustomAttribute) {
                CustomAttribute custom = (CustomAttribute)attribute;
                if (custom.getName().startsWith(TransformationUtil.ASPECTWERKZ_PREFIX)) {
                    // skip 'system' annotations
                    continue;
                }
                addAnnotation(custom);
            }
        }
    }
}
