/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.joinpoint.impl;

import org.codehaus.aspectwerkz.MethodTuple;
import org.codehaus.aspectwerkz.annotation.Annotation;
import org.codehaus.aspectwerkz.annotation.Annotations;
import org.codehaus.aspectwerkz.joinpoint.MethodSignature;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Implementation for the method signature.
 * 
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public class MethodSignatureImpl implements MethodSignature {
    private final Class m_declaringType;

    private final MethodTuple m_methodTuple;

    /**
     * @param declaringType
     * @param methodTuple
     */
    public MethodSignatureImpl(final Class declaringType, final MethodTuple methodTuple) {
        m_declaringType = declaringType;
        m_methodTuple = methodTuple;
    }

    /**
     * Returns the method tuple.
     * 
     * @return the method tuple
     */
    public MethodTuple getMethodTuple() {
        return m_methodTuple;
    }

    /**
     * Returns the method.
     * 
     * @return the method
     */
    public Method getMethod() {
        return m_methodTuple.getOriginalMethod();
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
        return m_methodTuple.getOriginalMethod().getModifiers();
    }

    /**
     * Returns the name (f.e. name of method of field).
     * 
     * @return
     */
    public String getName() {
        return m_methodTuple.getName();
    }

    /**
     * Returns the exception types declared by the code block.
     * 
     * @return the exception types
     */
    public Class[] getExceptionTypes() {
        return m_methodTuple.getOriginalMethod().getExceptionTypes();
    }

    /**
     * Returns the parameter types.
     * 
     * @return the parameter types
     */
    public Class[] getParameterTypes() {
        return m_methodTuple.getOriginalMethod().getParameterTypes();
    }

    /**
     * Returns the return type.
     * 
     * @return the return type
     */
    public Class getReturnType() {
        return m_methodTuple.getOriginalMethod().getReturnType();
    }
    
    /**
     * Return the annotation with a specific name.
     * 
     * @param annotationName the annotation name
     * @return the annotation or null
     */
    public Annotation getAnnotation(final String annotationName) {
        return Annotations.getAnnotation(annotationName, m_methodTuple.getWrapperMethod());
    }

    /**
     * Return a list with the annotations with a specific name.
     * 
     * @param annotationName the annotation name
     * @return the annotations in a list (can be empty)
     */
    public List getAnnotations(final String annotationName) {
        return Annotations.getAnnotations(annotationName, m_methodTuple.getWrapperMethod());        
    }

    /**
     * Return all the annotations <p/>Each annotation is wrapped in
     * {@link org.codehaus.aspectwerkz.annotation.AnnotationInfo}instance.
     * 
     * @return a list with the annotations
     */
    public List getAnnotationInfos() {
        return Annotations.getAnnotationInfos(m_methodTuple.getWrapperMethod());
    }

    /**
     * Returns a string representation of the signature.
     * 
     * @return a string representation
     * @TODO: implement toString to something meaningful
     */
    public String toString() {
        StringBuffer signature = new StringBuffer();
        signature.append(getReturnType().getName()).append(" ");
        signature.append(getName()).append("(");
        Class[] params = getParameterTypes();
        for (int i = 0; i < params.length; i++) {
            signature.append(params[i].getName());
            if (i < params.length-1 ) {
                signature.append(", ");
            }
        }
        signature.append(")");
        return signature.toString();
    }
}