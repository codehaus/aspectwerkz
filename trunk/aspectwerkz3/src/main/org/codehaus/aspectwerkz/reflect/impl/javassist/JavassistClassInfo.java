/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.reflect.impl.javassist;

import gnu.trove.TIntObjectHashMap;
import org.codehaus.aspectwerkz.annotation.AnnotationInfo;
import org.codehaus.aspectwerkz.annotation.instrumentation.AttributeExtractor;
import org.codehaus.aspectwerkz.annotation.instrumentation.Attributes;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.ConstructorInfo;
import org.codehaus.aspectwerkz.reflect.FieldInfo;
import org.codehaus.aspectwerkz.reflect.MethodInfo;

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
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur </a>
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
    private JavassistClassInfoRepository m_classInfoRepository;

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
    JavassistClassInfo(final CtClass klass, final ClassLoader loader) {
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
        m_classInfoRepository = JavassistClassInfoRepository.getRepository(loader);
        if (klass.isPrimitive()) {
            m_name = klass.getName();
            m_isPrimitive = true;
        } else if (klass.isArray()) {
            m_name = klass.getName();
            m_isArray = true;
            m_interfaces = new ClassInfo[0];
        } else {
            m_name = klass.getName();
            CtMethod[] methods = m_class.getDeclaredMethods();
            for (int i = 0; i < methods.length; i++) {
                m_methods.put(JavassistMethodInfo.calculateHash(methods[i]), new JavassistMethodInfo(
                    methods[i],
                    this,
                    loader,
                    m_attributeExtractor));
            }
            CtConstructor[] constructors = m_class.getDeclaredConstructors();
            for (int i = 0; i < constructors.length; i++) {
                CtConstructor constructor = constructors[i];
                m_constructors.put(JavassistConstructorInfo.calculateHash(constructor), new JavassistConstructorInfo(
                    constructor,
                    this,
                    loader,
                    m_attributeExtractor));
            }
            if (m_class.getClassInitializer() != null) {
                CtConstructor constructor = m_class.getClassInitializer();
                m_constructors.put(JavassistConstructorInfo.calculateHash(constructor), new JavassistConstructorInfo(
                    constructor,
                    this,
                    loader,
                    m_attributeExtractor));
            }
            CtField[] fields = m_class.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                CtField field = fields[i];
                m_fields.put(JavassistFieldInfo.calculateHash(field), new JavassistFieldInfo(
                    field,
                    this,
                    loader,
                    m_attributeExtractor));
            }
        }
        addAnnotations();
        m_classInfoRepository.addClassInfo(this);
    }

    /**
     * Returns the class info for a specific ctClass.
     * 
     * @param clazz
     * @param loader
     * @return the class info
     */
    public static ClassInfo getClassInfo(final CtClass clazz, final ClassLoader loader) {
        JavassistClassInfoRepository repository = JavassistClassInfoRepository.getRepository(loader);
        ClassInfo classInfo = repository.getClassInfo(clazz.getName());
        if (classInfo == null) {
            classInfo = new JavassistClassInfo(clazz, loader);
        }
        return classInfo;
    }
    
    /**
     * Marks the class as dirty (since it has been modified and needs to be rebuild).
     *  
     * @param clazz
     * @param loader
     */
    public static void markDirty(final CtClass clazz, final ClassLoader loader) {
        JavassistClassInfoRepository.getRepository(loader).removeClassInfo(clazz);
    }

    /**
     * Returns the annotations.
     * 
     * @return the annotations
     */
    public List getAnnotations() {
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
        return (ConstructorInfo) m_constructors.get(hash);
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
        return (MethodInfo) m_methods.get(hash);
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
        return (FieldInfo) m_fields.get(hash);
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
            try {
                CtClass[] interfaces = m_class.getInterfaces();
                m_interfaces = new ClassInfo[interfaces.length];
                for (int i = 0; i < interfaces.length; i++) {
                    CtClass anInterface = interfaces[i];
                    ClassInfo classInfo = JavassistClassInfo.getClassInfo(anInterface, (ClassLoader) m_loaderRef.get());
                    m_interfaces[i] = classInfo;
                    if (!m_classInfoRepository.hasClassInfo(anInterface.getName())) {
                        m_classInfoRepository.addClassInfo(classInfo);
                    }
                }
            } catch (NotFoundException e) {
                // swallow, since ok
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
                        m_superClass = JavassistClassInfo.getClassInfo(superclass, (ClassLoader) m_loaderRef.get());
                        m_classInfoRepository.addClassInfo(m_superClass);
                    }
                }
            } catch (NotFoundException e) {
                // swallow, since ok
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
        ClassInfo classInfo = (ClassInfo) o;
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