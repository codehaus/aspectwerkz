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

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Iterator;
import java.io.ObjectInputStream;

import org.codehaus.aspectwerkz.metadata.ReflectionMetaDataMaker;
import org.codehaus.aspectwerkz.pointcut.MethodPointcut;

/**
 * Matches well defined point of execution in the program where a
 * member method is executed.<br/>
 * Stores meta data from the join point.
 * I.e. a reference to original object A method, the parameters to
 * A the result from the original method invocation etc.<br/>
 * Handles the invocation of the advices added to the join point.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 * @version $Id: MemberMethodJoinPoint.java,v 1.9 2003-07-08 16:44:17 jboner Exp $
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
        m_targetClass = targetObject.getClass();

        m_originalMethod = m_system.getMethod(m_targetClass, m_methodId);
        m_originalMethod.setAccessible(true);

        createMetaData();

        // get all the pointcuts for this class
        List pointcuts = m_system.getMethodPointcuts(getTargetClass().getName(), m_metadata);

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

        // get the cflow pointcuts that affects this join point
        m_cflowPointcuts = m_system.getCFlowPointcuts(m_targetClass.getName(), m_metadata);
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
        return m_targetClass;
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
     * Invokes the origignal method.
     *
     * @return the result from the method invocation
     * @throws Throwable the exception from the original method
     */
    protected Object invokeOriginalMethod() throws Throwable {
        Object result = null;
        try {
            result = m_originalMethod.invoke(m_targetObject, m_parameters);
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
    protected MemberMethodJoinPoint deepCopy() {
        final MemberMethodJoinPoint clone =
                new MemberMethodJoinPoint(m_uuid, m_targetObject, m_methodId);
        clone.m_targetClass = m_targetClass;
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
                areEqualsOrBothNull(obj.m_targetClass, this.m_targetClass) &&
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
