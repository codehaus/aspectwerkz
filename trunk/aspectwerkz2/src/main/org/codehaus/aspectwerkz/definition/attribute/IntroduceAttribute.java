/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition.attribute;

/**
 * Attribute for the inner class Introduction construct.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class IntroduceAttribute implements Attribute {

    private static final long serialVersionUID = -146743510655018866L;

    /**
     * The expression for the introduction.
     */
    private final String m_expression;

    /**
     * The FQN of the inner class for default introduction impl.
     */
    private final String m_innerClassName;

    /**
     * The FQN of interface implemented by the inner class.
     */
    private final String[] m_introducedInterfaceNames;

    /**
     * Deployment model for the mixin
     */
    private final String m_deploymentModel;

    /**
     * Create an Introduction attribute.
     *
     * @param expression      the expression for the introduction
     * @param innerClassName
     * @param interfaceNames
     * @param deploymentModel the deployment model for the aspect
     */
    public IntroduceAttribute(final String expression, final String innerClassName, final String[] interfaceNames, final String deploymentModel) {
        if (expression == null) throw new IllegalArgumentException("expression is not valid for introduction");
        m_expression = expression;
        m_innerClassName = innerClassName;
        m_introducedInterfaceNames = interfaceNames;
        if (deploymentModel == null || deploymentModel.equals("")) {
            m_deploymentModel = null;//will follow aspect deployment model at prototype creation time
            //todo could AspectC should be able to handle this "mixin follows aspect deploy model" ?
        }
        else {
            m_deploymentModel = deploymentModel;
        }
        verify();
    }

    /**
     * Return the expression.
     *
     * @return the expression
     */
    public String getExpression() {
        return m_expression;
    }

    public String getInnerClassName() {
        return m_innerClassName;
    }

    public String[] getIntroducedInterfaceNames() {
        return m_introducedInterfaceNames;
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
     * Verifies that the deployment model is valid. TODO verify according to Aspect DM
     */
    private void verify() {
        if (m_deploymentModel != null &&
                !m_deploymentModel.equalsIgnoreCase("perJVM") &&
                !m_deploymentModel.equalsIgnoreCase("perClass") &&
                !m_deploymentModel.equalsIgnoreCase("perInstance") &&
                !m_deploymentModel.equalsIgnoreCase("perThread")) {
            throw new IllegalArgumentException("deployment model is not valid for mixin");
        }
    }
}
