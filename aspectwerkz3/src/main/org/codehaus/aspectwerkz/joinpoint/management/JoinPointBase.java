/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.joinpoint.management;

import org.codehaus.aspectwerkz.AspectSystem;
import org.codehaus.aspectwerkz.SystemLoader;
import org.codehaus.aspectwerkz.expression.CflowExpressionVisitorRuntime;
import org.codehaus.aspectwerkz.expression.ExpressionContext;
import org.codehaus.aspectwerkz.expression.PointcutType;
import org.codehaus.aspectwerkz.joinpoint.FieldSignature;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.joinpoint.Rtti;
import org.codehaus.aspectwerkz.joinpoint.impl.ConstructorRttiImpl;
import org.codehaus.aspectwerkz.joinpoint.impl.ConstructorSignatureImpl;
import org.codehaus.aspectwerkz.joinpoint.impl.FieldRttiImpl;
import org.codehaus.aspectwerkz.joinpoint.impl.MethodRttiImpl;
import org.codehaus.aspectwerkz.joinpoint.impl.MethodSignatureImpl;

import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Base class for the join point implementations.
 * 
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur </a>
 */
public abstract class JoinPointBase implements JoinPoint, Serializable {
    protected Class m_targetClass;

    protected int m_type;

    protected String m_typeAsString;

    protected transient AspectSystem m_system;

    protected boolean m_checkCflow;

    protected AroundAdviceExecutor m_aroundAdviceExecutor;

    protected BeforeAdviceExecutor m_beforeAdviceExecutor;

    protected AfterAdviceExecutor m_afterAdviceExecutor;

    //protected transient WeakReference m_targetInstanceRef;//AW-265

    protected Map m_metaData = new HashMap();

    protected PointcutType m_pointcutType;

    protected transient JoinPointMetaData m_joinPointMetaData;

    /**
     * Creates a new join point base instance.
     * 
     * @param type
     * @param targetClass
     * @param joinPointMetaData
     * @param aroundAdviceExecutor
     * @param beforeAdviceExecutor
     * @param afterAdviceExecutor
     */
    public JoinPointBase(final int type,
                         final Class targetClass,
                         final JoinPointMetaData joinPointMetaData,
                         final AroundAdviceExecutor aroundAdviceExecutor,
                         final BeforeAdviceExecutor beforeAdviceExecutor,
                         final AfterAdviceExecutor afterAdviceExecutor) {
        m_type = type;
        m_typeAsString = getJoinPointTypeAsString(type);
        m_pointcutType = getPointcutType(type);
        m_targetClass = targetClass;
        m_checkCflow = joinPointMetaData.cflowExpressions.size() > 0;
        m_joinPointMetaData = joinPointMetaData;
        m_aroundAdviceExecutor = aroundAdviceExecutor;
        m_beforeAdviceExecutor = beforeAdviceExecutor;
        m_afterAdviceExecutor = afterAdviceExecutor;
        m_system = SystemLoader.getSystem(targetClass.getClassLoader());
    }

    /**
     * Creates a new join point base instance.
     * 
     * @param uuid
     * @param type
     * @param targetClass
     * @param joinPointMetaData
     * @param aroundAdviceExecutor
     * @param beforeAdviceExecutor
     * @param afterAdviceExecutor
     */
    public JoinPointBase(final String uuid,
                         final int type,
                         final Class targetClass,
                         final JoinPointMetaData joinPointMetaData,
                         final AroundAdviceExecutor aroundAdviceExecutor,
                         final BeforeAdviceExecutor beforeAdviceExecutor,
                         final AfterAdviceExecutor afterAdviceExecutor) {
        this(type, targetClass, joinPointMetaData, aroundAdviceExecutor, beforeAdviceExecutor, afterAdviceExecutor);
    }

