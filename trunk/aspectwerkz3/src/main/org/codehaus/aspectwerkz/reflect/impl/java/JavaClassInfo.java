/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.reflect.impl.java;

import org.codehaus.aspectwerkz.annotation.Annotations;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.ClassInfoRepository;
import org.codehaus.aspectwerkz.reflect.ConstructorInfo;
import org.codehaus.aspectwerkz.reflect.FieldInfo;
import org.codehaus.aspectwerkz.reflect.MethodInfo;
import org.codehaus.aspectwerkz.transform.TransformationUtil;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import gnu.trove.TIntObjectHashMap;

/**
 * Implementation of the ClassInfo interface for java.lang.reflect.*.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
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
    private final TIntObjectHashMap m_constructors = new TIntObjectHashMap();

    /**
     * A list with the <code>MethodInfo</code> instances.
     */
    private final TIntObjectHashMap m_methods = new TIntObjectHashMap();

    /**
     * A list with the <code>FieldMetaData</code> instances.
     */
    private final TIntObjectHashMap m_fields = new TIntObjectHashMap();

    /**
     * A list with the interfaces.
     */
    private ClassInfo[] m_interfaces = null;

    /**
     * The super class.
     */
    private ClassInfo m_superClass = null;

    /**
     * The annotations.
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
     * Returns the class info for a specific class.
     *
     * @return the class info
     */
    public static ClassInfo getClassInfo(final Class clazz) {
        ClassInfoRepository repository = ClassInfoRepository.getRepository(clazz.getClassLoader());
        ClassInfo classInfo = repository.getClassInfo(clazz.getName());
        if (classInfo == null) {
            classInfo = new JavaClassInfo(clazz);
        }
        return classInfo;
    }

    /**
     * Creates a new class meta data instance.
     *
     * @param klass
     */
    JavaClassInfo(final Class klass) {
        if (klass == null) {
            throw new IllegalArgumentException("class can not be null");
        }
        m_class = klass;
        m_classInfoRepository = ClassInfoRepository.getRepository(klass.getClassLoader());
        m_isInterface = klass.isInterface();
        if (klass.isPrimitive()) {
            m_name = klass.getName();
            m_isPrimitive = true;
        } else if (klass.getComponentType() != null) {
            m_name = convertArrayTypeName(klass.getName());
            m_isArray = true;
            m_interfaces = new ClassInfo[0];
        } else {
            m_name = klass.getName();
            Method[] methods = m_class.getDeclaredMethods();
            for (int i = 0; i < methods.length; i++) {
                Method method = methods[i];
                m_methods.put(JavaMethodInfo.calculateHash(method), new JavaMethodInfo(method, this));
            }
            Constructor[] constructors = m_class.getDeclaredConstructors();
            for (int i = 0; i < constructors.length; i++) {
                Constructor constructor = constructors[i];
                m_constructors.put(JavaConstructorInfo.calculateHash(constructor), new JavaConstructorInfo(constructor, this));
            }
            Field[] fields = m_class.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                if (fields[i].getName().startsWith(TransformationUtil.ASPECTWERKZ_PREFIX)) {
                    continue;
                }
                Field field = fields[i];
                m_fields.put(JavaFieldInfo.calculateHash(field), new JavaFieldInfo(field, this));
            }
        }
        m_classInfoRepository.addClassInfo(this);
    }

    /**
     * Returns the annotations infos.
     *
     * @return the annotations infos
     */
    public List getAnnotations() {
        if (m_annotations == null) {
            m_annotations = Annotations.getAnnotationInfos(m_class);
        }
        return m_annotations;
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
     * Returns a constructor info by its hash.
     *
     * @param hash
     * @return
     */
    public ConstructorInfo getConstructor(final int hash) {
        return (ConstructorInfo)m_constructors.get(hash);
    }

    /**
     * Returns a list with all the constructors info.
     *
     * @return the constructors info
     */
    public ConstructorInfo[] getConstructors() {
        Object[] values = m_methods.getValues();
        ConstructorInfo[] methodInfos = new ConstructorInfo[values.length];
        for (int i = 0; i < values.length; i++) {
            methodInfos[i] = (ConstructorInfo)values[i];

        }
        return methodInfos;
    }

    /**
     * Returns a method info by its hash.
     *
     * @param hash
     * @return
     */
    public MethodInfo getMethod(final int hash) {
        return (MethodInfo)m_methods.get(hash);
    }

    /**
     * Returns a list with all the methods info.
     *
     * @return the methods info
     */
    public MethodInfo[] getMethods() {
        Object[] values = m_methods.getValues();
        MethodInfo[] methodInfos = new MethodInfo[values.length];
        for (int i = 0; i < values.length; i++) {
            methodInfos[i] = (MethodInfo)values[i];

        }
        return methodInfos;
    }

    /**
     * Returns a field info by its hash.
     *
     * @param hash
     * @return
     */
    public FieldInfo getField(final int hash) {
        return (FieldInfo)m_fields.get(hash);
    }

    /**
     * Returns a list with all the field info.
     *
     * @return the field info
     */
    public FieldInfo[] getFields() {
        Object[] values = m_fields.getValues();
        FieldInfo[] fieldInfos = new FieldInfo[values.length];
        for (int i = 0; i < values.length; i++) {
            fieldInfos[i] = (FieldInfo)values[i];

        }
        return fieldInfos;
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
                ClassInfo classInfo = JavaClassInfo.getClassInfo(anInterface);
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
                    m_superClass = JavaClassInfo.getClassInfo(superclass);
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
                m_componentType = JavaClassInfo.getClassInfo(componentType);
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
}
