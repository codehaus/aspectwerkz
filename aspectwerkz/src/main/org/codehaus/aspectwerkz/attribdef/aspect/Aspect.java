/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.attribdef.aspect;

import java.io.Serializable;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.aspectwerkz.joinpoint.JoinPoint;
import org.codehaus.aspectwerkz.attribdef.definition.StartupManager;
import org.codehaus.aspectwerkz.attribdef.definition.AspectDefinition;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.exception.DefinitionException;
import org.codehaus.aspectwerkz.ContainerType;
import org.codehaus.aspectwerkz.DeploymentModel;
import org.codehaus.aspectwerkz.System;
import org.codehaus.aspectwerkz.SystemLoader;

/**
 * Abstract base class that all Aspect implementations must extend.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public abstract class Aspect implements Serializable {

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
     * The aspect definition.
     */
    private AspectDefinition m_aspectDef;

    /**
     * The target instance for this aspect (is null if not deployed as perInstance)
     */
    private Object m_targetInstance = null;

    /**
     * The target class for this aspect (is null if not deployed as perClass)
     */
    private Object m_targetClass = null;

    /**
     * Creates a new abstract advice.
     */
    public Aspect() {
    }

    /**
     * Copy constructor - creates a clone of an advice.
     *
     * @return a clone of the advice
     */
    public static Aspect newInstance(final Aspect prototype) {
        try {
            Aspect clone = (Aspect)prototype.m_aspectClass.newInstance();
            clone.m_uuid = prototype.m_uuid;
            clone.m_name = prototype.m_name;
            clone.m_aspectClass = prototype.m_aspectClass;
            clone.m_container = prototype.m_container;
            clone.m_deploymentModel = prototype.m_deploymentModel;
            clone.m_parameters = prototype.m_parameters;

            return clone;
        }
        catch (Exception e) {
            throw new RuntimeException("could not clone aspect [" + prototype.___AW_getName() + "]");
        }
    }

    /**
     * Returns the AspectWerkz system housing this advice.
     *
     * @return the system
     */
    public System ___AW_getSystem() {
        if (m_system == null) {
            m_system = SystemLoader.getSystem(m_uuid);
            m_system.initialize();
        }
        return m_system;
    }

    /**
     * Invokes an introduced method with the index specified.
     *
     * @param methodIndex the method index
     * @param joinPoint the join point
     * @return the result from the invocation
     */
    public Object ___AW_invokeAdvice(final int methodIndex, final JoinPoint joinPoint) {
        try {
            Object result = null;
            switch (m_deploymentModel) {

                case DeploymentModel.PER_JVM:
                    result = ___AW_invokeAdvicePerJvm(methodIndex, joinPoint);
                    break;

                case DeploymentModel.PER_CLASS:
                    result = ___AW_invokeAdvicePerClass(methodIndex, joinPoint);
                    break;

                case DeploymentModel.PER_INSTANCE:
                    result = ___AW_invokeAdvicePerInstance(methodIndex, joinPoint);
                    break;

                case DeploymentModel.PER_THREAD:
                    result = ___AW_invokeAdvicePerThread(methodIndex, joinPoint);
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
     * @param joinPoint the join point
     * @return the result from the method invocation
     */
    private Object ___AW_invokeAdvicePerJvm(final int methodIndex, final JoinPoint joinPoint) {
        return m_container.invokeAdvicePerJvm(methodIndex, joinPoint);
    }

    /**
     * Invokes an introduced method on a per class basis.
     *
     * @param methodIndex the method index
     * @param joinPoint the join point
     * @return the result from the method invocation
     */
    private Object ___AW_invokeAdvicePerClass(final int methodIndex, final JoinPoint joinPoint) {
        return m_container.invokeAdvicePerClass(methodIndex, joinPoint);
    }

    /**
     * Invokes an introduced method on a per instance basis.
     *
     * @param methodIndex the method index
     * @param joinPoint the join point
     * @return the result from the method invocation
     */
    private Object ___AW_invokeAdvicePerInstance(final int methodIndex, final JoinPoint joinPoint) {
        return m_container.invokeAdvicePerInstance(methodIndex, joinPoint);
    }

    /**
     * Invokes an introduced method on a per thread basis.
     *
     * @param methodIndex the method index
     * @param joinPoint the join point
     * @return the result from the method invocation
     */
    private Object ___AW_invokeAdvicePerThread(final int methodIndex, final JoinPoint joinPoint) {
        return m_container.invokeAdvicePerThread(methodIndex, joinPoint);
    }

    /**
     * Sets the name of the aspect.
     *
     * @param name the name of the aspect
     */
    public void ___AW_setName(final String name) {
        m_name = name;
    }

    /**
     * Returns the name of the aspect.
     *
     * @return the name of the aspect
     */
    public String ___AW_getName() {
        return m_name;
    }

    /**
     * Sets the deployment model.
     *
     * @param deploymentModel the deployment model
     */
    public void ___AW_setDeploymentModel(final int deploymentModel) {
        m_deploymentModel = deploymentModel;
    }

    /**
     * Returns the deployment model.
     *
     * @return the deployment model
     */
    public int ___AW_getDeploymentModel() {
        return m_deploymentModel;
    }

    /**
     * Returns the aspect class.
     *
     * @return the aspect class
     */
    public Class ___AW_getAspectClass() {
        return m_aspectClass;
    }

    /**
     * Sets the aspect class.
     *
     * @param aspectClass the aspect class
     */
    public void ___AW_setAspectClass(final Class aspectClass) {
        m_aspectClass = aspectClass;
    }

    /**
     * Sets the container.
     *
     * @param container the container
     */
    public void ___AW_setContainer(final AspectContainer container) {
        m_container = container;
    }

    /**
     * Returns the container.
     *
     * @return the container
     */
    public AspectContainer ___AW_getContainer() {
        return m_container;
    }

    /**
     * Returns the container type.
     *
     * @return the container type
     */
    public ContainerType ___AW_getContainerType() {
        return m_container.getContainerType();
    }

    /**
     * Returns the aspect definition.
     *
     * @return the aspect definition
     */
    public AspectDefinition ___AW_getAspectDef() {
        return m_aspectDef;
    }

    /**
     * Sets the aspect definition.
     *
     * @param aspectDef the aspect definition
     */
    public void ___AW_setAspectDef(final AspectDefinition aspectDef) {
        m_aspectDef = aspectDef;
    }

    /**
     * Returns the target instance if aspect is deployed as 'perInstance' otherwise null.
     *
     * @return the target instance
     */
    public Object ___AW_getTargetInstance() {
        return m_targetInstance;
    }

    /**
     * Sets the target instance.
     *
     * @param targetInstance the target instance
     */
    public void ___AW_setTargetInstance(final Object targetInstance) {
        m_targetInstance = targetInstance;
        m_targetClass = targetInstance.getClass();
    }

    /**
     * Returns the target class if aspect is deployed as 'perInstance' or 'perClass' otherwise null.
     *
     * @return the target class
     */
    public Object ___AW_getTargetClass() {
        return m_targetClass;
    }

    /**
     * Sets the target class.
     *
     * @param targetClass the target class
     */
    public void ___AW_setTargetClass(final Object targetClass) {
        m_targetClass = targetClass;
    }

    /**
     * Sets a parameter for the advice.
     *
     * @param name the name of the parameter
     * @param value the value of the parameter
     */
    public void ___AW_setParameter(final String name, final String value) {
        m_parameters.put(name, value);
    }

    /**
     * Returns the value of a parameter with the name specified.
     *
     * @param name the name of the parameter
     * @return the value of the parameter
     */
    public String ___AW_getParameter(final String name) {
        if (!m_parameters.containsKey(name)) throw new DefinitionException("parameter to advice not specified: " + name);
        return (String)m_parameters.get(name);
    }

    /**
     * Returns the target instance for the mixin of given name which is defined
     * from within this aspect (mixin can have different deployment model from aspect)
     *
     * @param mixinName of the mixin
     * @param mixinImpl miximImplementation aka "this" when called from within the mixin impl
     * @return the target instance or null if not compliant deployment model
     */
    public Object ___AW_getMixinTargetInstance(String mixinName, Object mixinImpl) {
        return m_container.getIntroductionContainer(mixinName).getTargetInstance(mixinImpl);
    }

    /**
     * Returns the target class for the mixin of given name which is defined
     * from within this aspect (mixin can have different deployment model from aspect)
     *
     * @param mixinName of the mixin
     * @param mixinImpl miximImplementation aka "this" when called from within the mixin impl
     * @return the target class or null if not compliant deployment model
     */
    public Class ___AW_getMixinTargetClass(String mixinName, Object mixinImpl) {
        return m_container.getIntroductionContainer(mixinName).getTargetClass(mixinImpl);
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
        m_targetInstance = fields.get("m_targetInstance", null);
        m_targetClass = fields.get("m_targetClass", null);
        m_containerType = (ContainerType)fields.get("m_containerType", ContainerType.TRANSIENT);
        m_deploymentModel = fields.get("m_deploymentModel", DeploymentModel.PER_JVM);
        m_aspectDef = (AspectDefinition)fields.get("m_aspectDef", null);
        m_parameters = (Map)fields.get("m_parameters", null);
        m_container = StartupManager.createAspectContainer(this);
        m_system = SystemLoader.getSystem(m_uuid);
        m_system.initialize();
//        m_parameters = (Map)fields.get("m_parameters", null);
    }
}
