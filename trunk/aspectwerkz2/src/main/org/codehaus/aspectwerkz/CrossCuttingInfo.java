/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz;

import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.lang.*;

import org.codehaus.aspectwerkz.aspect.AspectContainer;
import org.codehaus.aspectwerkz.definition.AspectDefinition;
import org.codehaus.aspectwerkz.exception.DefinitionException;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @TODO document
 */
public class CrossCuttingInfo implements Serializable {

    /**
     * An empty <code>Object</code> array.
     */
    public static final Object[] EMPTY_OBJECT_ARRAY = new Object[]{};

    /**
     * The name for the cross-cuttable class.
     */
    protected String m_name;

    /**
     * The cross-cuttable class.
     */
    protected Class m_aspectClass;

    /**
     * Holds the deployment model.
     */
    protected int m_deploymentModel;

    /**
     * The container strategy for this cross-cuttable class.
     */
    protected transient AspectContainer m_container;

    /**
     * Holds the parameters passed to the advice.
     */
    protected Map m_parameters = new HashMap();

    /**
     * The UUID for the system housing this advice.
     */
    private String m_uuid;

    /**
     * A reference to the AspectWerkz system housing this cross-cuttable class.
     */
    private System m_system;

    /**
     * The aspect definition.
     */
    private AspectDefinition m_aspectDef;

    /**
     * The target instance for this cross-cuttable class (is null if not deployed as perInstance)
     */
    private Object m_targetInstance = null;

    /**
     * The target class for this cross-cuttable class (is null if not deployed as perClass)
     */
    private Class m_targetClass = null;

    /**
     * The constructor for the aspect class.
     */
    private Constructor m_aspectConstructor = null;

    /**
     * Creates a new cross-cutting info instance.
     */
    public CrossCuttingInfo() {
    }

    /**
     * Copy constructor - creates a clone of the cross-cutting info.
     *
     * @return a clone of the cross-cutting info
     */
    public static CrossCuttingInfo newInstance(final CrossCuttingInfo prototype) {
        try {
            CrossCuttingInfo clone = new CrossCuttingInfo();
            clone.m_uuid = prototype.m_uuid;
            clone.m_name = prototype.m_name;
            clone.m_aspectClass = prototype.m_aspectClass;
            clone.m_aspectConstructor = prototype.m_aspectConstructor;
            clone.m_container = prototype.m_container;
            clone.m_deploymentModel = prototype.m_deploymentModel;
            clone.m_parameters = prototype.m_parameters;

            return clone;
        }
        catch (Exception e) {
            throw new RuntimeException(
                    "could not clone cross-cutting info [" + prototype.getName() + "]: " + e.toString()
            );
        }
    }

    /**
     * Returns the AspectWerkz system housing this cross-cuttable class.
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
     * Invokes an introduced method with the index specified.
     *
     * @param methodIndex the method index
     * @param joinPoint   the join point
     * @return the result from the invocation
     */
    public Object invokeAdvice(final int methodIndex, final JoinPoint joinPoint) throws Throwable {
        Object result = null;
        switch (m_deploymentModel) {

            case DeploymentModel.PER_JVM:
                result = m_container.invokeAdvicePerJvm(methodIndex, joinPoint);
                break;

            case DeploymentModel.PER_CLASS:
                result = m_container.invokeAdvicePerClass(methodIndex, joinPoint);
                break;

            case DeploymentModel.PER_INSTANCE:
                result = m_container.invokeAdvicePerInstance(methodIndex, joinPoint);
                break;

            case DeploymentModel.PER_THREAD:
                result = m_container.invokeAdvicePerThread(methodIndex, joinPoint);
                break;

            default:
                throw new RuntimeException("invalid deployment model: " + m_deploymentModel);
        }
        return result;
    }

    /**
     * Retrieves an advice method with the index specified.
     *
     * @param methodIndex the method index
     * @return the advice method
     */
    public Method getAdvice(final int methodIndex) {
        return m_container.getAdvice(methodIndex);
    }

    /**
     * Retrieves all advice.
     *
     * @return the advice
     */
    public Method[] getAdvice() {
        return m_container.getAdvice();
    }

    /**
     * Sets the UUID for the system.
     *
     * @param uuid the UUID for the system
     */
    public void setUuid(final String uuid) {
        m_uuid = uuid;
    }

    /**
     * Returns the UUID for the system.
     *
     * @return the UUID for the system
     */
    public String getUuid() {
        return m_uuid;
    }

    /**
     * Sets the name of the aspect.
     *
     * @param name the name of the aspect
     */
    public void setName(final String name) {
        m_name = name;
    }

    /**
     * Returns the name of the aspect.
     *
     * @return the name of the aspect
     */
    public String getName() {
        return m_name;
    }

    /**
     * Sets the deployment model.
     *
     * @param deploymentModel the deployment model
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
     * Returns the cross-cuttable class' constructor.
     *
     * @return the cross-cuttable class' constructor
     */
    public Constructor getAspectConstructor() {
//        if (m_aspectConstructor == null) {
//            try {
//                m_aspectConstructor = m_aspectClass.getConstructor(new Class[]{CrossCuttingInfo.class});
//            }
//            catch (NoSuchMethodException e) {
//                throw new WrappedRuntimeException(e);
//            }
//        }
        return m_aspectConstructor;
    }

