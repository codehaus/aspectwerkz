/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.attribdef.aspect;

import org.codehaus.aspectwerkz.Mixin;
import org.codehaus.aspectwerkz.ContextClassLoader;
import org.codehaus.aspectwerkz.MethodComparator;
import org.codehaus.aspectwerkz.DeploymentModel;
import org.codehaus.aspectwerkz.transform.TransformationUtil;
import org.codehaus.aspectwerkz.attribdef.definition.IntroductionDefinition;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.exception.DefinitionException;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Arrays;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Constructor;
import java.io.Serializable;
import java.io.ObjectInputStream;

/**
 * Interface+Implementation Introduction
 *
 * This represents the inner class mixin based implementation in the system
 * todo: is serializable needed ? if so move all non serializable to a container
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
     * Mixin implementation as aspect inner class
     * Note: when swapped the impl is an autonomous class
     */
    private Class m_mixinImplClass;

    /**
     * Mixin implementation as aspect inner class
     * Note: when swapped the impl is an autonomous class
     */
    private Object m_mixinImpl;

    public void setContainer(DefaultIntroductionContainerStrategy m_container) {
        this.m_container = m_container;
    }

    private DefaultIntroductionContainerStrategy m_container;

    public Aspect getAspect() {
        return m_aspect;
    }

    public IntroductionDefinition getIntroductionDefinition() {
        return m_definition;
    }

    /**
     * Aspect in which this mixin is defined
     */
    private Aspect m_aspect;

    /**
     * Defintion to which this mixin applies
     */
    private IntroductionDefinition m_definition;

    /**
     * Create a new introduction
     *
     * @param name of this introduction - by convention the AspectClassFQN $ InnerClass
     * @param aspect which defines this mixin
     * @param definition
     */
    public Introduction(String name, Class implClass, Aspect aspect, IntroductionDefinition definition) {
        m_name = name;
        m_aspect = aspect;
        m_definition = definition;
        m_mixinImplClass = implClass;

        try {
            // assume mixin is an inner class
            //TODO check thru
            //aspect.getClass().getClasses()
            Constructor constructor = m_mixinImplClass.getConstructors()[0];
            constructor.setAccessible(true);
            m_mixinImpl = constructor.newInstance(new Object[]{aspect});
        } catch (Exception e) {
            throw new RuntimeException("could no create mixin from aspect [be sure to have a public Mixin impl as inner class]: " + e.getMessage());
        }
    }

    public static Introduction newInstance(Introduction prototype, Aspect aspect) {
        Introduction clone = new Introduction(
                prototype.m_name,
                prototype.m_mixinImplClass,
                aspect,
                prototype.m_definition);
        return clone;
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
     * Returns the deployment model.
     * For now there is one mixin instance per aspect instance
     *
     * @return the deployment model
     */
    public int ___AW_getDeploymentModel() {
        return m_aspect.___AW_getDeploymentModel();
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
    public Object ___AW_invokeMixin(int methodIndex, Object callingObject) {
        return ___AW_invokeMixin(methodIndex, EMPTY_OBJECT_ARRAY, callingObject);
    }

    /**
     * Invokes an introduced method with the index specified.
     *
     * @param methodIndex the method index
     * @param parameters the parameters for the invocation
     * @param callingObject a reference to the calling object
     * @return the result from the invocation
     */
    public Object ___AW_invokeMixin(int methodIndex, Object[] parameters, Object callingObject) {
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
    public void ___AW_swapImplementation(String className) {
        if (className == null) throw new IllegalArgumentException("class name can not be null");
        try {
            Class newImplClass = ContextClassLoader.loadClass(className);//todo pbly old impl.getClassLoader() would be safer
            m_container.swapImplementation(newImplClass);
        }
        catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }

    public void swapImplementation(Class newImplClass) {
        try {
            m_mixinImplClass = newImplClass;
            //rebuild instance
            // assume mixin is an inner class
            //TODO check thru
            //aspect.getClass().getClasses()
            Constructor constructor = newImplClass.getConstructors()[0];
            constructor.setAccessible(true);
            m_mixinImpl = constructor.newInstance(new Object[]{m_aspect});
         } catch (Exception e) {
            throw new RuntimeException("could no create mixin from aspect [be sure to have a public Mixin impl as inner class]: " + e.getMessage());
        }
    }

}
