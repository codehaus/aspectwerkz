/*
 * AspectWerkz - a dynamic, lightweight and high-performant AOP/AOSD framework for Java.
 * Copyright (C) 2002-2003  Jonas Bonér. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.codehaus.aspectwerkz.joinpoint;

import java.lang.reflect.Method;
import java.util.StringTokenizer;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.bcel.generic.Type;

import org.codehaus.aspectwerkz.AspectWerkz;
import org.codehaus.aspectwerkz.Aspect;
import org.codehaus.aspectwerkz.pointcut.CallerSidePointcut;
import org.codehaus.aspectwerkz.definition.metadata.MethodMetaData;
import org.codehaus.aspectwerkz.transform.TransformationUtil;

/**
 * Matches well defined point of execution in the program where a method is
 * invoked. Stores meta data from the join point. I.e. a reference to
 * original object and method, name and type of the field etc. Handles the
 * invocation of the advices added to the join point.
 *
 * @todo if a parameter type or return type is an array => always returned as Object[] (fix bug in TransformationUtil.convertBcelTypeToClass(Type)
 *
 * @author <a href="mailto:jboner@acm.org">Jonas Bonér</a>
 * @version $Id: CallerSideJoinPoint.java,v 1.2 2003-05-14 19:44:59 jboner Exp $
 */
public class CallerSideJoinPoint implements JoinPoint {

    /**
     * The serial version uid for the class.
     */
    private static final long serialVersionUID = -7017159488344181865L;

    /**
     * A reference to the caller class.
     */
    protected final Class m_targetClass;

    /**
     * The caller method.
     */
    protected Method m_callerMethod;

    /**
     * The name of the callee method.
     */
    protected final String m_calleeMethodName;

    /**
     * The name of the callee class.
     */
    protected final String m_calleeClassName;

    /**
     * The signature for the callee method.
     */
    protected final String m_calleeMethodSignature;

    /**
     * The callee method parameter types.
     */
    protected Class[] m_calleeMethodParameterTypes = null;

    /**
     * The callee method parameter types names.
     */
    protected String[] m_calleeMethodParameterTypeNames = null;

    /**
     * The callee method return type.
     */
    protected Class m_calleeMethodReturnType = null;

    /**
     * The callee method return type name.
     */
    protected String m_calleeMethodReturnTypeName = null;

    /**
     * The name of the caller method.
     */
    protected final String m_callerMethodName;

    /**
     * The signature for the caller method.
     */
    protected final String m_callerMethodSignature;

    /**
     * The caller method parameter types.
     */
    protected Class[] m_callerMethodParameterTypes = null;

    /**
     * The caller method parameter types names.
     */
    protected String[] m_callerMethodParameterTypeNames = null;

    /**
     * The caller method return type.
     */
    protected Class m_callerMethodReturnType = null;

    /**
     * The caller method return type name.
     */
    protected String m_callerMethodReturnTypeName = null;

    /**
     * The pre advices applied to the join point.
     */
    protected int[] m_preAdvices = new int[0];

    /**
     * The post advices applied to the join point.
     */
    protected int[] m_postAdvices = new int[0];

    /**
     * Marks the join point as initialized.
     */
    protected boolean m_initialized = false;

    /**
     * Meta-data for the method.
     */
    protected MethodMetaData m_metadata;

    /**
     * Creates a new CallerSideJoinPoint object.
     *
     * @param targetClass the original class
     * @param callerMethodName the full caller method name (including the class name)
     * @param callerMethodSignature the caller method signature
     * @param calleeMethodName the full callee method name (including the class name)
     * @param calleeMethodSignature the callee method signature
     */
    public CallerSideJoinPoint(final Class targetClass,
                               final String callerMethodName,
                               final String callerMethodSignature,
                               final String calleeMethodName,
                               final String calleeMethodSignature) {
        if (targetClass == null) throw new IllegalArgumentException("original class can not be null");
        if (callerMethodName == null) throw new IllegalArgumentException("caller method name can not be null");
        if (callerMethodSignature == null) throw new IllegalArgumentException("caller signature can not be null");
        if (calleeMethodName == null) throw new IllegalArgumentException("callee method name can not be null");
        if (calleeMethodSignature == null) throw new IllegalArgumentException("callee signature can not be null");

        AspectWerkz.initialize();

        m_targetClass = targetClass;
        m_callerMethodName = callerMethodName;
        m_callerMethodSignature = callerMethodSignature;

        StringTokenizer tokenizer = new StringTokenizer(
                calleeMethodName,
                TransformationUtil.CALL_SIDE_DELIMITER);
        m_calleeClassName = tokenizer.nextToken();
        m_calleeMethodName = tokenizer.nextToken();
        m_calleeMethodSignature = calleeMethodSignature;

        createMetaData();
    }

