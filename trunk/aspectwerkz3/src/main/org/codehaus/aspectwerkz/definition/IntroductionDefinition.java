/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition;

import org.codehaus.aspectwerkz.expression.ExpressionInfo;
import org.codehaus.aspectwerkz.reflect.impl.java.JavaMethodInfo;
import org.codehaus.aspectwerkz.transform.TransformationUtil;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Holds the meta-data for an interface + implementation introduction.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class IntroductionDefinition {
    /**
    * The deployment model for the introduction.
    */
    private final String m_deploymentModel;

    /**
    * The introduced methods info list.
    */
    private final List m_methodsToIntroduce = new ArrayList();

    /**
    * The interface classes name.
    */
    private final List m_interfaceClassNames = new ArrayList();

    /**
    * The name of the interface introduction.
    */
    private final String m_name;

    /**
    * The introduction expressions.
    */
    private ExpressionInfo[] m_expressionInfos;

    /**
    * The attribute for the introduction.
    */
    private String m_attribute = "";

    /**
    * Construct a new Definition for introduction.
    *
    * @param mixinClass          the mixin class
    * @param expressionInfo      the expression info
    * @param deploymentModel     introduction deployment model
    */
    public IntroductionDefinition(final Class mixinClass, final ExpressionInfo expressionInfo,
                                  final String deploymentModel) {
        m_name = mixinClass.getName();
        m_expressionInfos = new ExpressionInfo[] { expressionInfo };
        List sortedMethodList = TransformationUtil.createSortedMethodList(mixinClass);
        for (Iterator iterator = sortedMethodList.iterator(); iterator.hasNext();) {
            m_methodsToIntroduce.add(JavaMethodInfo.getMethodInfo((Method)iterator.next()));
        }
        collectInterfaces(mixinClass);
        m_deploymentModel = deploymentModel;
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
    * Returns the name of the introduction.
    *
    * @return the name
    */
    public String getName() {
        return m_name;
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
    public String getInterfaceClassName() {
        return (String)m_interfaceClassNames.get(0);
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
    * Collects the interfaces from all the base class mixins.
    *
    * @param mixinClass
    */
    private void collectInterfaces(final Class mixinClass) {
        Class[] interfaces = mixinClass.getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            m_interfaceClassNames.add(interfaces[i].getName());
        }
        Class superClass = mixinClass.getSuperclass();
        if (superClass != null) {
            collectInterfaces(superClass);
        }
    }
}
