/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.reflect.impl.asm;

import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.MethodInfo;
import org.codehaus.aspectwerkz.transform.inlining.AsmHelper;
import org.codehaus.aspectwerkz.annotation.instrumentation.asm.AsmAnnotationHelper;
import org.objectweb.asm.Type;
import org.objectweb.asm.ClassReader;

import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.io.InputStream;

/**
 * ASM implementation of the MethodInfo interface.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public class AsmMethodInfo extends AsmMemberInfo implements MethodInfo {
    /**
     * The return type name.
     */
    private String m_returnTypeName = null;

    /**
     * A list with the parameter type names.
     */
    private String[] m_parameterTypeNames = null;

    /**
     * A list with the exception type names.
     */
    private String[] m_exceptionTypeNames = null;

    /**
     * The return type.
     */
    private ClassInfo m_returnType = null;

    /**
     * A list with the parameter types.
     */
    private ClassInfo[] m_parameterTypes = null;

    /**
     * A list with the exception types.
     */
    private ClassInfo[] m_exceptionTypes = null;

    /**
     * Creates a new method info instance.
     *
     * @param method
     * @param declaringType
     * @param loader
     */
    AsmMethodInfo(final MethodStruct method, final String declaringType, final ClassLoader loader) {
        super(method, declaringType, loader);

        m_returnTypeName = Type.getReturnType(method.desc).getClassName();
        Type[] argTypes = Type.getArgumentTypes(method.desc);
        m_parameterTypeNames = new String[argTypes.length];
        for (int i = 0; i < argTypes.length; i++) {
            m_parameterTypeNames[i] = argTypes[i].getClassName();
        }
        // FIXME: how to do exceptions? needed?
        m_exceptionTypeNames = new String[]{};
    }

    /**
     * Returns the method info for the method specified.
     *
     * @param methodName
     * @param methodDesc
     * @param bytecode
     * @param loader
     * @return the method info
     */
    public static MethodInfo getMethodInfo(final String methodName,
                                           final String methodDesc,
                                           final byte[] bytecode,
                                           final ClassLoader loader) {
        String className = AsmClassInfo.retrieveClassNameFromBytecode(bytecode);
        AsmClassInfoRepository repository = AsmClassInfoRepository.getRepository(loader);
        ClassInfo classInfo = repository.getClassInfo(className);
        if (classInfo == null) {
            classInfo = AsmClassInfo.getClassInfo(bytecode, loader);
        }
        return classInfo.getMethod(AsmHelper.calculateMethodHash(methodName, methodDesc));
    }

    /**
     * Returns the signature for the element.
     *
     * @return the signature for the element
     */
    public String getSignature() {
        return AsmHelper.getMethodDescriptor(this);
    }

    /**
     * Returns the return type.
     *
     * @return the return type
     */
    public ClassInfo getReturnType() {
        if (m_returnType == null) {
            m_returnType = AsmClassInfo.getClassInfo(m_returnTypeName, (ClassLoader) m_loaderRef.get());
        }
        return m_returnType;
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
                m_parameterTypes[i] = AsmClassInfo.getClassInfo(
                        m_parameterTypeNames[i],
                        (ClassLoader) m_loaderRef.get()
                );
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
                m_exceptionTypes[i] = AsmClassInfo.getClassInfo(
                        m_exceptionTypeNames[i],
                        (ClassLoader) m_loaderRef.get()
                );
            }
        }
        return m_exceptionTypes;
    }

    /**
     * Returns the annotations.
     *
     * @return the annotations
     */
    public List getAnnotations() {
        if (m_annotations == null) {
            try {
                InputStream in = null;
                ClassReader cr = null;
                try {
                    in = ((ClassLoader) m_loaderRef.get()).getResourceAsStream(
                                m_declaringTypeName.replace('.', '/') + ".class"
                    );
                    cr = new ClassReader(in);
                } finally {
                    try { in.close();} catch(Exception e) {;}
                }
                List annotations = new ArrayList();
                cr.accept(
                        new AsmAnnotationHelper.MethodAnnotationExtractor(
                                annotations, m_member.name, m_member.desc, (ClassLoader) m_loaderRef.get()
                        ),
                        AsmAnnotationHelper.ANNOTATIONS_ATTRIBUTES,
                        true
                );
                m_annotations = annotations;
            } catch (IOException e) {
                // unlikely to occur since ClassInfo relies on getResourceAsStream
                System.err.println(
                        "WARN - could not load " + m_declaringTypeName + " as a resource to retrieve annotations"
                );
                m_annotations = AsmClassInfo.EMPTY_LIST;
            }
        }
        return m_annotations;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MethodInfo)) {
            return false;
        }
        MethodInfo methodInfo = (MethodInfo) o;
        if (!m_declaringTypeName.equals(methodInfo.getDeclaringType().getName())) {
            return false;
        }
        if (!m_member.name.equals(methodInfo.getName())) {
            return false;
        }
        ClassInfo[] parameterTypes = methodInfo.getParameterTypes();
        if (m_parameterTypeNames.length != parameterTypes.length) {//check on names length for lazyness optim
            return false;
        }
        for (int i = 0; i < m_parameterTypeNames.length; i++) {
            if (!m_parameterTypeNames[i].equals(parameterTypes[i].getName())) {
                return false;
            }
        }
        return true;
    }

    public int hashCode() {
        int result = 29;
        result = (29 * result) + m_declaringTypeName.hashCode();
        result = (29 * result) + m_member.name.hashCode();
        for (int i = 0; i < m_parameterTypeNames.length; i++) {
            result = (29 * result) + m_parameterTypeNames[i].hashCode();
        }
        return result;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer(m_declaringTypeName);
        sb.append('.').append(m_member.name);
        sb.append(m_member.desc);
        return sb.toString();
    }
}