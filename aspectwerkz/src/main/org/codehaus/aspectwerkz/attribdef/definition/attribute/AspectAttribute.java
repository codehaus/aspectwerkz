/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.attribdef.definition.attribute;

import org.codehaus.aspectwerkz.DeploymentModel;

/**
 * Attribute for the AspectMetaData construct.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public class AspectAttribute implements Attribute {

    private static final long serialVersionUID = 5565371328658309916L;

    /**
     * The name of the aspect.
     */
    private final String m_name;

    /**
     * The deployment model of the aspect.
     */
    private final String m_deploymentModel;

    /**
     * Create an AspectMetaData attribute.
     *
     * @param name the name of the aspect
     * @param deploymentModel the deployment model for the aspect
     */
    public AspectAttribute(final String name, final String deploymentModel) {
        m_name = name;
        if (deploymentModel == null || deploymentModel.equals("")) {
            m_deploymentModel = "perJVM";
        }
        else {
            m_deploymentModel = deploymentModel;
        }
        verify();
    }

    /**
     * Create an AspectMetaData attribute.
     *
     * @param name the name of the aspect
     * @param deploymentModel the deployment model for the aspect
     */
    public AspectAttribute(final String name, final int deploymentModel) {
        m_name = name;
        m_deploymentModel = DeploymentModel.getDeploymentModelAsString(deploymentModel);
        verify();
    }

    /**
     * Returns the name of the aspect.
     *
     * @return the name
     */
    public String getName() {
        return m_name;
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
     * Verifies that the deployment model is valid.
     */
    private void verify() {
        if (!m_deploymentModel.equalsIgnoreCase("perJVM") &&
                !m_deploymentModel.equalsIgnoreCase("perClass") &&
                !m_deploymentModel.equalsIgnoreCase("perInstance") &&
                !m_deploymentModel.equalsIgnoreCase("perThread")) {
            throw new IllegalArgumentException("deployment model is not valid for aspect");
        }
    }
}
