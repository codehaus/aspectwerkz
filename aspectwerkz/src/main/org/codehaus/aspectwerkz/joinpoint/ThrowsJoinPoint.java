/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.joinpoint;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.ObjectInputStream;

import org.codehaus.aspectwerkz.AspectWerkz;
import org.codehaus.aspectwerkz.exception.DefinitionException;
import org.codehaus.aspectwerkz.pointcut.ThrowsPointcut;
import org.codehaus.aspectwerkz.metadata.MethodMetaData;
import org.codehaus.aspectwerkz.metadata.ReflectionMetaDataMaker;
import org.codehaus.aspectwerkz.metadata.ClassMetaData;

/**
 * Matches well defined point of execution in the program where an exception is
 * thrown out of a method.<br/>Stores meta data from the join point.
 * I.e a reference to original object an method, the original exception etc.<br/>
 * Handles the invocation of the advices added to the join point.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class ThrowsJoinPoint implements JoinPoint {

    /**
     * The AspectWerkz system for this join point.
     */
    protected transient AspectWerkz m_system;

    /**
     * The serial version uid for the class.
     */
    private static final long serialVersionUID = 6363637170952486892L;

    /**
     * The method join point for this join point.
     */
    protected MethodJoinPoint m_methodJoinPoint;

    /**
     * The exception for this join point.
     */
    protected Throwable m_exception;

    /**
     * The advice indexes.
     */
    protected int[] m_adviceIndexes = new int[0];

    /**
     * The index of the current advice.
     */
    protected int m_currentAdviceIndex = -1;

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
     * Creates a new throws join point.
     *
     * @param uuid the UUID for the AspectWerkz system to use
     * @param methodJoinPoint the method join point
     * @param exception the exception
     */
    public ThrowsJoinPoint(final String uuid,
                           final MethodJoinPoint methodJoinPoint,
                           final Throwable exception) {
        if (methodJoinPoint == null) throw new IllegalArgumentException("method join point can not be null");
        if (exception == null) throw new IllegalArgumentException("exception exception can not be null");

        m_system = AspectWerkz.getSystem(uuid);
        m_system.initialize();

        m_uuid = uuid;
        m_methodJoinPoint = methodJoinPoint;
        m_exception = exception;

        createMetaData();
        AspectWerkz.fakeStackTrace(m_exception, getTargetClass().getName());
        loadAdvices();
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
     * Invokes the next advice in the chain until it reaches the end.
     *
     * @return null the result from a throws pointcut always returns null
     * @throws Throwable
     */
    public Object proceed() throws Throwable {
        m_currentAdviceIndex++;
        if (m_currentAdviceIndex != m_adviceIndexes.length) {
            try {
                m_system.getAdvice(m_adviceIndexes[m_currentAdviceIndex]).doExecute(this);
            }
            catch (ArrayIndexOutOfBoundsException ex) {
                StringBuffer cause = new StringBuffer();
                cause.append("advices for ");
                cause.append(getTargetClass().getName());
                cause.append("#");
                cause.append(getMethodName());
                cause.append("#");
                cause.append(getExceptionName());
                cause.append(" are not correctly mapped");
                throw new DefinitionException(cause.toString());
            }
        }
        return null;
    }

    /**
     * Returns the exception.
     *
     * @return the exception
     */
    public Throwable getException() {
        return m_exception;
    }

    /**
     * Returns the exception class.
     *
     * @return the exception class
     */
    public Class getExceptionClass() {
        return m_exception.getClass();
    }

    /**
     * Returns the exception class name.
     *
     * @return the exception class name
     */
    public String getExceptionName() {
        return m_exception.getClass().getName();
    }

    /**
     * Returns the message.
     *
     * @return the message
     */
    public String getMessage() {
        return m_exception.getMessage();
    }

    /**
     * Returns the localized message.
     *
     * @return the localized message
     */
    public String getLocalizedMessage() {
        return m_exception.getLocalizedMessage();
    }

    /**
     * Returns the line number in the file where the exception was thrown.
     *
     * @return the line number
     */
    // Works for JDK 1.4.x only
//    public int getLineNumberForThrow() {
//        return m_exception.getStackTrace()[0].getLineNumber();
//    }

    /**
     * Returns the method name where the exception was thrown.
     *
     * @return the method name
     */
    // Works for JDK 1.4.x only
//    public String getMethodNameForThrow() {
//        return m_exception.getStackTrace()[0].getMethodName();
//    }

    /**
     * Returns the file name where the exception was thrown.
     *
     * @return the file name
     */
    // Works for JDK 1.4.x only
//    public String getFileNameForThrow() {
//        return m_exception.getStackTrace()[0].getFileName();
//    }

    /**
     * Returns the class name where the exception was thrown.
     *
     * @return the class name
     */
    // Works for JDK 1.4.x only
//    public String getClassNameForThrow() {
//        return m_exception.getStackTrace()[0].getClassName();
//    }

    /**
     * Returns the target object.
     *
     * @return the target object
     */
    public Object getTargetObject() {
        return m_methodJoinPoint.getTargetObject();
    }

    /**
     * Returns the target class.
     *
     * @return the target class
     */
    public Class getTargetClass() {
        return m_methodJoinPoint.getTargetClass();
    }

    /**
     * Returns the target method.
     *
     * @return the target method
     */
    public java.lang.reflect.Method getMethod() {
        return m_methodJoinPoint.getMethod();
    }

    /**
     * Returns the method name of the original invocation.
     *
     * @return the method name
     */
    public String getMethodName() {
        return m_methodJoinPoint.getMethodName();
    }

    /**
     * Returns the parameters from the original invocation.
     *
     * @return the parameters
     */
    public Object[] getMethodParameters() {
        return m_methodJoinPoint.getParameters();
    }

    /**
     * Returns the parameter types from the original invocation.
     *
     * @return the parameter types
     */
    public Class[] getMethodParameterTypes() {
        return m_methodJoinPoint.getParameterTypes();
    }

    /**
     * Returns the return type from the original invocation.
     *
     * @return the return type
     */
    public Class getMethodReturnType() {
        return m_methodJoinPoint.getReturnType();
    }

    /**
     * Loads the advices for this pointcut.
     */
    protected void loadAdvices() {
        synchronized (m_adviceIndexes) {
            List adviceIndexes = new ArrayList();

            // get all the throws pointcuts for this class
            List pointcuts = m_system.getThrowsPointcuts(m_classMetaData, m_methodMetaData);

            for (Iterator it = pointcuts.iterator(); it.hasNext();) {
                ThrowsPointcut throwsPointcut = (ThrowsPointcut)it.next();
                int[] advices = throwsPointcut.getAdviceIndexes();
                for (int j = 0; j < advices.length; j++) {
                    adviceIndexes.add(new Integer(advices[j]));
                }
            }

            m_adviceIndexes = new int[adviceIndexes.size()];
            int i = 0;
            for (Iterator it = adviceIndexes.iterator(); it.hasNext(); i++) {
                m_adviceIndexes[i] = ((Integer)it.next()).intValue();
            }
        }
    }

    /**
     * Creates meta-data for the join point.
     */
    protected void createMetaData() {
        m_classMetaData = ReflectionMetaDataMaker.createClassMetaData(getTargetClass());
        m_methodMetaData = ReflectionMetaDataMaker.createMethodMetaData(
                getMethodName(),
                getMethodParameterTypes(),
                getMethodReturnType());
    }

    /**
     * Makes a deep copy of the join point.
     *
     * @return the clone of the join point
     */
    protected ThrowsJoinPoint deepCopy() {
        final ThrowsJoinPoint clone = new ThrowsJoinPoint(m_uuid, m_methodJoinPoint, m_exception);
        clone.m_currentAdviceIndex = m_currentAdviceIndex;
        clone.m_adviceIndexes = new int[m_adviceIndexes.length];
        System.arraycopy(m_adviceIndexes, 0, clone.m_adviceIndexes, 0, m_adviceIndexes.length);
        return clone;
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
        m_currentAdviceIndex = fields.get("m_currentAdviceIndex", -1);
        m_classMetaData = (ClassMetaData)fields.get("m_classMetaData", null);
        m_methodMetaData = (MethodMetaData)fields.get("m_fieldMetaData", null);
        m_methodJoinPoint = (MethodJoinPoint)fields.get("m_methodJoinPoint", null);
        m_exception = (Throwable)fields.get("m_exception", null);
        m_adviceIndexes = (int[])fields.get("m_adviceIndexes", null);
        m_system = AspectWerkz.getSystem(m_uuid);
        m_system.initialize();
    }
}
