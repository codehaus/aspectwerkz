/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.joinpoint;

import java.lang.reflect.Method;
import java.util.StringTokenizer;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.ObjectInputStream;

import org.apache.bcel.generic.Type;

import org.codehaus.aspectwerkz.AspectWerkz;
import org.codehaus.aspectwerkz.pointcut.CallerSidePointcut;
import org.codehaus.aspectwerkz.metadata.MethodMetaData;
import org.codehaus.aspectwerkz.metadata.ReflectionMetaDataMaker;
import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.transform.TransformationUtil;

/**
 * Matches well defined point of execution in the program where a method is
 * invoked. Stores meta data from the join point. I.e. a reference to
 * original object A method, name A type of the field etc. Handles the
 * invocation of the advices added to the join point.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class CallerSideJoinPoint implements JoinPoint {

    /**
     * The AspectWerkz system for this join point.
     */
    private transient AspectWerkz m_system;

    /**
     * The serial version uid for the class.
     */
    private static final long serialVersionUID = -8831127199517513612L;;

    /**
     * A reference to the caller class.
     */
    protected Class m_targetClass;

    /**
     * The caller method.
     */
    protected Method m_callerMethod;

    /**
     * The name of the callee method.
     */
    protected String m_calleeMethodName;

    /**
     * The name of the callee class.
     */
    protected String m_calleeClassName;

    /**
     * The signature for the callee method.
     */
    protected String m_calleeMethodSignature;

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
    protected String m_callerMethodName;

    /**
     * The signature for the caller method.
     */
    protected String m_callerMethodSignature;

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
     * Meta-data for the class.
     */
    protected ClassMetaData m_classMetaData;

    /**
     * Meta-data for the method.
     */
    protected MethodMetaData m_methodMetaData;

    /**
     * The UUID for the AspectWerkz system to use.
     */
    protected String m_uuid;

    /**
     * Creates a new CallerSideJoinPoint object.
     *
     * @param uuid the UUID for the AspectWerkz system to use
     * @param callerClass the caller class
     * @param callerMethodName the full caller method name (including the class name)
     * @param callerMethodSignature the caller method signature
     * @param calleeMethodName the full callee method name (including the class name)
     * @param calleeMethodSignature the callee method signature
     */
    public CallerSideJoinPoint(final String uuid,
                               final Class callerClass,
                               final String callerMethodName,
                               final String callerMethodSignature,
                               final String calleeMethodName,
                               final String calleeMethodSignature) {
        if (uuid == null) throw new IllegalArgumentException("uuid can not be null");
        if (callerClass == null) throw new IllegalArgumentException("original class can not be null");
        if (callerMethodName == null) throw new IllegalArgumentException("caller method name can not be null");
        if (callerMethodSignature == null) throw new IllegalArgumentException("caller signature can not be null");
        if (calleeMethodName == null) throw new IllegalArgumentException("callee method name can not be null");
        if (calleeMethodSignature == null) throw new IllegalArgumentException("callee signature can not be null");

        m_system = AspectWerkz.getSystem(uuid);
        m_system.initialize();

        m_uuid = uuid;
        m_targetClass = callerClass;
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
     * Invokes the next advice in the chain A when it reaches the end
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
                m_system.getAdvice(m_preAdvices[i]).doExecute(this);
            }
            catch (ArrayIndexOutOfBoundsException ex) {
                throw new RuntimeException(createAdvicesNotCorrectlyMappedMessage());
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
                m_system.getAdvice(m_postAdvices[i]).doExecute(this);
            }
            catch (ArrayIndexOutOfBoundsException ex) {
                throw new RuntimeException(createAdvicesNotCorrectlyMappedMessage());
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
     * Same as getCallerClass().
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
            m_calleeMethodReturnTypeName = Type.getReturnType(m_calleeMethodSignature).toString();
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
     * Returns the name of caller class.
     *
     * @return the caller class name
     */
    public String getCallerClassName() {
        return m_targetClass.getName();
    }

    /**
     * Returns the caller class.
     * Same as getTargetClass().
     *
     * @return the caller class
     */
    public Class getCallerClass() {
        return m_targetClass;
    }

    /**
     * Returns the parameter types for the method.
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
     *
     * @return the return type
     */
    public Class getCallerMethodReturnType() {
        if (m_callerMethodReturnType == null) {
            Type returnType = Type.getReturnType(m_callerMethodSignature);
            m_callerMethodReturnType = TransformationUtil.convertBcelTypeToClass(returnType);
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
            m_callerMethodReturnTypeName = Type.getReturnType(m_callerMethodSignature).toString();
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

                List pointcuts = m_system.getCallerSidePointcuts(getCalleeClassName(), m_methodMetaData);

                for (Iterator it = pointcuts.iterator(); it.hasNext();) {
                    CallerSidePointcut callerSidePointcut = (CallerSidePointcut)it.next();

                    int[] preAdviceIndexes = callerSidePointcut.getPreAdviceIndexes();
                    for (int j = 0; j < preAdviceIndexes.length; j++) {
                        preAdvices.add(new Integer(preAdviceIndexes[j]));
                    }

                    int[] postAdviceIndexes = callerSidePointcut.getPostAdviceIndexes();
                    for (int j = 0; j < postAdviceIndexes.length; j++) {
                        postAdvices.add(new Integer(postAdviceIndexes[j]));
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
        m_classMetaData = ReflectionMetaDataMaker.createClassMetaData(getTargetClass());
        m_methodMetaData = ReflectionMetaDataMaker.createMethodMetaData(
                getCalleeMethodName(),
                getCalleeMethodParameterTypes(),
                getCalleeMethodReturnType());
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

    /**
     * Provides custom deserialization.
     *
     * @param stream the object input stream containing the serialized object
     * @throws java.lang.Exception in case of failure
     */
    private void readObject(final ObjectInputStream stream) throws Exception {
        ObjectInputStream.GetField fields = stream.readFields();
        m_uuid = (String)fields.get("m_uuid", null);
        m_targetClass = (Class)fields.get("m_targetClass", null);
        m_callerMethod = (Method)fields.get("m_callerMethod", null);
        m_callerMethodName = (String)fields.get("m_callerMethodName", null);
        m_callerMethodSignature = (String)fields.get("m_callerMethodSignature", null);
        m_callerMethodParameterTypes = (Class[])fields.get("m_callerMethodParameterTypes", null);
        m_callerMethodParameterTypeNames = (String[])fields.get("m_callerMethodParameterTypeNames", null);
        m_callerMethodReturnType = (Class)fields.get("m_callerMethodReturnType", null);
        m_callerMethodReturnTypeName = (String)fields.get("m_callerMethodReturnTypeName", null);
        m_calleeMethodName = (String)fields.get("m_calleeMethodName", null);
        m_calleeMethodSignature = (String)fields.get("m_calleeMethodSignature", null);
        m_calleeClassName = (String)fields.get("m_calleeClassName", null);
        m_calleeMethodParameterTypes = (Class[])fields.get("m_calleeMethodParameterTypes", null);
        m_calleeMethodParameterTypeNames = (String[])fields.get("m_calleeMethodParameterTypeNames", null);
        m_calleeMethodReturnType = (Class)fields.get("m_calleeMethodReturnType", null);
        m_calleeMethodReturnTypeName = (String)fields.get("m_calleeMethodReturnTypeName", null);
        m_preAdvices = (int[])fields.get("m_preAdvices", null);
        m_postAdvices = (int[])fields.get("m_postAdvices", null);
        m_classMetaData = (ClassMetaData)fields.get("m_classMetaData", null);
        m_methodMetaData = (MethodMetaData)fields.get("m_fieldMetaData", null);
        m_initialized = fields.get("m_initialized", false);
        m_system = AspectWerkz.getSystem(m_uuid);
        m_system.initialize();
    }
}
