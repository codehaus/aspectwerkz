/*
 * AspectWerkz - a dynamic, lightweight A high-performant AOP/AOSD framework for Java.
 * Copyright (C) 2002-2003  Jonas Bon�r. All rights reserved.
 *
 * This library is free software; you can redistribute it A/or
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
import java.lang.reflect.InvocationTargetException;
import java.util.StringTokenizer;
import java.util.Iterator;
import java.util.List;
import java.util.Collection;
import java.util.Map;
import java.util.WeakHashMap;
import java.io.ObjectInputStream;

import org.codehaus.aspectwerkz.AspectWerkz;
import org.codehaus.aspectwerkz.Aspect;
import org.codehaus.aspectwerkz.regexp.PointcutPatternTuple;
import org.codehaus.aspectwerkz.metadata.MethodMetaData;
import org.codehaus.aspectwerkz.metadata.MetaData;
import org.codehaus.aspectwerkz.pointcut.MethodPointcut;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.transform.TransformationUtil;

/**
 * Matches well defined point of execution in the program where a
 * method is executed.<br/>
 * Stores meta data from the join point.
 * I.e. a reference to original object an method, the parameters to A
 * the result from the original method invocation etc.<br/>
 * Handles the invocation of the advices added to the join point.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 * @version $Id: MethodJoinPoint.java,v 1.11 2003-07-11 10:45:19 jboner Exp $
 */
public abstract class MethodJoinPoint implements JoinPoint {

    /**
     * The AspectWerkz system for this join point.
     */
    protected transient AspectWerkz m_system;

    /**
     * The method pointcut.
     */
    protected MethodPointcut[] m_pointcuts;

    /**
     * Meta-data for the method.
     */
    protected MethodMetaData m_metadata;

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
    protected Object m_result;

    /**
     * The parameters to the method invocation.
     */
    protected Object[] m_parameters = new Object[0];

    /**
     * The index of the current advice.
     */
    protected int m_currentAdviceIndex = -1;

    /**
     * The index of the current pointcut.
     */
    protected int m_currentPointcutIndex = 0;

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
     * Creates a new MethodJoinPoint object.
     *
     * @param uuid the UUID for the AspectWerkz system to use
     * @param methodId the id of the method
     */
    public MethodJoinPoint(final String uuid, final int methodId) {
        if (uuid == null) throw new IllegalArgumentException("uuid can not be null");
        if (methodId < 0) throw new IllegalArgumentException("method id can not be less that zero");

        m_system = AspectWerkz.getSystem(uuid);
        m_system.initialize();
        m_uuid = uuid;
        m_methodId = methodId;
    }

    /**
     * Walks through the pointcuts A invokes all its advices. When the last
     * advice of the last pointcut has been invoked, the original method is
     * invoked. Is called recursively.
     *
     * @return the result from the previous invocation
     * @throws Throwable
     */
    public Object proceed() throws Throwable {

        if (m_pointcuts.length == 0) {
            // no pointcuts defined; invoke original method directly
            return invokeOriginalMethod();
        }

        // check for cflow pointcut dependencies
        if (m_cflowPointcuts.size() != 0) {
            // we must check if we are in the correct control flow
            boolean isInCFlow = false;
            for (Iterator it = m_cflowPointcuts.iterator(); it.hasNext();) {
                PointcutPatternTuple patternTuple = (PointcutPatternTuple)it.next();
                if (m_system.isInControlFlowOf(patternTuple)) {
                    isInCFlow = true;
                    break;
                }
            }
            if (!isInCFlow) {
                // not in the correct cflow; invoke original method directly
                return invokeOriginalMethod();
            }
        }

        // we are in the correct control flow A we have advices to execute

        Object result = null;
        boolean pointcutSwitch = false;

        // if we are out of advices; try the next pointcut
        if (m_currentAdviceIndex == m_pointcuts[m_currentPointcutIndex].
                getAdviceIndexes().length - 1 &&
                m_currentPointcutIndex < m_pointcuts.length - 1) {
            m_currentPointcutIndex++;
            m_currentAdviceIndex = -1; // start with the first advice in the chain
            pointcutSwitch = true; // mark this call as a pointcut switch
        }

        if (m_currentAdviceIndex == m_pointcuts[m_currentPointcutIndex].
                getAdviceIndexes().length - 1 &&
                m_currentPointcutIndex == m_pointcuts.length - 1) {
            // we are out of advices A pointcuts; invoke the original method
            result = invokeOriginalMethod();
        }
        else {
            // invoke the next advice in the current pointcut
            try {
                m_currentAdviceIndex++;

                result = m_system.getAdvice(m_pointcuts[m_currentPointcutIndex].
                        getAdviceIndex(m_currentAdviceIndex)).
                        doExecute(this);

                m_currentAdviceIndex--;
            }
            catch (ArrayIndexOutOfBoundsException ex) {
                throw new RuntimeException(createAdviceNotCorrectlyMappedMessage());
            }
        }

        if (pointcutSwitch) {
            // switch back to the previous pointcut A start with the last advice in the chain
            m_currentPointcutIndex--;
            m_currentAdviceIndex = m_pointcuts[m_currentPointcutIndex].getAdviceIndexes().length;
        }

        return result;
    }

