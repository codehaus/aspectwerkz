/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz;

import org.codehaus.aspectwerkz.aspect.AspectContainer;
import org.codehaus.aspectwerkz.aspect.management.Aspects;
import org.codehaus.aspectwerkz.definition.AspectDefinition;
import org.codehaus.aspectwerkz.definition.StartupManager;
import org.codehaus.aspectwerkz.exception.DefinitionException;

import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Contains information about and for classes that has been defined as cross-cutting.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r </a>
 */
public final class AspectContext implements Serializable {
    /**
     * An empty <code>Object</code> array.
     */
    public static final Object[] EMPTY_OBJECT_ARRAY = new Object[]{};

    /**
     * The name for the cross-cuttable class.
     */
    private String m_name;

    /**
     * The cross-cuttable class.
     */
    private Class m_aspectClass;

    /**
     * The container.
     */
    private transient AspectContainer m_container = null;

    /**
     * Holds the deployment model.
     */
    private int m_deploymentModel;

    /**
     * Holds the parameters passed to the aspect.
     */
    private Map m_parameters = new HashMap();

    /**
     * Holds the metadata.
     */
    private Map m_metaData = new HashMap();

    /**
     * The UUID for the system.
     */
    private String m_uuid;

    /**
     * The aspect definition.
     */
    private transient AspectDefinition m_aspectDefinition;

    /**
     * Creates a new cross-cutting info instance.
     *
     * @param uuid
     * @param aspectClass
     * @param deploymentModel
     * @param aspectDef
     * @param parameters
     */
    public AspectContext(final String uuid,
                         final Class aspectClass,
                         final String name,
                         final int deploymentModel,
                         final AspectDefinition aspectDef,
                         final Map parameters) {
        m_uuid = uuid;
        m_aspectClass = aspectClass;
        m_name = name;
        m_deploymentModel = deploymentModel;
        m_aspectDefinition = aspectDef;
        if (parameters != null) {
            m_parameters = parameters;
        }
    }

    /**
     * Copy constructor - creates a clone of the cross-cutting info.
     * Creates a new instance of the cross-cutting class it holds.
     *
     * @return a clone of the cross-cutting info
     */
    public static AspectContext newInstance(final AspectContext prototype) {
        try {
            return new AspectContext(
                    prototype.m_uuid,
                    prototype.m_aspectClass,
                    prototype.m_name,
                    prototype.m_deploymentModel,
                    prototype.m_aspectDefinition,
                    prototype.m_parameters
            );
        } catch (Exception e) {
            throw new RuntimeException(
                    "could not clone cross-cutting info ["
                    + prototype.getName()
                    + "]: "
                    + e.toString()
            );
        }
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
     * Returns the name of the aspect.
     *
     * @return the name of the aspect
     */
    public String getName() {
        return m_name;
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
     * Returns the cross-cuttable class.
     *
     * @return the cross-cuttable class
     */
    public Class getAspectClass() {
        return m_aspectClass;
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
     * <p/>
     * Will return null after deserialization.
     *
     * @return the aspect definition
     */
    public AspectDefinition getAspectDefinition() {
        return m_aspectDefinition;
    }

    /**
     * Sets a parameter.
     *
     * @param name  the name of the parameter
     * @param value the value of the parameter
     */
    public void setParameter(final String name, final String value) {
        m_parameters.put(name, value);
    }

    /**
     * Returns the value of a parameter.
     *
     * @param name the name of the parameter
     * @return the value of the parameter
     */
    public String getParameter(final String name) {
        if (!m_parameters.containsKey(name)) {
            throw new DefinitionException("parameter to advice not specified: " + name);
        }
        return (String) m_parameters.get(name);
    }

    /**
     * Adds metadata.
     *
     * @param key   the key
     * @param value the value
     */
    public void addMetaData(final Object key, final Object value) {
        m_metaData.put(key, value);
    }

    /**
     * Returns the metadata for a specific key.
     *
     * @param key the key
     * @return the value
     */
    public Object getMetaData(final Object key) {
        return m_metaData.get(key);
    }

    /**
     * Returns the target instance for the mixin of given name which is defined from within this aspect (mixin can have
     * different deployment model from aspect)
     *
     * @param mixinImpl miximImplementation aka "this" when called from within the mixin impl
     * @return the target instance or null if not compliant deployment model
     */
    public Object getMixinTargetInstance(final Object mixinImpl) {
        return getMixinTargetInstance(mixinImpl.getClass().getName(), mixinImpl);
    }

    /**
     * Returns the target class for the mixin of given name which is defined from within this aspect (mixin can have
     * different deployment model from aspect)
     *
     * @param mixinImpl miximImplementation aka "this" when called from within the mixin impl
     * @return the target class or null if not compliant deployment model
     */
    public Class getMixinTargetClass(final Object mixinImpl) {
        return getMixinTargetClass(mixinImpl.getClass().getName(), mixinImpl);
    }

    /**
     * Returns the target instance for the mixin of given name which is defined from within this aspect (mixin can have
     * different deployment model from aspect)
     *
     * @param mixinName of the mixin
     * @param mixinImpl miximImplementation aka "this" when called from within the mixin impl
     * @return the target instance or null if not compliant deployment model
     */
    public Object getMixinTargetInstance(final String mixinName, final Object mixinImpl) {
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
    public Class getMixinTargetClass(final String mixinName, final Object mixinImpl) {
        return m_container.getIntroductionContainer(mixinName).getTargetClass(mixinImpl);
    }

    /**
     * Return true if the AspectContext has not yet the AspectContainer set, that means this is the prototype init time
     */
    public boolean isPrototype() {
        return (m_container == null);
    }

    /**
     * Provides custom deserialization.
     *
     * @param stream the object input stream containing the serialized object
     * @throws Exception in case of failure
     */
    private void readObject(final ObjectInputStream stream) throws Exception {
        ObjectInputStream.GetField fields = stream.readFields();
        m_uuid = (String) fields.get("m_uuid", null);
        m_name = (String) fields.get("m_name", null);
        m_aspectClass = (Class) fields.get("m_aspectClass", null);
        m_deploymentModel = fields.get("m_deploymentModel", DeploymentModel.PER_JVM);
        m_parameters = (Map) fields.get("m_parameters", new HashMap());
        m_metaData = (Map) fields.get("m_metaData", new HashMap());
        m_container = Aspects.createAspectContainer(this);
        Aspects.initialize(m_container.getContext().getAspectClass().getClassLoader());
    }
}