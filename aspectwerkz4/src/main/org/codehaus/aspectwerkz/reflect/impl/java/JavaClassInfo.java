/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.reflect.impl.java;

import gnu.trove.TIntObjectHashMap;
import org.codehaus.aspectwerkz.annotation.Annotations;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.ConstructorInfo;
import org.codehaus.aspectwerkz.reflect.FieldInfo;
import org.codehaus.aspectwerkz.reflect.MethodInfo;
import org.codehaus.aspectwerkz.reflect.ReflectHelper;
import org.codehaus.aspectwerkz.reflect.ReflectHelper;
import org.codehaus.aspectwerkz.transform.TransformationConstants;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Implementation of the ClassInfo interface for java.lang.reflect.*.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r </a>
 */
public class JavaClassInfo implements ClassInfo {
    /**
     * The class.
     */
    // TODO might be safer to wrap this member in a weak ref
    private final Class m_class;

    /**
     * The name of the class.
     */
    private String m_name;

    /**
     * The signature of the class.
     */
    private String m_signature;

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
     * A list with the <code>ConstructorInfo</code> instances.
     */
    private final TIntObjectHashMap m_constructors = new TIntObjectHashMap();

    /**
     * A list with the <code>MethodInfo</code> instances.
     */
    private final TIntObjectHashMap m_methods = new TIntObjectHashMap();

    /**
     * A list with the <code>FieldInfo</code> instances.
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
    private final JavaClassInfoRepository m_classInfoRepository;

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

        m_signature = ReflectHelper.getClassSignature(klass);

        m_classInfoRepository = JavaClassInfoRepository.getRepository(klass.getClassLoader());
        m_isInterface = klass.isInterface();
        if (klass.isPrimitive()) {
            m_name = klass.getName();
            m_isPrimitive = true;
        } else if (klass.getComponentType() != null) {
            m_name = convertJavaArrayTypeNameToHumanTypeName(klass.getName());
            m_isArray = true;
            m_interfaces = new ClassInfo[0];
        } else {
            m_name = klass.getName();
            Method[] methods = m_class.getDeclaredMethods();
            for (int i = 0; i < methods.length; i++) {
                Method method = methods[i];
                m_methods.put(ReflectHelper.calculateHash(method), new JavaMethodInfo(method, this));
            }
            Constructor[] constructors = m_class.getDeclaredConstructors();
            for (int i = 0; i < constructors.length; i++) {
                Constructor constructor = constructors[i];
                m_constructors.put(
                        ReflectHelper.calculateHash(constructor), new JavaConstructorInfo(
                                constructor,
                                this
                        )
                );
            }
            Field[] fields = m_class.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                if (fields[i].getName().startsWith(TransformationConstants.ASPECTWERKZ_PREFIX)) {
                    continue;
                }
                Field field = fields[i];
                m_fields.put(ReflectHelper.calculateHash(field), new JavaFieldInfo(field, this));
            }
        }
        m_classInfoRepository.addClassInfo(this);
    }

    /**
     * Returns the class info for a specific class.
     *
     * @return the class info
     */
    public static ClassInfo getClassInfo(final Class clazz) {
        JavaClassInfoRepository repository = JavaClassInfoRepository.getRepository(clazz.getClassLoader());
        ClassInfo classInfo = repository.getClassInfo(clazz.getName());
        if (classInfo == null) {
            classInfo = new JavaClassInfo(clazz);
        }
        return classInfo;
    }

    /**
     * Returns the annotations infos.
     *
     * @return the annotations infos
     */
    public List getAnnotations() {
        if (m_annotations == null) {
            // TODO this means that JavaClassInfo is always using AsmClassInfo to get that annotations
            // TODO should optimize for Java5
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
        return m_name.replace('/', '.');
    }

    /**
     * Checks if the class has a static initalizer.
     *
     * @return
     */
    public boolean hasStaticInitializer() {
        throw new UnsupportedOperationException("FIXME: hasStaticInitializer() not implemented yet");
    }

    /**
     * Returns the signature for the element.
     *
     * @return the signature for the element
     */
    public String getSignature() {
        return m_signature;
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
     * Returns the class loader that loaded this class.
     *
     * @return the class loader
     */
    public ClassLoader getClassLoader() {
        return m_class.getClassLoader();
    }

    /**
     * Returns a constructor info by its hash.
     *
     * @param hash
     * @return
     */
    public ConstructorInfo getConstructor(final int hash) {
        ConstructorInfo constructor = (ConstructorInfo) m_constructors.get(hash);
        if (constructor == null && getSuperclass() != null) {
            constructor = getSuperclass().getConstructor(hash);
        }
        return constructor;
    }

    /**
     * Returns a list with all the constructors info.
     *
     * @return the constructors info
     */
    public ConstructorInfo[] getConstructors() {
        Object[] values = m_constructors.getValues();
        ConstructorInfo[] methodInfos = new ConstructorInfo[values.length];
        for (int i = 0; i < values.length; i++) {
            methodInfos[i] = (ConstructorInfo) values[i];
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
        MethodInfo method = (MethodInfo) m_methods.get(hash);
        if (method == null) {
            for (int i = 0; i < getInterfaces().length; i++) {
                method = getInterfaces()[i].getMethod(hash);
                if (method != null) {
                    break;
                }
            }
        }
        if (method == null && getSuperclass() != null) {
            method = getSuperclass().getMethod(hash);
        }
        return method;
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
            methodInfos[i] = (MethodInfo) values[i];
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
        FieldInfo field = (FieldInfo) m_fields.get(hash);
        if (field == null && getSuperclass() != null) {
            field = getSuperclass().getField(hash);
        }
        return field;
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
            fieldInfos[i] = (FieldInfo) values[i];
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
    public ClassInfo getSuperclass() {
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
    public static String convertJavaArrayTypeNameToHumanTypeName(final String typeName) {
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
        ClassInfo classInfo = (ClassInfo) o;
        return m_class.getName().toString().equals(classInfo.getName().toString());
    }

    public int hashCode() {
        return m_class.getName().toString().hashCode();
    }

    public String toString() {
        return getName();
    }
}