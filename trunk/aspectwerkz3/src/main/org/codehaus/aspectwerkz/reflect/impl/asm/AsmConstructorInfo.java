/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.reflect.impl.asm;

import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.ConstructorInfo;
import org.codehaus.aspectwerkz.reflect.MethodInfo;
import org.codehaus.aspectwerkz.transform.inlining.AsmHelper;
import org.objectweb.asm.Type;

/**
 * Implementation of the ConstructorInfo interface for java.lang.reflect.*.
 * 
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public class AsmConstructorInfo extends AsmMemberInfo implements ConstructorInfo {

    /**
     * A list with the parameter type names.
     */
    private String[] m_parameterTypeNames = null;

    /**
     * A list with the exception type names.
     */
    private String[] m_exceptionTypeNames = null;

    /**
     * A list with the parameter types.
     */
    private ClassInfo[] m_parameterTypes = null;

    /**
     * A list with the exception types.
     */
    private ClassInfo[] m_exceptionTypes = null;

    /**
     * Creates a new method meta data instance.
     * 
     * @param method
     * @param declaringType
     * @param loader
     */
    AsmConstructorInfo(final MethodStruct method, final String declaringType, final ClassLoader loader) {
        super(method, declaringType, loader);
        Type[] argTypes = Type.getArgumentTypes(method.desc);
        m_parameterTypeNames = new String[argTypes.length];
        for (int i = 0; i < argTypes.length; i++) {
            m_parameterTypeNames[i] = argTypes[i].getClassName();
        }
        // FIXME: how to do exceptions?
        m_exceptionTypeNames = new String[] {};
    }

    /**
     * Returns the constructor info for the constructor specified.
     * 
     * @param constructorDesc
     * @param bytecode
     * @param loader
     * @return the constructor info
     */
    public static MethodInfo getConstructorInfo(final String constructorDesc, final byte[] bytecode, final ClassLoader loader) {
        String className = AsmClassInfo.retrieveClassNameFromBytecode(bytecode);
        AsmClassInfoRepository repository = AsmClassInfoRepository.getRepository(loader);
        ClassInfo classInfo = repository.getClassInfo(className);
        if (classInfo == null) {
            classInfo = AsmClassInfo.getClassInfo(bytecode, loader);
        }
        return classInfo.getMethod(AsmHelper.calculateConstructorHash(constructorDesc));
    }

    /**
     * Returns the parameter types.
     * 
     * @return the parameter types
     */
    public ClassInfo[] getParameterTypes() {
        if (m_parameterTypes == null) {
            m_parameterTypes = new ClassInfo[m_parameterTypeNames.length];
            for (int i = 0; i < m_parameterTypeNames.length; i++) {
                m_parameterTypes[i] = AsmClassInfo.createClassInfoFromStream(m_parameterTypeNames[i], m_loader);
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
            m_exceptionTypes = new ClassInfo[m_exceptionTypeNames.length];
            for (int i = 0; i < m_exceptionTypeNames.length; i++) {
                m_exceptionTypes[i] = AsmClassInfo.createClassInfoFromStream(m_exceptionTypeNames[i], m_loader);
            }
        }
        return m_exceptionTypes;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ConstructorInfo)) {
            return false;
        }
        ConstructorInfo constructorInfo = (ConstructorInfo) o;
        if (!m_declaringTypeName.equals(constructorInfo.getDeclaringType().getName().toString())) {
            return false;
        }
        if (!m_member.name.equals(constructorInfo.getName().toString())) {
            return false;
        }
        ClassInfo[] parameterTypes = constructorInfo.getParameterTypes();
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
        result = (29 * result) + m_declaringTypeName.hashCode();
        result = (29 * result) + m_member.name.hashCode();
        if (m_parameterTypes == null) {
            getParameterTypes();
        }
        for (int i = 0; i < m_parameterTypes.length; i++) {
            result = (29 * result) + m_parameterTypes[i].getName().toString().hashCode();
        }
        return result;
    }
}