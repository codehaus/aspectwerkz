/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.joinpoint.impl;

import org.codehaus.aspectwerkz.joinpoint.Signature;
import org.codehaus.aspectwerkz.transform.TransformationConstants;

import java.lang.reflect.Modifier;

/**
 * The class static initializer signature
 *
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class StaticInitializerSignatureImpl implements Signature {

    private final static int CLINIT_MODIFIERS = Modifier.STATIC;//TODO whatelse

    private final Class m_declaringType;

    public StaticInitializerSignatureImpl(Class declaringType) {
        m_declaringType = declaringType;
    }

    public Class getDeclaringType() {
        return m_declaringType;
    }

    public int getModifiers() {
        return CLINIT_MODIFIERS;
    }

    public String getName() {
        return TransformationConstants.CLINIT_METHOD_NAME;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(m_declaringType.getName());
        sb.append('.');
        sb.append(TransformationConstants.CLINIT_METHOD_NAME);
        return sb.toString();
    }
}
