/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.xmldef.introduction;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.codehaus.aspectwerkz.DeploymentModel;
import org.codehaus.aspectwerkz.ContainerType;
import org.codehaus.aspectwerkz.ContextClassLoader;
import org.codehaus.aspectwerkz.Mixin;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.exception.DefinitionException;

/**
 * Implements the concept of Introductions, which is similar to the concept
 * of Open Classes A Mixins.<br/>
 *
 * An Introduction makes it possible to extend a class with a new interface
 * A a new implementation (methods A fields).<p/>
 *
 * Supports four different deployment models:
 *     PER_JVM, PER_CLASS, PER_INSTANCE A PER_THREAD.<br/>
 *
 * The PER_JVM deployment model performance a bit better than the other models
 * since no synchronization A object creation is needed.
 *
 * todo: m_container is transient but not restored. is that ok ?
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class Introduction implements Serializable, Mixin {

    /**
     * An empty <code>Object</code> array.
     */
    public static final Object[] EMPTY_OBJECT_ARRAY = new Object[]{};

    /**
     * Holds the name of the introduction.
     */
    protected String m_name;

    /**
     * Holds the interface class name.
     */
    protected final String m_interface;

    /**
     * Holds the implementation class name.
     */
    protected String m_implementation;

    /**
     * Holds the deployment model.
     */
    protected final int m_deploymentModel;

    /**
     * Holds the class for the implementation.
     */
    protected Class m_implClass;

    /**
     * The memory strategy for this introduction.
     */
    protected transient IntroductionContainer m_container;

    /**
     * Creates an introduction with an interface.<br/>
     * Must be an interface without any methods.
     *
     * @param name the name of the introduction
     * @param anInterface an interface
     */
    public Introduction(final String name, final String anInterface) {
        this(name, anInterface, null, DeploymentModel.PER_JVM);
    }

    /**
     * Creates an introduction with an interface A an implementation.
     *
     * @param name the name of the introduction
     * @param intf the interface
     * @param implClass the implementation class
     * @param deploymentModel the deployment model
     */
    public Introduction(final String name,
                        final String intf,
                        final Class implClass,
                        final int deploymentModel) {
        if (name == null) throw new IllegalArgumentException("name can not be null");
        if (intf == null) throw new IllegalArgumentException("interface can not be null");

        m_name = name;
        m_interface = intf;
        m_implClass = implClass;
        m_deploymentModel = deploymentModel;

        if (implClass != null) {
            m_implementation = implClass.getName();
            checkIfInterfaceImplementationMatches();
        }
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
    public Object ___AW_invokeMixin(final int methodIndex, final Object callingObject) {
        return ___AW_invokeMixin(methodIndex, EMPTY_OBJECT_ARRAY, callingObject);
    }

    /**
     * Invokes the method with the index specified.
     *
     * @param methodIndex the method index
     * @param parameters the parameters for the invocation
     * @param callingObject a reference to the calling object
     * @return the result from the invocation
     */

    public Object ___AW_invokeMixin(final int methodIndex,
                                    final Object[] parameters,
                                    final Object callingObject) {
        try {
            Object result = null;
            switch (m_deploymentModel) {

                case DeploymentModel.PER_JVM:
                    result = invokePerJvm(methodIndex, parameters);
                    break;

                case DeploymentModel.PER_CLASS:
                    result = invokePerClass(callingObject, methodIndex, parameters);
                    break;

                case DeploymentModel.PER_INSTANCE:
                    result = invokePerInstance(callingObject, methodIndex, parameters);
                    break;

                case DeploymentModel.PER_THREAD:
                    result = invokePerThread(methodIndex, parameters);
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
     * Returns the implementation class name for the mixin.
     *
     * @return the implementation class name for the mixin
     */
    public String ___AW_getImplementationClassName() {
        return m_implementation;
    }

    /**
     * Swaps the current introduction implementation.
     *
     * @param className the class name of the new implementation
     */
    public void ___AW_swapImplementation(final String className) {
        if (className == null) throw new IllegalArgumentException("class name can not be null");
        synchronized (m_implClass) {
            try {
                m_implementation = className;
                m_implClass = ContextClassLoader.loadClass(className);
                Class[] interfaces = m_implClass.getInterfaces();
                boolean implementsInterface = false;
                for (int i = 0; i < interfaces.length; i++) {
                    if (interfaces[i].getName().equals(m_interface)) {
                        implementsInterface = true;
                    }
                }
                if (!implementsInterface) throw new DefinitionException("introduced implementation " + m_implementation + " has to implement introduced interface " + m_interface);
            }
            catch (Exception e) {
                throw new WrappedRuntimeException(e);
            }
        }

        m_container.swapImplementation(m_implClass);
    }

    /**
     * Returns the name of the introduction.
     *
     * @return the name
     */
    public String ___AW_getName() {
        return m_name;
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
     * Sets the name of the introduction.
     *
     * @param name the name of the introduction
     */
    public void setName(final String name) {
        m_name = name;
    }

    /**
     * Returns the interface.
     *
     * @return the interface
     */
    public String getInterface() {
        return m_interface;
    }

    /**
     * Returns a specific method by the method index.
     *
     * @param index the method index
     * @return the method
     */
    public Method getMethod(final int index) {
        return m_container.getMethod(index);
    }

    /**
     * Returns all the methods for this introduction.
     *
     * @return the methods
     */
    public Method[] getMethods() {
        return m_container.getMethods();
    }

    /**
     * Sets the container.
     *
     * @param container the container
     */
    public void setContainer(final IntroductionContainer container) {
        m_container = container;
    }

    /**
     * Returns the container.
     *
     * @return the container
     */
    public IntroductionContainer getContainer() {
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
     * Invokes the method on a per JVM basis.
     *
     * @param methodIndex the method index
     * @param parameters the parameters for the invocation
     * @return the result from the method invocation
     */
    private Object invokePerJvm(final int methodIndex, final Object[] parameters) {
        return m_container.invokePerJvm(methodIndex, parameters);
    }

    /**
     * Invokes the method on a per class basis.
     *
     * @param callingObject a reference to the calling object
     * @param methodIndex the method index
     * @param parameters the parameters for the invocation
     * @return the result from the method invocation
     */
    private Object invokePerClass(final Object callingObject,
                                  final int methodIndex,
                                  final Object[] parameters) {
        return m_container.invokePerClass(callingObject, methodIndex, parameters);
    }

    /**
     * Invokes the method on a per instance basis.
     *
     * @param callingObject a reference to the calling object
     * @param methodIndex the method index
     * @param parameters the parameters for the invocation
     * @return the result from the method invocation
     */
    private Object invokePerInstance(final Object callingObject,
                                     final int methodIndex,
                                     final Object[] parameters) {
        return m_container.invokePerInstance(callingObject, methodIndex, parameters);
    }

    /**
     * Invokes the method on a per thread basis.
     *
     * @param methodIndex the method index
     * @param parameters the parameters for the invocation
     * @return the result from the method invocation
     */
    private Object invokePerThread(final int methodIndex, final Object[] parameters) {
        return m_container.invokePerThread(methodIndex, parameters);
    }

    /**
     * Checks if the implementation class implementents the correct interface.
     */
    private void checkIfInterfaceImplementationMatches() {
        if (!findInterface(m_implClass, m_interface)) {
            throw new DefinitionException("introduced implementation " + m_implementation + " has to implement introduced interface " + m_interface);
        }
    }

    /**
     * Recursively traverse the interface hierarchy implemented by the given root class in
     * order to find one that matches the given name.
     *
     * @param root is the class or interface to start the search at.
     * @param requiredInterface that we are looking for.
     * @return <code>true</code> if we found the interface, <code>false</code> otherwise.
     */
    private boolean findInterface(final Class root, final String requiredInterface) {

        // The implementation uses a single loop over the directly
        // implemented interfaces. In the loop, first we check if the
        // current interface is the one we're looking for, A then if not
        // we call this same method starting at that current interface.
        Class[] interfaces = root.getInterfaces();

        for (int i = 0; i < interfaces.length; i++) {
            Class implemented = interfaces[i];
            if (implemented.getName().equals(requiredInterface)
                    || findInterface(implemented, requiredInterface)) {
                return true;
            }
        }
        return false;
    }
}
