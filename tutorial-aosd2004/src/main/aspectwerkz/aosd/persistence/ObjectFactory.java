/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD-style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package aspectwerkz.aosd.persistence;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.codehaus.aspectwerkz.exception.WrappedRuntimeException;

/**
 * A default implemetation of the ObjectFactory interface.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class ObjectFactory {

    private Class m_class = null;
    private Constructor m_constructor = null;
    private Object[] m_parameters = null;

    /**
     * Constructor.
     * Uses the context classloader
     *
     * @param classname the name of the class to create
     * @throws java.lang.ClassNotFoundException
     */
    public ObjectFactory(final String classname) throws ClassNotFoundException {
        if (classname == null) throw new IllegalArgumentException("classname can not be null");

        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) {
            loader = getClass().getClassLoader();
        }

        m_class = loader.loadClass(classname);
    }

    /**
     * Constructor.
     *
     * @param classname the name of the class to create
     * @param loader the classloader to use when loading the class
     * @throws java.lang.ClassNotFoundException
     */
    public ObjectFactory(final String classname, final ClassLoader loader) throws ClassNotFoundException {
        if (classname == null || loader == null)
            throw new IllegalArgumentException("classname or class loader can not be null");

        m_class = loader.loadClass(classname);
    }

    /**
     * Constructor.
     *
     * @param klass the class of the class to create
     */
    public ObjectFactory(final Class klass) {
        if (klass == null) throw new IllegalArgumentException("class can not be null");
        m_class = klass;
    }

    /**
     * Constructor.
     *
     * @param klass the class of the class to create
     * @param parameterTypes the parameter types in the constructor of the class
     * @param parameters the parameters for the constructor invocation
     * @throws java.lang.NoSuchMethodException
     */
    public ObjectFactory(final Class klass, final Class[] parameterTypes, final Object[] parameters)
            throws NoSuchMethodException {
        if (klass == null || parameterTypes == null || parameters == null) throw new IllegalArgumentException("class, parameter types or parameters can not be null");
        m_class = klass;
        m_constructor = m_class.getConstructor(parameterTypes);
        m_parameters = parameters;
    }

    /**
     * Constructor.
     *
     * @param constructor the constructor of the class
     * @param parameters the parameters for the constructor invocation
     */
    public ObjectFactory(final Constructor constructor, final Object[] parameters) {
        if (constructor == null || parameters == null) throw new IllegalArgumentException("constructor or parameters can not be null");
        m_constructor = constructor;
        m_parameters = parameters;
        m_class = m_constructor.getDeclaringClass();
    }

    /**
     * Returns a new object instance.
     *
     * @return a new object instance
     */
    public Object newInstance() {
        try {
            if (m_parameters == null) {
                return runDefaultConstructor();
            }
            else {
                return runConstructorWithParameters();
            }
        }
        catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Returns the name of the class that the factory creates.
     *
     * @return the class
     */
    public Class getCreatedClass() {
        return m_class;
    }

    /**
     * Executes the default constructor.
     *
     * @return the instance
     * @throws java.lang.InstantiationException
     * @throws java.lang.IllegalAccessException
     */
    private Object runDefaultConstructor() throws InstantiationException, IllegalAccessException {
        return m_class.newInstance();
    }

    /**
     * Executes the constructor with the given parameter list.
     *
     * @return the instance
     * @throws java.lang.InstantiationException
     * @throws java.lang.IllegalAccessException
     * @throws java.lang.reflect.InvocationTargetException
     */
    private Object runConstructorWithParameters()
            throws InstantiationException, IllegalAccessException, InvocationTargetException {
        return m_constructor.newInstance(m_parameters);
    }
}

