/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.definition.attribute;

import attrib4j.Attribute;

/**
 * Attribute for the Aspect construct.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class AspectAttribute implements Attribute {

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
            m_deploymentModel = "perJVM";

//        if (deploymentModel == null) {
//            m_deploymentModel = "perJVM";
//        }
//        else {
//            m_deploymentModel = deploymentModel;
//        }
//        if (m_deploymentModel.equalsIgnoreCase("perJVM") ||
//                m_deploymentModel.equalsIgnoreCase("perClass") ||
//                m_deploymentModel.equalsIgnoreCase("perInstance") ||
//                m_deploymentModel.equalsIgnoreCase("perThread")) {
//            throw new IllegalArgumentException("deployment model is not valid for aspect");
//        }
    }

    /**
     * Returns the deployment model.
     *
     * @return the deployment model
     */
    public String getDeploymentModel() {
        return m_deploymentModel;
    }
}
