/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.joinpoint.management;

import java.lang.reflect.Method;

import org.codehaus.aspectwerkz.MethodTuple;
import org.codehaus.aspectwerkz.joinpoint.MethodSignature;

/**
 * Implementation for the method signature.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class MethodSignatureImpl implements MethodSignature {

    private final Class m_declaringType;
    private final MethodTuple m_methodTuple;

    private Object[] m_parameterValues = EMPTY_OBJECT_ARRAY;
    private Object m_returnValue;

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
     * Returns the return type.
     *
     * @return the return type
     */
    public Class getReturnType() {
        return m_methodTuple.getOriginalMethod().getReturnType();
    }

    /**
     * Sets the return value.
     *
     * @param returnValue the return value
     */
    public void setReturnValue(final Object returnValue) {
        m_returnValue = returnValue;
    }

    /**
     * Returns the value of the return type.
     *
     * @return the value of the return type
     */
    public Object getReturnValue() {
        return m_returnValue;
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
