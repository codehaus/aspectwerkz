/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.attribdef.definition.attribute.bcel;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.classfile.Unknown;
import org.apache.bcel.classfile.Attribute;

import org.codehaus.aspectwerkz.attribdef.definition.attribute.AttributeExtractor;
import org.codehaus.aspectwerkz.attribdef.definition.DescriptorUtil;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;

/**
 * BCEL implementation of the AttributeExtractor interface.
 * Extracts attributes from the class file on class, method and field level.
 *
 * Based on code from the Attrib4j project by Mark Pollack and Ted Neward (http://attrib4j.sourceforge.net/).
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class BcelAttributeExtractor implements AttributeExtractor {

    /**
     * The BCEL java class.
     */
    private JavaClass m_javaClass;

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
            ClassParser classParser = new ClassParser(classStream, classFileName);
            m_javaClass = classParser.parse();
        }
        catch (IOException e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Returns the class attributes.
     *
     * @return the class attributes
     */
    public Object[] getClassAttributes() {
        ArrayList attributes = new ArrayList();

        Attribute[] classAttributes = m_javaClass.getAttributes();
        for (int i = 0; i < classAttributes.length; i++) {

            if (classAttributes[i] instanceof Unknown) {
                Unknown unknownAttrib = (Unknown)classAttributes[i];
                byte[] serializedAttribute = unknownAttrib.getBytes();
                try {
                    Object attribute = new ObjectInputStream(
                            new ByteArrayInputStream(serializedAttribute)
                    ).readObject();
                    attributes.add(attribute);
                }
                catch (Exception e) {
                    throw new WrappedRuntimeException(e);
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
        ArrayList attributes = new ArrayList();

        Method[] methods = m_javaClass.getMethods();
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().equals(methodName)) {
                if (Arrays.equals(methodParamTypes,
                        DescriptorUtil.convertToJavaFormat(methods[i].getSignature()))
                ) {
                    Attribute[] methodAttributes = methods[i].getAttributes();
                    for (int j = 0; j < methodAttributes.length; j++) {

                        if (methodAttributes[j] instanceof Unknown) {
                            Unknown unknownAttrib = (Unknown)methodAttributes[j];
                            byte[] serializedAttribute = unknownAttrib.getBytes();
                            try {
                                Object attribute = new ObjectInputStream(
                                        new ByteArrayInputStream(serializedAttribute)
                                ).readObject();
                                attributes.add(attribute);
                            }
                            catch (Exception e) {
                                throw new WrappedRuntimeException(e);
                            }
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
        ArrayList al = new ArrayList();
        Field[] fields = m_javaClass.getFields();
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].getName().equals(fieldName)) {
                Attribute[] fieldAttributes = fields[i].getAttributes();
                for (int j = 0; j < fieldAttributes.length; j++) {
                    if (fieldAttributes[j] instanceof Unknown) {
                        Unknown unknownAttrib = (Unknown)fieldAttributes[j];
                        byte[] serializedAttribute = unknownAttrib.getBytes();
                        try {
                            Object attribute = new ObjectInputStream(
                                    new ByteArrayInputStream(serializedAttribute)
                            ).readObject();
                            al.add(attribute);
                        }
                        catch (Exception e) {
                            throw new WrappedRuntimeException(e);
                        }
                    }
                }
            }
        }
        return al.toArray(new Object[al.size()]);
    }
}
