/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.joinpoint.management;

import org.codehaus.aspectwerkz.System;
import org.codehaus.aspectwerkz.SystemLoader;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.FieldSignature;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public abstract class JoinPointBase implements JoinPoint {

    protected final String m_uuid;
    protected final Class m_targetClass;
    protected final int m_type;
    protected final String m_typeAsString;
    protected System m_system;

    protected final AroundAdviceExecutor m_aroundAdviceExecutor;
    protected final BeforeAdviceExecutor m_beforeAdviceExecutor;
    protected final AfterAdviceExecutor m_afterAdviceExecutor;

    protected Object m_targetInstance;

    /**
     * @param uuid
     * @param type
     * @param targetClass
     * @param aroundAdviceExecutor
     * @param beforeAdviceExecutor
     * @param afterAdviceExecutor
     */
    public JoinPointBase(
            final String uuid,
            final int type,
            final Class targetClass,
            final AroundAdviceExecutor aroundAdviceExecutor,
            final BeforeAdviceExecutor beforeAdviceExecutor,
            final AfterAdviceExecutor afterAdviceExecutor) {
        m_uuid = uuid;
        m_type = type;
        m_typeAsString = getJoinPointTypeAsString(type);
        m_targetClass = targetClass;
        m_aroundAdviceExecutor = aroundAdviceExecutor;
        m_beforeAdviceExecutor = beforeAdviceExecutor;
        m_afterAdviceExecutor = afterAdviceExecutor;
        m_system = SystemLoader.getSystem(m_uuid);
    }

    /**
     * Returns the target instance ('this'). If the join point is executing in a static context it returns null.
     *
     * @return the target instance
     */
    public Object getTargetInstance() {
        return m_targetInstance;
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
     * Returns the join point type.
     *
     * @return the type
     */
    public String getType() {
        return m_typeAsString;
    }

    /**
     * Sets the target instance.
     *
     * @param targetInstance the target instance
     */
    public void setTargetInstance(final Object targetInstance) {
        m_targetInstance = targetInstance;
    }

    /**
     * Sets the join point type to a string representation.
     *
     * @param type the type
     */
    protected String getJoinPointTypeAsString(final int type) {
        if (type == JoinPointType.METHOD_EXECUTION) {
            return JoinPoint.METHOD_EXECUTION;
        }
        else if (type == JoinPointType.METHOD_CALL) {
            return JoinPoint.METHOD_CALL;
        }
        else if (type == JoinPointType.CONSTRUCTOR_EXECUTION) {
            return JoinPoint.CONSTRUCTOR_EXECUTION;
        }
        else if (type == JoinPointType.CONSTRUCTOR_CALL) {
            return JoinPoint.CONSTRUCTOR_CALL;
        }
        else if (type == JoinPointType.FIELD_SET) {
            return JoinPoint.FIELD_SET;
        }
        else if (type == JoinPointType.FIELD_GET) {
            return JoinPoint.FIELD_GET;
        }
        else if (type == JoinPointType.HANDLER) {
            return JoinPoint.CATCH_CLAUSE;
        }
        else if (type == JoinPointType.STATIC_INITALIZATION) {
            return JoinPoint.STATIC_INITALIZATION;
        }
        else {
            throw new RuntimeException("join point type [" + type + "] is not a valid type");
        }
    }

    /**
     * Invokes the original method.
     *
     * @param joinPoint the join point instance
     * @return the result from the method invocation
     * @throws Throwable the exception from the original method
     */
    public static Object invokeTargetMethod(final JoinPoint joinPoint) throws Throwable {
        MethodSignatureImpl signature = (MethodSignatureImpl)joinPoint.getSignature();
        Method targetMethod = signature.getMethodTuple().getOriginalMethod();
        Object[] parameterValues = signature.getParameterValues();
        Object targetInstance = joinPoint.getTargetInstance();
        try {
            return targetMethod.invoke(targetInstance, parameterValues);
        }
        catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

    /**
     * Invokes the prefixed constructor.
     *
     * @param joinPoint the join point instance
     * @return the newly created instance
     * @throws Throwable the exception from the original constructor
     */
    public static Object invokeTargetConstructorExecution(final JoinPoint joinPoint) throws Throwable {
        ConstructorSignatureImpl signature = (ConstructorSignatureImpl)joinPoint.getSignature();
        Constructor targetConstructor = signature.getConstructorTuple().getOriginalConstructor();
        Object[] parameterValues = signature.getParameterValues();
        int length = parameterValues.length;
        Object[] fakeParameterValues = new Object[length + 1];
        java.lang.System.arraycopy(parameterValues, 0, fakeParameterValues, 0, length);
        fakeParameterValues[length] = null;
        try {
            return targetConstructor.newInstance(fakeParameterValues);
        }
        catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

    /**
     * Invokes the original constructor.
     *
     * @param joinPoint the join point instance
     * @return the newly created instance
     * @throws Throwable the exception from the original constructor
     */
    public static Object invokeTargetConstructorCall(final JoinPoint joinPoint) throws Throwable {
        ConstructorSignatureImpl signature = (ConstructorSignatureImpl)joinPoint.getSignature();
        Constructor targetConstructor = signature.getConstructorTuple().getWrapperConstructor();
        Object[] parameterValues = signature.getParameterValues();
        try {
            return targetConstructor.newInstance(parameterValues);
        }
        catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

    /**
     * Sets the target field.
     *
     * @param joinPoint the join point instance
     * @throws Throwable the exception from the original method
     */
    public static void setTargetField(final JoinPoint joinPoint) throws Throwable {
        FieldSignature signature = (FieldSignature)joinPoint.getSignature();
        Field targetField = signature.getField();
        Object fieldValue = signature.getFieldValue();
        Object targetInstance = joinPoint.getTargetInstance();
        targetField.set(targetInstance, fieldValue);
    }

    /**
     * Gets the target field.
     *
     * @param joinPoint the join point instance
     * @return the target field
     * @throws Throwable the exception from the original method
     */
    public static Object getTargetField(final JoinPoint joinPoint) throws Throwable {
        FieldSignature signature = (FieldSignature)joinPoint.getSignature();
        Field targetField = signature.getField();
        Object targetInstance = joinPoint.getTargetInstance();
        return targetField.get(targetInstance);
    }
}
