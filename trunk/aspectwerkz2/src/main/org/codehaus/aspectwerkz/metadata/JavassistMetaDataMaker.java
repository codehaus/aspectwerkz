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

import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;

import javassist.bytecode.AttributeInfo;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.ConstantAttribute;
import javassist.bytecode.InnerClassesAttribute;
import javassist.bytecode.LineNumberAttribute;
import javassist.bytecode.SourceFileAttribute;
import javassist.bytecode.SyntheticAttribute;

/**
 * Convenience methods to construct <code>MetaDataBase</code> instances from Javassist classes.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @author <a href="mailto:vta@medios.fi">Tibor Varga</a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class JavassistMetaDataMaker
{
    private MetaDataMaker m_metaDataMakerDelegate;

    public JavassistMetaDataMaker(MetaDataMaker metaDataMakerDelegate)
    {
        m_metaDataMakerDelegate = metaDataMakerDelegate;
    }

    /**
     * Construct class meta-data from a Javassist <code>JavaClass</code> object.
     *
     * @param javaClass is the <code>JavaClass</code> object to extract details from.
     * @return a <code>ClassMetaData</code> instance.
     */
    public ClassMetaData createClassMetaData(final CtClass javaClass)
    {
        if (javaClass == null)
        {
            throw new IllegalArgumentException("class can not be null");
        }

        String className = javaClass.getName().replace('/', '.');

        if (m_metaDataMakerDelegate.m_classMetaDataCache.containsKey(className))
        {
            return (ClassMetaData) m_metaDataMakerDelegate.m_classMetaDataCache
            .get(className);
        }

        ClassMetaDataImpl classMetaData = new ClassMetaDataImpl();

        classMetaData.setName(className);
        classMetaData.setModifiers(javaClass.getModifiers());

        // constructors
        List constructorList = new ArrayList();
        CtConstructor[] constructors = javaClass.getConstructors();

        for (int i = 0; i < constructors.length; i++)
        {
            CtConstructor constructor = constructors[i];

            constructorList.add(createConstructorMetaData(constructor));
        }

        classMetaData.setConstructors(constructorList);

        // methods
        List methodList = new ArrayList();
        CtMethod[] methods = javaClass.getDeclaredMethods();

        for (int i = 0; i < methods.length; i++)
        {
            CtMethod method = methods[i];

            methodList.add(createMethodMetaData(method));
        }

        classMetaData.setMethods(methodList);

        // fields
        List fieldList = new ArrayList();
        CtField[] fields = javaClass.getDeclaredFields();

        for (int i = 0; i < fields.length; i++)
        {
            CtField field = fields[i];

            fieldList.add(createFieldMetaData(field));
        }

        classMetaData.setFields(fieldList);

        // attributes
        // TODO not supported by Javassist ??
        //        for (Iterator attrs = javaClass.getClassFile().getAttributes().iterator(); attrs.hasNext();) {
        //            addAttribute(classMetaData, ((AttributeInfo)attrs.next()));
        //        }
        // interfaces
        List interfaceList = new ArrayList();

        try
        {
            CtClass[] interfaces = javaClass.getInterfaces();

            for (int i = 0; i < interfaces.length; i++)
            {
                CtClass anInterface = interfaces[i];

                interfaceList.add(createInterfaceMetaData(anInterface));
            }
        }
        catch (NotFoundException e)
        {
            System.err.println(
                "AspectWerkz - <WARN> unable to build metadata for "
                + className + ", missing interface " + e.getMessage());
        }
        finally
        {
            classMetaData.setInterfaces(interfaceList);
        }

        try
        {
            // super class
            CtClass superClass = javaClass.getSuperclass();

            if (superClass != null)
            { // has super class?

                ClassMetaData superClassMetaData = createClassMetaData(superClass);

                classMetaData.setSuperClass(superClassMetaData);
            }
        }
        catch (NotFoundException e)
        {
            System.err.println(
                "AspectWerkz - <WARN> unable to build metadata for "
                + className + ", missing superclass " + e.getMessage());
        }

        synchronized (m_metaDataMakerDelegate.m_classMetaDataCache)
        {
            m_metaDataMakerDelegate.m_classMetaDataCache.put(classMetaData
                .getName(), classMetaData);
        }

        return classMetaData;
    }

    /**
     * Construct interface meta-data from a Javassist <code>JavaClass</code> object.
     *
     * @param javaClass is the <code>JavaClass</code> object to extract details from.
     * @return a <code>InterfaceMetaData</code> instance.
     */
    private InterfaceMetaData createInterfaceMetaData(final CtClass javaClass)
        throws NotFoundException
    {
        if (javaClass == null)
        {
            throw new IllegalArgumentException("class can not be null");
        }

        String className = javaClass.getName().replace('/', '.');

        ;

        if (m_metaDataMakerDelegate.m_interfaceMetaDataCache.containsKey(
                className))
        {
            return (InterfaceMetaData) m_metaDataMakerDelegate.m_interfaceMetaDataCache
            .get(className);
        }

        InterfaceMetaDataImpl interfaceMetaData = new InterfaceMetaDataImpl();

        interfaceMetaData.setName(className);

        // TODO not supported by Javassist ??
        //        // attributes
        //        for (Iterator attrs = javaClass.getClassFile().getAttributes().iterator(); attrs.hasNext();) {
        //            addAttribute(interfaceMetaData, ((AttributeInfo)attrs.next()));
        //        }
        try
        {
            List interfaceList = new ArrayList();
            CtClass[] interfaces = javaClass.getInterfaces();

            for (int i = 0; i < interfaces.length; i++)
            {
                CtClass anInterface = interfaces[i];

                interfaceList.add(createInterfaceMetaData(anInterface));
            }

            interfaceMetaData.setInterfaces(interfaceList);
        }
        catch (RuntimeException e)
        {
            // wrap a IOException from Javassist ClassPoolTail
            // TODO : occurs only when *running* WLS and opening a WLW app
            // on weblogic.jdbc.rmi.internal.ConnectionImpl_weblogic_jdbc_wrapper_PoolConnection_com_pointbase_net_netJDBCConnection
            // and alike
            System.err.println(
                "AspectWerkz - <WARN> unable to build interface metadata for "
                + javaClass.getName().replace('/', '.') + "." + ": "
                + e.getMessage());
        }

        //    throw new WrappedRuntimeException(e);
        //}
        synchronized (m_metaDataMakerDelegate.m_interfaceMetaDataCache)
        {
            m_metaDataMakerDelegate.m_interfaceMetaDataCache.put(interfaceMetaData
                .getName(), interfaceMetaData);
        }

        return interfaceMetaData;
    }

    /**
     * Construct method meta-data from a Javassist <code>CtMethod</code> object.
     *
     * @param method is the <code>CtMethod</code> object to extract details from.
     * @return a <code>MethodMetaData</code> instance.
     */
    public static MethodMetaData createMethodMetaData(final CtMethod method)
    {
        if (method == null)
        {
            throw new IllegalArgumentException("method can not be null");
        }

        MethodMetaDataImpl methodMetaData = new MethodMetaDataImpl();

        methodMetaData.setName(method.getName());

        //Javassist modifier is the same as java modifier used in ReflectionMetaDataMaker
        methodMetaData.setModifiers(method.getModifiers());

        try
        {
            // return type
            methodMetaData.setReturnType(method.getReturnType().getName()
                                               .replace('/', '.'));

            // parameters
            CtClass[] javaParameters = method.getParameterTypes();
            String[] parameterTypes = new String[javaParameters.length];

            for (int j = 0; j < javaParameters.length; j++)
            {
                parameterTypes[j] = javaParameters[j].getName().replace('/', '.');
                ;
            }

            methodMetaData.setParameterTypes(parameterTypes);

            // exceptions
            CtClass[] exceptionTables = method.getExceptionTypes();
            String[] exceptions = new String[exceptionTables.length];

            for (int k = 0; k < exceptionTables.length; k++)
            {
                exceptions[k] = exceptionTables[k].getName().replace('/', '.');
            }

            methodMetaData.setExceptionTypes(exceptions);

            //            // attributes
            //            for (Iterator attrs = method.getMethodInfo().getAttributes().iterator(); attrs.hasNext();) {
            //                addAttribute(methodMetaData, ((AttributeInfo)attrs.next()));
            //            }
            return methodMetaData;
        }
        catch (NotFoundException e)
        {
            // might happen if one parameter type is not in classpath
            System.err.println(
                "AspectWerkz - <WARN> unable to build metadata for "
                + method.getDeclaringClass().getName().replace('/', '.') + "."
                + method.getSignature() + ": " + e.getMessage());

            return MethodMetaData.NullMethodMetaData.NULL_METHOD_METADATA;
        }
    }

    /**
     * Construct field meta-data from a Javassist <code>CtField</code> object.
     *
     * @param field is the <code>CtField</code> object to extract details from.
     * @return a <code>FieldMetaData</code> instance.
     */
    public static FieldMetaData createFieldMetaData(final CtField field)
    {
        if (field == null)
        {
            throw new IllegalArgumentException("field can not be null");
        }

        FieldMetaDataImpl fieldMetaData = new FieldMetaDataImpl();

        fieldMetaData.setName(field.getName());
        fieldMetaData.setModifiers(field.getModifiers());

        try
        {
            fieldMetaData.setType(field.getType().getName().replace('/', '.'));

            //            // attributes
            //            for (Iterator attrs = field.getFieldInfo().getAttributes().iterator(); attrs.hasNext();) {
            //                addAttribute(fieldMetaData, ((AttributeInfo)attrs.next()));
            //            }
            return fieldMetaData;
        }
        catch (NotFoundException e)
        {
            // might happen if field type is not in classpath
            System.err.println(
                "AspectWerkz - <WARN> unable to build metadata for "
                + field.getDeclaringClass().getName().replace('/', '.') + "."
                + field.getName() + ": " + e.getMessage());

            return FieldMetaData.NullFieldMetaData.NULL_FIELD_METADATA;
        }
    }

    /**
     * Construct method meta-data from a Javassist <code>CtConstructor</code> object.
     *
     * @param constructor is the <code>CtConstructor</code> object to extract details from.
     * @return a <code>ConstructorMetaData</code> instance.
     */
    public static ConstructorMetaData createConstructorMetaData(
        final CtConstructor constructor)
    {
        if (constructor == null)
        {
            throw new IllegalArgumentException("constructor can not be null");
        }

        ConstructorMetaDataImpl constructorMetaData = new ConstructorMetaDataImpl();

        constructorMetaData.setName(MetaDataMaker.CONSTRUCTOR_NAME);

        //Javassist modifier is the same as java modifier used in ReflectionMetaDataMaker
        constructorMetaData.setModifiers(constructor.getModifiers());

        try
        {
            // parameters
            CtClass[] javaParameters = constructor.getParameterTypes();
            String[] parameterTypes = new String[javaParameters.length];

            for (int j = 0; j < javaParameters.length; j++)
            {
                parameterTypes[j] = javaParameters[j].getName().replace('/', '.');
            }

            constructorMetaData.setParameterTypes(parameterTypes);

            // exceptions
            CtClass[] exceptionTables = constructor.getExceptionTypes();
            String[] exceptions = new String[exceptionTables.length];

            for (int k = 0; k < exceptionTables.length; k++)
            {
                exceptions[k] = exceptionTables[k].getName().replace('/', '.');
            }

            constructorMetaData.setExceptionTypes(exceptions);

            // TODO not supported by Javassist ??
            //            // attributes
            //            for (Iterator attrs =constructor.getMethodInfo().getAttributes().iterator(); attrs.hasNext();) {
            //                addAttribute(constructorMetaData, ((AttributeInfo)attrs.next()));
            //            }
            return constructorMetaData;
        }
        catch (NotFoundException e)
        {
            // might happen if one parameter type is not in classpath
            System.err.println(
                "AspectWerkz - <WARN> unable to build metadata for "
                + constructor.getDeclaringClass().getName().replace('/', '.')
                + ".<init> " + constructor.getSignature() + ": "
                + e.getMessage());

            return ConstructorMetaData.NullConstructorMetaData.NULL_CONSTRUCTOR_METADATA;
        }
    }

    //    private static void addAttribute(final MemberMetaData memberMetaData, final AttributeInfo attributeInfo) {
    //        if (true || filter(attributeInfo)) {
    //            return;
    //        }
    //        byte[] serializedAttribute = attributeInfo.get();
    //        try {
    //            Object attribute = new ObjectInputStream(new ByteArrayInputStream(serializedAttribute)).readObject();
    //            if (attribute instanceof CustomAttribute) {
    //                memberMetaData.addAttribute((CustomAttribute)attribute);
    //            }
    //        }
    //        catch (Exception e) {
    //            throw new WrappedRuntimeException(e);
    //        }
    //    }
    //
    //    /**
    //     * @param classMetaData
    //     * @param attributeInfo
    //     * @TODO ALEX - needed, not used?
    //     */
    //    private static void addAttribute(final ClassMetaData classMetaData, final AttributeInfo attributeInfo) {
    //        if (true || filter(attributeInfo)) {
    //            return;
    //        }
    //        byte[] serializedAttribute = attributeInfo.get();
    //        try {
    //            Object attribute = new ObjectInputStream(new ByteArrayInputStream(serializedAttribute)).readObject();
    //            if (attribute instanceof CustomAttribute) {
    //                classMetaData.addAttribute((CustomAttribute)attribute);
    //            }
    //        }
    //        catch (Exception e) {
    //            throw new WrappedRuntimeException(e);
    //        }
    //    }
    //
    //    /**
    //     * @param interfaceMetaData
    //     * @param attributeInfo
    //     * @TODO ALEX - needed, not used?
    //     */
    //    private static void addAttribute(final InterfaceMetaData interfaceMetaData, final AttributeInfo attributeInfo) {
    //        if (true || filter(attributeInfo)) {
    //            return;
    //        }
    //        byte[] serializedAttribute = attributeInfo.get();
    //        try {
    //            Object attribute = new ObjectInputStream(new ByteArrayInputStream(serializedAttribute)).readObject();
    //            if (attribute instanceof CustomAttribute) {
    //                interfaceMetaData.addAttribute((CustomAttribute)attribute);
    //            }
    //        }
    //        catch (Exception e) {
    //            throw new WrappedRuntimeException(e);
    //        }
    //    }

    /**
     * @param attr
     * @return
     */
    private static boolean filter(final AttributeInfo attr)
    {
        if (attr instanceof CodeAttribute || attr instanceof ConstantAttribute
            || attr instanceof SourceFileAttribute
            || attr instanceof LineNumberAttribute
            || attr instanceof SyntheticAttribute
            || attr instanceof InnerClassesAttribute)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}
