/**************************************************************************************
 * Copyright (c) The AspectWerkz Team. All rights reserved.                           *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.codehaus.aspectwerkz.xmldef.definition;

import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;

/**
 * Holds the advice definition.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class AdviceDefinition implements Serializable {

    private String m_name;
    private String m_adviceClassName;
    private String m_deploymentModel;
    private String m_isPersistent;
    private String m_attribute = "";
    private Map m_parameters = new HashMap();

    /**
     * Returns the name of the advice.
     *
     * @return the name
     */
    public String getName() {
        return m_name;
    }

    /**
     * Sets the name of the advice.
     *
     * @param name the name
     */
    public void setName(final String name) {
        m_name = name.trim();
    }

    /**
     * Returns the class name of the advice.
     *
     * @return the class name of the advice
     */
    public String getAdviceClassName() {
        return m_adviceClassName;
    }

    /**
     * Sets the class name of the advice.
     *
     * @param adviceClassName the class name of the advice
     */
    public void setAdviceClassName(final String adviceClassName) {
        m_adviceClassName = adviceClassName.trim();
    }

    /**
     * Returns the the deployment model for the advice
     *
     * @return the deployment model
     */
    public String getDeploymentModel() {
        return m_deploymentModel;
    }

    /**
     * Sets the deployment model for the advice.
     *
     * @param deploymentModel the deployment model
     */
    public void setDeploymentModel(final String deploymentModel) {
        m_deploymentModel = deploymentModel.trim();
    }

    /**
     * Gets the persistent attribute.
     *
     * @return the persistent attribute
     */
    public String getIsPersistent() {
        return m_isPersistent;
    }

    /**
     * Sets the persistent attribute.
     *
     * @param isPersistent the persistent attribute
     */
    public void setIsPersistent(final String isPersistent) {
        m_isPersistent = isPersistent;
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
     * Checks if the introduction is persistent.
     *
     * @return true if introduction is persistent
     */
    public boolean isPersistent() {
        if (m_isPersistent != null &&
                (m_isPersistent.equalsIgnoreCase("true") ||
                m_isPersistent.equalsIgnoreCase("yes"))) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Adds a new parameter to the advice.
     *
     * @param name the name of the parameter
     * @param value the value for the parameter
     */
    public void addParameter(final String name, final String value) {
        m_parameters.put(name, value);
    }

    /**
     * Returns the parameters as a Map.
     *
     * @return the parameters
     */
    public Map getParameters() {
        return m_parameters;
    }
}
