/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz;

import org.codehaus.aspectwerkz.exception.DefinitionException;

/**
 * Enum containing the different deployment model types.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public final class DeploymentModel {

    public static final DeploymentModel PER_JVM = new DeploymentModel("perJVM");
    public static final DeploymentModel PER_CLASS = new DeploymentModel("perClass");
    public static final DeploymentModel PER_INSTANCE = new DeploymentModel("perInstance");
    public static final DeploymentModel PER_TARGET = new DeploymentModel("perTarget");
    public static final DeploymentModel PER_THIS = new DeploymentModel("perThis");
    public static final DeploymentModel PER_CFLOW = new DeploymentModel("perCflow");
    public static final DeploymentModel PER_CFLOWBELOW = new DeploymentModel("perCflowbelow");

    private final String m_name;

    private DeploymentModel(String name) {
        m_name = name;
    }

    public String toString() {
        return m_name;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DeploymentModel)) {
            return false;
        }
        final DeploymentModel adviceType = (DeploymentModel) o;
        if ((m_name != null) ? (!m_name.equals(adviceType.m_name)) : (adviceType.m_name != null)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return ((m_name != null) ? m_name.hashCode() : 0);
    }

    public static DeploymentModel getDeploymentModelFor(final String deploymentModelAsString) {
        if (deploymentModelAsString == null || deploymentModelAsString.equals("")) {
            return PER_JVM; // default is PER_JVM
        }
        if (deploymentModelAsString.equalsIgnoreCase(PER_JVM.toString())) {
            return PER_JVM;
        } else if (deploymentModelAsString.equalsIgnoreCase(PER_CLASS.toString())) {
            return PER_CLASS;
        } else if (deploymentModelAsString.equalsIgnoreCase(PER_INSTANCE.toString())) {
            return PER_INSTANCE;
        } else if (deploymentModelAsString.equalsIgnoreCase(PER_TARGET.toString())) {
            return PER_TARGET;
        } else if (deploymentModelAsString.equalsIgnoreCase(PER_THIS.toString())) {
            return PER_THIS;
        } else if (deploymentModelAsString.equalsIgnoreCase(PER_CFLOW.toString())) {
            return PER_CFLOW;
        } else if (deploymentModelAsString.equalsIgnoreCase(PER_CFLOWBELOW.toString())) {
            return PER_CFLOWBELOW;
        } else {
            System.out.println(
                    "AW::WARNING - no such deployment model [" + deploymentModelAsString + "] using default (perJVM)"
            );
            return PER_JVM; // falling back to default - PER_JVM
        }
    }
}