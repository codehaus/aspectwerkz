/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.annotation.instrumentation.bcel;

import org.apache.bcel.classfile.Attribute;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.classfile.Unknown;
import org.codehaus.aspectwerkz.annotation.instrumentation.AttributeExtractor;
import org.codehaus.aspectwerkz.definition.DescriptorUtil;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * BCEL implementation of the AttributeExtractor interface. <p/>Extracts attributes from the class file on class,
 * method and field level.
 * 
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public class BcelAttributeExtractor implements AttributeExtractor {
    /**
     * The BCEL java class.
     */
    private JavaClass m_javaClass = null;

    /**
     * Open the classfile and parse it in to the BCEL library.
     * 
     * @param className the class name to load.
     * @param loader the classloader to use to get the inputstream of the .class file.
     */
    public void initialize(final String className, final ClassLoader loader) {
        String classFileName = className.replace('.', '/') + ".class";
        try {
            InputStream classStream = loader.getResourceAsStream(classFileName);
            if (classStream == null) {
                return;
            }
            ClassParser classParser = new ClassParser(classStream, classFileName);
            m_javaClass = classParser.parse();
        } catch (IOException e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Returns the class attributes.
     * 
     * @return the class attributes
     */
    public Object[] getClassAttributes() {
        List attributes = new ArrayList();
        Attribute[] classAttributes = m_javaClass.getAttributes();
        for (int i = 0; i < classAttributes.length; i++) {
            if (classAttributes[i] instanceof Unknown) {
                Unknown unknownAttrib = (Unknown) classAttributes[i];
                byte[] serializedAttribute = unknownAttrib.getBytes();
                try {
                    Object attribute = new ObjectInputStream(new ByteArrayInputStream(serializedAttribute))
                            .readObject();
                    attributes.add(attribute);
                } catch (Exception e) {
                    // ignore
                }
            }
        }
        return attributes.toArray(new Object[attributes.size()]);
    }

    /**
     * Return all the attributes associated with a method that have a particular method signature.
     * 
     * @param methodName The name of the method.
     * @param methodParamTypes An array of parameter types as given by the reflection api.
     * @return the method attributes.
     */
    public Object[] getMethodAttributes(final String methodName, final String[] methodParamTypes) {
        List attributes = new ArrayList();
        Method[] methods = m_javaClass.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if (method.getName().equals(methodName)) {
                if (Arrays.equals(methodParamTypes, DescriptorUtil.getParameters(method.getSignature()))) {
                    Attribute[] methodAttributes = method.getAttributes();
                    for (int j = 0; j < methodAttributes.length; j++) {
                        if (methodAttributes[j] instanceof Unknown) {
                            Unknown unknownAttrib = (Unknown) methodAttributes[j];
                            byte[] serializedAttribute = unknownAttrib.getBytes();
                            try {
                                Object attribute = new ObjectInputStream(new ByteArrayInputStream(serializedAttribute))
                                        .readObject();
                                attributes.add(attribute);
                            } catch (Exception e) {
                                // ignore
                            }
                        }
                    }
                }
            }
        }
        return attributes.toArray(new Object[attributes.size()]);
    }

    /**
     * Return all the attributes associated with a constructor that have a particular method signature.
     * 
     * @param constructorParamTypes An array of parameter types as given by the reflection api.
     * @return the constructor attributes.
     */
    public Object[] getConstructorAttributes(String[] constructorParamTypes) {
        List attributes = new ArrayList();
        Method[] methods = m_javaClass.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if (method.getName().equals("<init>"))
            if (Arrays.equals(constructorParamTypes, DescriptorUtil.getParameters(method.getSignature()))) {
                Attribute[] methodAttributes = method.getAttributes();
                for (int j = 0; j < methodAttributes.length; j++) {
                    if (methodAttributes[j] instanceof Unknown) {
                        Unknown unknownAttrib = (Unknown) methodAttributes[j];
                        byte[] serializedAttribute = unknownAttrib.getBytes();
                        try {
                            Object attribute = new ObjectInputStream(new ByteArrayInputStream(serializedAttribute))
                                    .readObject();
                            attributes.add(attribute);
                        } catch (Exception e) {
                            // ignore
                        }
                    }
                }
            }
        }
        return attributes.toArray(new Object[attributes.size()]);
    }

    /**
     * Return all the attributes associated with a field.
     * 
     * @param fieldName The name of the field.
     * @return the field attributes.
     */
    public Object[] getFieldAttributes(final String fieldName) {
        List attributes = new ArrayList();
        Field[] fields = m_javaClass.getFields();
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].getName().equals(fieldName)) {
                Attribute[] fieldAttributes = fields[i].getAttributes();
                for (int j = 0; j < fieldAttributes.length; j++) {
                    if (fieldAttributes[j] instanceof Unknown) {
                        Unknown unknownAttrib = (Unknown) fieldAttributes[j];
                        byte[] serializedAttribute = unknownAttrib.getBytes();
                        try {
                            Object attribute = new ObjectInputStream(new ByteArrayInputStream(serializedAttribute))
                                    .readObject();
                            attributes.add(attribute);
                        } catch (Exception e) {
                            // ignore
                        }
                    }
                }
            }
        }
        return attributes.toArray(new Object[attributes.size()]);
    }
}