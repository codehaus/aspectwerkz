/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition.attribute;

import attrib4j.Attribute;

import org.codehaus.aspectwerkz.DeploymentModel;

/**
 * Attribute for the Aspect construct.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public class AspectAttribute implements Attribute {

    private static final long serialVersionUID = 1L;

    /**
     * The deployment model of the aspect.
     */
    private String m_deploymentModel;

    /**
     * Create an Aspect attribute.
     *
     * @param deploymentModel the deployment model for the aspect
     */
    public AspectAttribute(final String deploymentModel) {
        if (deploymentModel == null) {
            m_deploymentModel = "perJVM";
        }
        else {
            m_deploymentModel = deploymentModel;
        }
        verify();
    }

    /**
     * Create an Aspect attribute.
     *
     * @param deploymentModel the deployment model for the aspect
     */
    public AspectAttribute(final int deploymentModel) {
        m_deploymentModel = DeploymentModel.getDeploymentModelAsString(deploymentModel);
        verify();
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
