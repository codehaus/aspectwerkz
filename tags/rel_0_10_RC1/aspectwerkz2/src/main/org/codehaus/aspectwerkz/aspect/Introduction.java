/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.aspect;

import java.lang.reflect.Constructor;

import org.codehaus.aspectwerkz.ContextClassLoader;
import org.codehaus.aspectwerkz.DeploymentModel;
import org.codehaus.aspectwerkz.Mixin;
import org.codehaus.aspectwerkz.definition.IntroductionDefinition;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;

/**
 * Interface+Implementation Introduction
 * <p/>
 * This represents the inner class mixin based implementation in the system todo: is serializable needed ? if so move
 * all non serializable to a container todo: fix methods name ___AW - does it matters ?
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class Introduction implements Mixin {

    /**
     * An empty <code>Object</code> array.
     */
    public static final Object[] EMPTY_OBJECT_ARRAY = new Object[]{};

    /**
     * Mixin name
     */
    private String m_name;

    /**
     * Mixin implementation as aspect inner class Note: when swapped the impl can be an autonomous class
     */
    private Class m_mixinImplClass;

    /**
     * Mixin implementation as aspect inner class Note: when swapped the impl can be an autonomous class
     */
    private Object m_mixinImpl;

    /**
     * The container for the introduction (single per JVM)
     */
    private IntroductionContainer m_container;

    /**
     * Aspect in which this mixin is defined
     */
    private Aspect m_aspect;

    /**
     * Defintion to which this mixin relates
     */
    private IntroductionDefinition m_definition;

    /**
     * Holds the deployment model. The deployment model of an introduction is tight to the aspect deployment model that
     * defines it
     */
    protected int m_deploymentModel;

    /**
     * Create a new introduction
     *
     * @param name       of this introduction - by convention the AspectClassFQN $ InnerClass
     * @param aspect     which defines this mixin
     * @param definition
     */
    public Introduction(
            final String name,
            final Class implClass,
            final Aspect aspect,
            final IntroductionDefinition definition) {
        m_name = name;
        m_aspect = aspect;
        m_definition = definition;
        m_mixinImplClass = implClass;

        // handle deploymentModel dependancies
        // defaults to Aspect deploymentModel
        // else supported models are:
        // Mixin			Aspect
        // perJVM			perJVM
        // perClass		    perJVM,perClass
        // perInstance		perJVM,perClass,perInstance
        // perThread        perThread
        // todo all those checks should be done earlier
        // (AspectC thought doclet inheritance might cause problem when inheritating compiled aspects without source code)
        if (definition.getDeploymentModel() == null) {
            m_deploymentModel = m_aspect.___AW_getDeploymentModel();
        }
        else {
            int model = DeploymentModel.getDeploymentModelAsInt(definition.getDeploymentModel());
            if (DeploymentModel.isMixinDeploymentModelCompatible(model, m_aspect.___AW_getDeploymentModel())) {
                m_deploymentModel = model;
            }
            else {
                throw new RuntimeException(
                        "could no create mixin from aspect: incompatible deployment models : mixin " +
                        DeploymentModel.getDeploymentModelAsString(model) +
                        " with aspect " +
                        DeploymentModel.getDeploymentModelAsString(m_aspect.___AW_getDeploymentModel())
                );
            }
        }

        try {
            if (isInnerClassOf(implClass, aspect.___AW_getAspectClass())) {
                // mixin is an inner class
                Constructor constructor = m_mixinImplClass.getConstructors()[0];
                constructor.setAccessible(true);
                m_mixinImpl = constructor.newInstance(new Object[]{aspect});
            }
            else {
                m_mixinImpl = m_mixinImplClass.newInstance();
            }
        }
        catch (Exception e) {
            throw new RuntimeException(
                    "could no create mixin from aspect [be sure to have a public Mixin impl as inner class]: " +
                    e.getMessage()
            );
        }
    }

    /**
     * Clone the prototype Introduction.
     *
     * @param prototype introduction
     * @param aspect    related aspect (not prototype)
     * @return new introduction instance
     */
    public static Introduction newInstance(final Introduction prototype, final Aspect aspect) {
        return new Introduction(
                prototype.m_name,
                prototype.m_mixinImplClass,
                aspect,
                prototype.m_definition
        );
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
     * @return aspect attached to this introduction
     */
    public Aspect getAspect() {
        return m_aspect;
    }

    /**
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
    public String ___AW_getName() {
        return m_name;
    }

    /**
     * Returns the mixin deployment model.
     *
     * @return the deployment model
     */
    public int ___AW_getDeploymentModel() {
        return m_deploymentModel;//aspect.___AW_getDeploymentModel();
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
     * Invokes the method with the index specified. Invoked by methods without any parameters (slight performance gain
     * since we are saving us one array creation).
     *
     * @param methodIndex   the method index
     * @param callingObject a reference to the calling object
     * @return the result from the invocation
     */
    public Object ___AW_invokeMixin(final int methodIndex, final Object callingObject) {
        return ___AW_invokeMixin(methodIndex, EMPTY_OBJECT_ARRAY, callingObject);
    }

    /**
     * Invokes an introduced method with the index specified.
     *
     * @param methodIndex   the method index
     * @param parameters    the parameters for the invocation
     * @param callingObject a reference to the calling object
     * @return the result from the invocation
     */
    public Object ___AW_invokeMixin(final int methodIndex, final Object[] parameters, final Object callingObject) {
        try {
            Object result = null;
            switch (___AW_getDeploymentModel()) {

                case DeploymentModel.PER_JVM:
                    result = m_container.invokeIntroductionPerJvm(methodIndex, parameters);
                    break;

                case DeploymentModel.PER_CLASS:
                    result = m_container.invokeIntroductionPerClass(callingObject, methodIndex, parameters);
                    break;

                case DeploymentModel.PER_INSTANCE:
                    result = m_container.invokeIntroductionPerInstance(callingObject, methodIndex, parameters);
                    break;

                case DeploymentModel.PER_THREAD:
                    result = m_container.invokeIntroductionPerThread(methodIndex, parameters);
                    break;

                default:
                    throw new RuntimeException("invalid deployment model: " + m_aspect.___AW_getDeploymentModel());
            }
            return result;
        }
        catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Returns the implementation class name for the mixin.
     *
     * @return the implementation class name for the mixin
     */
    public String ___AW_getImplementationClassName() {
        return m_mixinImpl.getClass().getName();
    }

    /**
     * Returns the implementation object for the mixin.
     *
     * @return the implementation for the mixin
     */
    public Object ___AW_getImplementation() {
        return m_mixinImpl;
    }

    /**
     * Swaps the current introduction implementation.
     *
     * @param className the class name of the new implementation
     */
    public void ___AW_swapImplementation(final String className) {
        if (className == null) {
            throw new IllegalArgumentException("class name can not be null");
        }
        try {
            Class newImplClass = ContextClassLoader.loadClass(className);//todo pbly old impl.getClassLoader() would be safer
            m_container.swapImplementation(newImplClass);
        }
        catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Swap the implementation of the mixin represented by this Introduction wrapper.
     *
     * @param newImplClass
     * @TODO called by container - should not be public
     */
    public void swapImplementation(final Class newImplClass) {
        try {
            m_mixinImplClass = newImplClass;
            if (isInnerClassOf(m_mixinImplClass, m_aspect.___AW_getAspectClass())) {
                // mixin is an inner class
                Constructor constructor = newImplClass.getConstructors()[0];
                constructor.setAccessible(true);
                m_mixinImpl = constructor.newInstance(new Object[]{m_aspect});
            }
            else {
                m_mixinImpl = m_mixinImplClass.newInstance();
            }
        }
        catch (Exception e) {
            throw new RuntimeException(
                    "could no create mixin from aspect [be sure to have a public Mixin impl as inner class]: " +
                    e.getMessage()
            );
        }
    }

    /**
     * Check if klazz is an inner class of containingClass. Based on name to support private inner class with no arg
     * ctor.
     *
     * @param klazz
     * @param containingClass
     * @return true if is an inner class
     */
    private static boolean isInnerClassOf(final Class klazz, final Class containingClass) {
        return klazz.getName().startsWith(containingClass.getName() + "$");
    }
}
