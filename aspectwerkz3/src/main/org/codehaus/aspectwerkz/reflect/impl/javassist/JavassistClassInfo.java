/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.reflect.impl.javassist;

import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.ClassInfoRepository;
import org.codehaus.aspectwerkz.reflect.ConstructorInfo;
import org.codehaus.aspectwerkz.reflect.FieldInfo;
import org.codehaus.aspectwerkz.reflect.MethodInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;

/**
 * Implementation of the ClassInfo interface for Javassist.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class JavassistClassInfo implements ClassInfo {
    /**
     * The class.
     */
    private final CtClass m_class;

    /**
     * The name of the class.
     */
    private String m_name;

    /**
     * Is the class a primitive type.
     */
    private boolean m_isPrimitive = false;

    /**
     * Is the class an interface.
     */
    private boolean m_isInterface = false;

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
    private final List m_attributes = new ArrayList();

    /**
     * The component type if array type.
     */
    private ClassInfo m_componentType = null;

    /**
     * The class info repository.
     */
    private final ClassInfoRepository m_classInfoRepository;

    /**
     * The class loader that loaded the class.
     */
    private final ClassLoader m_loader;

    /**
     * Creates a new class meta data instance.
     *
     * @param klass
     * @param loader
     */
    public JavassistClassInfo(final CtClass klass, final ClassLoader loader) {
        if (klass == null) {
            throw new IllegalArgumentException("class can not be null");
        }
        if (loader == null) {
            throw new IllegalArgumentException("class loader can not be null");
        }
        m_class = klass;
        m_loader = loader;
        m_classInfoRepository = ClassInfoRepository.getRepository(m_loader);
        m_isInterface = klass.isInterface();

        if (klass.isPrimitive()) {
            m_name = klass.getName();
            m_isPrimitive = true;
        } else if (klass.isArray()) {
            m_name = klass.getName();
            m_isArray = true;
            m_methods = new MethodInfo[0];
            m_constructors = new ConstructorInfo[0];
            m_fields = new FieldInfo[0];
            m_interfaces = new ClassInfo[0];
        } else {
            m_name = klass.getName();

            CtMethod[] methods = m_class.getDeclaredMethods();

            m_methods = new MethodInfo[methods.length];

            for (int i = 0; i < methods.length; i++) {
                m_methods[i] = new JavassistMethodInfo(methods[i], this, m_loader);
            }

            CtConstructor[] constructors = m_class.getDeclaredConstructors();

            m_constructors = new ConstructorInfo[constructors.length];

            for (int i = 0; i < constructors.length; i++) {
                m_constructors[i] = new JavassistConstructorInfo(constructors[i], this, m_loader);
            }

            CtField[] fields = m_class.getDeclaredFields();

            m_fields = new FieldInfo[fields.length];

            for (int i = 0; i < fields.length; i++) {
                m_fields[i] = new JavassistFieldInfo(fields[i], this, m_loader);
            }
        }

        m_classInfoRepository.addClassInfo(this);
    }

    /**
     * Returns the attributes.
     *
     * @return the attributes
     */
    public List getAttributes() {
        return m_attributes;
    }

    /**
     * Adds an attribute.
     *
     * @param attribute the attribute
     */
    public void addAttribute(final Object attribute) {
        m_attributes.add(attribute);
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
            try {
                CtClass[] interfaces = m_class.getInterfaces();

                m_interfaces = new ClassInfo[interfaces.length];

                for (int i = 0; i < interfaces.length; i++) {
                    CtClass anInterface = interfaces[i];
                    ClassInfo classInfo = new JavassistClassInfo(anInterface, m_loader);

                    m_interfaces[i] = classInfo;

                    if (!m_classInfoRepository.hasClassInfo(anInterface.getName())) {
                        m_classInfoRepository.addClassInfo(classInfo);
                    }
                }
            } catch (NotFoundException e) {
                e.printStackTrace();
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
            try {
                CtClass superclass = m_class.getSuperclass();

                if (superclass != null) {
                    if (m_classInfoRepository.hasClassInfo(superclass.getName())) {
                        m_superClass = m_classInfoRepository.getClassInfo(superclass.getName());
                    } else {
                        m_superClass = new JavassistClassInfo(superclass, m_loader);
                        m_classInfoRepository.addClassInfo(m_superClass);
                    }
                }
            } catch (NotFoundException e) {
                e.printStackTrace();
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
            // TODO: how to impl. array component types? Is it needed?
            //            Class componentType = m_class.getComponentType();
            //            if (m_classInfoRepository.hasClassInfo(componentType.getName())) {
            //                m_componentType = m_classInfoRepository.getClassInfo(componentType.getName());
            //            }
            //            else {
            //                m_componentType = new JavassistClassInfo(componentType);
            //                m_classInfoRepository.addClassInfo(m_componentType);
            //            }
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

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof JavassistClassInfo)) {
            return false;
        }

        final JavassistClassInfo javassistClassInfo = (JavassistClassInfo)o;

        if (m_isArray != javassistClassInfo.m_isArray) {
            return false;
        }

        if (m_isPrimitive != javassistClassInfo.m_isPrimitive) {
            return false;
        }

        if (m_isInterface != javassistClassInfo.m_isInterface) {
            return false;
        }

        if ((m_attributes != null) ? (!m_attributes.equals(javassistClassInfo.m_attributes))
                                   : (javassistClassInfo.m_attributes != null)) {
            return false;
        }

        if ((m_class != null) ? (!m_class.equals(javassistClassInfo.m_class)) : (javassistClassInfo.m_class != null)) {
            return false;
        }

        if ((m_classInfoRepository != null) ? (!m_classInfoRepository.equals(javassistClassInfo.m_classInfoRepository))
                                            : (javassistClassInfo.m_classInfoRepository != null)) {
            return false;
        }

        if ((m_componentType != null) ? (!m_componentType.equals(javassistClassInfo.m_componentType))
                                      : (javassistClassInfo.m_componentType != null)) {
            return false;
        }

        if (!Arrays.equals(m_constructors, javassistClassInfo.m_constructors)) {
            return false;
        }

        if (!Arrays.equals(m_fields, javassistClassInfo.m_fields)) {
            return false;
        }

        if (!Arrays.equals(m_interfaces, javassistClassInfo.m_interfaces)) {
            return false;
        }

        if ((m_loader != null) ? (!m_loader.equals(javassistClassInfo.m_loader)) : (javassistClassInfo.m_loader != null)) {
            return false;
        }

        if (!Arrays.equals(m_methods, javassistClassInfo.m_methods)) {
            return false;
        }

        if ((m_name != null) ? (!m_name.equals(javassistClassInfo.m_name)) : (javassistClassInfo.m_name != null)) {
            return false;
        }

        if ((m_superClass != null) ? (!m_superClass.equals(javassistClassInfo.m_superClass))
                                   : (javassistClassInfo.m_superClass != null)) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result;

        result = ((m_class != null) ? m_class.hashCode() : 0);
        result = (29 * result) + ((m_name != null) ? m_name.hashCode() : 0);
        result = (29 * result) + (m_isPrimitive ? 1 : 0);
        result = (29 * result) + (m_isArray ? 1 : 0);
        result = (29 * result) + (m_isInterface ? 1 : 0);
        result = (29 * result) + ((m_superClass != null) ? m_superClass.hashCode() : 0);
        result = (29 * result) + ((m_attributes != null) ? m_attributes.hashCode() : 0);
        result = (29 * result) + ((m_componentType != null) ? m_componentType.hashCode() : 0);
        result = (29 * result) + ((m_classInfoRepository != null) ? m_classInfoRepository.hashCode() : 0);
        result = (29 * result) + ((m_loader != null) ? m_loader.hashCode() : 0);

        return result;
    }
}
