/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.joinpoint.impl;

import org.codehaus.aspectwerkz.ConstructorTuple;
import org.codehaus.aspectwerkz.annotation.Annotation;
import org.codehaus.aspectwerkz.annotation.Annotations;
import org.codehaus.aspectwerkz.joinpoint.ConstructorSignature;

import java.lang.reflect.Constructor;
import java.util.List;

/**
 * Implementation for the constructor signature.
 * 
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public class ConstructorSignatureImpl implements ConstructorSignature {
    private final Class m_declaringType;

    private final ConstructorTuple m_constructorTuple;

    /**
     * @param declaringType
     * @param constructorTuple
     */
    public ConstructorSignatureImpl(final Class declaringType, final ConstructorTuple constructorTuple) {
        m_declaringType = declaringType;
        m_constructorTuple = constructorTuple;
    }

    /**
     * Returns the constructor tuple.
     * 
     * @return the constructor tuple
     */
    public ConstructorTuple getConstructorTuple() {
        return m_constructorTuple;
    }

    /**
     * Returns the constructor.
     * 
     * @return the constructor
     */
    public Constructor getConstructor() {
        return m_constructorTuple.getOriginalConstructor();
    }

    /**
     * Returns the declaring class.
     * 
     * @return the declaring class
     */
    public Class getDeclaringType() {
        return m_declaringType;
    }

    /**
     * Returns the modifiers for the signature. <p/>Could be used like this:
     * 
     * <pre>
     * boolean isPublic = java.lang.reflect.Modifier.isPublic(signature.getModifiers());
     * </pre>
     * 
     * @return the mofifiers
     */
    public int getModifiers() {
        return m_constructorTuple.getOriginalConstructor().getModifiers();
    }

    /**
     * Returns the name (f.e. name of method of field).
     * 
     * @return
     */
    public String getName() {
        return m_constructorTuple.getName();
    }

    /**
     * Returns the exception types declared by the code block.
     * 
     * @return the exception types
     */
    public Class[] getExceptionTypes() {
        return m_constructorTuple.getOriginalConstructor().getExceptionTypes();
    }

    /**
     * Returns the parameter types.
     * 
     * @return the parameter types
     */
    public Class[] getParameterTypes() {
        return m_constructorTuple.getOriginalConstructor().getParameterTypes();
    }

    /**
     * Return the annotation with a specific name.
     * 
     * @param annotationName the annotation name
     * @return the annotation or null
     */
    public Annotation getAnnotation(final String annotationName) {
        return Annotations.getAnnotation(annotationName, m_constructorTuple.getWrapperConstructor());
    }

    /**
     * Return a list with the annotations with a specific name.
     * 
     * @param annotationName the annotation name
     * @return the annotations in a list (can be empty)
     */
    public List getAnnotations(final String annotationName) {
        return Annotations.getAnnotations(annotationName, m_constructorTuple.getWrapperConstructor());        
    }

    /**
     * Return all the annotations <p/>Each annotation is wrapped in
     * {@link org.codehaus.aspectwerkz.annotation.AnnotationInfo}instance.
     * 
     * @return a list with the annotations
     */
    public List getAnnotationInfos() {
        return Annotations.getAnnotationInfos(m_constructorTuple.getWrapperConstructor());
    }

    /**
     * Returns a string representation of the signature.
     * 
     * @return a string representation
     * @TODO: implement toString to something meaningful
     */
    public String toString() {
        return super.toString();
    }
}