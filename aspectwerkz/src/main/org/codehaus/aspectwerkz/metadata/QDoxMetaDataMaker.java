/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.metadata;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.aspectwerkz.transform.TransformationUtil;

import com.thoughtworks.qdox.model.JavaField;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.JavaParameter;
import com.thoughtworks.qdox.model.Type;
import com.thoughtworks.qdox.model.JavaClass;

/**
 * Convenience methods to construct <code>MetaData</code> instances from QDox classes.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @author <a href="mailto:vta@medios.fi">Tibor Varga</a>
 */
public class QDoxMetaDataMaker extends MetaDataMaker {

    /**
     * Construct class meta-data from a BCEL <code>JavaClass</code> object.
     *
     * @param javaClass is the <code>JavaClass</code> object to extract details from.
     * @return a <code>ClassMetaData</code> instance.
     */
    public static ClassMetaData createClassMetaData(final JavaClass javaClass) {
        if (javaClass == null) throw new IllegalArgumentException("class can not be null");

        if (s_classMetaDataCache.containsKey(javaClass.getName())) {
            return (ClassMetaData)s_classMetaDataCache.get(javaClass.getName());
        }

        ClassMetaData classMetaData = new ClassMetaData();
        classMetaData.setName(javaClass.getName());

        // methods
        List methodList = new ArrayList();
        JavaMethod[] methods = javaClass.getMethods();
        for (int i = 0; i < methods.length; i++) {
            JavaMethod method = methods[i];
            methodList.add(createMethodMetaData(method));
        }
        classMetaData.setMethods(methodList);

        // fields
        List fieldList = new ArrayList();
        JavaField[] fields = javaClass.getFields();
        for (int i = 0; i < fields.length; i++) {
            JavaField field = fields[i];
            fieldList.add(createFieldMetaData(field));
        }
        classMetaData.setFields(fieldList);

        // interfaces
        List interfaceList = new ArrayList();
        Type[] interfaces = javaClass.getImplements();
        for (int i = 0; i < interfaces.length; i++) {
            Type anInterface = interfaces[i];
            interfaceList.add(createInterfaceMetaData(anInterface));
        }
        classMetaData.setInterfaces(interfaceList);

        // super class
        JavaClass superClass = javaClass.getSuperJavaClass();
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
     * @todo add the interface's interfaces to the InterfaceMetaData (if needed)
     *
     * @param type is the <code>Type</code> object to extract details from.
     * @return a <code>InterfaceMetaData</code> instance.
     */
    public static InterfaceMetaData createInterfaceMetaData(final Type type) {
        if (type == null) throw new IllegalArgumentException("interface can not be null");

        if (s_interfaceMetaDataCache.containsKey(type.getValue())) {
            return (InterfaceMetaData)s_interfaceMetaDataCache.get(type.getValue());
        }

        InterfaceMetaData interfaceMetaData = new InterfaceMetaData();
        interfaceMetaData.setName(type.getValue());

//        List interfaceList = new ArrayList();
//        Class[] interfaces = klass.getInterfaces();
//        for (int i = 0; i < interfaces.length; i++) {
//            Class anInterface = interfaces[i];
//            interfaceList.add(createInterfaceMetaData(anInterface));
//        }
//        interfaceMetaData.setInterfaces(interfaceList);

        synchronized (s_interfaceMetaDataCache) {
            s_interfaceMetaDataCache.put(interfaceMetaData.getName(), interfaceMetaData);
        }
        return interfaceMetaData;
    }

    /**
     * Construct meta-data from a QDox <code>JavaMethod</code> object.
     *
     * @param method is the <code>JavaMethod</code> object to extract details from.
     * @return a <code>MethodMetaData</code> instance.
     */
    public static MethodMetaData createMethodMetaData(final JavaMethod method) {

        MethodMetaData data = new MethodMetaData();
        data.setName(method.getName());
        data.setModifiers(TransformationUtil.getModifiersAsInt(method.getModifiers()));

        Type returnType = method.getReturns();
        if (returnType != null) {
            data.setReturnType(TypeConverter.convertTypeToJava(returnType));
        }

        JavaParameter[] parameters = method.getParameters();
        String[] parameterTypes = new String[parameters.length];
        for (int j = 0; j < parameters.length; j++) {
            parameterTypes[j] = TypeConverter.convertTypeToJava(parameters[j].getType());
        }
        data.setParameterTypes(parameterTypes);

        Type[] exceptions = method.getExceptions();
        String[] exceptionTypes = new String[exceptions.length];
        for (int j = 0; j < exceptions.length; j++) {
            exceptionTypes[j] = TypeConverter.convertTypeToJava(exceptions[j]);
        }
        data.setExceptionTypes(exceptionTypes);

        return data;
    }

    /**
     * Create a new <code>FieldMetaData</code> based on the QDox
     * <code>JavaField</code passed as parameter.
     *
     * @param field the QDox field
     * @return the field meta-data
     */
    public static FieldMetaData createFieldMetaData(final JavaField field) {
        final FieldMetaData data = new FieldMetaData();
        data.setName(field.getName());
        data.setModifiers(TransformationUtil.getModifiersAsInt(field.getModifiers()));
        data.setType(TypeConverter.convertTypeToJava(field.getType()));
        return data;
    }
}