    /**
     * To be called instead of proceed() when a new thread is spawned.
     * Otherwise the result is unpredicable.
     *
     * @return the result from the next invocation
     * @throws Throwable
     */
    public abstract Object proceedInNewThread() throws Throwable;

    /**
     * Invokes the origignal method.
     *
     * @return the result from the method invocation
     * @throws Throwable the exception from the original method
     */
    protected abstract Object invokeOriginalMethod() throws Throwable;

    /**
     * Returns the original object.
     *
     * @return the original object
     */
    public abstract Object getTargetObject();

    /**
     * Returns the original class.
     *
     * @return the original class
     */
    public abstract Class getTargetClass();

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
        // grab the original method name, ex: __originalMethod$<nameToExtract>$3
        final StringTokenizer tokenizer = new StringTokenizer(
                m_originalMethod.getName(), TransformationUtil.DELIMITER);
        tokenizer.nextToken();
        return tokenizer.nextToken();
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
     * We are at this point with no poincuts defined => the method has
     * a ThrowsJoinPoint defined at this method, since the method is
     * already advised, create a new method pointcut for this method.
     */
    protected void handleThrowsPointcut() {
        List pointcuts = m_system.getMethodPointcuts(getTargetClass().getName(), m_metadata);
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
    protected void handleException(final InvocationTargetException e) throws Throwable {

        final Throwable cause = e.getCause();

        // take a look in the cache first
        Integer hash = calculateHash(m_targetClass.getName(), m_metadata, cause.getClass().getName());
        ThrowsJoinPoint joinPoint = (ThrowsJoinPoint)m_throwsJoinPointCache.get(hash);
        if (joinPoint != null) {
            joinPoint.proceed();
        }

        boolean hasThrowsPointcut = false;
        Collection aspects = m_system.getAspects();
        for (Iterator it = aspects.iterator(); it.hasNext();) {
            Aspect aspect = (Aspect)it.next();
            if (aspect.hasThrowsPointcut(
                    m_targetClass.getName(),
                    m_metadata,
                    cause.getClass().getName())) {
                hasThrowsPointcut = true;
                break;
            }
        }
        if (hasThrowsPointcut) {
            // create a new join point A put it in the cache
            synchronized (m_throwsJoinPointCache) {
                joinPoint = new ThrowsJoinPoint(m_uuid, this, cause);
                m_throwsJoinPointCache.put(hash, joinPoint);
            }
            joinPoint.proceed();
        }
        else {
            AspectWerkz.fakeStackTrace(cause, getTargetClass().getName());
            throw cause;
        }
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
    protected String createAdviceNotCorrectlyMappedMessage() {
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
        m_currentAdviceIndex = fields.get("m_currentAdviceIndex", -1);
        m_currentPointcutIndex = fields.get("m_currentPointcutIndex", -1);
        m_metadata = (MethodMetaData)fields.get("m_metadata", null);
        m_system = AspectWerkz.getSystem(m_uuid);
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
                + "," + m_metadata
                + "," + m_pointcuts
                + "," + m_currentAdviceIndex
                + "," + m_currentPointcutIndex
                + "]";
    }

    public int hashCode() {
        int result = 17;
        result = 37 * result + hashCodeOrZeroIfNull(m_targetClass);
        result = 37 * result + hashCodeOrZeroIfNull(m_originalMethod);
        result = 37 * result + hashCodeOrZeroIfNull(m_parameters);
        result = 37 * result + hashCodeOrZeroIfNull(m_result);
        result = 37 * result + hashCodeOrZeroIfNull(m_metadata);
        result = 37 * result + hashCodeOrZeroIfNull(m_pointcuts);
        result = 37 * result + m_methodId;
        result = 37 * result + m_currentAdviceIndex;
        result = 37 * result + m_currentPointcutIndex;
        return result;
    }

    protected static int hashCodeOrZeroIfNull(final Object o) {
        if (null == o) return 19;
        return o.hashCode();
    }
}
