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

/**
 * Interface+Implementation Introduction
 *
 * This represents the inner class mixin based implementation in the system
 * todo: is serializable needed ?
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
     * Sorted introduced methods
     */
    private Method[] m_methods;

    /**
     * Mixin implementation as aspect inner class
     * Note: when swapped the impl is an autonomous class
     */
    private Object m_mixinImpl;

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
    public Introduction(String name, Aspect aspect, IntroductionDefinition definition) {
        m_name = name;
        m_aspect = aspect;
        m_definition = definition;
        try {
            // mixin belongs to the same loader as the aspect
            Class mixinClass = aspect.getClass().getClassLoader().loadClass(name);
            // mixin is an inner class
            Constructor constructor = mixinClass.getConstructors()[0];
            constructor.setAccessible(true);
            m_mixinImpl = constructor.newInstance(new Object[]{aspect});
        } catch (Exception e) {
            throw new RuntimeException("could no create mixin from aspect [be sure to have a public Mixin impl as inner class]: " + e.getMessage());
        }
        // gather mixin introduced methods
        //todo : would n it be better theory to gather only intf methods ?
        m_methods = m_mixinImpl.getClass().getDeclaredMethods();
        Arrays.sort(m_methods, MethodComparator.getInstance(MethodComparator.NORMAL_METHOD));
        for (int i = 0; i < m_methods.length; i++) {
            m_methods[i].setAccessible(true);
        }
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
            //todo impl deployment model at mixin level
            //in that case callingObject instance / class / currentThread is used a key cache
            result = ___AW_invokeIntroductionPerAspect(methodIndex, parameters);
            return result;
        }
        catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }

    /**
     * Invoke sole mixin instance attached to aspect instance
     *
     * @param methodIndex the method index
     * @param parameters the parameters for the invocation
     * @return the result from the invocation
     */
    private Object ___AW_invokeIntroductionPerAspect(final int methodIndex, final Object[] parameters) {
        //todo move to a container when we have mixin deployment model
        //return m_container.invokeIntroductionPerJvm(methodIndex, parameters);
        Object result = null;
        try {
            result = m_methods[methodIndex].invoke(m_mixinImpl,parameters);
        }
        catch (InvocationTargetException e) {
            throw new WrappedRuntimeException(e.getTargetException());
        }
        catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
        return result;
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
     * Swaps the current introduction implementation.
     *
     * @param className the class name of the new implementation
     */
    public void ___AW_swapImplementation(String className) {
        if (className == null) throw new IllegalArgumentException("class name can not be null");
        synchronized (m_mixinImpl) {
            try {
                Class newImplClass = ContextClassLoader.loadClass(className);//todo pbly old impl.getClassLoader() would be safer
                // verify inroduced interface compatibility
                Class[] newInterfaces = newImplClass.getInterfaces();
                List newInterfaceNames = new ArrayList(newInterfaces.length);
                for (int i = 0; i < newInterfaces.length; i++) {
                    newInterfaceNames.add(newInterfaces[i].getName());
                }
                boolean missingInterface = false;
                String requiredInterfaceName = null;
                for (Iterator i = m_definition.getInterfaceIntroductions().iterator(); i.hasNext();) {
                    requiredInterfaceName = (String)i.next();
                    if ( ! newInterfaceNames.contains(requiredInterfaceName)) {
                        missingInterface = true;
                        break;
                    }
                }
                if (missingInterface) throw new DefinitionException("introduced implementation " + className + " has to implement introduced interface " + requiredInterfaceName);
                m_mixinImpl = newImplClass.newInstance();//TODO : add support for replacement thru inner classes by calling constructor[0](aspect)
                // reregister methods
                synchronized (m_methods) {
                    //TODO unsafe if extra public method gets inserted in
                    m_methods = newImplClass.getDeclaredMethods();
                    Arrays.sort(m_methods, MethodComparator.getInstance(MethodComparator.NORMAL_METHOD));
                    for (int i = 0; i < m_methods.length; i++) {
                        m_methods[i].setAccessible(true);
                    }
                }
            }
            catch (Exception e) {
                throw new WrappedRuntimeException(e);
            }
        }
    }

}