    /**
     * Creates a new join point base instance.
     * 
     * @param uuid
     * @param type
     * @param targetClass
     * @param cflow
     * @param ctx
     * @param aroundAdviceExecutor
     * @param beforeAdviceExecutor
     * @param afterAdviceExecutor
     */
    public JoinPointBase(final String uuid,
                         final int type,
                         final Class targetClass,
                         final List cflow,
                         ExpressionContext ctx,
                         final AroundAdviceExecutor aroundAdviceExecutor,
                         final BeforeAdviceExecutor beforeAdviceExecutor,
                         final AfterAdviceExecutor afterAdviceExecutor) {
        //TODO clean me
        m_type = type;
        m_typeAsString = getJoinPointTypeAsString(type);
        m_pointcutType = getPointcutType(type);
        m_targetClass = targetClass;
        m_joinPointMetaData = new JoinPointMetaData();
        m_joinPointMetaData.expressionContext = ctx;
        m_joinPointMetaData.cflowExpressions = cflow;
        m_checkCflow = m_joinPointMetaData.cflowExpressions.size() > 0;
        m_aroundAdviceExecutor = aroundAdviceExecutor;
        m_beforeAdviceExecutor = beforeAdviceExecutor;
        m_afterAdviceExecutor = afterAdviceExecutor;
        m_system = SystemLoader.getSystem(targetClass.getClassLoader());
    }

    /**
     * Resets the join point. <p/>Will restart the execution chain of advice.
     */
    public void reset() {
        m_aroundAdviceExecutor.reset();
    }

    /**
     * Returns metadata matchingn a specfic key.
     * 
     * @param key the key to the metadata
     * @return the value
     */
    public Object getMetaData(final Object key) {
        return m_metaData.get(key);
    }

    /**
     * Adds metadata.
     * 
     * @param key the key to the metadata
     * @param value the value
     */
    public void addMetaData(final Object key, final Object value) {
        m_metaData.put(key, value);
    }

    /**
      * Returns the callee instance.
      *
      * @return the callee instance
      */
     public Object getCallee() {
        throw new UnsupportedOperationException("method not supported in 1.0");
    }

     /**
      * Returns the caller instance.
      *
      * @return the caller instance
      */
     public Object getCaller() {
         throw new UnsupportedOperationException("method not supported in 1.0");
     }

     /**
      * Returns the 'this' instance (the one currently executing).
      *
      * @return 'this'
      */
     public Object getThis() {
         throw new UnsupportedOperationException("method not supported in 1.0");
     }

     /**
      * Returns the caller class.
      *
      * @return the caller class
      */
     public Class getCallerClass() {
         throw new UnsupportedOperationException("method not supported in 1.0");
     }

