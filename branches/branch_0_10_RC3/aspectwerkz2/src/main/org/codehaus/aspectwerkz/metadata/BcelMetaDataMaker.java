/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.metadata;

import java.util.ArrayList;
import java.util.List;

import org.apache.bcel.classfile.ExceptionTable;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.FieldInstruction;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.Type;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;

/**
 * Convenience methods to construct <code>MetaDataBase</code> instances from BCEL classes.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @author <a href="mailto:vta@medios.fi">Tibor Varga</a>
 */
public class BcelMetaDataMaker extends MetaDataMaker {

    /**
     * Construct class meta-data from a BCEL <code>JavaClass</code> object.
     *
     * @param javaClass is the <code>JavaClass</code> object to extract details from.
     * @return a <code>ClassMetaData</code> instance.
     */
    public static ClassMetaData createClassMetaData(final JavaClass javaClass) {
        if (javaClass == null) {
            throw new IllegalArgumentException("class can not be null");
        }

        if (s_classMetaDataCache.containsKey(javaClass.getClassName())) {
            return (ClassMetaData)s_classMetaDataCache.get(javaClass.getClassName());
        }

        ClassMetaDataImpl classMetaData = new ClassMetaDataImpl();
        classMetaData.setName(javaClass.getClassName());
        classMetaData.setModifiers(javaClass.getModifiers());

        // methods
        List methodList = new ArrayList();
        Method[] methods = javaClass.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            methodList.add(createMethodMetaData(method));
        }
        classMetaData.setMethods(methodList);

