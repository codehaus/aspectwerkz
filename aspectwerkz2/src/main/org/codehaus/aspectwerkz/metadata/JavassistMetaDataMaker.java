/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.metadata;

import java.util.List;
import java.util.ArrayList;

import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtField;
import javassist.NotFoundException;

/**
 * Convenience methods to construct <code>MetaData</code> instances from Javassist classes.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @author <a href="mailto:vta@medios.fi">Tibor Varga</a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class JavassistMetaDataMaker extends MetaDataMaker {

    /**
     * Construct class meta-data from a Javassist <code>JavaClass</code> object.
     *
     * @param javaClass is the <code>JavaClass</code> object to extract details from.
     * @return a <code>ClassMetaData</code> instance.
     */
    public static ClassMetaData createClassMetaData(final CtClass javaClass) {
        if (javaClass == null) throw new IllegalArgumentException("class can not be null");

        if (s_classMetaDataCache.containsKey(javaClass.getName())) {
            return (ClassMetaData)s_classMetaDataCache.get(javaClass.getName());
        }

        ClassMetaData classMetaData = new ClassMetaData();
        classMetaData.setName(javaClass.getName());
        classMetaData.setModifiers(javaClass.getModifiers());

        // methods
        List methodList = new ArrayList();
        CtMethod[] methods = javaClass.getDeclaredMethods(); //TODO declared method ?
        for (int i = 0; i < methods.length; i++) {
            CtMethod method = methods[i];
            methodList.add(createMethodMetaData(method));
        }
        classMetaData.setMethods(methodList);

        // fields
        List fieldList = new ArrayList();
        CtField[] fields = javaClass.getFields();
        for (int i = 0; i < fields.length; i++) {
            CtField field = fields[i];
            fieldList.add(createFieldMetaData(field));
        }
        classMetaData.setFields(fieldList);

        try {
            // interfaces
            List interfaceList = new ArrayList();
            CtClass[] interfaces = javaClass.getInterfaces();
            for (int i = 0; i < interfaces.length; i++) {
                CtClass anInterface = interfaces[i];
                interfaceList.add(createInterfaceMetaData(anInterface));
            }
            classMetaData.setInterfaces(interfaceList);

            // super class
            CtClass superClass = javaClass.getSuperclass();
            if (superClass != null) { // has super class?
                ClassMetaData superClassMetaData = createClassMetaData(superClass);
                classMetaData.setSuperClass(superClassMetaData);
            }
        }
        catch (NotFoundException e) {
            throw new WrappedRuntimeException(e);
        }

        synchronized (s_classMetaDataCache) {
            s_classMetaDataCache.put(classMetaData.getName(), classMetaData);
        }
        return classMetaData;
    }

    /**
     * Construct interface meta-data from a Javassist <code>JavaClass</code> object.
     *
     * @param javaClass is the <code>JavaClass</code> object to extract details from.
     * @return a <code>InterfaceMetaData</code> instance.
     */
    private static InterfaceMetaData createInterfaceMetaData(final CtClass javaClass) {
        if (javaClass == null) throw new IllegalArgumentException("class can not be null");

        if (s_interfaceMetaDataCache.containsKey(javaClass.getName())) {
            return (InterfaceMetaData)s_interfaceMetaDataCache.get(javaClass.getName());
        }

        InterfaceMetaData interfaceMetaData = new InterfaceMetaData();
        interfaceMetaData.setName(javaClass.getName());

        try {
            List interfaceList = new ArrayList();
            CtClass[] interfaces = javaClass.getInterfaces();
            for (int i = 0; i < interfaces.length; i++) {
                CtClass anInterface = interfaces[i];
                interfaceList.add(createInterfaceMetaData(anInterface));
            }
            interfaceMetaData.setInterfaces(interfaceList);
        }
        catch (NotFoundException e) {
            throw new WrappedRuntimeException(e);
        }

        synchronized (s_interfaceMetaDataCache) {
            s_interfaceMetaDataCache.put(interfaceMetaData.getName(), interfaceMetaData);
        }
        return interfaceMetaData;
    }

    /**
     * Construct method meta-data from a Javassist <code>Method</code> object.
     *
     * @param method is the <code>Method</code> object to extract details from.
     * @return a <code>MethodMetaData</code> instance.
     */
    public static MethodMetaData createMethodMetaData(final CtMethod method) {
        if (method == null) throw new IllegalArgumentException("method can not be null");

        try {
            MethodMetaData methodMetaData = new MethodMetaData();
            methodMetaData.setName(method.getName());

            // return type
            methodMetaData.setReturnType(method.getReturnType().getName());

            // parameters
            CtClass[] javaParameters = method.getParameterTypes();
            String[] parameterTypes = new String[javaParameters.length];
            for (int j = 0; j < javaParameters.length; j++) {
                parameterTypes[j] = javaParameters[j].getName();
            }
            methodMetaData.setParameterTypes(parameterTypes);

            // exceptions
            CtClass[] exceptionTables = method.getExceptionTypes();
            String[] exceptions = new String[exceptionTables.length];
            for (int k = 0; k < exceptionTables.length; k++) {
                exceptions[k] = exceptionTables[k].getName();
            }
            methodMetaData.setExceptionTypes(exceptions);

            //Javassist modifier is the same as java modifier used in ReflectionMetaDataMaker
            methodMetaData.setModifiers(method.getModifiers());

            return methodMetaData;
        }
        catch (NotFoundException e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Construct method meta-data from a Java <code>InvokeInstruction</code> object.
     *
     * @param instruction is the method invocation object to extract details from.
     * @param cpg is the constant pool generator.
     * @return a <code>MethodMetaData</code> instance.
     */
//    public static MethodMetaData createMethodMetaData(final InvokeInstruction instruction,
//                                                      final ConstantPoolGen cpg) {
//        if (instruction == null) throw new IllegalArgumentException("instruction can not be null");
//        if (cpg == null) throw new IllegalArgumentException("constant pool can not be null");
//
//        MethodMetaData methodMetaData = new MethodMetaData();
//
//        String signature = instruction.getSignature(cpg);
//        methodMetaData.setName(instruction.getName(cpg));
//
//        Type[] parameterTypes = Type.getArgumentTypes(signature);
//        String[] parameterTypeNames = new String[parameterTypes.length];
//
//        for (int j = 0; j < parameterTypes.length; j++) {
//            parameterTypeNames[j] = parameterTypes[j].toString();
//        }
//        methodMetaData.setParameterTypes(parameterTypeNames);
//        methodMetaData.setReturnType(Type.getReturnType(signature).toString());
//
//        return methodMetaData;
//    }

    /**
     * Construct field meta-data from a Javassist <code>Field</code> object.
     *
     * @param field is the <code>Field</code> object to extract details from.
     * @return a <code>FieldMetaData</code> instance.
     */
    public static FieldMetaData createFieldMetaData(final CtField field) {
        if (field == null) throw new IllegalArgumentException("field can not be null");

        try {
            FieldMetaData fieldMetaData = new FieldMetaData();
            fieldMetaData.setName(field.getName());
            fieldMetaData.setType(field.getType().getName());
            fieldMetaData.setModifiers(field.getModifiers());
            return fieldMetaData;
        }
        catch (NotFoundException e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Creates a FieldMetaData instance out of the Javassist field access instruction.
     *
     * @param instruction the field instruction
     * @param cpg the constant pool
     * @return the field meta-data
     */
//    public static FieldMetaData createFieldMetaData(final FieldInstruction instruction,
//                                                    final ConstantPoolGen cpg) {
//        if (instruction == null) throw new IllegalArgumentException("instruction can not be null");
//        if (cpg == null) throw new IllegalArgumentException("constant pool can not be null");
//
//        FieldMetaData fieldMetaData = new FieldMetaData();
//        fieldMetaData.setName(instruction.getFieldName(cpg));
//        fieldMetaData.setType(instruction.getFieldType(cpg).toString());
//        return fieldMetaData;
//    }

}