    /**
     * Returns the cross-cuttable class.
     *
     * @return the cross-cuttable class
     */
    public Class getAspectClass() {
        return m_aspectClass;
    }

    /**
     * Sets the cross-cuttable class.
     *
     * @param klass the cross-cuttable class
     */
    public void setAspectClass(final Class klass) {
        m_aspectClass = klass;
              try {
                m_aspectConstructor = m_aspectClass.getConstructor(new Class[]{CrossCuttingInfo.class});
            }
            catch (NoSuchMethodException e) {
                throw new WrappedRuntimeException(e);
            }
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
     * Returns the aspect definition.
     *
     * @return the aspect definition
     */
    public AspectDefinition getAspectDefinition() {
        return m_aspectDef;
    }

    /**
     * Sets the aspect definition.
     *
     * @param aspectDef the aspect definition
     */
    public void setAspectDef(final AspectDefinition aspectDef) {
        m_aspectDef = aspectDef;
    }

    /**
     * Returns the target instance if aspect is deployed as 'perInstance' otherwise null.
     *
     * @return the target instance
     */
    public Object getTargetInstance() {
        return m_targetInstance;
    }

    /**
     * Sets the target instance.
     *
     * @param targetInstance the target instance
     */
    public void setTargetInstance(final Object targetInstance) {
        m_targetInstance = targetInstance;
        m_targetClass = targetInstance.getClass();
    }

    /**
     * Returns the target class if aspect is deployed as 'perInstance' or 'perClass' otherwise null.
     *
     * @return the target class
     */
    public Class getTargetClass() {
        return m_targetClass;
    }

    /**
     * Sets the target class.
     *
     * @param targetClass the target class
     */
    public void setTargetClass(final Class targetClass) {
        m_targetClass = targetClass;
    }

    /**
     * Sets a parameter for the advice.
     *
     * @param name  the name of the parameter
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
        if (!m_parameters.containsKey(name)) {
            throw new DefinitionException("parameter to advice not specified: " + name);
        }
        return (String)m_parameters.get(name);
    }

    /**
     * Returns the target instance for the mixin of given name which is defined from within this aspect (mixin can have
     * different deployment model from aspect)
     *
     * @param mixinName of the mixin
     * @param mixinImpl miximImplementation aka "this" when called from within the mixin impl
     * @return the target instance or null if not compliant deployment model
     */
    public Object getMixinTargetInstance(String mixinName, Object mixinImpl) {
        return m_container.getIntroductionContainer(mixinName).getTargetInstance(mixinImpl);
    }

    /**
     * Returns the target class for the mixin of given name which is defined from within this aspect (mixin can have
     * different deployment model from aspect)
     *
     * @param mixinName of the mixin
     * @param mixinImpl miximImplementation aka "this" when called from within the mixin impl
     * @return the target class or null if not compliant deployment model
     */
    public Class getMixinTargetClass(String mixinName, Object mixinImpl) {
        return m_container.getIntroductionContainer(mixinName).getTargetClass(mixinImpl);
    }

    /**
     * Returns the perJVM aspect.
     *
     * @return the perJVM aspect
     */
    public CrossCutting getPerJvmAspect() {
        return m_container.getPerJvmAspect();
    }

    /**
     * Returns the perClass aspect.
     *
     * @param callingClass the calling class
     * @return the perClass aspect
     */
    public CrossCutting getPerClassAspect(final Class callingClass) {
        return m_container.getPerClassAspect(callingClass);
    }

    /**
     * Returns the perInstance aspect.
     *
     * @param callingInstance the calling instance
     * @return the perInstance aspect
     */
    public CrossCutting getPerInstanceAspect(final Object callingInstance) {
        return m_container.getPerInstanceAspect(callingInstance);
    }

    /**
     * Returns the perThread aspect.
     *
     * @return the perThread aspect
     */
    public CrossCutting getPerThreadAspect() {
        return m_container.getPerThreadAspect();
    }

    /**
     * Provides custom deserialization.
     *
     * @param stream the object input stream containing the serialized object
     * @throws Exception in case of failure
     */
    private void readObject(final ObjectInputStream stream) throws Exception {
        ObjectInputStream.GetField fields = stream.readFields();
        m_uuid = (String)fields.get("m_uuid", null);
        m_name = (String)fields.get("m_name", null);
        m_aspectClass = (Class)fields.get("m_aspectClass", null);
        m_targetInstance = fields.get("m_targetInstance", null);
        m_targetClass = (Class)fields.get("m_targetClass", null);
        m_deploymentModel = fields.get("m_deploymentModel", DeploymentModel.PER_JVM);
        m_aspectDef = (AspectDefinition)fields.get("m_aspectDef", null);
        m_parameters = (Map)fields.get("m_parameters", null);
        m_container = new AspectContainer(this);
        m_system = SystemLoader.getSystem(m_uuid);
        m_system.initialize();
        try {
            m_aspectConstructor = m_aspectClass.getConstructor(new Class[]{CrossCuttingInfo.class});
        }
        catch (NoSuchMethodException e) {
            throw new WrappedRuntimeException(e);
        }
    }
}
