/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
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
import org.codehaus.aspectwerkz.reflect.impl.asm.AsmClassInfo;
import org.codehaus.aspectwerkz.DeploymentModel;
import org.codehaus.aspectwerkz.transform.TransformationConstants;
import org.codehaus.aspectwerkz.delegation.AdvisableImpl;
import org.codehaus.aspectwerkz.delegation.Advisable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.lang.ref.WeakReference;

/**
 * Definition for the mixin construct.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r </a>
 */
public class MixinDefinition {
    /**
     * The deployment model for the mixin.
     */
    private DeploymentModel m_deploymentModel;

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
    private final List m_interfaceClassNames = new ArrayList();

    /**
     * The class name for the mixin impl.
     */
    private final String m_mixinImplClassName;

    /**
     * The class loader.
     */
    private final WeakReference m_loaderRef;

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
     * @param isTransient     transient flag
     * @param systemDef       the system definition
     */
    public MixinDefinition(ClassInfo mixinClass,
                           final DeploymentModel deploymentModel,
                           final boolean isTransient,
                           final SystemDefinition systemDef) {
        if (isSystemMixin(mixinClass)) {
            mixinClass = defineSystemMixin(mixinClass.getClassLoader());
        } else {
            ClassInfo[] interfaces = mixinClass.getInterfaces();
            for (int i = 0; i < interfaces.length; i++) {
                m_interfaceClassNames.add(interfaces[i].getName());
            }

            List interfaceDeclaredMethods = ClassInfoHelper.collectMethodsFromInterfacesImplementedBy(mixinClass);
            List sortedMethodList = ClassInfoHelper.createInterfaceDefinedSortedMethodList(
                    mixinClass, interfaceDeclaredMethods
            );
            for (Iterator iterator = sortedMethodList.iterator(); iterator.hasNext();) {
                MethodInfo methodInfo = (MethodInfo) iterator.next();
                m_methodsToIntroduce.add(methodInfo);
            }
        }

        m_mixinImplClassName = mixinClass.getName();
        m_loaderRef = new WeakReference(mixinClass.getClassLoader());
        m_systemDefinition = systemDef;
        m_expressionInfos = new ExpressionInfo[]{};

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
    public DeploymentModel getDeploymentModel() {
        return m_deploymentModel;
    }

    /**
     * Sets the deployment model.
     *
     * @param deploymentModel
     */
    public void setDeploymentModel(final DeploymentModel deploymentModel) {
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
        return AsmClassInfo.getClassInfo(m_mixinImplClassName, (ClassLoader) m_loaderRef.get());
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
    public List getInterfaceClassNames() {
        return m_interfaceClassNames;
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
     * Defines system mixins.
     *
     * @param loader
     * @return
     */
    private ClassInfo defineSystemMixin(final ClassLoader loader) {
        // if advisable impl mixin get the class info from the AsmClassInfo to keep the methods starting with aw$
        ClassInfo mixinClass = AsmClassInfo.getClassInfo(AdvisableImpl.class.getName(), loader);
        MethodInfo[] methods = mixinClass.getMethods();
        for (int i = 0; i < methods.length; i++) {
            MethodInfo method = methods[i];
            if (method.getName().startsWith(TransformationConstants.SYNTHETIC_MEMBER_PREFIX)) {
                m_methodsToIntroduce.add(method);
            }
        }
        m_interfaceClassNames.add(Advisable.class.getName());
        return mixinClass;
    }

    /**
     * Checks if the mixin is a system mixin.
     *
     * @param mixinClass
     * @return
     */
    private boolean isSystemMixin(final ClassInfo mixinClass) {
        return mixinClass.getName().equals(AdvisableImpl.class.getName());
    }
}