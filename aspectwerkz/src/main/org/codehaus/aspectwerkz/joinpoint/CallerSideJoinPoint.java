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
 * @version $Id: CallerSideJoinPoint.java,v 1.1.1.1 2003-05-11 15:14:25 jboner Exp $
 */
public class CallerSideJoinPoint implements JoinPoint {

    /**
     * The serial version uid for the class.
     */
    private static final long serialVersionUID = 5361657399009859300L;

    /**
     * A reference to the target class.
     */
    protected final Class m_targetClass;

    /**
     * The target method.
     */
    protected Method m_targetMethod;

    /**
     * The name of the method.
     */
    protected final String m_methodName;

    /**
     * The name of the class holding the method.
     */
    protected final String m_methodClassName;

    /**
     * The signature for the method.
     */
    protected final String m_signature;

    /**
     * The parameter types.
     */
    protected Class[] m_parameterTypes = null;

    /**
     * The parameter types names.
     */
    protected String[] m_parameterTypeNames = null;

    /**
     * The return type.
     */
    protected Class m_returnType = null;

    /**
     * The return type name.
     */
    protected String m_returnTypeName = null;

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
     * @param fullMethodName the full method name (including the class name)
     * @param signature the method signature
     */
    public CallerSideJoinPoint(final Class targetClass,
                             final String fullMethodName,
                             final String signature) {
        if (targetClass == null) throw new IllegalArgumentException("original class can not be null");
        if (fullMethodName == null) throw new IllegalArgumentException("method name can not be null");
        if (signature == null) throw new IllegalArgumentException("signature can not be null");

        AspectWerkz.initialize();

        m_targetClass = targetClass;

        StringTokenizer tokenizer = new StringTokenizer(
                fullMethodName,
                TransformationUtil.CALL_SIDE_DELIMITER);

        m_methodClassName = tokenizer.nextToken();
        m_methodName = tokenizer.nextToken();

        m_signature = signature;

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
     * Returns the method name.
     *
     * @return the method name
     */
    public String getMethodName() {
        return m_methodName;
    }

    /**
     * Returns the name of the class holding the method.
     *
     * @return the class name
     */
    public String getMethodClassName() {
        return m_methodClassName;
    }

    /**
     * Returns the parameter types for the method.
     * @todo does not represent array structures properly.
     *
     * @return the parameter types
     */
    public Class[] getParameterTypes() {
        if (m_parameterTypes == null) {
            Type[] parameterTypes = Type.getArgumentTypes(m_signature);
            m_parameterTypes = new Class[parameterTypes.length];
            for (int i = 0; i < parameterTypes.length; i++) {
                m_parameterTypes[i] = TransformationUtil.
                        convertBcelTypeToClass(parameterTypes[i]);
            }
        }
        return m_parameterTypes;
    }

    /**
     * Returns the parameter type names for the method.
     *
     * @return the parameter type names
     */
    public String[] getParameterTypeNames() {
        if (m_parameterTypeNames == null) {
            Type[] parameterTypes = Type.getArgumentTypes(m_signature);
            m_parameterTypeNames = new String[parameterTypes.length];
            for (int i = 0; i < parameterTypes.length; i++) {
                m_parameterTypeNames[i] = parameterTypes[i].toString();
            }
        }
        return m_parameterTypeNames;
    }

    /**
     * Returns the return type for the method.
     * @todo does not represent array structures properly.
     *
     * @return the return type
     */
    public Class getReturnType() {
        if (m_returnType == null) {
            Type returnType = Type.getReturnType(m_signature);
            m_returnType = TransformationUtil.convertBcelTypeToClass(returnType);
        }
        return m_returnType;
    }

    /**
     * Returns the return type name for the method.
     *
     * @return the return type name
     */
    public String getReturnTypeName() {
        if (m_returnTypeName == null) {
            m_returnTypeName = Type.getReturnType(m_signature).toString();
        }
        return m_returnTypeName;
    }

    /**
     * Returns the method signature.
     *
     * @return the method signature
     */
    public String getSignature() {
        return m_signature;
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
        m_metadata.setName(getMethodName());
        Class[] parameterTypes = getParameterTypes();
        String[] parameterTypeNames = new String[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            parameterTypeNames[i] = parameterTypes[i].getName();
        }
        m_metadata.setParameterTypes(parameterTypeNames);
        Class returnType = getReturnType();
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
        cause.append(getMethodClassName());
        cause.append("#");
        cause.append(getMethodName());
        cause.append(" are not correctly mapped");
        return cause.toString();
    }
}