     /**
     * Invokes the original method - execution context.
     * 
     * @param joinPoint the join point instance
     * @return the result from the method invocation
     * @throws Throwable the exception from the original method
     */
    public static Object invokeTargetMethodExecution(final JoinPoint joinPoint) throws Throwable {
        MethodSignatureImpl signature = (MethodSignatureImpl) joinPoint.getSignature();
        MethodRttiImpl rtti = (MethodRttiImpl) joinPoint.getRtti();
        Method targetMethod = signature.getMethodTuple().getOriginalMethod();
        Object[] parameterValues = rtti.getParameterValues();
        Object targetInstance = joinPoint.getTarget();
        try {
            return targetMethod.invoke(targetInstance, parameterValues);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

    /**
     * Invokes the original method - call context.
     * 
     * @param joinPoint the join point instance
     * @return the result from the method invocation
     * @throws Throwable the exception from the original method
     */
    public static Object invokeTargetMethodCall(final JoinPoint joinPoint) throws Throwable {
        MethodSignatureImpl signature = (MethodSignatureImpl) joinPoint.getSignature();
        MethodRttiImpl rtti = (MethodRttiImpl) joinPoint.getRtti();
        Method targetMethod = signature.getMethodTuple().getWrapperMethod();
        Object[] parameterValues = rtti.getParameterValues();
        Object targetInstance = joinPoint.getTarget();
        try {
            return targetMethod.invoke(targetInstance, parameterValues);
        } catch (InvocationTargetException e) {
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
        ConstructorSignatureImpl signature = (ConstructorSignatureImpl) joinPoint.getSignature();
        ConstructorRttiImpl rtti = (ConstructorRttiImpl) joinPoint.getRtti();
        Constructor targetConstructor = signature.getConstructorTuple().getOriginalConstructor();
        Object[] parameterValues = rtti.getParameterValues();
        int length = parameterValues.length;
        Object[] fakeParameterValues = new Object[length + 1];
        java.lang.System.arraycopy(parameterValues, 0, fakeParameterValues, 0, length);
        fakeParameterValues[length] = null;
        try {
            return targetConstructor.newInstance(fakeParameterValues);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

    /**
     * Invokes the original constructor.
     * 
     * @param joinPoint the join point instance
     * @return the newly created instance
     * @throws Throwable the exception from the original constructor TODO: FIX BUG - When a constructor has both a CALL
     *             and EXECUTION join point, only the CALL will be executed, redirecting to the wrapper constructor
     */
    public static Object invokeTargetConstructorCall(final JoinPoint joinPoint) throws Throwable {
        ConstructorSignatureImpl signature = (ConstructorSignatureImpl) joinPoint.getSignature();
        ConstructorRttiImpl rtti = (ConstructorRttiImpl) joinPoint.getRtti();
        Object[] parameterValues = rtti.getParameterValues();
        Constructor wrapperConstructor = signature.getConstructorTuple().getWrapperConstructor();
        Constructor originalConstructor = signature.getConstructorTuple().getOriginalConstructor();
        if (originalConstructor.equals(wrapperConstructor)) {
            try {
                return wrapperConstructor.newInstance(parameterValues);
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        } else {
            java.lang.System.err
                    .println("WARNING: When a constructor has both a CALL and EXECUTION join point, only the CALL will be executed. This limitation is due to a bug that has currently not been fixed yet.");
            Object[] parameters = new Object[parameterValues.length + 1];
            for (int i = 0; i < parameterValues.length; i++) {
                parameters[i] = parameterValues[i];
            }
            try {
                return originalConstructor.newInstance(parameters);
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        }
    }

    /**
     * Sets the target field.
     * 
     * @param joinPoint the join point instance
     * @throws Throwable the exception from the original method
     */
    public static void setTargetField(final JoinPoint joinPoint) throws Throwable {
        FieldSignature signature = (FieldSignature) joinPoint.getSignature();
        FieldRttiImpl rtti = (FieldRttiImpl) joinPoint.getRtti();
        Field targetField = signature.getField();
        Object fieldValue = rtti.getFieldValue();
        Object targetInstance = joinPoint.getTarget();
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
        FieldSignature signature = (FieldSignature) joinPoint.getSignature();
        Field targetField = signature.getField();
        Object targetInstance = joinPoint.getTarget();
        return targetField.get(targetInstance);
    }

    /**
     * Sets the join point type to a string representation.
     * 
     * @param type the type
     */
    public static String getJoinPointTypeAsString(final int type) {
        switch (type) {
            case JoinPointType.METHOD_EXECUTION:
                return JoinPoint.METHOD_EXECUTION;
            case JoinPointType.METHOD_CALL:
                return JoinPoint.METHOD_CALL;
            case JoinPointType.CONSTRUCTOR_EXECUTION:
                return JoinPoint.CONSTRUCTOR_EXECUTION;
            case JoinPointType.CONSTRUCTOR_CALL:
                return JoinPoint.CONSTRUCTOR_CALL;
            case JoinPointType.FIELD_SET:
                return JoinPoint.FIELD_SET;
            case JoinPointType.FIELD_GET:
                return JoinPoint.FIELD_GET;
            case JoinPointType.HANDLER:
                return JoinPoint.HANDLER;
            case JoinPointType.STATIC_INITALIZATION:
                return JoinPoint.STATIC_INITIALIZATION;
            default:
                throw new RuntimeException("join point type [" + type + "] is not a valid type");
        }
    }

    /**
     * Sets the join point type to a string representation.
     * 
     * @param type the type
     */
    public static PointcutType getPointcutType(final int type) {
        switch (type) {
            case JoinPointType.METHOD_EXECUTION:
                return PointcutType.EXECUTION;
            case JoinPointType.METHOD_CALL:
                return PointcutType.CALL;
            case JoinPointType.CONSTRUCTOR_EXECUTION:
                return PointcutType.EXECUTION;
            case JoinPointType.CONSTRUCTOR_CALL:
                return PointcutType.CALL;
            case JoinPointType.FIELD_SET:
                return PointcutType.SET;
            case JoinPointType.FIELD_GET:
                return PointcutType.GET;
            case JoinPointType.HANDLER:
                return PointcutType.HANDLER;
            case JoinPointType.STATIC_INITALIZATION:
                return PointcutType.STATIC_INITIALIZATION;
            default:
                throw new RuntimeException("join point type [" + type + "] is not a valid type");
        }
    }

    /**
     * Invoke the join point.
     * 
     * @param joinPoint the join point instance
     * @return the result from the invocation
     * @throws Throwable
     */
    public static Object invokeJoinPoint(final JoinPoint joinPoint, final int joinPointType) throws Throwable {
        Object result = null;
        switch (joinPointType) {
            case JoinPointType.METHOD_EXECUTION:
                result = invokeTargetMethodExecution(joinPoint);
                break;
            case JoinPointType.METHOD_CALL:
                result = invokeTargetMethodCall(joinPoint);
                break;
            case JoinPointType.CONSTRUCTOR_EXECUTION:
                result = invokeTargetConstructorExecution(joinPoint);
                break;
            case JoinPointType.CONSTRUCTOR_CALL:
                result = invokeTargetConstructorCall(joinPoint);
                break;
            case JoinPointType.FIELD_SET:
                // for field set join point, the returned value from around advice is ignored
                setTargetField(joinPoint);
                break;
            case JoinPointType.FIELD_GET:
                result = getTargetField(joinPoint);
                break;
        }
        return result;
    }

    /**
     * Returns the target instance ('this'). If the join point is executing in a static context it returns null.
     * 
     * @return the target instance
     */
    public Object getTarget() {
        return getRtti().getTarget();//m_targetInstanceRef.get();//AW-265
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
    //AW-265
//    public void setTarget(final Object targetInstance) {
//        m_targetInstanceRef = new WeakReference(targetInstance);
//    }

    /**
     * Checks if the join point is in the correct control flow.
     * 
     * @return true if we have a parse
     */
    public boolean isInCflow() {
        if (m_checkCflow) {
            boolean isInCFlow = false;
            for (Iterator iterator = m_joinPointMetaData.cflowExpressions.iterator(); iterator.hasNext();) {
                CflowExpressionVisitorRuntime cflowExpressionRuntime = (CflowExpressionVisitorRuntime) iterator.next();
                if (m_system.isInControlFlowOf(cflowExpressionRuntime, m_joinPointMetaData.expressionContext)) {
                    isInCFlow = true;
                    break;
                }
            }
            if (!isInCFlow) {
                return false;
            }
        }
        return true;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer(30);
        sb.append(getJoinPointTypeAsString(m_type));
        sb.append(":").append(m_targetClass.getName());
        sb.append(".").append(getSignature().getName());
        return sb.toString();
    }

    /**
     * Provides custom deserialization.
     * 
     * @param stream the object input stream containing the serialized object
     * @throws Exception in case of failure
     * @TODO: for this to work it requires that the instance is read from the same CL that it was written in
     * @TODO: target instance is not read in
     */
    private void readObject(final ObjectInputStream stream) throws Exception {
        ObjectInputStream.GetField fields = stream.readFields();
        m_type = fields.get("m_type", 0);
        m_typeAsString = getJoinPointTypeAsString(m_type);
        m_targetClass = (Class) fields.get("m_targetClass", null);
        m_checkCflow = m_joinPointMetaData.cflowExpressions.size() > 0;
        m_aroundAdviceExecutor = (AroundAdviceExecutor) fields.get("m_aroundAdviceExecutor", null);
        m_beforeAdviceExecutor = (BeforeAdviceExecutor) fields.get("m_beforeAdviceExecutor", null);
        m_afterAdviceExecutor = (AfterAdviceExecutor) fields.get("m_afterAdviceExecutor", null);
        m_metaData = (Map) fields.get("m_metaData", new HashMap());
        m_system = SystemLoader.getSystem(m_targetClass.getClassLoader());
        m_system.initialize();
    }

    /**
     * Extracts a subset of the joinPoint instance RTTI arguments.
     * This is used to support args() syntax.
     * This method is not exposed in the JoinPoint interface since user does not need it.
     * 
     * @param methodToArgIndexes
     * @return
     */
    public abstract Object[] extractArguments(int[] methodToArgIndexes);

    /**
     * Allows to pass the RTTI to the JP. The JPBase implementation delegates getTarget to the RTTI.
     * Since in 1.0 engine, JP are cached and shared, while the RTTI is not, we need to set the RTTI (AW-265).
     * This method MUST not be called by the user.
     *
     * @param rtti
     */
    public abstract void setRtti(Rtti rtti);
}