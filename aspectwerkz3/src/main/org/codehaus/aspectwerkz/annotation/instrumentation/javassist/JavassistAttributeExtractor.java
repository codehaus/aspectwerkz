/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.annotation.instrumentation.javassist;

import org.codehaus.aspectwerkz.util.Base64;
import org.codehaus.aspectwerkz.annotation.instrumentation.AttributeExtractor;
import org.codehaus.aspectwerkz.annotation.instrumentation.asm.CustomAttributeHelper;
import org.codehaus.aspectwerkz.definition.DescriptorUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.bytecode.AttributeInfo;
import javassist.bytecode.ClassFile;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.annotation.StringMemberValue;
import javassist.bytecode.annotation.Annotation;

/**
 * Javassist implementation of the AttributeExtractor interface. Extracts attributes from the class file on class,
 * method and field level.
 * 
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class JavassistAttributeExtractor implements AttributeExtractor {

    private final static String RUNTIME_INVISIBLE_ANNOTATIONS = "RuntimeInvisibleAnnotations";
    private final static String CUSTOM_ATTRIBUTE_CLASSNAME = "org.codehaus.aspectwerkz.annotation.instrumentation.asm.CustomAttribute";
    private final static String VALUE = "value";

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
            m_ctClass.stopPruning(true);
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
            retrieveCustomAttributes((AttributeInfo) it.next(), attributes);
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
                        retrieveCustomAttributes((AttributeInfo) it.next(), attributes);
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
    public Object[] getConstructorAttributes(final String[] constructorParamTypes) {
        if (m_ctClass.isPrimitive() || m_ctClass.isArray()) {
            return EMPTY_OBJECT_ARRAY;
        }
        List attributes = new ArrayList();
        CtConstructor[] constructors = m_ctClass.getDeclaredConstructors();
        for (int i = 0; i < constructors.length; i++) {
            CtConstructor constructor = constructors[i];
            if (Arrays.equals(constructorParamTypes, DescriptorUtil.getParameters(constructors[i].getSignature()))) {
                for (Iterator it = constructor.getMethodInfo().getAttributes().iterator(); it.hasNext();) {
                    retrieveCustomAttributes((AttributeInfo) it.next(), attributes);
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
                    retrieveCustomAttributes((AttributeInfo) it.next(), attributes);
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
        if (attributeInfo.getName().equals(RUNTIME_INVISIBLE_ANNOTATIONS)) {
            AnnotationsAttribute annotationAttribute = (AnnotationsAttribute)attributeInfo;
            for (int i = 0; i < annotationAttribute.getAnnotations().length; i++) {
                Annotation annotation = annotationAttribute.getAnnotations()[i];
                // TODO: stuff is hard coded here - dump it with AW 2.0
                if (annotation.getTypeName().equals(CUSTOM_ATTRIBUTE_CLASSNAME)) {
                    String value = ((StringMemberValue)annotation.getMemberValue(VALUE)).getValue();
                    byte[] bytes = Base64.decode(value);
                    listToPutAttributesIn.add(CustomAttributeHelper.extractCustomAnnotation(bytes));
                }
            }
        }
        // NOTE: Matching on 1.5 annotations is not supported in the delegation engine and will not be supported
    }

}