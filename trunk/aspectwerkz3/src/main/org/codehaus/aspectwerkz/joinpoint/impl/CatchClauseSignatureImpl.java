/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.joinpoint.impl;

import org.codehaus.aspectwerkz.joinpoint.CatchClauseSignature;
import org.codehaus.aspectwerkz.joinpoint.Signature;

/**
 * Implementation for the catch clause signature.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class CatchClauseSignatureImpl implements CatchClauseSignature {
    private final Class m_declaringType;
    private final int m_modifiers;
    private final String m_name;
    private Class m_parameterType;
    private String m_joinPointSignature;

    /**
    * Creates a new catch clause signature.
    *
    * @param exceptionClass
    * @param declaringClass
    * @param joinPointSignature
    */
    public CatchClauseSignatureImpl(final Class exceptionClass, final Class declaringClass,
                                    final String joinPointSignature) {
        m_declaringType = declaringClass;
        m_joinPointSignature = joinPointSignature;
        m_parameterType = exceptionClass;
        m_modifiers = exceptionClass.getModifiers();
        m_name = exceptionClass.getName();
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
        return m_modifiers;
    }

    /**
    * Returns the name (f.e. name of method of field).
    *
    * @return
    */
    public String getName() {
        return m_name;
    }

    /**
    * Returns the parameter type.
    *
    * @return the parameter type
    */
    public Class getParameterType() {
        return m_parameterType;
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

    /**
    * Creates a deep copy of the signature.
    *
    * @return a deep copy of the signature
    */
    public Signature newInstance() {
        return new CatchClauseSignatureImpl(m_parameterType, m_declaringType, m_joinPointSignature);
    }
}
