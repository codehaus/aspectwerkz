/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD-style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz;

import java.lang.reflect.Constructor;

/**
 * Contains a pair of the original method and the wrapper method if such a method exists.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class ConstructorTuple {
    private final Constructor m_wrapperConstructor;
    private final Constructor m_originalConstructor;
    private final Class m_declaringClass;

    /**
     * Creates a new tuple.
     *
     * @param wrapperConstructor
     * @param originalConstructor
     */
    public ConstructorTuple(Constructor wrapperConstructor, Constructor originalConstructor) {
        if (originalConstructor == null) {
            originalConstructor = wrapperConstructor;
        }
        if (wrapperConstructor.getDeclaringClass() != originalConstructor.getDeclaringClass()) {
            throw new RuntimeException(
                    wrapperConstructor.getName() + " and " + originalConstructor.getName()
                    + " does not have the same declaring class"
            );
        }
        m_declaringClass = wrapperConstructor.getDeclaringClass();
        m_wrapperConstructor = wrapperConstructor;
        m_wrapperConstructor.setAccessible(true);
        m_originalConstructor = originalConstructor;
        m_originalConstructor.setAccessible(true);
    }

    public boolean isWrapped() {
        return m_originalConstructor != null;
    }

    public Class getDeclaringClass() {
        return m_declaringClass;
    }

    public Constructor getWrapperConstructor() {
        return m_wrapperConstructor;
    }

    public Constructor getOriginalConstructor() {
        return m_originalConstructor;
    }

    public String getName() {
        return m_wrapperConstructor.getName();
    }
}
