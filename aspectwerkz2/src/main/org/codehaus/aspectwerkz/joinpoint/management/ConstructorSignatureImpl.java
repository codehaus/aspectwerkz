/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.joinpoint.management;

import java.lang.reflect.Constructor;

import org.codehaus.aspectwerkz.ConstructorTuple;
import org.codehaus.aspectwerkz.joinpoint.ConstructorSignature;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
class ConstructorSignatureImpl implements ConstructorSignature {

    private final Class m_declaringType;
    private final ConstructorTuple m_constructorTuple;

    private Object[] m_parameterValues = EMPTY_OBJECT_ARRAY;
    private Object m_newInstance;

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
     * Returns the new instance created by the constructor.
     *
     * @return the new instance
     */
    public Object getNewInstance() {
        return m_newInstance;
    }

    /**
     * Sets the new instance created by the constructor.
     *
     * @param newInstance
     */
    public void setNewInstance(final Object newInstance) {
        m_newInstance = newInstance;
    }

    /**
     * Returns the modifiers for the signature.
     * <p/>
     * Could be used like this:
     * <pre>
     *      boolean isPublic = java.lang.reflect.Modifier.isPublic(signature.getModifiers());
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
     * Sets the values of the parameters.
     *
     * @param parameterValues
     */
    public void setParameterValues(final Object[] parameterValues) {
        m_parameterValues = parameterValues;
    }

    /**
     * Returns the values of the parameters.
     *
     * @return the values of the parameters
     */
    public Object[] getParameterValues() {
        return m_parameterValues;
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
