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
import java.lang.reflect.Method;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.io.ObjectInputStream;

import org.codehaus.aspectwerkz.Aspect;
import org.codehaus.aspectwerkz.AspectWerkz;
import org.codehaus.aspectwerkz.definition.metadata.MethodMetaData;
import org.codehaus.aspectwerkz.pointcut.MethodPointcut;

/**
 * Matches well defined point of execution in the program where a
 * member method is executed.<br/>
 * Stores meta data from the join point.
 * I.e. a reference to original object and method, the parameters to
 * and the result from the original method invocation etc.<br/>
 * Handles the invocation of the advices added to the join point.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: MemberMethodJoinPoint.java,v 1.3 2003-06-09 08:24:49 jboner Exp $
 */
public class MemberMethodJoinPoint extends MethodJoinPoint {

    /**
     * The serial version uid for the class.
     * @todo recalculate
     */
    private static final long serialVersionUID = 1963482423844166453L;

    /**
     * A reference to the target instance.
     */
    protected Object m_targetObject;

    /**
     * Creates a new MemberMethodJoinPoint object.
     *
     * @param uuid the UUID for the AspectWerkz system to use
     * @param targetObject the target object
     * @param methodId the id of the original method
     */
    public MemberMethodJoinPoint(final String uuid,
                                 final Object targetObject,
                                 final int methodId) {
        super(uuid, methodId);
        if (targetObject == null) throw new IllegalArgumentException("target object can not be null");

        m_targetObject = targetObject;
        m_originalMethod = m_system.getMethod(m_targetObject.getClass(), methodId);

        createMetaData();

        // get all the aspects for this class
        List aspects = m_system.getAspects(getTargetClass().getName());

        List pointcuts = new ArrayList();
        for (Iterator it = aspects.iterator(); it.hasNext();) {

            // get the method pointcuts for each aspect
            MethodPointcut[] methodPointcuts =
                    ((Aspect)it.next()).getMethodPointcuts(m_metadata);
            for (int i = 0; i < methodPointcuts.length; i++) {
                pointcuts.add(methodPointcuts[i]);
            }
        }

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
     * @return the result from the previous invocation
     * @throws Throwable
     */
    public Object proceed() throws Throwable {
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
            try {
                result = m_originalMethod.invoke(m_targetObject, m_parameters);
                setResult(result);
            }
            catch (InvocationTargetException e) {
                handleException(e);
            }

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
     * Returns the original object.
     *
     * @return the original object
     */
    public Object getTargetObject() {
        return m_targetObject;
    }

    /**
     * Returns the original class.
     *
     * @return the original object
     */
    public Class getTargetClass() {
        return m_targetObject.getClass();
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
     * Makes a deep copy of the join point.
     *
     * @return the clone of the join point
     */
    protected MemberMethodJoinPoint deepCopy() {
        final MemberMethodJoinPoint clone =
                new MemberMethodJoinPoint(m_uuid, m_targetObject, m_methodId);
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
        if (!(o instanceof MemberMethodJoinPoint)) return false;
        final MemberMethodJoinPoint obj = (MemberMethodJoinPoint)o;
        return areEqualsOrBothNull(obj.m_originalMethod, this.m_originalMethod) &&
                areEqualsOrBothNull(obj.m_parameters, this.m_parameters) &&
                areEqualsOrBothNull(obj.m_targetObject, this.m_targetObject) &&
                areEqualsOrBothNull(obj.m_pointcuts, this.m_pointcuts) &&
                areEqualsOrBothNull(obj.m_result, this.m_result) &&
                areEqualsOrBothNull(obj.m_metadata, this.m_metadata) &&
                (obj.m_methodId == this.m_methodId) &&
                (obj.m_currentPointcutIndex == this.m_currentPointcutIndex) &&
                (obj.m_currentAdviceIndex == this.m_currentAdviceIndex);
    }

    /**
     * Provides custom deserialization.
     *
     * @param stream the object input stream containing the serialized object
     * @throws java.lang.Exception in case of failure
     */
    private void readObject(final ObjectInputStream stream) throws Exception {
        ObjectInputStream.GetField fields = stream.readFields();
        m_targetObject = fields.get("m_targetObject", null);
    }
}
