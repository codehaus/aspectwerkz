/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.joinpoint.impl;

import org.codehaus.aspectwerkz.joinpoint.CatchClauseRtti;
import java.lang.ref.WeakReference;

/**
 * Implementation for the catch clause RTTI.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class CatchClauseRttiImpl implements CatchClauseRtti {
    private final CatchClauseSignatureImpl m_signature;
    private final WeakReference m_thisRef;
    private final WeakReference m_targetRef;
    private Object m_parameterValue;

    /**
    * Creates a new catch clause RTTI.
    *
    * @param signature
    * @param thisInstance
    * @param targetInstance
    */
    public CatchClauseRttiImpl(final CatchClauseSignatureImpl signature, final Object thisInstance,
                               final Object targetInstance) {
        m_signature = signature;
        m_thisRef = new WeakReference(thisInstance);
        m_targetRef = new WeakReference(targetInstance);
    }

    /**
    * Returns the instance currently executing.
    *
    * @return the instance currently executing
    */
    public Object getThis() {
        return m_thisRef.get();
    }

    /**
    * Returns the target instance.
    *
    * @return the target instance
    */
    public Object getTarget() {
        return m_targetRef.get();
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
    * @return
    */
    public String getName() {
        return m_signature.getName();
    }

    /**
    * Returns the parameter type.
    *
    * @return the parameter type
    */
    public Class getParameterType() {
        return m_signature.getParameterType();
    }

    /**
    * Returns the value of the parameter.
    *
    * @return the value of the parameter
    */
    public Object getParameterValue() {
        return m_parameterValue;
    }

    /**
    * Sets the value of the parameter.
    *
    * @param parameterValue the value of the parameter
    */
    public void setParameterValue(final Object parameterValue) {
        m_parameterValue = parameterValue;
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