    /**
     * To be called instead of proceed() when a new thread is spawned.
     * Otherwise the result is unpredicable.
     *
     * @return the result from the next invocation
     * @throws Throwable
     */
    public Object proceedInNewThread() throws Throwable {
        return null;
    }

    /**
     * Invokes the next advice in the chain and when it reaches the end
     * of the chain the original method.
     *
     * @return the result from the next invocation
     * @throws Throwable
     */
    public Object proceed() throws Throwable {
        return null;
    }

    /**
     * Invokes the next pre advice in the chain until it reaches the end.
     */
    public void pre() throws Throwable {
        if (!m_initialized) {
            loadAdvices();
        }
        for (int i = 0, j = m_preAdvices.length; i < j; i++) {
            try {
                AspectWerkz.getAdvice(m_preAdvices[i]).doExecute(this);
            }
            catch (ArrayIndexOutOfBoundsException ex) {
                throw new RuntimeException(
                        createAdvicesNotCorrectlyMappedMessage());
            }
        }
    }

    /**
     * Invokes the next post advice in the chain until it reaches the end.
     */
    public void post() throws Throwable {
        if (!m_initialized) {
            loadAdvices();
        }
        for (int i = m_postAdvices.length - 1; i >= 0; i--) {
            try {
                AspectWerkz.getAdvice(m_postAdvices[i]).doExecute(this);
            }
            catch (ArrayIndexOutOfBoundsException ex) {
                throw new RuntimeException(
                        createAdvicesNotCorrectlyMappedMessage());
            }
        }
    }

    /**
     * Returns the target object.
     *
     * @return the target object
     */
    public Object getTargetObject() {
        return null;
    }

    /**
     * Returns the target class.
     *
     * @return the target class
     */
    public Class getTargetClass() {
        return m_targetClass;
    }

    /**
     * Returns the callee method name.
     *
     * @return the callee method name
     */
    public String getCalleeMethodName() {
        return m_calleeMethodName;
    }

    /**
     * Returns the name of callee class.
     *
     * @return the callee class name
     */
    public String getCalleeClassName() {
        return m_calleeClassName;
    }

    /**
     * Returns the parameter types for the callee method.
     * @todo does not represent array structures properly.
     *
     * @return the parameter types
     */
    public Class[] getCalleeMethodParameterTypes() {
        if (m_calleeMethodParameterTypes == null) {
            Type[] parameterTypes = Type.getArgumentTypes(m_calleeMethodSignature);
            m_calleeMethodParameterTypes = new Class[parameterTypes.length];
            for (int i = 0; i < parameterTypes.length; i++) {
                m_calleeMethodParameterTypes[i] = TransformationUtil.
                        convertBcelTypeToClass(parameterTypes[i]);
            }
        }
        return m_calleeMethodParameterTypes;
    }

    /**
     * Returns the parameter type names for the callee method.
     *
     * @return the parameter type names
     */
    public String[] getCalleeMethodParameterTypeNames() {
        if (m_calleeMethodParameterTypeNames == null) {
            Type[] parameterTypes = Type.getArgumentTypes(m_calleeMethodSignature);
            m_calleeMethodParameterTypeNames = new String[parameterTypes.length];
            for (int i = 0; i < parameterTypes.length; i++) {
                m_calleeMethodParameterTypeNames[i] = parameterTypes[i].toString();
            }
        }
        return m_calleeMethodParameterTypeNames;
    }

    /**
     * Returns the return type for the callee method.
     * @todo does not represent array structures properly.
     *
     * @return the return type
     */
    public Class getCalleeMethodReturnType() {
        if (m_calleeMethodReturnType == null) {
            Type returnType = Type.getReturnType(m_calleeMethodSignature);
            m_calleeMethodReturnType =
                    TransformationUtil.convertBcelTypeToClass(returnType);
        }
        return m_calleeMethodReturnType;
    }

    /**
     * Returns the return type name for the callee method.
     *
     * @return the return type name
     */
    public String getCalleeMethodReturnTypeName() {
        if (m_calleeMethodReturnTypeName == null) {
            m_calleeMethodReturnTypeName =
                    Type.getReturnType(m_calleeMethodSignature).toString();
        }
        return m_calleeMethodReturnTypeName;
    }

    /**
     * Returns the callee method signature.
     *
     * @return the callee method signature
     */
    public String getCalleeMethodSignature() {
        return m_calleeMethodSignature;
    }

    /**
     * Returns the caller method name.
     *
     * @return the caller method name
     */
    public String getCallerMethodName() {
        return m_callerMethodName;
    }

