/*
 * AspectWerkz - a dynamic, lightweight and high-performant AOP/AOSD framework for Java.
 * Copyright (C) 2002-2003  Jonas Bonér. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
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
package org.codehaus.aspectwerkz.introduction;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.codehaus.aspectwerkz.DeploymentModel;
import org.codehaus.aspectwerkz.Identifiable;
import org.codehaus.aspectwerkz.ContainerType;
import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;
import org.codehaus.aspectwerkz.exception.DefinitionException;

/**
 * Implements the concept of Introductions, which is similar to the concept
 * of Open Classes and Mixins.<br/>
 *
 * An Introduction makes it possible to extend a class with a new interface
 * and a new implementation (methods and fields).<p/>
 *
 * Supports four different deployment models:
 *     PER_JVM, PER_CLASS, PER_INSTANCE and PER_THREAD.<br/>
 *
 * The PER_JVM deployment model performance a bit better than the other models
 * since no synchronization and object creation is needed.
 *
 * @see aspectwerkz.DeploymentModel
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @version $Id: Introduction.java,v 1.3 2003-06-17 15:02:15 jboner Exp $
 */
public class Introduction implements Serializable {

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
     * Creates an introduction with an interface and an implementation.
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
    public Object invoke(final int methodIndex, final Object callingObject) {
        return invoke(methodIndex, EMPTY_OBJECT_ARRAY, callingObject);
    }

    /**
     * Invokes the method with the index specified.
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
                    result = invokePerJvm(methodIndex, parameters);
                    break;

                case DeploymentModel.PER_CLASS:
                    result = invokePerClass(
                            callingObject,
                            methodIndex,
                            parameters);
                    break;

                case DeploymentModel.PER_INSTANCE:
                    result = invokePerInstance(
                            callingObject,
                            methodIndex,
                            parameters);
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
     * Swaps the current introduction implementation.
     *
     * @param className the class name of the new implementation
     */
    public void swapImplementation(final String className) {
        if (className == null) throw new IllegalArgumentException("class name can not be null");
        synchronized (m_implClass) {
            try {
                m_implementation = className;
                m_implClass = Thread.currentThread().
                        getContextClassLoader().loadClass(className);
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
     * Sets the name of the introduction.
     *
     * @param name the name of the introduction
     */
    public void setName(final String name) {
        m_name = name;
    }

    /**
     * Returns the name of the introduction.
     *
     * @return the name
     */
    public String getName() {
        return m_name;
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
     * Returns the implementation.
     *
     * @return the implementation
     */
    public String getImplementation() {
        return m_implementation;
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
     * Returns the deployment model.
     *
     * @return the deployment model
     */
    public int getDeploymentModel() {
        return m_deploymentModel;
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
     * Returns the memory type.
     *
     * @return the memory type
     */
    public ContainerType getMemoryType() {
        return m_container.getContainerType();
    }

    /**
     * Invokes the method on a per JVM basis.
     *
     * @param methodIndex the method index
     * @param parameters the parameters for the invocation
     * @return the result from the method invocation
     */
    private Object invokePerJvm(final int methodIndex,
                                final Object[] parameters) {
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
        return m_container.invokePerClass(
                callingObject, methodIndex, parameters);
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
        return m_container.invokePerInstance(
                callingObject, methodIndex, parameters);
    }

    /**
     * Invokes the method on a per thread basis.
     *
     * @param methodIndex the method index
     * @param parameters the parameters for the invocation
     * @return the result from the method invocation
     */
    private Object invokePerThread(final int methodIndex,
                                   final Object[] parameters) {
        return m_container.invokePerThread(methodIndex, parameters);
    }

    /**
     * Checks if the implementation class implementents the correct interface.
     */
    private void checkIfInterfaceImplementationMatches() {
        try {
            Class[] interfaces = m_implClass.getInterfaces();
            boolean implementsInterface = false;
            for (int i = 0; i < interfaces.length; i++) {
                if (interfaces[i].getName().equals(m_interface)) {
                    implementsInterface = true;
                }
            }
            if (!implementsInterface) throw new RuntimeException("introduced implementation " + m_implementation + " has to implement introduced interface " + m_interface);
        }
        catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }

// --- over-ridden methods ---
//
//    public boolean equals(final Object o) {
//        if (this == o) return true;
//        if (!(o instanceof Introduction)) return false;
//        final Introduction obj = (Introduction)o;
//        return areEqualsOrBothNull(obj.m_interface, this.m_interface) &&
//                areEqualsOrBothNull(obj.m_implementation, this.m_implementation) &&
//                areEqualsOrBothNull(obj.m_methods, this.m_methods) &&
//                areEqualsOrBothNull(obj.m_expression, this.m_expression) &&
//                areEqualsOrBothNull(obj.m_perClass, this.m_perClass) &&
//                areEqualsOrBothNull(obj.m_perInstance, this.m_perInstance) &&
//                areEqualsOrBothNull(obj.m_perClass, this.m_perClass) &&
//                areEqualsOrBothNull(obj.m_instancePerJvm, this.m_instancePerJvm);
//    }
//
//    protected static boolean areEqualsOrBothNull(final Object o1, final Object o2) {
//        if (null == o1) return (null == o2);
//        return o1.equals(o2);
//    }
//
//    public String toString() {
//        return "["
//                + super.toString()
//                + ": "
//                + m_interface
//                + "," + m_implementation
//                + "," + m_methods
//                + "," + m_expression
//                + "," + m_perClass
//                + "," + m_perInstance
//                + "," + m_perClass
//                + "," + m_instancePerJvm
//                + "]";
//    }
//
//    public int hashCode() {
//        int result = 17;
//        result = 37 * result + hashCodeOrZeroIfNull(m_interface);
//        result = 37 * result + hashCodeOrZeroIfNull(m_implementation);
//        result = 37 * result + hashCodeOrZeroIfNull(m_methods);
//        result = 37 * result + hashCodeOrZeroIfNull(m_expression);
//        result = 37 * result + hashCodeOrZeroIfNull(m_perClass);
//        result = 37 * result + hashCodeOrZeroIfNull(m_perInstance);
//        result = 37 * result + hashCodeOrZeroIfNull(m_perClass);
//        result = 37 * result + hashCodeOrZeroIfNull(m_instancePerJvm);
//        return result;
//    }
//
//    protected static int hashCodeOrZeroIfNull(final Object o) {
//        if (null == o) return 19;
//        return o.hashCode();
//    }
}
