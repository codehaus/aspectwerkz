/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.reflect.impl.javassist;

import org.codehaus.aspectwerkz.annotation.instrumentation.AttributeExtractor;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.NotFoundException;

/**
 * Base class for the code members (method and constructor) for javassist.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
abstract class JavassistCodeInfo extends JavassistMemberInfo {
    /**
     * A list with the parameter types.
     */
    protected ClassInfo[] m_parameterTypes = null;

    /**
     * A list with the exception types.
     */
    protected ClassInfo[] m_exceptionTypes = null;

    /**
     * Creates a new method meta data instance.
     *
     * @param method
     * @param declaringType
     * @param loader
     * @param attributeExtractor
     */
    JavassistCodeInfo(final CtBehavior method, final JavassistClassInfo declaringType, final ClassLoader loader,
                      final AttributeExtractor attributeExtractor) {
        super(method, declaringType, loader, attributeExtractor);
    }

    /**
     * Returns the parameter types.
     *
     * @return the parameter types
     */
    public ClassInfo[] getParameterTypes() {
        if (m_parameterTypes == null) {
            try {
                CtClass[] parameterTypes = ((CtBehavior)m_member).getParameterTypes();
                m_parameterTypes = new ClassInfo[parameterTypes.length];
                for (int i = 0; i < parameterTypes.length; i++) {
                    CtClass parameterType = parameterTypes[i];
                    ClassInfo metaData;
                    if (m_classInfoRepository.hasClassInfo(parameterType.getName())) {
                        metaData = m_classInfoRepository.getClassInfo(parameterType.getName());
                    } else {
                        metaData = new JavassistClassInfo(parameterType, (ClassLoader)m_loaderRef.get());
                        m_classInfoRepository.addClassInfo(metaData);
                    }
                    m_parameterTypes[i] = metaData;
                }
            } catch (NotFoundException e) {
                e.printStackTrace();
            }
        }
        return m_parameterTypes;
    }

    /**
     * Returns the exception types.
     *
     * @return the exception types
     */
    public ClassInfo[] getExceptionTypes() {
        if (m_exceptionTypes == null) {
            try {
                CtClass[] exceptionTypes = ((CtBehavior)m_member).getExceptionTypes();
                m_exceptionTypes = new ClassInfo[exceptionTypes.length];
                for (int i = 0; i < exceptionTypes.length; i++) {
                    CtClass exceptionType = exceptionTypes[i];
                    ClassInfo metaData;
                    if (m_classInfoRepository.hasClassInfo(exceptionType.getName())) {
                        metaData = m_classInfoRepository.getClassInfo(exceptionType.getName());
                    } else {
                        metaData = new JavassistClassInfo(exceptionType, (ClassLoader)m_loaderRef.get());
                        m_classInfoRepository.addClassInfo(metaData);
                    }
                    m_exceptionTypes[i] = metaData;
                }
            } catch (NotFoundException e) {
                e.printStackTrace();
            }
        }
        return m_exceptionTypes;
    }
}
