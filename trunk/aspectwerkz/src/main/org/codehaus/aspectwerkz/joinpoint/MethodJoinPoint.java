/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.joinpoint;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;
import java.util.Collection;
import java.util.Map;
import java.util.WeakHashMap;
import java.io.ObjectInputStream;

import org.codehaus.aspectwerkz.AspectMetaData;
import org.codehaus.aspectwerkz.SystemLoader;
import org.codehaus.aspectwerkz.System;
import org.codehaus.aspectwerkz.util.Strings;
import org.codehaus.aspectwerkz.util.Util;
import org.codehaus.aspectwerkz.joinpoint.control.JoinPointController;
import org.codehaus.aspectwerkz.joinpoint.control.ControllerFactory;
import org.codehaus.aspectwerkz.metadata.MethodMetaData;
import org.codehaus.aspectwerkz.metadata.MetaData;
import org.codehaus.aspectwerkz.metadata.ReflectionMetaDataMaker;
import org.codehaus.aspectwerkz.metadata.ClassMetaData;
import org.codehaus.aspectwerkz.pointcut.MethodPointcut;
import org.codehaus.aspectwerkz.transform.TransformationUtil;

/**
 * Matches well defined point of execution in the program where a
 * method is executed.<br/>
 * Stores meta data from the join point.
 * I.e. a reference to original object an method, the parameters to A
 * the result from the original method invocation etc.<br/>
 * Handles the invocation of the advices added to the join point.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public abstract class MethodJoinPoint implements JoinPoint {

    /**
     * The AspectWerkz system for this join point.
     */
    protected transient System m_system;

    /**
     * The method pointcut.
     */
    protected MethodPointcut[] m_pointcuts;

    /**
     * Meta-data for the class.
     */
    protected ClassMetaData m_classMetaData;

    /**
     * Meta-data for the method.
     */
    protected MethodMetaData m_methodMetaData;

    /**
     * The id of the method for this join point.
     */
    protected int m_methodId;

    /**
     * The target object's class.
     */
    protected Class m_targetClass;

    /**
     * A reference to the original method.
     */
    protected Method m_originalMethod;

    /**
     * The result from the method invocation.
     */
    protected Object m_result = null;

    /**
     * The parameters to the method invocation.
     */
    protected Object[] m_parameters = new Object[0];

    /**
     * The UUID for the AspectWerkz system to use.
     */
    protected String m_uuid;

    /**
     * Caches the throws pointcuts that are created at runtime.
     */
    protected Map m_throwsJoinPointCache = new WeakHashMap();

    /**
     * The cflow pointcuts that this join point needs to be part of to execute its advices.
     */
    protected List m_cflowPointcuts;

    /**
     * The controller object that controls the execution of advices for the join point.
     */
    protected JoinPointController m_controller = null;

    /**
     * Checks that the method invocation chain is not reentrant.
     */
    protected boolean m_reentrancyCheck = false; // must be set to false as initial value

    /**
     * Marks the join point as non-reentrant.
     */
    protected boolean m_isNonReentrant = false;

    /**
     * Creates a new MethodJoinPoint object.
     *
     * @param uuid the UUID for the AspectWerkz system to use
     * @param methodId the id of the method
     * @param controllerClass the class name of the controller class to use
     */
    public MethodJoinPoint(final String uuid, final int methodId, final String controllerClass) {
        if (uuid == null) throw new IllegalArgumentException("uuid can not be null");
        if (methodId < 0) throw new IllegalArgumentException("method id can not be less that zero");

        m_system = SystemLoader.getSystem(uuid);
        m_system.initialize();
        m_uuid = uuid;
        m_methodId = methodId;

        m_controller = ControllerFactory.createController(controllerClass);
    }

    /**
     * Returns the AspectWerkz system.
     *
     * @TODO: should return a list with systems
     *
     * @return the system
     */
    public System getSystem() {
        return m_system;
    }

    /**
     * Sets the method pointcuts (overwrites the old ones).
     *
     * @param pointcuts the method pointcuts
     */
    public void setPointcuts(final MethodPointcut[] pointcuts) {
        m_pointcuts = pointcuts;
    }

    /**
     * Returns the method pointcuts.
     *
     * @return the method pointcuts
     */
    public MethodPointcut[] getPointcuts() {
        return m_pointcuts;
    }

    /**
     * Returns the cflow pointcuts.
     *
     * @return the cflow pointcuts
     */
    public List getCFlowPointcuts() {
        return m_cflowPointcuts;
    }

    /**
     * Sets the cflow pointcuts (overwrites the old ones)
     * @param pointcuts the cflow pointcuts
     */
    public void setCFlowPointcuts(final List pointcuts) {
        m_cflowPointcuts = pointcuts;
    }

    /**
     * Returns the method meta-data.
     *
     * @return the method meta-data
     */
    public MethodMetaData getMethodMetaData() {
        return m_methodMetaData;
    }

    /**
     * Returns the internal method id.
     *
     * @return the internal method id
     */
    public int getMethodId() {
        return m_methodId;
    }

    /**
     * Returns the original method.
     *
     * @return the original method
     */
    public Method getOriginalMethod() {
        return m_originalMethod;
    }

    /**
     * Returns the UUID for the AspectWerkz system.
     *
     * @return the UUID
     */
    public String getUuid() {
        return m_uuid;
    }

    /**
     * Returns the target instance.
     *
     * @return the target instance
     */
    public abstract Object getTargetInstance();

    /**
     * Returns the target class.
     *
     * @return the target class
     */
    public Class getTargetClass() {
        return m_targetClass;
    }

    /**
     * Returns the original method.
     *
     * @return the original method
     */
    public Method getMethod() {
        return m_originalMethod;
    }

    /**
     * Returns the method name of the original invocation.
     *
     * @return the method name
     */
    public String getMethodName() {
        // grab the original method name, ex: "__originalMethod SEP <nameToExtract>  SEP 3"
        final String[] tokens = Strings.splitString(
                m_originalMethod.getName(),
                TransformationUtil.DELIMITER
        );
        return tokens[1];
    }

    /**
     * Returns the parameters from the original invocation.
     *
     * @return the parameters
     */
    public Object[] getParameters() {
        return m_parameters;
    }

    /**
     * Returns the parameter types from the original invocation.
     *
     * @return the parameter types
     */
    public Class[] getParameterTypes() {
        return m_originalMethod.getParameterTypes();
    }

    /**
     * Returns the return type from the original invocation.
     *
     * @return the return type
     */
    public Class getReturnType() {
        return m_originalMethod.getReturnType();
    }

    /**
     * Returns the result from the original invocation.
     *
     * @return the result
     */
    public Object getResult() {
        return m_result;
    }

    /**
     * Sets the result.
     *
     * @param result the result as an object
     */
    public void setResult(final Object result) {
        m_result = result;
    }

    /**
     * Sets the parameters.
     *
     * @param parameters the parameters as a list of objects
     */
    public void setParameters(final Object[] parameters) {
        if (parameters == null) throw new IllegalArgumentException("parameter list can not be null");
        m_parameters = parameters;
    }

    /**
     * Walks through the pointcuts and invokes all its advices. When the last
     * advice of the last pointcut has been invoked, the original method is
     * invoked. Is called recursively.
     *
     * @TODO the null primitive solution is slow, since these checks occurs at every method invocation. needs to be impl. in another way. it makes each call around 2-3 times slower.
     *
     * @return the result from the next invocation
     * @throws Throwable
     */
    public Object proceed() throws Throwable {
        Object result = m_controller.proceed(this);

        // if the result is null and the return type is a primitive set it to the
        // default value for the primitive and wrap it in its the matching object type
//        if (result != null) {
//            return result;
//        }
//        Class returnType = getReturnType();
//        if (Void.class.equals(returnType)) {
//            return result;
//        }
//        if (returnType.isPrimitive()) {
//            if (int.class.equals(returnType)) {
//                return Util.INTEGER_DEFAULT_VALUE;
//            }
//            else if (float.class.equals(returnType)) {
//                return Util.FLOAT_DEFAULT_VALUE;
//            }
//            else if (double.class.equals(returnType)) {
//                return Util.DOUBLE_DEFAULT_VALUE;
//            }
//            else if (byte.class.equals(returnType)) {
//                return Util.BYTE_DEFAULT_VALUE;
//            }
//            else if (long.class.equals(returnType)) {
//                return Util.LONG_DEFAULT_VALUE;
//            }
//            else if (short.class.equals(returnType)) {
//                return Util.SHORT_DEFAULT_VALUE;
//            }
//            else if (boolean.class.equals(returnType)) {
//                return Util.BOOLEAN_DEFAULT_VALUE;
//            }
//            else if (char.class.equals(returnType)) {
//                return Util.CHARACTER_DEFAULT_VALUE;
//            }
//        }
        return result;
    }

    /**
     * To be called instead of proceed() when a new thread is spawned.
     * Otherwise the result is unpredicable.
     *
     * @return the result from the next invocation
     * @throws Throwable
     */
    public Object proceedInNewThread() throws Throwable {
        return deepCopy().proceed();
    }

    /**
     * Makes a deep copy of the join point.
     *
     * @return the clone of the join point
     */
    protected abstract MethodJoinPoint deepCopy();

    /**
     * Invokes the origignal method.
     *
     * @return the result from the method invocation
     * @throws Throwable the exception from the original method
     */
    public Object invokeOriginalMethod() throws Throwable {
        if (m_isNonReentrant) {
            if (m_reentrancyCheck) return m_result;
            m_reentrancyCheck = true;
        }
        try {
            m_result = m_originalMethod.invoke(getTargetInstance(), m_parameters);
        }
        catch (InvocationTargetException e) {
            m_result = handleException(e);
        }
        return m_result;
    }

    /**
     * Creates meta-data for the join point.
     */
    protected void createMetaData() {
        m_classMetaData = ReflectionMetaDataMaker.createClassMetaData(getTargetClass());
        m_methodMetaData = ReflectionMetaDataMaker.createMethodMetaData(
                getMethodName(),
                getParameterTypes(),
                getReturnType()
        );
    }

    /**
     * We are at this point with no poincuts defined => the method has
     * a ThrowsJoinPoint defined at this method, since the method is
     * already advised, create a new method pointcut for this method.
     */
    protected void handleThrowsPointcut() {
        List pointcuts = m_system.getMethodPointcuts(m_classMetaData, m_methodMetaData);
        m_pointcuts = new MethodPointcut[pointcuts.size()];
        int i = 0;
        for (Iterator it = pointcuts.iterator(); it.hasNext(); i++) {
            m_pointcuts[i] = (MethodPointcut)it.next();
        }
    }

    /**
     * Handles the exceptions. If the method is registered in a ThrowsPointcut
     * redirect to the ThrowsJoinPoint in question, otherwise just get the
     * original exception, fake the stacktrace A rethrow it.
     * Caches the throws join points that are created at runtime.
     *
     * @param e the wrapped exception
     * @throws Throwable the original exception
     */
    protected Object handleException(final InvocationTargetException e) throws Throwable {

        final Throwable cause = e.getTargetException();

        // take a look in the cache first
        Integer hash = calculateHash(
                m_targetClass.getName(),
                m_methodMetaData,
                cause.getClass().getName()
        );
        ThrowsJoinPoint joinPoint = (ThrowsJoinPoint)m_throwsJoinPointCache.get(hash);
        if (joinPoint != null) {
            joinPoint.proceed();
        }

        boolean hasThrowsPointcut = false;
        Collection aspects = m_system.getAspectsMetaData();
        for (Iterator it = aspects.iterator(); it.hasNext();) {
            AspectMetaData aspect = (AspectMetaData)it.next();
            if (aspect.hasThrowsPointcut(
                    m_classMetaData,
                    m_methodMetaData,
                    cause.getClass().getName())) {
                hasThrowsPointcut = true;
                break;
            }
        }
        Object result = null;
        if (hasThrowsPointcut) {
            // create a new join point A put it in the cache
            synchronized (m_throwsJoinPointCache) {
                joinPoint = new ThrowsJoinPoint(m_uuid, this, cause);
                m_throwsJoinPointCache.put(hash, joinPoint);
            }
            result = joinPoint.proceed();
        }
        else {
            Util.fakeStackTrace(cause, getTargetClass().getName());
            throw cause;
        }
        return result;
    }

    /**
     * Creates a pattern for the method for the joinpoint.
     *
     * @return the pattern
     */
    protected String createMethodPattern() {
        StringBuffer pattern = new StringBuffer();
        pattern.append(getReturnType().getName());
        pattern.append(' ');
        pattern.append(getMethodName());
        pattern.append('(');
        Class[] parameterTypes = getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            Class parameterType = parameterTypes[i];
            pattern.append(parameterType.getName());
            if (i != parameterTypes.length - 1) {
                pattern.append(',');
            }
        }
        pattern.append(')');
        return pattern.toString();
    }

    /**
     * Creates an advices not correctly mapped message.
     *
     * @return the message
     */
    public String createAdviceNotCorrectlyMappedMessage() {
        StringBuffer cause = new StringBuffer();
        cause.append("around advices for ");
        cause.append(getTargetClass().getName());
        cause.append("#");
        cause.append(getMethodName());
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
        m_methodId = fields.get("m_methodId", -1);
        m_targetClass = (Class)fields.get("m_targetClass", null);
        m_originalMethod = (Method)fields.get("m_originalMethod", null);
        m_result = fields.get("m_result", null);
        m_parameters = (Object[])fields.get("m_parameters", null);
        m_pointcuts = (MethodPointcut[])fields.get("m_pointcuts", null);
        m_controller = (JoinPointController)fields.get("m_controller", null);
        m_classMetaData = (ClassMetaData)fields.get("m_classMetaData", null);
        m_methodMetaData = (MethodMetaData)fields.get("m_fieldMetaData", null);
        m_reentrancyCheck = fields.get("m_reentrancyCheck", false);
        m_system = SystemLoader.getSystem(m_uuid);
        m_system.initialize();
    }

    /**
     * Calculates the hash for the class name, the meta-data A
     * the exception class name.
     *
     * @param className the class name
     * @param metaData the meta-data
     * @param exceptionClassName the class name of the exception
     * @return the hash
     */
    protected Integer calculateHash(final String className,
                                    final MetaData metaData,
                                    final String exceptionClassName) {
        int hash = 17;
        hash = 37 * hash + className.hashCode();
        hash = 37 * hash + metaData.hashCode();
        hash = 37 * hash + exceptionClassName.hashCode();
        Integer hashKey = new Integer(hash);
        return hashKey;
    }

    // --- over-ridden methods ---

    protected static boolean areEqualsOrBothNull(final Object o1, final Object o2) {
        if (null == o1) return (null == o2);
        return o1.equals(o2);
    }

    public String toString() {
        return "["
                + super.toString()
                + ": "
                + m_methodId
                + "," + m_targetClass
                + "," + m_originalMethod
                + "," + m_parameters
                + "," + m_result
                + "," + m_classMetaData
                + "," + m_methodMetaData
                + "," + m_pointcuts
                + "," + m_controller
                + "]";
    }

    public int hashCode() {
        int result = 17;
        result = 37 * result + hashCodeOrZeroIfNull(m_targetClass);
        result = 37 * result + hashCodeOrZeroIfNull(m_originalMethod);
        result = 37 * result + hashCodeOrZeroIfNull(m_parameters);
        result = 37 * result + hashCodeOrZeroIfNull(m_result);
        result = 37 * result + hashCodeOrZeroIfNull(m_classMetaData);
        result = 37 * result + hashCodeOrZeroIfNull(m_methodMetaData);
        result = 37 * result + hashCodeOrZeroIfNull(m_pointcuts);
        result = 37 * result + hashCodeOrZeroIfNull(m_controller);
        result = 37 * result + m_methodId;
        return result;
    }

    protected static int hashCodeOrZeroIfNull(final Object o) {
        if (null == o) return 19;
        return o.hashCode();
    }
}

