/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.metadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.io.ObjectInputStream;
import java.io.ByteArrayInputStream;

import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.AttributeInfo;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.ConstantAttribute;
import javassist.bytecode.SourceFileAttribute;
import javassist.bytecode.LineNumberAttribute;
import javassist.bytecode.SyntheticAttribute;
import javassist.bytecode.InnerClassesAttribute;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.definition.attribute.CustomAttribute;

/**
 * Convenience methods to construct <code>MetaDataBase</code> instances from Javassist classes.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
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
        if (javaClass == null) {
            throw new IllegalArgumentException("class can not be null");
        }

        if (s_classMetaDataCache.containsKey(javaClass.getName())) {
            return (ClassMetaData)s_classMetaDataCache.get(javaClass.getName());
        }

        ClassMetaDataImpl classMetaData = new ClassMetaDataImpl();
        classMetaData.setName(javaClass.getName());
        classMetaData.setModifiers(javaClass.getModifiers());

        // constructors
        List constructorList = new ArrayList();
        CtConstructor[] constructors = javaClass.getConstructors();
        for (int i = 0; i < constructors.length; i++) {
            CtConstructor constructor = constructors[i];
            constructorList.add(createConstructorMetaData(constructor));
        }
        classMetaData.setConstructors(constructorList);

        // methods
        List methodList = new ArrayList();
        CtMethod[] methods = javaClass.getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            CtMethod method = methods[i];
            methodList.add(createMethodMetaData(method));
        }
        classMetaData.setMethods(methodList);

        // fields
        List fieldList = new ArrayList();
        CtField[] fields = javaClass.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            CtField field = fields[i];
            fieldList.add(createFieldMetaData(field));
        }
        classMetaData.setFields(fieldList);

        // attributes
        // TODO not supported by Javassist ??
//        for (Iterator attrs = javaClass.getClassFile().getAttributes().iterator(); attrs.hasNext();) {
//            addAttribute(classMetaData, ((AttributeInfo)attrs.next()));
//        }

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
            System.err.println("AspectWerkz - <WARN> unable to build metadata for "
                    + javaClass.getName()+" interfaces/superclasses"
            );
            //throw new WrappedRuntimeException(e);
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
    private static InterfaceMetaData createInterfaceMetaData(final CtClass javaClass)
    throws NotFoundException {
        if (javaClass == null) {
            throw new IllegalArgumentException("class can not be null");
        }

        if (s_interfaceMetaDataCache.containsKey(javaClass.getName())) {
            return (InterfaceMetaData)s_interfaceMetaDataCache.get(javaClass.getName());
        }

        InterfaceMetaDataImpl interfaceMetaData = new InterfaceMetaDataImpl();
        interfaceMetaData.setName(javaClass.getName());

        // TODO not supported by Javassist ??
//        // attributes
//        for (Iterator attrs = javaClass.getClassFile().getAttributes().iterator(); attrs.hasNext();) {
//            addAttribute(interfaceMetaData, ((AttributeInfo)attrs.next()));
//        }

        //try {
            List interfaceList = new ArrayList();
            CtClass[] interfaces = javaClass.getInterfaces();
            for (int i = 0; i < interfaces.length; i++) {
                CtClass anInterface = interfaces[i];
                interfaceList.add(createInterfaceMetaData(anInterface));
            }
            interfaceMetaData.setInterfaces(interfaceList);
        //}
        //catch (NotFoundException e) {
        //    throw new WrappedRuntimeException(e);
        //}

        synchronized (s_interfaceMetaDataCache) {
            s_interfaceMetaDataCache.put(interfaceMetaData.getName(), interfaceMetaData);
        }
        return interfaceMetaData;
    }

    /**
     * Construct method meta-data from a Javassist <code>CtMethod</code> object.
     *
     * @param method is the <code>CtMethod</code> object to extract details from.
     * @return a <code>MethodMetaData</code> instance.
     */
    public static MethodMetaData createMethodMetaData(final CtMethod method) {
        if (method == null) {
            throw new IllegalArgumentException("method can not be null");
        }

        MethodMetaDataImpl methodMetaData = new MethodMetaDataImpl();
        methodMetaData.setName(method.getName());

        //Javassist modifier is the same as java modifier used in ReflectionMetaDataMaker
        methodMetaData.setModifiers(method.getModifiers());

        try {
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

            // attributes
            for (Iterator attrs = method.getMethodInfo().getAttributes().iterator(); attrs.hasNext();) {
                addAttribute(methodMetaData, ((AttributeInfo)attrs.next()));
            }

            return methodMetaData;
        }
        catch (NotFoundException e) {
            // might happen if one parameter type is not in classpath
            // fake the returned metadata
            methodMetaData.setReturnType("void");
            methodMetaData.setParameterTypes(new String[]{});
            methodMetaData.setExceptionTypes(new String[]{});
            System.err.println("AspectWerkz - <WARN> unable to build metadata for "
                    + method.getDeclaringClass().getName()+"."+method.getName()+"(..)"
            );
            return methodMetaData;
            //throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Construct field meta-data from a Javassist <code>CtField</code> object.
     *
     * @param field is the <code>CtField</code> object to extract details from.
     * @return a <code>FieldMetaData</code> instance.
     */
    public static FieldMetaData createFieldMetaData(final CtField field) {
        if (field == null) {
            throw new IllegalArgumentException("field can not be null");
        }

        FieldMetaDataImpl fieldMetaData = new FieldMetaDataImpl();
        fieldMetaData.setName(field.getName());
        fieldMetaData.setModifiers(field.getModifiers());
        try {
            fieldMetaData.setType(field.getType().getName());

//            // attributes
//            for (Iterator attrs = field.getFieldInfo().getAttributes().iterator(); attrs.hasNext();) {
//                addAttribute(fieldMetaData, ((AttributeInfo)attrs.next()));
//            }

            return fieldMetaData;
        }
        catch (NotFoundException e) {
            // might happen if field type is not in classpath
            // fake the returned metadata
            fieldMetaData.setType(TypeConverter.convertTypeToJava(Object.class));
            System.err.println("AspectWerkz - <WARN> unable to build metadata for "
                    + field.getDeclaringClass().getName()+"."+field.getName()
            );
            return fieldMetaData;
            //throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Construct method meta-data from a Javassist <code>CtConstructor</code> object.
     *
     * @param constructor is the <code>CtConstructor</code> object to extract details from.
     * @return a <code>ConstructorMetaData</code> instance.
     */
    public static ConstructorMetaData createConstructorMetaData(final CtConstructor constructor) {
        if (constructor == null) {
            throw new IllegalArgumentException("constructor can not be null");
        }

        ConstructorMetaDataImpl constructorMetaData = new ConstructorMetaDataImpl();
        constructorMetaData.setName(CONSTRUCTOR_NAME);

        //Javassist modifier is the same as java modifier used in ReflectionMetaDataMaker
        constructorMetaData.setModifiers(constructor.getModifiers());

        try {
            // parameters
            CtClass[] javaParameters = constructor.getParameterTypes();
            String[] parameterTypes = new String[javaParameters.length];
            for (int j = 0; j < javaParameters.length; j++) {
                parameterTypes[j] = javaParameters[j].getName();
            }
            constructorMetaData.setParameterTypes(parameterTypes);

            // exceptions
            CtClass[] exceptionTables = constructor.getExceptionTypes();
            String[] exceptions = new String[exceptionTables.length];
            for (int k = 0; k < exceptionTables.length; k++) {
                exceptions[k] = exceptionTables[k].getName();
            }
            constructorMetaData.setExceptionTypes(exceptions);

            // TODO not supported by Javassist ??
//            // attributes
//            for (Iterator attrs =constructor.getMethodInfo().getAttributes().iterator(); attrs.hasNext();) {
//                addAttribute(constructorMetaData, ((AttributeInfo)attrs.next()));
//            }

            return constructorMetaData;
        }
        catch (NotFoundException e) {
            // might happen if one parameter type is not in classpath
            // fake the returned metadata
            constructorMetaData.setParameterTypes(new String[]{});
            constructorMetaData.setExceptionTypes(new String[]{});
            System.err.println("AspectWerkz - <WARN> unable to build metadata for "
                    + constructor.getDeclaringClass().getName()+".<init>"
            );
            return constructorMetaData;
            //throw new WrappedRuntimeException(e);
        }
    }

    private static void addAttribute(final MemberMetaData memberMetaData, final AttributeInfo attributeInfo) {
        if (true || filter(attributeInfo)) return;
        byte[] serializedAttribute = attributeInfo.get();
        try {
            Object attribute = new ObjectInputStream(
                    new ByteArrayInputStream(serializedAttribute)
            ).readObject();
            if (attribute instanceof CustomAttribute) {
                memberMetaData.addAttribute((CustomAttribute)attribute);
            }
        }
        catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * @TODO ALEX - needed, not used?
     *
     * @param classMetaData
     * @param attributeInfo
     */
    private static void addAttribute(final ClassMetaData classMetaData, final AttributeInfo attributeInfo) {
        if (true || filter(attributeInfo)) return;
        byte[] serializedAttribute = attributeInfo.get();
        try {
            Object attribute = new ObjectInputStream(
                    new ByteArrayInputStream(serializedAttribute)
            ).readObject();
            if (attribute instanceof CustomAttribute) {
                classMetaData.addAttribute((CustomAttribute)attribute);
            }
        }
        catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * @TODO ALEX - needed, not used?
     *
     * @param interfaceMetaData
     * @param attributeInfo
     */
    private static void addAttribute(final InterfaceMetaData interfaceMetaData, final AttributeInfo attributeInfo) {
        if (true || filter(attributeInfo)) return;
        byte[] serializedAttribute = attributeInfo.get();
        try {
            Object attribute = new ObjectInputStream(
                    new ByteArrayInputStream(serializedAttribute)
            ).readObject();
            if (attribute instanceof CustomAttribute) {
                interfaceMetaData.addAttribute((CustomAttribute)attribute);
            }
        }
        catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     *
     * @param attr
     * @return
     */
    private static boolean filter(final AttributeInfo attr) {
        if (attr instanceof CodeAttribute ||
            attr instanceof ConstantAttribute ||
            attr instanceof SourceFileAttribute ||
            attr instanceof LineNumberAttribute ||
            attr instanceof SyntheticAttribute ||
            attr instanceof InnerClassesAttribute)
        return true;
        else
        return false;
    }
}
