/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.metadata;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

/**
 * Convenience methods to construct <code>MetaData</code> instances out of Java's
 * reflection package's classes.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @author <a href="mailto:vta@medios.fi">Tibor Varga</a>
 */
public class ReflectionMetaDataMaker extends MetaDataMaker {

    /**
     * Construct class meta-data from a <code>Class</code> object.
     *
     * @param klass is the class.
     * @return a <code>ClassMetaData</code> instance.
     */
    public static ClassMetaData createClassMetaData(final Class klass) {
        if (klass == null) throw new IllegalArgumentException("class can not be null");

        if (s_classMetaDataCache.containsKey(klass.getName())) {
            return (ClassMetaData)s_classMetaDataCache.get(klass.getName());
        }
        ClassMetaData classMetaData = new ClassMetaData();
        classMetaData.setName(klass.getName());
        classMetaData.setModifiers(klass.getModifiers());

        // methods
        List methodList = new ArrayList();
        // get the methods for the class only (not the super class' methods)
        Method[] methods = klass.getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            methodList.add(createMethodMetaData(method));
        }
        // get the constructors (<init> methods)
        Constructor[] constructors = klass.getDeclaredConstructors();
        for (int i = 0; i < constructors.length; i++) {
            Constructor constructor = constructors[i];
            methodList.add(createMethodMetaData(constructor));
        }
        classMetaData.setMethods(methodList);

        // fields
        List fieldList = new ArrayList();
        // get the fields for the class only (not the super class' fields)
        Field[] fields = klass.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            fieldList.add(createFieldMetaData(field));
        }
        classMetaData.setFields(fieldList);

        // interfaces
        List interfaceList = new ArrayList();
        Class[] interfaces = klass.getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            Class anInterface = interfaces[i];
            interfaceList.add(createInterfaceMetaData(anInterface));
        }
        classMetaData.setInterfaces(interfaceList);

        // super class
        Class superClass = klass.getSuperclass();
        if (superClass != null) { // has super class?
            ClassMetaData superClassMetaData = createClassMetaData(superClass);
            classMetaData.setSuperClass(superClassMetaData);
        }

        synchronized (s_classMetaDataCache) {
            s_classMetaDataCache.put(classMetaData.getName(), classMetaData);
        }
        return classMetaData;
    }

    /**
     * Construct interface meta-data from a <code>Class</code> object.
     *
     * @param anInterface is the interface's <code>Class</code> to extract details from.
     * @return a <code>InterfaceMetaData</code> instance.
     */
    public static InterfaceMetaData createInterfaceMetaData(final Class anInterface) {
        if (anInterface == null) throw new IllegalArgumentException("interface can not be null");

        if (s_interfaceMetaDataCache.containsKey(anInterface.getName())) {
            return (InterfaceMetaData)s_interfaceMetaDataCache.get(anInterface.getName());
        }

        InterfaceMetaData interfaceMetaData = new InterfaceMetaData();
        interfaceMetaData.setName(anInterface.getName());

        List interfaceList = new ArrayList();
        Class[] interfaces = anInterface.getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            Class klass = interfaces[i];
            interfaceList.add(createInterfaceMetaData(klass));
        }
        interfaceMetaData.setInterfaces(interfaceList);

        synchronized (s_interfaceMetaDataCache) {
            s_interfaceMetaDataCache.put(interfaceMetaData.getName(), interfaceMetaData);
        }
        return interfaceMetaData;
    }

    /**
     * Construct method meta-data from its basic details.
     *
     * @param methodName is the name of the method.
     * @param parameterTypes is the list of parameter types.
     * @param returnType is the return type.
     * @return a <code>MethodMetaData</code> instance.
     */
    public static MethodMetaData createMethodMetaData(final String methodName,
                                                      final Class[] parameterTypes,
                                                      final Class returnType) {
        MethodMetaData data = new MethodMetaData();
        data.setName(methodName);
        data.setParameterTypes(TypeConverter.convertTypeToJava(parameterTypes));
        data.setReturnType(TypeConverter.convertTypeToJava(returnType));
        return data;
    }

    /**
     * Construct method meta-data from a Java <code>Method</code> object.
     *
     * @param method is the <code>Method</code> object to extract details from.
     * @return a <code>MethodMetaData</code> instance.
     */
    public static MethodMetaData createMethodMetaData(final Method method) {
        MethodMetaData data = new MethodMetaData();
        data.setName(method.getName());
        data.setModifiers(method.getModifiers());
        data.setReturnType(TypeConverter.convertTypeToJava(method.getReturnType()));
        data.setParameterTypes(TypeConverter.convertTypeToJava(method.getParameterTypes()));
        data.setExceptionTypes(TypeConverter.convertTypeToJava(method.getExceptionTypes()));
        return data;
    }

    /**
     * Construct method meta-data from a Java <code>&lt;init&gt;</code> object.
     *
     * @param method is the <code>Constructor</code> object to extract details from.
     * @return a <code>MethodMetaData</code> instance.
     */
    public static MethodMetaData createMethodMetaData(final Constructor method) {
        MethodMetaData data = new MethodMetaData();
        data.setName("<init>");
        data.setModifiers(method.getModifiers());
        data.setReturnType("void"); //bcel is using this return type for <init> methods
        data.setParameterTypes(TypeConverter.convertTypeToJava(method.getParameterTypes()));
        data.setExceptionTypes(TypeConverter.convertTypeToJava(method.getExceptionTypes()));
        return data;
    }

    /**
     *
     * Construct field meta-data from its basic details.
     *
     * @param fieldName is the name of the field.
     * @param typeName is the type of the field.
     * @return a <code>FieldMetaData</code> instance.
     */
    public static FieldMetaData createFieldMetaData(final String fieldName,
                                                    final String typeName) {
        FieldMetaData data = new FieldMetaData();
        data.setName(fieldName);
        data.setType(typeName);
        return data;
    }

    /**
     * Construct field meta-data from a <code>Field</code> object.
     *
     * @param field the field
     * @return the field meta-data
     */
    public static FieldMetaData createFieldMetaData(final Field field) {
        final FieldMetaData fieldMetaData = new FieldMetaData();
        fieldMetaData.setName(field.getName());
        fieldMetaData.setModifiers(field.getModifiers());
        fieldMetaData.setType(TypeConverter.convertTypeToJava(field.getType()));
        return fieldMetaData;
    }
}
