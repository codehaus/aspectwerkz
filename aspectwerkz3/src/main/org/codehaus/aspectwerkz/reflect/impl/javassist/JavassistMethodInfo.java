/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.reflect.impl.javassist;

import org.codehaus.aspectwerkz.annotation.AnnotationInfo;
import org.codehaus.aspectwerkz.annotation.instrumentation.AttributeExtractor;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.MethodInfo;
import org.codehaus.aspectwerkz.transform.JavassistHelper;

import java.util.List;

import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

/**
 * Implementation of the MethodInfo interface for Javassist.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class JavassistMethodInfo extends JavassistCodeInfo implements MethodInfo {
    /**
     * The return type.
     */
    private ClassInfo m_returnType = null;

    /**
     * Creates a new method meta data instance.
     *
     * @param method
     * @param declaringType
     * @param loader
     * @param attributeExtractor
     */
    JavassistMethodInfo(
            final CtMethod method, final JavassistClassInfo declaringType, final ClassLoader loader,
            final AttributeExtractor attributeExtractor) {
        super(method, declaringType, loader, attributeExtractor);
        addAnnotations();
    }

    /**
     * Returns the method info for the method specified.
     *
     * @param method the method
     * @param loader the class loader
     * @return the method info
     */
    public static MethodInfo getMethodInfo(final CtMethod method, final ClassLoader loader) {
        CtClass declaringClass = method.getDeclaringClass();
        JavassistClassInfoRepository repository = JavassistClassInfoRepository.getRepository(loader);
        ClassInfo classInfo = repository.getClassInfo(declaringClass.getName());
        if (classInfo == null) {
            classInfo = JavassistClassInfo.getClassInfo(declaringClass, loader);
        }
        return classInfo.getMethod(calculateHash(method));
    }

    /**
     * Calculates the method hash.
     *
     * @param method
     * @return the hash
     */
    public static int calculateHash(final CtMethod method) {
        int hash = method.getName().hashCode();
        try {
            for (int i = 0; i < method.getParameterTypes().length; i++) {
                String name = method.getParameterTypes()[i].getName();
                name = JavassistHelper.convertJavassistTypeSignatureToReflectTypeSignature(name);
                hash = (17 * hash) + name.hashCode();
            }
        } catch (NotFoundException e) {
            throw new WrappedRuntimeException(e);
        }
        return hash;
    }

    /**
     * Returns the annotations.
     *
     * @return the annotations
     */
    public List getAnnotations() {
        return m_annotations;
    }

    /**
     * Returns the return type.
     *
     * @return the return type
     */
    public ClassInfo getReturnType() {
        if (m_returnType == null) {
            try {
                CtClass returnTypeClass = ((CtMethod)m_member).getReturnType();
                if (m_classInfoRepository.hasClassInfo(returnTypeClass.getName())) {
                    m_returnType = m_classInfoRepository.getClassInfo(returnTypeClass.getName());
                } else {
                    m_returnType = JavassistClassInfo.getClassInfo(returnTypeClass, (ClassLoader)m_loaderRef.get());
                    m_classInfoRepository.addClassInfo(m_returnType);
                }
            } catch (NotFoundException e) {
                e.printStackTrace();
            }
        }
        return m_returnType;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MethodInfo)) {
            return false;
        }
        MethodInfo methodInfo = (MethodInfo)o;
        if (!m_declaringType.getName().toString().equals(methodInfo.getDeclaringType().getName().toString())) {
            return false;
        }
        if (!m_member.getName().toString().equals(methodInfo.getName().toString())) {
            return false;
        }
        ClassInfo[] parameterTypes = methodInfo.getParameterTypes();
        if (m_parameterTypes.length != parameterTypes.length) {
            return false;
        }
        for (int i = 0; i < m_parameterTypes.length; i++) {
            if (!m_parameterTypes[i].getName().toString().equals(parameterTypes[i].getName().toString())) {
                return false;
            }
        }
        return true;
    }

    public int hashCode() {
        int result = 29;
        result = (29 * result) + m_declaringType.getName().toString().hashCode();
        result = (29 * result) + m_member.getName().toString().hashCode();
        if (m_parameterTypes == null) {
            getParameterTypes();
        }
        for (int i = 0; i < m_parameterTypes.length; i++) {
            result = (29 * result) + m_parameterTypes[i].getName().toString().hashCode();
        }
        return result;
    }

    /**
     * Adds annotations to the method info.
     */
    private void addAnnotations() {
        if (m_attributeExtractor == null) {
            return;
        }
        try {
            CtClass[] parameterTypes = ((CtBehavior)m_member).getParameterTypes();
            String[] parameterNames = new String[parameterTypes.length];
            for (int i = 0; i < parameterTypes.length; i++) {
                parameterNames[i] = parameterTypes[i].getName();
            }
            Object[] attributes = m_attributeExtractor.getMethodAttributes(getName(), parameterNames);
            for (int i = 0; i < attributes.length; i++) {
                Object attribute = attributes[i];
                if (attribute instanceof AnnotationInfo) {
                    m_annotations.add(attribute);
                }
            }
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
    }
}
