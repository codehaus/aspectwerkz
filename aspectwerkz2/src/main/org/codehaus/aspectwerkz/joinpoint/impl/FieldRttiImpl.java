/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.joinpoint.impl;

import java.lang.reflect.Field;

import org.codehaus.aspectwerkz.joinpoint.FieldRtti;

/**
 * Implementation for the field signature.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class FieldRttiImpl implements FieldRtti {

    private final FieldSignatureImpl m_signature;
    private final Object m_this;
    private final Object m_target;

    private Object m_fieldValue;


    /**
     * Creates a new field RTTI.
     *
     * @param signature
     * @param thisInstance
     * @param targetInstance
     */
    public FieldRttiImpl(
            final FieldSignatureImpl signature,
            final Object thisInstance,
            final Object targetInstance) {
        m_signature = signature;
        m_this = thisInstance;
        m_target = targetInstance;
    }


      /**
     * Returns the target instance.
     *
     * @return the target instance
     */
    public Object getTarget() {
        return m_target;
    }

    /**
     * Returns the instance currently executing.
     *
     * @return the instance currently executing
     */
    public Object getThis() {
        return m_this;
    }

    /**
     * Returns the declaring class.
     *
     * @return the declaring class
     */
    public Class getDeclaringType() {
        return m_signature.getDeclaringType();
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
        return m_signature.getModifiers();
    }

    /**
     * Returns the name (f.e. name of method of field).
     *
     * @return the name
     */
    public String getName() {
        return m_signature.getName();
    }

    /**
     * Returns the field.
     *
     * @return the field
     */
    public Field getField() {
        return m_signature.getField();
    }

    /**
     * Returns the field type.
     *
     * @return the field type
     */
    public Class getFieldType() {
        return m_signature.getFieldType();
    }

    /**
     * Returns the value of the field.
     *
     * @return the value of the field
     */
    public Object getFieldValue() {
        return m_fieldValue;
    }

    /**
     * Sets the value of the field.
     *
     * @param fieldValue the value of the field
     */
    public void setFieldValue(final Object fieldValue) {
        m_fieldValue = fieldValue;
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
