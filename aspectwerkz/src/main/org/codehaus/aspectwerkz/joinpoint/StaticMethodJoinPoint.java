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

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Iterator;
import java.io.ObjectInputStream;

import org.codehaus.aspectwerkz.pointcut.MethodPointcut;
import org.codehaus.aspectwerkz.metadata.MethodMetaData;
import org.codehaus.aspectwerkz.metadata.ReflectionMetaDataMaker;

/**
 * Mathes well defined point of execution in the program where a static method
 * is executed.<br/>Stores meta data from the join point. I.e. a reference to
 * original object and method, the parameters to and the result from the
 * original method invocation etc.<br/>Handles the invocation of the advices
 * added to the join point.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: StaticMethodJoinPoint.java,v 1.6 2003-06-26 19:27:17 jboner Exp $
 */
public class StaticMethodJoinPoint extends MethodJoinPoint {

    /**
     * The serial version uid for the class.
     * @todo recalculate
     */
    private static final long serialVersionUID = 5798526226816577476L;

    /**
     * Creates a new MemberMethodJoinPoint object.
     *
     * @param uuid the UUID for the AspectWerkz system to use
     * @param targetClass the target class
     * @param methodId the id of the original method
     */
    public StaticMethodJoinPoint(final String uuid,
                                 final Class targetClass,
                                 final int methodId) {
        super(uuid, methodId);
        if (targetClass == null) throw new IllegalArgumentException("target class can not be null");

        m_targetClass = targetClass;
        m_originalMethod = m_system.getMethod(m_targetClass, methodId);

        createMetaData();

        // get all the pointcuts for this class
        List pointcuts = m_system.getMethodPointcuts(
                getTargetClass().getName(), m_metadata);

        // put the pointcuts in the pointcut array
        m_pointcuts = new MethodPointcut[pointcuts.size()];
        int i = 0;
        for (Iterator it = pointcuts.iterator(); it.hasNext(); i++) {
            m_pointcuts[i] = (MethodPointcut)it.next();
        }

        if (m_pointcuts.length == 0) {
            // we are at this point with no poincuts defined => the method has
            // a ThrowsJoinPoint defined at this method, since the method is
            // already advised, create a method pointcut for this method anyway
            handleThrowsPointcut();
        }
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
     * Walks through the pointcuts and invokes all its advices. When the last
     * advice of the last pointcut has been invoked, the original method is
     * invoked. Is called recursively.
     *
     * @return the result from the next invocation
     * @throws Throwable
     */
    public Object proceed() throws Throwable {
        if (m_pointcuts.length == 0) {
            // no pointcuts defined; invoke original method directly
            return invokeOriginalMethod();
        }

        Object result = null;
        boolean pointcutSwitch = false;

        m_currentAdviceIndex++;

        // if we are out of advices; try the next pointcut
        if (m_currentAdviceIndex == m_pointcuts[m_currentPointcutIndex].
                getAdviceIndexes().length &&
                m_currentPointcutIndex < m_pointcuts.length - 1) {
            m_currentPointcutIndex++;
            m_currentAdviceIndex = 0; // start with the first advice again
            pointcutSwitch = true; // mark this call as a pointcut switch
        }

        // if we are out of advices and pointcuts; invoke the original method
        if (m_currentAdviceIndex == m_pointcuts[m_currentPointcutIndex].
                getAdviceIndexes().length &&
                m_currentPointcutIndex == m_pointcuts.length - 1) {

            result = invokeOriginalMethod();

            if (pointcutSwitch) {
                m_currentPointcutIndex--; // switch back to the previous pointcut
                m_currentAdviceIndex = 0; // start with the first advice
            }
            m_currentAdviceIndex--;

            return result;
        }
        else {
            // invoke the next advice in the current pointcut
            try {
                result = m_system.getAdvice(
                        m_pointcuts[m_currentPointcutIndex].
                        getAdviceIndex(m_currentAdviceIndex)).
                        doExecute(this);
            }
            catch (ArrayIndexOutOfBoundsException ex) {
                throw new RuntimeException(
                        createAdviceNotCorrectlyMappedMessage());
            }

            if (pointcutSwitch) {
                m_currentPointcutIndex--; // switch back to the previous pointcut
                m_currentAdviceIndex = 0; // start with the first advice
            }
            m_currentAdviceIndex--;

            return result;
        }
    }

    /**
     * Returns the original class.
     *
     * @return the original class
     */
    public Class getTargetClass() {
        return m_targetClass;
    }

    /**
     * Returns the original object.
     * Always returns null since it is a static join point.
     *
     * @return null always returns null
     */
    public Object getTargetObject() {
        return null;
    }

    /**
     * Invokes the origignal method.
     *
     * @return the result from the method invocation
     * @throws Throwable the exception from the original method
     */
    protected Object invokeOriginalMethod() throws Throwable {
        Object result = null;
        try {
            result = m_originalMethod.invoke(null, m_parameters);
            setResult(result);
        }
        catch (InvocationTargetException e) {
            handleException(e);
        }
        return result;
    }

    /**
     * Creates meta-data for the join point.
     */
    protected void createMetaData() {
        m_metadata = ReflectionMetaDataMaker.createMethodMetaData(
                getMethodName(),
                getParameterTypes(),
                getReturnType());
    }

    /**
     * Makes a deep copy of the join point.
     *
     * @return the clone of the join point
     */
    protected StaticMethodJoinPoint deepCopy() {
        final StaticMethodJoinPoint clone =
                new StaticMethodJoinPoint(m_uuid, m_targetClass, m_methodId);
        clone.m_originalMethod = m_originalMethod;
        clone.m_pointcuts = m_pointcuts;
        clone.m_currentAdviceIndex = m_currentAdviceIndex;
        clone.m_currentPointcutIndex = m_currentPointcutIndex;
        clone.m_parameters = m_parameters;
        clone.m_result = m_result;
        clone.m_metadata = m_metadata;
        return clone;
    }

    /**
     * The overridden equals method.
     *
     * @param o the other object
     * @return boolean
     */
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof StaticMethodJoinPoint)) return false;
        final StaticMethodJoinPoint obj = (StaticMethodJoinPoint)o;
        return areEqualsOrBothNull(obj.m_originalMethod, this.m_originalMethod) &&
                areEqualsOrBothNull(obj.m_parameters, this.m_parameters) &&
                areEqualsOrBothNull(obj.m_targetClass, this.m_targetClass) &&
                areEqualsOrBothNull(obj.m_pointcuts, this.m_pointcuts) &&
                areEqualsOrBothNull(obj.m_result, this.m_result) &&
                areEqualsOrBothNull(obj.m_metadata, this.m_metadata) &&
                (obj.m_methodId == this.m_methodId) &&
                (obj.m_currentAdviceIndex == this.m_currentAdviceIndex) &&
                (obj.m_currentPointcutIndex == this.m_currentPointcutIndex);
    }
    /**
     * Provides custom deserialization.
     *
     * @param stream the object input stream containing the serialized object
     * @throws java.lang.Exception in case of failure
     */
//    private void readObject(final ObjectInputStream stream) throws Exception {
//        ObjectInputStream.GetField fields = stream.readFields();
//    }
}
