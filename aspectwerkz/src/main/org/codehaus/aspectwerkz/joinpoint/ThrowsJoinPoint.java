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

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import org.codehaus.aspectwerkz.AspectWerkz;
import org.codehaus.aspectwerkz.Aspect;
import org.codehaus.aspectwerkz.pointcut.ThrowsPointcut;
import org.codehaus.aspectwerkz.definition.metadata.MethodMetaData;

/**
 * Matches well defined point of execution in the program where an exception is
 * thrown out of a method.<br/>Stores meta data from the join point.
 * I.e a reference to original object an method, the original exception etc.<br/>
 * Handles the invocation of the advices added to the join point.
 *
 * @author <a href="mailto:jboner@acm.org">Jonas Bonér</a>
 * @version $Id: ThrowsJoinPoint.java,v 1.1.1.1 2003-05-11 15:14:37 jboner Exp $
 */
public class ThrowsJoinPoint implements JoinPoint {

    /**
     * The serial version uid for the class.
     */
    private static final long serialVersionUID = -4857649400733289567L;

    /**
     * The method join point for this join point.
     */
    protected final MethodJoinPoint m_methodJoinPoint;

    /**
     * The exception for this join point.
     */
    protected final Throwable m_exception;

    /**
     * The advice indexes.
     */
    protected int[] m_adviceIndexes = new int[0];

    /**
     * The index of the current advice.
     */
    protected int m_currentAdviceIndex = -1;

    /**
     * Meta-data for the method.
     */
    protected MethodMetaData m_metadata;

    /**
     * Creates a new throws join point.
     *
     * @param methodJoinPoint the method join point
     * @param exception the exception
     */
    public ThrowsJoinPoint(final MethodJoinPoint methodJoinPoint,
                           final Throwable exception) {
        if (methodJoinPoint == null) throw new IllegalArgumentException("method join point can not be null");
        if (exception == null) throw new IllegalArgumentException("exception exception can not be null");
        AspectWerkz.initialize();

        m_methodJoinPoint = methodJoinPoint;
        m_exception = exception;

        createMetaData();

        AspectWerkz.fakeStackTrace(m_exception, getTargetObjectsClassName());

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
                AspectWerkz.getAdvice(m_adviceIndexes[m_currentAdviceIndex]).doExecute(this);
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
                throw new RuntimeException(cause.toString());
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
    public int getLineNumberForThrow() {
        return m_exception.getStackTrace()[0].getLineNumber();
    }

    /**
     * Returns the method name where the exception was thrown.
     *
     * @return the method name
     */
    public String getMethodNameForThrow() {
        return m_exception.getStackTrace()[0].getMethodName();
    }

    /**
     * Returns the file name where the exception was thrown.
     *
     * @return the file name
     */
    public String getFileNameForThrow() {
        return m_exception.getStackTrace()[0].getFileName();
    }

    /**
     * Returns the class name where the exception was thrown.
     *
     * @return the class name
     */
    public String getClassNameForThrow() {
        return m_exception.getStackTrace()[0].getClassName();
    }

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
     * Returns the target object's class name.
     *
     * @return the target object's class name
     */
    public String getTargetObjectsClassName() {
        return m_methodJoinPoint.getTargetClass().getName();
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
            List aspects = AspectWerkz.getAspects(getTargetObjectsClassName());

            for (Iterator it = aspects.iterator(); it.hasNext();) {
                Aspect aspect = (Aspect)it.next();

                ThrowsPointcut[] pointcuts = aspect.
                        getThrowsPointcuts(m_metadata, getExceptionName());

                for (int i = 0; i < pointcuts.length; i++) {
                    int[] advices = pointcuts[i].getAdviceIndexes();
                    for (int j = 0; j < advices.length; j++) {
                        adviceIndexes.add(new Integer(advices[j]));
                    }
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
        m_metadata = new MethodMetaData();
        m_metadata.setName(getMethodName());
        Class[] parameterTypes = getMethodParameterTypes();
        String[] parameterTypeNames = new String[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            parameterTypeNames[i] = parameterTypes[i].getName();
        }
        m_metadata.setParameterTypes(parameterTypeNames);
        Class returnType = getMethodReturnType();
        if (returnType == null) {
            m_metadata.setReturnType("void");
        }
        else {
            m_metadata.setReturnType(returnType.getName());
        }
    }

    /**
     * Makes a deep copy of the join point.
     *
     * @return the clone of the join point
     */
    protected ThrowsJoinPoint deepCopy() {
        final ThrowsJoinPoint clone =
                new ThrowsJoinPoint(m_methodJoinPoint, m_exception);
        clone.m_currentAdviceIndex = m_currentAdviceIndex;
        clone.m_adviceIndexes = new int[m_adviceIndexes.length];
        System.arraycopy(m_adviceIndexes, 0, clone.m_adviceIndexes, 0, m_adviceIndexes.length);
        return clone;
    }

    // --- over-ridden methods ---
/*
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof ThrowsJoinPoint)) return false;
        final ThrowsJoinPoint obj = (ThrowsJoinPoint)o;
        return areEqualsOrBothNull(obj.m_methodJoinPoint, this.m_methodJoinPoint) &&
                areEqualsOrBothNull(obj.m_exception, this.m_exception) &&
                areEqualsOrBothNull(obj.m_adviceIndexes, this.m_adviceIndexes) &&
                (obj.m_currentAdviceIndex == this.m_currentAdviceIndex);
    }

    protected static boolean areEqualsOrBothNull(final Object o1, final Object o2) {
        if (null == o1) return (null == o2);
        return o1.equals(o2);
    }

    public String toString() {
        return "["
                + super.toString()
                + ": "
                + m_methodJoinPoint
                + "," + m_exception
                + "," + m_adviceIndexes
                + "," + m_currentAdviceIndex
                + "]";
    }

    public int hashCode() {
        int result = 17;
        result = 37 * result + hashCodeOrZeroIfNull(m_methodJoinPoint);
        result = 37 * result + hashCodeOrZeroIfNull(m_exception);
        result = 37 * result + hashCodeOrZeroIfNull(m_adviceIndexes);
        result = 37 * result + m_currentAdviceIndex;
        return result;
    }

    protected static int hashCodeOrZeroIfNull(final Object o) {
        if (null == o) return 19;
        return o.hashCode();
    }
*/
}
