/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.annotation.instrumentation.javassist;

import org.codehaus.aspectwerkz.ContextClassLoader;
import org.codehaus.aspectwerkz.annotation.instrumentation.AttributeEnhancer;
import org.codehaus.aspectwerkz.annotation.instrumentation.AttributeExtractor;
import org.codehaus.aspectwerkz.definition.DescriptorUtil;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.bytecode.AttributeInfo;
import javassist.bytecode.ClassFile;

/**
 * Javassist implementation of the AttributeExtractor interface. Extracts attributes from the class file on class,
 * method and field level.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class JavassistAttributeExtractor implements AttributeExtractor {
    /**
     * The Javassist class.
     */
    private CtClass m_ctClass;

    /**
     * Open the classfile and parse it in to the Javassist library.
     *
     * @param ctClass the class
     */
    public void initialize(final CtClass ctClass) {
        m_ctClass = ctClass;
        if (!(m_ctClass.isPrimitive() || m_ctClass.isArray())) {
            m_ctClass.defrost();
        }
    }

    /**
     * Returns the class attributes.
     *
     * @return the class attributes
     */
    public Object[] getClassAttributes() {
        if (m_ctClass.isPrimitive() || m_ctClass.isArray()) {
            return EMPTY_OBJECT_ARRAY;
        }
        List attributes = new ArrayList();
        ClassFile classFile = m_ctClass.getClassFile();
        List attrs = classFile.getAttributes();
        for (Iterator it = attrs.iterator(); it.hasNext();) {
            retrieveCustomAttributes((AttributeInfo)it.next(), attributes);
        }
        return attributes.toArray(new Object[attributes.size()]);
    }

    /**
     * Return all the attributes associated with a method that have a particular method signature.
     *
     * @param methodName       The name of the method.
     * @param methodParamTypes An array of parameter types as given by the reflection api.
     * @return the method attributes.
     */
    public Object[] getMethodAttributes(final String methodName, final String[] methodParamTypes) {
        if (m_ctClass.isPrimitive() || m_ctClass.isArray()) {
            return EMPTY_OBJECT_ARRAY;
        }
        List attributes = new ArrayList();
        CtMethod[] methods = m_ctClass.getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            CtMethod method = methods[i];
            if (method.getName().equals(methodName)) {
                if (Arrays.equals(methodParamTypes, DescriptorUtil.getParameters(methods[i].getSignature()))) {
                    for (Iterator it = method.getMethodInfo().getAttributes().iterator(); it.hasNext();) {
                        retrieveCustomAttributes((AttributeInfo)it.next(), attributes);
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
        if (m_ctClass.isPrimitive() || m_ctClass.isArray()) {
            return EMPTY_OBJECT_ARRAY;
        }
        List attributes = new ArrayList();
        CtField[] fields = m_ctClass.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].getName().equals(fieldName)) {
                CtField field = fields[i];
                for (Iterator it = field.getFieldInfo().getAttributes().iterator(); it.hasNext();) {
                    retrieveCustomAttributes((AttributeInfo)it.next(), attributes);
                }
            }
        }
        return attributes.toArray(new Object[attributes.size()]);
    }

    /**
     * Retrieves custom attributes and puts them in a list.
     *
     * @param attributeInfo
     * @param listToPutAttributesIn
     */
    private void retrieveCustomAttributes(final AttributeInfo attributeInfo, final List listToPutAttributesIn) {
        if (attributeInfo.getName().startsWith(AttributeEnhancer.CUSTOM_ATTRIBUTE)) {
            byte[] serializedAttribute = attributeInfo.get();
            try {
                Object attribute = new ContextClassLoader.NotBrokenObjectInputStream(
                        new ByteArrayInputStream(serializedAttribute)
                )
                        .readObject();
                listToPutAttributesIn.add(attribute);
            } catch (Exception e) {
                System.out.println("WARNING: could not retrieve annotation due to: " + e.toString());

                // ignore
            }
        }
    }
}