    /**
     * Returns the parameter types for the method.
     * @todo does not represent array structures properly.
     *
     * @return the parameter types
     */
    public Class[] getCallerMethodParameterTypes() {
        if (m_callerMethodParameterTypes == null) {
            Type[] parameterTypes = Type.getArgumentTypes(m_callerMethodSignature);
            m_callerMethodParameterTypes = new Class[parameterTypes.length];
            for (int i = 0; i < parameterTypes.length; i++) {
                m_callerMethodParameterTypes[i] = TransformationUtil.
                        convertBcelTypeToClass(parameterTypes[i]);
            }
        }
        return m_callerMethodParameterTypes;
    }

    /**
     * Returns the parameter type names for the caller method.
     *
     * @return the parameter type names
     */
    public String[] getCallerMethodParameterTypeNames() {
        if (m_callerMethodParameterTypeNames == null) {
            Type[] parameterTypes = Type.getArgumentTypes(m_callerMethodSignature);
            m_callerMethodParameterTypeNames = new String[parameterTypes.length];
            for (int i = 0; i < parameterTypes.length; i++) {
                m_callerMethodParameterTypeNames[i] = parameterTypes[i].toString();
            }
        }
        return m_callerMethodParameterTypeNames;
    }

    /**
     * Returns the return type for the caller method.
     * @todo does not represent array structures properly.
     *
     * @return the return type
     */
    public Class getCallerMethodReturnType() {
        if (m_callerMethodReturnType == null) {
            Type returnType = Type.getReturnType(m_callerMethodSignature);
            m_callerMethodReturnType =
                    TransformationUtil.convertBcelTypeToClass(returnType);
        }
        return m_callerMethodReturnType;
    }

    /**
     * Returns the return type name for the caller method.
     *
     * @return the return type name
     */
    public String getCallerMethodReturnTypeName() {
        if (m_callerMethodReturnTypeName == null) {
            m_callerMethodReturnTypeName =
                    Type.getReturnType(m_callerMethodSignature).toString();
        }
        return m_callerMethodReturnTypeName;
    }

    /**
     * Returns the caller method signature.
     *
     * @return the caller method signature
     */
    public String getCallerMethodSignature() {
        return m_callerMethodSignature;
    }

    /**
     * Loads the advices for this pointcut.
     */
    private void loadAdvices() {
        synchronized (m_preAdvices) {
            synchronized (m_postAdvices) {

                List preAdvices = new ArrayList();
                List postAdvices = new ArrayList();
                List aspects = AspectWerkz.getAspects(m_targetClass.getName());

                for (Iterator it = aspects.iterator(); it.hasNext();) {
                    Aspect aspect = (Aspect)it.next();

                    CallerSidePointcut[] pointcuts =
                            aspect.getCallerSidePointcuts(m_metadata);

                    for (int i = 0; i < pointcuts.length; i++) {
                        int[] advices = pointcuts[i].getPreAdviceIndexes();
                        for (int j = 0; j < advices.length; j++) {
                            preAdvices.add(new Integer(advices[j]));
                        }
                    }

                    for (int i = 0; i < pointcuts.length; i++) {
                        int[] advices = pointcuts[i].getPostAdviceIndexes();
                        for (int j = 0; j < advices.length; j++) {
                            postAdvices.add(new Integer(advices[j]));
                        }
                    }
                }

                m_preAdvices = new int[preAdvices.size()];
                int i = 0;
                for (Iterator it = preAdvices.iterator(); it.hasNext(); i++) {
                    m_preAdvices[i] = ((Integer)it.next()).intValue();
                }
                m_postAdvices = new int[postAdvices.size()];
                i = 0;
                for (Iterator it = postAdvices.iterator(); it.hasNext(); i++) {
                    m_postAdvices[i] = ((Integer)it.next()).intValue();
                }

                m_initialized = true;
            }
        }
    }

    /**
     * Creates meta-data for the join point.
     */
    protected void createMetaData() {
        m_metadata = new MethodMetaData();
        m_metadata.setName(getCalleeMethodName());
        Class[] parameterTypes = getCalleeMethodParameterTypes();
        String[] parameterTypeNames = new String[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            parameterTypeNames[i] = parameterTypes[i].getName();
        }
        m_metadata.setParameterTypes(parameterTypeNames);
        Class returnType = getCalleeMethodReturnType();
        if (returnType == null) {
            m_metadata.setReturnType("void");
        }
        else {
            m_metadata.setReturnType(returnType.getName());
        }
    }

    /**
     * Creates an advices not correctly mapped message.
     *
     * @return the message
     */
    protected String createAdvicesNotCorrectlyMappedMessage() {
        StringBuffer cause = new StringBuffer();
        cause.append("advices for ");
        cause.append(getCalleeClassName());
        cause.append("#");
        cause.append(getCalleeMethodName());
        cause.append(" are not correctly mapped");
        return cause.toString();
    }
}
