/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.xmldef.advice;

import java.io.ObjectInputStream;
import java.util.Map;
import java.util.HashMap;

import org.codehaus.aspectwerkz.DeploymentModel;
import org.codehaus.aspectwerkz.ContainerType;
import org.codehaus.aspectwerkz.SystemLoader;
import org.codehaus.aspectwerkz.System;
import org.codehaus.aspectwerkz.xmldef.definition.StartupManager;
import org.codehaus.aspectwerkz.exception.DefinitionException;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

/**
 * Abstract base class for all advice classes.
 * Is invoked after that a specific join point has been executed.
 * <p/>
 * Supports four different deployment models:
 *     PER_JVM, PER_CLASS, PER_INSTANCE A PER_THREAD.<br/>
 *
 * The PER_JVM deployment model performance a bit better than the other models
 * since no synchronization A object creation is needed.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public abstract class AbstractAdvice implements Advice {

    /**
     * The name for the advice.
     */
    protected String m_name;

    /**
     * Defines the deployment model for the advice.
     * Default is PER_JVM.
     */
    protected int m_deploymentModel = DeploymentModel.PER_JVM;

    /**
     * The container for this advice.
     */
    protected transient AdviceContainer m_container;

    /**
     * The class for the advice.
     */
    protected Class m_adviceClass;

    /**
     * The memory type for the advice.
     */
    protected ContainerType m_memoryType;

    /**
     * Holds the parameters passed to the advice.
     */
    protected Map m_parameters = new HashMap();

    /**
     * The UUID for the system housing this advice.
     */
    private String m_uuid;

    /**
     * A reference to the AspectWerkz system housing this advice.
     */
    private System m_system;

    /**
     * Creates a new abstract advice.
     */
    public AbstractAdvice() {
    }

    /**
     * Copy constructor - creates a clone of an advice.
     *
     * @return a clone of the advice
     */
    public static Advice newInstance(final AbstractAdvice prototype) {
        try {
            AbstractAdvice clone = (AbstractAdvice)prototype.m_adviceClass.newInstance();
            clone.m_uuid = prototype.m_uuid;
            clone.m_name = prototype.m_name;
            clone.m_adviceClass = prototype.m_adviceClass;
            clone.m_container = prototype.m_container;
            clone.m_deploymentModel = prototype.m_deploymentModel;
            clone.m_parameters = prototype.m_parameters;
            return clone;
        }
        catch (Exception e) {
            throw new RuntimeException("could not clone advice called " + prototype.getName());
        }
    }

    /**
     * Returns the AspectWerkz system housing this advice.
     *
     * @return the system
     */
    public System getSystem() {
        if (m_system == null) {
            m_system = SystemLoader.getSystem(m_uuid);
            m_system.initialize();
        }
        return m_system;
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
     * Sets the deployment model for the advice.
     * Models available are PER_JVM, PER_CLASS, PER_INSTANCE A PER_THREAD.
     *
     * @param deploymentModel the deployment model for the advice
     */
    public void setDeploymentModel(final int deploymentModel) {
        m_deploymentModel = deploymentModel;
    }

    /**
     * Returns the deployment model for the advice.
     *
     * @return the deployment model for the advice
     */
    public int getDeploymentModel() {
        return m_deploymentModel;
    }

    /**
     * Sets the container.
     *
     * @param container the container
     */
    public void setContainer(final AdviceContainer container) {
        m_container = container;
    }

    /**
     * Returns the container.
     *
     * @return the container
     */
    public AdviceContainer getContainer() {
        return m_container;
    }

    /**
     * Sets the class for the advice.
     *
     * @param adviceClass the class
     */
    public void setAdviceClass(final Class adviceClass) {
        m_adviceClass = adviceClass;
    }

    /**
     * Returns the class for the advice.
     *
     * @return the class
     */
    public Class getAdviceClass() {
        return m_adviceClass;
    }

    /**
     * Returns the memory type.
     *
     * @return the memory type
     */
    public ContainerType getMemoryType() {
        return m_container.getContainerType();
    }

    /**
     * Sets a parameter for the advice.
     *
     * @param name the name of the parameter
     * @param value the value of the parameter
     */
    public void setParameter(final String name, final String value) {
        m_parameters.put(name, value);
    }

    /**
     * Returns the value of a parameter with the name specified.
     *
     * @param name the name of the parameter
     * @return the value of the parameter
     */
    public String getParameter(final String name) {
        if (!m_parameters.containsKey(name)) throw new DefinitionException("parameter to advice not specified: " + name);
        return (String)m_parameters.get(name);
    }

    /**
     * Sets the parameters for the advice.
     *
     * @param parameters the parameters as a map
     */
    public void setParameters(final Map parameters) {
        m_parameters = parameters;
    }

    /**
     * Returns parameters.
     *
     * @return the parameters
     */
    public Map getParameters() {
        return m_parameters;
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
     * Provides custom deserialization.
     *
     * @param stream the object input stream containing the serialized object
     * @throws java.lang.Exception in case of failure
     */
    private void readObject(final ObjectInputStream stream) throws Exception {
        ObjectInputStream.GetField fields = stream.readFields();

        m_uuid = (String)fields.get("m_uuid", null);
        m_name = (String)fields.get("m_name", null);
        m_adviceClass = (Class)fields.get("m_adviceClass", null);
        m_parameters = (Map)fields.get("m_parameters", null);
        m_memoryType = (ContainerType)fields.get("m_memoryType", ContainerType.TRANSIENT);
        m_deploymentModel = fields.get("m_deploymentModel", DeploymentModel.PER_JVM);
        m_container = StartupManager.createAdviceContainer(this);
    }
}