        // fields
        List fieldList = new ArrayList();
        Field[] fields = javaClass.getFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            fieldList.add(createFieldMetaData(field));
        }
        classMetaData.setFields(fieldList);

        try {
            // interfaces
            List interfaceList = new ArrayList();
            JavaClass[] interfaces = javaClass.getInterfaces();
            for (int i = 0; i < interfaces.length; i++) {
                JavaClass anInterface = interfaces[i];
                interfaceList.add(createInterfaceMetaData(anInterface));
            }
            classMetaData.setInterfaces(interfaceList);

            // super class
            JavaClass superClass = javaClass.getSuperClass();
            if (superClass != null) { // has super class?
                ClassMetaData superClassMetaData = createClassMetaData(superClass);
                classMetaData.setSuperClass(superClassMetaData);
            }
        }
        catch (ClassNotFoundException e) {
            throw new WrappedRuntimeException(e);
        }

        synchronized (s_classMetaDataCache) {
            s_classMetaDataCache.put(classMetaData.getName(), classMetaData);
        }
        return classMetaData;
    }

    /**
     * Construct interface meta-data from a BCEL <code>JavaClass</code> object.
     *
     * @param javaClass is the <code>JavaClass</code> object to extract details from.
     * @return a <code>InterfaceMetaData</code> instance.
     */
    private static InterfaceMetaData createInterfaceMetaData(final JavaClass javaClass) {
        if (javaClass == null) {
            throw new IllegalArgumentException("class can not be null");
        }

        if (s_interfaceMetaDataCache.containsKey(javaClass.getClassName())) {
            return (InterfaceMetaData)s_interfaceMetaDataCache.get(javaClass.getClassName());
        }

        InterfaceMetaDataImpl interfaceMetaData = new InterfaceMetaDataImpl();
        interfaceMetaData.setName(javaClass.getClassName());

        try {
            List interfaceList = new ArrayList();
            JavaClass[] interfaces = javaClass.getInterfaces();
            for (int i = 0; i < interfaces.length; i++) {
                JavaClass anInterface = interfaces[i];
                interfaceList.add(createInterfaceMetaData(anInterface));
            }
            interfaceMetaData.setInterfaces(interfaceList);
        }
        catch (ClassNotFoundException e) {
            throw new WrappedRuntimeException(e);
        }

        synchronized (s_interfaceMetaDataCache) {
            s_interfaceMetaDataCache.put(interfaceMetaData.getName(), interfaceMetaData);
        }
        return interfaceMetaData;
    }

    /**
     * Construct method meta-data from a BCEL <code>Method</code> object.
     *
     * @param method is the <code>Method</code> object to extract details from.
     * @return a <code>MethodMetaData</code> instance.
     */
    public static MethodMetaData createMethodMetaData(final Method method) {
        if (method == null) {
            throw new IllegalArgumentException("method can not be null");
        }

        MethodMetaDataImpl methodMetaData = new MethodMetaDataImpl();
        methodMetaData.setName(method.getName());

        // return type
        methodMetaData.setReturnType(method.getReturnType().toString());

        // parameters
        Type[] javaParameters = method.getArgumentTypes();
        String[] parameterTypes = new String[javaParameters.length];
        for (int j = 0; j < javaParameters.length; j++) {
            parameterTypes[j] = javaParameters[j].toString();
        }
        methodMetaData.setParameterTypes(parameterTypes);

        // exceptions
        String[] exceptions;
        ExceptionTable exceptionTable = method.getExceptionTable();
        if (exceptionTable != null) {
            exceptions = exceptionTable.getExceptionNames();
        }
        else {
            exceptions = new String[0];
        }
        methodMetaData.setExceptionTypes(exceptions);

        //BCEL modifier is the same as java modifier used in ReflectionMetaDataMaker
        methodMetaData.setModifiers(method.getModifiers());

        return methodMetaData;
    }

    /**
     * Construct method meta-data from a Java <code>InvokeInstruction</code> object.
     *
     * @param instruction is the method invocation object to extract details from.
     * @param cpg         is the constant pool generator.
     * @return a <code>MethodMetaData</code> instance.
     */
    public static MethodMetaData createMethodMetaData(
            final InvokeInstruction instruction,
            final ConstantPoolGen cpg) {
        if (instruction == null) {
            throw new IllegalArgumentException("instruction can not be null");
        }
        if (cpg == null) {
            throw new IllegalArgumentException("constant pool can not be null");
        }

        MethodMetaDataImpl methodMetaData = new MethodMetaDataImpl();

        String signature = instruction.getSignature(cpg);
        methodMetaData.setName(instruction.getName(cpg));

        Type[] parameterTypes = Type.getArgumentTypes(signature);
        String[] parameterTypeNames = new String[parameterTypes.length];

        for (int j = 0; j < parameterTypes.length; j++) {
            parameterTypeNames[j] = parameterTypes[j].toString();
        }
        methodMetaData.setParameterTypes(parameterTypeNames);
        methodMetaData.setReturnType(Type.getReturnType(signature).toString());

        return methodMetaData;
    }

    /**
     * Construct field meta-data from a BCEL <code>Field</code> object.
     *
     * @param field is the <code>Field</code> object to extract details from.
     * @return a <code>FieldMetaData</code> instance.
     */
    private static FieldMetaData createFieldMetaData(final Field field) {
        if (field == null) {
            throw new IllegalArgumentException("field can not be null");
        }

        FieldMetaDataImpl fieldMetaData = new FieldMetaDataImpl();
        fieldMetaData.setName(field.getName());
        fieldMetaData.setType(field.getType().toString());
        fieldMetaData.setModifiers(field.getModifiers());
        return fieldMetaData;
    }

    /**
     * Creates a FieldMetaData instance out of the BCEL field access instruction.
     *
     * @param instruction the field instruction
     * @param cpg         the constant pool
     * @return the field meta-data
     */
    public static FieldMetaData createFieldMetaData(
            final FieldInstruction instruction,
            final ConstantPoolGen cpg) {
        if (instruction == null) {
            throw new IllegalArgumentException("instruction can not be null");
        }
        if (cpg == null) {
            throw new IllegalArgumentException("constant pool can not be null");
        }

        FieldMetaDataImpl fieldMetaData = new FieldMetaDataImpl();
        fieldMetaData.setName(instruction.getFieldName(cpg));
        fieldMetaData.setType(instruction.getFieldType(cpg).toString());
        return fieldMetaData;
    }

}
