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
import java.lang.reflect.InvocationTargetException;
import java.util.StringTokenizer;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import org.codehaus.aspectwerkz.AspectWerkz;
import org.codehaus.aspectwerkz.Aspect;
import org.codehaus.aspectwerkz.definition.metadata.MethodMetaData;
import org.codehaus.aspectwerkz.pointcut.MethodPointcut;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.transform.TransformationUtil;

/**
 * Matches well defined point of execution in the program where a
 * method is executed.<br/>
 * Stores meta data from the join point.
 * I.e. a reference to original object an method, the parameters to and
 * the result from the original method invocation etc.<br/>
 * Handles the invocation of the advices added to the join point.
 *
 * @author <a href="mailto:jboner@acm.org">Jonas Bonér</a>
 * @version $Id: MethodJoinPoint.java,v 1.1.1.1 2003-05-11 15:14:30 jboner Exp $
 */
public abstract class MethodJoinPoint implements JoinPoint {

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
    protected final int m_methodId;

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
     * Creates a new MethodJoinPoint object.
     *
     * @param methodId the id of the method
     */
    public MethodJoinPoint(final int methodId) {
        if (methodId < 0) throw new IllegalArgumentException("method id can not be less that zero");
        AspectWerkz.initialize();
        m_methodId = methodId;
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
     * Invokes the next advice in the chain and when it reaches the end
     * of the chain the original method.
     *
     * @return the result from the invocation
     */
    public abstract Object proceed() throws Throwable;

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
        List pointcuts = new ArrayList();
        List aspects = AspectWerkz.getAspects(getTargetClass().getName());

        for (Iterator it = aspects.iterator(); it.hasNext();) {
            Aspect aspect = (Aspect)it.next();
            if (aspect.hasThrowsPointcut(m_metadata)) {
                pointcuts.add(aspect.createMethodPointcut(
                        createMethodPattern()));
                break;
            }
        }

        m_pointcuts = new MethodPointcut[pointcuts.size()];
        int i = 0;
        for (Iterator it = pointcuts.iterator(); it.hasNext(); i++) {
            m_pointcuts[i] = (MethodPointcut)it.next();
        }
    }

    /**
     * Handles the exceptions.
     * If the method is registered in a ThrowsPointcut redirect to the
     * ThrowsJoinPoint in question, otherwise just get the original exception,
     * fake the stacktrace and rethrow it.
     *
     * @param e the wrapped exception
     * @throws Throwable the original exception
     */
    protected void handleException(final InvocationTargetException e)
            throws Throwable {

        final Throwable cause = e.getCause();
        List aspects = AspectWerkz.getAspects(getTargetClass().getName());
        boolean hasThrowsPointcut = false;

        for (Iterator it = aspects.iterator(); it.hasNext();) {
            Aspect aspect = (Aspect)it.next();
            if (aspect.hasThrowsPointcut(
                    getMethodName(),
                    cause.getClass().getName())) {
                hasThrowsPointcut = true;
                break;
            }
        }
        if (hasThrowsPointcut) {
            final ThrowsJoinPoint joinPoint = new ThrowsJoinPoint(this, cause);
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
