/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz;

import org.codehaus.aspectwerkz.perx.PerObjectAspect;

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

    private static final String PERTHIS_MODEL_NAME = "perThis";
    private static final String PERTARGET_MODEL_NAME = "perTarget";
    
    private static final String THIS_POINTCUT = "this(" + PerObjectAspect.ADVICE_ARGUMENT_NAME + ")";
    private static final String TARGET_POINTCUT = "target(" + PerObjectAspect.ADVICE_ARGUMENT_NAME + ")";

    private final String m_name;
    private final String m_expression;

    private DeploymentModel(String name) {
        this(name, "");
    }

    private DeploymentModel(String name, String expression) {
        m_name = name;
        m_expression = expression;
    }
    
    public String getDeploymentExpression() {
        return m_expression;
    }
    
    public String toString() {
        if ("".equals(m_expression)) {
            return m_name;
        } else {
            return m_name + "(" + m_expression + ")";
        }
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
        } else if (deploymentModelAsString.equalsIgnoreCase(PER_CFLOW.toString())) {
            return PER_CFLOW;
        } else if (deploymentModelAsString.equalsIgnoreCase(PER_CFLOWBELOW.toString())) {
            return PER_CFLOWBELOW;
        } else if (deploymentModelAsString.toLowerCase().startsWith(PERTHIS_MODEL_NAME.toLowerCase())) {
            return new DeploymentModel(PERTHIS_MODEL_NAME,
                                       getDeploymentExpression(deploymentModelAsString, THIS_POINTCUT)
                                       );
        } else if (deploymentModelAsString.toLowerCase().startsWith(PERTARGET_MODEL_NAME.toLowerCase())) {
            return new DeploymentModel(PERTARGET_MODEL_NAME,
                                       getDeploymentExpression(deploymentModelAsString, TARGET_POINTCUT)
                                       );            
        } else {
            System.out.println(
                    "AW::WARNING - no such deployment model [" + deploymentModelAsString + "] using default (perJVM)"
            );
            return PER_JVM; // falling back to default - PER_JVM
        }
    }
    
    /**
     * @param deploymentModelAsString
     * @return
     */
    private static String getDeploymentExpression(String deploymentModelAsString, 
                                                  final String pointcut) {
        int startIndex = deploymentModelAsString.indexOf('(');
        int endIndex = deploymentModelAsString.lastIndexOf(')');
        
        if(startIndex == -1 || endIndex == -1 || startIndex >= endIndex) {
            System.out.println(
                    "AW::ERROR - wrong deployment model definition [" + deploymentModelAsString +"]");
            
            return "";
        }

        return deploymentModelAsString.substring(startIndex + 1, endIndex).trim()
                + " && "
                + pointcut;
    }
}