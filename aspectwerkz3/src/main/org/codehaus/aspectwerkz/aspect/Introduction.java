/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.aspect;

import org.codehaus.aspectwerkz.ContextClassLoader;
import org.codehaus.aspectwerkz.CrossCuttingInfo;
import org.codehaus.aspectwerkz.DeploymentModel;
import org.codehaus.aspectwerkz.Mixin;
import org.codehaus.aspectwerkz.definition.IntroductionDefinition;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Interface+Implementation Introduction <p/>This represents the inner class mixin based
 * implementation in the system
 * 
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur </a>
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public class Introduction implements Mixin {
    private static final int MIXIN_CONSTRUCTION_TYPE_UNKNOWN = 0;

    private static final int MIXIN_CONSTRUCTION_TYPE_DEFAULT = 1;

    private static final int MIXIN_CONSTRUCTION_TYPE_CROSS_CUTTING_INFO = 2;

    private static final Object[] ARRAY_WITH_CROSS_CUTTING_INFO = new Object[1];

    /**
     * An empty <code>Object</code> array.
     */
    public static final Object[] EMPTY_OBJECT_ARRAY = new Object[] {};

    /**
     * Mixin name
     */
    private String m_name;

    /**
     * Mixin implementation as aspect inner class Note: when swapped the impl can be an autonomous
     * class
     */
    private Class m_mixinImplClass;

    /**
     * Mixin implementation as aspect inner class Note: when swapped the impl can be an autonomous
     * class
     */
    private Object m_mixinImpl;

    /**
     * The constructor for the mixin.
     */
    private Constructor m_mixinConstructor;

    /**
     * The container for the introduction (single per JVM)
     */
    private IntroductionContainer m_container;

    /**
     * The cross-cutting info for the mixin.
     */
    private CrossCuttingInfo m_crossCuttingInfo;

    /**
     * Defintion to which this mixin relates
     */
    private IntroductionDefinition m_definition;

    /**
     * Holds the deployment model. The deployment model of an introduction is tight to the aspect
     * deployment model that defines it
     */
    protected int m_deploymentModel;

    /**
     * The mixin construction type.
     */
    private int m_mixinConstructionType = MIXIN_CONSTRUCTION_TYPE_UNKNOWN;

    /**
     * Create a new introduction
     * 
     * @param name of this introduction - by convention the AspectClassFQN $ InnerClass
     * @param implClass
     * @param crossCuttingInfo which defines this mixin
     * @param definition
     */
    public Introduction(final String name,
                        final Class implClass,
                        final CrossCuttingInfo crossCuttingInfo,
                        final IntroductionDefinition definition) {
        m_name = name;
        m_crossCuttingInfo = crossCuttingInfo;
        m_definition = definition;
        m_mixinImplClass = implClass;
        m_mixinConstructor = findConstructor();
        ARRAY_WITH_CROSS_CUTTING_INFO[0] = m_crossCuttingInfo;

        // handle deploymentModel dependancies
        // defaults to Aspect deploymentModel
        // else supported models are:
        // Mixin Aspect
        // perJVM perJVM
        // perClass perJVM,perClass
        // perInstance perJVM,perClass,perInstance
        // perThread perThread
        // todo all those checks should be done earlier
        // (AspectC thought doclet inheritance might cause problem when inheritating compiled
        // aspects without source
        // code)
        if (definition.getDeploymentModel() == null) {
            m_deploymentModel = m_crossCuttingInfo.getDeploymentModel();
        } else {
            int model = DeploymentModel.getDeploymentModelAsInt(definition.getDeploymentModel());
            if (DeploymentModel.isMixinDeploymentModelCompatible(model, m_crossCuttingInfo
                    .getDeploymentModel())) {
                m_deploymentModel = model;
            } else {
                throw new RuntimeException(
                    "could no create mixin from aspect: incompatible deployment models : mixin "
                        + DeploymentModel.getDeploymentModelAsString(model)
                        + " with aspect "
                        + DeploymentModel.getDeploymentModelAsString(m_crossCuttingInfo
                                .getDeploymentModel()));
            }
        }
    }

    /**
     * Clone the prototype Introduction.
     * 
     * @param prototype introduction
     * @param crossCuttingInfo the cross-cutting info
     * @return new introduction instance
     */
    public static Introduction newInstance(
        final Introduction prototype,
        final CrossCuttingInfo crossCuttingInfo) {
        Introduction introduction = new Introduction(
            prototype.m_name,
            prototype.m_mixinImplClass,
            crossCuttingInfo,
            prototype.m_definition);

        //AW-207//introduction.createMixin();
        return introduction;
    }

    /**
     * Creates a new mixin instance.
     */
    public void createMixin() {
        try {
            switch (m_mixinConstructionType) {
                case MIXIN_CONSTRUCTION_TYPE_DEFAULT:
                    m_mixinImpl = m_mixinConstructor.newInstance(EMPTY_OBJECT_ARRAY);
                    break;
                case MIXIN_CONSTRUCTION_TYPE_CROSS_CUTTING_INFO:
                    m_mixinImpl = m_mixinConstructor.newInstance(ARRAY_WITH_CROSS_CUTTING_INFO);
                    break;
                default:
                    throw new RuntimeException(
                        "mixin ["
                            + m_mixinImplClass.getName()
                            + "] does not have a valid constructor (either default no-arg or one that takes a CrossCuttingInfo type as its only parameter)");
            }
        } catch (InstantiationException e) {
            throw new WrappedRuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new WrappedRuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new WrappedRuntimeException(e.getTargetException());
        }
    }

    /**
     * Set the container.
     * 
     * @param container
     */
    public void setContainer(final IntroductionContainer container) {
        m_container = container;
    }

    /**
     * Returns the cross-cutting info.
     * 
     * @return the cross-cutting info.
     */
    public CrossCuttingInfo getCrossCuttingInfo() {
        return m_crossCuttingInfo;
    }

    /**
     * Returns the definition.
     * 
     * @return definition related to this introduction
     */
    public IntroductionDefinition getIntroductionDefinition() {
        return m_definition;
    }

    /**
     * Returns the name of the mixin.
     * 
     * @return the name
     */
    public String getName() {
        return m_name;
    }

    /**
     * Returns the mixin deployment model.
     * 
     * @return the deployment model
     */
    public int getDeploymentModel() {
        return m_deploymentModel;
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
     * Invokes the method with the index specified. Invoked by methods without any parameters
     * (slight performance gain since we are saving us one array creation).
     * 
     * @param methodIndex the method index
     * @param callingObject a reference to the calling object
     * @return the result from the invocation
     */
    public Object invokeMixin(final int methodIndex, final Object callingObject) throws Throwable {
        return invokeMixin(methodIndex, EMPTY_OBJECT_ARRAY, callingObject);
    }

    /**
     * Invokes an introduced method with the index specified.
     * 
     * @param methodIndex the method index
     * @param parameters the parameters for the invocation
     * @param callingObject a reference to the calling object
     * @return the result from the invocation
     */
    public Object invokeMixin(
        final int methodIndex,
        final Object[] parameters,
        final Object callingObject) throws Throwable {
        Object result = null;
        switch (m_deploymentModel) {
            case DeploymentModel.PER_JVM:
                result = m_container.invokeIntroductionPerJvm(methodIndex, parameters);
                break;
            case DeploymentModel.PER_CLASS:
                result = m_container.invokeIntroductionPerClass(
                    callingObject,
                    methodIndex,
                    parameters);
                break;
            case DeploymentModel.PER_INSTANCE:
                result = m_container.invokeIntroductionPerInstance(
                    callingObject,
                    methodIndex,
                    parameters);
                break;
            case DeploymentModel.PER_THREAD:
                result = m_container.invokeIntroductionPerThread(methodIndex, parameters);
                break;
            default:
                throw new RuntimeException("invalid deployment model: "
                    + m_crossCuttingInfo.getDeploymentModel());
        }
        return result;
    }

    /**
     * Returns the implementation class name for the mixin.
     * 
     * @return the implementation class name for the mixin
     */
    public String getImplementationClassName() {
        return m_mixinImplClass.getName();
    }

    /**
     * Returns the implementation object for the mixin.
     * 
     * @return the implementation for the mixin
     */
    public Class getImplementationClass() {
        return m_mixinImplClass;
    }

    /**
     * Returns the implementation object for the mixin.
     * 
     * @return the implementation for the mixin
     */
    public Object getImplementation() {
        return m_mixinImpl;
    }

    /**
     * Swaps the current introduction implementation.
     * 
     * @param className the class name of the new implementation
     */
    public void swapImplementation(final String className) {
        if (className == null) {
            throw new IllegalArgumentException("class name can not be null");
        }
        try {
            Class newImplClass = ContextClassLoader.loadClass(className); //todo pbly old
                                                                          // impl.getClassLoader()
                                                                          // would
            // be safer
            m_container.swapImplementation(newImplClass);
        } catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Grabs the correct constructor for the mixin.
     * 
     * @return the constructor for the mixin
     */
    private Constructor findConstructor() {
        Constructor mixinConstructor = null;
        Constructor[] constructors = m_mixinImplClass.getDeclaredConstructors();
        for (int i = 0; i < constructors.length; i++) {
            Constructor constructor = constructors[i];
            Class[] parameterTypes = constructor.getParameterTypes();
            if (parameterTypes.length == 0) {
                m_mixinConstructionType = MIXIN_CONSTRUCTION_TYPE_DEFAULT;
                mixinConstructor = constructor;
            } else if ((parameterTypes.length == 1)
                && parameterTypes[0].getName().equals(CrossCuttingInfo.class.getName())) {
                m_mixinConstructionType = MIXIN_CONSTRUCTION_TYPE_CROSS_CUTTING_INFO;
                mixinConstructor = constructor;
                break;
            }
        }
        if (m_mixinConstructionType == MIXIN_CONSTRUCTION_TYPE_UNKNOWN) {
            throw new RuntimeException(
                "mixin ["
                    + m_mixinImplClass.getName()
                    + "] does not have a valid constructor (either default no-arg or one that takes a CrossCuttingInfo type as its only parameter)");
        }
        return mixinConstructor;
    }

    /**
     * Swap the implementation of the mixin represented by this Introduction wrapper.
     * 
     * @param newImplClass
     */
    void swapImplementation(final Class newImplClass) {
        m_mixinImplClass = newImplClass;
        m_mixinConstructor = findConstructor();
        createMixin();
    }
}