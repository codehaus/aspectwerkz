/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition;

import org.codehaus.aspectwerkz.expression.ExpressionInfo;
import org.codehaus.aspectwerkz.reflect.ClassInfo;
import org.codehaus.aspectwerkz.reflect.ClassInfoHelper;
import org.codehaus.aspectwerkz.reflect.MethodInfo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Definition for the mixin construct.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public class MixinDefinition {
    /**
     * The deployment model for the mixin.
     */
    private String m_deploymentModel;

    /**
     * Flags the mixin as transient.
     */
    private boolean m_isTransient;

    /**
     * The introduced methods info list.
     */
    private final List m_methodsToIntroduce = new ArrayList();

    /**
     * The interface classes name.
     */
    private final List m_interfaceClassInfos = new ArrayList();

    /**
     * The class info for the mixin.
     */
    private final ClassInfo m_mixinImpl;

    /**
     * The mixin expressions.
     */
    private ExpressionInfo[] m_expressionInfos = new ExpressionInfo[]{};

    /**
     * The attribute for the mixin.
     */
    private String m_attribute = "";

    /**
     * The factory class name.
     */
    private String m_factoryClassName;

    /**
     * The system definition.
     */
    private SystemDefinition m_systemDefinition;

    /**
     * Construct a new definition for mixin.
     *
     * @param mixinClass      the mixin class
     * @param deploymentModel mixin deployment model
     * @param isTransient transient flag
     * @param systemDef the system definition
     */
    public MixinDefinition(final ClassInfo mixinClass,
                           final String deploymentModel,
                           final boolean isTransient,
                           final SystemDefinition systemDef) {
        m_mixinImpl = mixinClass;
        m_systemDefinition = systemDef;
        m_expressionInfos = new ExpressionInfo[]{};
        List interfaceDeclaredMethods = collectMethodsFromInterfaces(mixinClass);
        List sortedMethodList = ClassInfoHelper.createInterfaceDefinedSortedMethodList(
                mixinClass, interfaceDeclaredMethods
        );
        for (Iterator iterator = sortedMethodList.iterator(); iterator.hasNext();) {
            m_methodsToIntroduce.add((MethodInfo) iterator.next());
        }
        m_deploymentModel = deploymentModel;
        m_isTransient = isTransient;
    }

    /**
     * Sets the factory class name.
     *
     * @param factoryClassName
     */
    public void setFactoryClassName(final String factoryClassName) {
        m_factoryClassName = factoryClassName;
    }

    /**
     * Returns the factory class name.
     *
     * @return
     */
    public String getFactoryClassName() {
        return m_factoryClassName;
    }
    /**
     * Returns the methods to introduce.
     *
     * @return the methods to introduce
     */
    public List getMethodsToIntroduce() {
        return m_methodsToIntroduce;
    }

    /**
     * Returns the deployment model.
     *
     * @return the deployment model
     */
    public String getDeploymentModel() {
        return m_deploymentModel;
    }

    /**
     * Sets the deployment model.
     *
     * @param deploymentModel
     */
    public void setDeploymentModel(final String deploymentModel) {
        m_deploymentModel = deploymentModel;
    }

    /**
     * Checks if the mixin is transient.
     *
     * @return
     */
    public boolean isTransient() {
        return m_isTransient;
    }

    /**
     * Sets the mixin as transient.
     *
     * @param isTransient
     */
    public void setTransient(boolean isTransient) {
        m_isTransient = isTransient;
    }

    /**
     * Returns the class info for the mixin impl.
     *
     * @return the class info
     */
    public ClassInfo getMixinImpl() {
        return m_mixinImpl;
    }

    /**
     * Returns the expressions.
     *
     * @return the expressions array
     */
    public ExpressionInfo[] getExpressionInfos() {
        return m_expressionInfos;
    }

    /**
     * Returns the class name of the interface.
     *
     * @return the class name of the interface
     */
    public List getInterfaces() {
        return m_interfaceClassInfos;
    }

    /**
     * Returns the attribute.
     *
     * @return the attribute
     */
    public String getAttribute() {
        return m_attribute;
    }

    /**
     * Sets the attribute.
     *
     * @param attribute the attribute
     */
    public void setAttribute(final String attribute) {
        m_attribute = attribute;
    }

    /**
     * Returns the system definition.
     *
     * @return the system definition
     */
    public SystemDefinition getSystemDefinition() {
        return m_systemDefinition;
    }

    /**
     * Adds a new expression info.
     *
     * @param expression a new expression info
     */
    public void addExpressionInfo(final ExpressionInfo expression) {
        final ExpressionInfo[] tmpExpressions = new ExpressionInfo[m_expressionInfos.length + 1];
        java.lang.System.arraycopy(m_expressionInfos, 0, tmpExpressions, 0, m_expressionInfos.length);
        tmpExpressions[m_expressionInfos.length] = expression;
        m_expressionInfos = new ExpressionInfo[m_expressionInfos.length + 1];
        java.lang.System.arraycopy(tmpExpressions, 0, m_expressionInfos, 0, tmpExpressions.length);
    }

    /**
     * Adds an array with new expression infos.
     *
     * @param expressions an array with new expression infos
     */
    public void addExpressionInfos(final ExpressionInfo[] expressions) {
        final ExpressionInfo[] tmpExpressions = new ExpressionInfo[m_expressionInfos.length + expressions.length];
        java.lang.System.arraycopy(m_expressionInfos, 0, tmpExpressions, 0, m_expressionInfos.length);
        java.lang.System.arraycopy(expressions, 0, tmpExpressions, m_expressionInfos.length, expressions.length);
        m_expressionInfos = new ExpressionInfo[m_expressionInfos.length + expressions.length];
        java.lang.System.arraycopy(tmpExpressions, 0, m_expressionInfos, 0, tmpExpressions.length);
    }

    /**
     * Collects the interfaces from all the base class mixins and the methods in the mixin interfaces
     *
     * @param mixinClass
     * @return list of methods declared in given class interfaces
     */
    private List collectMethodsFromInterfaces(final ClassInfo mixinClass) {
        final List interfaceDeclaredMethods = new ArrayList();
        ClassInfo[] interfaces = mixinClass.getInterfaces();

        // grab methods from all interfaces and their super interfaces
        for (int i = 0; i < interfaces.length; i++) {
            m_interfaceClassInfos.add(interfaces[i]);
            final List sortedMethodList = ClassInfoHelper.createSortedMethodList(interfaces[i]);
            for (Iterator it = sortedMethodList.iterator(); it.hasNext();) {
                MethodInfo methodInfo = (MethodInfo) it.next();
                if (methodInfo.getDeclaringType().getName().equals("java.lang.Object")) {
                    continue;
                }
                interfaceDeclaredMethods.add(methodInfo);
            }
        }
        // grab methods from all super classes' interfaces 
        ClassInfo superClass = mixinClass.getSuperclass();
        if (superClass != null && !superClass.getName().equals("java.lang.Object")) {
            interfaceDeclaredMethods.addAll(collectMethodsFromInterfaces(superClass));
        }
        return interfaceDeclaredMethods;
    }
}