/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.joinpoint.impl;

import org.codehaus.backport175.reader.Annotation;
import org.codehaus.backport175.reader.Annotations;
import org.codehaus.aspectwerkz.joinpoint.ConstructorSignature;

import java.lang.reflect.Constructor;
import java.util.List;

/**
 * Implementation for the constructor signature.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r </a>
 */
public class ConstructorSignatureImpl implements ConstructorSignature {
    private final Class m_declaringType;

    private final Constructor m_constructor;

    /**
     * @param declaringType
     * @param constructor
     */
    public ConstructorSignatureImpl(final Class declaringType, final Constructor constructor) {
        m_declaringType = declaringType;
        m_constructor = constructor;
    }

    /**
     * Returns the constructor.
     *
     * @return the constructor
     */
    public Constructor getConstructor() {
        return m_constructor;
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
     * <p/>
     * <pre>
     * boolean isPublic = java.lang.reflect.Modifier.isPublic(signature.getModifiers());
     * </pre>
     *
     * @return the mofifiers
     */
    public int getModifiers() {
        return m_constructor.getModifiers();
    }

    /**
     * Returns the name (f.e. name of method of field).
     *
     * @return
     */
    public String getName() {
        //return m_constructorTuple.getName();
        return m_constructor.getName();
    }

    /**
     * Returns the exception types declared by the code block.
     *
     * @return the exception types
     */
    public Class[] getExceptionTypes() {
        return m_constructor.getExceptionTypes();
    }

    /**
     * Returns the parameter types.
     *
     * @return the parameter types
     */
    public Class[] getParameterTypes() {
        return m_constructor.getParameterTypes();
    }

    /**
     * Return the given annotation if any.
     *
     * @param annotationClass the annotation class
     * @return the annotation or null
     */
    public Annotation getAnnotation(final Class annotationClass) {
        return Annotations.getAnnotation(annotationClass, m_constructor);
    }

    /**
     * Return all the annotations.
     *
     * @return annotations
     */
    public Annotation[] getAnnotations() {
        return Annotations.getAnnotations(m_constructor);
    }

    /**
     * Returns a string representation of the signature.
     *
     * @return a string representation
     */
    public String toString() {
        return m_constructor.toString();
    }
}