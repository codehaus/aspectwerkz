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
import org.codehaus.aspectwerkz.annotation.instrumentation.Attributes;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.ClassInfoRepository;
import org.codehaus.aspectwerkz.reflect.ConstructorInfo;
import org.codehaus.aspectwerkz.reflect.FieldInfo;
import org.codehaus.aspectwerkz.reflect.MethodInfo;
import org.codehaus.aspectwerkz.transform.TransformationUtil;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
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
    private List m_annotations = null;

    /**
     * The component type if array type.
     */
    private ClassInfo m_componentType = null;

    /**
     * The class info repository.
     */
    private ClassInfoRepository m_classInfoRepository;

    /**
     * The class loader that loaded the class.
     */
    private transient final WeakReference m_loaderRef;

    /**
     * The attribute extractor.
     */
    private AttributeExtractor m_attributeExtractor;

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
        m_loaderRef = new WeakReference(loader);
        m_isInterface = klass.isInterface();
        m_attributeExtractor = Attributes.getAttributeExtractor(m_class, loader);
        m_classInfoRepository = ClassInfoRepository.getRepository(loader);
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
                m_methods[i] = new JavassistMethodInfo(methods[i], this, loader, m_attributeExtractor);
            }
            CtConstructor[] constructors = m_class.getDeclaredConstructors();
            m_constructors = new ConstructorInfo[constructors.length];
            for (int i = 0; i < constructors.length; i++) {
                m_constructors[i] = new JavassistConstructorInfo(constructors[i], this, loader, m_attributeExtractor);
            }
            CtField[] fields = m_class.getDeclaredFields();
            m_fields = new FieldInfo[fields.length];
            for (int i = 0; i < fields.length; i++) {
                if (fields[i].getName().startsWith(TransformationUtil.ASPECTWERKZ_PREFIX)) {
                    continue;
                }
                m_fields[i] = new JavassistFieldInfo(fields[i], this, loader, m_attributeExtractor);
            }
        }
        m_classInfoRepository.addClassInfo(this);
    }

    /**
     * Returns the annotations.
     *
     * @return the annotations
     */
    public List getAnnotations() {
        if (m_annotations == null) {
            addAnnotations();
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
                    ClassInfo classInfo = new JavassistClassInfo(anInterface, (ClassLoader)m_loaderRef.get());
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
                        m_superClass = new JavassistClassInfo(superclass, (ClassLoader)m_loaderRef.get());
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
        m_annotations = new ArrayList();
        Object[] attributes = m_attributeExtractor.getClassAttributes();
        for (int i = 0; i < attributes.length; i++) {
            Object attribute = attributes[i];
            if (attribute instanceof AnnotationInfo) {
                m_annotations.add(attribute);
            }
        }
    }
}
