/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz;

import java.io.Serializable;
import java.io.ObjectInputStream;

import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.definition2.StartupManager2;

/**
 * Abstract base class that all Aspect implementations must extend.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public abstract class AbstractAspect implements Serializable {

    /**
     * An empty <code>Object</code> array.
     */
    public static final Object[] EMPTY_OBJECT_ARRAY = new Object[]{};

    /**
     * The name for the aspect.
     */
    protected String m_name;

    /**
     * The class for the aspect.
     */
    protected Class m_aspectClass;

    /**
     * Holds the deployment model.
     */
    protected int m_deploymentModel;

    /**
     * The container strategy for this aspect.
     */
    protected transient AspectContainer m_container;

    /**
     * The container type for the aspect.
     */
    protected ContainerType m_containerType;

    /**
     * The UUID for the system housing this advice.
     */
    private String m_uuid;

    /**
     * A reference to the AspectWerkz system housing this advice.
     */
    private AspectWerkz m_system;

    /**
     * Creates a new abstract advice.
     */
    public AbstractAspect() {
    }

    /**
     * Copy constructor - creates a clone of an advice.
     *
     * @return a clone of the advice
     */
    public static AbstractAspect newInstance(final AbstractAspect prototype) {
        try {
            AbstractAspect clone = (AbstractAspect)prototype.m_aspectClass.newInstance();
            clone.m_uuid = prototype.m_uuid;
            clone.m_name = prototype.m_name;
            clone.m_aspectClass = prototype.m_aspectClass;
            clone.m_container = prototype.m_container;
            clone.m_deploymentModel = prototype.m_deploymentModel;
//            clone.m_parameters = prototype.m_parameters;
            return clone;
        }
        catch (Exception e) {
            throw new RuntimeException("could not clone aspect called " + prototype.getName());
        }
    }

    /**
     * Returns the AspectWerkz system housing this advice.
     *
     * @return the system
     */
    public AspectWerkz getSystem() {
        if (m_system == null) {
            m_system = AspectWerkz.getSystem(m_uuid);
            m_system.initialize();
        }
        return m_system;
    }

    /**
     * Returns the sole per JVM advice.
     *
     * @param joinPoint the joint point
     * @return the advice
     */
    public Object getPerJvmAdvice(final JoinPoint joinPoint) {
        return m_container.getPerJvmAdvice(joinPoint);
    }

    /**
     * Returns the advice for the class, of the original object.
     * If the cache is empty; create a new advice A cache it.
     *
     * @param joinPoint the join point
     * @return the advice
     */
    protected Object getPerClassAdvice(final JoinPoint joinPoint) {
        return m_container.getPerClassAdvice(joinPoint);
    }

    /**
     * Returns the advice for the instance of the original object.
     * If the cache is empty; create a new advice A cache it.
     *
     * @param joinPoint the join point
     * @return the advice
     */
    protected Object getPerInstanceAdvice(final JoinPoint joinPoint) {
        return m_container.getPerInstanceAdvice(joinPoint);
    }

    /**
     * Returns the advice for the current thread.
     * If the cache is empty; create a new advice A cache it.
     *
     * @return the advice
     */
    protected Object getPerThreadAdvice() {
        return m_container.getPerThreadAdvice();
    }

    /**
     * Invokes the method with the index specified.
     * Invoked by methods without any parameters (slight performance gain since
     * we are saving us one array creation).
     *
     * @param methodIndex the method index
     * @param callingObject a reference to the calling object
     * @return the result from the invocation
     */
    public Object invoke(final int methodIndex, final Object callingObject) {
        return invoke(methodIndex, EMPTY_OBJECT_ARRAY, callingObject);
    }

    /**
     * Invokes an introduced method with the index specified.
     *
     * @param methodIndex the method index
     * @param parameters the parameters for the invocation
     * @param callingObject a reference to the calling object
     * @return the result from the invocation
     */

    public Object invoke(final int methodIndex,
                         final Object[] parameters,
                         final Object callingObject) {
        try {
            Object result = null;
            switch (m_deploymentModel) {

                case DeploymentModel.PER_JVM:
                    result = invokeIntroductionPerJvm(methodIndex, parameters);
                    break;

                case DeploymentModel.PER_CLASS:
                    result = invokeIntroductionPerClass(callingObject, methodIndex, parameters);
                    break;

                case DeploymentModel.PER_INSTANCE:
                    result = invokeIntroductionPerInstance(callingObject, methodIndex, parameters);
                    break;

                case DeploymentModel.PER_THREAD:
                    result = invokeIntroductionPerThread(methodIndex, parameters);
                    break;

                default:
                    throw new RuntimeException("invalid deployment model: " + m_deploymentModel);
            }
            return result;
        }
        catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Invokes an introduced method on a per JVM basis.
     *
     * @param methodIndex the method index
     * @param parameters the parameters for the invocation
     * @return the result from the method invocation
     */
    private Object invokeIntroductionPerJvm(final int methodIndex, final Object[] parameters) {
        return m_container.invokeIntroductionPerJvm(methodIndex, parameters);
    }

    /**
     * Invokes an introduced method on a per class basis.
     *
     * @param callingObject a reference to the calling object
     * @param methodIndex the method index
     * @param parameters the parameters for the invocation
     * @return the result from the method invocation
     */
    private Object invokeIntroductionPerClass(final Object callingObject,
                                              final int methodIndex,
                                              final Object[] parameters) {
        return m_container.invokeIntroductionPerClass(callingObject, methodIndex, parameters);
    }

    /**
     * Invokes an introduced method on a per instance basis.
     *
     * @param callingObject a reference to the calling object
     * @param methodIndex the method index
     * @param parameters the parameters for the invocation
     * @return the result from the method invocation
     */
    private Object invokeIntroductionPerInstance(final Object callingObject,
                                                 final int methodIndex,
                                                 final Object[] parameters) {
        return m_container.invokeIntroductionPerInstance(callingObject, methodIndex, parameters);
    }

    /**
     * Invokes an introduced method on a per thread basis.
     *
     * @param methodIndex the method index
     * @param parameters the parameters for the invocation
     * @return the result from the method invocation
     */
    private Object invokeIntroductionPerThread(final int methodIndex, final Object[] parameters) {
        return m_container.invokeIntroductionPerThread(methodIndex, parameters);
    }

    /**
     * Sets the name of the advice.
     *
     * @param name the name of the advice
     */
    public void setName(final String name) {
        m_name = name;
    }

    /**
     * Returns the name of the advice.
     *
     * @return the name of the advice
     */
    public String getName() {
        return m_name;
    }

    /**
     * Sets the deployment model.
     *
     * @param the deployment model
     */
    public void setDeploymentModel(final int deploymentModel) {
        m_deploymentModel = deploymentModel;
    }

    /**
     * Returns the deployment model.
     *
     * @return the deployment model
     */
    public int getDeploymentModel() {
        return m_deploymentModel;
    }

    /**
     * Returns the aspect class.
     *
     * @return the aspect class
     */
    public Class getAspectClass() {
        return m_aspectClass;
    }

    /**
     * Sets the aspect class.
     *
     * @param aspectClass the aspect class
     */
    public void setAspectClass(final Class aspectClass) {
        m_aspectClass = aspectClass;
    }

    /**
     * Sets the container.
     *
     * @param container the container
     */
    public void setContainer(final AspectContainer container) {
        m_container = container;
    }

    /**
     * Returns the container.
     *
     * @return the container
     */
    public AspectContainer getContainer() {
        return m_container;
    }

    /**
     * Returns the container type.
     *
     * @return the container type
     */
    public ContainerType getContainerType() {
        return m_container.getContainerType();
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
        m_name = (String)fields.get("m_name", null);
        m_aspectClass = (Class)fields.get("m_aspectClass", null);
        m_containerType = (ContainerType)fields.get("m_containerType", ContainerType.TRANSIENT);
        m_deploymentModel = fields.get("m_deploymentModel", DeploymentModel.PER_JVM);
        m_container = StartupManager2.createAspectContainer(this);
        m_system = AspectWerkz.getSystem(m_uuid);
        m_system.initialize();
//        m_parameters = (Map)fields.get("m_parameters", null);
    }
}
